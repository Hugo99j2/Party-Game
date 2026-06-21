package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ToRun;

/** First screen of the application. Displayed after the application is created. */
public class PausedGameScreen extends HoldFrameScreen {
    public PausedGameScreen() {
        super(ScreenSSBuilder.create().build());
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || ((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.MENU)) {
            ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new PlayScreen()));
            SoundManager.getSound("pause").playSingle(1);
            if(GameData.getCurrentMatch() != null) GameData.getCurrentMatch().getCurrentMinigame().setPaused(false);
        }
    }
}
