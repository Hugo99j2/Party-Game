package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.bot.BotController;
import com.hugo99j.chaosparty.bot.HotPotatoBot;
import com.hugo99j.chaosparty.effect.EffectType;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.*;
import com.hugo99j.chaosparty.ui.element.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HotPotatoMinigame extends AbstractMinigame {
    private static final int TIME = 20;

    private Timer timer;
    private SoundInstance music;
    private ParticleEffect hotEffect;
    private ParticleEffect hotScreenEffect;
    private MatchPlayer hotPlayer;
    private boolean hotCollisionCooldown = false;
    private int potatoPassThreshold = 0;
    private float largestTime = TIME;

    public HotPotatoMinigame() {
        super("hot_potato");
    }

    @Override
    public void start() {
        super.start();
        timer = new Timer("timer", TIME, 2, false) {
            @Override
            public float getX() {
                return 40;
            }

            @Override
            public float getY() {
                return 20;
            }

            @Override
            public float getWidth() {
                return 120;
            }

            @Override
            public float getHeight() {
                return 120;
            }
        };
        music = SoundManager.getSound("potato_music").playSingle(1);
        hotEffect = new ParticleEffect();
        hotEffect.load(Gdx.files.internal(PathUtil.asset("particles/flame.p")), GameData.atlas);
        hotEffect.setEmittersCleanUpBlendFunction(false);
        hotEffect.scaleEffect(0.01f);
        hotEffect.setDuration(1000000);
        hotEffect.start();
        hotScreenEffect = new ParticleEffect();
        hotScreenEffect.load(Gdx.files.internal(PathUtil.asset("particles/flame.p")), GameData.atlas);
        hotScreenEffect.setEmittersCleanUpBlendFunction(false);
        hotScreenEffect.scaleEffect(1);
        hotScreenEffect.setDuration(1000000);
        hotScreenEffect.getEmitters().get(0).getSpawnWidth().setHigh(GameData.width);
        hotScreenEffect.start();
        GameData.getLevelOrThrow().particles.add(hotEffect);

        hotPlayer = GameData.getCurrentMatch().getPlayers().getFirst();
    }

    @Override
    public void tick() {
        potatoPassThreshold--;
        hotCollisionCooldown = false;
        this.defaultPlayerMovements();
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if(player.getPlayerObject().isNoClip()) {
                int change = 0;
                if(((ControllerUtil) player.controller).wasJustPressedThisTick(ControllerInput.RIGHT_BUMPER)) change += 1;
                if(((ControllerUtil) player.controller).wasJustPressedThisTick(ControllerInput.LEFT_BUMPER)) change -= 1;
                if(change != 0) {
                    List<MatchPlayer> notDead = new ArrayList<>();
                    MatchPlayer current = null;
                    float closest = 100000;
                    for (MatchPlayer matchPlayer : GameData.getCurrentMatch().getPlayers()) {
                        if(matchPlayer.getPlayerObject().isNoClip()) continue;
                        notDead.add(matchPlayer);
                        if(matchPlayer.getPlayerObject().getPos().dst(player.getPlayerObject().getPos()) < closest) {
                            closest = matchPlayer.getPlayerObject().getPos().dst(player.getPlayerObject().getPos());
                            current = matchPlayer;
                        }
                    }
                    player.getPlayerObject().setPos((change == 1 ? Looper.nextValue(notDead, current) : Looper.previousValue(notDead, current)).getPlayerObject().getPos());
                }
            }
        }
        hotEffect.setPosition(hotPlayer.getPlayerObject().getPos().x+0.3f, hotPlayer.getPlayerObject().getPos().y+1);

        if(((ControllerUtil) hotPlayer.controller).wasJustPressedThisTick(ControllerInput.RIGHT_BUMPER)) {
            Potato potato = new Potato();
            potato.setX(hotPlayer.getPlayerObject().getPos().x);
            potato.setY(hotPlayer.getPlayerObject().getPos().y);
            GameData.getLevelOrThrow().addObject(potato);
            potato.getPhysics().applyForceToCenter(((ControllerUtil) hotPlayer.controller).getValue(ControllerInput.RIGHT_STICK_RIGHT)*7000, ((ControllerUtil) hotPlayer.controller).getValue(ControllerInput.RIGHT_STICK_UP)*7000, true);
        }

        if(timer.getSeconds() <= 0 || potatoPassThreshold > 300) {
            potatoPassThreshold = 0;
            getHotPlayer().getPlayerObject().setNoClip(true);
            for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
                if(!player.getPlayerObject().isNoClip()) GameData.getCurrentMatch().getCurrentMinigame().addScore(player, 1);
            }
            SoundManager.getSound("flame_explode").playSingle(1);
            ((ControllerUtil) getHotPlayer().controller).vibrate(VibrationAmount.of(new float[]{0.0848817f,0.2079646f,0.46620733f,0.048672568f,0.9848102f,0.0f}, new float[]{0.0f,0.013274336f,0.19673721f,0.22123894f,0.35943615f,0.0f}));
            var boom = new ParticleEffect();
            boom.load(Gdx.files.internal(PathUtil.asset("particles/boom.p")), GameData.atlas);
            boom.setEmittersCleanUpBlendFunction(false);
            boom.scaleEffect(0.01f);
            boom.start();
            boom.setPosition(getHotPlayer().getPlayerObject().getPos().x+0.5f, getHotPlayer().getPlayerObject().getPos().y+0.5f);
            GameData.getLevelOrThrow().particles.add(boom);
            int matches = 0;
            ArrayList<MatchPlayer> players = new ArrayList<>(GameData.getCurrentMatch().getPlayers());
            Collections.shuffle(players);
            for (MatchPlayer player : players) {
                if(!player.getPlayerObject().isNoClip()) {
                    if(matches == 0) setHotPlayer(player);
                    matches++;
                }
            }
            this.timer.setTime(TIME, false);
            largestTime = TIME;
            if(matches <= 1) {
                ToRun.run(() -> GameData.getCurrentMatch().finishCurrentMinigame());
            }
        }
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        timer.render(new RenderState(false, false, false, false, false, false, 0, 0, delta));
        GameData.spriteBatch.end();
    }

    @Override
    public void renderSegment(float delta, MatchView view) {
        super.renderSegment(delta, view);
        if(view.getPlayer() == hotPlayer) {
            GameData.uiViewport.apply();
            GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
            GameData.spriteBatch.begin();
            hotScreenEffect.draw(GameData.spriteBatch, Gdx.graphics.getDeltaTime());
            GameData.spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            GameData.spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        music.fade(1, 0);
        hotScreenEffect.dispose();
    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.FOUR_CORNERS;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            matchViews.add(new MatchView(16, 9, player));
        }
    }

    @Override
    public void setPaused(boolean paused) {
        if(paused) music.pause();
        else music.play();
    }

    public boolean setHotPlayer(MatchPlayer hotPlayer) {
        if(this.hotPlayer == hotPlayer) return false;
        if(hotPlayer.getPlayerObject().isNoClip()) {
            Logger.error("Shouldnt make dead player hot");
        }
        potatoPassThreshold += 50;
        for (MatchView matchView : GameData.getCurrentMatch().getMatchViews()) {
            matchView.getActiveEffects().clear();
        }
        this.hotPlayer = hotPlayer;
        this.timer.setTime((int) Math.min(TIME-((TIME-this.timer.getSeconds())/1.5f), largestTime), false);
        largestTime = Math.min(largestTime, timer.getSeconds());
        for (MatchView matchView : GameData.getCurrentMatch().getMatchViews()) {
            if(matchView.getPlayer() == hotPlayer) {
                matchView.addEffect(EffectType.LIQUID, 1000);
            }
        }
        SoundManager.getSound("flame").playSingle(1);
        ((ControllerUtil) hotPlayer.controller).vibrate(VibrationAmount.of(new float[]{0.11030341f,1.0f,0.16114683f,0.06637168f,0.3035084f,0.057522126f,0.35943615f,0.0f}, new float[]{0.12555644f,0.32743362f,0.24758063f,0.0f}));
        return true;
    }

    public boolean setHotPlayerAndCooldown(MatchPlayer matchPlayer) {
        if(this.hotPlayer == matchPlayer) return false;
        if(hotCollisionCooldown) return false;
        hotCollisionCooldown = true;
        return setHotPlayer(matchPlayer);
    }

    public MatchPlayer getHotPlayer() {
        return hotPlayer;
    }

    @Override
    public BotController createBotController(Player player) {
        return new HotPotatoBot(player);
    }
}
