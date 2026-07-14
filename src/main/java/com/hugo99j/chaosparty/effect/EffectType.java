package com.hugo99j.chaosparty.effect;

import java.util.HashMap;
import java.util.Map;

public enum EffectType {
    RED("red", "pixel.r = 1f;"),
    GREEN("green", "pixel.g = 1f;"),
    BLUE("blue", "pixel.b = 1f;"),
    WARM("warm", """
            vec3 glowColor = vec3(1.0, 0.5, 0.2);
            float glowIntensity = 0.7;

            float pulse = 1.0 + sin(u_time) * 0.2;

            // Create a basic circular mask for the center of the object
            vec2 center = v_texCoords - vec2(0.5);
            float dist = 0.7-length(center)+(sin(u_time+v_texCoords.x+tan(v_texCoords.y*u_time))*0.2);

            // Smoothstep creates a glowing gradient from the center outwards
            float glow = smoothstep(0.5, 0.1, dist);

            vec3 finalGlow = glowColor * glow * glowIntensity * pulse;

            // Output final color with alpha (adjust alpha for transparency)
            pixel.rgb += finalGlow;
        """),
    THERMAL("thermal", """
        vec3 sceneColor = pixel.rgb;

        // 2. Calculate luminance to serve as our "fake" temperature (0.0 to 1.0)
        // Standard Rec. 709 grayscale coefficients
        float temperature = dot(sceneColor, vec3(0.2126, 0.7152, 0.0722));

        // 3. Define the thermal gradient lookup array (Cold -> Hot)
        // Index 0: Dark/Cold (Blue/Purple)
        // Index 1: Cool (Cyan)
        // Index 2: Neutral (Green)
        // Index 3: Warm (Yellow)
        // Index 4: Hot (Red/White)
        vec3 colors[5];
        colors[0] = vec3(0.0, 0.0, 0.1); // Ultra cold purple-blue
        colors[1] = vec3(0.0, 0.5, 1.0); // Cold blue
        colors[2] = vec3(0.0, 1.0, 0.0); // Medium green
        colors[3] = vec3(1.0, 1.0, 0.0); // Warm yellow
        colors[4] = vec3(1.0, 0.0, 0.0); // Hot red

        // 4. Map the 0.0 - 1.0 temperature range into the array indices
        float ix = temperature * 4.0;
        int index = int(floor(ix));
        float factor = fract(ix); // How far between the two colors we are

        // 5. Clamp values to avoid array out-of-bounds errors
        index = clamp(index, 0, 3);

        // 6. Linearly interpolate between the two closest colors in the spectrum
        vec3 thermalColor = mix(colors[index], colors[index + 1], factor);

        pixel = vec4(thermalColor, 1.0);
        """),
    BITSHIFT("bitshift", """
        vec4 oldpixel = pixel;
        pixel.r = oldpixel.g;
        pixel.g = oldpixel.b;
        pixel.b = oldpixel.r;
    """),


    OVERSATURATION("oversaturation", "pixel.r *= 1.01;\npixel.rgb *= 4;"),

    INVERT("invert", """
        pixel.r = 1-pixel.r;
        pixel.g = 1-pixel.g;
        pixel.b = 1-pixel.b;
        """),
    SPIRAL("spiral", """
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
        """),
    BOUNCY("bouncy", """
        pixel = v_color * texture2D(u_texture, vec2(v_texCoords.x, v_texCoords.y+sin(u_time+v_texCoords.x*20)/8));
        """),
    COLOURBLIND("colourblind", """
        float average = (pixel.r + pixel.g)/2;
        pixel.r = average;
        pixel.g = average;
        """),
    LIQUID("liquid", """
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
        """),
    FISHEYE("fisheye", """
          float aperture = 178.0;
          float apertureHalf = 0.5 * aperture * (PI / 180.0);
          float maxFactor = sin(apertureHalf);

          vec2 uv;
          vec2 xy = 2.0 * v_texCoords - 1.0;
          float d = length(xy);
          if (d < (2.0-maxFactor))
          {
            d = length(xy * maxFactor);
            float z = sqrt(1.0 - d * d);
            float r = atan(d, z) / PI;
            float phi = atan(xy.y, xy.x);

            uv.x = r * cos(phi) + 0.5;
            uv.y = r * sin(phi) + 0.5;
          }
          else
          {
            uv = v_texCoords;
          }
          vec4 c = texture2D(u_texture, uv);
          pixel = c;
                """);

    private static final Map<String, EffectType> NAME_TO_EFFECT;

    static {
        Map<String, EffectType> map = new HashMap<>();
        for (EffectType value : EffectType.values()) {
            map.put(value.name, value);
        }
        NAME_TO_EFFECT = Map.copyOf(map);
    }

    private final String name;
    private final String shader;

    EffectType(String name, String shader) {
        this.name = name;
        this.shader = shader;
    }

    public String getName() {
        return name;
    }

    public String getShader() {
        return shader;
    }

    public static Map<String, EffectType> getNameToEffect() {
        return NAME_TO_EFFECT;
    }

    public static EffectType getEffectType(String name) {
        return NAME_TO_EFFECT.get(name);
    }
}
