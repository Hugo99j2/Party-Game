package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.hugo99j.chaosparty.util.ShouldNotBeFinal;

public class Zipline extends AbstractObject {
    @ShouldNotBeFinal
    private float segmentYOffset;
    @ShouldNotBeFinal
    private float segmentWidth;
    @ShouldNotBeFinal
    private float segments;
    @ShouldNotBeFinal
    private String texture;

    public Zipline(float segmentYOffset, float segmentWidth, float segments, String texture) {
        this.segmentYOffset = segmentYOffset;
        this.segmentWidth = segmentWidth;
        this.segments = segments;
        this.texture = texture;
    }

    @Override
    public void render(MatchView matchView) {
        Vector2 pos = this.getPos();
        for (int i = 0; i < segments; i++) {
            GameData.spriteBatch.draw(ImageUtil.get(texture), pos.x+(i*segmentWidth), pos.y+(i*segmentYOffset), segmentWidth, 1);
        }
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("segment_y_offset", segmentYOffset);
        object.addProperty("segment_width", segmentWidth);
        object.addProperty("segments", segments);
        object.addProperty("texture", texture);
    }

    public static Zipline read(JsonObject object) {
        return new Zipline(object.get("segment_y_offset").getAsFloat(), object.get("segment_width").getAsFloat(), object.get("segments").getAsFloat(), object.get("texture").getAsString());
    }

    @Override
    public ObjectType<Zipline> getType() {
        return ObjectTypes.ZIPLINE;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Zipline";
    }

    public static Zipline createDefault() {
        return new Zipline(1, 1, 10, "zipline");
    }
}
