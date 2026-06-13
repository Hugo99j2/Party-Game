package com.daniel99j.dungeongame.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.daniel99j.djutil.MiscUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

public class PathUtil {
    private static final Map<String, String> files = new HashMap<>();

    public static String codingDir(String old) {
        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            String p = Path.of(old).toAbsolutePath().toString();
            if(p.contains("Party-Game")) return MiscUtils.getTextBetween(p, "", "Party-Game")+"Party-Game/core/src/main/resources/"+old;
            if(p.contains("Chaos Party")) return MiscUtils.getTextBetween(p, "", "Chaos Party")+"Chaos Party/core/src/main/resources/"+old;
            throw new IllegalStateException("Incorrect path");
        }
        throw new RuntimeException("Game is not being developed");
    }

    public static String texture(String old) {
        return asset("textures/"+old);
    }

    public static String asset(String old) {
        return "assets/"+old;
    }

    public static String data(String old) {
        return "data/"+old;
    }

    public static String generated(String old) {
        return "gen/"+old;
    }

    public static List<String> getFilesIn(String p) {
        String folders = get("tree.txt", true);
        List<String> paths = new ArrayList<>();
        folders.lines().forEach((l) -> {
            if(l.startsWith(p)) paths.add(l);
        });
        return paths;
    }

    public static String get(String p, boolean cache) {
        if(files.containsKey(p)) return files.get(p);

        try (InputStream in = Gdx.files.internal(p).read()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String out = "";
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        if (!out.isEmpty()) out += "\n";
                        out += line;
                    } else break;
                }
                if(cache) files.put(p, out);
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
