package com.hugo99j.chaosparty.entity;

import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RequiresRefresh;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.util.RenderLayer;

import java.util.HashMap;
import java.util.Map;

public class CollisionObject extends AbstractObject implements DontCollideTogether {
    @RequiresRefresh
    float sizeX = 1;
    @RequiresRefresh
    float sizeY = 1;
    Map<String, Boolean> categories = new HashMap<>();

    public CollisionObject() {
        super();
        CollisionCategories.getAllCategories().forEach((name, key) -> categories.put(name, false));
    }

    @Override
    protected PhysicsSettings createPhysics() {
        ValueHolder<PhysicsSettings> settings = new ValueHolder<>(PhysicsSettings.immovable(sizeX, sizeY, 0, 0));
        categories.forEach((key, value) -> {
            if(value) {
                settings.object = settings.object.group(CollisionCategories.getAllCategories().get(key));
            }
        });
        return settings.object;
    }

    @Override
    public void render(MatchView matchView) {
        if(GameData.getCurrentMinigame() instanceof MapEditor) GameData.spriteBatch.draw(ImageUtil.get("barrier"), this.getPos().x, this.getPos().y, this.sizeX, this.sizeY);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("sizeX", sizeX);
        object.addProperty("sizeY", sizeY);
        JsonObject cat = new JsonObject();
        categories.forEach(cat::addProperty);
        object.add("categories", cat);
    }

    public static CollisionObject read(JsonObject object) {
        CollisionObject c = new CollisionObject();
        c.sizeX = object.get("sizeX").getAsFloat();
        c.sizeY = object.get("sizeY").getAsFloat();
        if(object.has("categories")) {
            for (String value : CollisionCategories.getAllCategories().keySet()) {
                c.categories.put(value, object.get("categories").getAsJsonObject().has(value) ? object.get("categories").getAsJsonObject().get(value).getAsBoolean() : false);
            }
        }
        return c;
    }

    @Override
    public ObjectType<CollisionObject> getType() {
        return ObjectTypes.COLLISION;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.TOP;
    }

    @Override
    public String toString() {
        return "Collision object";
    }

    public static CollisionObject createDefault() {
        CollisionObject c = new CollisionObject();
        c.categories.put("DEFAULT", true);
        return c;
    }

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return super.shouldCollideWith(other) && !(other instanceof DontCollideTogether);
    }
}
