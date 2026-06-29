package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.entity.Sheep;
import com.hugo99j.chaosparty.entity.TilesetObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.Timer;

import java.util.List;

public class HerdSheepMinigame extends AbstractMinigame {
    private Timer timer;
    private final CombinedScreenSS ss = ScreenSSBuilder.create()
        .set("xSize", "1vw")
        .set("ySize", "1vh")
        .set("x", 0)
        .set("y", 0)
        .newChild("rightcorner")
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
        .finishChild()
        .build();
    private SoundInstance music;

    public HerdSheepMinigame() {
        super("herd_sheep");
    }

    @Override
    public void start() {
        super.start();
        timer = new Timer("timer", 45, 2, false);
        timer.setStyle(ss.get("timer"));
        music = SoundManager.getSound("sheep_music").playSingle(1);

        for (TilesetObject o : GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(1, 9), new Vector2(11, 17), TilesetObject.class, false)) {
            o.setTint(GameData.getCurrentMatch().getPlayers().getFirst().getUser().getColour());
        }
        for (TilesetObject o : GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(20, 9), new Vector2(31, 17), TilesetObject.class, false)) {
            o.setTint(GameData.getCurrentMatch().getPlayers().get(1).getUser().getColour());
        }
        if (GameData.getCurrentMatch().getPlayers().size() >= 3) {
            for (TilesetObject o : GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(1, 1), new Vector2(11, 8), TilesetObject.class, false)) {
                o.setTint(GameData.getCurrentMatch().getPlayers().get(2).getUser().getColour());
            }
        }
        if (GameData.getCurrentMatch().getPlayers().size() == 4) {
            for (TilesetObject o : GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(20, 1), new Vector2(31, 8), TilesetObject.class, false)) {
                o.setTint(GameData.getCurrentMatch().getPlayers().get(3).getUser().getColour());
            }
        }
    }

    @Override
    public void tick() {
        this.defaultPlayerMovements();
        this.setScore(GameData.getCurrentMatch().getPlayers().getFirst(), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(1, 10), new Vector2(11, 17), Sheep.class, true).size());
        this.setScore(GameData.getCurrentMatch().getPlayers().get(1), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(21, 10), new Vector2(31, 17), Sheep.class, true).size());
        if (GameData.getCurrentMatch().getPlayers().size() >= 3) {
            this.setScore(GameData.getCurrentMatch().getPlayers().get(2), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(1, 1), new Vector2(11, 8), Sheep.class, true).size());
        }
        if (GameData.getCurrentMatch().getPlayers().size() == 4) {
            this.setScore(GameData.getCurrentMatch().getPlayers().get(3), GameData.getLevelOrThrow().getObjectsBetweenClass(new Vector2(21, 1), new Vector2(31, 8), Sheep.class, true).size());
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
        //RenderUtil.renderText("Scores: ", ss.get("score").getX(), ss.get("score").getY()+offset, 1f, ss.get("score").getXSize(), Align.left, false);
        RenderUtil.renderText("Scores: ", ss.get("score"));
        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {
        music.fade(1, 0);
    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.FOUR_CORNERS;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        matchViews.add(new MatchView(32, 18));
    }

    @Override
    public void setPaused(boolean paused) {
        if(paused) music.pause();
        else music.play();
    }
}
