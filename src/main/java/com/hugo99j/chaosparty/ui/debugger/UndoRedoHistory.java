package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.level.Level;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.Logger;

import java.util.*;


public class UndoRedoHistory {
    private static final ArrayList<String> revisions = new ArrayList<>();
    private static int currentRevision = 0;

    protected static void render() {
        if(GameData.level == null || GameData.getCurrentMatch() == null || !(GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            loadRevision(currentRevision+(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 1 : -1));
        }
    }

    protected static void onEdited() {
        if(GameData.level == null || GameData.getCurrentMatch() == null || !(GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor)) return;

        //weve undone then edited, clear future edits (redos)
        while(revisions.size() > (currentRevision + 1)) {
            revisions.removeLast();
        }

        createRevision();
    }

    private static void loadRevision(int id) {
        if(id < 0) return;
        if(id >= revisions.size()) return;
        List<AbstractObject> toReAdd = new ArrayList<>();
        for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
            if(allObject instanceof TemporaryDevObject dev && dev.keep()) toReAdd.add(allObject);
        }
        //super jank but it prevents the objects from being erased
        //noinspection usagelimited
        GameData.getLevelOrThrow().getRealListOfObjects().removeAll(toReAdd);
        GameData.getLevelOrThrow().dispose();
        Level out = LevelLoader.load(revisions.get(id));
        GameData.level = out;
        for (AbstractObject object : toReAdd) {
            Vector2 oldPos = object.getPos();
            UUID old = object.getUUID();
            object.dispose();
            object.unmarkRemoved();
            out.addObject(object);
            object.setUuid(old);
            object.setPos(oldPos);
        }
        currentRevision = id;
    }

    private static void createRevision() {
        try {
            String data = LevelLoader.saveLevel(GameData.getLevelOrThrow(), false);
            if(currentRevision < revisions.size() && currentRevision >= 0) {
                String old = revisions.get(currentRevision);
                if(old.equals(data)) {
                    Logger.info("Revision was same as previous");
                    return;
                }
            }
            Logger.info("Created revision");
            revisions.add(data);
            currentRevision++;
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
        currentRevision = -1;
        createRevision();
    }
}
