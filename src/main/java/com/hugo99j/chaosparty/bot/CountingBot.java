package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountingBot extends BotController {
    //The bots cant do crosseyes :(
    private int REAL_AMOUNT;
    private Vector2 eyeball;
    private final List<Clown> activeTracked = new ArrayList<>();
    private final List<Clown> droppingTracking = new ArrayList<>();
    private final Map<Vector2, Float> lookBias = new HashMap<>();
    private int currentCount = 0;
    private static final float VIEW_RADIUS = 2f;
    private static final float PERIPHERAL_RADIUS = VIEW_RADIUS*2f;
    private int timeUntilMoveEye = 0;
    private int eyeMoveTime = 0;
    private boolean verifyMode = false;
    private final List<Clown> alreadyVerified = new ArrayList<>();
    private int verifyCooldown = 0;
    private int giveUpOnVerifying = 0;
    private int ticksSinceBigChange = 0;

    public CountingBot(Player player) {
        super(player);
        this.REAL_AMOUNT = this.getPlayer().getLevel().getObjectsBetweenClass(Vector2.Zero, new Vector2(100, 100), Clown.class, false).size();
        this.getPathfinder().setMaxDistance(3);

        lookBias.put(new Vector2(VIEW_RADIUS, VIEW_RADIUS), 20f);
        lookBias.put(new Vector2(VIEW_RADIUS, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight/2f - VIEW_RADIUS /2f), 20f);
        lookBias.put(new Vector2(VIEW_RADIUS, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight - VIEW_RADIUS), 20f);

        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth/2f - VIEW_RADIUS /2f, VIEW_RADIUS), 20f);
        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth/2f - VIEW_RADIUS /2f, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight/2f - VIEW_RADIUS /2f), 20f);
        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth/2f - VIEW_RADIUS /2f, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight - VIEW_RADIUS), 20f);

        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth - VIEW_RADIUS, VIEW_RADIUS), 20f);
        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth - VIEW_RADIUS, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight/2f - VIEW_RADIUS /2f), 20f);
        lookBias.put(new Vector2(GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth - VIEW_RADIUS, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight - VIEW_RADIUS), 20f);

    }

    @Override
    public void tick() {
        super.tick();
        int old = this.currentCount;

        if(timeUntilMoveEye == 0) {
            //Find highest bias
            Vector2 best = null;
            float bestValue = -1000;
            for (Map.Entry<Vector2, Float> e : lookBias.entrySet()) {
                if (e.getValue() > bestValue) {
                    bestValue = e.getValue();
                    best = e.getKey();
                }
            }

            if (best != null) {
                lookBias.remove(best);
                eyeball = best;
            } else {
                eyeball = new Vector2(NumberUtils.getRandomFloat(VIEW_RADIUS / 2f, GameData.getCurrentMatch().getMatchViews().getFirst().worldWidth - VIEW_RADIUS / 2f), NumberUtils.getRandomFloat(VIEW_RADIUS / 2f, GameData.getCurrentMatch().getMatchViews().getFirst().worldHeight - VIEW_RADIUS / 2f));
            }

            eyeMoveTime = NumberUtils.getRandomInt(3, 6);
            timeUntilMoveEye = NumberUtils.getRandomInt(8, 12);
        } else if(eyeMoveTime == 0 && timeUntilMoveEye > 0) timeUntilMoveEye--;
        if(eyeMoveTime > 0) eyeMoveTime--;

        if(eyeMoveTime == 0 && timeUntilMoveEye != 0 && NumberUtils.getRandomInt(1, 3) == 1) {
            for (Clown directlySee : this.getPlayer().getLevel().getObjectsInRadius(eyeball, VIEW_RADIUS, Clown.class, false, false, null)) {
                if(this.activeTracked.contains(directlySee)) continue;
                this.activeTracked.add(directlySee);
                if(NumberUtils.getRandomInt(1, 40) != 1) changeCount(1);
            }
            for (Clown almostSee : this.getPlayer().getLevel().getObjectsInRadius(eyeball, PERIPHERAL_RADIUS, Clown.class, false, false, null)) {
                if(this.activeTracked.contains(almostSee)) continue;
                if(this.droppingTracking.contains(almostSee) && NumberUtils.getRandomInt(1, 5) == 1) {
                    this.activeTracked.add(almostSee);
                    continue;
                }
                lookAt(almostSee, 1f);
                if(!this.droppingTracking.contains(almostSee)) {
                    this.droppingTracking.add(almostSee);
                    if (NumberUtils.getRandomInt(1, 30) != 1) changeCount(1);
                }
            }
        }

        List<Clown> forgot = new ArrayList<>();
        for (Clown clown : this.droppingTracking) {
            if(NumberUtils.getRandomInt(1, 200/Math.max(1, (this.droppingTracking.size()*2+this.activeTracked.size())/10)) == 1) {
                forgot.add(clown);
                if (NumberUtils.getRandomInt(1, 20) != 1) changeCount(-1);
                if(NumberUtils.getRandomInt(1, 2) == 1) this.lookAt(clown, 0.8f);
            }
            if(NumberUtils.getRandomInt(1, 100) == 1) this.lookAt(clown, 0.3f);
        }
        this.droppingTracking.removeAll(forgot);

        List<Clown> forgetting = new ArrayList<>();
        for (Clown clown : this.activeTracked) {
            if(NumberUtils.getRandomInt(1, 200) == 1) {
                forgetting.add(clown);
                this.lookAt(clown, 2f);
            }
        }
        this.droppingTracking.addAll(forgetting);
        this.activeTracked.removeAll(forgetting);

        this.verifyCooldown--;

        if((this.currentCount >= REAL_AMOUNT-2 || (this.ticksSinceBigChange > 80 && this.currentCount >= REAL_AMOUNT-6)) && this.verifyCooldown <= 0) {
            this.verifyMode = true;
            this.lookBias.clear();
            this.giveUpOnVerifying++;
            if(!this.verifyMode) this.alreadyVerified.clear();
        } else {
            this.verifyMode = false;
            this.giveUpOnVerifying = 0;
            this.alreadyVerified.clear();
        }

        if(this.verifyMode && (this.alreadyVerified.size() > REAL_AMOUNT-2 || giveUpOnVerifying > 200)) {
            this.verifyMode = false;
            this.verifyCooldown = 30;
            if(this.currentCount > REAL_AMOUNT) this.currentCount--;
            if(this.currentCount < REAL_AMOUNT) this.currentCount++;
        }

        if(old < this.currentCount-5 || old > this.currentCount+5) this.ticksSinceBigChange = 0;
        this.ticksSinceBigChange++;
    }

    private void changeCount(int change) {
        if(!this.verifyMode) this.currentCount += change;
    }

    private void lookAt(Clown clown, float score) {
        if(this.verifyMode && this.alreadyVerified.contains(clown)) return;
        if(this.verifyMode && NumberUtils.getRandomInt(1, 6) != 1) this.alreadyVerified.add(clown);
        lookAt(clown.getPos(), score);
    }

    private void lookAt(Vector2 pos, float score) {
        this.lookBias.putIfAbsent(pos, 0f);
        this.lookBias.put(pos, this.lookBias.get(pos)+score);
    }

    @Override
    protected void addDebugInfo(List<String> info) {
        info.add("Count: "+currentCount);
        info.add("Active: "+activeTracked.size());
        info.add("Dropping: "+droppingTracking.size());
        info.add("Verifying: "+verifyMode);
        info.add("Verified: "+alreadyVerified.size());
        info.add("Real: "+REAL_AMOUNT);
    }

    @Override
    public void render() {
        super.render();
        if(GameData.DEBUGGING && Debuggers.isEnabled("botDebug")) {
            GameData.spriteBatch.end();
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            RenderUtil.enableBlending();
            GameData.shapeRenderer.setColor(Color.YELLOW.cpy().mul(1, 1, 1, 0.7f));
            GameData.shapeRenderer.circle(eyeball.x, eyeball.y, PERIPHERAL_RADIUS, 50);
            GameData.shapeRenderer.setColor((eyeMoveTime == 0 ? Color.GREEN : Color.ORANGE).cpy().mul(1, 1, 1, 0.7f));
            GameData.shapeRenderer.circle(eyeball.x, eyeball.y, VIEW_RADIUS, 50);
            GameData.shapeRenderer.end();
            GameData.spriteBatch.begin();
            GameData.spriteBatch.draw(ImageUtil.get("camera"), eyeball.x-0.5f, eyeball.y-0.5f, 1, 1);
        }
    }
}
