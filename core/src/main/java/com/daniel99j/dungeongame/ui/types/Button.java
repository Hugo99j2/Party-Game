package com.daniel99j.dungeongame.ui.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.utils.Align;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.ui.NinePatchLoader;
import com.daniel99j.dungeongame.ui.renderable.ClickType;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.hugo99j.chaosparty.util.RenderUtil;

public class Button extends Renderable {
    private NinePatch ninePatch;
    private NinePatch ninePatchHovered;
    private String text;

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
        NinePatch patch = this.isHovered() ? this.ninePatchHovered : this.ninePatch;
        patch.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getStyle().getXSize() / (float) this.getStyle().get("scale"), this.getStyle().getYSize() / (float) this.getStyle().get("scale"), (float) this.getStyle().get("scale"), (float) this.getStyle().get("scale") ,0);
        if(!this.text.isBlank()) RenderUtil.renderText(this.text, this.getX(), (int) (this.getY()+this.getStyle().getYSize()/1.5f), 1f, this.getStyle().getXSize(), Align.center, false);
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
        return this.isHovered() ? CursorType.HAND_POINT : null;
    }
}
