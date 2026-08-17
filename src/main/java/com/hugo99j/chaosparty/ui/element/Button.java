package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.ui.screenss.CombinedScreenSS;
import com.hugo99j.chaosparty.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.NinePatchLoader;
import com.hugo99j.chaosparty.ui.renderable.ClickType;
import com.hugo99j.chaosparty.ui.renderable.CursorType;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.RenderUtil;

import static com.hugo99j.chaosparty.GameData.px;

public abstract class Button extends UiElement {
    private NinePatch ninePatch;
    private NinePatch ninePatchHovered;
    private String text;
    private CombinedScreenSS.ScreenParentSS screenParentSS;

    public Button(String elementId, String texture, String text) {
        super(elementId);
        this.text = text;
        this.ninePatch = NinePatchLoader.getNinePatch(texture.replace(".png", ""));
        this.ninePatchHovered = NinePatchLoader.getNinePatch(texture.replace(".png", "")+"_hovered");
        this.usesMouse = true;
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        GameData.spriteBatch.setColor(Color.WHITE);
        NinePatch patch = this.isSelected() ? this.ninePatchHovered : this.ninePatch;
        float cornerPixelSize = 8;
        patch.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getWidth() / cornerPixelSize, this.getHeight() / cornerPixelSize, cornerPixelSize, cornerPixelSize,0);
        if(!this.text.isBlank()) RenderUtil.renderText(this.text, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    public void onDown(int relativeX, int relativeY, ClickType type) {
        if(type == ClickType.LEFT) onClick();
    }

    public void onClick() {
        String sound = getSound();
        if(!sound.isEmpty()) SoundManager.getSound(sound).play(1);
    }

    public String getSound() {
        return "click";
    }

    @Override
    public CursorType getCursorOverride() {
        return this.isSelected() ? CursorType.HAND_POINT : null;
    }
}
