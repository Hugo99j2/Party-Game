package com.hugo99j.chaosparty;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.launchermixin.LauncherMixin;
import com.hugo99j.chaosparty.launchermixin.Test2Mixin3;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.SystemProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static net.fabricmc.loader.impl.util.SystemProperties.SKIP_MC_PROVIDER;

public class LoaderMain {
    static Thread d = Thread.currentThread();

    public static void main(String[] args) {
//        String s = "";
//        for (Field field : SystemProperties.class.getFields()) {
//            try {
//                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
//                    s += "-D" + field.get(null) + "=true\n";
//                }
//            } catch (IllegalAccessException e) {}
//        }
//        System.out.println(s);
//
        System.setProperty(SKIP_MC_PROVIDER, "true");
//
//        new Thread(() -> {
//            while (true) {
//                if(isClassLoaded("com.badlogic.gdx.graphics.glutils.ShapeRenderer")) {
//                    break;
//                }
//            }
//            System.err.println(Arrays.toString(d.getStackTrace()));
//            throw new RuntimeException("NOW!");
//        }).start();

//        System.out.println(NumberUtils.class.getClassLoader());
//        System.out.println(LoaderMain.class.getClassLoader());
//        System.out.println(ShapeRenderer.class.getClassLoader());

        // Start up Fabric Loader
        Knot.launch(args, EnvType.CLIENT);
    }

    public static boolean isClassLoaded(String className) {
        try {
            // Get the current system class loader
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();

            // Access the protected findLoadedClass method
            Method method = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            method.setAccessible(true);

            // Invoke the method. It returns null if the class is not loaded.
            Object result = method.invoke(classLoader, className);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

}
