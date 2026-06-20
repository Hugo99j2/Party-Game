package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapeRenderer.class)
public interface ShapeRendererLineWidth {
    @Accessor
    void setDefaultRectLineWidth(float w);

    @Accessor
    float getDefaultRectLineWidth();
}
