package com.hugo99j.chaosparty.ui.debugger;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.level.Level;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.google.common.io.PatternFilenameFilter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.ObjectTypes;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.*;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.*;
import imgui.type.ImString;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.zip.Deflater;

import static com.hugo99j.chaosparty.ui.debugger.Debuggers.*;
import static com.hugo99j.chaosparty.ui.debugger.UndoRedoHistory.onEdited;

public class ObjectEditor {
    static Vector2 oldPos;
    static Vector2 startObjectPos = null;
    private static final SafeObjectHolder selected = new  SafeObjectHolder();
    private static final SafeObjectList popouts = new SafeObjectList();
    private static boolean preventMoving = false;
    private static Vector2 holdingOntoPos = null;
    private static boolean justChangedSelection = false;
    private static String search = "";

    static {
        MenuBar.registerBuilder((b) -> {
            b.addItem("File", Input.Keys.O, "Open", () -> {
                FileDialog dialog = new FileDialog((Frame) null, "Select map to open", FileDialog.LOAD);
                dialog.setFilenameFilter(new PatternFilenameFilter(".*\\.map$"));
                dialog.setVisible(true);

                String directory = dialog.getDirectory();
                String file = dialog.getFile();

                //selected a file
                if (file != null) {
                    Logger.info("Loaded "+directory+file);
                    GameData.startMatch(List.of(new MatchPlayer(User.getUser(5)))).setCurrentMinigame(new MapEditor(directory+file));
                }

                dialog.dispose();
            });
            if(GameData.getCurrentMinigame() instanceof MapEditor) {
                b.addItem("File", Input.Keys.S, "Save", Debuggers::saveMap);
                b.addItem("Edit", Input.Keys.C, "Copy", () -> {
                    JsonObject data = selected.get().write();
                    Gdx.app.getClipboard().setContents(data.toString());
                }, selected.get() != null);
                b.addItem("Edit", Input.Keys.X, "Cut", () -> {
                    JsonObject data = selected.get().write();
                    Gdx.app.getClipboard().setContents(data.toString());
                    if(selected.get() instanceof SelectionGroup group) {
                        for (AbstractObject abstractObject : group.selected) {
                            abstractObject.dispose();
                        }
                    }
                    selected.get().dispose();
                    UndoRedoHistory.onEdited();
                }, selected.get() != null);
                b.addItem("Edit", Input.Keys.V, "Paste", () -> {
                    try {
                        JsonObject data = GsonUtil.parse(Gdx.app.getClipboard().getContents());
                        data.addProperty("uuid", UUID.randomUUID().toString());
                        selected.set(LevelLoader.createObject(data, GameData.getLevelOrThrow()));
                        UndoRedoHistory.onEdited();
                    } catch (Exception e) {
                        Logger.error("Error pasting object", e);
                    }
                });
            }
        });
    }

