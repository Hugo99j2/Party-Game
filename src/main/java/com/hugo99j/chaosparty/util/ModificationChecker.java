package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ModificationChecker {
    private static final Map<Field, Integer> requiredValues = new HashMap<>();

    static {
        add(Vector2.class, "Zero");
        add(Vector2.class, "One");
        add(Vector2.class, "X");
        add(Vector2.class, "Y");

        add(Vector4.class, "Zero");
        add(Vector4.class, "One");
        add(Vector4.class, "X");
        add(Vector4.class, "Y");
        add(Vector4.class, "Z");
        add(Vector4.class, "W");

        for (Field field : Color.class.getFields()) {
            if(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) add(field);
        }
    }

    private static void add(Class<?> clazz, String name) {
        try {
            add(clazz.getField(name));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void add(Field f) {
        try {
            requiredValues.put(f, f.get(null).hashCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void verify() {
        requiredValues.forEach((field, value) -> {
            int newHash = 0;
            try {
                newHash = field.get(null).hashCode();
            } catch (IllegalAccessException ignored) {}

            if(newHash != value) throw new RuntimeException("Field " + field.getName() + " from " + field.getDeclaringClass().getName() + " has changed!");
        });
    }
}
