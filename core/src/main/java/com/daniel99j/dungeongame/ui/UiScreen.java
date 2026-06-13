package com.daniel99j.dungeongame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.dungeongame.ui.renderable.ClickType;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSS;
import com.hugo99j.chaosparty.GameData;

import java.util.ArrayList;
import java.util.List;

public class UiScreen implements Screen {
    private final ArrayList<Renderable> renderables = new ArrayList<>();
    private final CombinedScreenSS combinedScreenSS;
    private Renderable controllerSelected;

    public UiScreen(CombinedScreenSS combinedScreenSS) {
        this.combinedScreenSS = combinedScreenSS;
    }

    public void addRenderable(Renderable renderable) {
        this.renderables.add(renderable);
        renderable.setScreen(this);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(Controllers.getCurrent() != null) {
            if(Controllers.getCurrent().getButton(Controllers.getCurrent().getMapping().buttonDpadUp)) controllerStick(new Vector2(0, 50));
            if(Controllers.getCurrent().getButton(Controllers.getCurrent().getMapping().buttonDpadDown)) controllerStick(new Vector2(0, -50));
            if(Controllers.getCurrent().getButton(Controllers.getCurrent().getMapping().buttonA) && controllerSelected != null) {
                controllerSelected.onDown(0, 0, ClickType.LEFT);
            }
        }
        RenderState state = new RenderState(
            Gdx.input.isButtonPressed(Input.Buttons.LEFT),
            Gdx.input.isButtonJustPressed(Input.Buttons.LEFT),
            Gdx.input.isButtonPressed(Input.Buttons.MIDDLE),
            Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE),
            Gdx.input.isButtonPressed(Input.Buttons.RIGHT),
            Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT),
            Gdx.input.getX(), GameData.height-Gdx.input.getY(),
            delta);
        for (Renderable renderable : this.renderables) {
            if(renderable == controllerSelected) state = new RenderState(state.left(), state.leftJust(), state.middle(), state.middleJust(), state.right(), state.rightJust(), (int) controllerSelected.getCenter().x, (int) controllerSelected.getCenter().y, state.time());
            renderable.render(state);
        }
    }

    private void controllerStick(Vector2 change) {
        if(Controllers.getCurrent() != null) {
            Controllers.getCurrent().startVibration(1, 1);
            Vector2 pos = Vector2.Zero;
            if(controllerSelected != null) {
                pos = controllerSelected.getCenter();
            }

            Vector2 pos2 = pos.cpy().add(change);

            List<Renderable> nextList = new ArrayList<>(renderables);
            nextList.sort((a, b) -> {
                if (a == b) return 0;

                if (a.usesMouse != b.usesMouse) {
                    return a.usesMouse ? -1 : 1;
                }

                //if (a == controllerSelected) return 1;
                //if (b == controllerSelected) return -1;

                float distanceA = a.getCenter().dst(pos2);
                float distanceB = b.getCenter().dst(pos2);

                return Float.compare(distanceA, distanceB);
            });

            controllerSelected = nextList.getFirst();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        this.renderables.clear();
        this.controllerSelected = null;
    }

    public boolean isUsingMouse() {
        for (Renderable renderable : this.renderables) {
            if(renderable.usesMouse) return true;
        }
        return false;
    }

    public CursorType getCursorType() {
        for (Renderable renderable : this.renderables) {
            CursorType c = renderable.getCursorOverride();
            if(renderable.usesMouse && c != null) return c;
        }
        return null;
    }

    public CombinedScreenSS getStyle() {
        return this.combinedScreenSS;
    }
}
