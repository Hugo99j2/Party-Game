package com.hugo99j.chaosparty.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics$1")
public abstract class ResizeSpeederUpperMixin {
    @Redirect(
        method = "invoke",
        at = @At(
            value = "INVOKE",
    target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"
        )
    )
    public void speed(long window) {
    }
}
