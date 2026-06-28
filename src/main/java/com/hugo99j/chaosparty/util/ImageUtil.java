package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.hugo99j.chaosparty.GameData;

import java.util.*;

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
}
