package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.hugo99j.chaosparty.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.bot.BotController;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.match.Match;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMinigame implements Disposable {
    private final String mapName;
    private Map<MatchPlayer, Integer> scores = new HashMap<>();

    protected AbstractMinigame(String mapName) {
        this.mapName = mapName;
    }

    public boolean shouldAutoCenterCameras() {
        return false;
    }

    public void start() {
        GameData.level = LevelLoader.loadFromDataOrDisk(mapName);
    }

    public abstract void tick();

    protected void defaultPlayerMovements() {
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if(player.controller == null || player.getPlayerObject() == null) continue;
            float speed = getPlayerSpeed(player);
            float move = Math.max(speed-player.getPlayerObject().getVelocity().len(), 0);

            Vector2 movement = new Vector2(0, 0);
            Vector2 controller = new Vector2(player.controller.getAxis(player.controller.getMapping().axisLeftX), -player.controller.getAxis(player.controller.getMapping().axisLeftY));
            if (controller.len() > 0.2f) movement = controller;

            //diagonal isnt faster
            if(movement.len() > 1) movement.nor();

            if(GameData.DEBUGGING && Debuggers.isEnabled("freecam")) {
                float mul = 0.25f;
                Debuggers.freecam.add(new Vector2(movement.x*mul, movement.y*mul));
            }
            else if(movement.len() > 0) player.getPlayerObject().getPhysics().applyForceToCenter(new Vector2(movement.x*move, movement.y*move), true);

        }
    }

    public float getPlayerSpeed(MatchPlayer player) {
        return 400;
    }

    public void renderSegment(float delta, MatchView view) {
        renderWorld(view);
    }

    protected void renderWorld(MatchView view) {
        GameData.spriteBatch.begin();

        GameData.spriteBatch.enableBlending();
        GameData.getLevelOrThrow().render(view, false);

        GameData.spriteBatch.end();
    }

    public String getMapName() {
        return mapName;
    }

    public void addScore(MatchPlayer player, int score) {
        setScore(player, getScore(player)+score);
    }

    public void setScore(MatchPlayer player, int score) {
        scores.put(player, score);
    }

    public int getScore(MatchPlayer player) {
        return scores.getOrDefault(player, 0);
    }

    public abstract MinigameScreenLayout getLayout();

    public void render(float delta) {

    }

    public void setupViews(List<MatchView> matchViews) {

    }

    public void setPaused(boolean paused) {

    }

    protected Match getMatch() {
        return GameData.getCurrentMatch();
    }

    public boolean splitHorizontal2Views() {
        return true;
    }

    public BotController createBotController(Player player) {
        return null;
    }

    public Map<MatchPlayer, Integer> getScores() {
        return scores;
    }
}
