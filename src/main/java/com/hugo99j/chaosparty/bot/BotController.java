package com.hugo99j.chaosparty.bot;

import box2dLight.PointLight;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.CachedPathfinder;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.daniel99j.djutil.pathfinder.PathfinderOptions;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.entity.CollisionCategories;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.ui.ControllerInput;
import com.hugo99j.chaosparty.ui.ControllerUtil;
import com.hugo99j.chaosparty.ui.Debuggers;
import net.java.games.input.AbstractController;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class BotController {
    private final CachedPathfinder pathfinder;
    private final Player player;
    private PathfindPos oldPos = null;
    private int potatoCooldown = 0;
    private Vector2 pathfindTarget = null;

    public BotController(Player player) {
        this.player = player;
        this.pathfinder = new CachedPathfinder(GameData.createPathfinding(player).walkablePredicate(this.createWalkPredicate()).build(), 100);
    }

    public void tick() {
        runPathfinding(false);
        boolean isHot = GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame hp && hp.getHotPlayer() == player.getMatchPlayer();
        if(potatoCooldown < 0 && isHot) {
            //if(true) return;
            boolean tried = false;
            for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(player.getPos(), 15, Player.class, true, player)) {
                if(target.isNoClip()) continue; //skip dead players
                ValueHolder<Boolean> hit = new ValueHolder<>(false);
                player.getLevel().getBox2dWorld().rayCast((fixture, point, normal, fraction) -> {
                    //Skip players or explosive barrels
                    if(fixture.getBody().getUserData() instanceof Player || (fixture.getBody().getUserData() instanceof LiquidBarrelObject liquidBarrelObject && liquidBarrelObject.isExplosive())) {
                        return 0;
                    }
                    hit.object = true;
                    return 1;
                }, player.getPos(), target.getPos());
                if(hit.object != true) {
                    Potato potato = new Potato();
                    potato.setX(player.getPos().x);
                    potato.setY(player.getPos().y);
                    GameData.getLevelOrThrow().addObject(potato);
                    potato.moveTowardTarget(target.getPos(), 100);
                    tried = true;
                    break;
                }
            }
            potatoCooldown = (int) ((0.5f*GameData.TICKS_PER_SECOND)* NumberUtils.getRandomFloat(0.7f, 1.7f));

            if(!tried) {
                //move toward other players
                for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(player.getPos(), 45, Player.class, true, player)) {
                    if(target.isNoClip()) continue; //skip dead players
                    pathfindTarget = target.getPos().cpy();
                    break;
                }
            }
        }
        potatoCooldown--;
        if(!isHot) {
            potatoCooldown = (int) ((0.5f*GameData.TICKS_PER_SECOND)* NumberUtils.getRandomFloat(1.0f, 1.7f));
        }
    }

    private void runPathfinding(boolean invalid) {
        if(GameData.DEBUGGING && Debuggers.isEnabled("disablePathfinding")) return;
        if(Debuggers.isEnabled("pathfindingRender")) {
            if(Debuggers.pathfindDebuggerTimers.getOrDefault(String.valueOf(this.player.hashCode()), 99999) < 40) Debuggers.pathfindDebuggerTimers.put(String.valueOf(this.player.hashCode()), 40);
        }
        Vector2 target = getTarget();
        if(target != null) {
            PathfindPos cachedTarget = toPathfindPos(target);
            PathfindPos pos = toPathfindPos(this.player.getPos());
            PathfindPos nextPos = null;
            List<PathfindPos> nodes = pathfinder.findPath(oldPos == null ? pos : oldPos, cachedTarget, pos);
            if(nodes.size() <= 2) return; //2 so that if the path is just start to end, dont do it
            //its a hack fix to stop it pathfinding to invalid places

            if(pathfinder.wasLastInvalid() || invalid) {
                oldPos = pos;
                nextPos = nodes.get(1);
            } else {
                int i = 0;
                for (PathfindPos node : nodes) {
                    if(node.equals(pos)) {
                        if(i >= nodes.size()-1) return;
                        nextPos = nodes.get(i+1);
                        break;
                    }
                    i++;
                }

                if(nextPos == null) {
                    runPathfinding(true);
                    return;
                }
            }

            float speed = Math.max(this.getSpeed()-this.player.getVelocity().len(), 0);

            this.player.moveTowardTarget(new Vector2(nextPos.getX() + 0.5f, nextPos.getY() + 0.5f), speed);
        }
    }

    private static PathfindPos toPathfindPos(Vector2 pos) {
        return new PathfindPos((int) Math.floor(pos.x), (int) Math.floor(pos.y));
    }

    protected Predicate<PathfindPos> createWalkPredicate() {
        Vector4 hitbox = this.player.getHitboxWidthHeight(this.player.getPhysics().getFixtureList().first()).sub(this.player.getPos().x, this.player.getPos().y, 0, 0);
        Vector2 size = new Vector2(Math.abs(hitbox.x)+Math.abs(hitbox.z), Math.abs(hitbox.y)+Math.abs(hitbox.w));
        float distance = size.len()+0.1f;
        return (pos) -> {
            AtomicBoolean hit = new AtomicBoolean(false);
            QueryCallback callback = fixture -> {
                if (blocksPathfinding(fixture)) {
                    hit.set(true);
                    return false;
                }
                return true;
            };
            GameData.level.getBox2dWorld().QueryAABB(callback, pos.getX()+hitbox.x, pos.getY()+hitbox.y, pos.getX()+hitbox.z, pos.getY()+hitbox.w);
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
        return 4;
    }
}
