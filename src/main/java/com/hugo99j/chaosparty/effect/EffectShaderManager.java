package com.hugo99j.chaosparty.effect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.lwjgl.Sys;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EffectShaderManager {
    private static final Map<List<String>, CacheKey> usedShader = new HashMap<>();
    private static float lastFrame = 0;

    public static void apply(List<ActiveEffect> active) {
        List<String> effects = new ArrayList<>();
        active.forEach(effect -> effects.add(effect.getEffect()));

        if(lastFrame != Gdx.graphics.getDeltaTime()) {
            lastFrame = Gdx.graphics.getDeltaTime();
            List<List<String>> toRemove = new ArrayList<>();
            usedShader.forEach((name, key) -> {
                key.timer.object-=Gdx.graphics.getDeltaTime();
                if(key.timer.object < 0) {
                    toRemove.add(name);
                    key.shader.dispose();
                }
            });
            toRemove.forEach(usedShader::remove);
        }
        if(effects.isEmpty()) return;
        if (!usedShader.containsKey(effects)) {
            String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
            String fragmentShader = """
                #ifdef GL_ES
                #define LOWP lowp
                precision mediump float;
                #else
                #define LOWP\s
                #endif
                varying LOWP vec4 v_color;
                varying vec2 v_texCoords;
                uniform sampler2D u_texture;
                uniform vec2 u_resolution;
                uniform float u_time;
                void main()
                {
                  vec4 pixel = v_color * texture2D(u_texture, v_texCoords);
                  EFFECTS
                  gl_FragColor = pixel;
                }""".replace("EFFECTS", String.join("\n", effects));
            System.out.println(fragmentShader);

            ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
            if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling effects shader: " + shader.getLog());
            usedShader.put(effects, new CacheKey(shader, new ValueHolder<>(5f)));
        }
        usedShader.get(effects).timer.object = 5f;
        ShaderProgram shader = usedShader.get(effects).shader;
        shader.bind();
        shader.setUniformf("u_resolution", GameData.width, GameData.height);
        shader.setUniformf("u_time", GameData.time);
        GameData.spriteBatch.setShader(shader);
    }

    private record CacheKey(ShaderProgram shader, ValueHolder<Float> timer) {}
}
