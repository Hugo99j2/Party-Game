package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.util.ControllerInput;
import com.hugo99j.chaosparty.util.ControllerUtil;
import com.hugo99j.chaosparty.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.ui.element.Button;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ToRun;

/** First screen of the application. Displayed after the application is created. */
public class PausedGameScreen extends HoldFrameScreen {
    public PausedGameScreen() {
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        Button resume = this.addElement(new Button("resume", "button", "Resume") {
            @Override
            public void onClick() {
                super.onClick();
                unpause();
            }

            @Override
            public float getX() {
                return GameData.width/2f;
            }

            @Override
            public float getY() {
                return GameData.height/2f;
            }

            @Override
            public float getWidth() {
                return 320;
            }

            @Override
            public float getHeight() {
                return 32;
            }
        });
        this.addElement(new Button("exit", "button", "Exit") {
            @Override
            public float getX() {
                return resume.getX();
            }

            @Override
            public float getY() {
                return resume.getY()-resume.getHeight()-10;
            }

            @Override
            public float getWidth() {
                return resume.getWidth();
            }

            @Override
            public float getHeight() {
                return resume.getHeight();
            }

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
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || ControllerUtil.getCurrent().wasJustPressed(ControllerInput.MENU)) {
            unpause();
        }
    }
}
