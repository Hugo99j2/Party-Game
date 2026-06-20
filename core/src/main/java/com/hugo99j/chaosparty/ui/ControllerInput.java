package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controller;
import com.daniel99j.djutil.UsageLimited;

import java.util.function.Function;

public enum ControllerInput {
    LEFT_STICK_UP(
        c -> -c.getAxis(c.getMapping().axisLeftY),
        c -> c.getAxis(c.getMapping().axisLeftY) < -0.25f
    ),
    LEFT_STICK_DOWN(
        c -> c.getAxis(c.getMapping().axisLeftY),
        c -> c.getAxis(c.getMapping().axisLeftY) > 0.25f
    ),
    LEFT_STICK_RIGHT(
        c -> c.getAxis(c.getMapping().axisLeftX),
        c -> c.getAxis(c.getMapping().axisLeftX) > 0.25f
    ),
    LEFT_STICK_LEFT(
        c -> -c.getAxis(c.getMapping().axisLeftX),
        c -> c.getAxis(c.getMapping().axisLeftX) < -0.25f
    ),

    LEFT_STICK_BUTTON(
        c -> c.getButton(c.getMapping().buttonLeftStick) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonLeftStick)
    ),

    RIGHT_STICK_UP(
        c -> -c.getAxis(c.getMapping().axisRightY),
        c -> c.getAxis(c.getMapping().axisRightY) < -0.25f
    ),
    RIGHT_STICK_DOWN(
        c -> c.getAxis(c.getMapping().axisRightY),
        c -> c.getAxis(c.getMapping().axisRightY) > 0.25f
    ),
    RIGHT_STICK_RIGHT(
        c -> c.getAxis(c.getMapping().axisRightX),
        c -> c.getAxis(c.getMapping().axisRightX) > 0.25f
    ),
    RIGHT_STICK_LEFT(
        c -> -c.getAxis(c.getMapping().axisRightX),
        c -> c.getAxis(c.getMapping().axisRightX) < -0.25f
    ),

    RIGHT_STICK_BUTTON(
        c -> c.getButton(c.getMapping().buttonRightStick) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonRightStick)
    ),

    A(
        c -> c.getButton(c.getMapping().buttonA) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonA)
    ),
    B(
        c -> c.getButton(c.getMapping().buttonB) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonB)
    ),
    X(
        c -> c.getButton(c.getMapping().buttonX) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonX)
    ),
    Y(
        c -> c.getButton(c.getMapping().buttonY) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonY)
    ),

    DPAD_UP(
        c -> c.getButton(c.getMapping().buttonDpadUp) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadUp)
    ),
    DPAD_DOWN(
        c -> c.getButton(c.getMapping().buttonDpadDown) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadDown)
    ),
    DPAD_RIGHT(
        c -> c.getButton(c.getMapping().buttonDpadRight) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadRight)
    ),
    DPAD_LEFT(
        c -> c.getButton(c.getMapping().buttonDpadLeft) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonDpadLeft)
    ),

    MENU(
        c -> c.getButton(c.getMapping().buttonStart) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonStart)
    ),

    SCREEN(
        c -> c.getButton(c.getMapping().buttonBack) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonBack)
    ),

    LEFT_BUMPER(
        c -> c.getButton(c.getMapping().buttonL1) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonL1)
    ),

    RIGHT_BUMPER(
        c -> c.getButton(c.getMapping().buttonR1) ? 1f : 0f,
        c -> c.getButton(c.getMapping().buttonR1)
    ),

    LEFT_TRIGGER(
        c -> c.getAxis(5),
        c -> c.getAxis(5) > 0.25f
    ),

    RIGHT_TRIGGER(
        c -> c.getAxis(6),
        c -> c.getAxis(6) > 0.25f
    );

    private final Function<Controller, Float> getValue;
    private final Function<Controller, Boolean> getPressed;

    ControllerInput(Function<Controller, Float> getValue, Function<Controller, Boolean> getPressed) {
        this.getValue = getValue;
        this.getPressed = getPressed;
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
