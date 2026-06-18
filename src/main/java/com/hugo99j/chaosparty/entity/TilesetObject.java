package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.daniel99j.dungeongame.entity.StaticObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.google.gson.JsonObject;

public class TilesetObject extends StaticObject {
    private int width = 1;
    private int height = 1;
    private String sprite;
    private final Vector2 size;
    private boolean flipX, flipY;
    private float scale;
    private boolean hasHitbox;
    private int rotation;
    private Color tint;

    public TilesetObject(String sprite, int width, int height, boolean flipX, boolean flipY, int rotation, float scale, boolean hasHitbox, Color tint) {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
        this.scale = scale;
        //slightly extra so that
        this.size = new Vector2((ImageUtil.get(this.sprite).packedWidth / 16.0f), (ImageUtil.get(this.sprite).packedHeight / 16.0f));
        this.flipX = flipX;
        this.flipY = flipY;
        this.hasHitbox = hasHitbox;
        this.rotation = rotation;
        this.tint = tint.cpy();
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void render() {
        Color old = GameData.spriteBatch.getColor();
        GameData.spriteBatch.setColor(tint);
        for (float x = 0; x < this.width*this.size.x; x+=this.size.x) {
            for (float y = 0; y < this.height*this.size.y; y+=this.size.y) {
                //, 0, 0, (int)this.size.x*16, (int)this.size.y*16
                GameData.spriteBatch.draw(ImageUtil.get(sprite), this.getPos().x+x, this.getPos().y+y, 0.5f, 0.5f, this.size.x, this.size.y, flipX ? -1 : 1, flipY ? -1 : 1, rotation+90, true);
            }
        }
        GameData.spriteBatch.setColor(old);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("width", width);
        object.addProperty("height", height);
        object.addProperty("sprite", sprite);
        object.addProperty("flipX", flipX);
        object.addProperty("flipY", flipY);
        object.addProperty("hasHitbox", hasHitbox);
        object.addProperty("rotation", rotation);
        object.addProperty("scale", scale);
        object.addProperty("tint", tint.toString());
    }

    public static TilesetObject read(JsonObject object) {
        return new TilesetObject(object.get("sprite").getAsString(), object.get("width").getAsInt(), object.get("height").getAsInt(), object.get("flipX").getAsBoolean(), object.get("flipY").getAsBoolean(), object.get("rotation").getAsInt(), object.get("scale").getAsFloat(), object.get("hasHitbox").getAsBoolean(), Color.valueOf(object.get("tint").getAsString()));
    }

    @Override
    public ObjectType<TilesetObject> getType() {
        return ObjectTypes.TILESET;
    }

    @Override
    public float getLayer() {
        return RenderLayer.TILESETS;
    }

    @Override
    public String toString() {
        return "Tileset";
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setHasHitbox(boolean hasHitbox) {
        this.hasHitbox = hasHitbox;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setTint(Color tint) {
        this.tint = tint;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public static TilesetObject createDefault() {
        return new TilesetObject("sheep", 2, 2, false, false, 0, 1, false, Color.WHITE);
    }
}
