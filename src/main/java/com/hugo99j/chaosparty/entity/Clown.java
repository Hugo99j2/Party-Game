package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.CountingMinigame;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;

import static com.hugo99j.chaosparty.GameData.getCurrentMinigame;
import static com.hugo99j.chaosparty.GameData.px;

public class Clown extends AbstractObject {
    private int sheepTime = 0;
    private Vector2 move = Vector2.Zero;

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        if(NumberUtils.getRandomInt(1, 3) == 1 && getCurrentMinigame() instanceof CountingMinigame) this.dispose();
    }

    @Override
    public void tick() {
        sheepTime -= 1;
        if(sheepTime <= 0) {
            sheepTime = NumberUtils.getRandomInt(40, 100);
            if (NumberUtils.getRandomInt(1, 2) == 1) {
                move = new Vector2(NumberUtils.getRandomFloat(-1, 1), 0);
            } else move = Vector2.Zero.cpy();
            move.nor();
        }

        float speed = 20;
        float actualSpeed = Math.max(speed-this.getVelocity().len(), 0);
        if (move.len() > 0) this.getPhysics().applyForceToCenter(new Vector2(move.x * actualSpeed, move.y * actualSpeed), true);

        super.tick();
    }

    @Override
    public void render(MatchView matchView) {
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(ImageUtil.get("clown"), pos.x, pos.y, 1, 1);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.texture("clown", 0.5f, 0.5f, 1f, 1f);
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Clown read(JsonObject object) {
        return new Clown();
    }

    @Override
    public ObjectType<Clown> getType() {
        return ObjectTypes.CLOWN;
    }

    @Override
    public float getLayer() {
        return RenderLayer.NPC;
    }

    @Override
    public String toString() {
        return "Clown";
    }

    public static Clown createDefault() {
        return new Clown();
    }

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return super.shouldCollideWith(other) && !(other instanceof Clown);
    }
}
