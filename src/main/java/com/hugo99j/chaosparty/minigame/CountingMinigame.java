package com.hugo99j.chaosparty.minigame;

import com.hugo99j.chaosparty.sounds.SoundInstance;
import com.hugo99j.chaosparty.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.ControllerInput;
import com.hugo99j.chaosparty.util.ControllerUtil;
import com.hugo99j.chaosparty.ui.ScreenCenterer;

import java.util.List;

public class CountingMinigame extends AbstractMinigame {
    private SoundInstance music;

    public CountingMinigame() {
        super("counting");
    }

    @Override
    public boolean shouldAutoCenterCameras() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        music = SoundManager.getSound("sheep_music").playSingle(1);
    }

    @Override
    public void tick() {
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if (player.controller == null || player.getPlayerObject() == null) continue;
            if (((ControllerUtil) player.controller).wasJustPressedThisTick(ControllerInput.LEFT_BUMPER)) {
                this.addScore(player, -1);
            }
            if (((ControllerUtil) player.controller).wasJustPressedThisTick(ControllerInput.RIGHT_BUMPER)) {
                this.addScore(player, 1);
            }
        }
    }

    @Override
    public void dispose() {
        music.fade(1, 0);
    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.SINGLE;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        matchViews.add(new MatchView(40, 22));
        ((ScreenCenterer) matchViews.getFirst().gameViewport).party_Game$setCenter(true);
    }

    @Override
    public float getPlayerSpeed(MatchPlayer player) {
        return 100;
    }
}
