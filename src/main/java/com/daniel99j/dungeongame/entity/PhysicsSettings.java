package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.hugo99j.chaosparty.util.ImageUtil;

import static com.hugo99j.chaosparty.GameData.px;

public record PhysicsSettings(BodyDef.BodyType bodyType, Shape shape, float density, float drag, short collisionGroup, short collidesWith) {
    public static PhysicsSettings create(float sizeX, float sizeY, float xOffset, float yOffset, float density, float drag) {
        sizeX /= 2;
        sizeY /= 2;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(sizeX, sizeY, new Vector2(sizeX+xOffset, sizeY+yOffset), 0);
        return new PhysicsSettings(BodyDef.BodyType.DynamicBody, shape, density, drag, CollisionCategories.DEFAULT, CollisionCategories.all());
    }

    public static PhysicsSettings immovable(float sizeX, float sizeY, float xOffset, float yOffset) {
        return create(sizeX, sizeY, xOffset, yOffset, 999999999, 999999999);
    }

    public PhysicsSettings group(short collisionGroup) {
        PhysicsSettings newSettings = new PhysicsSettings(bodyType, shape, density, drag, collisionGroup, collidesWith);
        if(collisionGroup == CollisionCategories.LIGHT_BLOCKING) {
            newSettings = newSettings.andCollidesWith(CollisionCategories.LIGHT_BLOCKING);
        }
        return newSettings;
    }

    public PhysicsSettings andCollidesWith(short collidesWith) {
        return new PhysicsSettings(bodyType, shape, density, drag, collisionGroup, (short) (collidesWith | this.collidesWith));
    }

    public PhysicsSettings onlyCollidesWith(short collidesWith) {
        return new PhysicsSettings(bodyType, shape, density, drag, collisionGroup, collidesWith);
    }


    public static PhysicsSettings texture(String texture, float density, float drag, float scaleX, float scaleY) {
        Rectangle bounds = ImageUtil.getSize(texture);
        return create(px(bounds.width*scaleX), px(bounds.height*scaleY), px(bounds.x*scaleX), px(bounds.y*scaleY), density, drag);
    }

    public static PhysicsSettings textureImmovable(String texture, float scaleX, float scaleY) {
        return texture(texture, 999999999, 999999999, scaleX, scaleY);
    }
}
