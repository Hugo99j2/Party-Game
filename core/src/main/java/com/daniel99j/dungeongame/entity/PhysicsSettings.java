package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public record PhysicsSettings(BodyDef.BodyType bodyType, Shape shape, float density, float drag) {
    public static PhysicsSettings create(float sizeX, float sizeY, float xOffset, float yOffset, float density, float drag) {
        sizeX /= 2;
        sizeY /= 2;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(sizeX, sizeY, new Vector2(sizeX+xOffset, sizeY+yOffset), 0);
        return new PhysicsSettings(BodyDef.BodyType.DynamicBody, shape, density, drag);
    }

    public static PhysicsSettings immovable(float sizeX, float sizeY, float xOffset, float yOffset) {
        return create(sizeX, sizeY, xOffset, yOffset, 999999999, 999999999);
    }
}
