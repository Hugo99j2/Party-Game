package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.hugo99j.chaosparty.ui.ControllerInput;
import com.hugo99j.chaosparty.ui.ControllerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(JamepadController.class)
public class ControllerUtilMixin implements ControllerUtil {
    @Unique
    private final List<ControllerInput> wasPressedValues = new ArrayList<>();

    @Override
    public float getValue(ControllerInput input) {
        //noinspection usagelimited
        return input.getGetValue().apply((Controller) this);
    }

    @Override
    public boolean isPressed(ControllerInput input) {
        //noinspection usagelimited
        return input.getGetPressed().apply((Controller) this);
    }

    @Override
    public boolean wasJustPressed(ControllerInput input) {
        boolean actuallyPressed = isPressed(input);
        boolean wasPressed = wasPressedValues.contains(input);

        if(actuallyPressed && !wasPressed) {
            wasPressedValues.add(input);
            return true;
        }
        if(!actuallyPressed && wasPressed) {
            wasPressedValues.remove(input);
        }
        return false;
    }
}
