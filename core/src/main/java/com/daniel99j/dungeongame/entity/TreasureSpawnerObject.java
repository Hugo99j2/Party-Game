package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.util.GlobalRunnables;
import com.daniel99j.dungeongame.util.PathUtil;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.entity.ObjectTypes;

public class TreasureSpawnerObject extends StaticObject {
    private final float chance;
    private final String spawnType;

    public TreasureSpawnerObject(float chance, String spawnType) {
        this.chance = chance;
        this.spawnType = spawnType;
    }

    public void fire() {
        if(this.spawnType.equals("treasure") && NumberUtils.getRandomFloat(0, 1) <= this.chance) {
            TreasureObject treasure = new TreasureObject(GlobalRunnables.COLLECT_TREASURE, "coin", Color.valueOf("#fcb603"));
            treasure.setPos(this.getPos().add(NumberUtils.getRandomFloat(-0.2f, 0.2f), NumberUtils.getRandomFloat(-0.2f, 0.2f)));
            this.getLevel().addObject(treasure);

            ParticleEffect effect = new ParticleEffect();
            effect.load(Gdx.files.internal(PathUtil.asset("game/test.p")), GameData.atlas);
            effect.setEmittersCleanUpBlendFunction(false);
            effect.scaleEffect(0.01f);
            effect.start();

            effect.setPosition(treasure.getPos().x, treasure.getPos().y);

            this.getLevel().particles.add(effect);
        }
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void render() {

    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    @Override
    public ObjectType<?> getType() {
        return ObjectTypes.TREASURE_SPAWNER;
    }

    @Override
    public float getLayer() {
        return 0;
    }

    public static TreasureSpawnerObject read(JsonObject object) {
        return new TreasureSpawnerObject(object.get("chance").getAsFloat(), object.get("spawn_type").getAsString());
    }

    public String getSpawnType() {
        return spawnType;
    }
}
