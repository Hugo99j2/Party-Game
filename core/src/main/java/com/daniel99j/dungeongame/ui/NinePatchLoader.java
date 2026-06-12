package com.daniel99j.dungeongame.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.util.GsonUtil;
import com.daniel99j.dungeongame.util.PathUtil;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class NinePatchLoader {
    private static final Map<String, NinePatch> patches = new HashMap<>();

    public static NinePatch getNinePatch(String name) {
        name = name.replace(".png", "");
        if(patches.containsKey(name)) return patches.get(name);
        try {
            JsonObject data = GsonUtil.parse(PathUtil.get(PathUtil.texture("ui/"+name+".json"), true));
            NinePatch patch = new NinePatch(GameData.atlas.findRegion("ui/"+name), data.get("left").getAsInt(), data.get("right").getAsInt(), data.get("top").getAsInt(), data.get("bottom").getAsInt());
            patches.put(name, patch);
            return patch;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
