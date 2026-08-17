package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteBatch.class)
public class SpriteBatchMixin {
    @Inject(method = "setColor(Lcom/badlogic/gdx/graphics/Color;)V", at = @At(value = "HEAD"), cancellable = true)
    private void noChangingAllowed(Color tint, CallbackInfo ci) {
        if(Debuggers.disableChangingColour) ci.cancel();
    }

    @Inject(method = "setColor(FFFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void noChangingAllowed2(float r, float g, float b, float a, CallbackInfo ci) {
        if(Debuggers.disableChangingColour) ci.cancel();
    }
}
