package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.renderable.ClickType;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.RenderUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        if(Controllers.getCurrent() != null && controllerSelected == null) {
            controllerStick(Vector2.Zero);
        }
    }

    @Override
    public void render(float delta) {
        if(Controllers.getCurrent() != null && RenderUtil.isFocused()) {
            Vector2 controllerStickMove = new Vector2();
            if(((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.LEFT_STICK_UP)) controllerStickMove.add(0, 5);
            if(((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.LEFT_STICK_DOWN)) controllerStickMove.add(0, -5);
            if(((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.LEFT_STICK_LEFT)) controllerStickMove.add(-5, 0);
            if(((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.LEFT_STICK_RIGHT)) controllerStickMove.add(5, 0);
            if(controllerStickMove.len() > 0) controllerStick(controllerStickMove);

            if(((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.A) && controllerSelected != null) {
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
            Vector2 pos = Vector2.Zero.cpy();
            if(controllerSelected != null) {
                pos = controllerSelected.getCenter();
            }

            Map<Vector4, Renderable> selectors = new HashMap<>();
            for (Renderable renderable : renderables) {
                if(renderable.usesMouse && renderable != controllerSelected) {
                    selectors.put(new Vector4(renderable.getX(), renderable.getY(), renderable.getX()+renderable.getStyle().getXSize(), renderable.getY()+renderable.getStyle().getYSize()), renderable);
                    if(controllerSelected == null) {
                        if(change.len() > 0) {
                            Controllers.getCurrent().startVibration(100, 1);
                            SoundManager.getSound("select").play(1);
                        }
                        controllerSelected = renderable;
                        return;
                    }
                }
            }

            for (int i = 0; i < 1000; i++) {
                pos.add(change);
                for (Vector4 aabb : selectors.keySet()) {
                    if(pos.x > aabb.x && pos.y > aabb.y && pos.x < aabb.z && pos.y < aabb.w) {
                        controllerSelected = selectors.get(aabb);
                        Controllers.getCurrent().startVibration(100, 1);
                        SoundManager.getSound("select").play(1);
                        return;
                    }
                }
            }
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

    public void setControllerSelected(String elementId) {
        for (Renderable renderable : renderables) {
            if(renderable.usesMouse && renderable.getElementId().equals(elementId)) {
                this.controllerSelected = renderable;
                break;
            }
        }
    }
}
