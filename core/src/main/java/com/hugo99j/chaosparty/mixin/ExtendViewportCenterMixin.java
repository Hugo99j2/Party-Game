package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hugo99j.chaosparty.ui.BitmapCacheScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendViewport.class)
public abstract class ExtendViewportCenterMixin extends Viewport {
    @Shadow
    private float minWorldWidth;

    @Shadow
    private float minWorldHeight;

    @Shadow
    private float maxWorldWidth;

    @Shadow
    private float maxWorldHeight;

    @Shadow
    private Scaling scaling;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(int screenWidth, int screenHeight, boolean centerCamera, CallbackInfo ci) {
        centerCamera = true;
        ci.cancel();
        // Fit min size to the screen.
        float worldWidth = minWorldWidth;
        float worldHeight = minWorldHeight;
        Vector2 scaled = scaling.apply(worldWidth, worldHeight, screenWidth, screenHeight);


        // Extend, possibly in both directions depending on the scaling.
        int viewportWidth = Math.round(scaled.x);
        int viewportHeight = Math.round(scaled.y);

        Vector2 test = new Vector2((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2);
        int extraX = 0, extraY = 0;

        if (viewportWidth < screenWidth) {
            float toViewportSpace = viewportHeight / worldHeight;
            float toWorldSpace = worldHeight / viewportHeight;
            float lengthen = (screenWidth - viewportWidth) * toWorldSpace;
            if (maxWorldWidth > 0) lengthen = Math.min(lengthen, maxWorldWidth - minWorldWidth);
            worldWidth += lengthen;
            extraX = (int) Math.floor(toViewportSpace*lengthen);
            viewportWidth += Math.round(lengthen * toViewportSpace);
        }
        if (viewportHeight < screenHeight) {
            float toViewportSpace = viewportWidth / worldWidth;
            float toWorldSpace = worldWidth / viewportWidth;
            float lengthen = (screenHeight - viewportHeight) * toWorldSpace;
            if (maxWorldHeight > 0) lengthen = Math.min(lengthen, maxWorldHeight - minWorldHeight);
            worldHeight += lengthen;
            extraY = (int) Math.floor(toViewportSpace*lengthen);
            viewportHeight += Math.round(lengthen * toViewportSpace);
        }

        setWorldSize(worldWidth, worldHeight);

        // Center.
        //setScreenBounds((int) test.x, (int) test.y, viewportWidth+((int) test.x)/2, viewportHeight+((int) test.y)/2);
        setScreenBounds((screenWidth - viewportWidth) / 2 + extraX/2, (screenHeight - viewportHeight) / 2 + extraY/2, viewportWidth+extraX, viewportHeight);

        //apply(centerCamera);
        HdpiUtils.glViewport(getScreenX(), getScreenY(), screenWidth, screenHeight);
        getCamera().viewportWidth = worldWidth;
        getCamera().viewportHeight = worldHeight;
        if (centerCamera) getCamera().position.set(worldWidth / 2 + extraX + 10, worldHeight / 2 + extraY, 0);
        getCamera().update();
    }
}
