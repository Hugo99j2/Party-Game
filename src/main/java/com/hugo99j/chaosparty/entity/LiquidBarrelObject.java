package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.Contact;
import com.daniel99j.dungeongame.entity.*;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.RenderLayer;

import static com.hugo99j.chaosparty.GameData.px;

public class LiquidBarrelObject extends StaticObject {
    private String sprite;
    private boolean explosive;

    public LiquidBarrelObject(String sprite, boolean explosive) {
        this.sprite = sprite;
        this.explosive = explosive;
    }

    @Override
    public void onCollision(Contact contact, AbstractObject object) {
        super.onCollision(contact, object);
        if(object instanceof Potato) {
            explode();
        } else if(object instanceof Player player && GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame hotPotatoMinigame && hotPotatoMinigame.getHotPlayer().getPlayer().equals(player)) {
            explode();
        }
    }

    private void explode() {
        if(!explosive) return;
        var boom = new ParticleEffect();
        boom.load(Gdx.files.internal(PathUtil.asset("particles/boom.p")), GameData.atlas);
        boom.setEmittersCleanUpBlendFunction(false);
        boom.scaleEffect(0.01f);
        boom.start();
        boom.setPosition(this.getPos().x+0.5f, this.getPos().y+0.5f);
        GameData.getLevelOrThrow().particles.add(boom);
        this.dispose();
        SoundManager.getSound("explode").playSingle(1);
    }

    @Override
    public void render() {
        GameData.spriteBatch.draw(ImageUtil.get(sprite), this.getPos().x, this.getPos().y, 2, 2);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(1, 1.5f, 0, 0);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("sprite", sprite);
        object.addProperty("explosive", explosive);
    }

    public static LiquidBarrelObject read(JsonObject object) {
        return new LiquidBarrelObject(object.get("sprite").getAsString(), object.get("explosive").getAsBoolean());
    }

    @Override
    public ObjectType<LiquidBarrelObject> getType() {
        return ObjectTypes.LIQUID_BARREL;
    }

    @Override
    public float getLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Liquid Barrel";
    }

    public static LiquidBarrelObject createDefault() {
        return new LiquidBarrelObject("oil_drum", true);
    }
}
