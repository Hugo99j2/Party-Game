package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;

/** First screen of the application. Displayed after the application is created. */
public class WinScreen extends UiScreen {
    Texture backgroundTexture;
    ParticleEffect firework;

    public WinScreen() {
        super(ScreenSSBuilder.create()
            .set("x", "0vw")
            .set("y", "0vh")
            .set("xSize", "100vw")
            .set("ySize", "100vh")
            .newChild("menu")
            .set("x", "0.5vw")
            .set("y", "0.5vh")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("center", true)
            .set("scale", 2)
            .finishChild()
            .newChild("menu2")
            .set("x", "0.5vw")
            .set("y", "0.3vh")
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
        firework = new ParticleEffect();
        firework.load(Gdx.files.internal(PathUtil.asset("particles/w.p")), GameData.atlas);
        firework.setEmittersCleanUpBlendFunction(false);
        firework.scaleEffect(1);
        firework.setDuration(1000000);
        firework.start();

        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        this.addRenderable(new Button("menu", "button", "Back to menu") {
            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new MenuScreen()));
            }
        });
        this.addRenderable(new Button("menu2", "button", "Back to menu") {
            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new MenuScreen()));
            }
        });
        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
        this.addRenderable(new Text("text", "<colour:green>YOU WON!"));
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        // Draw your screen here. "delta" is the time since last render in seconds.
        float worldWidth = GameData.uiViewport.getWorldWidth();
        float worldHeight = GameData.uiViewport.getWorldHeight();

        GameData.spriteBatch.setColor(Color.LIME);

        GameData.spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        super.render(delta);


        if(firework.isComplete()) {
            firework.dispose();
        } else {
            firework.setPosition(NumberUtils.getRandomFloat(0, GameData.width), NumberUtils.getRandomFloat(-30, 30));
            firework.draw(GameData.spriteBatch, Gdx.graphics.getDeltaTime());
        }
        GameData.spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }
}
