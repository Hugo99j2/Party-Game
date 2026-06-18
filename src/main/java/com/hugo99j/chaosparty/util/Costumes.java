package com.hugo99j.chaosparty.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Costumes {
    private static final Map<CostumePart, List<String>> sprites;

    static {
        Map<CostumePart, List<String>> list = new HashMap<>();
        for (String fileName : PathUtil.getFilesIn(PathUtil.asset("textures/costumes"))) {
            fileName = fileName.replace("assets/textures/costumes/", "").replace(".png", "");
            CostumePart part = null;
            for (CostumePart value : CostumePart.values()) {
                if(fileName.startsWith(value.name().toLowerCase())) {
                    part = value;
                    break;
                }
            }

            if(part == null) continue;
            if(!list.containsKey(part)) list.put(part, new ArrayList<>());
            list.get(part).add(fileName);
        }

        sprites = Map.copyOf(list);
    }

    public static List<String> getVariants(CostumePart part) {
        return List.copyOf(sprites.get(part));
    }
}
