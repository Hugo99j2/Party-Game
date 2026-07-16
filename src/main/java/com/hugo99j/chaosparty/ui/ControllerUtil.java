package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controllers;
import com.hugo99j.chaosparty.util.DummyController;

public interface ControllerUtil {
    boolean wasJustPressed(ControllerInput input);
    boolean isPressed(ControllerInput input);
    float getValue(ControllerInput input);

    float DEAD_ZONE = 0.25f;

    static ControllerUtil getCurrent() {
        if(Controllers.getCurrent() == null) return new DummyController();
        return (ControllerUtil) Controllers.getCurrent();
    }
}
