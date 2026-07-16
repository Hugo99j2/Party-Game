package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.controllers.ControllerMapping;
import com.hugo99j.chaosparty.util.DummyController;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ControllerMapping.class)
public class ControllerMappingAccessor {
    public ControllerMappingAccessor(int axisLeftX, int axisLeftY, int axisRightX, int axisRightY,
                                     int buttonA, int buttonB, int buttonX, int buttonY, int buttonBack, int buttonStart,
                                     int buttonL1, int buttonL2, int buttonR1, int buttonR2,
                                     int buttonLeftStick, int buttonRightStick,
                                     int buttonDpadUp, int buttonDpadDown, int buttonDpadLeft, int buttonDpadRight) {
    }

    static {
        DummyController.DUMMY_MAPPING = (ControllerMapping) (Object) new ControllerMappingAccessor(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
