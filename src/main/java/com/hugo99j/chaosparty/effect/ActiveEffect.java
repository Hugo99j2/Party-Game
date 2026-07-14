package com.hugo99j.chaosparty.effect;

public class ActiveEffect {
    private final EffectType effect;
    private float remaining;

    public ActiveEffect(EffectType effect, float time) {
        this.effect = effect;
        this.remaining = time;
    }

    public EffectType getEffect() {
        return effect;
    }

    public float getRemaining() {
        return remaining;
    }

    public void setRemaining(float v) {
        remaining = v;
    }
}
