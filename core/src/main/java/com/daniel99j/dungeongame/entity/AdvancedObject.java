package com.daniel99j.dungeongame.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.hugo99j.chaosparty.GameData;

public abstract class AdvancedObject extends AbstractObject {
    @Override
    public abstract void render();

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        this.getLevel().getAdvancedObjects().add(this);
    }

    public void tick() {

    }
}
