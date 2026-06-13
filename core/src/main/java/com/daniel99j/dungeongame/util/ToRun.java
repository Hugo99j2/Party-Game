package com.daniel99j.dungeongame.util;

import java.util.ArrayList;

public class ToRun {
    public static ArrayList<Runnable> runnables = new ArrayList<>();

    public static void run(Runnable runnable) {
        runnables.add(runnable);
    }
}
