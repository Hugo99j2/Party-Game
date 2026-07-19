package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.daniel99j.dungeongame.ui.NinePatchLoader;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.RenderUtil;

public class ProgressBar extends Renderable {
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
        background.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getStyle().getXSize() / cornerPixelSize, this.getStyle().getYSize() / cornerPixelSize, cornerPixelSize, cornerPixelSize, 0);

        Color old = GameData.spriteBatch.getColor().cpy();
        if(color != null) {
            GameData.spriteBatch.setColor(color);
        }
        GameData.spriteBatch.draw(ImageUtil.get("ui/progress_bar_progress"), this.getX(), this.getY(), this.getStyle().getXSize()*(value/max), this.getStyle().getYSize());
        if(color != null) {
            GameData.spriteBatch.setColor(old);
        }
        overlay.draw(GameData.spriteBatch, this.getX(), this.getY(), 0, 0, this.getStyle().getXSize() / cornerPixelSize, this.getStyle().getYSize() / cornerPixelSize, cornerPixelSize, cornerPixelSize, 0);
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
