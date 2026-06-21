package com.daniel99j.dungeongame.ui.renderable;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.ui.UiScreen;
import com.daniel99j.dungeongame.ui.screenss.ScreenSS;

public class Renderable {
    private final String elementId;
    public boolean usesMouse = false;
    public boolean isLeftDown = false;
    public boolean isMiddleDown = false;
    public boolean isRightDown = false;
    private boolean hovered;
    private ScreenSS style;

    public Renderable(String elementId) {
        this.elementId = elementId;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean capturingMouse() {
        return false;
    }

    public boolean isInRange(int x, int y) {
        return x > this.getX() && x < this.getX()+this.getStyle().getXSize() && y > this.getY() && y < this.getY()+this.getStyle().getYSize();
    }

    public Vector2 getCenter() {
        return new Vector2(this.getX() + this.getStyle().getXSize() / 2f, this.getY() + this.getStyle().getYSize() / 2f);
    }

    public void render(RenderState state) {
        if(usesMouse) {
            hovered = false;
            if(isInRange(state.mouseX(), state.mouseY())) {
                hovered = true;
                if (!isLeftDown && state.leftJust()) {
                    onDown(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.LEFT);
                    isLeftDown = true;
                }
                if (!isMiddleDown && state.middleJust()) {
                    onDown(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.MIDDLE);
                    isMiddleDown = true;
                }
                if (!isRightDown && state.rightJust()) {
                    onDown(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.RIGHT);
                    isRightDown = true;
                }
            }
            if (isLeftDown && !state.left()) {
                onUp(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.LEFT);
                isLeftDown = false;
            }
            if (isMiddleDown && !state.middle()) {
                onUp(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.MIDDLE);
                isMiddleDown = false;
            }
            if (isRightDown && !state.right()) {
                onUp(state.mouseX() - this.getX(), state.mouseY() - this.getY(), ClickType.RIGHT);
                isRightDown = false;
            }
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
        this.setStyle(screen.getStyle().get(this.elementId));
    }

    public void setStyle(ScreenSS style) {
        this.style = style;
    }

    public ScreenSS getStyle() {
        return style;
    }

    public int getX() {
        return this.style.getX();
    }

    public int getY() {
        return this.style.getY();
    }

    public CursorType getCursorOverride() {
        return null;
    }

    public void onControllerSelect() {

    }
}
