package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hugo99j.chaosparty.ui.ScreenCenterer;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Viewport.class)
public class ExtendViewportMixin implements ScreenCenterer {
    @Shadow
    private Camera camera;
    @Shadow
    private float worldWidth;
    @Shadow
    private float worldHeight;
    @Unique
    private boolean center = false;

    @Inject(method = "apply(Z)V", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/Camera;update()V", shift =  At.Shift.BEFORE))
    private void center(boolean centerCamera, CallbackInfo ci) {
        if (center && ((Object) this) instanceof ExtendViewport extendViewport) {
            this.camera.position.set((worldWidth / 2) - ((worldWidth-extendViewport.getMinWorldWidth()) / 2), (worldHeight / 2), 0);
        }
    }

    @Override
    public void party_Game$setCenter(boolean center) {
        this.center = center;
    }
}
