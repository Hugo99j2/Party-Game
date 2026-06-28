package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.entity.AdvancedObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;

import static com.hugo99j.chaosparty.GameData.px;

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

        float speed = 20;
        float actualSpeed = Math.max(speed-this.getVelocity().len(), 0);
        if (move.len() > 0) this.getPhysics().applyForceToCenter(new Vector2(move.x * actualSpeed, move.y * actualSpeed), true);

        super.tick();
    }

    @Override
    public void render() {
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(ImageUtil.get("sheep"), pos.x, pos.y, 1, 1);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.create(px(14), 1, px(1), 0, 0.5f, 0.5f);
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

    public static Sheep createDefault() {
        return new Sheep();
    }
}
