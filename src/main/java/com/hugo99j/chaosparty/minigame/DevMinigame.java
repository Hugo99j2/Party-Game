package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.utils.Align;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.element.Timer;

import java.util.List;

public class DevMinigame extends AbstractMinigame {
    private Timer timer;

    public DevMinigame() {
        super("dev");
        timer = new Timer("timer", 10, 2, false) {
            @Override
            public float getX() {
                return 0;
            }

            @Override
            public float getY() {
                return 0;
            }

            @Override
            public float getWidth() {
                return 0;
            }

            @Override
            public float getHeight() {
                return 0;
            }
        };
    }

    @Override
    public void tick() {
        if(timer.getSeconds() <= 0) {
            ToRun.run(() -> {
                GameData.getCurrentMatch().finishCurrentMinigame();
            });
        }
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        timer.render(new RenderState(false, false, false, false, false, false, 0, 0, delta));
        //RenderUtil.renderText("Score: 1", (int) ss.get("score").getX(), (int) ss.get("score").getY(), 1f, 100, Align.left, false);
        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {

    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.HALF_HALF;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        matchViews.add(new MatchView(16, 9));
    }
}
