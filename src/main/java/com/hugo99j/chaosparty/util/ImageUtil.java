package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector4;
import com.hugo99j.chaosparty.GameData;

import java.util.*;

import static com.hugo99j.chaosparty.GameData.px;

public class ImageUtil {
    private static final Map<String, TextureAtlas.AtlasRegion> cachedSprites = new HashMap<>();

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
    }

    public static Vector4 getSize(String texture) {
        TextureAtlas.AtlasRegion textureRegion = get(texture);
        TextureData textureData = textureRegion.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap pixmap = textureData.consumePixmap();

        Vector4 result = new Vector4(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        boolean found = false;

        for (int x = 0; x < textureRegion.packedWidth; x++) {
            for (int y = 0; y < textureRegion.packedHeight; y++) {
                int pixel = pixmap.getPixel(x+textureRegion.getRegionX(), y+textureRegion.getRegionY());
                int alpha = (pixel >>> 24) & 0xFF;

                if (alpha > 0) {
                    if (!found) {
                        result.set(x, y, x, y);
                        found = true;
                    } else {
                        result.x = Math.min(result.x, x);
                        result.y = Math.min(result.y, y);
                        result.z = Math.max(result.z, x);
                        result.w = Math.max(result.w, y);
                    }
                }
            }
        }

        if (!found) {
            throw new RuntimeException("Failed to find texture " + texture);
        }

        if (textureData.disposePixmap()) {
            pixmap.dispose();
        }

        return new Vector4(px(result.x-1), px(result.y+1), px(result.z), px(result.w));
    }
}
