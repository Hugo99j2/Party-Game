package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;

public class JuggleActivity extends Activity {
    public JuggleActivity(Clown clown) {
        super(clown);
    }

    @Override
    protected Vector2 getStartPos() {
        return new Vector2(31, 8);
    }

    @Override
    protected void start(int index) {
        super.start(index);
        this.getClown().getAnimator().start("juggle", () -> {
            this.getClown().rerollActivity();
        });
    }
}
