package com.hugo99j.chaosparty.minigame.counting;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.entity.Clown;
import com.hugo99j.chaosparty.util.ListUtil;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;

import java.util.List;

public class TurnIntoRabbitActivity extends Activity {
    private Vector2 startPos;
    private Vector2 velocity = new Vector2();

    public TurnIntoRabbitActivity(Clown clown) {
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
        this.getClown().getLevel().addParticle("magic_poof", this.getClown().getPos().add(0.5f,0.5f));
        this.getClown().getAnimator().start("rabbit");
    }

    @Override
    protected void tickDuring() {
        super.tickDuring();
        if(NumberUtils.getRandomInt(1, 400) == 1) {
            this.getClown().getAnimator().stop();
            this.getClown().rerollActivity();
            this.getClown().getLevel().addParticle("magic_poof", this.getClown().getPos().add(0.5f,0.5f));
        }
        this.getClown().setVelocity(velocity);
        if(NumberUtils.getRandomInt(1, 40) == 1) {
            float speed = 7;
            velocity = new Vector2(NumberUtils.getRandomFloat(-speed, speed), NumberUtils.getRandomFloat(-speed, speed));
        }
    }

    @Override
    protected boolean resetVelocity() {
        return false;
    }
}
