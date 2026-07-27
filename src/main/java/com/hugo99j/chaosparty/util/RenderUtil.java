package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.dungeongame.ui.screenss.ScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.mixin.WindowAccessor;
import com.hugo99j.chaosparty.ui.BitmapCacheScaler;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;

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
    private static int TEXT_HEIGHT = -1; //normally 27

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
                    colorMap.put(field.getName().toLowerCase(), (Color) field.get(null));
                    colorMapOther.put((Color) field.get(null), field.getName().toLowerCase());
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

    @Deprecated
    public static void renderText(String text, int x, int y, float size, int width, int align, boolean wrap) {
        GameData.FONT.getData().setScale(size);

        String newText = text.replace("[", "[[");
        while(newText.contains("<colour:")) {
            String data = MiscUtils.getTextBetween(newText, "<colour:", ">");
            newText = newText.replace("<colour:"+data+">", "["+data.toUpperCase()+"]");
        }
        GameData.FONT.draw(GameData.spriteBatch, newText, x, y, width, align, wrap);
    }

    public static void renderTextWorld(String text, float x, float y, float size) {
        renderText(text, ScreenSSBuilder.create().newChild("main").set("x", x).set("y", y).set("xSize", 1000).set("ySize", size).set("textOverrideSize", size).finishChild().build().get("main"));
    }

    public static TextData renderText(String text, ScreenSS ss) {
        TextData data = getInfoAbout(text, ss);
        GameData.FONT.getCache().translate(ss.getX()+data.offsetX, ss.getY()+data.offsetY);
        GameData.FONT.getCache().draw(GameData.spriteBatch);
        return data;
    }

    public static int getHeight(String text) {
        if(TEXT_HEIGHT == -1) {
            GameData.FONT.getData().setScale(1);
            GameData.FONT.getCache().clear();
            GlyphLayout layout = GameData.FONT.getCache().addText("TEST TEXT", 0, 0);
            TEXT_HEIGHT = (int) layout.height;
        }

        if(text.isEmpty()) return 0;
        int out = 27;
        while(text.contains("\n")) {
            out += 27 * 2;
            text = text.replaceFirst("\n", "");
        }
        return out;
    }

    public static TextData getInfoAbout(String text, ScreenSS ss) {
        //GameData.FONT.getData().setScale(size);
        GameData.FONT.getData().setScale(1);

        String newText = text.replace("[", "[[");
        newText = text.replace("\\<", "《");
        while(newText.contains("<colour:")) {
            String data = MiscUtils.getTextBetween(newText, "<colour:", ">");
            String newData = data.toUpperCase();
            if(data.equals("rainbow")) {
                int argb = java.awt.Color.HSBtoRGB(0.5f+(float) Math.cos(GameData.time/2+ss.getX()+ss.getY())/2f, 1, 1);
                // ARGB to RGBA
                int rgba = (argb << 8) | ((argb >>> 24) & 0xFF);
                Color c = new Color(rgba);
                newData = "#" + c;
            }
            newText = newText.replace("<colour:"+data+">", "["+newData+"]");
        }
        while(newText.contains("<icon:")) {
            String data = MiscUtils.getTextBetween(newText, "<icon:", ">");
            newText = newText.replace("<icon:"+data+">", String.valueOf(GameData.getIcons().get(data)));
        }
        while(newText.contains("<hidden:")) {
            String data = MiscUtils.getTextBetween(newText, "<hidden:", ">");
            newText = newText.replace("<hidden:"+data+">", "");
        }
        newText = newText.replace("《", "<");

        GameData.FONT.getCache().clear();
        GlyphLayout layout = GameData.FONT.getCache().addText(newText, 0, 0);
        float scaleX = (ss.getXSize()/layout.width);
        float scaleY = (ss.getYSize()/layout.height);
        float scale = Math.min(scaleX, scaleY);
        if(ss.has("textOverrideSize")) scale = (float) ss.get("textOverrideSize") * GameData.spriteBatch.getProjectionMatrix().getScaleX() / 16;
        ((BitmapCacheScaler) GameData.FONT.getCache()).scale(scale);
        float offsetX = 0;
        if(ss.has("textAlign") && ss.get("textAlign") != 0) {
            float align = (float) ss.get("textAlign"); // 0 = left, 0.5 = center, 1 = right
            offsetX = (ss.getXSize() - layout.width * scale) * align;
        }

        return new TextData(layout, offsetX, layout.height*scale, scale, layout.width, layout.height);
    }

    public static boolean isFocused() {
        if(GameData.DEBUGGING && Debuggers.isEnabled("forceFocus")) return true;
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Lwjgl3Window window = ((WindowAccessor) app).getWindows().get(0);
        return window != null && window.isFocused();
    }

    public static String toString(Color color) {
        return colorMapOther.get(color);
    }

    public static Color fromString(String color) {
        return colorMap.get(color.toLowerCase());
    }

    public static record TextData(GlyphLayout layout, float offsetX, float offsetY, float scale, float width, float height) {}
}
