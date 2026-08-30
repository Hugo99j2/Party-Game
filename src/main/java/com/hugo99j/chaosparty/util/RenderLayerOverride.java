package com.hugo99j.chaosparty.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class RenderLayerOverride implements RenderLayerOrOverride {
    private final RenderLayer source;
    private final float offset;

    public RenderLayerOverride(RenderLayer source, float offset) {
        this.source = source;
        this.offset = offset;
    }

    @Override
    public float getLayer() {
        return source.getLayer()+offset;
    }

    public JsonObject write() {
        JsonObject out = new JsonObject();
        out.addProperty("source", source.getName());
        out.addProperty("offset", offset);
        return out;
    }

    public static RenderLayerOverride read(JsonElement element) {
        return new RenderLayerOverride(RenderLayer.get(element.getAsJsonObject().get("source").getAsString()), element.getAsJsonObject().get("offset").getAsFloat());
    }

    public static RenderLayerOverride createDefault() {
        return new RenderLayerOverride(RenderLayer.BACKGROUND, 0);
    }

    public RenderLayer getSource() {
        return source;
    }

    public float getOffset() {
        return offset;
    }
}
