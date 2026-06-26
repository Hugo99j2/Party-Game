package com.hugo99j.chaosparty;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.launchermixin.LauncherMixin;
import com.hugo99j.chaosparty.launchermixin.Test2Mixin3;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.SystemProperties;
import org.lwjgl.Sys;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.launch.platform.MainAttributes;
import org.spongepowered.asm.util.VersionNumber;
import org.spongepowered.asm.util.asm.ASM;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.jar.Attributes;

import static net.fabricmc.loader.impl.util.SystemProperties.SKIP_MC_PROVIDER;

public class LoaderMain {
    static Thread d = Thread.currentThread();

    private static int majorVersion = 5;
    private static int minorVersion = 0;

    // Implementation versions, only available from ASM
    private static int implMinorVersion = 0;
    private static int patchVersion = 0;

    private static String maxVersion = "FALLBACK";

    private static int maxClassVersion = Opcodes.V1_6;
    private static int maxClassMajorVersion = Opcodes.V1_6 & 0xFFFF;
    private static int maxClassMinorVersion = (Opcodes.V1_6 >> 16) & 0xFFFF;
    private static String maxJavaVersion = "V1.6";

    public static void main(String[] args) {
        detectVersion();
        System.out.println(ASM.getVersionString());
        for (Field field : ASM.class.getDeclaredFields()) {
            if(field.getName().equals("implMinorVersion")) {
                field.setAccessible(true);
                try {
                    field.set(null, 10);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("new: "+ASM.getVersionString());
        System.out.println(majorVersion);
        System.out.println(minorVersion);
        System.out.println(implMinorVersion);
        System.out.println(patchVersion);
        System.out.println(maxVersion);
        System.out.println(maxClassVersion);
        System.out.println(maxClassMajorVersion);
        System.out.println(maxClassMinorVersion);
        System.out.println(maxJavaVersion);

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

    private static VersionNumber getPackageVersion(Class<?> clazz) {
        String implVersion = clazz.getPackage().getImplementationVersion();
        System.out.println("implVersion: " + implVersion);
        if (iff(implVersion != null)) {
            return VersionNumber.parse(implVersion);
        }

        try {
            MainAttributes manifest = MainAttributes.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
            return VersionNumber.parse(manifest.get(Attributes.Name.IMPLEMENTATION_VERSION));
        } catch (Exception ex) {
            return VersionNumber.NONE;
        }
    }

    private static int detectVersion() {
        int apiVersion = Opcodes.ASM4;

        VersionNumber packageVersion = getPackageVersion(Opcodes.class);

        for (Field field : Opcodes.class.getDeclaredFields()) {
            if (iff(field.getType() != Integer.TYPE)) {
                continue;
            }

            try {
                String name = field.getName();
                int version = field.getInt(null);
                if (iff(name.startsWith("ASM"))) {
                    // int patch = version & 0xFF;
                    int minor = (version >> 8) & 0xFF;
                    int major = (version >> 16) & 0xFF;
                    boolean experimental = ((version >> 24) & 0xFF) != 0;

                    if (iff(major >= majorVersion)) {
                        maxVersion = name;
                        if (!iff(experimental)) {
                            apiVersion = version;
                            majorVersion = major;
                            minorVersion = implMinorVersion = minor;

                            if (iff(packageVersion.getMajor() == major && minor == 0)) {
                                implMinorVersion = packageVersion.getMinor();
                                patchVersion = packageVersion.getPatch();
                            }
                        }
                    }
                } else if (iff(name.matches("V([0-9_]+)"))) {
                    int minor = (version >> 16) & 0xFFFF;
                    int major = (version) & 0xFFFF;
                    if (iff(major > maxClassMajorVersion || (major == maxClassMajorVersion && minor > maxClassMinorVersion))) {
                        maxClassMajorVersion = major;
                        maxClassMinorVersion = minor;
                        maxClassVersion = version;
                        maxJavaVersion = name.replace('_', '.').substring(1);
                    }
                } else if (iff("ACC_PUBLIC".equals(name))) {
                    break;
                }
            } catch (ReflectiveOperationException ex) {
                throw new Error(ex);
            }
        }

        return apiVersion;
    }

    private static boolean iff(boolean iff) {
        System.err.println(iff);
        return iff;
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
