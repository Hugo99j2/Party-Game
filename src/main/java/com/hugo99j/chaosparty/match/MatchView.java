package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.effect.ActiveEffect;
import com.hugo99j.chaosparty.ui.Debuggers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MatchView implements Disposable {
    private FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, GameData.width, GameData.height, false);
    public final OrthographicCamera gameCamera = new OrthographicCamera();
    public Viewport gameViewport;
    public int worldWidth, worldHeight;
    private final MatchPlayer player;
    private boolean center;
    private final List<ActiveEffect> activeEffects = new ArrayList<>();
    private float camerashakeTime = 0;

    public MatchView(int worldWidth, int worldHeight, MatchPlayer player) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.gameViewport = new ExtendViewport(worldWidth, worldHeight, gameCamera);
        this.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), center);
        this.player = player;
        this.center = false;
    }

    public MatchView(int worldWidth, int worldHeight) {
        this(worldWidth, worldHeight, null);
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    public void setCamerashakeTime(float camerashakeTime) {
        this.camerashakeTime = camerashakeTime;
    }

    public List<ActiveEffect> getActiveEffects() {
        return activeEffects;
    }

    public TextureRegion render() {
        camerashakeTime -= Gdx.graphics.getDeltaTime();

        if(this.player != null) {
            this.gameCamera.position.x = this.player.getPlayer().getPos().x;
            this.gameCamera.position.y = this.player.getPlayer().getPos().y;
        }
        fbo.begin();
        ScreenUtils.clear(new Color(0x331111ff));

        gameViewport.apply(center);

        Vector3 oldCameraPos = gameCamera.position.cpy();
        if(camerashakeTime > 0) {
            gameCamera.position.x += NumberUtils.getRandomFloat(-0.1f, 0.1f);
            gameCamera.position.y += NumberUtils.getRandomFloat(-0.1f, 0.1f);
            gameCamera.update();
        }

        GameData.shapeRenderer.setProjectionMatrix(gameCamera.combined);
        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        GameData.shapeRenderer.setColor(Color.BLACK);
        GameData.shapeRenderer.rect(gameCamera.position.x-1000, gameCamera.position.y-1000, 10000, 10000);
        GameData.shapeRenderer.end();

        GameData.spriteBatch.setProjectionMatrix(gameCamera.combined);

        GameData.getCurrentMatch().getCurrentMinigame().renderSegment(Gdx.graphics.getDeltaTime(), this);

        gameViewport.apply();
        GameData.shapeRenderer.setProjectionMatrix(gameCamera.combined);
        GameData.spriteBatch.setProjectionMatrix(gameCamera.combined);
        for (Consumer<MatchView> customRenderer : Debuggers.customLevelRenderers.keySet()) {
            customRenderer.accept(this);
        }

        fbo.end();

        gameCamera.position.set(oldCameraPos);

        return new TextureRegion(fbo.getColorBufferTexture());
    }

    @Override
    public void dispose() {
        fbo.dispose();
    }

    public void update() {
        fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, GameData.width, GameData.height, false);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public MatchPlayer getPlayer() {
        return player;
    }
}
