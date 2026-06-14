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
import com.hugo99j.chaosparty.ui.Debuggers;
import com.hugo99j.chaosparty.util.GlobalRunnables;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.hugo99j.chaosparty.util.ToRun;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.Main;

public class Player extends AdvancedObject {
    public static float MAX_HEALTH = 100.0f;

    public float health = MAX_HEALTH;

    @Override
    public void tick() {
        float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 500 : 300 ;
        float move = Math.max(speed-this.getVelocity().len(), 0);

        Vector2 movement = new Vector2(0, 0);

        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.add(0, 1);
        };
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement.add(-1, 0);
        };
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.add(0, -1);
        };
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement.add(1, 0);
        };

        if(Controllers.getCurrent() != null) {
            Vector2 controller = new Vector2(Controllers.getCurrent().getAxis(Controllers.getCurrent().getMapping().axisLeftX), -Controllers.getCurrent().getAxis(Controllers.getCurrent().getMapping().axisLeftY));
            if(controller.len() > 0.2f) movement = controller;
        }

        //diagonal isnt faster
        if(movement.len() > 1) movement.nor();

        if(Debuggers.isEnabled("freecam")) {
            float mul = 0.25f;
            Debuggers.freecam.add(new Vector2(movement.x*mul, movement.y*mul));
        }
        else if(movement.len() > 0) this.getPhysics().applyForceToCenter(new Vector2(movement.x*move, movement.y*move), true);
        super.tick();

        if(GameData.DEBUGGING) {
            this.getPhysics().getFixtureList().get(0).getFilterData().maskBits = (short) (Debuggers.isEnabled("noclip") ? 0 : -1);
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
        GameData.spriteBatch.draw(ImageUtil.get("player"), pos.x, pos.y, 1, 1);
        //pixelPerfect
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        Main.tempPlayer = this;
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
        return new Player();
    }

    public void damage(float amount) {
        if(Debuggers.isEnabled("invulnerable")) return;
        health-=amount;
        SoundManager.getSound("hurt").play(1);
        if(health <= 0) {
            ToRun.run(GlobalRunnables.FAIL_RUN);
        }
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
        return new Player();
    }
}
