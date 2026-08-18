package com.hugo99j.chaosparty;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.ScreenUtils;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.ui.debugger.DebugController;
import com.hugo99j.chaosparty.ui.renderable.CursorType;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.ui.screen.MenuScreen;
import com.hugo99j.chaosparty.ui.screen.PlayScreen;
import com.hugo99j.chaosparty.ui.screen.UiScreen;
import com.hugo99j.chaosparty.util.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Main extends Game {
    private CursorType oldCursor = CursorType.NORMAL;
    private boolean cursorCaught = false;
    private GLFWErrorCallback glfwErrorCallback;
    private float activeTimer;
    private int physicsTicks;
    private int oldXSize, oldYSize;

    @Override
    public void create() {
        //Makes errors send to the logger instead
        glfwErrorCallback = GLFWErrorCallback.createPrint(new PrintStream(new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    flushBuffer();
                } else if (b != '\r') {
                    buffer.append((char) b);
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                for (int i = off; i < off + len; i++) {
                    write(b[i]);
                }
            }

            @Override
            public void flush() {
                flushBuffer();
            }

            private void flushBuffer() {
                if (!buffer.isEmpty()) {
                    Logger.error(buffer.toString());
                    buffer.setLength(0);
                }
            }
        }, true));
        glfwErrorCallback.set();

        boolean needsRestart = false;
        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            try {
                Path path = Path.of(PathUtil.codingDir(PathUtil.generated("atlases/main.png")));
                int oldFile = Files.exists(path) ? Arrays.hashCode(Files.readAllBytes(path)) : 0;
                //Create atlases
                TexturePacker.Settings settings = new TexturePacker.Settings();
                settings.combineSubdirectories = true;
                TexturePacker.process(settings, PathUtil.codingDir(PathUtil.asset("textures")), PathUtil.codingDir(PathUtil.generated("atlases")), "main");
                int newFile = Arrays.hashCode(Files.readAllBytes(path));
                if(oldFile != newFile) {
                    needsRestart = true;
                } else {
                    Logger.info("No atlas changes.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //dont load it before texture packer else it will crash
        GameData.init(this);

        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            needsRestart = needsRestart || ImageUtil.generateImageBounds();
        }

        if(needsRestart) {
            throw new RuntimeException("Please restart as atlases/image bounds have changed!!!");
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor mapEditor) mapEditor.scroll(amountY);
                return super.scrolled(amountX, amountY);
            }
        });

        GameData.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        //GameData.gameViewport.updateUtil(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.setScreen(new MenuScreen());

        Debuggers.init();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        GameData.width = width;
        GameData.height = height;

        // Resize your screen here. The parameters represent the new window size.
        //GameData.gameViewport.updateUtil(width, height, true);
        GameData.uiViewport.update(width, height, true);

        if(GameData.getCurrentMatch() != null) GameData.getCurrentMatch().updateViews();

        super.resize(width, height);
    }

    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.F11) || ControllerUtil.getCurrent().wasJustPressed(ControllerInput.SCREEN)) {
            if (!Gdx.graphics.isFullscreen()) {
                oldXSize = GameData.width;
                oldYSize = GameData.height;
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(oldXSize, oldYSize);
            }
        }

        SoundManager.tick(Gdx.graphics.getDeltaTime());

        boolean cursorShouldBeCaught = !(Debuggers.isDebuggerOpen() || (this.getScreen() instanceof UiScreen ui && ui.isUsingMouse()));
        if(cursorShouldBeCaught != cursorCaught) {
            Gdx.input.setCursorCatched(cursorShouldBeCaught);
            cursorCaught = cursorShouldBeCaught;
        }

        CursorType newCursor = CursorType.NORMAL;
        CursorType uiCursor;
        if(this.getScreen() instanceof UiScreen ui && (uiCursor = ui.getCursorType()) != null) {
            newCursor = uiCursor;
        }
        if(oldCursor != newCursor && !Debuggers.isDebuggerOpen()) {
            GLFW.glfwSetCursor(((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle(), newCursor.getId());
            oldCursor = newCursor;
        }

        GameData.time += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(Color.BLACK);

        for (Controller controller : Controllers.getControllers()) {
            ((ControllerUtil) controller).updateUtil();
        }
        if(DebugController.INSTANCE instanceof ControllerUtil u) u.updateUtil();

        //so adding new ones whilst in the list works
        ArrayList<Runnable> oldRunnables = new ArrayList<>(ToRun.runnables);
        ToRun.runnables.clear();
        for (Runnable runnable : oldRunnables) {
            runnable.run();
        }
        if (GameData.DEBUGGING && Debuggers.isEnabled("freecam")) {
            //GameData.gameCamera.position.x = Debuggers.freecam.x;
            //GameData.gameCamera.position.y = Debuggers.freecam.y;
        } else {
//            if(GameData.player != null) {
//                GameData.gameCamera.position.x = GameData.player.getPos().x;
//                GameData.gameCamera.position.y = GameData.player.getPos().y;
//            }
        }

        //fbo.begin();

        //GameConstants.gameCamera.updateUtil();

        boolean inMapEditor = GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor;
        boolean tickIfMapEditor = (!inMapEditor || (GameData.DEBUGGING && Debuggers.isEnabled("tickMapEditor")));
        if(this.getScreen() instanceof PlayScreen && (!GameData.DEBUGGING || Debuggers.isEnabled("tick"))) {
            //Only add to timer if the game should currently be ticking!
            activeTimer += Gdx.graphics.getDeltaTime();

            if (activeTimer > GameData.SECONDS_PER_PHYSICS_TICK)
                while (activeTimer > GameData.SECONDS_PER_PHYSICS_TICK) {
                    activeTimer -= GameData.SECONDS_PER_PHYSICS_TICK;
                    physicsTicks++;
                    if (physicsTicks == GameData.PHYSICS_TICKS_PER_TICK) {
                        List<Consumer<MatchView>> customRenderersToRemove = new ArrayList<>();
                        Debuggers.customLevelRenderers.forEach((r, v) -> {
                            v.object = v.object - 1;
                            if (v.object <= 0) customRenderersToRemove.add(r);
                        });
                        for (Consumer<MatchView> runnable : customRenderersToRemove) {
                            Debuggers.customLevelRenderers.remove(runnable);
                        }

                        if (GameData.level != null && tickIfMapEditor) GameData.level.tickWorld();
                        if (GameData.getCurrentMatch() != null && tickIfMapEditor) GameData.getCurrentMatch().tick();
                        physicsTicks = 0;

                        for (Controller controller : Controllers.getControllers()) {
                            ((ControllerUtil) controller).onTick();
                        }
                        DebugController.tick();
                    }
                    if (GameData.level != null && tickIfMapEditor)
                        GameData.level.getBox2dWorld().step(GameData.SECONDS_PER_PHYSICS_TICK, 6, 2);
                }
        }




        //Start UI
        //GameConstants.uiCamera.position.set(0, 0, 0);
        GameData.uiViewport.apply();
        GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
        GameData.shapeRenderer.setProjectionMatrix(GameData.uiCamera.combined);

        this.screen.render(Gdx.graphics.getDeltaTime());

        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)) RenderUtil.takeScreenshot();

