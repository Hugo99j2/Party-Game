package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;

public class PyramidActivity extends Activity {
    public PyramidActivity(Clown clown) {
        super(clown);
    }

    @Override
    protected Vector2 getStartPos() {
        return new Vector2(13, 15);
    }

    @Override
    protected void start(int index) {
        super.start(index);
        if(index == 0) this.getClown().setPos(this.getStartPos());
        else if(index == 1) this.getClown().setPos(this.getStartPos().add(1, 0));
        else if(index == 2) this.getClown().setPos(this.getStartPos().add(0.5f, 1));
        this.getClown().getAnimator().start("juggle", () -> {
            this.getClown().rerollActivity();
        });
    }

    @Override
    protected int amountNeededToStart() {
        return 3;
    }
}
