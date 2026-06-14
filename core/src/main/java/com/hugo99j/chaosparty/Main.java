package com.hugo99j.chaosparty;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.ScreenUtils;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.daniel99j.dungeongame.ui.UiScreen;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.hugo99j.chaosparty.ui.PlayScreen;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.MenuScreen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Main extends Game {
    private CursorType oldCursor = CursorType.NORMAL;
    private boolean cursorCaught = false;
    private GLFWErrorCallback glfwErrorCallback;
    private float activeTimer;
    private float tickTimer;
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

        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            //Create atlases
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.combineSubdirectories = true;
            TexturePacker.process(settings, PathUtil.codingDir(PathUtil.asset("textures")), PathUtil.codingDir(PathUtil.generated("atlases")), "main");
        }

        //dont load it before texture packer else it will crash
        GameData.init(this);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if(GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor mapEditor) mapEditor.scroll(amountY);
                return super.scrolled(amountX, amountY);
            }
        });

        GameData.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        //GameData.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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
        //GameData.gameViewport.update(width, height, true);
        GameData.uiViewport.update(width, height, true);

        if(GameData.getCurrentMatch() != null) GameData.getCurrentMatch().updateViews();

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

        //GameConstants.gameCamera.update();
        activeTimer += Gdx.graphics.getDeltaTime();

        boolean inMapEditor = GameData.getCurrentMatch() != null && GameData.getCurrentMatch().getCurrentMinigame() instanceof MapEditor;
        if (activeTimer > GameData.SECONDS_PER_PHYSICS_TICK && (!inMapEditor || (GameData.DEBUGGING && Debuggers.isEnabled("tickMapEditor"))) && this.getScreen() instanceof PlayScreen)
            while ((activeTimer -= GameData.SECONDS_PER_PHYSICS_TICK) > 0) {
                tickTimer+= GameData.SECONDS_PER_PHYSICS_TICK;
                if(tickTimer >= GameData.SECONDS_PER_TICK) {
                    List<Consumer<MatchView>> customRenderersToRemove = new ArrayList<>();
                    Debuggers.customRenderers.forEach((r, v) -> {
                        v.object = v.object-1;
                        if(v.object <= 0) customRenderersToRemove.add(r);
                    });
                    for (Consumer<MatchView> runnable : customRenderersToRemove) {
                        Debuggers.customRenderers.remove(runnable);
                    }

                    if(GameData.level != null) GameData.level.tickWorld();
                    if(GameData.getCurrentMatch() != null) GameData.getCurrentMatch().tick();
                    tickTimer = 0;
                }
                if(GameData.level != null) GameData.level.getBox2dWorld().step(GameData.SECONDS_PER_PHYSICS_TICK, 6, 2);
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
