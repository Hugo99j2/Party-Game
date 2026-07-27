
package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.entity.Sheep;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.util.Logger;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class SheepHerderBot extends BotController {
    private Sheep target = null;
    private final Vector2 goal;
    private Mode mode = Mode.NEEDS_TARGET;
    private int tickSinceContact = 0;

    public SheepHerderBot(Player player) {
        super(player);
        int id = GameData.getCurrentMatch().getPlayers().indexOf(player.getMatchPlayer());
        if(id == 0) {
            goal = new Vector2(2, 14);
        } else if(id == 1) {
            goal = new Vector2(30, 14);
        } else if(id == 2) {
            goal = new Vector2(2, 2);
        } else if(id == 3) {
            goal = new Vector2(30, 2);
        } else throw new IllegalArgumentException("Invalid id: " + id);
    }

    @Override
    public void tick() {
        super.tick();
        tickSinceContact++;
        if(mode == Mode.NEEDS_TARGET || (mode == Mode.PUSHING_TARGET && target == null || target.getPos().dst(goal) < 5 || tickSinceContact >= 5)) {
            for (Sheep objectsInRadius : this.getPlayer().getLevel().getObjectsInRadius(this.getPlayer().getPos(), 30, Sheep.class, true, true, null)) {
                if (objectsInRadius.getPos().dst(goal) > 5) {
                    target = objectsInRadius;
                    mode = Mode.GOING_TO_TARGET;
                    break;
                }
            }
        }
        if(mode == Mode.GOING_TO_TARGET && this.getPlayer().getPos().dst(this.getTarget()) < 0.5f) {
            mode = Mode.GRABBING_TARGET;
        }
        if(mode == Mode.GRABBING_TARGET) {
            this.getPlayer().moveTowardTarget(target.getPos().add(0.5f, 0.5f), this.getSpeed());
        }
        if(mode == Mode.PUSHING_TARGET) {
            this.getPlayer().moveTowardTarget(goal, this.getSpeed());
            if(this.getPlayer().getPos().dst(target.getPos()) > 2) {
                mode = Mode.NEEDS_TARGET;
            }
        }
    }

    public void onSheepHit(Sheep sheep) {
        if(sheep == target && mode == Mode.GRABBING_TARGET) {
            mode = Mode.PUSHING_TARGET;
        }
        if(sheep == target) {
            tickSinceContact = 0;
        }
    }

    @Override
    public boolean allowsShortPaths() {
        return true;
    }

    @Override
    protected void addDebugInfo(List<String> info) {
        info.add("Target: " + target.getEntityId());
        info.add("Goal: " + goal);
        info.add("State: " + mode);
    }

    @Override
    public float getMaxDistance() {
        return 1;
    }

    @Override
    public @Nullable Vector2 getTarget() {
        if(mode == Mode.GOING_TO_TARGET) {
            Vector2 change = goal.cpy().sub(16, 8);
            change.x = Math.clamp(change.x, -1, 1);
            change.y = Math.clamp(change.y, -1, 1);
            return target.getPos().cpy().sub(change.scl(1));
        }
        return null;
    }

    public enum Mode {
        NEEDS_TARGET,
        GOING_TO_TARGET,
        GRABBING_TARGET,
        PUSHING_TARGET
    }

    @Override
    protected Predicate<PathfindPos> createWalkPredicate() {
        return super.createWalkPredicate();
    }

    public Mode getMode() {
        return mode;
    }
}
