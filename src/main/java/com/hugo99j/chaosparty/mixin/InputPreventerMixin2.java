package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.backends.lwjgl3.DefaultLwjgl3Input;
import com.hugo99j.chaosparty.util.InputPreventer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultLwjgl3Input.class)
public class InputPreventerMixin2 {

    @Inject(method = "isButtonPressed", at = @At("HEAD"), cancellable = true)
    private void prevent1(int button, CallbackInfoReturnable<Boolean> cir) {
        if(((InputPreventer) this).isPreventing()) cir.setReturnValue(false);
    }

    @Inject(method = "isButtonJustPressed", at = @At("HEAD"), cancellable = true)
    private void prevent2(int button, CallbackInfoReturnable<Boolean> cir) {
        if(((InputPreventer) this).isPreventing()) cir.setReturnValue(false);
    }
}
