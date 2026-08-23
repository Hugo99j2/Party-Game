package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.*;
import com.daniel99j.djutil.GenericValuesHolder;
import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.PathfindDebugPos;
import com.google.common.io.PatternFilenameFilter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hugo99j.chaosparty.effect.EffectType;
import com.hugo99j.chaosparty.util.NoDebugOption;
import com.hugo99j.chaosparty.util.RequiresRefresh;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.entity.SpriteObject;
import com.daniel99j.dungeongame.level.LevelLight;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.*;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.hugo99j.chaosparty.ui.debugger.DebugOptions.newMapNames;
import static com.hugo99j.chaosparty.ui.debugger.ObjectEditor.*;
import static com.hugo99j.chaosparty.ui.debugger.UndoRedoHistory.onEdited;

public class Debuggers {
    private static Box2DDebugRenderer box2dDebugRenderer;
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static InputProcessor tmpProcessor;
    static final Map<String, ValueHolder<Boolean>> debugOptions = new LinkedHashMap<>();
    private static UUID selectedLightId = null;
    private static Vector2 oldLightPos;
    static String data = null;
    static String createObjectData = null;
    private static final ArrayList<String> logger = new ArrayList<>();
    public static final Map<UUID, ArrayList<PathfindDebugPos>> pathfindDebuggers = new HashMap<>();
    public static final Map<UUID, Integer> pathfindDebuggerTimers = new HashMap<>();
    public static Vector2 freecam = Vector2.Zero;
    private static int selectedSound = 0;
    private static final ArrayList<String> audioNames = new ArrayList<>();
    private static boolean forceShow = false;
    public static Map<Consumer<MatchView>, ValueHolder<Integer>> customLevelRenderers = new HashMap<>();
    public static Map<Runnable, ValueHolder<Integer>> customUiRenderers = new HashMap<>();
    public static boolean disableChangingColour = false;
    public static final ShaderProgram objectIdProgram;
    public static int advancedPickColour = 0;

    private static void option(String name, boolean defaultValue) {
        debugOptions.put(name, new ValueHolder<>(defaultValue));
    }

    private static void category(String name) {
        debugOptions.put("__"+name, new ValueHolder<>(false));
    }

    static {
        if (GameData.DEBUGGING) {
            category("World");
            option("noclipToggleable", false);
            option("noclip", false);
            option("tick", true);
            option("staticLightUpdates", false);
            option("disablePathfinding", false);
            option("invulnerable", false);

            category("Rendering");
            option("hitboxes", false);
            option("lights", true);
            option("wireframe", false);
            option("pixelPerfect", false);
            option("pathfindingRender", false);
            option("markers", false);
            option("showAdvancedObjectPicking", false);
            option("validPathfindingSpotRenderer", false);
            option("botDebug", true);
            option("renderDevObjects", true);
            option("showBetweenBoxes", true);
            option("objectNames", false);

            category("Minigame");
            option("forceSingleView", false);
            option("pauseTimers", false);

            category("Controllers");
            option("fakeControllers+1", false);
            option("fakeControllers+2", false);
            option("showControllerSelect", false);

            category("Map Editor");
            option("tickMapEditor", false);
            option("advancedObjectPicking", true);
            option("alignPixel", true);
            option("highlightSelected", true);

            category("UI");
            option("renderUiDebugger", false);

            category("Misc");
            option("showing", false);
            option("forceFocus", false);
            option("demoWindow", false);
            option("selectingLight", false);
            option("freecam", false);

            try {
                for (JsonElement options : GsonUtil.parse(Files.readString(Path.of("debug.json"))).get("options").getAsJsonArray()) {
                    JsonObject map = options.getAsJsonObject();
                    if(debugOptions.containsKey(map.get("name").getAsString())) {
                        debugOptions.put(map.get("name").getAsString(), new ValueHolder<>(map.get("value").getAsBoolean()));
                    }
                }
            } catch (Exception ignored) {

            }

            PathUtil.getFilesIn(PathUtil.asset("sounds/")).forEach(e -> audioNames.add(e.replace("assets/sounds/", "").replace(".mp3", "")));
            String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
            String fragmentShader =
                """
                    #ifdef GL_ES
                    #define LOWP lowp
                    precision mediump float;
                    #else
                    #define LOWP
                    #endif
                    varying LOWP vec4 v_color;
                    varying vec2 v_texCoords;
                    uniform sampler2D u_texture;
                    uniform vec2 u_resolution;
                    void main() {
                        float alpha = ceil(texture2D(u_texture, v_texCoords).a);
                        gl_FragColor = v_color * alpha;
                    }""";

            ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
            if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling object id shader: " + shader.getLog());
            objectIdProgram = shader;
        } else {
            objectIdProgram = null;
        }
    }

