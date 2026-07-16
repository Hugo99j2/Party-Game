package com.hugo99j.chaosparty.util;

import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.GameData;

import java.util.ArrayList;

public class SafeObjectHolder {
    private AbstractObject held;

    public void set(AbstractObject held) {
        this.held = held;
    }

    public AbstractObject get() {
        if(this.held == null) return null;
        if(held.isRemoved() && GameData.level != null && GameData.level.getObjectByUUID(held.getUUID()) != null) {
            set(GameData.level.getObjectByUUID(held.getUUID()));
        } else if(held.isRemoved()) {
            set(null);
        }
        return held;
    }
}
