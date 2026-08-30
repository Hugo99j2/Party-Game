package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.maths.MathsContext;
import com.hugo99j.chaosparty.sounds.SoundManager;
import com.hugo99j.chaosparty.util.*;
import com.hugo99j.chaosparty.ui.renderable.ClickType;
import com.hugo99j.chaosparty.ui.renderable.CursorType;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UiScreen implements Screen {
    private final ArrayList<UiElement> uiElements = new ArrayList<>();
    private UiElement controllerSelected;
    private boolean firstFrame = true;

    public UiScreen() {
    }

    public <T extends UiElement> T addElement(T uiElement) {
        this.uiElements.add(uiElement);
        uiElement.setScreen(this);
        return uiElement;
    }

    public ArrayList<UiElement> getUiElements() {
        return uiElements;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(firstFrame) {
            firstFrame = false;
            if(ControllerUtil.getCurrent() != null && controllerSelected == null) {
                controllerStick(Vector2.Zero);
            }
        }
        if(ControllerUtil.getCurrent() != null && RenderUtil.isFocused()) {
            if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.LEFT_STICK_ANY)) {
                Vector2 controllerStickMove = new Vector2(ControllerUtil.getCurrent().getValue(ControllerInput.LEFT_STICK_RIGHT), ControllerUtil.getCurrent().getValue(ControllerInput.LEFT_STICK_UP));
                if (controllerStickMove.len() > 0) controllerStick(controllerStickMove);
            }

            if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.A) && controllerSelected != null) {
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
        for (UiElement uiElement : this.uiElements) {
            if(uiElement == controllerSelected) uiElement.render(new RenderState(state.left(), state.leftJust(), state.middle(), state.middleJust(), state.right(), state.rightJust(), (int) controllerSelected.getCenter().x, (int) controllerSelected.getCenter().y, state.time()));
            else uiElement.render(state);
        }

        if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.LEFT_STICK_BUTTON)) {
            ControllerUtil.getCurrent().vibrate(VibrationAmount.of((t) -> {
                if(t <= 6.3f) return (float) (0.5f+Math.sin(t-1.6f)/2f);
                return 0f;
            }, (t) -> 0f), 6.3f);
        }
        if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.RIGHT_STICK_BUTTON)) {
            ControllerUtil.getCurrent().vibrate(VibrationAmount.of((t) -> 0f, (t) -> {
                if(t <= 6.3f) return (float) (0.5f+Math.sin(t-1.6f)/2f);
                return 0f;
            }), 6.3f);
        }
        if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.X)) {
            ControllerUtil.getCurrent().vibrate(VibrationAmount.of((t) -> {
                if(t <= 6.3f) return (float) (0.5f+Math.sin(t-1.6f)/2f);
                return 0f;
            }, (t) -> {
                if(t > 7f) return (float) (0.5f+Math.sin(t-1.6f-5f)/2f);
                return 0f;
            }), 13.3f);
        }
        if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.B)) {
            ControllerUtil.getCurrent().vibrate(VibrationAmount.of(new float[]{0.0f,1.0f,2.0f,1.0f,2.0f,0.0f}, new float[]{0.0f,1.0f,2.0f,1.0f,2.0f,0.0f}));
        }
    }

    protected void controllerStick(Vector2 change) {
        if(ControllerUtil.getCurrent() != null) {
            Vector2 pos = Vector2.Zero.cpy();
            if(controllerSelected != null) {
                pos = controllerSelected.getCenter();
            }

            Map<Vector4, UiElement> selectors = new HashMap<>();
            for (UiElement uiElement : uiElements) {
                if(uiElement.usesMouse && uiElement != controllerSelected) {
                    selectors.put(new Vector4(uiElement.getX(), uiElement.getY(), uiElement.getX()+uiElement.getWidth(), uiElement.getY()+uiElement.getHeight()), uiElement);
                    if(controllerSelected == null) {
                        controllerSelected = uiElement;
                        controllerSelected.onControllerSelect();
                        return;
                    }
                }
            }

            for (int i = 0; i < 1000; i++) {
                pos.add(change);
                if(GameData.DEBUGGING && Debuggers.isEnabled("showControllerSelect")) {
                    ValueHolder<Vector2> valueHolder = new ValueHolder<>(pos.cpy());
                    Debuggers.customUiRenderers.put(() -> {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.BLUE);
                        GameData.shapeRenderer.circle(valueHolder.object.x, valueHolder.object.y, 10);
                        GameData.shapeRenderer.end();
                    }, new ValueHolder<>(10));
                }
                for (Vector4 aabb : selectors.keySet()) {
                    if(pos.x >= aabb.x && pos.y >= aabb.y && pos.x <= aabb.z && pos.y <= aabb.w) {
                        controllerSelected = selectors.get(aabb);
                        controllerSelected.onControllerSelect();
                        ControllerUtil.getCurrent().vibrate(0.1f, 0.1f);

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
        this.uiElements.forEach(UiElement::dispose);
        this.uiElements.clear();
        this.controllerSelected = null;
    }

    public boolean isUsingMouse() {
        for (UiElement uiElement : this.uiElements) {
            if(uiElement.usesMouse) return true;
        }
        return false;
    }

    public CursorType getCursorType() {
        for (UiElement uiElement : this.uiElements) {
            CursorType c = uiElement.getCursorOverride();
            if(uiElement.usesMouse && c != null) return c;
        }
        return null;
    }

    public void setControllerSelected(String elementId) {
        for (UiElement uiElement : uiElements) {
            if(uiElement.usesMouse && uiElement.getElementId().equals(elementId)) {
                this.controllerSelected = uiElement;
                return;
            }
        }
        throw new IllegalArgumentException("No element with id " + elementId);
    }

    public String getControllerSelectedId() {
        if(this.controllerSelected == null) return "";
        return this.controllerSelected.getElementId();
    }

    public UiElement getControllerSelected() {
        return this.controllerSelected;
    }

    public void editSSContext(MathsContext context) {
    }
}
