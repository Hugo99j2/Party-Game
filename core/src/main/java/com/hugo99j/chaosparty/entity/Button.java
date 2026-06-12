package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.daniel99j.dungeongame.entity.AdvancedObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.daniel99j.dungeongame.util.RenderLayer;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.Main;

public class Button extends AdvancedObject {
    @Override
    public void render() {
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(GameData.atlas.findRegion("button"), pos.x, pos.y, 1, 1);
    }

    @Override
    public void tick() {
        if (this.getPos().dst(Main.tempPlayer.getPos()) < 2) {
            GameData.getCurrentGame().addScore(0, 1);
        }
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(0.2f, 1f, 0f, 0f);
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Button read(JsonObject object) {
        return new Button();
    }

    @Override
    public ObjectType<Button> getType() {
        return ObjectTypes.BUTTON;
    }

    @Override
    public float getLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Button";
    }
}
