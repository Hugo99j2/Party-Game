package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.NumberUtils;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.CountingMinigame;
import com.hugo99j.chaosparty.minigame.counting.Activity;
import com.hugo99j.chaosparty.minigame.counting.JuggleActivity;
import com.hugo99j.chaosparty.minigame.counting.PyramidActivity;
import com.hugo99j.chaosparty.minigame.counting.WalkAroundActivity;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.*;
import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.hugo99j.chaosparty.GameData.*;

public class Clown extends AbstractObject {
    private Activity activity;
    private final EasyAnimator animator = new EasyAnimator();

    @Override
    public void onAdd(boolean fromLoad) {
        super.onAdd(fromLoad);
        if(NumberUtils.getRandomInt(1, 8) == 1 && getCurrentMinigame() instanceof CountingMinigame) this.dispose();
        else {
            this.rerollActivity();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.activity.tick();
        this.getAnimator().tick();
    }

    @Override
    public void render(MatchView matchView) {
        Vector2 pos = this.getPos();
        if(this.animator.isAnimating()) GameData.spriteBatch.draw(this.animator.getCurrentFrame(), pos.x, pos.y, 1, 1);
        else GameData.spriteBatch.draw(ImageUtil.get("clown"), pos.x, pos.y, 1, 1);
        if(GameData.DEBUGGING && Debuggers.isEnabled("botDebug")) {
            String s = "Activity: "+this.activity.getClass().getName().replace("com.hugo99j.chaosparty.minigame.counting", "");
            if(this.activity != null) {
                StringBuilder combined = new StringBuilder();
                List<String> lines = new ArrayList<>();
                this.activity.addDebugInfo(lines);
                for (String a : lines) {
                    combined.append(a).append("\n");
                }
                s += "\n" + combined.toString();
            }
            RenderUtil.renderTextWorld(s, this.getPos().x, this.getPos().y, 2);
        }
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.texture("clown", 0.5f, 1.0f, 1f, 1f);
    }

    @Override
    public void writeAdditional(JsonObject object) {

    }

    public static Clown read(JsonObject object) {
        return new Clown();
    }

    @Override
    public ObjectType<Clown> getType() {
        return ObjectTypes.CLOWN;
    }

    @Override
    public RenderLayer getDefaultLayer() {
        return RenderLayer.NPC;
    }

    @Override
    public String toString() {
        return "Clown";
    }

    public static Clown createDefault() {
        return new Clown();
    }

    @Override
    public boolean shouldCollideWith(AbstractObject other) {
        return super.shouldCollideWith(other) && !(other instanceof Clown);
    }

    public Activity getActivity() {
        return activity;
    }

    public EasyAnimator getAnimator() {
        return animator;
    }

    public void rerollActivity() {
        List<Function<Clown, Activity>> creators = List.of(JuggleActivity::new, WalkAroundActivity::new, PyramidActivity::new);
        this.activity = creators.get(NumberUtils.getRandomInt(0, creators.size()-1)).apply(this);
    }
}
