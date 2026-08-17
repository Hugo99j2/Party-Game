package com.hugo99j.chaosparty.entity;

import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.debugger.CopiedObjectSelection;
import com.hugo99j.chaosparty.util.RenderLayer;

public class TemporaryDevObject extends AbstractObject {
    @Override
    public void render(MatchView matchView) {

    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static TemporaryDevObject read(JsonObject object) {
        if(object.has("object_group")) {
            return new CopiedObjectSelection(object.get("objects").getAsJsonArray());
        }
        throw new RuntimeException("Cannot create");
    }

    @Override
    public ObjectType<TemporaryDevObject> getType() {
        return ObjectTypes.TEMP_DEV_OBJECT;
    }

    @Override
    public float getLayer() {
        return RenderLayer.UI;
    }

    @Override
    public String toString() {
        return "UNSET";
    }

    public static TemporaryDevObject createDefault() {
        throw new RuntimeException("Cannot create");
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    public boolean keep() {
        return true;
    }
}
