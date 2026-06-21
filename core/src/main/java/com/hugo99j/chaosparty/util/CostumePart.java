package com.hugo99j.chaosparty.util;

public enum CostumePart {
    HAT(true),
    FACE(true),
    SHIRT(true),
    PANTS(true),
    COLOUR(false);

    private final boolean shouldRender;

    CostumePart(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public boolean shouldRender() {
        return shouldRender;
    }
}
