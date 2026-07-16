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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class HotPotatoBot extends BotController {
    private int potatoCooldown = 0;

    public HotPotatoBot(Player player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        boolean isHot = GameData.getCurrentMinigame() instanceof HotPotatoMinigame hp && hp.getHotPlayer() == this.getPlayer().getMatchPlayer();
        if(potatoCooldown < 0 && isHot) {
            //if(true) return;
            boolean tried = false;
            for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(this.getPlayer().getPos(), 15, Player.class, true, this.getPlayer())) {
                if(target.isNoClip()) continue; //skip dead players
                ValueHolder<Boolean> hit = new ValueHolder<>(false);
                this.getPlayer().getLevel().getBox2dWorld().rayCast((fixture, point, normal, fraction) -> {
                    //Skip players or explosive barrels
                    if(fixture.getBody().getUserData() instanceof Player || (fixture.getBody().getUserData() instanceof LiquidBarrelObject liquidBarrelObject && liquidBarrelObject.isExplosive())) {
                        return 0;
                    }
                    hit.object = true;
                    return 1;
                }, this.getPlayer().getPos(), target.getPos());
                if(hit.object != true) {
                    Potato potato = new Potato();
                    potato.setX(this.getPlayer().getPos().x);
                    potato.setY(this.getPlayer().getPos().y);
                    GameData.getLevelOrThrow().addObject(potato);
                    potato.moveTowardTarget(target.getPos(), 100, NumberUtils.getRandomFloat(-10, 10));
                    tried = true;
                    break;
                }
            }
            potatoCooldown = (int) ((0.5f*GameData.TICKS_PER_SECOND)* NumberUtils.getRandomFloat(0.7f, 1.7f));

            if(!tried) {
                //move toward other players
                for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(this.getPlayer().getPos(), 45, Player.class, true, this.getPlayer())) {
                    if(target.isNoClip()) continue; //skip dead players
                    this.setTarget(target.getPos());
                    break;
                }
            }
        }
        potatoCooldown--;
        if(!isHot) {
            potatoCooldown = (int) ((0.5f*GameData.TICKS_PER_SECOND)* NumberUtils.getRandomFloat(1.0f, 1.7f));
        }
    }
}
