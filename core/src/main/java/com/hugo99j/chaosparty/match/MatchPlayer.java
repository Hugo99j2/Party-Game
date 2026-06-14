package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.controllers.Controller;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.ReconnectControllersScreen;
import org.jetbrains.annotations.Nullable;

public class MatchPlayer {
    private @Nullable Player player;
    private final String name;
    public Controller controller;

    public MatchPlayer(String name) {
        this.name = name;
    }

    public void setPlayer(@Nullable Player player) {
        this.player = player;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public void tick() {
        if(this.controller == null || !this.controller.isConnected()) {
            GameData.MAIN_INSTANCE.setScreen(new ReconnectControllersScreen());
        }
    }

    public String getName() {
        return name;
    }
}
