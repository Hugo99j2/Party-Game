package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.utils.Align;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.Timer;
import com.hugo99j.chaosparty.ui.WinScreen;

import java.util.List;

public class DevMinigame extends AbstractMinigame {
    private Timer timer;
    private CombinedScreenSS ss = ScreenSSBuilder.create()
        .set("x", "0.1vw")
        .set("y", "0.1vh")
        .set("xSize", "0.02vw")
        .set("ySize", "0.02vh")
        .newChild("timer")
        .set("x", "5%")
        .set("y", "0")
        .set("xSize", "95%")
        .set("ySize", "40%")
        .finishChild()
        .newChild("score")
        .set("x", "5%")
        .set("y", "40%+20%")
        .set("xSize", "95%")
        .set("ySize", "40%")
        .finishChild()
        .build();

    public DevMinigame() {
        super("dev");
        timer = new Timer("timer", 10, 2, false);
        timer.setStyle(ss.get("timer"));
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
        RenderUtil.renderText("Score: "+this.getScore(0), ss.get("score").getX(), ss.get("score").getY(), 1f, 100, Align.left, false);
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
