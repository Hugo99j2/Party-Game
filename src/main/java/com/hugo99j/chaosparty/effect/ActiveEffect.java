package com.hugo99j.chaosparty.effect;

public class ActiveEffect {
    private final String effect;
    private float remaining;

    public ActiveEffect(String effect) {
        this.effect = effect;
    }

    public String getEffect() {
        return effect;
    }
}
