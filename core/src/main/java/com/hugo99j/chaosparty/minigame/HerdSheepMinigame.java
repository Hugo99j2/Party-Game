package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.entity.Sheep;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.Timer;

import java.util.List;

public class HerdSheepMinigame extends AbstractMinigame {
    private final Timer timer;
    private final CombinedScreenSS ss = ScreenSSBuilder.create()
        .set("x", "20")
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

    public HerdSheepMinigame() {
        super("herd_sheep");
        timer = new Timer("timer", 45, 2, false);
        timer.setStyle(ss.get("timer"));
    }

    @Override
    public void tick() {
        this.defaultPlayerMovements();
        this.setScore(GameData.getCurrentMatch().getPlayers().getFirst(), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(0, 10), new Vector2(11, 18), Sheep.class).size());
        this.setScore(GameData.getCurrentMatch().getPlayers().get(1), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(21, 10), new Vector2(32, 18), Sheep.class).size());
        if (GameData.getCurrentMatch().getPlayers().size() >= 3) {
            this.setScore(GameData.getCurrentMatch().getPlayers().get(2), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(0, 0), new Vector2(11, 8), Sheep.class).size());
        }
        if (GameData.getCurrentMatch().getPlayers().size() == 4) {
            this.setScore(GameData.getCurrentMatch().getPlayers().get(3), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(21, 0), new Vector2(32, 8), Sheep.class).size());
        }

        if(timer.getSeconds() <= 0) {
            ToRun.run(() -> GameData.getCurrentMatch().finishCurrentMinigame());
        }
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        timer.render(new RenderState(false, false, false, false, false, false, 0, 0, delta));
        int offset = 0;
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            RenderUtil.renderText(player.getName()+": "+GameData.getCurrentMatch().getCurrentMinigame().getScore(player), ss.get("score").getX(), ss.get("score").getY()+offset, 1f, ss.get("score").getXSize(), Align.left, false);
            offset += 50;
        }
        RenderUtil.renderText("Scores: ", ss.get("score").getX(), ss.get("score").getY()+offset, 1f, ss.get("score").getXSize(), Align.left, false);
        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {

    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.FOUR_CORNERS;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        matchViews.add(new MatchView(32, 18));
    }
}
