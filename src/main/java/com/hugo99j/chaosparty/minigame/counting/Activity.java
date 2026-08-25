package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.ObjectPathfinder;

public class Activity {
    private final ObjectPathfinder pathfinder;

    public Activity(Clown clown) {
        this.pathfinder = new ObjectPathfinder(clown);
        this.getPathfinder().setTarget(this.getStartPos());
    }

    protected ObjectPathfinder getPathfinder() {
        return pathfinder;
    }

    public void tick() {
        this.pathfinder.tickPathfinder();
    }

    protected Vector2 getStartPos() {
        return new Vector2(10, 10);
    }

}
