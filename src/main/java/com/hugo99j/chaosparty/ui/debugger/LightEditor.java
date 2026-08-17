package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.dungeongame.level.LevelLight;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.MapEditor;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.hugo99j.chaosparty.ui.debugger.Debuggers.*;

public class LightEditor {
    public static void onAdded(LevelLight<?> levelLight) {
    }

    protected static void render(SelectedLightObject shadow) {
    }

    protected static class SelectedLightObject extends TemporaryDevObject {
        @Override
        public void render(MatchView matchView) {
            GameData.spriteBatch.draw(ImageUtil.get("lightbulb"), this.getPos().x, this.getPos().y);
        }

        @Override
        public String toString() {
            return "Light";
        }

        @Override
        public boolean keep() {
            return false;
        }
    }
}
