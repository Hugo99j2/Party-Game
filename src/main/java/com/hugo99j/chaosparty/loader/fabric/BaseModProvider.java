package com.hugo99j.chaosparty.loader.fabric;

import com.hugo99j.chaosparty.util.PathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import net.fabricmc.loader.impl.util.Arguments;

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
public final class BaseModProvider implements GameProvider {
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
        return "chaosparty";
    }

    @Override
    public String getGameName() {
        return "Chaos Party";
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
            //new BuiltinMod(classPath, new ExampleMetadata(getNormalizedGameVersion())),
            new BuiltinMod(classPath, new LoaderModMetadata() {
                @Override
                public String getType() {
                    return "1";
                }

                @Override
                public String getId() {
                    return "chaosparty";
                }

                @Override
                public Collection<String> getProvides() {
                    return List.of();
                }

                @Override
                public Version getVersion() {
                    try {
                        return SemanticVersion.parse("1.0.0");
                    } catch (VersionParsingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public ModEnvironment getEnvironment() {
                    return ModEnvironment.UNIVERSAL;
                }

                @Override
                public Collection<ModDependency> getDependencies() {
                    return List.of();
                }

                @Override
                public String getName() {
                    return "Chaos Party";
                }

                @Override
                public String getDescription() {
                    return "";
                }

                @Override
                public Collection<Person> getAuthors() {
                    return List.of();
                }

                @Override
                public Collection<Person> getContributors() {
                    return List.of();
                }

                @Override
                public ContactInformation getContact() {
                    return null;
                }

                @Override
                public Collection<String> getLicense() {
                    return List.of();
                }

                @Override
                public Optional<String> getIconPath(int size) {
                    return Optional.empty();
                }

                @Override
                public boolean containsCustomValue(String key) {
                    return false;
                }

                @Override
                public CustomValue getCustomValue(String key) {
                    return null;
                }

                @Override
                public Map<String, CustomValue> getCustomValues() {
                    return Map.of();
                }

                @Override
                public boolean containsCustomElement(String key) {
                    return false;
                }

                @Override
                public int getSchemaVersion() {
                    return 1;
                }

                @Override
                public Map<String, String> getLanguageAdapterDefinitions() {
                    return Map.of();
                }

                @Override
                public Collection<NestedJarEntry> getJars() {
                    return List.of();
                }

                @Override
                public Collection<String> getMixinConfigs(EnvType type) {
                    return List.of("game.mixins.json");
                }

                @Override
                public String getClassTweaker() {
                    return "";
                }

                @Override
                public boolean loadsInEnvironment(EnvType type) {
                    return true;
                }

                @Override
                public Collection<String> getOldInitializers() {
                    return List.of();
                }

                @Override
                public List<EntrypointMetadata> getEntrypoints(String type) {
                    return List.of();
                }

                @Override
                public Collection<String> getEntrypointKeys() {
                    return List.of();
                }

                @Override
                public void emitFormatWarnings() {

                }

                @Override
                public void setVersion(Version version) {

                }

                @Override
                public void setDependencies(Collection<ModDependency> dependencies) {

                }
            })
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

        classPath = new ArrayList<>();
        //If not in dev env, this is the JAR file.
        //In dev env, this is gradle compiled classes
        var codeSource = BaseModProvider.class.getProtectionDomain().getCodeSource();
        try {
            classPath.add(Paths.get(codeSource.getLocation().toURI()));
        } catch(URISyntaxException e) {
            throw new RuntimeException("Failed to find source of ExampleModProvider?", e);
        }

        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            //Add external dependencies so mixins can apply
            classPath.add(Path.of("../build/libs/PATH-1.0-SNAPSHOT.jar".replace("PATH", PathUtil.getDevPrefix())).toAbsolutePath());
        }
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
