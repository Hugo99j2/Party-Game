package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.ListUtil;

import java.util.List;

public class HideActivity extends Activity {
    private final Vector2 startPos;

    public HideActivity(Clown clown) {
        super(clown);
        this.startPos = ListUtil.randomOf(List.of(new Vector2(15, 7), new Vector2(20, 17), new Vector2(23, 3)));
    }

    @Override
    protected Vector2 getStartPos() {
        return startPos;
    }

    @Override
    protected void start(int index) {
        super.start(index);
        this.getClown().getAnimator().start("juggle", () -> {
            this.getClown().rerollActivity();
        });
    }
}
