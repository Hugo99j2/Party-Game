package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ControllerInput;
import com.hugo99j.chaosparty.util.ControllerUtil;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(JamepadController.class)
public abstract class ControllerUtilMixin implements ControllerUtil {
    @Shadow
    public abstract void startVibration(int duration, float strength);

    @Unique
    private EnumMap<ControllerInput, ValueHolder<Float>> wasPressedValues;
    @Unique
    private final List<ControllerInput> happenedThisTick = new ArrayList<>();
    @Unique
    float lastUpdate;
    @Unique
    private static final float NOT_PRESSED = Float.MIN_VALUE;

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
        }
    }

    @Override
    public void onTick() {
        happenedThisTick.clear();
    }

    @SuppressWarnings("usagelimited")
    @Override
    public void update() {
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
    }

    @Override
    public void vibrate(int time, float intensity) {
        this.startVibration(time, intensity);
    }
}
