package com.hugo99j.chaosparty.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.Contact;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.dungeongame.entity.*;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.google.gson.JsonObject;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.RenderLayer;

import static com.hugo99j.chaosparty.GameData.px;

public class LiquidBarrelObject extends AdvancedObject {
    private String sprite;
    private boolean explosive;
    private int ticksUntilExplode = -1;

    public LiquidBarrelObject(String sprite, boolean explosive) {
        this.sprite = sprite;
        this.explosive = explosive;
    }

    @Override
    public void onCollision(Contact contact, AbstractObject object) {
        super.onCollision(contact, object);
        if(object instanceof Potato) {
            explodeNow();
        } else if(object instanceof Player player && GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame hotPotatoMinigame && hotPotatoMinigame.getHotPlayer().getPlayerObject().equals(player)) {
            explodeNow();
        }
    }

    private void explodeAt(int time) {
        if(!explosive) return;
        if(this.ticksUntilExplode == -1) this.ticksUntilExplode = time;
    }

    private void explodeNow() {
        if(!explosive) return;
        var boom = new ParticleEffect();
        boom.load(Gdx.files.internal(PathUtil.asset("particles/boom.p")), GameData.atlas);
        boom.setEmittersCleanUpBlendFunction(false);
        boom.scaleEffect(0.01f);
        boom.start();
        boom.setPosition(this.getPos().x+0.5f, this.getPos().y+0.5f);
        GameData.getLevelOrThrow().particles.add(boom);
        SoundManager.getSound("explode").playSingle(1);
        int delay = 0;
        for (LiquidBarrelObject objectsInRadius : this.getLevel().getObjectsInRadius(this.getPos().add(0.5f, 0.5f), 5, LiquidBarrelObject.class, true, this)) {
            delay += NumberUtils.getRandomInt(3, 7);
            objectsInRadius.explodeAt(delay);
        }
        for (Player player : this.getLevel().getObjectsInRadius(this.getPos().add(0.5f, 0.5f), 5, Player.class, false, null)) {
            if(GameData.getCurrentMatch().getCurrentMinigame() instanceof HotPotatoMinigame hotPotatoMinigame) {
                if(hotPotatoMinigame.getHotPlayer().equals(player.getMatchPlayer())) continue;
                hotPotatoMinigame.setHotPlayer(player.getMatchPlayer());
                break;
            }
        }
        for (Player player : this.getLevel().getObjectsInRadius(this.getPos().add(0.5f, 0.5f), 20, Player.class, false, null)) {
            for (MatchView matchView : GameData.getCurrentMatch().getMatchViews()) {
                if(matchView.getPlayer().equals(player.getMatchPlayer())) {
                    matchView.setCamerashakeTime(1);
                }
            }
        }
        this.dispose();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.ticksUntilExplode > 0) this.ticksUntilExplode--;
        if(this.ticksUntilExplode == 0) explodeNow();
    }

    @Override
    public void render(MatchView matchView) {
        GameData.spriteBatch.draw(ImageUtil.get(sprite), this.getPos().x, this.getPos().y, 2, 2);
    }

    @Override
    protected PhysicsSettings createPhysics() {
        return PhysicsSettings.immovable(1, 1.5f, 0, 0);
    }

    @Override
    public void writeAdditional(JsonObject object) {
        object.addProperty("sprite", sprite);
        object.addProperty("explosive", explosive);
    }

    public static LiquidBarrelObject read(JsonObject object) {
        return new LiquidBarrelObject(object.get("sprite").getAsString(), object.get("explosive").getAsBoolean());
    }

    @Override
    public ObjectType<LiquidBarrelObject> getType() {
        return ObjectTypes.LIQUID_BARREL;
    }

    @Override
    public float getLayer() {
        return RenderLayer.DECORATIONS;
    }

    @Override
    public String toString() {
        return "Liquid Barrel";
    }

    public static LiquidBarrelObject createDefault() {
        return new LiquidBarrelObject("oil_drum", true);
    }
}
