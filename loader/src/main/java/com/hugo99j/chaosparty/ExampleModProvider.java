package com.hugo99j.chaosparty;

import com.hugo99j.chaosparty.util.PathUtil;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.Arguments;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * An example implementation of {@link GameProvider}.
 * <p>
 * It is important that this class is listed as a service, see {@link ServiceLoader}
 */
public final class ExampleModProvider implements GameProvider {
    private final GameTransformer transformer = new GameTransformer();

    /**
     * The class path for this mod.
     */
    private List<Path> classPath;

    /**
     * Fabric uses a wrapped version of the program arguments so it can take CLI args and strip them before it reaches
     * the target.
     */
    private Arguments arguments;

    /**
     * The entry point of the target, in this case it's "com.example.base.Launcher".
     */
    private String entryClass;

    /**
     * The version of the target.
     */
    private String version;

    @Override
    public String getGameId() {
        return "example";
    }

    @Override
    public String getGameName() {
        return "Example ModProvider project";
    }

    @Override
    public String getRawGameVersion() {
        return version;
    }

    @Override
    public String getNormalizedGameVersion() {
        return version;
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        // Fabric Loader takes care of the Java built-in mod, we only have to worry about the target.
        return List.of(
            new BuiltinMod(classPath, new ExampleMetadata(getNormalizedGameVersion()))
        );
    }

    @Override
    public String getEntrypoint() {
        return entryClass;
    }

    @Override
    public Path getLaunchDirectory() {
        try {
            return Paths.get(".").toRealPath();
        } catch(IOException e) {
            throw new RuntimeException("Failed to resolve launch dir", e);
        }
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public Set<BuiltinTransform> getBuiltinTransforms(String className) {
        return Set.of();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(FabricLauncher fabricLauncher, String[] args) {
        this.arguments = new Arguments();
        arguments.parse(args);

        entryClass = "com.hugo99j.chaosparty.Launcher";
        version = "1.0.0";

        Path codePath;
        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            //No jar executable in dev env, so use fatJar
            codePath = Path.of("../build/libs/PATH-1.0-SNAPSHOT-all.jar".replace("PATH", PathUtil.getDevPrefix())).toAbsolutePath();
        } else {
            //If not in dev env, this is the JAR file
            var codeSource = ExampleModProvider.class.getProtectionDomain().getCodeSource();
            try {
                codePath = Paths.get(codeSource.getLocation().toURI());
            } catch(URISyntaxException e) {
                throw new RuntimeException("Failed to find source of ExampleModProvider?", e);
            }
        }

        classPath = List.of(codePath);
        return true;
    }

    @Override
    public void initialize(FabricLauncher launcher) {
        var parentClassPath = Stream.of(System.getProperty("java.class.path").split(File.pathSeparator))
            .map(Path::of)
            .map((path) -> {
                try {
                    return path.toRealPath();
                } catch(IOException e) {
                    throw new RuntimeException("Failed to get real path of " + path, e);
                }
            })
            .filter((path) -> !classPath.contains(path))
            .toList();

        launcher.setValidParentClassPath(parentClassPath);

        transformer.locateEntrypoints(launcher, classPath);
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return transformer;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        classPath.forEach(launcher::addToClassPath);
    }

    @Override
    public void launch(ClassLoader loader) {
        // This is where you need to do a touch of reflection to load the target. You can't directly reference classes
        // otherwise they won't be able to be transformed and will be loaded under the wrong ClassLoader.
        var targetName = getEntrypoint();

        MethodHandle invoker;
        try {
            Class<?> target = loader.loadClass(targetName);
            invoker = MethodHandles.lookup().findStatic(target, "notMain", MethodType.methodType(void.class, String[].class));
        } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to find entry point", e);
        }

        try {
            // Idea doesn't understand that this is a polymorphic method.
            //noinspection ConfusingArgumentToVarargsMethod
            invoker.invokeExact(arguments.toArray());
        } catch(Throwable e) {
            throw new RuntimeException("Failed to launch", e);
        }
    }

    @Override
    public Arguments getArguments() {
        return arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        // If there are sensitive arguments (user tokens for example) you should strip them here when sanitize is true.
        return arguments.toArray();
    }
}
