package com.hugo99j.chaosparty.ui.debugger;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.*;
import com.daniel99j.djutil.GenericValuesHolder;
import com.daniel99j.djutil.MiscUtils;
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
import com.hugo99j.chaosparty.ui.screenss.ScreenSS;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.entity.SpriteObject;
import com.daniel99j.dungeongame.level.LevelLight;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.daniel99j.dungeongame.level.SaveConfig;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.*;
import imgui.*;
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
            option("showBetweenBoxes", true);
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
            option("screenSSDebugger", false);
            option("ignoreInvalidSS", false);

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


            if (tmpProcessor != null) { // Restore the input processor after ImGui caught all inputs, see #end()
                Gdx.input.setInputProcessor(tmpProcessor);
                tmpProcessor = null;
            }

            imGuiGl3.newFrame();
            imGuiGlfw.newFrame();
            ImGui.newFrame();

            //START

            //on wayland you can't figure out which monitor... use primary instead.
            boolean isFullScreenOrMaximised = Gdx.graphics.isFullscreen() || (Lwjgl3ApplicationConfiguration.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).width == Gdx.graphics.getWidth() && Gdx.graphics.getHeight()+200 >= Lwjgl3ApplicationConfiguration.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).height);

            ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);

            if(!isFullScreenOrMaximised && !forceShow) {
                ImGui.begin("Are you sure?");
                if(ImGui.button("Force show UI")) forceShow = true;
                ImGui.end();
            } else {
                ImGui.beginMainMenuBar();
                if(isInMapEditor) {
                    if(ImGui.beginMenu("File")) {
                        if(ImGui.menuItem("Open... CTRL+O")) {
                            FileDialog dialog = new FileDialog((Frame) null, "Select map to open", FileDialog.LOAD);
                            dialog.setFilenameFilter(new PatternFilenameFilter(".*\\.map$"));
                            dialog.setVisible(true);

                            String directory = dialog.getDirectory();
                            String file = dialog.getFile();

                            //selected a file
                            if (file != null) {
                                Logger.info("Loaded "+directory+file);
                                GameData.startMatch(List.of(new MatchPlayer(User.getUser(5)))).setCurrentMinigame(new MapEditor(directory+file));
                            }

                            dialog.dispose();

                        }
                        if(ImGui.beginMenu("Open map...")) {
                            for (String maps : PathUtil.getFilesIn(PathUtil.data("maps"))) {
                                String real = maps.replace(".map", "").replace("data/maps/", "");
                                if(ImGui.menuItem(real)) {
                                    Logger.info("Loaded "+real);
                                    GameData.startMatch(List.of(new MatchPlayer(User.getUser(5)))).setCurrentMinigame(new MapEditor(real));
                                }
                            }
                            ImGui.endMenu();
                        }
                        if(ImGui.menuItem("Save   CTRL+S")) {
                            saveMap();
                        }
                        ImGui.endMenu();
                    }
                } else {
                    if(ImGui.menuItem("Game")) {
                        if(ImGui.menuItem("Quick-Load")) {

                        }
                    }
                }
                ImGui.endMainMenuBar();

                if (Gdx.input.isKeyJustPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    saveMap();
                }
                UndoRedoHistory.render();

                ImGui.begin("Logger");
                for (String s : logger) {
                    if (s.startsWith("<error>")) {
                        ImGui.textColored(0xff0000ff, s.replace("<error>", ""));
                    } else ImGui.text(s);

                    if (!ImGui.isWindowHovered()) ImGui.setScrollY(10000);
                }
                ImGui.end();

                DebugOptions.render();

                ImGui.begin("Effects");
                for (String allEffect : EffectType.getNameToEffect().keySet()) {
                    if(ImGui.button("Apply "+allEffect)) {
                        GameData.getCurrentMatch().getMatchViews().getFirst().addEffect(EffectType.getEffectType(allEffect), 15);
                    }
                }
                ImGui.end();

                if(isEnabled("demoWindow")) ImGui.showDemoWindow();

                UUID hoveredObject = null;

                ImGui.begin("Lights");
                UUID hoveredLight = renderLightSelector();
                boolean showLights = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows);
                ImGui.end();

                DebugController.render();

                ImGui.begin("Sounds");
                if (ImGui.button("Play Sound")) {
                    try {
                        SoundManager.getSound(audioNames.get(selectedSound)).playSingle(1);
                    } catch (Exception e) {
                        Logger.error("Error playing sound", e);
                    }
                }
                ImGui.sameLine();
                ImInt newSound = new ImInt(selectedSound);
                if(ImGui.combo("Sound", newSound, audioNames.toArray(new String[0]))) {
                    selectedSound = newSound.get();
                }

                ImGui.separatorText("Active Sounds");
                int j = 0;
                for (SoundInstance activeSound : SoundManager.getActiveSounds()) {
                    if(ImGui.collapsingHeader("'"+activeSound.getName()+"' ("+j+")")) {
                        ImGui.text("Time for "+j+": "+activeSound.getCurrentTime()+"/"+activeSound.getDuration());
                        if(ImGui.button("Cancel "+j)) ToRun.run(activeSound::cancel);
                        if(ImGui.button("Pause "+j)) activeSound.pause();
                        if(ImGui.button("Play "+j)) activeSound.play();
                        slider("Pitch "+j, activeSound.getPitch(), activeSound::setPitch, 0, 2, "%.3f");
                        slider("Volume "+j, activeSound.getVolume(), activeSound::setVolume, 0, 1, "%.3f");
                        slider("Pan "+j, activeSound.getPan(), activeSound::setPan, -1, 1, "%.3f");
                    }
                    j++;
                }
                ImGui.end();

                renderObjectEditor();

                UiViewer.render();
                //DEBUGGERS

                // incase imgui changes the gameViewport
