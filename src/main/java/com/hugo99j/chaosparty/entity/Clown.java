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
import com.hugo99j.chaosparty.minigame.counting.Activity;
import com.hugo99j.chaosparty.minigame.counting.WalkAroundActivity;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;

import static com.hugo99j.chaosparty.GameData.getCurrentMinigame;
import static com.hugo99j.chaosparty.GameData.px;

public class Clown extends AbstractObject {
    private Activity activity;

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        if(NumberUtils.getRandomInt(1, 3) == 1 && getCurrentMinigame() instanceof CountingMinigame) this.dispose();
        else {
            this.activity = new WalkAroundActivity(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.activity.tick();
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
