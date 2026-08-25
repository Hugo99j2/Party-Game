package com.hugo99j.chaosparty.bot;

import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.Player;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.ObjectPathfinder;
import com.hugo99j.chaosparty.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class BotController {
    private final ObjectPathfinder pathfinder;
    private final Player player;

    public BotController(Player player) {
        this.player = player;
        this.pathfinder = new ObjectPathfinder(player);
    }

    protected void addDebugInfo(List<String> info) {}

    public void tick() {
        this.pathfinder.setSpeed(GameData.getCurrentMinigame().getPlayerSpeed(this.player.getMatchPlayer()));
        this.pathfinder.tickPathfinder();
    }

    public ObjectPathfinder getPathfinder() {
        return pathfinder;
    }

    public Player getPlayer() {
        return player;
    }

    public void render() {
        if(GameData.DEBUGGING && Debuggers.isEnabled("botDebug")) {
            List<String> text = new ArrayList<>();
            addDebugInfo(text);
            StringBuilder combined = new StringBuilder();
            for (String s : text) {
                combined.append(s).append("\n");
            }
            RenderUtil.renderTextWorld(combined.toString(), this.getPlayer().getPos().x, this.getPlayer().getPos().y, 2);
        }
    }
}
