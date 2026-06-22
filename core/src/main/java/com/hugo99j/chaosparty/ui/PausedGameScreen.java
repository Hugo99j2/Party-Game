package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ToRun;

/** First screen of the application. Displayed after the application is created. */
public class PausedGameScreen extends HoldFrameScreen {
    public PausedGameScreen() {
        super(ScreenSSBuilder.create()
            .set("x", 0)
            .set("y", 0)
            .set("xSize", "1vw")
            .set("ySize", "1vh")
            .newChild("resume")
            .set("x", "0.5vw")
            .set("y", "0.5vh")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("center", true)
            .set("scale", 2)
            .finishChild()
            .newChild("exit")
            .set("x", "0.5vw")
            .set("y", "0.5vh-64-50")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("center", true)
            .set("scale", 2)
            .finishChild()
            .build());
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        this.addRenderable(new Button("resume", "button", "Resume") {
            @Override
            public void onClick() {
                super.onClick();
                unpause();
            }
        });
        this.addRenderable(new Button("exit", "button", "Exit") {
            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.getCurrentMatch().finishCurrentMinigame());
            }
        });
    }

    private void unpause() {
        ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new PlayScreen()));
        SoundManager.getSound("pause").playSingle(1);
        if(GameData.getCurrentMatch() != null) GameData.getCurrentMatch().getCurrentMinigame().setPaused(false);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || ((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.MENU)) {
            unpause();
        }
    }
}
