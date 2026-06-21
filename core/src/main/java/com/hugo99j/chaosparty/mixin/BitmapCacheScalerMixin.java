package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.hugo99j.chaosparty.ui.BitmapCacheScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(BitmapFontCache.class)
public class BitmapCacheScalerMixin implements BitmapCacheScaler {
    @Shadow
    private boolean integer;

    @Shadow
    private float x;

    @Shadow
    private float y;

    @Shadow
    private float[][] pageVertices;

    @Shadow
    private int[] idx;

    @Override
    public void scale(float scale) {
        if (scale == 1) return;
        if (integer) {
            scale = Math.round(scale);
        }
        x *= scale;
        y *= scale;

        float[][] pageVertices = this.pageVertices;
        for (int i = 0, n = pageVertices.length; i < n; i++) {
            float[] vertices = pageVertices[i];

            for (int ii = 0, nn = idx[i]; ii < nn; ii += 5) {
                vertices[ii] *= scale;
                vertices[ii + 1] *= scale;
            }
        }
    }
}
