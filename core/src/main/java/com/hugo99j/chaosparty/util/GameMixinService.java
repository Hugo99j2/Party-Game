package com.hugo99j.chaosparty.util;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public class GameMixinService implements IMixinService {
    private final ReEntranceLock lock = new ReEntranceLock(2);

    @Override
    public String getName() {
        return "Game mixin service";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void prepare() {

    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.DEFAULT;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) {
            try {
                factory.createTransformer();
            } catch (MixinInitialisationError e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void beginPhase() {

    }

    @Override
    public void checkEnv(Object bootSource) {

    }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }

    @Override
    public IClassProvider getClassProvider() {
        return new BasicClassProvider();
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return new BasicBytecodeProvider();
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return new BasicTransformerProvider();
    }

    @Override
    public IClassTracker getClassTracker() {
        return new BasicClassTracker();
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public IFeatureValidator getFeatureValidator() {
        return null;
    }

    @Override
    public IAdviceProvider getAdviceProvider() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("com.hugo99j.chaosparty.util.GamePlatformAgent");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleVirtual(getName());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return List.of();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    @Override
    public String getSideName() {
        return "CLIENT";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_17;
    }

    @Override
    public ILogger getLogger(String name) {
        return new SimpleLogger(name);
    }

    class BasicClassTracker implements IClassTracker {

        @Override
        public void registerInvalidClass(String className) {}

        @Override
        public boolean isClassLoaded(String className) {
            try {
                Class.forName(className, false, getClass().getClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        @Override
        public String getClassRestrictions(String className) {
            return "";
        }
    }

    class BasicClassProvider implements IClassProvider {

        @Override
        public URL[] getClassPath() {
            return new URL[0];
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return Class.forName(name);
        }

        @Override
        public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
            return Class.forName(name, initialize, getClass().getClassLoader());
        }

        @Override
        public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
            throw new ClassNotFoundException("Agent classes not supported");
        }
    }

    class BasicBytecodeProvider implements IClassBytecodeProvider {

        @Override
        public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
            return getClassNode(name, true, 0);
        }

        @Override
        public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
            return getClassNode(name, runTransformers, 0);
        }

        @Override
        public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags)
            throws ClassNotFoundException, IOException {

            String path = name.replace('.', '/') + ".class";

            try (InputStream in = ClassLoader.getSystemResourceAsStream(path)) {
                if (in == null) {
                    throw new ClassNotFoundException("Missing bytecode: " + name);
                }

                byte[] bytes = in.readAllBytes();

                org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
                ClassNode node = new ClassNode();
                reader.accept(node, readerFlags);
                return node;
            }
        }
    }
    class BasicTransformerProvider implements ITransformerProvider {

        @Override
        public Collection<ITransformer> getTransformers() {
            return List.of();
        }

        @Override
        public Collection<ITransformer> getDelegatedTransformers() {
            return List.of();
        }

        @Override
        public void addTransformerExclusion(String name) {}
    }

    class SimpleLogger implements ILogger {

        private final String name;

        SimpleLogger(String name) {
            this.name = name;
        }

        private String format(String message, Object... params) {
            for (Object param : params) {
                int idx = message.indexOf("{}");
                if (idx == -1) break;
                message = message.substring(0, idx) + param + message.substring(idx + 2);
            }
            return message;
        }

        @Override
        public void info(String message, Object... params) {
            System.out.println("[INFO][" + name + "] " + format(message, params));
        }

        @Override
        public void info(String message, Throwable t) {
            System.out.println("[INFO][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override
        public void warn(String message, Object... params) {
            System.out.println("[WARN][" + name + "] " + format(message, params));
        }

        @Override
        public void warn(String message, Throwable t) {
            System.out.println("[WARN][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override
        public void error(String message, Object... params) {
            System.err.println("[ERROR][" + name + "] " + format(message, params));
        }

        @Override
        public void error(String message, Throwable t) {
            System.err.println("[ERROR][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override public String getId() { return name; }
        @Override public String getType() { return "LIBGDX"; }

        @Override public void log(Level level, String message, Object... params) {
            System.out.println("[" + level + "][" + name + "] " + format(message, params));
        }

        @Override public void log(Level level, String message, Throwable t) {
            System.out.println("[" + level + "][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override public <T extends Throwable> T throwing(T t) {
            t.printStackTrace();
            return t;
        }

        @Override public void trace(String message, Object... params) {}
        @Override public void trace(String message, Throwable t) {}
        @Override public void debug(String message, Object... params) {
            System.out.println("[DEBUG][" + name + "] " + format(message, params));
        }
        @Override public void debug(String message, Throwable t) {
            System.out.println("[DEBUG][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override public void fatal(String message, Object... params) {
            System.err.println("[FATAL][" + name + "] " + format(message, params));
        }

        @Override public void fatal(String message, Throwable t) {
            System.err.println("[FATAL][" + name + "] " + message);
            t.printStackTrace();
        }

        @Override public void catching(Level level, Throwable t) { t.printStackTrace(); }
        @Override public void catching(Throwable t) { t.printStackTrace(); }
    }
}
