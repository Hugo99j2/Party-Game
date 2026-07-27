package com.daniel99j.dungeongame.ui.renderable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.hugo99j.chaosparty.ui.UiScreen;

public class Renderable implements Disposable {
    private final String elementId;
    public boolean usesMouse = false;
    public boolean isLeftDown = false;
    public boolean isMiddleDown = false;
    public boolean isRightDown = false;
    private boolean selected;
    private CombinedScreenSS.ScreenParentSS style;
    private UiScreen screen;

    public Renderable(String elementId) {
        this.elementId = elementId;
    }

    public boolean isSelected() {
        return selected;
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
        this.setStyle(screen.getStyle().get(this.elementId));
        this.screen = screen;
    }

    public UiScreen getScreen() {
        return screen;
    }

    public void setStyle(CombinedScreenSS.ScreenParentSS style) {
        this.style = style;
    }

    public CombinedScreenSS.ScreenParentSS getStyle() {
        return style;
    }

    public float getX() {
        return this.style.getX();
    }

    public float getY() {
        return this.style.getY();
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
