package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.ObjectPathfinder;
import org.lwjgl.openal.AL;

import java.util.List;
import java.util.function.Function;

public abstract class Activity {
    private final ObjectPathfinder pathfinder;
    private final Clown clown;
    private boolean hasStarted = false;
    private int ticksPassed = 0;

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
        ticksPassed++;
        this.getClown().setVelocity(Vector2.Zero);
    }

    public int getTicksPassed() {
        return ticksPassed;
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
