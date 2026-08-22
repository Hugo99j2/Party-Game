package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.bot.BotController;
import com.hugo99j.chaosparty.bot.FallingFloorBot;
import com.hugo99j.chaosparty.entity.FallingFloorObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.ScreenCenterer;
import com.hugo99j.chaosparty.ui.element.ProgressBar;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;

import java.util.ArrayList;
import java.util.List;

public class FallingFloorMinigame extends AbstractMinigame {
    private int time = 0;
    private int maxTime = 5*GameData.TICKS_PER_SECOND;
    private Color safeColour = Color.GREEN;
    private int ticksUntilCanKill = 20;
    private ProgressBar progressBar;
    private SoundInstance music;

    public FallingFloorMinigame() {
        super("falling_floor");
    }

    @Override
    public boolean shouldAutoCenterCameras() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        progressBar = new ProgressBar("colourbar", 45) {
            @Override
            public float getX() {
                return 0;
            }

            @Override
            public float getY() {
                return 0;
            }

            @Override
            public float getWidth() {
                return 0;
            }

            @Override
            public float getHeight() {
                return 0;
            }
        };
        music = SoundManager.getSound("falling_floor_music").playSingle(1);
    }

    @Override
    public void tick() {
        this.defaultPlayerMovements();
        time--;
        ticksUntilCanKill--;
        if(time <= 0) {
            maxTime-=(int) Math.min(20, maxTime/2.0f);
            maxTime = (int) Math.max(maxTime, GameData.TICKS_PER_SECOND*0.05f);
            time = maxTime;
            if(ticksUntilCanKill <= 0) {
                List<Player> toKill = new ArrayList<>();
                List<Player> toNotKill = new ArrayList<>();
                for (AbstractObject object : GameData.getLevelOrThrow().getAllObjects()) {
                    if (object instanceof FallingFloorObject fallingFloorObject) {
                        if(fallingFloorObject.getColour().equals(this.safeColour)) {
                            toNotKill.addAll(GameData.getLevelOrThrow().getObjectsBetweenClass(object.getPos(), object.getPos().add(3, 3), Player.class, true));
                        }
                    } else if(object instanceof Player p) toKill.add(p);
                }
                toKill.removeAll(toNotKill);
                for (Player player : toKill) {
                    player.setPos(new Vector2(-10, -10));
                }
                if(toNotKill.isEmpty()) {
                    ToRun.run(() -> GameData.getCurrentMatch().finishCurrentMinigame());
                }
                for (Player player : toNotKill) {
                    GameData.getCurrentMatch().getCurrentMinigame().addScore(player.getMatchPlayer(), 1);
                }
            }
            List<Color> colors = List.of(Color.RED, Color.OLIVE, Color.YELLOW, Color.BLUE, Color.ORANGE, Color.PINK, Color.CYAN, Color.PURPLE, Color.LIME);
            this.safeColour = colors.get(NumberUtils.getRandomInt(0, colors.size()-1));
            Logger.info(RenderUtil.toString(this.safeColour));
            this.randomiseColours();
        }
        this.progressBar.setMax(maxTime);
        this.progressBar.setValue(time);
        this.progressBar.setColor(this.safeColour);
    }

    private void randomiseColours() {
        boolean safeShowedUp = false;
        for (AbstractObject object : GameData.getLevelOrThrow().getAllObjects()) {
            if (object instanceof FallingFloorObject fallingFloorObject) {
                fallingFloorObject.makeRandomlyColoured();
                if(fallingFloorObject.getColour().equals(this.safeColour)) safeShowedUp = true;
            }
        }
        if(!safeShowedUp) randomiseColours();
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        progressBar.render(new RenderState(false, false, false, false, false, false, 0, 0, delta));
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
        matchViews.add(new MatchView(33, 18));
        ((ScreenCenterer) matchViews.getFirst().gameViewport).party_Game$setCenter(true);
    }

    @Override
    public void setPaused(boolean paused) {
        if(paused) music.pause();
        else music.play();
    }

    public Color getSafeColour() {
        return safeColour;
    }

    @Override
    public BotController createBotController(Player player) {
        return new FallingFloorBot(player);
    }
}
