package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.RenderUtil;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;

import java.util.ArrayList;
import java.util.List;

public class UiViewer {
    public static List<UiElement> activeElements = new ArrayList<>();
    private static UiElement selected;

    protected static void render() {
        ImGui.begin("UI Viewer");
        ImGui.beginChild("Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
        ImGui.separatorText("Elements");

        if(selected != null && !activeElements.contains(selected)) selected = null;

        if (ImGui.beginTable("Element Selector", 1, ImGuiTableFlags.RowBg)) {
            for (UiElement element : activeElements) {
                if(element.getParent() != null) continue;
                renderElement(element);
            }
            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("Element Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);

        ImGui.separatorText("Current Element");

        if (selected != null) {
            ImGui.text("X: "+selected.getX());
            ImGui.text("Y: "+selected.getY());
            ImGui.text("Width: "+selected.getWidth());
            ImGui.text("Height: "+selected.getHeight());
        }

        ImGui.endChild();
        ImGui.end();
        UiViewer.activeElements.clear();
    }

    private static void renderElement(UiElement element) {
        ImGui.tableNextRow();
        ImGui.tableNextColumn();
        ImGui.pushID(element.getElementId().hashCode());

        int flags = ImGuiSelectableFlags.SpanAllColumns;
        if (element == selected) {
            flags |= ImGuiTreeNodeFlags.Selected;
        }
        if (ImGui.selectable(element.getElementId(), element == selected, flags)) selected = element;

        GameData.shapeRenderer.setProjectionMatrix(GameData.uiCamera.combined);
        if (ImGui.isItemHovered()) {
            Color c = Color.SCARLET.cpy();
            c.a = 0.5f;
            RenderUtil.enableBlending();
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            GameData.shapeRenderer.setColor(c);
        } else {
            //((ShapeRendererLineWidth) GameData.shapeRenderer).setDefaultRectLineWidth(100);
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            GameData.shapeRenderer.setColor(Color.ORANGE);
        }
        GameData.shapeRenderer.rect(element.getX(), element.getY(), element.getWidth(), element.getHeight());
        GameData.shapeRenderer.end();
        ImGui.popID();

        if(!element.getChildren().isEmpty()) {
            ImGui.indent();
            for (UiElement child : element.getChildren()) {
                renderElement(child);
            }
            ImGui.unindent();
        }
    }
}