//        fbo.end();
//
//        Gdx.gl.glClearColor(1, 1, 1, 1);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        GameData.uiViewport.apply();
//        GameData.spriteBatch.begin();
//        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), 0, GameData.height, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
//        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), 0, GameData.height-GameData.height/2f, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
//        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), GameData.width/2f, GameData.height, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
//        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), GameData.width/2f, GameData.height-GameData.height/2f, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
//        GameData.spriteBatch.end();
        Debuggers.render();

        ModificationChecker.verify();
    }

    @Override
    public void setScreen(Screen screen) {
        if(this.screen != null) {
            this.screen.hide();
            this.screen.dispose();
            this.screen = null;
        }
        super.setScreen(screen);
    }

    @Override
    public void dispose() {
        super.dispose();
        Debuggers.dispose();
        GameData.shapeRenderer.dispose();
        GameData.spriteBatch.dispose();
        for (CursorType value : CursorType.values()) {
            GLFW.glfwDestroyCursor(value.getId());
        }
        if (glfwErrorCallback != null) {
            glfwErrorCallback.free();
            glfwErrorCallback = null;
        }
    }

    @Override
    public void pause() {
        super.pause();
        //by turning off vsync it forces the game to render whilst minimised
        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(24);
        //super.pause();
    }

    @Override
    public void resume() {
        super.resume();
        Gdx.graphics.setVSync(true);
        Gdx.graphics.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
    }
}
