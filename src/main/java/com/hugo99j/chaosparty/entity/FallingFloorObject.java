package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;

import java.util.List;

import static com.hugo99j.chaosparty.GameData.px;

public class FallingFloorObject extends AbstractObject {
    private Color colour = Color.GREEN;

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(MatchView matchView) {
        Color old = GameData.spriteBatch.getColor().cpy();
        GameData.spriteBatch.setColor(colour);
        Vector2 pos = this.getPos();
        GameData.spriteBatch.draw(ImageUtil.get("falling_floor"), pos.x, pos.y, 3, 3);
        GameData.spriteBatch.setColor(old);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static FallingFloorObject read(JsonObject object) {
        return new FallingFloorObject();
    }

    @Override
    public ObjectType<FallingFloorObject> getType() {
        return ObjectTypes.FALLING_FLOOR;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Falling Floor";
    }

    public static FallingFloorObject createDefault() {
        return new FallingFloorObject();
    }

    public void makeRandomlyColoured() {
        List<Color> colors = List.of(Color.RED, Color.OLIVE, Color.YELLOW, Color.BLUE, Color.ORANGE, Color.PINK, Color.CYAN, Color.PURPLE, Color.LIME);
         this.colour = colors.get(NumberUtils.getRandomInt(0, colors.size()-1));
    }

    public Color getColour() {
        return colour;
    }
}
