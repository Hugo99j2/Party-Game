package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.daniel99j.dungeongame.level.Level;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.Logger;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shaders.ShadowShader;

@Mixin(targets = "box2dLight.LightMap")
public class ShadowShaderPixeliserMixin2 {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;setUniformf(Ljava/lang/String;FFFF)V", ordinal = 1))
    private static void pixel(ShaderProgram instance, String name, float value1, float value2, float value3, float value4) {
        instance.setUniformf(
            "cameraPosition",
            Level.lightCamera.position.x,
            Level.lightCamera.position.y
        );

        instance.setUniformf(
            "cameraWorldSize",
            Level.lightCamera.viewportWidth * Level.lightCamera.zoom,
            Level.lightCamera.viewportHeight * Level.lightCamera.zoom
        );

        instance.setUniformf(name, value1, value2, value3, value4);
    }
}
