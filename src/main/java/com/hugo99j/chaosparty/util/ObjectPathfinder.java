package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.daniel99j.djutil.pathfinder.CachedPathfinder;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class ObjectPathfinder {
    private final CachedPathfinder pathfinder;
    private final AbstractObject object;
    private PathfindPos oldPos = null;
    private Vector2 pathfindTarget = null;
    private int nextNode = 0;
    private List<Integer> completed = new ArrayList<>();
    private ToDoubleFunction<PathfindPos> walkCost = null;
    private boolean allowsShortPaths = false;
    private float speed = 200;
    private float maxDistance = 5;

    public ObjectPathfinder(AbstractObject object) {
        this.object = object;
        this.pathfinder = new CachedPathfinder(GameData.createPathfinding(object).walkablePredicate(this.createWalkPredicate()).build(), this.getMaxDistance());
    }

    public void tickPathfinder() {
        runPathfinding(false);
    }

    private void runPathfinding(boolean invalid) {
        if(GameData.DEBUGGING && Debuggers.isEnabled("disablePathfinding")) return;
        if(Debuggers.isEnabled("pathfindingRender")) {
            if(Debuggers.pathfindDebuggerTimers.getOrDefault(this.object.getUUID(), 99999) < 40) Debuggers.pathfindDebuggerTimers.put(this.object.getUUID(), 40);
        }
        Vector2 target = getTarget();
        if(target != null) {
            PathfindPos cachedTarget = toPathfindPos(target);
            PathfindPos pos = toPathfindPos(this.object.getPos().add(0.5f, 0.5f));
            List<PathfindPos> nodes = pathfinder.findPath(oldPos == null ? pos : oldPos, cachedTarget, pos);

            //2 so that if the path is just start to end, dont do it
            //its a hack fix to stop it pathfinding to invalid places and getting stuck
            boolean isShort = nodes.size() <= 2;
            if(isShort && allowsShortPaths()) {
                this.object.moveTowardTarget(target, this.getSpeed());
                return;
            } else if(isShort) return;

            if(pathfinder.wasLastInvalid() || invalid) {
                oldPos = pos;
                nextNode = 1;
                completed.clear();
                if(!invalid) runPathfinding(true);
            } else {
//                int i = 0;
//                for (PathfindPos node : nodes) {
//                    if(new Vector2(node.getX(), node.getY()).add(0.5f, 0.5f).dst(this.object.getPos().add(0.5f, 0.5f)) < 0.1f) {
//                        if(i >= nodes.size()-1) return;
//                        nextPos = nodes.get(i+1);
//                        break;
//                    }
//                    i++;
//                }
//
//                if(nextPos == null) {
//                    runPathfinding(true);
//                    return;
//                }
                if(this.getObject().getPos().dst(new Vector2(nodes.get(nextNode).getX(), nodes.get(nextNode).getY())) < 0.5f) {
                    nextNode++;
                }

                if(GameData.DEBUGGING && Debuggers.isEnabled("pathfindingRender")) {
                    for (PathfindPos node : nodes) {
                        if(nodes.indexOf(node) <= nextNode) {
                            completed.add(node.hashCode());
                        }
                    }
                }

                if(nextNode < 0 || nextNode >= nodes.size()) {
                    runPathfinding(true);
                    return;
                }
            }

            float speed = Math.max(this.getSpeed()-this.object.getVelocity().len(), 0);

            this.object.moveTowardTarget(new Vector2(nodes.get(nextNode).getX(), nodes.get(nextNode).getY()), speed);
        }
    }

    public boolean allowsShortPaths() {
        return allowsShortPaths;
    }

    public void setAllowsShortPaths(boolean allowsShortPaths) {
        this.allowsShortPaths = allowsShortPaths;
    }

    private static PathfindPos toPathfindPos(Vector2 pos) {
        return new PathfindPos((int) Math.floor(pos.x), (int) Math.floor(pos.y));
    }

    protected Predicate<PathfindPos> createWalkPredicate() {
        Vector4 hitbox = this.object.getHitboxWidthHeight(this.object.getPhysics().getFixtureList().first()).sub(this.object.getPos().x, this.object.getPos().y, 0, 0);
        Vector2 size = new Vector2(Math.abs(hitbox.x)+Math.abs(hitbox.z), Math.abs(hitbox.y)+Math.abs(hitbox.w));
        float distance = size.len()+0.2f;
        return (pos) -> {
            AtomicBoolean hit = new AtomicBoolean(false);
            QueryCallback callback = fixture -> {
                if (blocksPathfinding(fixture)) {
                    hit.set(true);
                    return false;
                }
                return true;
            };
            GameData.getLevelOrThrow().getBox2dWorld().QueryAABB(callback, pos.getX()+hitbox.x, pos.getY()+hitbox.y, pos.getX()+hitbox.z+hitbox.x, pos.getY()+hitbox.w+hitbox.y);
            return !hit.get();
        };
    }

    private boolean blocksPathfinding(Fixture fixture) {
        short categoryBits = fixture.getFilterData().categoryBits;
        return (categoryBits & CollisionCategories.WALL) != 0
            || (categoryBits & CollisionCategories.PATHFIND_BLOCKING) != 0;
    }

    public @Nullable Vector2 getTarget() {
        return pathfindTarget;
    };

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public AbstractObject getObject() {
        return object;
    }

    public void setTarget(@Nullable Vector2 pathfindTarget) {
        if(pathfindTarget == null) this.pathfindTarget = null;
        else this.pathfindTarget = pathfindTarget.cpy();
    }

    public CachedPathfinder getPathfinder() {
        return pathfinder;
    }

    public List<Integer> getCompleted() {
        return completed;
    }

    public void setWalkCost(ToDoubleFunction<PathfindPos> walkCost) {
        if(walkCost == null) walkCost = (pos) -> 1.0D;
        if(walkCost != this.walkCost) {
            this.walkCost = walkCost;
            this.getPathfinder().setOptions(this.getPathfinder().getOptions().newBuilder().positionCostFunction(this.walkCost).build());
        }
    }
}
