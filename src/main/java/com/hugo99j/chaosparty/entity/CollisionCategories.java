package com.hugo99j.chaosparty.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CollisionCategories {
    public static final short DEFAULT = get();
    public static final short PLAYER = get();
    public static final short WALL = get();
    public static final short PATHFIND_BLOCKING = get();
    public static final short LIGHT_BLOCKING = get();

    private static int current = 0;
    private static final Map<String, Short> allCategories = new HashMap<>();

    static {
        for (Field declaredField : CollisionCategories.class.getDeclaredFields()) {
            if(declaredField.getName().toUpperCase().equals(declaredField.getName())) {
                try {
                    allCategories.put(declaredField.getName(), (Short) declaredField.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static Map<String, Short> getAllCategories() {
        return new HashMap<>(allCategories);
    }

    private static short get() {
        if(current >= 16) throw new IllegalStateException("Too many collision categories");
        return (short)(1 << current++);
    }

    public static short allBut(short category) {
        return (short)(0xFFFF & ~category);
    }

    public static short all() {
        return (short)(0xFFFF);
    }
}
