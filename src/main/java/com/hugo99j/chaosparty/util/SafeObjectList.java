package com.hugo99j.chaosparty.util;

import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.GameData;

import java.util.ArrayList;

public class SafeObjectList extends ArrayList<AbstractObject> {
    public void ensureSafety() {
        ArrayList<AbstractObject> changedObjectButExistsUUID = new ArrayList<>();
        this.removeIf((object) -> {
            if(object == null) return true;
            if(object.isRemoved()) {
                if(GameData.level != null && GameData.level.getObjectByUUID(object.getUUID()) != null) {
                    changedObjectButExistsUUID.add(object);
                    return false;
                }
                return true;
            }
            return false;
        });

        for (AbstractObject changed : changedObjectButExistsUUID) {
            assert GameData.level != null;
            this.set(this.indexOf(changed), GameData.level.getObjectByUUID(changed.getUUID()));
        }

        //this.removeAll(changedObjectButExistsUUID);
    }
}
