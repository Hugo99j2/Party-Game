package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.Main;
import com.hugo99j.chaosparty.match.Match;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.Debuggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMinigame implements Disposable {
    private final String mapName;
    private Map<MatchPlayer, Integer> scores = new HashMap<>();

    protected AbstractMinigame(String mapName) {
        this.mapName = mapName;
        GameData.level = LevelLoader.loadFromData(mapName);
    }

    public abstract void tick();

    protected void defaultPlayerMovements() {
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if(player.controller == null || player.getPlayer() == null) continue;
            float speed = 300*(1+player.controller.getAxis(5));
            float move = Math.max(speed-player.getPlayer().getVelocity().len(), 0);

            Vector2 movement = new Vector2(0, 0);

//            if(Gdx.input.isKeyPressed(Input.Keys.W)) {
//                movement.add(0, 1);
//            };
//            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
//                movement.add(-1, 0);
//            };
//            if(Gdx.input.isKeyPressed(Input.Keys.S)) {
//                movement.add(0, -1);
//            };
//            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
//                movement.add(1, 0);
//            };

            Vector2 controller = new Vector2(player.controller.getAxis(player.controller.getMapping().axisLeftX), -player.controller.getAxis(player.controller.getMapping().axisLeftY));
            if (controller.len() > 0.2f) movement = controller;

            //diagonal isnt faster
            if(movement.len() > 1) movement.nor();

            if(GameData.DEBUGGING && Debuggers.isEnabled("freecam")) {
                float mul = 0.25f;
                Debuggers.freecam.add(new Vector2(movement.x*mul, movement.y*mul));
            }
            else if(movement.len() > 0) player.getPlayer().getPhysics().applyForceToCenter(new Vector2(movement.x*move, movement.y*move), true);

        }
    }

    public void renderSegment(float delta, MatchView view) {
        renderWorld(view);
    }

    protected void renderWorld(MatchView view) {
        GameData.spriteBatch.begin();

        GameData.spriteBatch.enableBlending();
        GameData.getLevelOrThrow().render();

        GameData.spriteBatch.end();

        if(!GameData.DEBUGGING || Debuggers.isEnabled("lights")) {
//                GameConstants.gameCamera.update();
//                GameConstants.gameViewport.apply();
            GameData.level.rayHandler.useCustomViewport(view.gameViewport.getScreenX(), view.gameViewport.getScreenY(), view.gameViewport.getScreenWidth(), view.gameViewport.getScreenHeight());
            GameData.level.rayHandler.setCombinedMatrix(view.gameCamera);
            GameData.level.rayHandler.updateAndRender();
        }
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
}
