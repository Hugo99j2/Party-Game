package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.*;

import java.util.HashMap;
import java.util.Map;

import static com.hugo99j.chaosparty.GameData.px;

public class DummyPlayer extends AbstractObject {
    @NoDebugOption
    private final Map<CostumePart, String> costume = new HashMap<>();
    @ShouldNotBeFinal
    private boolean flip;

    public DummyPlayer(boolean flip) {
        this.flip = flip;
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        for (CostumePart part : CostumePart.values()) {
            costume.put(part, Costumes.getVariants(part).get(NumberUtils.getRandomInt(0, Costumes.getVariants(part).size()-1)));
        }
    }

    @Override
    public void render(MatchView matchView) {
        Vector2 pos = this.getPos();
        for (CostumePart value : CostumePart.values()) {
            if(value.shouldRender()) GameData.spriteBatch.draw(ImageUtil.get("costumes/"+this.costume.get(value)), pos.x+(flip ? 1 : 0), pos.y, (flip ? -1 : 1), 1);
        }
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(px(10), px(16), px(3), 0);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("flip", flip);
    }

    public static DummyPlayer read(JsonObject object) {
        return new DummyPlayer(object.get("flip").getAsBoolean());
    }

    @Override
    public ObjectType<DummyPlayer> getType() {
        return ObjectTypes.DUMMY_PLAYER;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Dummy";
    }

    public static DummyPlayer createDefault() {
        return new DummyPlayer(false);
    }
}
