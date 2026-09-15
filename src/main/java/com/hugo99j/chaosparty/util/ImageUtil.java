package com.hugo99j.chaosparty.util;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    public static void clearCache() {
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

    private static Integer getHash(List<String> exclude) {
        StringBuilder longHash = new StringBuilder();
        List<String> paths = PathUtil.getFilesIn("assets/textures/");
        for (String full : paths) {
            String path = full.replace("assets/textures/", "");
            try {
                if (path.endsWith(".png") && exclude.stream().noneMatch((e) -> path.equals(e) || (e.endsWith("/") && path.startsWith(e)))) longHash.append(Arrays.hashCode(Files.readAllBytes(Path.of(PathUtil.codingDir(PathUtil.texture(path)))))).append(" ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return longHash.toString().hashCode();
    }

    public static void generateImageBounds() {
        List<String> exclude = Arrays.stream(PathUtil.get(PathUtil.texture(".excluded_bounds"), false).split("\n")).toList();
        boolean mustRecalculate = false;
        try {
            int old = GsonUtil.parse(PathUtil.get(PathUtil.codingDir(PathUtil.generated("image_bounds.json")), false)).get("version").getAsInt();
            if(!getHash(exclude).equals(old)) {
                mustRecalculate = true;
            }
        } catch (Exception e) {
            Logger.error("Error checking bounds changes", e);
            mustRecalculate = true;
        }

        if(!mustRecalculate) {
            Logger.info("No image bounds changes");
            return;
        }

        Logger.info("Regenerating image bounds");
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
            String fileWithPng = fileFull.replace("assets/textures/", "");
            if(!fileFull.endsWith(".png")) {
                Logger.info(fileWithPng +" was not an image, skipping");
                continue;
            }
            if(exclude.stream().anyMatch((e) -> fileWithPng.equals(e) || (e.endsWith("/") && fileWithPng.startsWith(e)))) {
                Logger.info(fileWithPng +" was excluded");
                continue;
            }
            String file = fileWithPng.replace(".png", "");
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
            out.addProperty("version", getHash(exclude));

            Path p = Path.of(PathUtil.codingDir("gen/image_bounds.json"));
            Files.writeString(p, GsonUtil.PARSER_COMPACT.toJson(out));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Logger.info("Bounds generation complete.");
    }
}
