package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.hugo99j.chaosparty.util.NinePatchLoader;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ImageUtil;

public abstract class ProgressBar extends UiElement {
    private float max;
    private float value;
    private Color color;

    public ProgressBar(String id, float max) {
        super(id);
        this.max = max;
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        NinePatch background = NinePatchLoader.getNinePatch("progress_bar_background");
        NinePatch overlay = NinePatchLoader.getNinePatch("progress_bar_overlay");

        float cornerPixelSize = 5;
        background.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getWidth() / cornerPixelSize, this.getHeight() / cornerPixelSize, cornerPixelSize, cornerPixelSize, 0);

        Color old = GameData.spriteBatch.getColor().cpy();
        if(color != null) {
            GameData.spriteBatch.setColor(color);
        }
        GameData.spriteBatch.draw(ImageUtil.get("ui/progress_bar_progress"), this.getX(), this.getY(), this.getWidth()*(value/max), this.getHeight());
        if(color != null) {
            GameData.spriteBatch.setColor(old);
        }
        overlay.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getWidth() / cornerPixelSize, this.getHeight() / cornerPixelSize, cornerPixelSize, cornerPixelSize, 0);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setMax(float max) {
        this.max = max;
    }
}
