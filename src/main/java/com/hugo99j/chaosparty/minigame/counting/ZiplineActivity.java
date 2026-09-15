package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;

public class ZiplineActivity extends Activity {
    public ZiplineActivity(Clown clown) {
        super(clown);
    }

    @Override
    protected Vector2 getStartPos() {
        return new Vector2(30.5f, 12.375f);
    }

    private Vector2 getEndPos() {
        return new Vector2(15.938f, 10.625f);
    }

    @Override
    protected void start(int index) {
        super.start(index);
    }

    @Override
    protected void tickDuring() {
        super.tickDuring();
        int maxTime = 100;
        this.getClown().setPos(new Vector2(Interpolation.smooth.apply(getStartPos().x, getEndPos().x, (float) this.getTicksPassed() /maxTime), Interpolation.smooth.apply(getStartPos().y, getEndPos().y, (float) this.getTicksPassed() /maxTime)));
        if(this.getTicksPassed() > maxTime) {
            this.getClown().rerollActivity();
        }
    }
}
