package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hugo99j.chaosparty.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lwjgl3Graphics.class)
public abstract class TestMixin {
    @Inject(method = "getMonitor", at = @At("HEAD"))
    private void hideIfInvisible1(CallbackInfoReturnable<Graphics.Monitor> cir) {
        throw new IllegalArgumentException("TEST");
    }
}
