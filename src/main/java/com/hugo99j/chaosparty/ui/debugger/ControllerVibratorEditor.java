package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.daniel99j.djutil.MiscUtils;
import com.google.common.primitives.Floats;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ControllerUtil;
import com.hugo99j.chaosparty.util.VibrationAmount;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotPoint;
import imgui.extension.implot.flag.ImPlotAxisFlags;
import imgui.extension.implot.flag.ImPlotDragToolFlags;
import imgui.extension.implot.flag.ImPlotFlags;
import imgui.flag.*;
import imgui.type.ImDouble;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerVibratorEditor {
    private static float[] intense;
    private static float[] fast;
    private static float testStartTime = 200;

    protected static void render() {
        if(ImGui.begin("Vibration editor")) {
            if (intense == null) {
                if (ImGui.button("Create new")) {
                    intense = new float[]{0, 1, 0.1f, 0.4f, 2f, 0f};
                    fast = new float[]{0, 1, 0.1f, 0.4f, 2f, 0f};
                }
                ImString val = new ImString(10000);
                if (ImGui.inputText("Import", val, ImGuiInputTextFlags.EnterReturnsTrue)) {
                    String s = Gdx.app.getClipboard().getContents();
                    s = MiscUtils.replaceTextBetween(s, "", "VibrationAmount.of(", "");
                    s = MiscUtils.replaceTextBetween(s, ")", "", "");
                    String fastPart = MiscUtils.getTextBetween(s, "new float[]{", "}");
                    String intensePart = MiscUtils.getTextBetween(s, ", new float[]{", "}");
                    String[] fastFloats = fastPart.split(",");
                    List<Float> fastFloatList = new ArrayList<>();
                    for (String fastFloat : fastFloats) {
                        fastFloatList.add(Floats.tryParse(fastFloat));
                    }
                    fast = Floats.toArray(fastFloatList);

                    String[] intenseFloats = intensePart.split(",");
                    List<Float> intenseFloatList = new ArrayList<>();
                    for (String intenseFloat : intenseFloats) {
                        intenseFloatList.add(Floats.tryParse(intenseFloat));
                    }
                    intense = Floats.toArray(intenseFloatList);

                    //new float[]{1.3682092,0.1460177,1.3682092,0.5176991,4.688129,0.0}, new float[]{0.0f,1.0f,2.0f,0.49557522f,2.0f,0.0f}
                }
            } else {
                ImPlot.setNextAxesLimits(0, 10, 0, 1);
                if(ImPlot.beginPlot("Editor", ImPlotFlags.NoBoxSelect | ImPlotFlags.NoMenus | ImPlotFlags.NoMouseText | ImPlotFlags.NoLegend)) {
                    ImPlot.setupAxes("Time", "Value", ImPlotAxisFlags.None, ImPlotAxisFlags.Lock);

                    plot("Intense", intense);

                    plot("Fast", fast);

                    if(GameData.time-testStartTime < Math.max(intense[intense.length-2], fast[fast.length-2])) ImPlot.plotLine("Current Time", new float[]{GameData.time-testStartTime, GameData.time-testStartTime}, new float[]{0f, 1f});

                    if(ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                        float radius = 8;
                        ImVec2 mousePixel = ImGui.getMousePos();
                        boolean found = false;
                        for (int i = 0; i < fast.length; i+=2) {
                            ImVec2 point = ImPlot.plotToPixels(fast[i], fast[i + 1]);
                            float dx = mousePixel.x - point.x;
                            float dy = mousePixel.y - point.y;

                            if(dx * dx + dy * dy <= (radius * radius)) {
                                List<Float> f = new ArrayList<>(Floats.asList(fast));
                                f.remove(i);
                                f.remove(i);
                                fast = Floats.toArray(f);
                                found = true;
                                break;
                            }
                        }

                        if(!found) for (int i = 0; i < intense.length; i+=2) {
                            ImVec2 point = ImPlot.plotToPixels(intense[i], intense[i + 1]);
                            float dx = mousePixel.x - point.x;
                            float dy = mousePixel.y - point.y;

                            if(dx * dx + dy * dy <= (radius * radius)) {
                                List<Float> f = new ArrayList<>(Floats.asList(intense));
                                f.remove(i);
                                f.remove(i);
                                intense = Floats.toArray(f);
                                found = true;
                                break;
                            }
                        }

                        if(!found) {
                            // add to intense
                            if(!enabled.getOrDefault("Fast", true)) {
                                ImPlotPoint pos = ImPlot.pixelsToPlot(mousePixel);
                                int index = intense.length;
                                for (int i = 0; i < intense.length; i+=2) {
                                    if(intense[i] > pos.x) {
                                        index = i;
                                        break;
                                    }
                                }
                                List<Float> f = new ArrayList<>(Floats.asList(intense));
                                f.add(index, (float) pos.y);
                                f.add(index, (float) pos.x);
                                intense = Floats.toArray(f);
                            }

                            if(!enabled.getOrDefault("Intense", true)) {
                                ImPlotPoint pos = ImPlot.pixelsToPlot(mousePixel);
                                int index = fast.length;
                                for (int i = 0; i < fast.length; i+=2) {
                                    if(fast[i] > pos.x) {
                                        index = i;
                                        break;
                                    }
                                }
                                List<Float> f = new ArrayList<>(Floats.asList(fast));
                                f.add(index, (float) pos.y);
                                f.add(index, (float) pos.x);
                                fast = Floats.toArray(f);
                            }
                        }
                    }
                    ImPlot.endPlot();
                }

                if (ImGui.button("Export")) {
                    Gdx.app.getClipboard().setContents(export());
                }
                if (ImGui.button("Test")) {
                    ControllerUtil.getCurrent().vibrate(VibrationAmount.of(enabled.getOrDefault("Fast", true) ? Floats.toArray(Floats.asList(fast)) : new float[]{}, enabled.getOrDefault("Intense", true) ? Floats.toArray(Floats.asList(intense)) : new float[]{}));
                    testStartTime = GameData.time;
                }
                if (ImGui.button("Clear")) {
                    fast = null;
                    intense = null;
                }
            }
        }
        ImGui.end();
    }

    private static String export() {
        StringBuilder out = new StringBuilder("new float[]{");
        for (int i = 0; i < fast.length; i++) {
            float f = fast[i];
            out.append(f).append("f");
            if(i != fast.length-1) out.append(",");
        }
        out.append("}, new float[]{");
        for (int i = 0; i < intense.length; i++) {
            float f = intense[i];
            out.append(f).append("f");
            if(i != intense.length-1) out.append(",");
        }
        out.append("}");
        return out.toString();
    }

    private static final Map<String, ImVec4> colours = new HashMap<>();
    private static final Map<String, Boolean> enabled = new HashMap<>();

    private static void plot(String name, float[] paired) {
        ImGui.pushStyleColor(ImGuiCol.CheckMark, colours.getOrDefault(name, new ImVec4(0.0f, 0.0f, 0.0f, 1.0f)));
        ImGui.pushStyleColor(ImGuiCol.FrameBg, colours.getOrDefault(name, new ImVec4(0.0f, 0.0f, 0.0f, 1.0f)).times(1, 1, 1, 0.2f));
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, colours.getOrDefault(name, new ImVec4(0.0f, 0.0f, 0.0f, 1.0f)).times(1, 1, 1, 0.6f));
        if(ImGui.checkbox(name, enabled.getOrDefault(name, true))) {
            enabled.put(name, !enabled.getOrDefault(name, true));
        }
        ImGui.popStyleColor();
        ImGui.popStyleColor();
        ImGui.popStyleColor();

        if(enabled.getOrDefault(name, true)) {
            float[] times = new float[paired.length / 2 + 1];
            float[] values = new float[paired.length / 2 + 1];
            for (int i = 0; i < paired.length; i += 2) {
                ImDouble time = new ImDouble(paired[i]);
                ImDouble value = new ImDouble(paired[i + 1]);
                //ImPlot.getPlotPosX()
                if (ImPlot.dragPoint(i + name.hashCode(), time, value, colours.getOrDefault(name, new ImVec4()), ImPlotDragToolFlags.Delayed)) {
                    float minX = 0;
                    float maxX = 100;
                    if (i != 0) minX = paired[i - 2];
                    if (i != paired.length - 2) maxX = paired[i + 2];
                    paired[i] = Math.clamp(time.floatValue(), Math.max(minX, 0), maxX);
                    paired[i + 1] = Math.clamp(value.floatValue(), 0, i == paired.length - 2 ? 0 : 1);
                }
                times[1 + (i / 2)] = paired[i];
                values[1 + (i / 2)] = paired[i + 1];
            }
            ImPlot.plotLine(name, times, values);
            if (!colours.containsKey(name)) colours.put(name, ImPlot.getLastItemColor());
        }
    }
}
