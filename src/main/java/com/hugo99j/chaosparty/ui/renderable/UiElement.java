package com.hugo99j.chaosparty.ui.renderable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.ui.debugger.UiViewer;
import com.hugo99j.chaosparty.ui.screen.UiScreen;

import java.util.ArrayList;
import java.util.List;

public abstract class UiElement implements Disposable {
    private final String elementId;
    public boolean usesMouse = false;
    public boolean isLeftDown = false;
    public boolean isMiddleDown = false;
    public boolean isRightDown = false;
    private boolean selected;
    private UiScreen screen;
    private List<UiElement> cachedChildren;

    public UiElement(String elementId) {
        this.elementId = elementId;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean capturingMouse() {
        return false;
    }

    public boolean isInRange(int x, int y) {
        return x > this.getX() && x < this.getX()+this.getWidth() && y > this.getY() && y < this.getY()+this.getHeight();
    }

    public Vector2 getCenter() {
        return new Vector2(this.getX() + this.getWidth() / 2f, this.getY() + this.getHeight() / 2f);
    }

    public void render(RenderState state) {
        if(GameData.DEBUGGING) {
            UiViewer.activeElements.add(this);
        }
        if(usesMouse) {
            selected = isInRange(state.mouseX(), state.mouseY());
        }
    }

    public void onDown(int relativeX, int relativeY, ClickType type) {

    }

    public void onUp(int relativeX, int relativeY, ClickType type) {

    }

    public String getElementId() {
        return elementId;
    }

    public void setScreen(UiScreen screen) {
        this.screen = screen;
        this.cachedChildren = null;
    }

    public UiScreen getScreen() {
        return screen;
    }

    public abstract float getX();

    public abstract float getY();

    public abstract float getWidth();

    public abstract float getHeight();

    public UiElement getParent() {
        return null;
    }

    public List<UiElement> getChildren() {
        if(cachedChildren == null) {
            cachedChildren = new ArrayList<>();
            if(this.getScreen() != null) {
                for (UiElement uiElement : this.getScreen().getUiElements()) {
                    if(uiElement.getParent() == this) {
                        cachedChildren.add(uiElement);
                    }
                }
            }
        }
        return cachedChildren;
    }

    public CursorType getCursorOverride() {
        return null;
    }

    public void onControllerSelect() {

    }

    @Override
    public void dispose() {

    }
}
