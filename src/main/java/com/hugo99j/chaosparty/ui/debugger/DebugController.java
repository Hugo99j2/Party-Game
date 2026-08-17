package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.controllers.Controllers;
import com.hugo99j.chaosparty.util.ControllerInput;
import com.hugo99j.chaosparty.util.DummyController;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugController {
    @SuppressWarnings("ClassEscapesDefinedScope")
    public static Instance INSTANCE;
    private static final List<ControllerInput> currentlyPressed = new ArrayList<>();
    private static final List<ControllerInput> currentlyPressedThisTick = new ArrayList<>();
    private static final Map<ControllerInput, Float> currentValues = new HashMap<>();

    protected static void render() {
        currentlyPressed.clear();
        currentValues.clear();
        ImGui.begin("Debug Controller");
        if(INSTANCE == null) {
            if(ImGui.checkbox("Enabled", false)) {
                INSTANCE = new Instance();
            }
        } else {
            if(ImGui.checkbox("Enabled", true)) {
                INSTANCE = null;
            }
            for (ControllerInput value : ControllerInput.values()) {
                ImGui.text(value.toString());
                ImGui.sameLine();
                if(ImGui.button("Press "+value)) {
                    currentlyPressed.add(value);
                }
            }
        }
        ImGui.end();
    }

    public static void tick() {
        currentlyPressedThisTick.clear();
    }

    private static class Instance extends DummyController {
        @Override
        public boolean isPressed(ControllerInput input) {
            return currentlyPressed.contains(input);
        }

        @Override
        public boolean wasJustPressed(ControllerInput input) {
            return currentlyPressed.contains(input);
        }

        @Override
        public boolean wasJustPressedThisTick(ControllerInput input) {
            return currentlyPressedThisTick.contains(input);
        }

        @Override
        public float getValue(ControllerInput input) {
            return currentValues.getOrDefault(input, 0f);
        }
    }
}
