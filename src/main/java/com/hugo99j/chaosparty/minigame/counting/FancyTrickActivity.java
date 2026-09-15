package com.hugo99j.chaosparty.minigame.counting;

import box2dLight.RayHandler;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.level.LevelLight;
import com.hugo99j.chaosparty.util.ListUtil;

import java.util.List;
import java.util.UUID;

public class FancyTrickActivity extends Activity {
    public static boolean ACTIVE = false;
    private LevelLight<?> spotlight = null;

    public FancyTrickActivity(Clown clown) {
        super(clown);
        if(ACTIVE) {
            clown.rerollActivity();
            return;
        }
        ACTIVE = true;
    }

    @Override
    protected Vector2 getStartPos() {
        return new Vector2(10, 10);
    }

    @Override
    protected void start(int index) {
        super.start(index);
        RayHandler.useDiffuseLight(true);
        this.getClown().getLevel().getLightByUUID(UUID.fromString("f4e2fd4a-dbe1-46ba-a501-53915daef99c")).light().setActive(false);
        this.spotlight = this.getClown().getLevel().getLightByUUID(UUID.fromString("cbe11689-c161-4acc-b4c7-8cd35f73a1cb"));
        this.spotlight.light().setActive(true);
        this.getClown().getAnimator().start("throw_hat", () -> {
            this.getClown().rerollActivity();
            RayHandler.useDiffuseLight(false);
            this.spotlight.light().setActive(false);
            ACTIVE = false;
            this.getClown().getLevel().getLightByUUID(UUID.fromString("f4e2fd4a-dbe1-46ba-a501-53915daef99c")).light().setActive(true);
        });
    }

    @Override
    protected void tickDuring() {
        super.tickDuring();
        this.spotlight.light().setPosition(this.getClown().getPos());
    }
}
