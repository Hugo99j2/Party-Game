package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lwjgl3Graphics.class)
public abstract class MonitorFixMixin {
    @Shadow
    public abstract Graphics.Monitor getPrimaryMonitor();

    @Inject(method = "getMonitor", at = @At("HEAD"), cancellable = true)
    private void noError(CallbackInfoReturnable<Graphics.Monitor> cir) {
        if(SharedLibraryLoader.os == Os.Linux) cir.setReturnValue(getPrimaryMonitor());
    }
}
