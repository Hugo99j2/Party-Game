package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.entity.Clown;

public class WalkAroundActivity extends Activity {
    public WalkAroundActivity(Clown clown) {
        super(clown);
    }

    @Override
    protected Vector2 getStartPos() {
        return new Vector2(NumberUtils.getRandomInt(0, 20), NumberUtils.getRandomInt(0, 20));
    }

    @Override
    public void tick() {
        super.tick();
        if(NumberUtils.getRandomInt(0, 100) == 0) this.getClown().rerollActivity();
    }

    @Override
    protected void tickDuring() {
        super.tickDuring();
        this.getClown().rerollActivity();
    }
}
