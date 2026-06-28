package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.ControllerInput;
import com.hugo99j.chaosparty.ui.ControllerUtil;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(JamepadController.class)
public class ControllerUtilMixin implements ControllerUtil {
    @Unique
    private final Map<ControllerInput, ValueHolder<Float>> wasPressedValues = new HashMap<>();
    @Unique
    private float lastUpdate = 0;

    @Override
    public float getValue(ControllerInput input) {
        if(!RenderUtil.isFocused()) return 0;
        //noinspection usagelimited
        return input.getGetValue().apply((Controller) this);
    }

    @Override
    public boolean isPressed(ControllerInput input) {
        //noinspection usagelimited
        return input.getGetPressed().apply((Controller) this) && RenderUtil.isFocused();
    }

    @Override
    public boolean wasJustPressed(ControllerInput input) {
        boolean actuallyPressed = isPressed(input);
        boolean wasPressed = wasPressedValues.containsKey(input);

        if(actuallyPressed && !wasPressed) {
            //noinspection usagelimited
            wasPressedValues.put(input, new ValueHolder<>(input.getReInputTimer()));
            return true;
        }
        if(!actuallyPressed && wasPressed) {
            wasPressedValues.remove(input);
        }

        if(lastUpdate != GameData.time) {
            float diff = GameData.time - lastUpdate;
            lastUpdate = GameData.time;
            List<ControllerInput> toRemove = new ArrayList<>();
            wasPressedValues.forEach((key, value) -> {
                value.object-=diff;
                //noinspection usagelimited
                if(value.object <= 0 && key.getReInputTimer() != -1) toRemove.add(key);
            });
            toRemove.forEach(wasPressedValues.keySet()::remove);
        }
        return false;
    }
}
