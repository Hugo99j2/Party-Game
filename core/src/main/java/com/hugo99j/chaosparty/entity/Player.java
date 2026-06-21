package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.daniel99j.dungeongame.entity.*;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.hugo99j.chaosparty.util.*;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.Main;

public class Player extends AdvancedObject {
    private final MatchPlayer matchPlayer;
    private boolean flip;

    public Player(MatchPlayer matchPlayer) {
        this.matchPlayer = matchPlayer;
        this.matchPlayer.setPlayer(this);
    }

    @Override
    public void tick() {
        super.tick();

        if(GameData.DEBUGGING) {
            this.getPhysics().getFixtureList().get(0).getFilterData().maskBits = (short) (Debuggers.isEnabled("noclip") ? 0 : -1);
        }
        if(this.getVelocity().len() > 0.3) {
            flip = this.getVelocity().x > 0.3;
        }
    }

    @Override
    public void render() {
        Vector2 pos = this.getPos();
        if(GameData.DEBUGGING && Debuggers.isEnabled("pixelPerfect")) {
            float m = 0.0625f;
            pos.x = Math.round(pos.x/m)*m;
            pos.y = Math.round(pos.y/m)*m;
        }
        //GameData.spriteBatch.draw(ImageUtil.get("player"), pos.x, pos.y, 1, 1);

        for (CostumePart value : CostumePart.values()) {
            if(value.shouldRender()) GameData.spriteBatch.draw(ImageUtil.get("costumes/"+this.matchPlayer.getUser().getWearing(value)), pos.x+(flip ? 1 : 0), pos.y, (flip ? -1 : 1), 1);
        }
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        Filter f = new Filter();
        f.categoryBits = CollisionCategories.PLAYER;
        this.getPhysics().getFixtureList().get(0).setFilterData(f);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f, new Vector2(0.5f, 0.5f), 0);
        return new PhysicsSettings(BodyDef.BodyType.DynamicBody, shape, 1.0f, 1.1f, CollisionCategories.DEFAULT, CollisionCategories.all());
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Player read(JsonObject object) {
        throw new IllegalArgumentException("Cannot create player");
    }

    @Override
    public ObjectType<Player> getType() {
        return ObjectTypes.PLAYER;
    }

    @Override
    public float getLayer() {
        return RenderLayer.PLAYER;
    }

    @Override
    public String toString() {
        return "Player";
    }

    public static Player createDefault() {
        throw new IllegalArgumentException("Cannot create player");
    }

    @Override
    public boolean shouldSave() {
        return false;
    }
}
