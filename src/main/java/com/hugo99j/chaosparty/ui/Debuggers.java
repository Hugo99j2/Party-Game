package com.hugo99j.chaosparty.ui;

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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.*;
import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.PathfindDebugPos;
import com.daniel99j.djutil.pathfinder.PathfindDebugType;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hugo99j.chaosparty.effect.ActiveEffect;
import com.hugo99j.chaosparty.effect.Effects;
import com.hugo99j.chaosparty.util.NoDebugOption;
import com.hugo99j.chaosparty.util.RequiresRefresh;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSS;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.entity.TilesetObject;
import com.daniel99j.dungeongame.level.LevelLight;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.daniel99j.dungeongame.level.SaveConfig;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.entity.ObjectTypes;
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

public class Debuggers {
    private static Box2DDebugRenderer box2dDebugRenderer;
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static InputProcessor tmpProcessor;
    private static final Map<String, ValueHolder<Boolean>> debugOptions = new LinkedHashMap<>();
    private static UUID selectedObjectId = null;
    private static UUID selectedLightId = null;
    private static Vector2 oldPos;
    private static Vector2 oldLightPos;
    private static String data = null;
    //short for less memory
    private static final ArrayList<Short> fpsCounter = new ArrayList<>();
    private static String createObjectData = null;
    private static final ArrayList<String> logger = new ArrayList<>();
    public static final Map<String, ArrayList<PathfindDebugPos>> pathfindDebuggers = new HashMap<>();
    public static final Map<String, Integer> pathfindDebuggerTimers = new HashMap<>();
    public static Vector2 freecam = Vector2.Zero;
    private static float lastTime = 0;
    private static int selectedSound = 0;
    private static final ArrayList<String> audioNames = new ArrayList<>();
    private static int newMapEditorName = 0;
    private static final List<String> newMapNames = new ArrayList<>();
    private static boolean forceShow = false;
    public static Map<Consumer<MatchView>, ValueHolder<Integer>> customLevelRenderers = new HashMap<>();
    public static Map<Runnable, ValueHolder<Integer>> customUiRenderers = new HashMap<>();
    public static List<ScreenSS> activeScreenSS = new ArrayList<>();
    private static int screenSS = 0;

