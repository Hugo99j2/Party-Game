package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.PathfindPos;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.Logger;
import net.fabricmc.loader.impl.util.log.Log;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class HotPotatoBot extends BotController {
    private int potatoCooldown = 0;
    private Vector2 hidingSpot = Vector2.Zero;
    private static final ToDoubleFunction<PathfindPos> DISLIKE_HOT_PLAYER = (pos) -> {
        Vector2 hotPos = ((HotPotatoMinigame) GameData.getCurrentMinigame()).getHotPlayer().getPlayerObject().getPos();
        return Math.max(0, 20-new Vector2(pos.getX(), pos.getY()).dst(hotPos));
    };
    private Player targetedPlayer;

    public HotPotatoBot(Player player) {
        super(player);
        this.getPathfinder().setMaxDistance(3);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.getPlayer().isNoClip()) return;
        if(GameData.getCurrentMinigame() instanceof HotPotatoMinigame hp) {
            boolean isHot = hp.getHotPlayer() == this.getPlayer().getMatchPlayer();
            if (potatoCooldown < 0 && isHot) {
                boolean tried = false;
                for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(this.getPlayer().getPos(), 15, Player.class, true, true, this.getPlayer())) {
                    if (target.isNoClip()) continue; //skip dead players
                    if (!this.getPlayer().getLevel().raycast(this.getPlayer().getPos(), target.getPos(), (e) -> {
                        //Skip players or explosive barrels
                        if (e instanceof Player || (e instanceof LiquidBarrelObject liquidBarrelObject && liquidBarrelObject.isExplosive())) {
                            return false;
                        }
                        return true;
                    })) {
                        targetedPlayer = target;
                        //Just tag them if too close!
                        if (this.getPlayer().getPos().dst(target.getPos()) < 1) {
                            this.getPlayer().moveTowardTarget(target.getPos(), this.getPathfinder().getSpeed());
                        } else {
                            Potato potato = new Potato();
                            potato.setX(this.getPlayer().getPos().x);
                            potato.setY(this.getPlayer().getPos().y);
                            GameData.getLevelOrThrow().addObject(potato);
                            float accuracy = 10;
                            potato.moveTowardTarget(target.getPos(), 7000, NumberUtils.getRandomFloat(-accuracy, accuracy));
                            tried = true;
                            break;
                        }
                    }
                }
                potatoCooldown = (int) ((0.5f * GameData.TICKS_PER_SECOND) * NumberUtils.getRandomFloat(0.7f, 1.7f));

                if (!tried) {
                    //move toward other players
                    for (Player target : GameData.getLevelOrThrow().getObjectsInRadius(this.getPlayer().getPos(), 45, Player.class, true, true, this.getPlayer())) {
                        if (target.isNoClip()) continue; //skip dead players
                        this.getPathfinder().setTarget(target.getPos());
                        targetedPlayer = target;
                        break;
                    }
                }
            }
            potatoCooldown--;
            if (!isHot) {
                targetedPlayer = null;
                this.getPathfinder().setWalkCost(DISLIKE_HOT_PLAYER);
                potatoCooldown = (int) ((0.5f * GameData.TICKS_PER_SECOND) * NumberUtils.getRandomFloat(1.0f, 1.7f));

                // || this.hidingSpot.dst(hp.getHotPlayer().getPlayerObject().getPos()) < 15
                if (this.getPathfinder().getPathfinder().wasLastInvalid() || (this.getPlayer().getPos().dst(hidingSpot) < 2)) {
                    hidingSpot = getPlayer().getPos();
                    while (hidingSpot.dst(this.getPlayer().getPos()) < 10) {
                        hidingSpot = new Vector2(NumberUtils.getRandomInt(0, 62),  NumberUtils.getRandomInt(0, 32));
                    }
                }
                this.getPathfinder().setTarget(hidingSpot);
            } else {
                this.getPathfinder().setWalkCost(null);
            }
        }
    }

    @Override
    protected void addDebugInfo(List<String> info) {
        info.add("Target: " + Debuggers.devName(targetedPlayer));
        info.add("Cooldown: " + potatoCooldown);
        info.add("Hiding spot: " + hidingSpot);
    }
}
