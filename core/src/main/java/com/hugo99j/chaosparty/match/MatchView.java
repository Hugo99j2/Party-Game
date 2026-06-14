package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.Debuggers;

public class MatchView implements Disposable {
    private FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, GameData.width, GameData.height, false);
    public final OrthographicCamera gameCamera = new OrthographicCamera();
    public Viewport gameViewport;
    public int worldWidth, worldHeight;

    public MatchView(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.gameViewport = new ExtendViewport(worldWidth, worldHeight, gameCamera);
        this.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public TextureRegion render() {
        fbo.begin();
        ScreenUtils.clear(new Color(0x331111ff));

        gameViewport.apply();

        GameData.shapeRenderer.setProjectionMatrix(gameCamera.combined);
        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        GameData.shapeRenderer.setColor(Color.BLACK);
        GameData.shapeRenderer.rect(gameCamera.position.x-1000, gameCamera.position.y-1000, 10000, 10000);
        GameData.shapeRenderer.end();

        GameData.spriteBatch.setProjectionMatrix(gameCamera.combined);

        GameData.spriteBatch.begin();

        if(GameData.level != null) {
            GameData.spriteBatch.enableBlending();
            GameData.getLevelOrThrow().render();

            if(!GameData.DEBUGGING || Debuggers.isEnabled("lights")) {
//                GameConstants.gameCamera.update();
//                GameConstants.gameViewport.apply();
                GameData.level.rayHandler.useCustomViewport(gameViewport.getScreenX(), gameViewport.getScreenY(), gameViewport.getScreenWidth(), gameViewport.getScreenHeight());
                GameData.level.rayHandler.setCombinedMatrix(gameCamera);
                GameData.level.rayHandler.updateAndRender();
            }
        }

        GameData.spriteBatch.end();

        fbo.end();

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
}
