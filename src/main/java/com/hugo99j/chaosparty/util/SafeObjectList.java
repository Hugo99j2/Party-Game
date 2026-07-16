package com.hugo99j.chaosparty.util;

import com.daniel99j.dungeongame.entity.AbstractObject;

import java.util.ArrayList;

public class SafeObjectList extends ArrayList<AbstractObject> {
    public void ensureSafety() {
        this.removeIf(AbstractObject::isRemoved);
    }
}
