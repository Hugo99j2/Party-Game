package com.hugo99j.chaosparty.level;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.hugo99j.chaosparty.entity.AbstractObject;
import com.hugo99j.chaosparty.util.GsonUtil;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.entity.ObjectTypes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class LevelLoader {
    public static Level loadFromDataOrDisk(String path) {
        if(path.contains(File.separator)) {
            if(!path.endsWith(".map")) {
                Logger.error("Map file "+path+" does not end with .map!");
                return null;
            }
            try {
                String data = Files.readString(Path.of(path));
                return load(data);
            } catch (Exception e) {
                Logger.error("Failed to load level from disk!", e);
                return null;
            }
        } else return loadFromData(path);
    }

    public static Level loadFromData(String name) {
        try {
            String data = PathUtil.get(PathUtil.data("maps/"+name+".map"), true);
            return load(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Level loadFromSave() {
        return null;
    }

    public static Level load(String string) {
        Level out = new Level();
        JsonObject levelObject = GsonUtil.parse(string);
        levelObject.get("objects").getAsJsonArray().forEach((jsonElement -> {
            createObject(jsonElement.getAsJsonObject(), out);
        }));
        levelObject.get("lights").getAsJsonArray().forEach((jsonElement -> {
            createLight(jsonElement.getAsJsonObject(), out);
        }));
        return out;
    }

    public static AbstractObject createObject(JsonObject data, Level out) {
        AbstractObject object;

        JsonObject customData = data.get("custom_data").getAsJsonObject();
        object = ObjectTypes.types.get(data.get("type").getAsString()).reader().apply(customData);
        out.addObjectFromLoad(object);

        AbstractObject.readBasic(object, data);

        object.onAdd(true);
        return object;
    }

    public static void createLight(JsonObject data, Level out) {
        String type = data.get("lightClass").getAsString().replace("class box2dLight.", "");
        UUID uuid = UUID.fromString(data.get("uuid").getAsString());
        Color colour = Color.valueOf(data.get("colour").getAsString());
        float direction = data.get("direction").getAsFloat();
        float distance = data.get("distance").getAsFloat();
        boolean xray = data.get("xray").getAsBoolean();
        boolean active = data.get("active").getAsBoolean();
        boolean _static = data.get("static").getAsBoolean();
        boolean soft = data.get("soft").getAsBoolean();
        float softShadowLength = data.get("softShadowLength").getAsFloat();
        int rays = data.get("rays").getAsInt();
        float x = data.get("x").getAsFloat();
        float y = data.get("y").getAsFloat();

        LevelLight<?> levelLight;

        if(type.equals("PointLight")) {
            levelLight = out.addLight((rayHandler -> new PointLight(rayHandler, rays, colour, distance, x, y)));
        } else if(type.equals("ConeLight")) {
            levelLight = out.addLight((rayHandler -> new ConeLight(rayHandler, rays, colour, distance, x, y, direction, data.get("coneDegree").getAsFloat())));
        } else if(type.equals("DirectionalLight")) {
            levelLight = out.addLight((rayHandler -> new DirectionalLight(rayHandler, rays, colour, direction)));
        } else {
            throw new IllegalStateException("Unknown light type");
        }

        //noinspection usagelimited
        levelLight.setUuid(uuid);

        levelLight.light().setActive(active);
        levelLight.light().setXray(xray);
        levelLight.light().setSoft(soft);
        levelLight.light().setStaticLight(_static);
        levelLight.light().setSoftnessLength(softShadowLength);
    }

    public static String saveLevel(Level level, boolean pretty) {
        JsonObject out = new JsonObject();

        JsonArray objects = new JsonArray();
        for (AbstractObject allObject : level.getAllObjects()) {
            if(allObject.shouldSave()) objects.add(allObject.write());
        }

        out.add("objects", objects);

        JsonArray lights = new JsonArray();
        for (LevelLight<?> levelLight : level.getLights()) {
            if(!levelLight.isDisposed()) lights.add(levelLight.write());
        }

        out.add("lights", lights);

        return pretty ? GsonUtil.PARSER.toJson(out) : GsonUtil.PARSER_COMPACT.toJson(out);
    }
}
