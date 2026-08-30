package com.hugo99j.chaosparty.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RenderLayer implements RenderLayerOrOverride {
    private static int current = 0;
    private static final Map<String, RenderLayer> layers = new LinkedHashMap<>();
    public static final RenderLayer BOTTOM = create("bottom");
    public static final RenderLayer BACKGROUND = create("background");
    public static final RenderLayer TILESETS = create("tileset");
    public static final RenderLayer TILESET_OVERLAYS = create("tileset_overlay");
    public static final RenderLayer COLLECTABLES = create("collectable");
    public static final RenderLayer DECORATIONS = create("deco");
    public static final RenderLayer NPC = create("npc");
    public static final RenderLayer PLAYER = create("player");
    public static final RenderLayer WALLS = create("walls");
    public static final RenderLayer TOP = create("top");

    private static RenderLayer create(String name) {
        RenderLayer layer = new RenderLayer(current++, name);
        layers.put(name, layer);
        return layer;
    }

    public static RenderLayer get(String name) {
        return layers.get(name);
    }

    private final float level;
    private final String name;

    private RenderLayer(float level, String name) {
        this.level = level;
        this.name = name;
    }

    public static RenderLayer[] getAll() {
        return layers.values().toArray(new RenderLayer[0]);
    }

    @Override
    public float getLayer() {
        return level;
    }

    public String getName() {
        return name;
    }
}
