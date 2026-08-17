package com.hugo99j.chaosparty.ui.element;

import com.hugo99j.chaosparty.ui.renderable.UiElement;

public abstract class DivElement extends UiElement implements PaddingSettings {
    public DivElement(String elementId) {
        super(elementId);
    }

    @Override
    public boolean isSelected() {
        return false;
    }
}
