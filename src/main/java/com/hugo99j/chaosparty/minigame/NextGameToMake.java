package com.hugo99j.chaosparty.minigame;

import com.daniel99j.dungeongame.sounds.SoundInstance;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.match.MatchView;

import java.util.List;

public class NextGameToMake extends AbstractMinigame {
    private SoundInstance music;

    public NextGameToMake() {
        super("new_game");
    }

    @Override
    public boolean shouldAutoCenterCameras() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        music = SoundManager.getSound("yay").playSingle(1);
    }

    @Override
    public void tick() {

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
        matchViews.add(new MatchView(32, 18));
    }
}
