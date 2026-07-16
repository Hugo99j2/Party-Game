package com.hugo99j.chaosparty.bot;

import com.badlogic.gdx.graphics.Color;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.ValueHolder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.FallingFloorObject;
import com.hugo99j.chaosparty.entity.LiquidBarrelObject;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.entity.Potato;
import com.hugo99j.chaosparty.minigame.FallingFloorMinigame;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;

public class FallingFloorBot extends BotController {
    private Color lastValid = Color.BLACK;

    public FallingFloorBot(Player player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        Color realValid = ((FallingFloorMinigame) GameData.getCurrentMinigame()).getSafeColour();
        if(!lastValid.equals(realValid)) {
            lastValid = realValid;
            for (FallingFloorObject objectsInRadius : GameData.getLevelOrThrow().getObjectsInRadius(this.getPlayer().getPos(), 10, FallingFloorObject.class, false, null)) {
                if(objectsInRadius.getColour().equals(realValid)) {
                    setTarget(objectsInRadius.getPos().add(1, 1));
                    break;
                }
            }
        }
    }

    @Override
    public float getMaxDistance() {
        return 1;
    }
}
