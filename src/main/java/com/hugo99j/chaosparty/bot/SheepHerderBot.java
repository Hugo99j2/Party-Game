
package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.entity.Sheep;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
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
        this.getPathfinder().setMaxDistance(0);
        this.getPathfinder().setAllowsShortPaths(true);
        this.getPathfinder().setStuckPrevention(true);
    }

    @Override
    public void tick() {
        if(mode == Mode.GOING_TO_TARGET) {
            Vector2 sheepPos = target.getPos().cpy().add(0.5f, 0.5f);
            Vector2 goalPos = goal.cpy();
            Vector2 toGoal = goalPos.cpy().sub(sheepPos).nor();
            float pushDistance = 1.0f;
            Vector2 edited = sheepPos.cpy().sub(toGoal.scl(pushDistance));

            this.getPathfinder().setTarget(edited);

            this.getPathfinder().setTarget(edited);

            Debuggers.customLevelRenderers.put((v) -> {
                GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GameData.shapeRenderer.setColor(Color.PINK);
                GameData.shapeRenderer.line(this.getPlayer().getPos().x, this.getPlayer().getPos().y, edited.x, edited.y);
                GameData.shapeRenderer.end();
            }, new ValueHolder<>(1));
        } else this.getPathfinder().setTarget(null);

        Debuggers.customLevelRenderers.put((v) -> {
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            GameData.shapeRenderer.setColor(Color.LIME);
            GameData.shapeRenderer.line(this.getPlayer().getPos().x, this.getPlayer().getPos().y, this.goal.x, this.goal.y);
            GameData.shapeRenderer.end();
        }, new ValueHolder<>(1));

        super.tick();
        tickSinceContact++;
        if(mode == Mode.NEEDS_TARGET || (mode == Mode.PUSHING_TARGET && target == null || target.getPos().dst(goal) < 4 || tickSinceContact >= 10)) {
            for (Sheep objectsInRadius : this.getPlayer().getLevel().getObjectsInRadius(this.getPlayer().getPos(), 30, Sheep.class, true, true, null)) {
                if (objectsInRadius.getPos().dst(goal) > 5) {
                    target = objectsInRadius;
                    mode = Mode.GOING_TO_TARGET;
                    break;
                }
            }
        }
        if(mode == Mode.GOING_TO_TARGET && this.getPathfinder().getTarget() != null && this.getPlayer().getPos().add(0.5f, 0.5f).dst(this.getPathfinder().getTarget().add(0.5f, 0.5f)) < 1.5f) {
            mode = Mode.GRABBING_TARGET;
        }
        if(mode == Mode.GRABBING_TARGET) {
            this.getPlayer().moveTowardTarget(target.getPos().add(0.5f, 0.5f), this.getPathfinder().getSpeed());
        }
        if(mode == Mode.PUSHING_TARGET) {
            this.getPlayer().moveTowardTarget(goal, this.getPathfinder().getSpeed());
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
    protected void addDebugInfo(List<String> info) {
        info.add("Target: " + Debuggers.devName(target));
        info.add("Goal: " + goal);
        info.add("State: " + mode);
    }

    public enum Mode {
        NEEDS_TARGET,
        GOING_TO_TARGET,
        GRABBING_TARGET,
        PUSHING_TARGET
    }

    public Mode getMode() {
        return mode;
    }
}
