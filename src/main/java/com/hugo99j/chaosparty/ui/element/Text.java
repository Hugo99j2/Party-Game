package com.hugo99j.chaosparty.ui.element;

import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.RenderUtil;

public abstract class Text extends UiElement {
    private final String text;

    public Text(String id, String text) {
        super(id);
        this.text = text;
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        RenderUtil.renderText(this.text, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
