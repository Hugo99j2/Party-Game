package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.daniel99j.dungeongame.entity.*;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import org.jetbrains.annotations.Nullable;

import static com.hugo99j.chaosparty.GameData.px;

public class Potato extends AdvancedObject {
    private ParticleEffect hotEffect;
    private float spin = 0;
    private int ticksUntilCollision = 2;

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        hotEffect = new ParticleEffect();
        hotEffect.load(Gdx.files.internal(PathUtil.asset("particles/flame.p")), GameData.atlas);
        hotEffect.setEmittersCleanUpBlendFunction(false);
        hotEffect.scaleEffect(0.01f);
        hotEffect.setDuration(1000000);
        hotEffect.start();
        GameData.getLevelOrThrow().particles.add(hotEffect);
        this.getPhysics().setLinearDamping(2);
        hotEffect.setPosition(this.getPos().x+0.1f, this.getPos().y+0.4f);
        //this.getPhysics().getFixtureList().get(0).
    }

    @Override
    public void tick() {
        super.tick();
        ticksUntilCollision--;
        if(ticksUntilCollision == 0) {
            this.setPhysicsSettings(PhysicsSettings.create(px(14), 1, px(1), 0, 0.5f, 0.5f));
        }
        hotEffect.setPosition(this.getPos().x+0.1f, this.getPos().y+0.4f);
        if(this.getVelocity().len() < 1) {
            goSplat(null);
        }
    }

    @Override
    public void onCollision(Contact contact, AbstractObject object) {
        super.onCollision(contact, object);
        goSplat(object instanceof Player player ? player : null);
    }

    private void goSplat(@Nullable Player player) {
        var splat = new ParticleEffect();
        splat.load(Gdx.files.internal(PathUtil.asset("particles/splat.p")), GameData.atlas);
        splat.setEmittersCleanUpBlendFunction(false);
        splat.scaleEffect(0.01f);
        splat.start();
        splat.setPosition(this.getPos().x+0.5f, this.getPos().y+0.5f);
        GameData.getLevelOrThrow().particles.add(splat);
        this.dispose();
        if(player != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame potatoMinigame) {
            potatoMinigame.setHotPlayer(player.getMatchPlayer());
        }
    }

    @Override
    public void render(MatchView matchView) {
        spin += Gdx.graphics.getDeltaTime()*500;
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(ImageUtil.get("potato"), pos.x, pos.y, 0.5f, 0.5f, 1, 1, 1, 1, spin);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.create(px(14), 1, px(1), 0, 0.5f, 0.5f).collidesWith(CollisionCategories.allBut(CollisionCategories.PLAYER));
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Potato read(JsonObject object) {
        return new Potato();
    }

    @Override
    public ObjectType<Potato> getType() {
        return ObjectTypes.POTATO;
    }

    @Override
    public float getLayer() {
        return RenderLayer.NPC;
    }

    @Override
    public String toString() {
        return "Potato";
    }

    public static Potato createDefault() {
        return new Potato();
    }

    @Override
    public void dispose() {
        this.getLevel().stopEmitting(hotEffect);
        super.dispose();
    }
}
