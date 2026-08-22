package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.util.ControllerInput;
import com.hugo99j.chaosparty.util.ControllerUtil;
import com.hugo99j.chaosparty.util.DummyController;
import com.studiohartman.jamepad.ControllerIndex;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DebugController {
    @SuppressWarnings("ClassEscapesDefinedScope")
    public static Mode INSTANCE;
    public static ControllerIndex DUMMY_INDEX;
    private static final List<ControllerInput> currentlyPressed = new ArrayList<>();
    private static final List<ControllerInput> currentlyPressedThisTick = new ArrayList<>();
    private static final Map<ControllerInput, Float> currentValues = new HashMap<>();
    private static final List<Runnable> scheduled = new ArrayList<>();
    private static boolean executeNow = true;

    private static final List<Integer> overrideModePressed = new ArrayList<>();
    private static final Map<Integer, Float> overrideModeValues = new HashMap<>();

    protected static void render() {
        currentlyPressed.clear();
        currentValues.clear();
        overrideModePressed.clear();
        ImGui.begin("Debug Controller");
        ImInt selected = new ImInt(INSTANCE == null ? 0 : INSTANCE instanceof NormalModeInstance ? 1 : 2);
        ImGui.listBox("Enabled", selected, new String[]{"Off", "Dummy", "Real"});
        if(selected.get() == 0 && INSTANCE != null) INSTANCE = null;
        if(selected.get() == 1 && !(INSTANCE instanceof NormalModeInstance)) INSTANCE = new NormalModeInstance();
        if(selected.get() == 2 && !(INSTANCE instanceof RealModeInstance)) INSTANCE = new RealModeInstance();

        switch (INSTANCE) {
            case null -> ImGui.text("Not enabled");
            case RealModeInstance realModeInstance -> {
                ImGui.pushID("LT");
                float[] lt = new float[]{0.0f};
                ImGui.vSliderFloat("", new ImVec2(20, 40), lt, 0, 1, "LT");
                overrideModeValues.put(5, lt[0]);
                ImGui.popID();
                ImGui.sameLine();
                ImGui.text("                   ");
                ImGui.sameLine();
                float[] rt = new float[]{0.0f};
                ImGui.pushID("RT");
                ImGui.vSliderFloat("", new ImVec2(20, 40), rt, 0, 1, "RT");
                overrideModeValues.put(6, rt[0]);
                ImGui.popID();

                if(ImGui.button("   LB   ")) {
                    overrideModePressed.add(realModeInstance.getMapping().buttonL1);
                }
                ImGui.sameLine();
                ImGui.text("       ");
                ImGui.sameLine();
                if(ImGui.button("   RB   ")) {
                    overrideModePressed.add(realModeInstance.getMapping().buttonR1);
                }
                //Text instead of indents because they dont work on sameLine
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.text(" ");
                ImGui.sameLine();
                float firstPoint = ImGui.getCursorPosY();
                if(ImGui.button("Y")) overrideModePressed.add(realModeInstance.getMapping().buttonY);
                float secondPoint = ImGui.getCursorPosY();
                if(ImGui.button("X")) overrideModePressed.add(realModeInstance.getMapping().buttonX);
                ImGui.sameLine();
                ImGui.text("");
                ImGui.sameLine();
                if(ImGui.button("B")) overrideModePressed.add(realModeInstance.getMapping().buttonB);
                ImGui.text(" ");
                ImGui.sameLine();
                if(ImGui.button("A")) overrideModePressed.add(realModeInstance.getMapping().buttonA);
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                float change = secondPoint-firstPoint;
                ImGui.setCursorPosY(ImGui.getCursorPosY()-change*3);
                selector2Axis("Left stick", (b) -> {
                    overrideModeValues.put(realModeInstance.getMapping().axisLeftX, -1+2*b.x);
                    overrideModeValues.put(realModeInstance.getMapping().axisLeftY, -1+2*b.y);
                    }, true);

                ImGui.newLine();
                ImGui.text("       ");
                ImGui.sameLine();
                if(ImGui.button("O")) overrideModePressed.add(realModeInstance.getMapping().buttonBack);
                ImGui.sameLine();
                if(ImGui.button("+")) overrideModePressed.add(-10);
                ImGui.sameLine();
                if(ImGui.button("=")) overrideModePressed.add(realModeInstance.getMapping().buttonStart);
                ImGui.newLine();
                ImGui.newLine();
                ImGui.setCursorPosY(ImGui.getCursorPosY()-change);

                ImGui.indent();
                ImGui.text(" ");
                ImGui.sameLine();
                if(ImGui.button("^")) overrideModePressed.add(realModeInstance.getMapping().buttonDpadUp);
                if(ImGui.button("<")) overrideModePressed.add(realModeInstance.getMapping().buttonDpadLeft);
                ImGui.sameLine();
                ImGui.text("");
                ImGui.sameLine();
                if(ImGui.button(">")) overrideModePressed.add(realModeInstance.getMapping().buttonDpadRight);
                ImGui.text(" ");
                ImGui.sameLine();
                if(ImGui.button("V")) overrideModePressed.add(realModeInstance.getMapping().buttonDpadDown);
                ImGui.unindent();

                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.indent();
                ImGui.setCursorPosY(ImGui.getCursorPosY()-change*2);
                selector2Axis("Right stick", (b) -> {
                    overrideModeValues.put(realModeInstance.getMapping().axisRightX, -1+2*b.x);
                    overrideModeValues.put(realModeInstance.getMapping().axisRightY, -1+2*b.y);
                }, false);
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
                ImGui.unindent();
            }
            case NormalModeInstance normalModeInstance -> {
                if (ImGui.checkbox("Execute inputs", executeNow)) {
                    executeNow = !executeNow;
                }
                for (ControllerInput value : ControllerInput.values()) {
                    ImGui.text(value.toString());
                    ImGui.sameLine();
                    ImGui.pushID("Press" + value);
                    if (ImGui.button("Press")) {
                        scheduled.add(() -> {
                            currentlyPressed.add(value);
                            currentlyPressedThisTick.add(value);
                        });
                    }
                    ImGui.popID();
                    ImGui.sameLine();
                    float[] f = {0.0f};
                    ImGui.pushID("Value" + value);
                    if (ImGui.sliderFloat("Value", f, -1, 1)) {
                        scheduled.add(() -> currentValues.put(value, f[0]));
                    }
                    ImGui.popID();
                }
                if (executeNow) {
                    scheduled.forEach(Runnable::run);
                    scheduled.clear();
                }
            }
            default -> {
            }
        }
        ImGui.end();
    }

    private static void selector2Axis(String name, Consumer<Vector2> out, boolean sameLine) {
        float startX = ImGui.getCursorPosX();
        float startY = ImGui.getCursorPosY();
        ImGui.pushID(name); //Fixes active being reset
        ImGui.button("     \n     \n     ");
        ImGui.popID();
        if(ImGui.isItemActive()) {
            ImVec2 mousePos = ImGui.getMousePos();
            ImVec2 buttonPos = ImGui.getItemRectMin();
            Vector2 relativePos = new Vector2(Math.clamp(mousePos.x - buttonPos.x, 0, ImGui.getItemRectSizeX()), Math.clamp(mousePos.y - buttonPos.y, 0, ImGui.getItemRectSizeY()));
            float originalX = ImGui.getCursorPosX();
            float originalY = ImGui.getCursorPosY();
            float xSize = ImGui.getItemRectSizeX();
            float ySize = ImGui.getItemRectSizeY();
            ImGui.setCursorPosX(startX+relativePos.x-5);
            ImGui.setCursorPosY(startY+relativePos.y-5);
            ImGui.text("+");
            if(sameLine) {
                ImGui.setCursorPosX(startX);
                ImGui.setCursorPosY(startY);
            } else {
                ImGui.setCursorPosX(originalX);
                ImGui.setCursorPosY(originalY);
            }
            out.accept(new Vector2(relativePos.x/xSize, relativePos.y/ySize));
        } else {
            float originalX = ImGui.getCursorPosX();
            float originalY = ImGui.getCursorPosY();
            float xSize = ImGui.getItemRectSizeX();
            float ySize = ImGui.getItemRectSizeY();
            ImGui.setCursorPosX(startX+xSize/2f-5);
            ImGui.setCursorPosY(startY+ySize/2f-5);
            ImGui.text("+");
            if(sameLine) {
                ImGui.setCursorPosX(startX);
                ImGui.setCursorPosY(startY);
            } else {
                ImGui.setCursorPosX(originalX);
                ImGui.setCursorPosY(originalY);
            }
            out.accept(new Vector2(0.5f, 0.5f));
        }
    }

    public static void tick() {
        currentlyPressedThisTick.clear();
        if(DebugController.INSTANCE instanceof ControllerUtil u) u.onTick();
    }

    private sealed interface Mode permits RealModeInstance, NormalModeInstance {

    }

    //Mixins make this an instance of ControllerUtil
    private static final class RealModeInstance extends JamepadController implements Mode {
        public RealModeInstance() {
            super(DebugController.DUMMY_INDEX);
        }

        @Override
        public boolean getButton(int buttonCode) {
            return overrideModePressed.contains(buttonCode);
        }

        @Override
        public float getAxis(int axisCode) {
            return overrideModeValues.getOrDefault(axisCode, 0f);
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }

    private static final class NormalModeInstance extends DummyController implements Mode {
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
