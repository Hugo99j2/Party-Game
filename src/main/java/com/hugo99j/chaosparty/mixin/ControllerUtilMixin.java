package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.ControllerInput;
import com.hugo99j.chaosparty.ui.ControllerUtil;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(JamepadController.class)
public class ControllerUtilMixin implements ControllerUtil {
    @Unique
    private EnumMap<ControllerInput, ValueHolder<Float>> wasPressedValues;
    @Unique
    float lastUpdate;
    @Unique
    private static final float NOT_PRESSED = Float.MIN_VALUE;

    @Override
    public float getValue(ControllerInput input) {
        tick();
        if(!RenderUtil.isFocused()) return 0;
        //noinspection usagelimited
        return input.getGetValue().apply((Controller) this);
    }

    @Override
    public boolean isPressed(ControllerInput input) {
        tick();
        //noinspection usagelimited
        return input.getGetPressed().apply((Controller) this) && RenderUtil.isFocused();
    }

    @Override
    public boolean wasJustPressed(ControllerInput input) {
        tick();
        return wasPressedValues.get(input).object.equals(GameData.time);
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

    @Unique
    @SuppressWarnings("usagelimited")
    private void tick() {
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
        }
    }
}
