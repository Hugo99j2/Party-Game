package com.hugo99j.chaosparty.entity;

import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.*;

public class PlayerSpawnPoint extends AbstractObject {
    @NoDebugOption
    public final int id;

    public PlayerSpawnPoint(int id) {
        this.id = id;
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
    }

    @Override
    public void render(MatchView matchView) {
        GameData.spriteBatch.draw(ImageUtil.get("player_spawn"), this.getPos().x, this.getPos().y, 1, 1);
    }

    @Override
    public boolean shouldRender(MatchView view) {
        return super.shouldRender(view) && Debuggers.isEnabled("renderDevObjects");
    }

    @Override
    protected PhysicsSettings createPhysics() {
        //fine as it never actually will collide
        return PhysicsSettings.immovable(1, 1, 0, 0);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("id", this.id);
    }

    public static PlayerSpawnPoint read(JsonObject object) {
        return new PlayerSpawnPoint(object.get("id").getAsInt());
    }

    @Override
    public ObjectType<PlayerSpawnPoint> getType() {
        return ObjectTypes.PLAYER_SPAWN_POINT;
    }

    @Override
    public float getLayer() {
        return RenderLayer.PLAYER;
    }

    @Override
    public String toString() {
        return "Spawn #"+id;
    }

    public static PlayerSpawnPoint createDefault() {
        throw new IllegalArgumentException("Cannot create spawn point");
    }
}
