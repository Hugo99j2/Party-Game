package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.daniel99j.djutil.MiscUtils;
import com.hugo99j.chaosparty.GameData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;

public class RenderUtil {
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

    public static void renderText(String text, int x, int y, float size, int width, int align, boolean wrap) {
        GameData.FONT.getData().setScale(size);

        String newText = text.replace("[", "[[");
        while(newText.contains("<colour:")) {
            String data = MiscUtils.getTextBetween(newText, "<colour:", ">");
            newText = newText.replace("<colour:"+data+">", "["+data.toUpperCase()+"]");
        }
        GameData.FONT.draw(GameData.spriteBatch, newText, x, y, width, align, wrap);
    }
}