    static void renderObjectEditor() {
        popouts.ensureSafety();

        for (AbstractObject popout : new ArrayList<>(popouts)) {
            if(ImGui.begin("Object: "+popout+" ("+popout.getEntityId()+")")) {
                renderEditingPane(popout, true);
            }
            ImGui.end();
        }

        boolean resetFocus = false;
        if(justChangedSelection) {
            if(!ImGui.isWindowFocused(ImGuiFocusedFlags.AnyWindow | ImGuiFocusedFlags.ChildWindows)) resetFocus = true;
            ImGui.setNextWindowFocus();
        }
        justChangedSelection = false;
        if(ImGui.begin("Objects")) {
            if (GameData.level != null) {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    int id = advancedPickColour >> 8;
                    boolean found = false;
                    for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                        if (allObject.getEntityId() == id / (isEnabled("showAdvancedObjectPicking") ? 10000 : 1)) {
                            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                                Logger.info("Added " + allObject + " (" + id + ") to selection");

                                if (selected.get() instanceof SelectionGroup group) {
                                    if (group.selected.contains(allObject)) {
                                        group.selected.remove(allObject);
                                    } else group.selected.add(allObject);
                                    group.update();
                                } else {
                                    SelectionGroup group = new SelectionGroup();
                                    group.selected.add(allObject);
                                    group.selected.add(selected.get());
                                    GameData.getLevelOrThrow().addObject(group);
                                    //group.updateUtil(); //it auto updates now
                                    selected.set(group);
                                }
                                if (selected.get() == null) {
                                    preventMoving = true;
                                    holdingOntoPos = null;
                                    startObjectPos = null;
                                    selected.set(null);
                                } else {
                                    preventMoving = true;
                                    holdingOntoPos = null;
                                    startObjectPos = selected.get().getPos().cpy();
                                    justChangedSelection = true;
                                }
                            } else {
                                Logger.info("Clicked " + allObject + " (" + id + ")");
                                if (allObject != selected.get()) {
                                    if (selected.get() instanceof SelectionGroup g && g.selected.contains(allObject)) {
                                    } else {
                                        preventMoving = true;
                                        holdingOntoPos = null;
                                        startObjectPos = allObject.getPos().cpy();
                                        justChangedSelection = true;
                                        selected.set(allObject);
                                    }
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Logger.info("No object selected");
                        preventMoving = true;
                        holdingOntoPos = null;
                        startObjectPos = null;
                        selected.set(null);
                    }
                }
                if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    if (holdingOntoPos != null && !selected.get().getPos().equals(startObjectPos)) onEdited();
                    preventMoving = false;
                    holdingOntoPos = null;
                    startObjectPos = null;
                }

                if (!preventMoving && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && selected.get() != null) {
                    Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                    Vector3 worldCoords = GameData.getCurrentMatch().getMatchViews().getFirst().gameViewport.unproject(screenCoords);
                    if (holdingOntoPos == null) {
                        holdingOntoPos = selected.get().getPos().sub(worldCoords.x, worldCoords.y);
                    }
                    selected.get().setPos(new Vector2(worldCoords.x, worldCoords.y).add(holdingOntoPos));

                    if (isEnabled("alignPixel") || selected.get() instanceof DebugOptions.Ruler) {
                        float x = selected.get().getPos().x;
                        float snappedX = Math.round(x * 16f) / 16f;
                        selected.get().setX(snappedX);

                        float y = selected.get().getPos().y;
                        float snappedY = Math.round(y * 16f) / 16f;
                        selected.get().setY(snappedY);
                    }
                }

                if (createObjectData != null) {
                    renderObjectCreator();
                } else {
                    //hoveredObject = renderObjectSelector();
                    renderObjectSelector();
                }

            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DEL) && selected.get() != null) {
                if (selected.get() instanceof SelectionGroup group) {
                    for (AbstractObject abstractObject : group.selected) {
                        abstractObject.dispose();
                    }
                }
                selected.get().dispose();
                UndoRedoHistory.onEdited();
            }
        }
        ImGui.end();

        if(resetFocus) {
            ImGui.setWindowFocus(null);
        }
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
                    Vector3 camPos = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position;
                    object.setPos(new Vector2(camPos.x, camPos.y));
                    createObjectData = null;
                    selected.set(object);
                } catch (Exception e) {
                    Logger.error("Error creating object", e);
                }
            }

            ImGui.separatorText("Default Objects");
            ObjectTypes.types.forEach((n, c) -> {
                if(c.showInEditor() && ImGui.button("Create " + n)) {
                    try {
                        AbstractObject object = c.constructor().get();
                        GameData.getLevelOrThrow().addObject(object);
                        Vector3 camPos = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position;
                        object.setPos(new Vector2(camPos.x, camPos.y));
                        createObjectData = null;
                        selected.set(object);
                    } catch (Exception e) {
                        Logger.error("Error adding object", e);
                    }
                }
            });

            ImGui.separatorText("Default Lights");
            if (ImGui.button("Add point light")) {
                select(GameData.getLevelOrThrow().getObjectByUUID(GameData.getLevelOrThrow().addLight((rayHandler -> new PointLight(rayHandler, 128))).uuid()));
            }

            if (ImGui.button("Add cone light")) {
                select(GameData.getLevelOrThrow().getObjectByUUID(GameData.getLevelOrThrow().addLight((rayHandler -> new ConeLight(rayHandler, 128, Color.RED, 5, 0, 0, 0, 45))).uuid()));
            }

            if (ImGui.button("Add directional light")) {
                select(GameData.getLevelOrThrow().getObjectByUUID(GameData.getLevelOrThrow().addLight((rayHandler -> new DirectionalLight(rayHandler, 128, Color.RED, 30))).uuid()));
            }
        }
    }

    static void renderObjectSelector() {
        if(GameData.level == null) return;
        if (ImGui.button("Add objects/lights")) createObjectData = "";

        ImGui.beginChild("Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
        ImGui.separatorText("All Objects");

        ImString imSearch = new ImString(search, 32);
        ImGui.inputText("Q", imSearch);
        search = imSearch.get();

        if (ImGui.beginTable("Object Selector", 1, ImGuiTableFlags.RowBg)) {
            for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                if(!search.isBlank() && !allObject.toString().toLowerCase().contains(search.toLowerCase()) && !String.valueOf(allObject.getEntityId()).contains(search) && !allObject.getType().id().contains(search.toLowerCase())) {
                    continue;
                }
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.pushID(allObject.getEntityId());
                int flags = ImGuiSelectableFlags.SpanAllColumns;
                boolean isSelected = allObject == selected.get();
                if (isSelected)
                    flags |= ImGuiTreeNodeFlags.Selected;
                if (ImGui.selectable(allObject + " (" + allObject.getEntityId() + ")", isSelected, flags))
                    selected.set(allObject);
                if(justChangedSelection && allObject.equals(selected.get())) {
                    ImGui.setScrollHereY();
                }
                ImGui.popID();
            }
            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);
        if(selected.get() != null) renderEditingPane(selected.get(), false);
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
        if (ImGui.isItemActive() && isEnabled("alignPixel")) {
            float x = object.getPos().x;
            float snappedX = Math.round(x * 16f) / 16f;
            object.setX(snappedX);
        }
        if (ImGui.isItemActive()) changing = true;
        slider("Y Pos", object.getPos().y, object::setY, middle.y - posOffset, middle.y + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
        if (ImGui.isItemActive() && isEnabled("alignPixel")) {
            float y = object.getPos().y;
            float snappedY = Math.round(y * 16f) / 16f;
            object.setY(snappedY);
        }
        if (ImGui.isItemActive()) changing = true;

        Vector3 camPos = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position;
        if (ImGui.button("Teleport to here")) {
            object.setPos(new Vector2(camPos.x, camPos.y));
            onEdited();
        }
        ImGui.sameLine();
        if (ImGui.button("Center view")) {
            camPos.x = object.getPos().x;
            camPos.y = object.getPos().y;
        }

        if (oldPos == null && changing) {
            oldPos = object.getPos();
        }
        if (oldPos != null && !changing) {
            oldPos = null;
            onEdited();
        }

        ImGui.separatorText("Properties");
        addVariables(object, object.getClass(), (holder) -> {
            try {
                holder.a().set(holder.b(), holder.c());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

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
                    selected.set(o);
                    onEdited();
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
                selected.set(o);
                onEdited();
            } catch (Exception e) {
                Logger.error("Error duplicating object", e);
            }
        }
        ImGui.sameLine();

        if (ImGui.button("Delete")) {
            GameData.level.removeObject(object);
            onEdited();
        }
    }

    public static void select(AbstractObject o) {
        selected.set(o);
    }

    public static boolean isSelected(AbstractObject object) {
        if(!GameData.DEBUGGING) return false;
        if(selected.get() != null) {
            if(selected.get().equals(object)) return true;
            if(selected.get() instanceof SelectionGroup selectionGroup) {
                return selectionGroup.selected.contains(object);
            }
        }
        return false;
    }

    public static class MapScreenshotter extends TemporaryDevObject {
        private boolean takePhoto = false;
        private int width = 10;
        private int height = 10;
        @NoDebugOption
        private float screenshotEffectFrames = 0;

        public MapScreenshotter(JsonObject data) {
            this(data.get("width").getAsInt(), data.get("height").getAsInt());
        }

        public MapScreenshotter(int width, int height) {
            super();
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(MatchView matchView) {
            super.render(matchView);
            Vector2 aligned = new Vector2(Math.round(this.getPos().x), Math.round(this.getPos().y));
            GameData.spriteBatch.draw(ImageUtil.get("camera"), aligned.x-0.5f, aligned.y-0.5f, 1, 1);
            GameData.spriteBatch.end();

            if(screenshotEffectFrames > 0) {
                GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                GameData.shapeRenderer.setColor(Color.WHITE);
                screenshotEffectFrames-=Gdx.graphics.getDeltaTime();
            } else {
                GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GameData.shapeRenderer.setColor(Color.YELLOW);
            }
            GameData.shapeRenderer.rect(aligned.x-width/2f, aligned.y-height/2f, width, height);
            GameData.shapeRenderer.end();
            if(takePhoto) {
                takePhoto = false;
                ToRun.run(() -> {
                    if(GameData.getCurrentMinigame() == null) return;
                    boolean old = debugOptions.get("renderDevObjects").object;
                    debugOptions.get("renderDevObjects").object = false;
                    MatchView tempView = new MatchView(width, height);
                    tempView.setCenter(false);
                    tempView.gameViewport.update(width*16, height*16, false);
                    tempView.gameCamera.position.x = aligned.x;
                    tempView.gameCamera.position.y = aligned.y;
                    tempView.gameCamera.update();
                    tempView.fbo.dispose();
                    tempView.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width*16, height*16, false);
                    Pixmap pixmap = RenderUtil.renderToPixmap(tempView.render(), width*8, height*8);
                    tempView.dispose();
                    PixmapIO.writePNG(Gdx.files.absolute(PathUtil.codingDir(PathUtil.texture("ui/minigame_selector/"+GameData.getCurrentMinigame().getMapName()+".png"))), pixmap, Deflater.DEFAULT_COMPRESSION, false);
                    pixmap.dispose();
                    screenshotEffectFrames = 0.8f;
                    debugOptions.get("renderDevObjects").object = old;
                });
            }
            GameData.spriteBatch.begin();
        }

        @Override
        public void writeAdditional(JsonObject object) {
            super.writeAdditional(object);
            JsonObject data = new JsonObject();
            data.addProperty("width", width);
            data.addProperty("height", height);
            object.add("camera", data);
        }

        @Override
        public String toString() {
            return "Screenshotter";
        }

        @Override
        public boolean shouldSave() {
            return true;
        }

        @Override
        public boolean keep() {
            return false;
        }
    }

    protected static class SelectionGroup extends TemporaryDevObject {
        @NoDebugOption
        SafeObjectList selected = new SafeObjectList();
        @NoDebugOption
        Map<UUID, Vector2> offsets = new HashMap<>();
        @NoDebugOption
        private int oldLevelHash;
        private boolean isAllowedToChange = false;

        public void update() {
            oldLevelHash = this.getLevel().hashCode();
            selected.ensureSafety();
            Vector2 combined = null;
            for (AbstractObject object : selected) {
                if(combined == null) combined = object.getPos();
                else combined.add(object.getPos());
            }
            if(selected.isEmpty()) {
                this.dispose();
            } else {
                Vector2 center = combined.cpy().scl(1.0f / selected.size());
                super.setPos(center);
                offsets.clear();
                for (AbstractObject object : selected) {
                    offsets.put(object.getUUID(), object.getPos().sub(center));
                }
            }
        }

        @Override
        public void setPos(Vector2 pos) {
            //if undo'd it will be in a different level!
            if(oldLevelHash != this.getLevel().hashCode()) {
                update();
                isAllowedToChange = false;
            } else if(isAllowedToChange) {
                super.setPos(pos);
                selected.ensureSafety();
                for (AbstractObject object : selected) {
                    object.setPos(offsets.get(object.getUUID()).cpy().add(pos));
                    if (isEnabled("alignPixel")) {
                        float x = object.getPos().x;
                        float snappedX = Math.round(x * 16f) / 16f;
                        object.setX(snappedX);

                        float y = object.getPos().y;
                        float snappedY = Math.round(y * 16f) / 16f;
                        object.setY(snappedY);
                    }
                }
            }
        }

        @Override
        public void writeAdditional(JsonObject object) {
            super.writeAdditional(object);
            object.addProperty("object_group", true);
            JsonArray array = new JsonArray();
            selected.ensureSafety();
            for (AbstractObject abstractObject : selected) {
                array.add(abstractObject.write());
            }
            object.add("objects", array);
        }

        @Override
        public String toString() {
            return "Selection";
        }

        @Override
        public void render(MatchView matchView) {
            super.render(matchView);
            //*vomit emoji* it must be done as it doesn't tick
            if(!ObjectEditor.isSelected(this) || this.selected.isEmpty()) {
                this.dispose();
            }
            isAllowedToChange = true;
        }
    }
}
