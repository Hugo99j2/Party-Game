package com.daniel99j.dungeongame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.RenderUtil;

/** First screen of the application. Displayed after the application is created. */
public class YouDiedScreen extends UiScreen {
    Texture backgroundTexture;
    float fade = 1;

    public YouDiedScreen() {
        super(ScreenSSBuilder.create()
            .set("x", "0vw")
            .set("y", "0vh")
            .set("xSize", "100vw")
            .set("ySize", "100vh")
            .newChild("respawn")
                .set("x", "0.5vw")
                .set("y", "0.5vh")
                .set("xSize", 320)
                .set("ySize", 64)
                .set("center", true)
                .set("scale", 2)
            .finishChild()
            .newChild("text")
                .set("x", "0.5vw")
                .set("y", "0.7vh")
                .set("xSize", 1)
                .set("ySize", 1)
            .finishChild()
            .build()
        );
        backgroundTexture = new Texture(PathUtil.asset("game/damage_overlay.png"));
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        this.addRenderable(new Button("respawn", "button", "Return to camp") {
            @Override
            public void onClick() {
                SoundManager.getSound("click").playSingle(1);
                Logger.info("clicked");
            }
        });
        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
        this.addRenderable(new Text("text", "<colour:red>The end."));
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        // Draw your screen here. "delta" is the time since last render in seconds.
        float worldWidth = GameData.uiViewport.getWorldWidth();
        float worldHeight = GameData.uiViewport.getWorldHeight();

        GameData.spriteBatch.setColor(Color.RED);

        GameData.spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        super.render(delta);
        GameData.spriteBatch.end();

        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if(fade > 0) fade -= delta;
        fade = Math.max(fade, 0);
        RenderUtil.enableBlending();
        GameData.shapeRenderer.setColor(new Color(0, 0, 0, fade));
        GameData.shapeRenderer.rect(0, 0, 10000, 10000);
        GameData.shapeRenderer.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }
}
