package com.hugo99j.chaosparty.match;

import com.badlogic.gdx.controllers.Controller;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.ReconnectControllersScreen;
import org.jetbrains.annotations.Nullable;

public class MatchPlayer {
    private @Nullable Player player;
    private final User user;
    public Controller controller;

    public MatchPlayer(User user) {
        this.user = user;
    }

    public void setPlayer(@Nullable Player player) {
        this.player = player;
    }

    public @Nullable Player getPlayerObject() {
        return player;
    }

    public void tick() {
        if(this.controller == null || !this.controller.isConnected()) {
            GameData.MAIN_INSTANCE.setScreen(new ReconnectControllersScreen());
        }
    }

    public String getName() {
        return user.getName();
    }

    public User getUser() {
        return user;
    }
}
