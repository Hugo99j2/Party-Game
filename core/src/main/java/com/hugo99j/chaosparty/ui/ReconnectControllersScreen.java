package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.ui.UiScreen;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.util.DummyController;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.ToRun;

/** First screen of the application. Displayed after the application is created. */
public class ReconnectControllersScreen extends UiScreen {
    Texture backgroundTexture;

    public ReconnectControllersScreen() {
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

        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        this.addRenderable(new Button("menu", "button", "Done") {
            @Override
            public void onClick() {
                reconnect();
            }
        });
        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
        this.addRenderable(new Text("text", "<colour:red>Please reconnect controllers!"));
        reconnect();
    }

    private void reconnect() {
        boolean connected = true;
        int i = 0;
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if(Controllers.getControllers().size <= i) {
                if(GameData.DEBUGGING && Debuggers.isEnabled("fakeControllers")) {
                    player.controller = (Controller) new DummyController();
                    continue;
                }
                connected = false;
                break;
            }
            player.controller = Controllers.getControllers().get(i);
            i++;
        }
        if(connected) ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new PlayScreen()));
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
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }
}
