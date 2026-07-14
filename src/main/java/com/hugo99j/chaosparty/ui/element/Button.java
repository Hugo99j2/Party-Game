package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.ui.NinePatchLoader;
import com.daniel99j.dungeongame.ui.renderable.ClickType;
import com.daniel99j.dungeongame.ui.renderable.CursorType;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.hugo99j.chaosparty.util.RenderUtil;

import static com.hugo99j.chaosparty.GameData.px;

public class Button extends Renderable {
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
        if(screenParentSS == null) {
            screenParentSS = CombinedScreenSS.phantomChild(this.getStyle(), ScreenSSBuilder.create()
                .set("x", "50%")
                .set("y", "50%")
                .set("xSize", this.getStyle().getXSize())
                .set("ySize", "100%-35")
                .set("center", true)
                .set("textAlign", 0.5f)
            );
        }
        GameData.spriteBatch.setColor(Color.WHITE);
        NinePatch patch = this.isSelected() ? this.ninePatchHovered : this.ninePatch;
        float cornerPixelSize = 5;
        patch.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getStyle().getXSize() / cornerPixelSize, this.getStyle().getYSize() / cornerPixelSize, cornerPixelSize, cornerPixelSize,0);
        if(!this.text.isBlank()) RenderUtil.renderText(this.text, screenParentSS);
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
