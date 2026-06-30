package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.physics.box2d.Filter;
import com.hugo99j.chaosparty.util.RequiresRefresh;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.daniel99j.dungeongame.entity.StaticObject;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.util.RenderLayer;

public class CollisionObject extends StaticObject {
    @RequiresRefresh
    float sizeX = 1;
    @RequiresRefresh
    float sizeY = 1;

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(sizeX, sizeY, 0, 0).collidesWith(CollisionCategories.allBut(CollisionCategories.DONT_COLLIDE_WITH_EACH_OTHER)).group(CollisionCategories.DONT_COLLIDE_WITH_EACH_OTHER);
    }

    @Override
    public void render() {
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("sizeX", sizeX);
        object.addProperty("sizeY", sizeY);
    }

    public static CollisionObject read(JsonObject object) {
        CollisionObject c = new CollisionObject();
        c.sizeX = object.get("sizeX").getAsFloat();
        c.sizeY = object.get("sizeY").getAsFloat();
        return c;
    }

    @Override
    public ObjectType<CollisionObject> getType() {
        return ObjectTypes.COLLISION;
    }

    @Override
    public float getLayer() {
        return RenderLayer.NPC;
    }

    @Override
    public String toString() {
        return "Collision object";
    }

    public static CollisionObject createDefault() {
        return new CollisionObject();
    }
}
