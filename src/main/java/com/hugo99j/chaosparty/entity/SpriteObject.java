package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.daniel99j.dungeongame.entity.ObjectType;
import com.daniel99j.dungeongame.entity.PhysicsSettings;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.Animator;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderLayer;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.util.RequiresRefresh;

public class SpriteObject extends AbstractObject implements DontCollideTogether {
    private int width = 1;
    private int height = 1;
    private String sprite;
    private final Vector2 size;
    private boolean flipX, flipY;
    private float scale;
    @RequiresRefresh
    private boolean hasHitbox;
    @RequiresRefresh
    private boolean textureHitbox;
    private int rotation;
    private Color tint;
    @RequiresRefresh
    private boolean animated = false;

    public SpriteObject(String sprite, int width, int height, boolean flipX, boolean flipY, int rotation, float scale, boolean hasHitbox, Color tint, boolean textureHitbox, boolean animated) {
        this.sprite = sprite;
        this.animated = animated;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.size = new Vector2((getCurrentSprite().packedWidth / 16.0f), (getCurrentSprite().packedHeight / 16.0f));
        this.flipX = flipX;
        this.flipY = flipY;
        this.hasHitbox = hasHitbox;
        this.rotation = rotation;
        this.tint = tint.cpy();
        this.textureHitbox = textureHitbox;
    }

    private TextureAtlas.AtlasRegion getCurrentSprite() {
        if(animated) {
            return Animator.get(sprite, this);
        } else return ImageUtil.get(sprite);
    };

    @Override
    protected PhysicsSettings createPhysics() {
        PhysicsSettings settings = null;
        if(hasHitbox) {
            if(textureHitbox) {
                settings = PhysicsSettings.textureImmovable(sprite, this.width, this.height);
            } else settings = PhysicsSettings.immovable(this.width*this.size.x, this.height*this.size.y, 0, 0);

            settings = settings.group(CollisionCategories.PATHFIND_BLOCKING);
        }
        return settings;
    }

    @Override
    public void render(MatchView matchView) {
        Color old = GameData.spriteBatch.getColor().cpy();
        GameData.spriteBatch.setColor(tint);
        for (float x = 0; x < this.width*this.size.x; x+=this.size.x) {
            for (float y = 0; y < this.height*this.size.y; y+=this.size.y) {
                //, 0, 0, (int)this.size.x*16, (int)this.size.y*16
                GameData.spriteBatch.draw(getCurrentSprite(), this.getPos().x+x, this.getPos().y+y, 0.5f, 0.5f, this.size.x, this.size.y, flipX ? -1 : 1, flipY ? -1 : 1, rotation+90, true);
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
        object.addProperty("textureHitbox", textureHitbox);
        object.addProperty("animated", animated);
    }

    public static SpriteObject read(JsonObject object) {
        return new SpriteObject(object.get("sprite").getAsString(), object.get("width").getAsInt(), object.get("height").getAsInt(), object.get("flipX").getAsBoolean(), object.get("flipY").getAsBoolean(), object.get("rotation").getAsInt(), object.get("scale").getAsFloat(), object.get("hasHitbox").getAsBoolean(), Color.valueOf(object.get("tint").getAsString()), object.has("textureHitbox") ? object.get("textureHitbox").getAsBoolean() : false, object.has("animated") ? object.get("animated").getAsBoolean() : false);
    }

    @Override
    public ObjectType<SpriteObject> getType() {
        return ObjectTypes.TILESET;
    }

    @Override
    public float getLayer() {
        return RenderLayer.TILESETS;
    }

    @Override
    public String toString() {
        return "Sprite '"+this.sprite+"'";
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
        this.tint = tint.cpy();
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public static SpriteObject createDefault() {
        return new SpriteObject("sheep", 2, 2, false, false, 0, 1, false, Color.WHITE, false, false);
    }

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return super.shouldCollideWith(other) && !(other instanceof DontCollideTogether);
    }
}
