package com.hugo99j.chaosparty.match;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.util.CostumePart;
import com.hugo99j.chaosparty.util.Costumes;
import com.hugo99j.chaosparty.util.GsonUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private final Map<CostumePart, String> costume = new HashMap<>();

    private static final List<User> loadedUsers = new ArrayList<>();

    protected User(String name) {
        this.name = name;
        for (CostumePart value : CostumePart.values()) {
            costume.put(value, Costumes.getVariants(value).getFirst());
        }
    }

    public static User getUser(int i) {
        return loadedUsers.get(i-1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWearing(CostumePart part) {
        return costume.get(part);
    }

    public static void saveUsers() {
        Path path = Path.of("profiles.json");
        JsonObject object = new JsonObject();
        JsonArray profiles = new JsonArray();
        for (User loadedUser : loadedUsers) {
            JsonObject userObject = new JsonObject();
            userObject.addProperty("name", loadedUser.getName());
            for (CostumePart part : CostumePart.values()) {
                userObject.addProperty(part.toString().toLowerCase(), loadedUser.getWearing(part));
            }
            profiles.add(userObject);
        }
        object.add("profiles", profiles);
        try {
            Files.writeString(path, GsonUtil.PARSER.toJson(object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        Path path = Path.of("profiles.json");
        if(Files.exists(path)) {
            try {
                JsonObject object = GsonUtil.parse(Files.readString(path));
                object.get("profiles").getAsJsonArray().forEach((e) -> {
                    JsonObject o = e.getAsJsonObject();
                    User u = new User(o.get("name").getAsString());
                    for (CostumePart part : CostumePart.values()) {
                        u.costume.put(part, o.get(part.toString().toLowerCase()).getAsString());
                    }
                    loadedUsers.add(u);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                loadedUsers.add(new User("Guest "+i));
            }
        }
        saveUsers();
    }
}
