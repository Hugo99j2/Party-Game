package com.daniel99j.dungeongame.util;

import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.YouDiedScreen;

import java.util.HashMap;
import java.util.Map;

public class GlobalRunnables {
    public static final Map<String, Runnable> nameToCode = new HashMap<>();
    public static final Map<Runnable, String> codeToName = new HashMap<>();

    public static final Runnable COLLECT_FROST_EMBER = register("collect_frost_ember", () -> {});
    public static final Runnable COLLECT_TREASURE = register("collect_treasure", () -> {
        SoundManager.getSound("coin").play(1);
    });
    public static final Runnable FAIL_RUN = register("fail_run", () -> {
        assert GameData.level != null;
        GameData.level.dispose();
        GameData.level = null;
        GameData.MAIN_INSTANCE.setScreen(new YouDiedScreen());
    });
    public static final Runnable SPAWN_TREASURE = register("spawn_treasure", () -> {

    });

    private static Runnable register(String name, Runnable code) {
        nameToCode.put(name, code);
        codeToName.put(code, name);
        return code;
    }
}
