package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.util.PathUtil;

/** First screen of the application. Displayed after the application is created. */
public class PlayScreen implements Screen {


    public PlayScreen() {
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        if(GameData.getCurrentGame() != null) GameData.getCurrentGame().render(delta);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
