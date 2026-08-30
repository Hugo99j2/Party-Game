package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animator {
    private static final Map<String, Animation> animations = new HashMap<>();

    public static TextureAtlas.AtlasRegion get(String name) {
        return get(name, null, 0);
    }

    public static TextureAtlas.AtlasRegion get(String name, @Nullable Object unique, float startTime) {
        float div = animations.containsKey(name) ? animations.get(name).frameTime() : 1;
        int hash = unique == null ? 0 : unique.hashCode();
        return getFrame(name, (int) Math.floor(Math.abs(hash)+Math.sin(hash)+(GameData.time-startTime)/div));
    }

    public static Animation getInfo(String name) {
        getFrame(name, 0);
        return animations.get(name);
    }

    public static TextureAtlas.AtlasRegion getFrame(String name, int frame) {
        if(!animations.containsKey(name)) {
            try {
                //only load once so no need to cache
                JsonObject data = GsonUtil.parse(PathUtil.get(PathUtil.texture(name + ".anim"), false));
                List<TextureAtlas.AtlasRegion> files = new ArrayList<>();
                if(data.has("sheet")) {
                    int size = data.get("frames").getAsInt();
                    String bigFile = data.get("sheet").getAsString();
                    TextureAtlas.AtlasRegion bigFileTex = ImageUtil.get(bigFile);
                    int pixelsPer = bigFileTex.packedHeight/size;
                    for (TextureRegion[] textureRegion : bigFileTex.split(bigFileTex.packedWidth, pixelsPer)) {
                        files.add(new TextureAtlas.AtlasRegion(textureRegion[0]));
                    }
                } else data.get("frames").getAsJsonArray().forEach(sprite -> {
                    files.add(ImageUtil.get(sprite.getAsString()));
                });
                float frameTime = data.get("frame_time").getAsFloat();
                animations.put(name, new Animation(name, frameTime, files, frameTime*files.size()));
            } catch (Exception e) {
                return ImageUtil.get("missing");
            }
        }
        Animation sprite = animations.get(name);
        int current = (frame + sprite.sprites().size()) % sprite.sprites().size();
        return sprite.sprites().get(current);
    }
}
