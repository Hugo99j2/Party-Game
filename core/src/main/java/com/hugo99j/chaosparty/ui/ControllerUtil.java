package com.hugo99j.chaosparty.ui;

public interface ControllerUtil {
    public boolean wasJustPressed(ControllerInput input);
    public boolean isPressed(ControllerInput input);
    public float getValue(ControllerInput input);
}
