package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.ObjectTypes;
import com.hugo99j.chaosparty.util.GsonUtil;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.SafeObjectList;
import com.hugo99j.chaosparty.util.ToRun;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.*;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.UUID;

import static com.hugo99j.chaosparty.ui.debugger.Debuggers.*;

public class ObjectEditor {
    static Vector2 oldPos;
    private static AbstractObject selected = null;
    private static final SafeObjectList popouts = new SafeObjectList();
    private static boolean preventMoving = false;
    private static Vector2 holdingOntoPos = null;

    static void renderObjectEditor() {
        if(selected != null && selected.isRemoved()) selected = null;
        popouts.ensureSafety();

        for (AbstractObject popout : new ArrayList<>(popouts)) {
            ImGui.begin("Object: "+popout+" ("+popout.getEntityId()+")");
            renderEditingPane(popout, true);
            ImGui.end();
        }

        ImGui.begin("Objects");

        if(GameData.level != null) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                int id = Debuggers.advancedPickColour >> 8;
                boolean found = false;
                for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                    if(allObject.getEntityId() == id/(Debuggers.isEnabled("showAdvancedObjectPicking") ? 10000 : 1)) {
                        Logger.info("Clicked " + allObject + " ("+ id +")");
                        if(allObject != selected) {
                            preventMoving = true;
                        }
                        selected = allObject;
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Logger.info("No object selected");
                    selected = null;
                }
            }
            if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                preventMoving = false;
                holdingOntoPos = null;
            }

            if(!preventMoving && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && selected != null && !ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 worldCoords = GameData.getCurrentMatch().getMatchViews().getFirst().gameViewport.unproject(screenCoords);
                if(holdingOntoPos == null) {
                    holdingOntoPos = selected.getPos().sub(worldCoords.x, worldCoords.y);
                }
                selected.setPos(new Vector2(worldCoords.x, worldCoords.y).add(holdingOntoPos));

                if (Debuggers.isEnabled("alignPixel")) {
                    float x = selected.getPos().x;
                    float snappedX = Math.round(x * 16f) / 16f;
                    selected.setX(snappedX);

                    float y = selected.getPos().y;
                    float snappedY = Math.round(y * 16f) / 16f;
                    selected.setY(snappedY);
                }
            }

            if (createObjectData != null) {
                renderObjectCreator();
            } else {
                //hoveredObject = renderObjectSelector();
                renderObjectSelector();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.C) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && selected != null) {
                JsonObject data = selected.write();
                Gdx.app.getClipboard().setContents(data.toString());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.X) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && selected != null) {
                JsonObject data = selected.write();
                Gdx.app.getClipboard().setContents(data.toString());
                selected.dispose();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.V) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                try {
                    JsonObject data = GsonUtil.parse(Gdx.app.getClipboard().getContents());
                    data.addProperty("uuid", UUID.randomUUID().toString());
                    selected = LevelLoader.createObject(data, GameData.level);
                } catch (Exception e) {
                    Logger.error("Error pasting object", e);
                }
            }
        }
        ImGui.end();
    }

    static void renderObjectCreator() {
        if (ImGui.button("Edit objects"))
            createObjectData = null;
        else {
            ImString objectCreator = new ImString(createObjectData, 10000);

            ImGui.inputTextMultiline("Create object", objectCreator, ImGuiInputTextFlags.None);

            createObjectData = objectCreator.get();

            boolean create = ImGui.button("Create");
            ImGui.sameLine();
            boolean forcedUUID = ImGui.button("Create (force UUID)");
            if (create || forcedUUID) {
                try {
                    JsonObject data = GsonUtil.parse(createObjectData);
                    if (!forcedUUID) {
                        data.addProperty("uuid", UUID.randomUUID().toString());
                    }
                    AbstractObject object = LevelLoader.createObject(data, GameData.getLevelOrThrow());
                    createObjectData = null;
                    selected = object;
                } catch (Exception e) {
                    Logger.error("Error creating object", e);
                }
            }

            ImGui.separatorText("Default Objects");
            ObjectTypes.types.forEach((n, c) -> {
                if(ImGui.button("Create " + n)) {
                    try {
                        AbstractObject object = c.constructor().get();
                        GameData.getLevelOrThrow().addObject(object);
                        createObjectData = null;
                        selected = object;
                    } catch (Exception e) {
                        Logger.error("Error adding object", e);
                    }
                }
            });
        }
    }

    static void renderObjectSelector() {
        if(GameData.level == null) return;
        if (ImGui.button("Add object")) createObjectData = "";

        ImGui.beginChild("Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
        ImGui.separatorText("All Objects");

        UUID hoveredObject = null;
        if (ImGui.beginTable("Object Selector", 1, ImGuiTableFlags.RowBg)) {
            for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.pushID(allObject.getEntityId());
                int flags = ImGuiSelectableFlags.SpanAllColumns;
                boolean isSelected = allObject == selected;
                if (isSelected)
                    flags |= ImGuiTreeNodeFlags.Selected;
                if (ImGui.selectable(allObject + " (" + allObject.getEntityId() + ")", isSelected, flags))
                    selected = allObject;
                if (ImGui.isItemHovered()) hoveredObject = allObject.getUUID();
                ImGui.popID();
            }
            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);
        if(selected != null) renderEditingPane(selected, false);
        ImGui.endChild();

    }

    static void renderEditingPane(AbstractObject object, boolean popout) {
        assert GameData.level != null;

        ImGui.separatorText("Current Object");
        boolean wasContained = popouts.contains(object);
        if(!popout && wasContained) ImGui.beginDisabled();
        ImGui.sameLine();
        if (ImGui.button(wasContained ? "-" : "^")) {
            if(wasContained) popouts.remove(object);
            else popouts.add(object);
        }
        if(!popout && wasContained) ImGui.endDisabled();

        Vector2 middle = oldPos == null ? object.getPos() : oldPos;
        int posOffset = ImGui.isKeyDown(ImGuiKey.ModShift) ? 1 : 10;

        boolean changing = false;
        slider("X Pos", object.getPos().x, object::setX, middle.x - posOffset, middle.x + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
        if (ImGui.isItemActive() && Debuggers.isEnabled("alignPixel")) {
            float x = object.getPos().x;
            float snappedX = Math.round(x * 16f) / 16f;
            object.setX(snappedX);
        }
        if (ImGui.isItemActive()) changing = true;
        slider("Y Pos", object.getPos().y, object::setY, middle.y - posOffset, middle.y + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
        if (ImGui.isItemActive() && Debuggers.isEnabled("alignPixel")) {
            float y = object.getPos().y;
            float snappedY = Math.round(y * 16f) / 16f;
            object.setY(snappedY);
        }
        if (ImGui.isItemActive()) changing = true;

        if (ImGui.button("TP to player"))
            object.setPos(GameData.getCurrentMatch().getPlayers().getFirst().getPlayerObject().getPos());
        ImGui.sameLine();
        if (ImGui.button("TP player to this"))
            GameData.getCurrentMatch().getPlayers().getFirst().getPlayerObject().setPos(object.getPos());

        if (oldPos == null && changing) {
            oldPos = object.getPos();
        }
        if (oldPos != null && !changing) {
            oldPos = null;
        }

        ImGui.separatorText("Java variables");
        addVariables(object, object.getClass());

        ImGui.separatorText("Data");
        if (data != null) {
            ImString input = new ImString(data, data.length() + 10000);

            ImGui.inputTextMultiline(" ", input, ImGuiInputTextFlags.None);

            data = input.get();
        }
        JsonObject objectData = object.write();
        data = GsonUtil.PARSER.toJson(objectData);

        if (ImGui.button("Refresh")) {
            try {
                ToRun.run(() -> {
                    JsonObject data = object.write();
                    AbstractObject o = LevelLoader.createObject(data, GameData.level);
                    object.dispose();
                    //noinspection usagelimited
                    object.setUUIDReallyUnsafeDoNotUse(UUID.randomUUID());
                    selected = o;
                });
            } catch (Exception e) {
                Logger.error("Error refreshing object", e);
            }
        }

        ImGui.sameLine();

        if (ImGui.button("Duplicate")) {
            try {
                JsonObject data = object.write();
                data.addProperty("uuid", UUID.randomUUID().toString());
                AbstractObject o = LevelLoader.createObject(data, GameData.level);
                selected = o;
            } catch (Exception e) {
                Logger.error("Error duplicating object", e);
            }
        }
        ImGui.sameLine();

        if (ImGui.button("Delete")) {
            GameData.level.removeObject(object);
        }
    }
}
