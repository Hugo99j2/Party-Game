package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.hugo99j.chaosparty.GameData;

public class EasyAnimator {
    private String name;
    private float startTime;
    private float length = 0;
    private Runnable onComplete;

    public void start(String name) {
        start(name, null);
    }

    public void start(String name, Runnable onComplete) {
        this.name = name;
        this.startTime = GameData.time;
        this.length = Animator.getInfo(name).maxLength();
        this.onComplete = onComplete;
    }

    public boolean isAnimating() {
        return name != null;
    }

    public void tick() {
        if(!isAnimating() && this.onComplete != null) {
            this.onComplete.run();
            this.onComplete = null;
        }
    }

    public TextureAtlas.AtlasRegion getCurrentFrame() {
        if(!isAnimating()) return null;
        TextureAtlas.AtlasRegion region = Animator.get(name, null, startTime);
        if(GameData.time-startTime > length) {
            this.name = null;
            this.length = 0;
            this.startTime = 0;
        }
        return region;
    }
}
