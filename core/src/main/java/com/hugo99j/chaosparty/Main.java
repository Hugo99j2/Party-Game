package com.hugo99j.chaosparty;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.ScreenUtils;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.daniel99j.dungeongame.ui.UiScreen;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.daniel99j.dungeongame.util.Logger;
import com.daniel99j.dungeongame.util.PathUtil;
import com.daniel99j.dungeongame.util.RenderUtil;
import com.daniel99j.dungeongame.util.ScheduledRunnables;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.MenuScreen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends Game {
    private CursorType oldCursor = CursorType.NORMAL;
    private boolean cursorCaught = false;
    private GLFWErrorCallback glfwErrorCallback;
    private static final List<Runnable> toRun = new ArrayList<>();
    private float activeTimer;
    private float tickTimer;
    private int oldXSize, oldYSize;

    public static Player tempPlayer;

    private FrameBuffer fbo;

    public static void run(Runnable o) {
        toRun.add(o);
    }

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

        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            //Create atlases
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.combineSubdirectories = true;
            TexturePacker.process(settings, PathUtil.codingDir(PathUtil.asset("textures")), PathUtil.codingDir(PathUtil.generated("atlases")), "main");
        }

        //dont load it before texture packer else it will crash
        GameData.init(this);
        GameData.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        GameData.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, GameData.width, GameData.height, false);

        // Resize your screen here. The parameters represent the new window size.
        GameData.gameViewport.update(width, height, true);
        GameData.uiViewport.update(width, height, true);

        super.resize(width, height);
    }

    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (!Gdx.graphics.isFullscreen()) {
                oldXSize = GameData.width;
                oldYSize = GameData.height;
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(oldXSize, oldYSize);
            }
        }
        fbo.begin();
        toRun.forEach(Runnable::run);
        toRun.clear();

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
        ScreenUtils.clear(new Color(0x111111ff));

        //so adding new ones whilst in the list works
        ArrayList<Runnable> oldRunnables = new ArrayList<>(ScheduledRunnables.runnables);
        ScheduledRunnables.runnables.clear();
        for (Runnable runnable : oldRunnables) {
            runnable.run();
        }
        if (GameData.DEBUGGING && Debuggers.isEnabled("freecam")) {
            GameData.gameCamera.position.x = Debuggers.freecam.x;
            GameData.gameCamera.position.y = Debuggers.freecam.y;
        } else {
//            if(GameData.player != null) {
//                GameData.gameCamera.position.x = GameData.player.getPos().x;
//                GameData.gameCamera.position.y = GameData.player.getPos().y;
//            }
        }
        //GameConstants.gameCamera.update();
        GameData.gameViewport.apply();

        GameData.spriteBatch.setProjectionMatrix(GameData.gameCamera.combined);

        GameData.shapeRenderer.setProjectionMatrix(GameData.gameCamera.combined);
        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        GameData.shapeRenderer.setColor(Color.BLACK);
        GameData.shapeRenderer.rect(GameData.gameCamera.position.x-10, GameData.gameCamera.position.y-10, 1000, 1000);
        GameData.shapeRenderer.end();

        activeTimer += Gdx.graphics.getDeltaTime();

        if (activeTimer > GameData.SECONDS_PER_PHYSICS_TICK)
            while ((activeTimer -= GameData.SECONDS_PER_PHYSICS_TICK) > 0) {
                tickTimer+= GameData.SECONDS_PER_PHYSICS_TICK;
                if(tickTimer >= GameData.SECONDS_PER_TICK) {
                    if(GameData.level != null) GameData.level.tickWorld();
                    if(GameData.getCurrentGame() != null) GameData.getCurrentGame().tick();
                    tickTimer = 0;
                }
                if(GameData.level != null) GameData.level.getBox2dWorld().step(GameData.SECONDS_PER_PHYSICS_TICK, 6, 2);
            }


        if(GameData.level != null) {
            GameData.spriteBatch.begin();
            GameData.spriteBatch.enableBlending();
            GameData.getLevelOrThrow().render();
            GameData.spriteBatch.end();

            if(!GameData.DEBUGGING || Debuggers.isEnabled("lights")) {
//                GameConstants.gameCamera.update();
//                GameConstants.gameViewport.apply();
                GameData.level.rayHandler.useCustomViewport(GameData.gameViewport.getScreenX(), GameData.gameViewport.getScreenY(), GameData.gameViewport.getScreenWidth(), GameData.gameViewport.getScreenHeight());
                GameData.level.rayHandler.setCombinedMatrix(GameData.gameCamera);
                GameData.level.rayHandler.updateAndRender();
            }
        }

        //Start UI
        //GameConstants.uiCamera.position.set(0, 0, 0);
        GameData.uiViewport.apply();
        GameData.spriteBatch.setProjectionMatrix(GameData.uiCamera.combined);
        GameData.shapeRenderer.setProjectionMatrix(GameData.uiCamera.combined);

        this.screen.render(Gdx.graphics.getDeltaTime());

        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)) RenderUtil.takeScreenshot();

        fbo.end();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameData.uiViewport.apply();
        GameData.spriteBatch.begin();
        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), 0, GameData.height, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), 0, GameData.height-GameData.height/2f, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), GameData.width/2f, GameData.height, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
        GameData.spriteBatch.draw(new TextureRegion(fbo.getColorBufferTexture()), GameData.width/2f, GameData.height-GameData.height/2f, 0, 0, GameData.width, GameData.height, 0.5f, -0.5f, 0);
        GameData.spriteBatch.end();
        Debuggers.render();
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
}
