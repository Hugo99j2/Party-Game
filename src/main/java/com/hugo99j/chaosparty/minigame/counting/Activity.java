package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.ObjectPathfinder;

import java.util.List;

public abstract class Activity {
    private final ObjectPathfinder pathfinder;
    private final Clown clown;
    private boolean hasStarted = false;

    public Activity(Clown clown) {
        this.pathfinder = new ObjectPathfinder(clown);
        this.clown = clown;
        this.getPathfinder().setTarget(this.getStartPos());
        this.getPathfinder().setAllowsShortPaths(true);
    }

    protected ObjectPathfinder getPathfinder() {
        return pathfinder;
    }

    public void tick() {
        if(!hasStarted) {
            this.pathfinder.tickPathfinder();
            if (this.clown.getPos().dst(this.getStartPos()) < 1) {
                //this.getClown().setVelocity(Vector2.Zero);
                List<Clown> clownsAtActivity = this.clown.getLevel().getObjectsInRadius(this.getStartPos(), 1, Clown.class, true, false, null);
                clownsAtActivity.removeIf((c) -> c.getActivity().getClass() != this.getClass() || c.getActivity().hasStarted);
                clownsAtActivity = clownsAtActivity.subList(0, Math.min(clownsAtActivity.size(), amountNeededToStart()));
                if(clownsAtActivity.size() >= amountNeededToStart()) {
                    int i = 0;
                    for (Clown clown1 : clownsAtActivity) {
                        clown1.getActivity().start(i);
                        i++;
                    }
                }
            }
        } else tickDuring();
    }

    protected void tickDuring() {
        this.getClown().setVelocity(Vector2.Zero);
    }

    protected abstract Vector2 getStartPos();

    protected int amountNeededToStart() {
        return 1;
    }

    protected void start(int index) {
        this.hasStarted = true;
    }

    protected boolean canStart() {
        return true;
    }

    public Clown getClown() {
        return clown;
    }

    public void addDebugInfo(List<String> lines) {
        lines.add("Started: "+this.hasStarted);
    }
}
