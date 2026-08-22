package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.hugo99j.chaosparty.GameData;

public class InterpolatedValue {
    private float start;
    private float end;
    private final Interpolation interpolation;
    private float startedTime;
    private float endingTime;
    private float defaultTime = 0;

    public InterpolatedValue(Interpolation interpolation, float value, float defaultTime) {
        this.interpolation = interpolation;
        this.defaultTime = defaultTime;
        this.set(value);
    }

    public void goTo(float end) {
        if(end == this.end) return;
        this.start = this.get();
        this.end = end;
        this.startedTime = GameData.time;
        this.endingTime = GameData.time+defaultTime;
    }

    public void goTo(float end, float time) {
        this.defaultTime = time;
        goTo(end);
    }

    public void set(float value) {
        this.start = value;
        this.end = value;
        this.startedTime = GameData.time;
        this.endingTime = GameData.time;
    }

    public float get() {
        if(this.endingTime-this.startedTime <= 0) return end;
        return interpolation.apply(start, end, Math.clamp((GameData.time-this.startedTime)/(this.endingTime-this.startedTime), 0, 1));
    }

    public boolean isDone() {
        return GameData.time>this.endingTime;
    }
}