    public static void init() {
        if (GameData.DEBUGGING) {
            box2dDebugRenderer = new Box2DDebugRenderer();

            imGuiGlfw = new ImGuiImplGlfw();
            imGuiGl3 = new ImGuiImplGl3();
            long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
            ImGui.createContext();
            ImPlot.createContext();
            ImGuiIO io = ImGui.getIO();
            io.getFonts().addFontDefault();
            io.getFonts().build();
            io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
            imGuiGlfw.init(windowHandle, true);
            imGuiGl3.init("#version 150");

            for (String e : PathUtil.getFilesIn(PathUtil.data("maps"))) {
                newMapNames.add(e.replace("data/maps/", "").replace(".map", ""));
            }
        }
    }

    public static boolean isDebuggerOpen() {
        return GameData.DEBUGGING && isEnabled("showing");
    }

    public static void render() {
        if (!GameData.DEBUGGING) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.END)) pause();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SCROLL_LOCK)) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }

        customUiRenderers.keySet().forEach(Runnable::run);

        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, isEnabled("wireframe") ? GL30.GL_LINE : GL30.GL_FILL);

        //GameData.gameViewport.apply();

        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            debugOptions.get("showing").object = !isEnabled("showing");
            save();
        }

        boolean isInMapEditor = GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor;

        if (isDebuggerOpen() || isInMapEditor) {
            if (isEnabled("staticLightUpdates") && GameData.level != null) {
                for (LevelLight<?> light : GameData.level.getLights()) {
                    if (light.light().isStaticLight()) {
                        light.light().setStaticLight(false);
                        light.light().setStaticLight(true);
                    }
                }
            }

            imGuiGl3.newFrame();
            imGuiGlfw.newFrame();
            ImGui.newFrame();

            //START

            //on wayland you can't figure out which monitor... use primary instead.
            boolean isFullScreenOrMaximised = Gdx.graphics.isFullscreen() || (Lwjgl3ApplicationConfiguration.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).width == Gdx.graphics.getWidth() && Gdx.graphics.getHeight()+200 >= Lwjgl3ApplicationConfiguration.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).height);

            ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);

            if(!isFullScreenOrMaximised && !forceShow) {
                if(ImGui.begin("Are you sure?")) {
                    if (ImGui.button("Force show UI")) forceShow = true;
                }
                ImGui.end();
            } else {
                MenuBar.render();

                if(ImGui.begin("Logger")) {
                    for (String s : logger) {
                        if (s.startsWith("<error>")) {
                            ImGui.textColored(0xff0000ff, s.replace("<error>", ""));
                        } else ImGui.text(s);

                        if (!ImGui.isWindowHovered()) ImGui.setScrollY(10000);
                    }
                }
                ImGui.end();

                DebugOptions.render();

                if(ImGui.begin("Effects")) {
                    for (String allEffect : EffectType.getNameToEffect().keySet()) {
                        if (ImGui.button("Apply " + allEffect)) {
                            GameData.getCurrentMatch().getMatchViews().getFirst().addEffect(EffectType.getEffectType(allEffect), 15);
                        }
                    }
                }
                ImGui.end();

                if(isEnabled("demoWindow")) ImGui.showDemoWindow();

                UUID hoveredObject = null;

//                ImGui.begin("Lights");
//                UUID hoveredLight = renderLightSelector();
//                boolean showLights = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows);
//                ImGui.end();

                DebugController.render();

                if(ImGui.begin("Sounds")) {
                    if (ImGui.button("Play Sound")) {
                        try {
                            SoundManager.getSound(audioNames.get(selectedSound)).playSingle(1);
                        } catch (Exception e) {
                            Logger.error("Error playing sound", e);
                        }
                    }
                    ImGui.sameLine();
                    ImInt newSound = new ImInt(selectedSound);
                    if (ImGui.combo("Sound", newSound, audioNames.toArray(new String[0]))) {
                        selectedSound = newSound.get();
                    }

                    ImGui.separatorText("Active Sounds");
                    int j = 0;
                    for (SoundInstance activeSound : SoundManager.getActiveSounds()) {
                        if (ImGui.collapsingHeader("'" + activeSound.getName() + "' (" + j + ")")) {
                            ImGui.text("Time for " + j + ": " + activeSound.getCurrentTime() + "/" + activeSound.getDuration());
                            if (ImGui.button("Cancel " + j)) ToRun.run(activeSound::cancel);
                            if (ImGui.button("Pause " + j)) activeSound.pause();
                            if (ImGui.button("Play " + j)) activeSound.play();
                            slider("Pitch " + j, activeSound.getPitch(), activeSound::setPitch, 0, 2, "%.3f");
                            slider("Volume " + j, activeSound.getVolume(), activeSound::setVolume, 0, 1, "%.3f");
                            slider("Pan " + j, activeSound.getPan(), activeSound::setPan, -1, 1, "%.3f");
                        }
                        j++;
                    }
                }
                ImGui.end();

                renderObjectEditor();

                UiViewer.render();

                ControllerVibratorEditor.render();
                //DEBUGGERS

                // incase imgui changes the gameViewport
//                GameData.gameCamera.updateUtil();
//                GameData.gameViewport.apply();

                if (GameData.level != null) {
                    if (isEnabled("hitboxes")) {
                        customLevelRenderers.put((v) -> {
                            box2dDebugRenderer.render(GameData.level.getBox2dWorld(), v.gameCamera.combined);
                        }, new ValueHolder<>(1));
                    }

                    AbstractObject selectedObject;
                    if (hoveredObject != null && (selectedObject = GameData.level.getObjectByUUID(hoveredObject)) != null) {
                        RenderUtil.enableBlending();
                        //GameData.shapeRenderer.setProjectionMatrix(GameData.gameCamera.combined);
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        if (selectedObject.hasPhysics()) {
                            for (Fixture fixture : selectedObject.getPhysics().getFixtureList()) {
                                Vector4 hitbox = selectedObject.getHitboxWidthHeight(fixture);
                                GameData.shapeRenderer.setColor(0xdf / 255.0f, 0xf0 / 255.0f, 0x29 / 255.0f, 0.5f);
                                GameData.shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.z, hitbox.w);
                            }
                        } else if (selectedObject instanceof SpriteObject spriteObject) {
                            GameData.shapeRenderer.setColor(0xdf / 255.0f, 0xf0 / 255.0f, 0x29 / 255.0f, 0.5f);
                            GameData.shapeRenderer.rect(spriteObject.getPos().x, spriteObject.getPos().y, spriteObject.getWidth(), spriteObject.getHeight());
                        }
                        GameData.shapeRenderer.end();
                    }
                }
            }

            //END
            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            // If ImGui wants to capture the input, disable libGDX's input processor
            if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantCaptureMouse()) {
                if(tmpProcessor == null) {
                    tmpProcessor = Gdx.input.getInputProcessor();
                    Gdx.input.setInputProcessor(null);
                    ((InputPreventer) Gdx.input).setPrevent(true);
                }
            } else if (tmpProcessor != null) {
                Gdx.input.setInputProcessor(tmpProcessor);
                tmpProcessor = null;
                ((InputPreventer) Gdx.input).setPrevent(false);
            }

            if (ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow) || ImGui.isWindowFocused(ImGuiFocusedFlags.AnyWindow) || (GameData.getCurrentMinigame() instanceof MapEditor)) {
                ImGui.getStyle().setAlpha(1.0f);
            } else {
                ImGui.getStyle().setAlpha(0.2f);
            }
        }

        List<Runnable> customRenderersToRemove = new ArrayList<>();
        Debuggers.customUiRenderers.forEach((r, v) -> {
            v.object = v.object - 1;
            if (v.object <= 0) customRenderersToRemove.add(r);
        });
        for (Runnable runnable : customRenderersToRemove) {
            Debuggers.customUiRenderers.remove(runnable);
        }
    }

    protected static void saveMap() {
        try {
            Files.write(Path.of(PathUtil.codingDir(PathUtil.data("maps/" + GameData.getCurrentMatch().getCurrentMinigame().getMapName() + ".map"))), LevelLoader.saveLevel(GameData.level, true).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Logger.info("Saved map");
            PathUtil.clearCache();
        } catch (Exception e) {
            Logger.error("Failed to save map", e);
        }
    }

    static void save() {
        JsonObject data = new JsonObject();
        JsonArray array = new JsonArray();
        debugOptions.forEach((name, value) -> {
            JsonObject map = new JsonObject();
            map.addProperty("name", name);
            map.addProperty("value", value.object);
            array.add(map);
        });
        data.add("options", array);
        try {
            Files.writeString(Path.of("debug.json"), new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(data));
        } catch (Exception ignored) {}
    }

    private static void intInput(String name, int getter, Consumer<Integer> setter) {
        ImInt check = new ImInt(getter);
        if (ImGui.inputInt(name, check)) {
            setter.accept(check.get());
        }
    }

    static void slider(String name, float getter, Consumer<Float> setter, float min, float max, String format) {
        if (Float.isFinite(min) && Float.isFinite(max)) {
            float[] check = {getter};
            if (ImGui.sliderFloat(name, check, min, max, format)) {
                setter.accept(check[0]);
            }
        } else {
            ImGui.text("Invalid bounds for slider "+name);
        }
    }

    static AbstractObject getHoveredObject() {
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        Vector3 screenCoords = new Vector3(mouseX, mouseY, 0);
        if(GameData.getCurrentMatch() == null) return null;
        Vector3 worldCoords = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.unproject(screenCoords);

        Vector2 point = new Vector2(worldCoords.x, worldCoords.y);
        float range = 0.001f;

        ValueHolder<AbstractObject> out = new ValueHolder<>(null);

        QueryCallback callback = fixture -> {
            if (fixture.getBody().getUserData() instanceof AbstractObject object) out.object = object;
            return true;
        };

        GameData.level.getBox2dWorld().QueryAABB(callback, point.x - range, point.y - range, point.x + range, point.y + range);

        return out.object;
    }

    private static LevelLight getHoveredLight() {
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        Vector3 screenCoords = new Vector3(mouseX, mouseY, 0);
        if(GameData.getCurrentMatch() == null) return null;
        Vector3 worldCoords = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.unproject(screenCoords);

        Vector2 point = new Vector2(worldCoords.x, worldCoords.y);
        float range = 0.3f;

        ValueHolder<LevelLight> out = new ValueHolder<>(null);

        GameData.level.getLights().forEach(light -> {
            if (light.light().getPosition().cpy().sub(point).len() <= range) out.object = light;
        });

        return out.object;
    }

    public static void dispose() {
        if (GameData.DEBUGGING) {
            imGuiGl3.shutdown();
            imGuiGl3 = null;
            imGuiGlfw.shutdown();
            imGuiGlfw = null;
            ImGui.destroyContext();
            ImPlot.destroyContext();
        }
    }

    public static void log(String s) {
        if (!GameData.DEBUGGING) return;
        Debuggers.logger.add(s);
    }

    public static void pause() {
        long t = System.currentTimeMillis();
        t += 1; //ADD HERE
        if (t >= System.currentTimeMillis() - 10) {
            Logger.error("Make sure to add a breakpoint here!");
        }
    }

    public static boolean isEnabled(String option) {
        return debugOptions.get(option).object;
    }


    static void addVariables(AbstractObject selectedObject, Class<?> clazz, Consumer<GenericValuesHolder<Field, AbstractObject, Object, ?, ?>> setter) {
        if(selectedObject instanceof LightEditor.SelectedLightObject selectedLight) {
            LightEditor.render(selectedLight);
            return;
        }
        if(selectedObject instanceof SelectionGroup group) {
            group.selected.ensureSafety();
            boolean allMatch = true;
            for (AbstractObject abstractObject : group.selected) {
                if(abstractObject.getClass() != group.selected.get(0).getClass()) {
                    allMatch = false;
                    break;
                }
            }
            if(allMatch) {
                addVariables(group.selected.get(0), group.selected.get(0).getClass(), (holder) -> {
                    for (AbstractObject abstractObject : group.selected) {
                        try {
                            holder.a().set(abstractObject, holder.c());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return;
            }
        }
        for (Field declaredField : clazz.getDeclaredFields()) {
            if(declaredField.getAnnotation(NoDebugOption.class) != null || Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers())) continue;
            declaredField.setAccessible(true);
            if(declaredField.getAnnotation(RequiresRefresh.class) != null) {
                ImGui.textColored(255, 0, 0, 255, "R");
                ImGui.setItemTooltip("Requires refresh");
                ImGui.sameLine();
            }
            if(declaredField.getAnnotation(NonEditable.class) != null) {
                ImGui.beginDisabled();
            }
            try {
                addVariable(declaredField.getName(), declaredField.getType(), declaredField.get(selectedObject), (t) -> {
                    try {
                        setter.accept(new GenericValuesHolder<>(declaredField, selectedObject, t));
                    } catch (Exception e) {
                        Logger.error("Error setting variable", e);
                    }
                });
            } catch (Exception e) {
                Logger.error("Error adding variable", e);
                ImGui.text("Error adding variable: " + e.getMessage());
            }
            if(declaredField.getAnnotation(NonEditable.class) != null) {
                ImGui.endDisabled();
            }
        }

        if(clazz.getSuperclass() != null) addVariables(selectedObject, clazz.getSuperclass(), setter);
    }

    private static <T> void addVariable(String name, Class<?> clazz, T current, Consumer<T> setter) {
        //noinspection unchecked
        addVariable2(name, (Class<T>) clazz, current, setter);
    }

    @SuppressWarnings("unchecked")
    private static <T> void addVariable2(String name, Class<T> clazz, T current, Consumer<T> setter) {
        if(current == null) {
            ImGui.text(name + " (null)");
            return;
        }
        Class<?> type = MiscUtils.getClassNonPrimitive(clazz);
        if(type.equals(Integer.class)) {
            ImInt check = new ImInt((Integer) current);
            if(ImGui.inputInt(name, check)) {
                setter.accept((T) (Object) check.get());
                onEdited();
            }
        } else if(type.equals(Float.class)) {
            ImFloat check = new ImFloat((Float) current);
            if(ImGui.inputFloat(name, check)) {
                setter.accept((T) (Object) check.get());
                onEdited();
            }
        } else if(type.equals(Double.class)) {
            ImDouble check = new ImDouble((Double) current);
            if(ImGui.inputDouble(name, check)) {
                setter.accept((T) (Object) check.get());
                onEdited();
            }
        } else if(type.equals(String.class)) {
            ImString check = new ImString((String) current, ((String) current).length()+10000);
            if(ImGui.inputText(name, check, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept((T) check.get());
                onEdited();
            }
        } else if(type.equals(UUID.class)) {
            ImString check = new ImString(current.toString());
            if(ImGui.inputText(name, check, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept((T) UUID.fromString(check.get()));
                onEdited();
            }
        } else if(type.equals(Boolean.class)) {
            if(ImGui.checkbox(name, (Boolean) current)) {
                setter.accept((T) ((Boolean) !((Boolean) current)));
                onEdited();
            }
        } else if(type.equals(Vector2.class)) {
            float[] check = {((Vector2) current).x, ((Vector2) current).y};
            if(ImGui.inputFloat2(name, check)) {
                setter.accept((T) new Vector2(check[0], check[1]));
                onEdited();
            }
        } else if(type.equals(Color.class)) {
            float[] check = {((Color) current).r, ((Color) current).g, ((Color) current).b, ((Color) current).a};
            if(ImGui.colorEdit4(name, check)) {
                setter.accept((T) new Color(check[0], check[1], check[2], check[3]));
            }
        } else if(type.equals(Map.class)) {
            int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg;
            if(ImGui.collapsingHeader(name)) {
                ImGui.indent();
                if (ImGui.beginTable(name, 1, flags)) {
                    ((Map<?, ?>) current).forEach((key, value) -> {
                        ImGui.tableNextRow();
                        ImGui.tableNextColumn();
                        addVariable(key.toString(), value.getClass(), value, (v) -> {
                            setMapValue(current, key, v);
                        });
                    });
                    ImGui.endTable();
                }
                ImGui.unindent();
            }
        } else {
            ImGui.text("Unsupported type: " + clazz + " (name: " + name + "value: " + current + ")");
        }
    }

    private static <A, B> void setMapValue(Object current, A key, B v) {
        //noinspection unchecked
        setMapValue((HashMap<A, B>) current, key, v);
    }

    private static <A, B> void setMapValue(HashMap<A, B> current, A key, B v) {
        current.put(key, v);
    }

    public static String devName(AbstractObject o) {
        if(o == null) return "None";
        return devName(o.getEntityId());
    }

    public static String devName(Object o) {
        return devName(o.hashCode());
    }

    private static final String[] parts = {
        "Bee",
        "Fart",
        "Explode",
        "Rumbler",
        "Gobbler",
        "Troll",
        "Excellent",
        "Archer",
        "Snot",
        "Milo",
        "Yuzu",
        "Feast",
        "Bread",
        "Parts",
        "Robot",
        "Metal",
        "Plastic",
        "Glass",
        "Plant",
        "Gold",
        "Shouter",
        "Treat",
        "Elf",
        "Santa",
        "Greed",
        "Balanced",
        "Heavy",
        "Light",
        "Interesting",
        "Bored",
        "Broken",
        "Panda",
        "Cow",
        "Sheep",
        "Pig"
    };
    public static String devName(int hash) {
        Random random = new Random(hash);
        return parts[random.nextInt(parts.length-1)] + parts[random.nextInt(parts.length-1)];
    }
}
