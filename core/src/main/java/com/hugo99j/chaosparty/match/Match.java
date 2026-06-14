package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.minigame.AbstractMinigame;
import com.hugo99j.chaosparty.minigame.MinigameScreenLayout;
import com.hugo99j.chaosparty.ui.PlayScreen;
import com.hugo99j.chaosparty.ui.WinScreen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Match {
    private AbstractMinigame currentMinigame = null;
    private final List<MatchView> matchViews = new ArrayList<>();
    private final List<MatchPlayer> players;

    public Match(List<MatchPlayer> players) {
        this.players = new ArrayList<>(players);
    }

    public AbstractMinigame getCurrentMinigame() {
        return currentMinigame;
    }

    public void setCurrentMinigame(@Nullable AbstractMinigame minigame) {
        this.currentMinigame = minigame;
        matchViews.forEach(MatchView::dispose);
        matchViews.clear();
        if(minigame != null) {
            GameData.MAIN_INSTANCE.setScreen(new PlayScreen());
            minigame.setupViews(matchViews);
            for (MatchPlayer player : players) {
                GameData.level.addObject(new Player(player));
            }
        }
        else {
            GameData.level.dispose();
        };
    }

    public void tick() {
        this.players.forEach(MatchPlayer::tick);
        if(this.getCurrentMinigame() != null) this.getCurrentMinigame().tick();
    }

    public void render(float delta) {
        if(this.getCurrentMinigame() != null) {
            if(this.getMatchViews().size() == 1) {
                renderView(this.getMatchViews().getFirst(), 0, 0, 1, 1);
            } else if(this.getMatchViews().size() == 2) {
                renderView(this.getMatchViews().getFirst(), 0, 0, 0.5f, 1);
                renderView(this.getMatchViews().get(1), 0.5f, 0, 0.5f, 1);
            } else {
                renderView(this.getMatchViews().getFirst(), 0, 0, 0.5f, 0.5f);
                renderView(this.getMatchViews().get(1), 0.5f, 0, 0.5f, 0.5f);
                renderView(this.getMatchViews().get(2), 0, 0.5f, 0.5f, 0.5f);
                if(this.getMatchViews().size() >= 4) renderView(this.getMatchViews().get(3), 0.5f, 0.5f, 0.5f, 0.5f);
            }
            this.getCurrentMinigame().render(delta);
        }
    }

    private void renderView(MatchView v, float x, float y, float sizeX, float sizeY) {
        y *= GameData.height;
        x *= GameData.width;
        TextureRegion r = v.render();
        GameData.uiViewport.apply();
        GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
        GameData.spriteBatch.begin();
        GameData.spriteBatch.draw(r, x, GameData.height-y, 0, 0, GameData.width, GameData.height, sizeX, -sizeY, 0);
        GameData.spriteBatch.end();
    }

    public void updateViews() {
        matchViews.forEach(MatchView::update);
    }

    public void finishCurrentMinigame() {
        this.setCurrentMinigame(null);
        GameData.MAIN_INSTANCE.setScreen(new WinScreen());
    }

    public List<MatchView> getMatchViews() {
        return matchViews;
    }

    public List<MatchPlayer> getPlayers() {
        return players;
    }
}
