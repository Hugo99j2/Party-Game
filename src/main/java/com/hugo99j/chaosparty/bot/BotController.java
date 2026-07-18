package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.CachedPathfinder;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class BotController {
    private final CachedPathfinder pathfinder;
    private final Player player;
    private PathfindPos oldPos = null;
    private Vector2 pathfindTarget = null;
    private int nextNode = 0;
    private List<Integer> completed = new ArrayList<>();
    private ToDoubleFunction<PathfindPos> walkCost = null;

    public BotController(Player player) {
        this.player = player;
        this.pathfinder = new CachedPathfinder(GameData.createPathfinding(player).walkablePredicate(this.createWalkPredicate()).build(), this.getMaxDistance());
    }

    public void tick() {
        runPathfinding(false);
    }

    private void runPathfinding(boolean invalid) {
        if(GameData.DEBUGGING && Debuggers.isEnabled("disablePathfinding")) return;
        if(Debuggers.isEnabled("pathfindingRender")) {
            if(Debuggers.pathfindDebuggerTimers.getOrDefault(this.player.getUUID(), 99999) < 40) Debuggers.pathfindDebuggerTimers.put(this.player.getUUID(), 40);
        }
        Vector2 target = getTarget();
        if(target != null) {
            PathfindPos cachedTarget = toPathfindPos(target);
            PathfindPos pos = toPathfindPos(this.player.getPos().add(0.5f, 0.5f));
            List<PathfindPos> nodes = pathfinder.findPath(oldPos == null ? pos : oldPos, cachedTarget, pos);
            if(nodes.size() <= 2) return; //2 so that if the path is just start to end, dont do it
            //its a hack fix to stop it pathfinding to invalid places

            if(pathfinder.wasLastInvalid() || invalid) {
                oldPos = pos;
                nextNode = 1;
                completed.clear();
                if(!invalid) runPathfinding(true);
            } else {
//                int i = 0;
//                for (PathfindPos node : nodes) {
//                    if(new Vector2(node.getX(), node.getY()).add(0.5f, 0.5f).dst(this.player.getPos().add(0.5f, 0.5f)) < 0.1f) {
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
                if(this.getPlayer().getPos().dst(new Vector2(nodes.get(nextNode).getX(), nodes.get(nextNode).getY())) < 0.5f) {
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

            float speed = Math.max(this.getSpeed()-this.player.getVelocity().len(), 0);

            this.player.moveTowardTarget(new Vector2(nodes.get(nextNode).getX(), nodes.get(nextNode).getY()), speed);
        }
    }

    private static PathfindPos toPathfindPos(Vector2 pos) {
        return new PathfindPos((int) Math.floor(pos.x), (int) Math.floor(pos.y));
    }

    protected Predicate<PathfindPos> createWalkPredicate() {
        Vector4 hitbox = this.player.getHitboxWidthHeight(this.player.getPhysics().getFixtureList().first()).sub(this.player.getPos().x, this.player.getPos().y, 0, 0);
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
        return 6;
    }

    public float getMaxDistance() {
        return 5;
    }

    public Player getPlayer() {
        return player;
    }

    public void setTarget(Vector2 pathfindTarget) {
        this.pathfindTarget = pathfindTarget.cpy();
    }

    public CachedPathfinder getPathfinder() {
        return pathfinder;
    }

    public List<Integer> getCompleted() {
        return completed;
    }

    protected void setWalkCost(ToDoubleFunction<PathfindPos> walkCost) {
        if(walkCost == null) walkCost = (pos) -> 1.0D;
        if(walkCost != this.walkCost) {
            this.walkCost = walkCost;
            this.getPathfinder().setOptions(this.getPathfinder().getOptions().newBuilder().positionCostFunction(this.walkCost).build());
        }
    }
}
