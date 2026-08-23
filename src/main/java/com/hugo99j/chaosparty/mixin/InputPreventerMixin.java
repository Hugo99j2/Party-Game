package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.backends.lwjgl3.DefaultLwjgl3Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.hugo99j.chaosparty.util.InputPreventer;
import org.checkerframework.checker.units.qual.A;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractInput.class)
public class InputPreventerMixin implements InputPreventer {
    @Unique
    private boolean prevent = false;

    @Inject(method = "isKeyJustPressed", at = @At("HEAD"), cancellable = true)
    private void prevent1(int button, CallbackInfoReturnable<Boolean> cir) {
        if(prevent) cir.setReturnValue(false);
    }

    @Inject(method = "isKeyPressed", at = @At("HEAD"), cancellable = true)
    private void prevent2(int button, CallbackInfoReturnable<Boolean> cir) {
        if(prevent) cir.setReturnValue(false);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setPrevent(boolean prevent) {
        this.prevent = prevent;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public boolean isPreventing() {
        return this.prevent;
    }
}
