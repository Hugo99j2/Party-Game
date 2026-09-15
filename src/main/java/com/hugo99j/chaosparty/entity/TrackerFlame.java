package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.CountingMinigame;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.minigame.counting.Activity;
import com.hugo99j.chaosparty.minigame.counting.JuggleActivity;
import com.hugo99j.chaosparty.minigame.counting.PyramidActivity;
import com.hugo99j.chaosparty.minigame.counting.WalkAroundActivity;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.hugo99j.chaosparty.GameData.getCurrentMinigame;
import static com.hugo99j.chaosparty.GameData.px;

public class TrackerFlame extends AbstractObject {
    private ObjectPathfinder pathfinder;
    private int remaining = 80;
    private ParticleEffect effect;
    private Player target;

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        this.pathfinder = new ObjectPathfinder(this);
        this.effect = this.getLevel().addParticle("tracker_flame", this.getPos());
        this.effect.setDuration((this.remaining/GameData.TICKS_PER_SECOND)*1000);
        if(GameData.getCurrentMinigame() instanceof HotPotatoMinigame hp) {
            List<Player> players = this.getLevel().getObjectsInRadius(this.getPos(), 400, Player.class, false, true, hp.getHotPlayer().getPlayerObject()).stream().filter((h) -> {return !h.isNoClip();}).toList();
            if(!players.isEmpty()) this.target = players.getFirst();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.pathfinder.setTarget(target.getPos());
        this.pathfinder.tickPathfinder();
        this.remaining--;
        this.effect.setPosition(this.getPos().x+0.5f, this.getPos().y+0.5f);
        if(this.remaining <= 0) this.dispose();
    }

    @Override
    public void render(MatchView matchView) {
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.create(px(4), px(4), px(6), px(6), 1, 2);
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static TrackerFlame read(JsonObject object) {
        return new TrackerFlame();
    }

    @Override
    public ObjectType<TrackerFlame> getType() {
        return ObjectTypes.TRACKER_FLAME;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Tracker flame";
    }

    public static TrackerFlame createDefault() {
        return new TrackerFlame();
    }

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return false;
    }
}
