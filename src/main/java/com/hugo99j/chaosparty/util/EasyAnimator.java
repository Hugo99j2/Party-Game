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

    public void stop() {
        this.name = null;
        this.length = 0;
        this.startTime = 0;
        if(this.onComplete != null) {
            Runnable onComplete = this.onComplete;
            this.onComplete = null;
            onComplete.run();
        }
    }

    public boolean isAnimating() {
        return name != null && GameData.time-startTime <= length;
    }

    public void tick() {
        if(GameData.time-startTime > length && name != null) {
            stop();
        }
    }

    public TextureAtlas.AtlasRegion getCurrentFrame() {
        if(!isAnimating()) return null;
        return Animator.get(name, null, startTime);
    }
}
