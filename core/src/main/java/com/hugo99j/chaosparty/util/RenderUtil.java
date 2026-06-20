package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.daniel99j.djutil.MiscUtils;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.mixin.WindowAccessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;

public class RenderUtil {
    private static final ShaderProgram blurProgram;

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

    public static boolean isFocused() {
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Lwjgl3Window window = ((WindowAccessor) app).getWindows().get(0);
        return window != null && window.isFocused();
    }
}
