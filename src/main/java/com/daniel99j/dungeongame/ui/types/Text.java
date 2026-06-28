package com.daniel99j.dungeongame.ui.types;

import com.badlogic.gdx.utils.Align;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.hugo99j.chaosparty.util.RenderUtil;

public class Text extends Renderable {
    private final String text;

    public Text(String id, String text) {
        super(id);
        this.text = text;
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        RenderUtil.renderText(this.text, this.getX(), this.getY()+this.getStyle().getYSize()/2, 1f, this.getStyle().getXSize(), Align.center, false);
    }
}
