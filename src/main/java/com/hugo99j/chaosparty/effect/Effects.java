package com.hugo99j.chaosparty.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Effects {
    private static final Map<String, String> ALL_EFFECTS = new HashMap<>();

    public static final String RED = add("red", "pixel.r = 1f;");
    public static final String GREEN = add("green", "pixel.g = 1f;");
    public static final String BLUE = add("blue", "pixel.b = 1f;");
    public static final String INVERT = add("invert", """
        pixel.r = 1-pixel.r;
        pixel.g = 1-pixel.g;
        pixel.b = 1-pixel.b;
        """);
    public static final String SPIRAL = add("spiral", """
vec2 uv = v_texCoords;

vec2 center = vec2(0.5, 0.5);
vec2 p = uv - center;

float r = length(p);
float angle = atan(p.y, p.x);

// Twist amount (stronger near the center)
angle += (1.0 - r) * 4.0 + u_time;

vec2 warped;
warped.x = cos(angle) * r;
warped.y = sin(angle) * r;

uv = warped + center;

pixel = v_color * texture2D(u_texture, uv);
        """);
    public static final String BOUNCY = add("bouncy", """
        pixel = v_color * texture2D(u_texture, vec2(v_texCoords.x, v_texCoords.y+sin(u_time+v_texCoords.x*20)/8));
        """);
    public static final String COLOURBLIND = add("colourblind", """
        float average = (pixel.r + pixel.g)/2;
        pixel.r = average;
        pixel.g = average;
        """);
    public static final String LIQUID = add("liquid", """
         float PI = 3.1415;
         float uTwist = 2;
         float uFalloff = 0.89;
         float uContrast = 1;
         float uColor = 1;
         mat3 m3 = mat3(-0.7373, 0.4562, 0.4980, 0, -0.7373, 0.6754, 0.6754, 0.4980, 0.5437);
         //https://oneshader.net/shader/92a24c32c4#uTwist=2&ufalloff=0.89&uContrast=0.25&uColor=0.52
        vec3 p = vec3(v_texCoords * PI, u_time * 0.2);

         float a = 1.;
          vec3 n = vec3(0);
          for(int i = 0; i <7 ; i++){
            p = m3 * p;
            vec3 s = sin( p.zxy / a) * a;
            p += s * uTwist;
            n += s;
            a *= uFalloff;
          }

        vec3 offset = mix(vec3((n.x + n.y + n.z) * 0.5), n, 0.5) * 0.25 + 0.5;

        offset *= offset.z;

        offset.x /= (u_resolution.x/10);
        offset.y /= (u_resolution.y/10);

        pixel = v_color * texture2D(u_texture, vec2(v_texCoords.x+offset.x, v_texCoords.y+offset.y));
        //pixel = vec4(offset.x, offset.y, 0, 1);
        """);

    private static String add(String name, String effect) {
        ALL_EFFECTS.put(name, effect);
        return effect;
    }

    public static Map<String, String> getAllEffects() {
        return ALL_EFFECTS;
    }
}
