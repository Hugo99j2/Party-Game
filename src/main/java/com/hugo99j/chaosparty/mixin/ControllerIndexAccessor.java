package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.controllers.ControllerMapping;
import com.hugo99j.chaosparty.ui.debugger.DebugController;
import com.hugo99j.chaosparty.util.DummyController;
import com.studiohartman.jamepad.ControllerIndex;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ControllerIndex.class)
public class ControllerIndexAccessor {
    public ControllerIndexAccessor(int index) {
    }

    static {
        DebugController.DUMMY_INDEX = (ControllerIndex) (Object) new ControllerIndexAccessor(0);
    }
}
