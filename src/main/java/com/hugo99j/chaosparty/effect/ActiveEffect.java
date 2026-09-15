package com.hugo99j.chaosparty.effect;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ActiveEffect {
    protected static final String DEFAULT_VERT = """
            attribute vec4 pos_attr;
            attribute vec4 color_attr;
            attribute vec2 tex_attr0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;

            void main() {
               v_color = color_attr;
               v_color.a = v_color.a * (255.0/254.0);
               v_texCoords = tex_attr0;
               gl_Position =  u_projTrans * pos_attr;
            }
            """.replace("pos_attr", ShaderProgram.POSITION_ATTRIBUTE).replace("color_attr", ShaderProgram.COLOR_ATTRIBUTE).replace("tex_attr", ShaderProgram.TEXCOORD_ATTRIBUTE);
    protected static final String DEFAULT_FRAG = """
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
        #define PI 3.1415926535
        void main() {
          gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
          //if(gl_FragColor.a < 1) gl_FragColor.g = 1;
        }""";

    private final EffectType effect;
    private float remaining;

    public ActiveEffect(EffectType effect, float time) {
        this.effect = effect;
        this.remaining = time;
    }

    public EffectType getEffect() {
        return effect;
    }

    public float getRemaining() {
        return remaining;
    }

    public void setRemaining(float v) {
        remaining = v;
    }
}
