package com.daniel99j.dungeongame.entity;

import com.hugo99j.chaosparty.match.MatchView;

public abstract class AdvancedObject extends AbstractObject {
    @Override
    public abstract void render(MatchView matchView);

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        this.getLevel().getAdvancedObjects().add(this);
    }

    public void tick() {

    }
}
