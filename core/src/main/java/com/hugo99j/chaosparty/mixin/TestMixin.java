package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hugo99j.chaosparty.Main;
import com.hugo99j.chaosparty.ui.MenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lwjgl3Graphics.class)
public abstract class TestMixin {
    @Shadow
    public abstract Graphics.Monitor getPrimaryMonitor();

    @Inject(method = "setFullscreenMode", at = @At("HEAD"), cancellable = true)
    private void noError(CallbackInfoReturnable<Graphics.Monitor> cir) {
        throw new IllegalStateException();
    }
}
