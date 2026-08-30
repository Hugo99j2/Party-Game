package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.badlogic.gdx.utils.TimeUtils;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.*;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(JamepadController.class)
public abstract class ControllerUtilMixin implements ControllerUtil {

    @Shadow
    private ControllerIndex controllerIndex;
    @Shadow
    private long vibrationEndMs;

    @Shadow
    public abstract void setDisconnected();

    @Shadow
    private Boolean canVibrate;
    @Unique
    private EnumMap<ControllerInput, ValueHolder<Float>> wasPressedValues;
    @Unique
    private final List<ControllerInput> happenedThisTick = new ArrayList<>();
    @Unique
    private float lastUpdate;
    @Unique
    private final List<VibrationInstance> vibrationSuppliers = new ArrayList<>();
    @Unique
    private static final float NOT_PRESSED = Float.MIN_VALUE;
    @Unique
    private final float[] previousVibration = {0f, 0f};
    @Unique
    //This fixes the controller queueing vibrate updates!
    private float controllerVibrateCooldown = 0;
    @Unique
    private float vibrateInterval;

    @Override
    public float getValue(ControllerInput input) {
        init();
        if(!RenderUtil.isFocused()) return 0;
        //noinspection usagelimited
        return input.getGetValue().apply((Controller) this);
    }

    @Override
    public boolean isPressed(ControllerInput input) {
        init();
        //noinspection usagelimited
        return input.getGetPressed().apply((Controller) this) && RenderUtil.isFocused();
    }

    @Override
    public boolean wasJustPressed(ControllerInput input) {
        init();
        return wasPressedValues.get(input).object.equals(GameData.time);
    }

    @Override
    public boolean wasJustPressedThisTick(ControllerInput input) {
        init();
        return happenedThisTick.contains(input);
    }

    @Unique
    private void init() {
        if(wasPressedValues == null) {
            wasPressedValues = new EnumMap<>(ControllerInput.class);
            for (ControllerInput value : ControllerInput.values()) {
                wasPressedValues.put(value, new ValueHolder<>(NOT_PRESSED));
            }

            if(((JamepadController) (Object) this).getName().equals("Xbox Series Controller") && (((JamepadController) (Object) this).getMaxButtonIndex() == 15)) {
                this.vibrateInterval = (1f/Gdx.graphics.getDisplayMode().refreshRate)*2f;
                Logger.info("Decreasing vibrate rate to "+this.vibrateInterval+" as controller is 3rd gen");
            } else {
                this.vibrateInterval = (1f/Gdx.graphics.getDisplayMode().refreshRate);
            }
        }
    }

    @Override
    public void onTick() {
        happenedThisTick.clear();
    }

    @SuppressWarnings("usagelimited")
    @Override
    public void updateUtil() {
        init();
        if(lastUpdate == GameData.time) return;
        lastUpdate = GameData.time;
        for (ControllerInput value : ControllerInput.values()) {
            boolean actuallyPressed = isPressed(value);
            boolean oldValue = wasPressedValues.get(value).object != NOT_PRESSED;
            if(!actuallyPressed && oldValue) {
                wasPressedValues.get(value).object = NOT_PRESSED;
            } else if(actuallyPressed && !oldValue) {
                wasPressedValues.get(value).object = GameData.time;
            } else if(value.getReInputTimer() != -1 && oldValue && GameData.time - wasPressedValues.get(value).object > value.getReInputTimer()) {
                wasPressedValues.get(value).object = GameData.time;
            }

            if(actuallyPressed && !oldValue) happenedThisTick.add(value);
        }

        controllerVibrateCooldown += Gdx.graphics.getDeltaTime();

        if(controllerVibrateCooldown > vibrateInterval) {
            controllerVibrateCooldown = 0;
            float leftMax = 0;
            float rightMax = 0;
            List<VibrationInstance> toRemove = new ArrayList<>();
            for (VibrationInstance v : vibrationSuppliers) {
                float time = GameData.time - v.startTime;
                float left = v.vibrationSupplier.getIntense(time);
                float right = v.vibrationSupplier.getFast(time);
                if (left > leftMax) leftMax = left;
                if (right > rightMax) rightMax = right;
                if (left == -1 || right == -1 || v.maxTime + v.startTime < GameData.time) {
                    toRemove.add(v);
                }
            }
            vibrationSuppliers.removeAll(toRemove);

            if (leftMax > 1) {
                Logger.error("Vibration was set too large: " + leftMax);
                leftMax = 1;
            }
            if (rightMax > 1) {
                Logger.error("Vibration was set too large: " + rightMax);
                rightMax = 1;
            }

            if (leftMax != previousVibration[0] || rightMax != previousVibration[1]) {
                try {
                    if (controllerIndex.doVibration(leftMax, rightMax, 100000)) {
                        vibrationEndMs = TimeUtils.millis() + 100000;
                        canVibrate = true;
                    }
                } catch (ControllerUnpluggedException | NullPointerException e) {
                    setDisconnected();
                }
            }

            previousVibration[0] = leftMax;
            previousVibration[1] = rightMax;
        }
    }

    @Override
    public void vibrate(VibrationAmount calculator) {
        vibrate(calculator, 999999999);
    }

    @Override
    public void vibrate(VibrationAmount calculator, float maxTime) {
        this.vibrationSuppliers.add(new VibrationInstance(GameData.time, calculator, maxTime));

    }

    @Override
    public void vibrate(float intensity, float maxTime) {
        vibrate(VibrationAmount.of(intensity), maxTime);
    }

    @Override
    public float[] getCurrentVibration() {
        return previousVibration;
    }

    private record VibrationInstance(float startTime, VibrationAmount vibrationSupplier, float maxTime) {

    }
}
