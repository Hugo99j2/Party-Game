package com.hugo99j.chaosparty.ui.debugger;

import com.hugo99j.chaosparty.entity.AbstractObject;
import com.hugo99j.chaosparty.level.LevelLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.util.ToRun;

import java.util.UUID;

public class CopiedObjectSelection extends TemporaryDevObject {
    private final JsonArray objects;

    public CopiedObjectSelection(JsonArray objects) {
        super();
        this.objects = objects;
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        ObjectEditor.SelectionGroup group = new ObjectEditor.SelectionGroup();
        for (JsonElement object : objects) {
            AbstractObject created = LevelLoader.createObject(object.getAsJsonObject(), this.getLevel());
            //noinspection usagelimited
            created.setUUIDReallyUnsafeDoNotUse(UUID.randomUUID());
            group.selected.add(created);
        }
        this.getLevel().addObject(group);
        ToRun.run(() -> ObjectEditor.select(group));
        this.dispose();
    }

    @Override
    public String toString() {
        return "Copied object selection";
    }
}
