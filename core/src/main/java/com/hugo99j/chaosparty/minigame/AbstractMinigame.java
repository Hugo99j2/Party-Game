package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.utils.Disposable;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.hugo99j.chaosparty.GameData;

public abstract class AbstractMinigame implements Disposable {
    private final String mapName;
    private int score;

    protected AbstractMinigame(String mapName) {
        this.mapName = mapName;
        GameData.level = LevelLoader.loadFromData(mapName);
    }

    public abstract void tick();

    public abstract void render(float delta);

    public String getMapName() {
        return mapName;
    }

    public void addScore(int i, int i1) {
        score += i1;
    }

    public int getScore(int i) {
        return score;
    }
}
