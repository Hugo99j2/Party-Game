package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.controllers.Controllers;
import com.hugo99j.chaosparty.ui.debugger.DebugController;

public interface ControllerUtil {
    boolean wasJustPressed(ControllerInput input);
    boolean isPressed(ControllerInput input);
    float getValue(ControllerInput input);
    boolean wasJustPressedThisTick(ControllerInput input);
    void onTick();
    void updateUtil();
    void vibrate(VibrationAmount calculator);
    void vibrate(VibrationAmount calculator, float maxTime);
    void vibrate(float intensity, float maxTime);
    float[] getCurrentVibration();

    float DEAD_ZONE = 0.25f;

    static ControllerUtil getCurrent() {
        if(Controllers.getCurrent() == null) {
            if(DebugController.INSTANCE != null) return (ControllerUtil) DebugController.INSTANCE;
            return new DummyController();
        }
        return (ControllerUtil) Controllers.getCurrent();
    }
}
