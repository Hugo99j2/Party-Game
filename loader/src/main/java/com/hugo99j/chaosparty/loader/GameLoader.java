package com.hugo99j.chaosparty.loader;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;

import static net.fabricmc.loader.impl.util.SystemProperties.SKIP_MC_PROVIDER;

/** Launches the desktop (LWJGL3) application. */
public class GameLoader {
    public static void main(String[] args) {
        System.setProperty(SKIP_MC_PROVIDER, "true");
        Knot.launch(args, EnvType.CLIENT);
    }
}
