package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.graphics.Color;
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
        GameData.spriteBatch.draw(ImageUtil.get("ui/progress_bar_background"), this.getX(), this.getY(), this.getStyle().getXSize(), this.getStyle().getYSize());
        Color old = GameData.spriteBatch.getColor().cpy();
        if(color != null) {
            GameData.spriteBatch.setColor(color);
        }
        GameData.spriteBatch.draw(ImageUtil.get("ui/progress_bar_progress"), this.getX(), this.getY(), this.getStyle().getXSize()*(value/max), this.getStyle().getYSize());
        if(color != null) {
            GameData.spriteBatch.setColor(old);
        }
        GameData.spriteBatch.draw(ImageUtil.get("ui/progress_bar_overlay"), this.getX(), this.getY(), this.getStyle().getXSize(), this.getStyle().getYSize());
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
