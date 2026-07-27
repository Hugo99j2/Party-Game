package com.hugo99j.chaosparty.entity;

import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RequiresRefresh;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.util.RenderLayer;

public class CollisionObject extends AbstractObject implements DontCollideTogether {
    @RequiresRefresh
    float sizeX = 1;
    @RequiresRefresh
    float sizeY = 1;

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(sizeX, sizeY, 0, 0).group(CollisionCategories.PATHFIND_BLOCKING);
    }

    @Override
    public void render(MatchView matchView) {
        if(GameData.getCurrentMinigame() instanceof MapEditor) GameData.spriteBatch.draw(ImageUtil.get("barrier"), this.getPos().x, this.getPos().y, this.sizeX, this.sizeY);
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

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return super.shouldCollideWith(other) && !(other instanceof DontCollideTogether);
    }
}
