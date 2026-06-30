package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.ControllerInput;
import com.hugo99j.chaosparty.ui.ControllerUtil;
import com.hugo99j.chaosparty.ui.Timer;
import com.hugo99j.chaosparty.util.PathUtil;

import java.util.List;

public class HotPotatoMinigame extends AbstractMinigame {
    private Timer timer;
    private final CombinedScreenSS ss = ScreenSSBuilder.create()
        .set("xSize", "1vw")
        .set("ySize", "1vh")
        .set("x", 0)
        .set("y", 0)
        .newChild("rightcorner")
        .set("x", "20")
        .set("y", "0.1vh")
        .set("xSize", "0.02vw")
        .set("ySize", "0.02vh")
        .newChild("timer")
        .set("x", "5%")
        .set("y", "0")
        .set("xSize", "95%")
        .set("ySize", "40%")
        .finishChild()
        .newChild("score")
        .set("x", "5%")
        .set("y", "40%+20%")
        .set("xSize", "95%")
        .set("ySize", "40%")
        .finishChild()
        .finishChild()
        .build();
    private SoundInstance music;
    private ParticleEffect hotEffect;
    private MatchPlayer hotPlayer;
    private boolean hotCollisionCooldown = false;

    public HotPotatoMinigame() {
        super("hot_potato");
    }

    @Override
    public void start() {
        super.start();
        timer = new Timer("timer", 60, 2, false);
        timer.setStyle(ss.get("timer"));
        music = SoundManager.getSound("potato_music").playSingle(1);
        hotEffect = new ParticleEffect();
        hotEffect.load(Gdx.files.internal(PathUtil.asset("particles/flame.p")), GameData.atlas);
        hotEffect.setEmittersCleanUpBlendFunction(false);
        hotEffect.scaleEffect(0.01f);
        hotEffect.setDuration(1000000);
        hotEffect.start();
        GameData.getLevelOrThrow().particles.add(hotEffect);

        hotPlayer = GameData.getCurrentMatch().getPlayers().getFirst();
    }

    @Override
    public void tick() {
        hotCollisionCooldown = false;
        this.defaultPlayerMovements();
        hotEffect.setPosition(hotPlayer.getPlayerObject().getPos().x+0.3f, hotPlayer.getPlayerObject().getPos().y+1);

        if(((ControllerUtil) hotPlayer.controller).wasJustPressed(ControllerInput.RIGHT_BUMPER)) {
            Potato potato = new Potato();
            potato.setX(hotPlayer.getPlayerObject().getPos().x);
            potato.setY(hotPlayer.getPlayerObject().getPos().y);
            GameData.getLevelOrThrow().addObject(potato);
            potato.getPhysics().applyForceToCenter(((ControllerUtil) hotPlayer.controller).getValue(ControllerInput.RIGHT_STICK_RIGHT)*10000, ((ControllerUtil) hotPlayer.controller).getValue(ControllerInput.RIGHT_STICK_UP)*10000, true);
        }

        if(timer.getSeconds() <= 0) {
            getHotPlayer().getPlayerObject().setNoClip(true);
            SoundManager.getSound("flame_erupt").playSingle(1);
            var boom = new ParticleEffect();
            boom.load(Gdx.files.internal(PathUtil.asset("particles/boom.p")), GameData.atlas);
            boom.setEmittersCleanUpBlendFunction(false);
            boom.scaleEffect(0.01f);
            boom.start();
            boom.setPosition(getHotPlayer().getPlayerObject().getPos().x+0.5f, getHotPlayer().getPlayerObject().getPos().y+0.5f);
            GameData.getLevelOrThrow().particles.add(boom);
            for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
                if(!player.getPlayerObject().isNoClip()) {
                    setHotPlayer(player);
                    break;
                }
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
    public void dispose() {
        music.fade(1, 0);
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

    public void setHotPlayer(MatchPlayer hotPlayer) {
        if(this.hotPlayer == hotPlayer) return;
        this.hotPlayer = hotPlayer;
        this.timer.setTime(10, false);
    }

    public void setHotPlayerAndCooldown(MatchPlayer matchPlayer) {
        if(this.hotPlayer == matchPlayer) return;
        if(!hotCollisionCooldown) setHotPlayer(matchPlayer);
        hotCollisionCooldown = true;
    }

    public MatchPlayer getHotPlayer() {
        return hotPlayer;
    }
}
