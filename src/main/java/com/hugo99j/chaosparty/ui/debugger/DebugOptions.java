package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.hugo99j.chaosparty.ui.debugger.Debuggers.*;

public class DebugOptions {
    private static float lastTime = 0;
    private static int newMapEditorName = 0;
    protected static final List<String> newMapNames = new ArrayList<>();
    private static final ArrayList<Short> fpsCounter = new ArrayList<>();
    private static int savedRevision = 0;

    protected static void render() {
        ImGui.begin("Options");

        if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor mapEditor) {
            if(ImGui.button("Ruler")) {
                Ruler ruler = new Ruler();
                GameData.getLevelOrThrow().addObject(ruler);
                Vector3 camPos = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position;
                ruler.setPos(new Vector2(camPos.x, camPos.y));
                ObjectEditor.select(ruler);
            }
            if (ImGui.button("Save map")) {
                try {
                    Files.write(Path.of(PathUtil.codingDir(PathUtil.data("maps/" + mapEditor.getMapName() + ".map"))), LevelLoader.saveLevel(GameData.getLevelOrThrow(), true).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    savedRevision = UndoRedoHistory.getCurrentRevision();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        boolean notSaved = UndoRedoHistory.getCurrentRevision() != savedRevision && !ImGui.isKeyDown(ImGuiKey.ModAlt);
        if(notSaved) {
            ImGui.beginDisabled();
        }
        if (ImGui.button("Load map")) {
            try {
                GameData.startMatch(List.of(new MatchPlayer(User.getUser(5)))).setCurrentMinigame(new MapEditor(newMapNames.get(newMapEditorName)));
                savedRevision = UndoRedoHistory.getCurrentRevision();
            } catch (Exception e) {
                Logger.error("Error loading map", e);
            }
        }
        if(notSaved) {
            ImGui.endDisabled();
            ImGui.setItemTooltip("You have not saved the map! Hold ALT to bypass.");
        }
        ImGui.sameLine();
        ImInt newName = new ImInt(newMapEditorName);
        if(ImGui.combo("Map", newName, newMapNames.toArray(new String[0]))) {
            newMapEditorName = newName.get();
        }

        ValueHolder<Boolean> lastHeaderActive = new ValueHolder<>(false);
        debugOptions.forEach((s, valueHolder) -> {
            if (s.startsWith("__")) {
                lastHeaderActive.object = ImGui.collapsingHeader(s.replace("__", ""));
            } else {
                if (lastHeaderActive.object && ImGui.checkbox(s, valueHolder.object)) {
                    valueHolder.object = !valueHolder.object;
                    save();
                }
            }
        });

        float[] fpsArray = new float[fpsCounter.size()];
        int i = 0;

        for (Short f : fpsCounter) {
            fpsArray[i++] = f;
        }

        ImGui.plotLines("FPS graph", fpsArray, 100, 1, "", 0, 200, new ImVec2(0, 80));
        if (GameData.time > lastTime + 0.02f) {
            lastTime = GameData.time;
            if (fpsCounter.size() > 100) fpsCounter.removeFirst();
            fpsCounter.add((short) Gdx.graphics.getFramesPerSecond());
        }

        ImGui.text("Current FPS: " + Gdx.graphics.getFramesPerSecond());

        ImGui.text("Cached images: " + ImageUtil.size());
        ImGui.text("Cached files: " + PathUtil.size());
        ImGui.text("Cached sounds: " + SoundManager.size());
        ImGui.text("Revisions: " + UndoRedoHistory.size());
        ImGui.text("Current revision: " + UndoRedoHistory.getCurrentRevision());

        if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getMatchViews() != null && !GameData.getCurrentMatch().getMatchViews().isEmpty()) {
            slider("zoom", GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom, (e) -> {GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom = e;}, 0, 20, "%.3f");
        }

        ImGui.end();

    }

    protected static class Ruler extends TemporaryDevObject {
        @SuppressWarnings("unused")
        private float angle;

        @Override
        public void render(MatchView matchView) {
            Vector2 center = this.getPos().cpy();
            for (int i = -10; i < 10; i++) {
                Vector2 current = center.cpy().add(new Vector2(i, 0).rotateDeg(angle));
                GameData.spriteBatch.draw(ImageUtil.get("ruler"), current.x, current.y, 0, 0, 1, 1, 1, 1, angle);
            }
        }

        @Override
        public String toString() {
            return "Ruler";
        }
    }
}
