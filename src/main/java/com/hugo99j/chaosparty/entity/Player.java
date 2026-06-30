package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.daniel99j.dungeongame.entity.*;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.hugo99j.chaosparty.util.*;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;

public class Player extends AdvancedObject {
    private final MatchPlayer matchPlayer;
    private boolean flip;
    private short lastMask = -10;

    public Player(MatchPlayer matchPlayer) {
        this.matchPlayer = matchPlayer;
        this.matchPlayer.setPlayer(this);
    }

    @Override
    public void tick() {
        super.tick();

        if(GameData.DEBUGGING && Debuggers.isEnabled("noclipToggleable")) {
            setNoClip(Debuggers.isEnabled("noclip"));
        }
        if(this.getVelocity().len() > 0.3) {
            flip = this.getVelocity().x > 0.3;
        }
    }

    @Override
    public void render(MatchView matchView) {
        Color old = GameData.spriteBatch.getColor().cpy();
        if(GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame potatoMinigame) {
            if(this.isNoClip()) {
                GameData.spriteBatch.setColor(new Color(1, 1, 1, 0.7f));
            }
        }
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
        GameData.spriteBatch.setColor(old);
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

    public MatchPlayer getMatchPlayer() {
        return matchPlayer;
    }

    @Override
    public void onCollision(Contact contact, AbstractObject object) {
        super.onCollision(contact, object);
        if(object instanceof Player player && GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame potatoMinigame && potatoMinigame.getHotPlayer().getPlayerObject() == this) {
            potatoMinigame.setHotPlayerAndCooldown(player.getMatchPlayer());
        }
    }

    public boolean isNoClip() {
        return lastMask != -10;
    }

    public void setNoClip(boolean noClip) {
        if(noClip && !isNoClip()) {
            lastMask = this.getPhysics().getFixtureList().get(0).getFilterData().maskBits;
            this.getPhysics().getFixtureList().get(0).getFilterData().maskBits = 0;
        }
        if(!noClip && isNoClip()) {
            this.getPhysics().getFixtureList().get(0).getFilterData().maskBits = lastMask;
            lastMask = -10;
        }
    }

    @Override
    public boolean shouldRender(MatchView view) {
        if(GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame potatoMinigame) {
            if(this.isNoClip() && view.getPlayer() != this.matchPlayer && (view.getPlayer() == null || view.getPlayer().getPlayerObject() == null || !view.getPlayer().getPlayerObject().isNoClip())) {
                return false;
            }
        }
        return super.shouldRender(view);
    }
}
