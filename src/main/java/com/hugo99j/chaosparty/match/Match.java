package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.AbstractObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.effect.EffectShaderManager;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.PlayerSpawnPoint;
import com.hugo99j.chaosparty.minigame.AbstractMinigame;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.ui.screen.PlayScreen;
import com.hugo99j.chaosparty.ui.screen.WinScreen;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    private AbstractMinigame currentMinigame = null;
    private final List<MatchView> matchViews = new ArrayList<>();
    private final List<MatchPlayer> players;
    private Map<String, Map<MatchPlayer, Integer>> scores = new HashMap<>();

    public Match(List<MatchPlayer> players) {
        this.players = new ArrayList<>(players);
    }

    public AbstractMinigame getCurrentMinigame() {
        return currentMinigame;
    }

    public void setCurrentMinigame(@Nullable AbstractMinigame minigame) {
        AbstractMinigame oldMinigame = currentMinigame;
        this.currentMinigame = minigame;
        matchViews.forEach(MatchView::dispose);
        matchViews.clear();
        if(minigame != null) {
            GameData.MAIN_INSTANCE.setScreen(new PlayScreen());
            minigame.setupViews(matchViews);
            for (MatchView matchView : matchViews) {
                matchView.setCenter(minigame.shouldAutoCenterCameras());
            }
            minigame.start();

            Map<Integer, Vector2> spawns = new HashMap<>();
            for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                if(allObject instanceof PlayerSpawnPoint spawn) {
                    spawns.put(spawn.id, spawn.getPos().cpy());
                    if (!(GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor)) {
                        spawn.dispose();
                    }
                }
            }

            int i = 1;
            for (MatchPlayer player : players) {
                Player p = new Player(player);
                if(spawns.containsKey(i)) {
                    p.setPos(spawns.get(i));
                } else Logger.error("Map "+this.getCurrentMinigame().getMapName()+" does not contain spawn point #"+i);
                GameData.level.addObject(p);
                i++;
            }
        }
        else {
            if(oldMinigame != null) oldMinigame.dispose();
            if(GameData.level != null) GameData.level.dispose();
        };
    }

    public void tick() {
        this.players.forEach(MatchPlayer::tick);
        if(this.getCurrentMinigame() != null) this.getCurrentMinigame().tick();
    }

    public void render(float delta) {
        if(this.getCurrentMinigame() != null) {
            if(this.getMatchViews().size() == 1 || (GameData.DEBUGGING && Debuggers.isEnabled("forceSingleView"))) {
                renderView(this.getMatchViews().getFirst(), 0, 0, 1, 1);
            } else if(this.getMatchViews().size() == 2 && this.getCurrentMinigame().splitHorizontal2Views()) {
                renderView(this.getMatchViews().getFirst(), 0, 0, 1, 0.5f);
                renderView(this.getMatchViews().get(1), 0, 0.5f, 1, 0.5f);
                GameData.spriteBatch.begin();
                GameData.spriteBatch.draw(ImageUtil.get("ui/border"), 0, GameData.height/2.0f, GameData.width, 6);
                GameData.spriteBatch.end();
            } else if(this.getMatchViews().size() == 2 && !this.getCurrentMinigame().splitHorizontal2Views()) {
                renderView(this.getMatchViews().getFirst(), 0, 0, 0.5f, 1);
                renderView(this.getMatchViews().get(1), 0.5f, 0, 0.5f, 1);
                GameData.spriteBatch.begin();
                GameData.spriteBatch.draw(ImageUtil.get("ui/border"), GameData.width/2.0f, 0, 6, GameData.height);
                GameData.spriteBatch.end();
            } else {
                renderView(this.getMatchViews().getFirst(), 0, 0, 0.5f, 0.5f);
                renderView(this.getMatchViews().get(1), 0.5f, 0, 0.5f, 0.5f);
                renderView(this.getMatchViews().get(2), 0, 0.5f, 0.5f, 0.5f);
                if(this.getMatchViews().size() >= 4) renderView(this.getMatchViews().get(3), 0.5f, 0.5f, 0.5f, 0.5f);
                GameData.spriteBatch.begin();
                GameData.spriteBatch.draw(ImageUtil.get("ui/border"), 0, GameData.height/2.0f, GameData.width, 6);
                GameData.spriteBatch.draw(ImageUtil.get("ui/border"), GameData.width/2.0f, 0, 6, GameData.height);
                GameData.spriteBatch.end();
            }
            GameData.uiViewport.apply();
            GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
            this.getCurrentMinigame().render(delta);
        }
    }

    private void renderView(MatchView v, float x, float y, float sizeX, float sizeY) {
        y *= GameData.height;
        x *= GameData.width;
        TextureRegion r = v.render();
        GameData.uiViewport.apply();
        EffectShaderManager.apply(v.getActiveEffects());
        GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
        GameData.spriteBatch.begin();
        GameData.spriteBatch.draw(r, x, GameData.height-y, 0, 0, GameData.width, GameData.height, sizeX, -sizeY, 0);
        GameData.spriteBatch.setShader(null);
        GameData.spriteBatch.end();
    }

    public void updateViews() {
        matchViews.forEach(MatchView::update);
    }

    public void finishCurrentMinigame() {
        this.scores.put("test", new HashMap<>());
        this.scores.get("test").putAll(this.getCurrentMinigame().getScores());
        GameData.MAIN_INSTANCE.setScreen(new WinScreen(this.getScores()));
        this.setCurrentMinigame(null);
    }

    public List<MatchView> getMatchViews() {
        return matchViews;
    }

    public List<MatchPlayer> getPlayers() {
        return players;
    }

    public Map<String, Map<MatchPlayer, Integer>> getScores() {
        return scores;
    }
}
