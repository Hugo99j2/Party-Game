package com.hugo99j.chaosparty.match;

import com.hugo99j.chaosparty.entity.Player;
import org.jetbrains.annotations.Nullable;

public class MatchPlayer {
    private @Nullable Player player;
    private final String name;

    public MatchPlayer(String name) {
        this.name = name;
    }
}
