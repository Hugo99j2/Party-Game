package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.daniel99j.dungeongame.level.Level;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.Logger;

import java.util.ArrayList;


public class UndoRedoHistory {
    private static final ArrayList<String> revisions = new ArrayList<>();
    private static int currentRevision = 0;

    protected static void render() {
        if(GameData.level == null || GameData.getCurrentMatch() == null || !(GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            loadRevision(currentRevision-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            loadRevision(currentRevision+1);
        }
    }

    protected static void onEdited() {
        if(GameData.level == null || GameData.getCurrentMatch() == null || !(GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor)) return;
        //weve undone then edited, clear future edits (redos)
        while(revisions.size() > (currentRevision + 1)) {
            revisions.removeLast();
        }

        createRevision();
        currentRevision++;
    }

    private static void loadRevision(int id) {
        if(id < 0) return;
        if(id >= revisions.size()) return;
        GameData.getLevelOrThrow().dispose();
        Level out = LevelLoader.load(revisions.get(id));
        out.completedLoad();
        GameData.level = out;
        currentRevision = id;
    }

    private static void createRevision() {
        try {
            String data = LevelLoader.saveLevel(GameData.getLevelOrThrow(), false);
            Logger.info("Created revision");
            revisions.add(data);
        } catch (Exception e) {
            Logger.error("Failed to create revision", e);
        }
    }

    public static int size() {
        return revisions.size();
    }

    public static int getCurrentRevision() {
        return currentRevision;
    }

    public static void onMapLoad() {
        revisions.clear();
        currentRevision = 0;
        createRevision();
    }
}
