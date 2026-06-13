package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.entity.ObjectTypes;

public class TilesetObject extends StaticObject {
    private int width = 1;
    private int height = 1;
    private String sprite;
    private final Vector2 size;

    public TilesetObject(String sprite, int width, int height) {
        this.sprite = sprite.contains("tilesets/") ? sprite : "tilesets/"+sprite;
        this.width = width;
        this.height = height;
        //slightly extra so that
        this.size = new Vector2((ImageUtil.get(this.sprite).packedWidth / 16.0f), (ImageUtil.get(this.sprite).packedHeight / 16.0f));
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return null;
    }

    @Override
    public void render() {
        for (float x = 0; x < this.width*this.size.x; x+=this.size.x) {
            for (float y = 0; y < this.height*this.size.y; y+=this.size.y) {
                //slightly more so that it doesnt have seams
                GameData.spriteBatch.draw(ImageUtil.get(sprite), this.getPos().x+x, this.getPos().y+y, this.size.x+0.0000000000000001f, this.size.y+0.0000000000000001f);
            }
        }
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("width", width);
        object.addProperty("height", height);
        object.addProperty("sprite", sprite);
    }

    public static TilesetObject read(JsonObject object) {
        return new TilesetObject(object.get("sprite").getAsString(), object.get("width").getAsInt(), object.get("height").getAsInt());
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

    public static TilesetObject createDefault() {
        return new TilesetObject("sheep", 2, 2);
    }
}
