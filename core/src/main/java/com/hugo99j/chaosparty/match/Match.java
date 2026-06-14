package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.minigame.AbstractMinigame;
import com.hugo99j.chaosparty.ui.PlayScreen;
import com.hugo99j.chaosparty.ui.WinScreen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Match {
    private AbstractMinigame currentMinigame = null;
    private final List<MatchView> matchViews = new ArrayList<>();

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
        }
        else {
            GameData.level.dispose();
        };
    }

    public void tick() {
        if(this.getCurrentMinigame() != null) this.getCurrentMinigame().tick();
    }

    public void render(float delta) {
        if(this.getCurrentMinigame() != null) {
            this.getCurrentMinigame().render(delta);
            TextureRegion r = matchViews.getFirst().render();
            GameData.uiViewport.apply();
            GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
            GameData.spriteBatch.begin();
            GameData.spriteBatch.draw(r, 0, GameData.height, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
            GameData.spriteBatch.end();
        }
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
}
