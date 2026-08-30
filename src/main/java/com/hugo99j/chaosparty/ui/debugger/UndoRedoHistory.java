package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.AbstractObject;
import com.hugo99j.chaosparty.level.Level;
import com.hugo99j.chaosparty.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.Logger;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.*;


public class UndoRedoHistory {
    private static final ArrayList<String> revisions = new ArrayList<>();
    private static int currentRevision = 0;

    static {
        MenuBar.registerBuilder((b) -> {
            if(GameData.getCurrentMinigame() instanceof MapEditor) b.addItem("Edit", Input.Keys.Z, "Undo", () -> {
                loadRevision(currentRevision+(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 1 : -1));
            });
        });
    }

    protected static void onEdited() {
        if(!(GameData.getCurrentMinigame() instanceof MapEditor)) return;

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

            try {
                if(currentRevision > 0) {
                    String name = GameData.getCurrentMatch().getCurrentMinigame().getMapName();
                    if (name.contains(File.separator)) {
                        name = name.replace(File.separator, "_");
                        name = name.replace(":", "__");
                    }
                    Path path = Path.of("map_backups", name);
                    if (!Files.exists(path.getParent())) Files.createDirectory(path.getParent());

                    try (FileSystem zip = FileSystems.newFileSystem(URI.create("jar:" + path.toUri() + "_" + System.currentTimeMillis() + ".zip"), Map.of("create", "true"))) {
                        Path pathInZip = zip.getPath("/" + name);
                        Files.writeString(pathInZip, data);
                    }
                }
            } catch (Exception e) {
                Logger.error("Failed to create backup of revision", e);
            }
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