//                GameData.gameCamera.update();
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

                    if (showLights) for (LevelLight<?> light : GameData.level.getLights()) {
                        Color c = light.light().getColor().cpy();
                        if (light.uuid().equals(hoveredLight)) c = Color.YELLOW;
                        //GameData.shapeRenderer.setProjectionMatrix(GameData.gameCamera.combined);
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(c);
                        GameData.shapeRenderer.circle(light.light().getPosition().x, light.light().getPosition().y, 0.2f, 20);
                        GameData.shapeRenderer.end();
                    }
                }
            }

            //END
            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            // If ImGui wants to capture the input, disable libGDX's input processor
            if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantCaptureMouse()) {
                tmpProcessor = Gdx.input.getInputProcessor();
                Gdx.input.setInputProcessor(null);
            }
            //END

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

    private static void saveMap() {
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

    private static UUID renderLightSelector() {
        if(GameData.level == null) return null;
        UUID hoveredLight = null;
        if (ImGui.button("Add point light")) {
            assert GameData.level != null;
            GameData.level.addLight((rayHandler -> new PointLight(rayHandler, 128)), SaveConfig.ALWAYS);
        }

        ImGui.sameLine();

        if (ImGui.button("Add cone light")) {
            assert GameData.level != null;
            selectedLightId = GameData.level.addLight((rayHandler -> new ConeLight(rayHandler, 128, Color.RED, 5, 0, 0, 0, 45)), SaveConfig.ALWAYS).uuid();
        }

        ImGui.sameLine();

        if (ImGui.button("Add directional light")) {
            assert GameData.level != null;
            selectedLightId = GameData.level.addLight((rayHandler -> new DirectionalLight(rayHandler, 128, Color.RED, 30)), SaveConfig.ALWAYS).uuid();
        }

        ImGui.sameLine();

        ImVec4 oldColour = ImGui.getStyle().getColor(ImGuiCol.Button);
        ImVec4 selectedColour = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);
        if (isEnabled("selectingLight"))
            ImGui.getStyle().setColor(ImGuiCol.Button, selectedColour.x, selectedColour.y, selectedColour.z, selectedColour.w);
        if (ImGui.button("Pick Light")) {
            debugOptions.get("selectingLight").object = !isEnabled("selectingLight");
        } else {
            if (debugOptions.get("selectingLight").object) {
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                    selectedLightId = getHoveredLight() == null ? null : getHoveredLight().uuid();
                    debugOptions.get("selectingLight").object = false;
                } else {
                    hoveredLight = getHoveredLight() == null ? null : getHoveredLight().uuid();
                }
            }
        }
        ImGui.getStyle().setColor(ImGuiCol.Button, oldColour.x, oldColour.y, oldColour.z, oldColour.w);

        ImGui.beginChild("Light Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
        ImGui.separatorText("All Lights");

        if (ImGui.beginTable("Light Selector", 1, ImGuiTableFlags.RowBg)) {
            int id = 0;
            for (LevelLight<?> light : GameData.getLevelOrThrow().getLights()) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.pushID(id);
                int flags = ImGuiSelectableFlags.SpanAllColumns;
                boolean selected = light.uuid().equals(selectedLightId);
                if (selected)
                    flags |= ImGuiTreeNodeFlags.Selected;
                if (ImGui.selectable(light.toString() + " (" + id + ")", selected, flags))
                    selectedLightId = light.uuid();
                if (ImGui.isItemHovered()) hoveredLight = light.uuid();
                ImGui.popID();

                id++;
            }
            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("Light Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);

        ImGui.separatorText("Current Light");

        LevelLight<?> selectedLight;
        if (selectedLightId != null && (selectedLight = GameData.level.getLights().stream().filter((o) -> o.uuid().equals(selectedLightId)).findFirst().orElse(null)) != null) {
            Vector2 middle = oldLightPos == null ? selectedLight.light().getPosition().cpy() : oldLightPos;
            int posOffset = 10;

            boolean changing = false;
            slider("X Pos", selectedLight.light().getPosition().x, (x) -> selectedLight.light().setPosition(x, selectedLight.light().getY()), middle.x - posOffset, middle.x + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
            if (ImGui.isItemActive()) changing = true;
            slider("Y Pos", selectedLight.light().getPosition().y, (y) -> selectedLight.light().setPosition(selectedLight.light().getX(), y), middle.y - posOffset, middle.y + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
            if (ImGui.isItemActive()) changing = true;

//            if (ImGui.button("TP to player")) selectedLight.light().setPosition(GameData.player.getPos());
//            ImGui.sameLine();
//            if (ImGui.button("TP player to this")) GameData.player.setPos(selectedLight.light().getPosition());

            if (oldLightPos == null && changing) {
                oldLightPos = selectedLight.light().getPosition().cpy();
            }
            if (oldLightPos != null && !changing) {
                oldLightPos = null;
            }

            float[] colours = {
                selectedLight.light().getColor().r,
                selectedLight.light().getColor().g,
                selectedLight.light().getColor().b,
                selectedLight.light().getColor().a
            };
            if (ImGui.colorPicker4("Colour", colours)) {
                selectedLight.light().setColor(colours[0], colours[1], colours[2], colours[3]);
            }

            if (ImGui.checkbox("X-Ray", selectedLight.light().isXray())) {
                selectedLight.light().setXray(!selectedLight.light().isXray());
            }

            if (ImGui.checkbox("Static", selectedLight.light().isStaticLight())) {
                selectedLight.light().setStaticLight(!selectedLight.light().isStaticLight());
            }

            if (ImGui.checkbox("Soft", selectedLight.light().isSoft())) {
                selectedLight.light().setSoft(!selectedLight.light().isSoft());
            }

            if (ImGui.checkbox("Active", selectedLight.light().isActive())) {
                selectedLight.light().setActive(!selectedLight.light().isActive());
            }

            slider("Softness", selectedLight.light().getSoftShadowLength(), selectedLight.light()::setSoftnessLength, 0, 5, "%.3f");

            slider("Distance", selectedLight.light().getDistance(), selectedLight.light()::setDistance, 0, 100, "%.3f");

            if (selectedLight.light() instanceof ConeLight coneLight) {
                ImGui.separatorText("Cone Light");

                slider("Direction", selectedLight.light().getDirection(), selectedLight.light()::setDirection, 0, 360, "%.0f");

                slider("Cone size", coneLight.getConeDegree(), coneLight::setConeDegree, 0, 180, "%.3f");
            }

            if (selectedLight.light() instanceof DirectionalLight) {
                ImGui.separatorText("Directional Light");

                slider("Direction", selectedLight.light().getDirection(), selectedLight.light()::setDirection, 0, 360, "%.0f");
            }

            if (ImGui.button("Delete")) {
                GameData.level.removeLight(selectedLight);
            }
        }

        ImGui.endChild();

        return hoveredLight;
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
}
