package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shaders.ShadowShader;

@Mixin(ShadowShader.class)
public class ShadowShaderPixeliserMixin {
    @Inject(method = "createShadowShader", at = @At(value = "HEAD"), cancellable = true)
    private static void pixel(CallbackInfoReturnable<ShaderProgram> cir) {
        final String vertexShader = "attribute vec4 a_position;\n" //
            + "attribute vec2 a_texCoord;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "\n" //
            + "void main()\n" //
            + "{\n" //
            + "   v_texCoords = a_texCoord;\n" //
            + "   gl_Position = a_position;\n" //
            + "}\n";
        final String fragmentShader = """
            #ifdef GL_ES
            precision lowp float;
            #define MED mediump
            #else
            #define MED
            #endif

            varying MED vec2 v_texCoords;

            uniform sampler2D u_texture;
            uniform vec4 ambient;

            uniform vec2 cameraPosition;
            uniform vec2 cameraWorldSize;

            void main()
            {
                vec2 uv = v_texCoords;

                //0.5 is center
                vec2 worldPos =
                    cameraPosition +
                    (uv - 0.5) * cameraWorldSize;

                float pixelSize = 1.0/16;

                // Lock the position to the world-space pixel grid.
                worldPos = floor(worldPos / pixelSize) * pixelSize;

                // Convert world position back -> UV.
                uv =
                    (worldPos - cameraPosition) /
                    cameraWorldSize +
                    0.5;

                vec4 c = texture2D(u_texture, uv);

                gl_FragColor.rgb = c.rgb * c.a + ambient.rgb;
                gl_FragColor.a = ambient.a - c.a;
            }
            """;
        ShaderProgram.pedantic = false;
        ShaderProgram shadowShader = new ShaderProgram(vertexShader,
            fragmentShader);
        if (!shadowShader.isCompiled()) {
            shadowShader = new ShaderProgram("#version 330 core\n" +vertexShader,
                "#version 330 core\n" +fragmentShader);
            if(!shadowShader.isCompiled()){
                Gdx.app.log("ERROR", shadowShader.getLog());
            }
        }

        cir.setReturnValue(shadowShader);
    }
}
