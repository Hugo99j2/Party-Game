package com.daniel99j.dungeongame.entity;

public class CollisionCategories {
    public static final short DONT_COLLIDE_WITH_EACH_OTHER = get();
    public static final short DEFAULT = get();
    public static final short PLAYER = get();
    public static final short ENEMY = get();
    public static final short WALL = get();
    public static final short PATHFIND_BLOCKING = get();
    public static final short LIGHT_BLOCKING = get();

    private static int current = 0;

    private static short get() {
        if(current >= 16) throw new IllegalStateException("Too many collision categories");
        return (short)(1 << current++);
    }

    public static short allBut(short category) {
        return (short)(0xFFFF & ~category);
    }
}
