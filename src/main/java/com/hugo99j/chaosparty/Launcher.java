package com.hugo99j.chaosparty;

import com.hugo99j.chaosparty.loader.Lwjgl3Launcher;

public final class Launcher {
    /**
     * The entry point to the example program.
     *
     * @param args The command line arguments
     */
    public static void notMain(String[] args) {
        new Lwjgl3Launcher().alsoNotMain(args);
    }
}
