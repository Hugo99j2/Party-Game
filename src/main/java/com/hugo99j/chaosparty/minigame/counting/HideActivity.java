package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.ListUtil;

import java.util.List;

public class HideActivity extends Activity {
    private Vector2 startPos;

    public HideActivity(Clown clown) {
        super(clown);
    }

    @Override
    protected Vector2 getStartPos() {
        if(startPos == null) {
            this.startPos = ListUtil.randomOf(List.of(new Vector2(15, 7), new Vector2(20, 17), new Vector2(23, 3)));
        }
        return startPos;
    }

    @Override
    protected void start(int index) {
        super.start(index);
        this.getClown().getAnimator().start("throw_hat", () -> {
            this.getClown().rerollActivity();
        });
    }
}