    static {
        if (GameData.DEBUGGING) {
            debugOptions.put("showing", new ValueHolder<>(false));
            debugOptions.put("hitboxes", new ValueHolder<>(false));
            debugOptions.put("lights", new ValueHolder<>(true));
            debugOptions.put("noclip", new ValueHolder<>(false));
            debugOptions.put("selecting", new ValueHolder<>(false));
            debugOptions.put("selectingLight", new ValueHolder<>(false));
            debugOptions.put("staticLightUpdates", new ValueHolder<>(false));
            debugOptions.put("pathfindingRender", new ValueHolder<>(false));
            debugOptions.put("disablePathfinding", new ValueHolder<>(false));
            debugOptions.put("freecam", new ValueHolder<>(false));
            debugOptions.put("tick", new ValueHolder<>(true));
            debugOptions.put("markers", new ValueHolder<>(false));
            debugOptions.put("invulnerable", new ValueHolder<>(false));
            debugOptions.put("pauseTimers", new ValueHolder<>(false));
            debugOptions.put("pixelPerfect", new ValueHolder<>(false));
            debugOptions.put("wireframe", new ValueHolder<>(false));
            debugOptions.put("tickMapEditor", new ValueHolder<>(false));
            debugOptions.put("demoWindow", new ValueHolder<>(false));
            debugOptions.put("showBetweenBoxes", new ValueHolder<>(true));
            debugOptions.put("fakeControllers+1", new ValueHolder<>(false));
            debugOptions.put("fakeControllers+2", new ValueHolder<>(false));
            debugOptions.put("screenSSDebugger", new ValueHolder<>(false));
            debugOptions.put("ignoreInvalidSS", new ValueHolder<>(false));
            debugOptions.put("showControllerSelect", new ValueHolder<>(false));
            debugOptions.put("forceSingleView", new ValueHolder<>(false));

            PathUtil.getFilesIn(PathUtil.asset("sounds/")).forEach(e -> audioNames.add(e.replace("assets/sounds/", "").replace(".mp3", "")));

            try {
                for (JsonElement options : GsonUtil.parse(Files.readString(Path.of("debug.json"))).get("options").getAsJsonArray()) {
                    JsonObject map = options.getAsJsonObject();
                    if(debugOptions.containsKey(map.get("name").getAsString())) {
                        debugOptions.put(map.get("name").getAsString(), new ValueHolder<>(map.get("value").getAsBoolean()));
                    }
                }
            } catch (Exception ignored) {

            }
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            debugOptions.get("showing").object = !isEnabled("showing");
            save();
        }

        if (isDebuggerOpen()) {
            if (isEnabled("staticLightUpdates") && GameData.level != null) {
                for (LevelLight<?> light : GameData.level.getLights()) {
                    if (light.light().isStaticLight()) {
                        light.light().setStaticLight(false);
                        light.light().setStaticLight(true);
                    }
                }
            }

            if(isEnabled("pathfindingRender")) {
                RenderUtil.enableBlending();
                pathfindDebuggers.forEach((hash, debuggers) -> {
                    for (PathfindDebugPos pathfindDebugPos : debuggers) {
                        float transparency = pathfindDebuggerTimers.get(hash).floatValue()/(5* GameData.TICKS_PER_SECOND);

                        //GameData.shapeRenderer.setProjectionMatrix(GameData.gameCamera.combined);
                        if (pathfindDebugPos.type().equals(PathfindDebugType.SUCCESSFUL_PATH)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                            GameData.shapeRenderer.setColor(Color.GREEN.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.line(pathfindDebugPos.pos().getX() + 0.5f, pathfindDebugPos.pos().getY() + 0.5f, pathfindDebugPos.previous().getX() + 0.5f, pathfindDebugPos.previous().getY() + 0.5f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.CONNECTION)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                            GameData.shapeRenderer.setColor(Color.YELLOW.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.line(pathfindDebugPos.pos().getX() + 0.5f, pathfindDebugPos.pos().getY() + 0.5f, pathfindDebugPos.previous().getX() + 0.5f, pathfindDebugPos.previous().getY() + 0.5f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.INVALID)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            GameData.shapeRenderer.setColor(Color.GRAY.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.OPEN_SET)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            GameData.shapeRenderer.setColor(Color.RED.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.CLOSED_SET)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            GameData.shapeRenderer.setColor(Color.YELLOW.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.START)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            GameData.shapeRenderer.setColor(Color.BLUE.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                        } else if (pathfindDebugPos.type().equals(PathfindDebugType.END)) {
                            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            GameData.shapeRenderer.setColor(Color.PURPLE.cpy().mul(1, 1, 1, transparency));
                            GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                        }
                        GameData.shapeRenderer.end();
                    }
                });
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

            if(!isFullScreenOrMaximised && !forceShow) {
                ImGui.begin("Are you sure?");
                if(ImGui.button("Force show UI")) forceShow = true;
                ImGui.end();
            } else {
                ImGui.begin("Logger");
                for (String s : logger) {
                    if (s.startsWith("<error>")) {
                        ImGui.textColored(0xff0000ff, s.replace("<error>", ""));
                    } else ImGui.text(s);

                    if (!ImGui.isWindowHovered()) ImGui.setScrollY(10000);
                }
                ImGui.end();

                ImGui.begin("Options");

                if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor mapEditor) {
                    if (ImGui.button("Save map")) {
                        try {
                            Files.write(Path.of(PathUtil.codingDir(PathUtil.data("maps/" + mapEditor.getMapName() + ".map"))), LevelLoader.saveLevel(GameData.level).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                if (ImGui.button("Kill")) {
                    //GameData.player.damage(100000);
                }

                if (ImGui.button("Load map")) {
                    try {
                        GameData.startMatch(List.of(new MatchPlayer(User.getUser(5)))).setCurrentMinigame(new MapEditor(newMapNames.get(newMapEditorName)));
                    } catch (Exception e) {
                        Logger.error("Error loading map", e);
                    }
                }
                ImGui.sameLine();
                ImInt newName = new ImInt(newMapEditorName);
                if(ImGui.combo("Map", newName, newMapNames.toArray(new String[0]))) {
                    newMapEditorName = newName.get();
                }

                debugOptions.forEach((s, valueHolder) -> {
                    if (!s.equals("showing") && !s.equals("selecting") && !s.equals("selectingLight"))
                        if (ImGui.checkbox(s, valueHolder.object)) {
                            valueHolder.object = !valueHolder.object;
                            save();
                        }
                });

                float[] fpsArray = new float[fpsCounter.size()];
                int i = 0;

                for (Short f : fpsCounter) {
                    fpsArray[i++] = f;
                }

                ImGui.plotLines("FPS graph", fpsArray, 100, 1, "", 0, 200, new ImVec2(0, 80));
                if (GameData.time > lastTime + 0.02f) {
                    lastTime = GameData.time;
                    if (fpsCounter.size() > 100) fpsCounter.removeFirst();
                    fpsCounter.add((short) Gdx.graphics.getFramesPerSecond());
                }

                ImGui.text("Current FPS: " + Gdx.graphics.getFramesPerSecond());

                ImGui.text("Cached images: " + ImageUtil.size());
                ImGui.text("Cached files: " + PathUtil.size());
                ImGui.text("Cached sounds: " + SoundManager.size());

                if(ImGui.button("Why is my code not working?")) {
                    debugOptions.put("lights", new ValueHolder<>(false));
                }

                if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getMatchViews() != null && !GameData.getCurrentMatch().getMatchViews().isEmpty()) {
                    slider("zoom", GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom, (e) -> {GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom = e;}, -10, 10, "%.3f");
                }

                ImGui.end();

                ImGui.begin("Effects");
                for (String allEffect : Effects.getAllEffects().keySet()) {
                    if(ImGui.button("Apply "+allEffect)) {
                        GameData.getCurrentMatch().getMatchViews().getFirst().getActiveEffects().add(new ActiveEffect(Effects.getAllEffects().get(allEffect)));
                    }
                }
                ImGui.end();

                if(isEnabled("demoWindow")) ImGui.showDemoWindow();

                UUID hoveredObject = null;

                ImGui.begin("Lights");
                UUID hoveredLight = renderLightSelector();
                boolean showLights = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows);
                ImGui.end();

                ImGui.begin("Controlller");
                if(Controllers.getCurrent() == null) {
                    ImGui.text("None connected");
                } else {
                    for (int j = 0; j < 30; j++) {
                        ImGui.text("Button " + j + ": " + Controllers.getCurrent().getButton(j));
                    }
                    for (int j = 0; j < 10; j++) {
                        ImGui.text("Axis " + j + ": " + Controllers.getCurrent().getAxis(j));
                    }
                }
                ImGui.end();

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

                ImGui.begin("Objects");

                if (createObjectData != null) {
                    renderObjectCreator();
                } else {
                    hoveredObject = renderObjectSelector();
                }

                ImGui.end();

                ImGui.begin("ScreenSS");
                if(Debuggers.isEnabled("screenSSDebugger")) {
                    ImGui.beginChild("Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
                    ImGui.separatorText("All Objects");

                    ScreenSS currentlySelected = null;

                    if (ImGui.beginTable("Object Selector", 1, ImGuiTableFlags.RowBg)) {
                        ValueHolder<Integer> id = new ValueHolder<>(0);
                        ValueHolder<ScreenSS> selected = new ValueHolder<>(null);
                        for (ScreenSS ss : Debuggers.activeScreenSS) {
                            if(!(ss instanceof CombinedScreenSS.ScreenParentSS) || ((CombinedScreenSS.ScreenParentSS) ss).getParent() == null) addSS(ss, id, selected, "");
                        }
                        currentlySelected = selected.object;
                        ImGui.endTable();
                    }

                    ImGui.endChild();

                    ImGui.sameLine();

                    ImGui.beginChild("ScreenSS Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);

                    ImGui.separatorText("Current ScreenSS");

                    if(currentlySelected != null) {
                        ImString added = new ImString();
                        if(ImGui.inputText("To add", added, ImGuiInputTextFlags.EnterReturnsTrue) && !added.get().isBlank()) {
                            currentlySelected.getGetters().put(added.get(), "1");
                        }
                        ValueHolder<ScreenSS> screenSSValueHolder = new ValueHolder<>(currentlySelected);
                        List<String> toRemove = new ArrayList<>();
                        currentlySelected.getGetters().forEach((g, v) -> {
                            if(ImGui.isKeyDown(ImGuiKey.ModShift)) {
                                try {
                                    if (v.endsWith("%")) {
                                        slider(g, Float.parseFloat(v.replace("%", "")), (val) -> ToRun.run(() -> screenSSValueHolder.object.getGetters().put(g, val + "%")), 0, 100, "%.0f");
                                    } else if (v.endsWith("vw")) {
                                        slider(g, Float.parseFloat(v.replace("vw", "")), (val) -> ToRun.run(() -> screenSSValueHolder.object.getGetters().put(g, val + "vw")), 0, 1, "%.3f");
                                    } else if (v.endsWith("vh")) {
                                        slider(g, Float.parseFloat(v.replace("vh", "")), (val) -> ToRun.run(() -> screenSSValueHolder.object.getGetters().put(g, val + "vh")), 0, 1, "%.3f");
                                    } else if (v.contains("vw")) {
                                        slider(g, Float.parseFloat(v.replace("vw", "")), (val) -> ToRun.run(() -> screenSSValueHolder.object.getGetters().put(g, val + "vw")), 0, 1, "%.3f");
                                        //no + or - etc
                                    } else if (v.matches("^-?\\d+(\\.\\d+)?[A-Za-z]*$")) {
                                        float current = Float.parseFloat(v);
                                        slider(g, current, (val) -> ToRun.run(() -> screenSSValueHolder.object.getGetters().put(g, String.valueOf(val))), 0, (current > 100 ? 10000 : current > 10 ? 100 : 10), "%.3f");
                                    } else ImGui.text("Cannot create slider");
                                } catch (Exception e) {
                                    ImGui.text("Error creating slider");
                                }
                            } else {
                                ImString text = new ImString(v, v.length()+100);
                                if(ImGui.inputText(g, text, isEnabled("ignoreInvalidSS") ? ImGuiInputTextFlags.None : ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    ToRun.run(() -> {screenSSValueHolder.object.getGetters().put(g, text.get());});
                                }
                                ImGui.sameLine();
                                ImGui.text("(Current: "+screenSSValueHolder.object.get(g)+")");
                            }
                            ImGui.sameLine();
                            if(ImGui.button("X")) {
                                toRemove.add(g);
                            }
                        });
                        for (String s : toRemove) {
                            currentlySelected.getGetters().remove(s);
                        }
                    }

                    ImGui.endChild();
                } else {
                    ImGui.text("Enable screenSSDebugger to use");
                }
                ImGui.end();

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
                        } else if (selectedObject instanceof TilesetObject tilesetObject) {
                            GameData.shapeRenderer.setColor(0xdf / 255.0f, 0xf0 / 255.0f, 0x29 / 255.0f, 0.5f);
                            GameData.shapeRenderer.rect(tilesetObject.getPos().x, tilesetObject.getPos().y, tilesetObject.getWidth(), tilesetObject.getHeight());
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

            if (ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow) || ImGui.isWindowFocused(
                ImGuiFocusedFlags.AnyWindow)) {
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
        activeScreenSS.clear();
    }

    private static void save() {
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

    private static void addSS(ScreenSS ss, ValueHolder<Integer> i, ValueHolder<ScreenSS> selectedHolder, String prepend) {
        int id = i.object;
        ImGui.tableNextRow();
        ImGui.tableNextColumn();
        ImGui.pushID(id);

        int flags = ImGuiSelectableFlags.SpanAllColumns;
        boolean selected = screenSS == ss.hashCode();
        if (selected) {
            flags |= ImGuiTreeNodeFlags.Selected;
            selectedHolder.object = ss;
        }
        if (ImGui.selectable(prepend + ss.getElementId() + " (" + id + ")", selected, flags)) screenSS = ss.hashCode();

        GameData.shapeRenderer.setProjectionMatrix(GameData.uiCamera.combined);
        if (ImGui.isItemHovered()) {
            Color c = Color.SCARLET.cpy();
            c.a = 0.5f;
            RenderUtil.enableBlending();
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            GameData.shapeRenderer.setColor(c);
        } else {
            //((ShapeRendererLineWidth) GameData.shapeRenderer).setDefaultRectLineWidth(100);
            GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            GameData.shapeRenderer.setColor(Color.ORANGE);
        }
        GameData.shapeRenderer.rect(ss.getX(), ss.getY(), ss.getXSize(), ss.getYSize());
        GameData.shapeRenderer.end();
        ImGui.popID();
        i.object++;

        for (ScreenSS activeScreenSS1 : Debuggers.activeScreenSS) {
            if(activeScreenSS1 instanceof CombinedScreenSS.ScreenParentSS parent && parent.getParent() == ss) {
                addSS(activeScreenSS1, i, selectedHolder, prepend+"    ");
            }
        }
    }

    private static void renderObjectCreator() {
        if (ImGui.button("Edit objects"))
            createObjectData = null;
        else {
            ImString objectCreator = new ImString(createObjectData, 10000);

            ImGui.inputTextMultiline("Create object", objectCreator, ImGuiInputTextFlags.None);

            createObjectData = objectCreator.get();

            boolean create = ImGui.button("Create");
            ImGui.sameLine();
            boolean forcedUUID = ImGui.button("Create (force UUID)");
            if (create || forcedUUID) {
                try {
                    JsonObject data = GsonUtil.parse(createObjectData);
                    if (!forcedUUID) {
                        data.addProperty("uuid", UUID.randomUUID().toString());
                    }
                    AbstractObject object = LevelLoader.createObject(data, GameData.level);
                    createObjectData = null;
                    assert object != null;
                    selectedObjectId = object.getUUID();
                } catch (Exception e) {
                    Logger.error("Error creating object", e);
                }
            }

            ImGui.separatorText("Default Objects");
            ObjectTypes.types.forEach((n, c) -> {
                if(ImGui.button("Create " + n)) {
                    try {
                        AbstractObject object = c.constructor().get();
                        GameData.getLevelOrThrow().addObject(object);
                        createObjectData = null;
                        selectedObjectId = object.getUUID();
                    } catch (Exception e) {
                        Logger.error("Error adding object", e);
                    }
                }
            });
        }
    }

    private static UUID renderObjectSelector() {
        if(GameData.level == null) return null;
        UUID hoveredObject = null;
        if (ImGui.button("Add object")) createObjectData = "";

        ImGui.sameLine();

        ImVec4 oldColour = ImGui.getStyle().getColor(ImGuiCol.Button);
        ImVec4 selectedColour = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);
        if (isEnabled("selecting"))
            ImGui.getStyle().setColor(ImGuiCol.Button, selectedColour.x, selectedColour.y, selectedColour.z, selectedColour.w);
        if (ImGui.button("Pick Object")) {
            debugOptions.get("selecting").object = !isEnabled("selecting");
        } else {
            if (debugOptions.get("selecting").object) {
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                    selectedObjectId = getHoveredObject() == null ? null : getHoveredObject().getUUID();
                    debugOptions.get("selecting").object = false;
                } else {
                    hoveredObject = getHoveredObject() == null ? null : getHoveredObject().getUUID();
                }
            }
        }
        ImGui.getStyle().setColor(ImGuiCol.Button, oldColour.x, oldColour.y, oldColour.z, oldColour.w);

        ImGui.beginChild("Left Panel", new ImVec2(300, 0), ImGuiChildFlags.Border | ImGuiChildFlags.ResizeX);
        ImGui.separatorText("All Objects");

        if (ImGui.beginTable("Object Selector", 1, ImGuiTableFlags.RowBg)) {
            int id = 0;
            for (AbstractObject allObject : GameData.getLevelOrThrow().getAllObjects()) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.pushID(id);
                int flags = ImGuiSelectableFlags.SpanAllColumns;
                boolean selected = allObject.getUUID().equals(selectedObjectId);
                if (selected)
                    flags |= ImGuiTreeNodeFlags.Selected;
                if (ImGui.selectable(allObject.toString() + " (" + allObject + ")", selected, flags))
                    selectedObjectId = allObject.getUUID();
                if (ImGui.isItemHovered()) hoveredObject = allObject.getUUID();
                ImGui.popID();

                id++;
            }
            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("Right Panel", new ImVec2(0, 0), ImGuiChildFlags.Border);

        ImGui.separatorText("Current Object");

        AbstractObject selectedObject;
        if (selectedObjectId != null && (selectedObject = GameData.level.getObjectByUUID(selectedObjectId)) != null) {

            Vector2 middle = oldPos == null ? selectedObject.getPos() : oldPos;
            int posOffset = ImGui.isKeyDown(ImGuiKey.ModShift) ? 1 : 10;

            boolean changing = false;
            slider("X Pos", selectedObject.getPos().x, selectedObject::setX, middle.x - posOffset, middle.x + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
            if (ImGui.isItemActive() && ImGui.isKeyDown(ImGuiKey.ModShift)) {
                float x = selectedObject.getPos().x;
                float snappedX = Math.round(x * 16f) / 16f;
                selectedObject.setX(snappedX);
            }
            if (ImGui.isItemActive()) changing = true;
            slider("Y Pos", selectedObject.getPos().y, selectedObject::setY, middle.y - posOffset, middle.y + posOffset, ImGui.isKeyDown(ImGuiKey.ModAlt) ? "%.0f" : "%.3f");
            if (ImGui.isItemActive() && ImGui.isKeyDown(ImGuiKey.ModShift)) {
                float y = selectedObject.getPos().y;
                float snappedX = Math.round(y * 16f) / 16f;
                selectedObject.setY(snappedX);
            }
            if (ImGui.isItemActive()) changing = true;

            if (ImGui.button("TP to player")) selectedObject.setPos(GameData.getCurrentMatch().getPlayers().getFirst().getPlayer().getPos());
            ImGui.sameLine();
            if (ImGui.button("TP player to this")) GameData.getCurrentMatch().getPlayers().getFirst().getPlayer().setPos(selectedObject.getPos());

            if (oldPos == null && changing) {
                oldPos = selectedObject.getPos();
            }
            if (oldPos != null && !changing) {
                oldPos = null;
            }

            ImGui.separatorText("Java variables");
            if(ImGui.collapsingHeader("Show")) addVariables(selectedObject, selectedObject.getClass());

            ImGui.separatorText("Data");
            if (data != null) {
                ImString input = new ImString(data, data.length() + 10000);

                ImGui.inputTextMultiline(" ", input, ImGuiInputTextFlags.None);

                data = input.get();
            }
            JsonObject object = selectedObject.write();
            data = GsonUtil.PARSER.toJson(object);

            if (ImGui.button("Refresh")) {
                try {
                    ToRun.run(() -> {
                        JsonObject data = selectedObject.write();
                        AbstractObject o = LevelLoader.createObject(data, GameData.level);
                        selectedObject.dispose();
                        //noinspection usagelimited
                        selectedObject.setUUIDReallyUnsafeDoNotUse(UUID.randomUUID());
                        selectedObjectId = o.getUUID();
                    });
                } catch (Exception e) {
                    Logger.error("Error refreshing object", e);
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Duplicate")) {
                try {
                    JsonObject data = selectedObject.write();
                    data.addProperty("uuid", UUID.randomUUID().toString());
                    AbstractObject o = LevelLoader.createObject(data, GameData.level);
                    selectedObjectId = o.getUUID();
                } catch (Exception e) {
                    Logger.error("Error duplicating object", e);
                }
            }
            ImGui.sameLine();

            if (ImGui.button("Delete your computer")) {
                for (int i = 0; i < 500; i++) {
                    try {
                        JsonObject data = selectedObject.write();
                        data.addProperty("uuid", UUID.randomUUID().toString());
                        AbstractObject o = LevelLoader.createObject(data, GameData.level);
                        o.setPos(o.getPos().add(NumberUtils.getRandomFloat(-100, 100), NumberUtils.getRandomFloat(-100, 100)));
                        selectedObjectId = o.getUUID();
                    } catch (Exception e) {
                        Logger.error("Error duplicating object", e);
                    }
                }
            }
            ImGui.sameLine();

            if (ImGui.button("Delete")) {
                GameData.level.removeObject(selectedObject);
            }
        }

        ImGui.endChild();

        return hoveredObject;
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

    private static void slider(String name, float getter, Consumer<Float> setter, float min, float max, String format) {
        if (Float.isFinite(min) && Float.isFinite(max)) {
            float[] check = {getter};
            if (ImGui.sliderFloat(name, check, min, max, format)) {
                setter.accept(check[0]);
            }
        } else {
            ImGui.text("Invalid bounds for slider "+name);
        }
    }

    private static AbstractObject getHoveredObject() {
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
        t += 1;
        if (t >= System.currentTimeMillis() - 10) {
            Logger.error("Make sure to run breakpoint here!");
        }
    }

    public static boolean isEnabled(String option) {
        return debugOptions.get(option).object;
    }


    private static void addVariables(AbstractObject selectedObject, Class<?> clazz) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            if(declaredField.getAnnotation(NoDebugOption.class) != null || Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers())) continue;
            declaredField.setAccessible(true);
            if(declaredField.getAnnotation(RequiresRefresh.class) != null) {
                ImGui.textColored(255, 0, 0, 255, "R");
                ImGui.setItemTooltip("Requires refresh");
                ImGui.sameLine();
            }
            try {
                addVariable(declaredField.getName(), declaredField.getType(), declaredField.get(selectedObject), (t) -> {
                    try {
                        declaredField.set(selectedObject, t);
                    } catch (IllegalAccessException e) {
                        Logger.error("Error setting variable", e);
                    }
                });
            } catch (Exception e) {
                Logger.error("Error adding variable", e);
                ImGui.text("Error adding variable: " + e.getMessage());
            }
        }

        if(clazz.getSuperclass() != null) addVariables(selectedObject, clazz.getSuperclass());
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
            }
        } else if(type.equals(Float.class)) {
            ImFloat check = new ImFloat((Float) current);
            if(ImGui.inputFloat(name, check)) {
                setter.accept((T) (Object) check.get());
            }
        } else if(type.equals(Double.class)) {
            ImDouble check = new ImDouble((Double) current);
            if(ImGui.inputDouble(name, check)) {
                setter.accept((T) (Object) check.get());
            }
        } else if(type.equals(String.class)) {
            ImString check = new ImString((String) current, ((String) current).length()+10000);
            if(ImGui.inputText(name, check, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept((T) check.get());
            }
        } else if(type.equals(UUID.class)) {
            ImString check = new ImString(current.toString());
            if(ImGui.inputText(name, check, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept((T) UUID.fromString(check.get()));
            }
        } else if(type.equals(Boolean.class)) {
            if(ImGui.checkbox(name, (Boolean) current)) {
                setter.accept((T) ((Boolean) !((Boolean) current)));
            }
        } else if(type.equals(Vector2.class)) {
            float[] check = {((Vector2) current).x, ((Vector2) current).y};
            if(ImGui.inputFloat2(name, check)) {
                ((Vector2) current).x = check[0];
                ((Vector2) current).y = check[1];
            }
        } else if(type.equals(Color.class)) {
            float[] check = {((Color) current).r, ((Color) current).g, ((Color) current).b, ((Color) current).a};
            if(ImGui.colorEdit4(name, check)) {
                ((Color) current).r = check[0];
                ((Color) current).g = check[1];
                ((Color) current).b = check[2];
                ((Color) current).a = check[3];
            }
        } else {
            ImGui.text("Unsupported type: " + name + " (value: " + current + ")");
        }
    }
}
