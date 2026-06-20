package com.hugo99j.chaosparty;

import com.hugo99j.chaosparty.loader.Lwjgl3Launcher;
import org.lwjgl.Sys;

public final class Launcher {
    /**
     * The entry point to the example program.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        var base = new Lwjgl3Launcher();
        base.main(args);
    }
}
