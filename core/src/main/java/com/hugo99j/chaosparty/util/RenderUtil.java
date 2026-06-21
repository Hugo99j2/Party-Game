package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.dungeongame.ui.screenss.ScreenSS;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.mixin.WindowAccessor;
import com.hugo99j.chaosparty.ui.BitmapCacheScaler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

public class RenderUtil {
    private static final ShaderProgram blurProgram;
    private static final Map<String, Color> colorMap = new HashMap<>();
    private static final Map<Color, String> colorMapOther = new HashMap<>();

    static  {
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
        String fragmentShader =
            """
                #ifdef GL_ES
                #define LOWP lowp
                precision mediump float;
                #else
                #define LOWP
                #endif
                varying LOWP vec4 v_color;
                varying vec2 v_texCoords;
                uniform sampler2D u_texture;
                uniform vec2 u_resolution;
                void main() {
                    vec2 texOffset = 1.0 / u_resolution;
                    vec4 color = vec4(0.0);
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2(-1.0, -1.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 0.0, -1.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 1.0, -1.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2(-1.0,  0.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 0.0,  0.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 1.0,  0.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2(-1.0,  1.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 0.0,  1.0));
                    color += texture2D(u_texture, v_texCoords + texOffset * vec2( 1.0,  1.0));
                    color /= 9.0;
                    gl_FragColor = v_color * color;
                }""";

        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling blur shader: " + shader.getLog());
        blurProgram = shader;

        try {
            for (Field field : Color.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.getType().equals(Color.class)) {
                    colorMap.put(field.getName(), (Color) field.get(null));
                    colorMapOther.put((Color) field.get(null), field.getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ShaderProgram getBlurProgram() {
        return blurProgram;
    }

    public static void enableBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void takeScreenshot() {
        String name = "Screenshot on "+DateTimeFormatter.ofPattern("dd MMM uuuu 'at' HH:mm:ss").format(LocalDateTime.now())+".png";
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local(name), pixmap, Deflater.DEFAULT_COMPRESSION, true);
        pixmap.dispose();
    }

    public static Texture getCurrentFrameBuffer() {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static void renderText(String text, int x, int y, float size, int width, int align, boolean wrap) {
        GameData.FONT.getData().setScale(size);

        String newText = text.replace("[", "[[");
        while(newText.contains("<colour:")) {
            String data = MiscUtils.getTextBetween(newText, "<colour:", ">");
            newText = newText.replace("<colour:"+data+">", "["+data.toUpperCase()+"]");
        }
        GameData.FONT.draw(GameData.spriteBatch, newText, x, y, width, align, wrap);
    }

    public static void renderText(String text, ScreenSS ss) {
        //GameData.FONT.getData().setScale(size);
        GameData.FONT.getData().setScale(1);

        String newText = text.replace("[", "[[");
        while(newText.contains("<colour:")) {
            String data = MiscUtils.getTextBetween(newText, "<colour:", ">");
            newText = newText.replace("<colour:"+data+">", "["+data.toUpperCase()+"]");
        }

        GameData.FONT.getCache().clear();
        GlyphLayout layout = GameData.FONT.getCache().addText(newText, 0, 0);
        float actualWidth = layout.width;
        float scale = (ss.getXSize()/actualWidth);
        ((BitmapCacheScaler) GameData.FONT.getCache()).scale(scale);
        GameData.FONT.getCache().translate(ss.getX(), ss.getY()+(layout.height*scale));
        GameData.FONT.getCache().draw(GameData.spriteBatch);
    }

    public static boolean isFocused() {
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Lwjgl3Window window = ((WindowAccessor) app).getWindows().get(0);
        return window != null && window.isFocused();
    }

    public static String toString(Color color) {
        return colorMapOther.get(color);
    }

    public static Color fromString(String color) {
        return colorMap.get(color);
    }
}
