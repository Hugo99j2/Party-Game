package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.entity.AdvancedObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.util.GlobalRunnables;
import com.daniel99j.dungeongame.util.Logger;
import com.daniel99j.dungeongame.util.RenderLayer;
import com.daniel99j.dungeongame.util.ScheduledRunnables;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.Main;
import com.hugo99j.chaosparty.ui.Debuggers;

public class Sheep extends AdvancedObject {
    private int sheepTime = 0;
    private Vector2 move = Vector2.Zero;

    @Override
    public void tick() {
        sheepTime -= 1;
        if(sheepTime <= 0) {
            sheepTime = NumberUtils.getRandomInt(40, 100);
            if (NumberUtils.getRandomInt(1, 2) == 1) {
                move = new Vector2(NumberUtils.getRandomFloat(-1, 1), NumberUtils.getRandomFloat(-1, 1));
            } else move = Vector2.Zero.cpy();
            move.nor();
        }
        float speed = 1f;
        if (move.len() > 0) this.getPhysics().setLinearVelocity(move.x * speed, move.y * speed);

        super.tick();
    }

    @Override
    public void render() {
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(GameData.atlas.findRegion("sheep"), pos.x, pos.y, 1, 1);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.create(1, 1, 0.5f, 0.5f, 0.5f, 0.5f);
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Sheep read(JsonObject object) {
        return new Sheep();
    }

    @Override
    public ObjectType<Sheep> getType() {
        return ObjectTypes.SHEEP;
    }

    @Override
    public float getLayer() {
        return RenderLayer.NPC;
    }

    @Override
    public String toString() {
        return "Sheep";
    }
}
