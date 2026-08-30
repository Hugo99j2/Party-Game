package com.hugo99j.chaosparty.util;

public sealed interface RenderLayerOrOverride permits RenderLayerOverride, RenderLayer {
    float getLayer();
}
