package com.hugo99j.chaosparty;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.studiohartman.jamepad.ControllerAxis;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private float time = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx128.png");

        for (Controller controller : Controllers.getControllers()) {
            Gdx.app.log("TAG", controller.getName());
        }
    }

    @Override
    public void render() {
        time += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();

        Controller current = Controllers.getCurrent();

        if(current == null || !current.isConnected()) {
            batch.setColor(Color.ORANGE);
            batch.draw(image, 140F, 210F, (float) 0, (float) 0, (float) image.getWidth(), (float) image.getHeight(), 1F, 1F, (float) time*40, 0, 0, image.getWidth(), image.getHeight(), false, false);
            batch.end();


        } else {
            if(current.getButton(current.getMapping().buttonA)) batch.setColor(Color.GREEN);
            if(current.getButton(current.getMapping().buttonX)) batch.setColor(Color.BLUE);
            if(current.getButton(current.getMapping().buttonY)) batch.setColor(Color.YELLOW);
            if(current.getButton(current.getMapping().buttonB)) batch.setColor(Color.RED);
            if(current.getButton(current.getMapping().buttonRightStick)) current.startVibration(2000, 1);


            batch.draw(image,
                400F+(current.getAxis(current.getMapping().axisLeftX)*20),
                210F-(current.getAxis(current.getMapping().axisLeftY)*20),
                (float) 0, (float) 0, (float) image.getWidth(), (float) image.getHeight(), 1F+(current.getAxis(5)*(current.getButton(current.getMapping().buttonDpadDown) ? -0.9f : 1)), 1F+(current.getAxis(4)*(current.getButton(current.getMapping().buttonDpadDown) ? -0.9f : 1)),
                180+(float) (Math.atan2(current.getAxis(current.getMapping().axisRightX), current.getAxis(current.getMapping().axisRightY))*(180/Math.PI)),
                0, 0, image.getWidth(), image.getHeight(), current.getButton(current.getMapping().buttonR1), current.getButton(current.getMapping().buttonL1)
            );
            batch.end();
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
