package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lwjgl3Window.class)
public abstract class WindowFixMixin {
    @Inject(method = "getPositionX", at = @At("HEAD"), cancellable = true)
    private void noError(CallbackInfoReturnable<Integer> cir) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) cir.setReturnValue(0);
    }

    @Inject(method = "getPositionY", at = @At("HEAD"), cancellable = true)
    private void noError2(CallbackInfoReturnable<Integer> cir) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) cir.setReturnValue(0);
    }
}
