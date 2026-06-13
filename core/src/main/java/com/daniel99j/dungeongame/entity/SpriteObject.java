package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.daniel99j.dungeongame.RequiresRefresh;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.util.RenderLayer;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.entity.ObjectTypes;

public class SpriteObject extends StaticObject {
    private String sprite;
    private final PolygonShape hitbox;
    private final Vector2 size;
    @RequiresRefresh
    private float scale;
    private boolean flipX, flipY;
    @RequiresRefresh
    private boolean hasHitbox;

    public SpriteObject(String sprite) {
        this(sprite, 1.0f);
    }

    public SpriteObject(String sprite, float scale) {
        super();
        this.sprite = sprite;
        this.hitbox = new PolygonShape();
        this.size = new Vector2((GameData.atlas.findRegion(sprite).packedWidth / 16.0f)*scale, (GameData.atlas.findRegion(sprite).packedHeight / 16.0f)*scale);

        this.hitbox.setAsBox(this.size.x/2, this.size.y/2, new Vector2(0.5f*scale, 0.5f*scale), 0);
        this.scale = scale;
    }

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        if(!hasHitbox) return;
        Filter f = new Filter();
        f.categoryBits = (short) (CollisionCategories.LIGHT_BLOCKING | CollisionCategories.WALL);
        this.getPhysics().getFixtureList().get(0).setFilterData(f);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        if(!hasHitbox) return null;
        return new PhysicsSettings(BodyDef.BodyType.StaticBody, this.hitbox, 1.0f, 0.0f);
    }

    @Override
    public void render() {
        GameData.spriteBatch.draw(GameData.atlas.findRegion(sprite), flipX ? this.getPos().x+this.size.x : this.getPos().x, flipY ? this.getPos().y+this.size.y : this.getPos().y, flipX ? -this.size.x : this.size.x, flipY ? -this.size.y : this.size.y);
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("sprite", sprite);
        object.addProperty("scale", scale);
        object.addProperty("flipX", flipX);
        object.addProperty("flipY", flipY);
        object.addProperty("hitbox", hasHitbox);
    }

    public void setHasHitbox(boolean hasHitbox) {
        this.hasHitbox = hasHitbox;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public static SpriteObject read(JsonObject object) {
        SpriteObject o = new SpriteObject(object.get("sprite").getAsString(), object.get("scale").getAsFloat());
        o.setFlipX(object.get("flipX").getAsBoolean());
        o.setFlipY(object.get("flipY").getAsBoolean());
        o.setHasHitbox(object.get("hitbox").getAsBoolean());
        return o;
    }

    @Override
    public ObjectType<SpriteObject> getType() {
        return ObjectTypes.SPRITE;
    }

    @Override
    public float getLayer() {
        return RenderLayer.TILESET_OVERLAYS;
    }

    @Override
    public String toString() {
        return "SpriteObject{" +
            "sprite='" + sprite + '\'' +
            ", scale=" + scale +
            '}';
    }
}
