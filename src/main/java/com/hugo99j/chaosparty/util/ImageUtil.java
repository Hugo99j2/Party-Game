package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import net.fabricmc.loader.impl.util.log.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.Deflater;

import static com.hugo99j.chaosparty.GameData.px;

public class ImageUtil {
    private static final Map<String, TextureAtlas.AtlasRegion> cachedSprites = new HashMap<>();
    private static final Map<String, Rectangle> cachedSpriteSizes = new HashMap<>();

    public static TextureAtlas.AtlasRegion get(String name) {
        if(cachedSprites.containsKey(name)) return cachedSprites.get(name);
        TextureAtlas.AtlasRegion textureRegion = GameData.atlas.findRegion(name);
        if(textureRegion == null) textureRegion = get("missing");
        cachedSprites.put(name, textureRegion);
        return textureRegion;
    }

    public static int size() {
        return cachedSprites.size();
    }

    public static void clear() {
        cachedSprites.clear();
        cachedSpriteSizes.clear();
    }

    /**
     * Returns startX, startY, width, height
     */
    public static Rectangle getSize(String texture) {
        if(!cachedSpriteSizes.containsKey(texture)) {
            JsonArray jsonArray = GsonUtil.parse(PathUtil.get(PathUtil.generated("image_bounds.json"), true)).get("values").getAsJsonObject().get(texture).getAsJsonArray();
            cachedSpriteSizes.put(texture, new Rectangle(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsInt(), jsonArray.get(2).getAsInt(), jsonArray.get(3).getAsInt()));
        }
        return cachedSpriteSizes.get(texture);
    }

    public static boolean generateImageBounds() {
        boolean mustRecalculate = true;
        try {
            int realHash = Arrays.hashCode(Files.readAllBytes(Path.of(PathUtil.codingDir(PathUtil.generated("atlases/main.png")))));
            int lastHash = GsonUtil.parse(PathUtil.get(PathUtil.generated("image_bounds.json"), false)).get("version").getAsInt();
            if(realHash == lastHash) mustRecalculate = false;
        } catch (Exception ignored) {}

        if(!mustRecalculate) {
            Logger.info("No image bounds changes.");
            return false;
        }

        Logger.info("Generating image bounds");
        int maxSize = 128;

        JsonObject out = new JsonObject();
        JsonObject values = new JsonObject();

        FrameBuffer buffer = new FrameBuffer(Pixmap.Format.RGBA8888, maxSize, maxSize, false);
        buffer.begin();
        OrthographicCamera camera = new OrthographicCamera();
        Viewport uiViewport = new ScreenViewport(camera);
        uiViewport.update(128, 128, true);
        uiViewport.apply();
        GameData.spriteBatch.setProjectionMatrix(camera.combined);

        GameData.spriteBatch.begin();

        List<String> paths = PathUtil.getFilesIn("assets/textures/");
        for (String fileFull : paths) {
            String file = fileFull.replace(".png", "").replace("assets/textures/", "");
            if(!fileFull.endsWith(".png")) {
                Logger.info(file +" was not an image, skipping");
                continue;
            }
            TextureAtlas.AtlasRegion region = ImageUtil.get(file);
            if(region.originalHeight > maxSize || region.originalWidth > maxSize) {
                Logger.info(file +" was too large, skipping");
                continue;
            }

            GameData.spriteBatch.draw(region, 0, 0, region.getRegionWidth(), region.getRegionHeight());
            //noinspection GDXJavaFlushInsideLoop
            GameData.spriteBatch.flush();

            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, maxSize, maxSize);
            //PixmapIO.writePNG(Gdx.files.local("gen/output_"+ file +".png"), pixmap, Deflater.DEFAULT_COMPRESSION, true);

            boolean foundPixel = false;
            int minX = 10000, minY = 100000, maxX = -1, maxY = -1;

            for (int xPos = 0; xPos < maxSize; xPos++) {
                for (int yPos = 0; yPos < maxSize; yPos++) {
                    int alpha = ((pixmap.getPixel(xPos, yPos) >> 24) & 0xFF);
                    if (alpha > 0) {
                        foundPixel = true;
                        if (xPos < minX) minX = xPos;
                        if (xPos > maxX) maxX = xPos;
                        if (yPos < minY) minY = yPos;
                        if (yPos > maxY) maxY = yPos;
                    }
                }
            }

            Rectangle boundingBox = new Rectangle(minX, minY, maxX-minX+1, maxY-minY+1);
            if (!foundPixel) boundingBox = new Rectangle(0, 0, 0, 0);

            JsonArray array = new JsonArray();
            array.add((int) boundingBox.x);
            array.add((int) boundingBox.y);
            array.add((int) boundingBox.width);
            array.add((int) boundingBox.height);
            values.add(file, array);
            pixmap.dispose();

            ScreenUtils.clear(Color.CLEAR);
            Logger.info("Completed "+ file +" ("+(paths.indexOf(fileFull)+1)+"/"+paths.size()+")");
        }
        GameData.spriteBatch.end();
        buffer.end();
        buffer.dispose();

        try {
            out.add("values", values);
            out.addProperty("version", Arrays.hashCode(Files.readAllBytes(Path.of(PathUtil.codingDir(PathUtil.generated("atlases/main.png"))))));

            Path p = Path.of(PathUtil.codingDir("gen/image_bounds.json"));
            Files.writeString(p, GsonUtil.PARSER_COMPACT.toJson(out));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Logger.info("Bounds generation complete.");
        return true;
    }
}
