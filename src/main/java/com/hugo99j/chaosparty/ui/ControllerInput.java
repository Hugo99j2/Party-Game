package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controller;
import com.daniel99j.djutil.UsageLimited;

import java.util.function.Function;

import static com.hugo99j.chaosparty.ui.ControllerUtil.DEAD_ZONE;

public enum ControllerInput {
    LEFT_STICK_UP(
        c -> -c.getAxis(c.getMapping().axisLeftY),
        c -> c.getAxis(c.getMapping().axisLeftY) < -DEAD_ZONE,
        0.5f),
    LEFT_STICK_DOWN(
        c -> c.getAxis(c.getMapping().axisLeftY),
        c -> c.getAxis(c.getMapping().axisLeftY) > DEAD_ZONE,
        0.5f),
    LEFT_STICK_RIGHT(
        c -> c.getAxis(c.getMapping().axisLeftX),
        c -> c.getAxis(c.getMapping().axisLeftX) > DEAD_ZONE,
        0.5f),
    LEFT_STICK_LEFT(
        c -> -c.getAxis(c.getMapping().axisLeftX),
        c -> c.getAxis(c.getMapping().axisLeftX) < -DEAD_ZONE,
        0.5f),
    LEFT_STICK_ANY(
        c -> (c.getAxis(c.getMapping().axisLeftX) < -DEAD_ZONE || c.getAxis(c.getMapping().axisLeftX) > DEAD_ZONE || c.getAxis(c.getMapping().axisLeftY) < -DEAD_ZONE || c.getAxis(c.getMapping().axisLeftY) > DEAD_ZONE) ? 1f : 0f,
        c -> c.getAxis(c.getMapping().axisLeftX) < -DEAD_ZONE || c.getAxis(c.getMapping().axisLeftX) > DEAD_ZONE || c.getAxis(c.getMapping().axisLeftY) < -DEAD_ZONE || c.getAxis(c.getMapping().axisLeftY) > DEAD_ZONE,
        0.5f),

    LEFT_STICK_BUTTON(
        c -> c.getButton(c.getMapping().buttonLeftStick) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonLeftStick),
        -0.5f),

    RIGHT_STICK_UP(
        c -> -c.getAxis(c.getMapping().axisRightY),
        c -> c.getAxis(c.getMapping().axisRightY) < -DEAD_ZONE,
        0.5f),
    RIGHT_STICK_DOWN(
        c -> c.getAxis(c.getMapping().axisRightY),
        c -> c.getAxis(c.getMapping().axisRightY) > DEAD_ZONE,
        0.5f),
    RIGHT_STICK_RIGHT(
        c -> c.getAxis(c.getMapping().axisRightX),
        c -> c.getAxis(c.getMapping().axisRightX) > DEAD_ZONE,
        0.5f),
    RIGHT_STICK_LEFT(
        c -> -c.getAxis(c.getMapping().axisRightX),
        c -> c.getAxis(c.getMapping().axisRightX) < -DEAD_ZONE,
        0.5f),

    RIGHT_STICK_BUTTON(
        c -> c.getButton(c.getMapping().buttonRightStick) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonRightStick),
        -1),

    A(
        c -> c.getButton(c.getMapping().buttonA) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonA),
        -1),
    B(
        c -> c.getButton(c.getMapping().buttonB) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonB),
        -1),
    X(
        c -> c.getButton(c.getMapping().buttonX) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonX),
        -1),
    Y(
        c -> c.getButton(c.getMapping().buttonY) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonY),
        -1),

    DPAD_UP(
        c -> c.getButton(c.getMapping().buttonDpadUp) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadUp),
        -1),
    DPAD_DOWN(
        c -> c.getButton(c.getMapping().buttonDpadDown) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadDown),
        -1),
    DPAD_RIGHT(
        c -> c.getButton(c.getMapping().buttonDpadRight) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadRight),
        -1),
    DPAD_LEFT(
        c -> c.getButton(c.getMapping().buttonDpadLeft) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadLeft),
        -1),

    MENU(
        c -> c.getButton(c.getMapping().buttonStart) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonStart),
        -1),

    SCREEN(
        c -> c.getButton(c.getMapping().buttonBack) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonBack),
        -1),

    LEFT_BUMPER(
        c -> c.getButton(c.getMapping().buttonL1) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonL1),
        -1),

    RIGHT_BUMPER(
        c -> c.getButton(c.getMapping().buttonR1) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonR1),
        -1),

    LEFT_TRIGGER(
        c -> c.getAxis(5),
        c -> c.getAxis(5) > DEAD_ZONE,
        -1),

    RIGHT_TRIGGER(
        c -> c.getAxis(6),
        c -> c.getAxis(6) > DEAD_ZONE,
        -1);

    private final Function<Controller, Float> getValue;
    private final Function<Controller, Boolean> getPressed;
    private final float reInputTimer;

    ControllerInput(Function<Controller, Float> getValue, Function<Controller, Boolean> getPressed, float reInputTimer) {
        this.getValue = getValue;
        this.getPressed = getPressed;
        this.reInputTimer = reInputTimer;
    }

    @UsageLimited
    public float getReInputTimer() {
        return reInputTimer;
    }

    @UsageLimited
    public Function<Controller, Float> getGetValue() {
        return getValue;
    }

    @UsageLimited
    public Function<Controller, Boolean> getGetPressed() {
        return getPressed;
    }
}
