package com.hugo99j.chaosparty.ui.debugger;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.daniel99j.dungeongame.level.Level;
import com.daniel99j.dungeongame.level.LevelLight;
import com.daniel99j.dungeongame.level.LevelLoader;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.entity.TemporaryDevObject;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.util.ImageUtil;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.*;

import java.util.ArrayList;
import java.util.UUID;

import static com.hugo99j.chaosparty.ui.debugger.Debuggers.*;

public class LightEditor {
    public static void onAdded(LevelLight<?> levelLight, Level level) {
        if(!GameData.DEBUGGING) return;
        SelectedLightObject selectedLightObject = new SelectedLightObject(levelLight);
        level.addObject(selectedLightObject);
    }

    public static void onRemoved(LevelLight<?> levelLight, Level level) {
        if(!GameData.DEBUGGING) return;
        AbstractObject o = level.getObjectByUUID(levelLight.uuid());
        if(o != null) o.dispose();
    }

    protected static void render(SelectedLightObject shadow) {
        LevelLight<?> selectedLight = shadow.getLevelLight();

        if(selectedLight == null) {
            ImGui.text("Error (light is null)");
            return;
        }

        float[] colours = {
            selectedLight.light().getColor().r,
            selectedLight.light().getColor().g,
            selectedLight.light().getColor().b,
            selectedLight.light().getColor().a
        };
        if (ImGui.colorPicker4("Colour", colours)) {
            selectedLight.light().setColor(colours[0], colours[1], colours[2], colours[3]);
        }

        if (ImGui.checkbox("X-Ray", selectedLight.light().isXray())) {
            selectedLight.light().setXray(!selectedLight.light().isXray());
        }

        if (ImGui.checkbox("Static", selectedLight.light().isStaticLight())) {
            selectedLight.light().setStaticLight(!selectedLight.light().isStaticLight());
        }

        if (ImGui.checkbox("Soft", selectedLight.light().isSoft())) {
            selectedLight.light().setSoft(!selectedLight.light().isSoft());
        }

        if (ImGui.checkbox("Active", selectedLight.light().isActive())) {
            selectedLight.light().setActive(!selectedLight.light().isActive());
        }

        slider("Softness", selectedLight.light().getSoftShadowLength(), selectedLight.light()::setSoftnessLength, 0, 5, "%.3f");

        slider("Distance", selectedLight.light().getDistance(), selectedLight.light()::setDistance, 0, 100, "%.3f");

        if (selectedLight.light() instanceof ConeLight coneLight) {
            ImGui.separatorText("Cone Light");

            slider("Direction", selectedLight.light().getDirection(), selectedLight.light()::setDirection, 0, 360, "%.0f");

            slider("Cone size", coneLight.getConeDegree(), coneLight::setConeDegree, 0, 180, "%.3f");
        }

        if (selectedLight.light() instanceof DirectionalLight) {
            ImGui.separatorText("Directional Light");

            slider("Direction", selectedLight.light().getDirection(), selectedLight.light()::setDirection, 0, 360, "%.0f");
        }
    }

    public static class SelectedLightObject extends TemporaryDevObject {
        private final JsonObject light;
        private final LevelLight<?> levelLight;
        private boolean isAllowedToChange = false;

        public SelectedLightObject(LevelLight<?> light) {
            super();
            this.light = null;
            this.levelLight = light;
        }

        public SelectedLightObject(JsonObject light) {
            super();
            this.light = light;
            this.levelLight = null;
        }

        @Override
        public void onAdd(boolean fromLoad) {
            super.onAdd(fromLoad);
            if(light != null) {
                LevelLoader.createLight(light, this.getLevel());
                this.dispose();
            }
        }

        @Override
        public Vector2 getPos() {
            return levelLight == null ? new Vector2() : levelLight.light().getPosition().cpy();
        }

        @Override
        public void setPos(Vector2 pos) {
            if(isAllowedToChange && levelLight != null) levelLight.light().setPosition(pos.cpy());
        }

        @Override
        public void render(MatchView matchView) {
            GameData.spriteBatch.draw(ImageUtil.get("bricks"), this.getPos().x, this.getPos().y, 1, 1);
            GameData.spriteBatch.draw(ImageUtil.get("lightbulb"), this.getPos().x-0.5f, this.getPos().y-0.5f, 1, 1);
            isAllowedToChange = true;
        }

        @Override
        public String toString() {
            return "Light";
        }

        @Override
        public void dispose() {
            this.levelLight.dispose();
            super.dispose();
        }

        public LevelLight<?> getLevelLight() {
            return levelLight;
        }

        @Override
        public void writeAdditional(JsonObject object) {
            super.writeAdditional(object);
            assert levelLight != null;
            object.add("light", levelLight.write());
        }

        @Override
        public boolean keep() {
            return false;
        }
    }
}
