package com.daniel99j.dungeongame.level;

import box2dLight.Light;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.daniel99j.djutil.UsageLimited;
import com.daniel99j.djutil.ValueHolder;
import com.daniel99j.djutil.pathfinder.PathfindDebugPos;
import com.daniel99j.djutil.pathfinder.PathfindDebugType;
import com.hugo99j.chaosparty.GameData;
import com.daniel99j.dungeongame.entity.*;
import com.hugo99j.chaosparty.match.MatchView;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.hugo99j.chaosparty.util.RenderUtil;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Level implements Disposable {
    private final World box2dWorld;
    private final ArrayList<AbstractObject> objects = new ArrayList<>();
    private int time;
    public RayHandler rayHandler;
    private final ArrayList<LevelLight<?>> lights = new ArrayList<>();
    public final ArrayList<ParticleEffect> particles = new ArrayList<>();
    private float lastRenderedFrame;
    private final List<Runnable> collisions = new ArrayList<>();

    public Level() {
        this.box2dWorld = new World(new Vector2(0, 0), true);
        this.box2dWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                //schedule so that entities dont delete themselves whilst ticking box2d
                AbstractObject a = ((AbstractObject) contact.getFixtureA().getBody().getUserData());
                AbstractObject b = ((AbstractObject) contact.getFixtureB().getBody().getUserData());
                collisions.add(() -> {
                    if(a.isRemoved() || b.isRemoved()) return;
                    a.onCollision(contact, b);
                    b.onCollision(contact, a);
                });
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
        RayHandler.setGammaCorrection(true);
        this.rayHandler = new RayHandler(this.getBox2dWorld());
        this.rayHandler.setBlurNum(3);
        this.rayHandler.setAmbientLight(1);
        RayHandler.useDiffuseLight(false);
        this.rayHandler.setShadows(true);
    }

    public void tickWorld() {
        time++;
        collisions.forEach(Runnable::run);
        collisions.clear();
        for (AbstractObject o : new ArrayList<>(this.objects)) {
            o.tick();
        }
    }

    public void render(MatchView matchView) {
        ArrayList<AbstractObject> objects = getAllObjects();
        objects.sort((one, two) -> {
            float layer1 = one.getLayer();
            float layer2 = two.getLayer();
            if(layer1 == layer2) return 0;
            return Float.compare(layer1, layer2);
        });
        objects.forEach((a) -> a.renderInternal(matchView));

        for (ParticleEffect particle : new ArrayList<>(particles)) {
            if(particle.isComplete()) {
                particles.remove(particle);
                particle.dispose();
            }
            particle.draw(GameData.spriteBatch, lastRenderedFrame == Gdx.graphics.getDeltaTime() ? 0 : Gdx.graphics.getDeltaTime());
        }
        GameData.spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if(Debuggers.isEnabled("pathfindingRender")) {
            GameData.spriteBatch.end();
            RenderUtil.enableBlending();
            Debuggers.pathfindDebuggers.forEach((hash, debuggers) -> {
                for (PathfindDebugPos pathfindDebugPos : debuggers) {
                    float transparency = Debuggers.pathfindDebuggerTimers.get(hash).floatValue()/(5* GameData.TICKS_PER_SECOND);

                    //GameData.shapeRenderer.setProjectionMatrix(GameData.gameCamera.combined);
                    if (pathfindDebugPos.type().equals(PathfindDebugType.SUCCESSFUL_PATH)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                        GameData.shapeRenderer.setColor(Color.GREEN.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.line(pathfindDebugPos.pos().getX() + 0.5f, pathfindDebugPos.pos().getY() + 0.5f, pathfindDebugPos.previous().getX() + 0.5f, pathfindDebugPos.previous().getY() + 0.5f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.CONNECTION)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                        GameData.shapeRenderer.setColor(Color.YELLOW.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.line(pathfindDebugPos.pos().getX() + 0.5f, pathfindDebugPos.pos().getY() + 0.5f, pathfindDebugPos.previous().getX() + 0.5f, pathfindDebugPos.previous().getY() + 0.5f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.INVALID)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.GRAY.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.OPEN_SET)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.RED.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.CLOSED_SET)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.YELLOW.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.START)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.BLUE.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                    } else if (pathfindDebugPos.type().equals(PathfindDebugType.END)) {
                        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        GameData.shapeRenderer.setColor(Color.PURPLE.cpy().mul(1, 1, 1, transparency));
                        GameData.shapeRenderer.rect(pathfindDebugPos.pos().getX() + 0.3f, pathfindDebugPos.pos().getY() + 0.3f, 0.4f, 0.4f);
                    }
                    GameData.shapeRenderer.end();
                }
            });
            GameData.spriteBatch.begin();
        }

        if(GameData.DEBUGGING && Debuggers.isEnabled("pathfindingRender") && lastRenderedFrame != Gdx.graphics.getDeltaTime()) {
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : Debuggers.pathfindDebuggerTimers.entrySet()) {
                if (entry.getValue() <= 0) {
                    toRemove.add(entry.getKey());
                } else {
                    Debuggers.pathfindDebuggerTimers.replace(entry.getKey(), entry.getValue() - 1);
                }
            }
            toRemove.forEach(Debuggers.pathfindDebuggerTimers.keySet()::remove);
            toRemove.forEach(Debuggers.pathfindDebuggers.keySet()::remove);
        }

        //makes particles update at normal speed with multiple screens
        lastRenderedFrame = Gdx.graphics.getDeltaTime();
    }

    @Override
    public void dispose() {
        for (AbstractObject allObject : this.getAllObjects()) {
            allObject.dispose();
        }
        this.rayHandler.dispose();
        this.box2dWorld.dispose();

        new ArrayList<>(this.particles).forEach(this::removeParticleImmediately);

        if(GameData.level == this) GameData.level = null;
    }

    public World getBox2dWorld() {
        return box2dWorld;
    }

    public ArrayList<AbstractObject> getAllObjects() {
        return new ArrayList<>(this.objects);
    }

    public void addObject(AbstractObject object) {
        //noinspection usagelimited
        object.init(this, false);
    }

    public void addObjectFromLoad(AbstractObject object) {
        //noinspection usagelimited
        object.init(this, true);
    }

    @UsageLimited
    public void addObjectToList(AbstractObject object) {
        this.objects.add(object);
    }

    public void completedLoad() {
        for (AbstractObject o : this.getAllObjects()) {
            o.markFromWorldLoad();
        }
    }

    public int getTime() {
        return time;
    }

    public @Nullable AbstractObject getObjectByUUID(UUID uuid) {
        return this.getAllObjects().stream().filter((object -> object.getUUID() == uuid)).findFirst().orElse(null);
    }

    public ArrayList<LevelLight<?>> getLights() {
        return this.lights;
    }

    public void removeObject(AbstractObject object) {
        object.dispose();
        this.objects.remove(object);
    }

    public <T extends Light> LevelLight<T> addLight(Function<RayHandler, T> function, SaveConfig saveConfig) {
        T light = function.apply(this.rayHandler);
        light.setContactFilter((short) 1, (short) 0, CollisionCategories.LIGHT_BLOCKING);
        LevelLight<T> levelLight = new LevelLight<>(light, saveConfig, UUID.randomUUID());
        this.lights.add(levelLight);
        return levelLight;
    }

    public void removeLight(Light light) {
        LevelLight<?> toRemove = null;
        for (LevelLight<?> levelLight : this.lights) {
            if(levelLight.light().equals(light)) {
                toRemove = levelLight;
            }
        }
        if(toRemove != null) removeLight(toRemove);
    }

    public void removeLight(LevelLight<?> light) {
        this.lights.remove(light);
        light.light().remove();
    }

    public <T extends AbstractObject> List<T> getObjectsInRadius(Vector2 pos, float radius, Class<T> clazz, boolean physics, @Nullable T exclude) {
        List<T> objects = getObjectsBetweenClass(pos.cpy().sub(radius, radius), pos.cpy().add(radius, radius), clazz, physics);
        if(!physics) {
            objects.removeIf(object -> object.getPos().dst(pos) > radius);
        } else {
            objects.removeIf(object -> {
                Vector4 hitbox = object.getHitboxWorld(object.getPhysics().getFixtureList().get(0));
                Vector2 point = new Vector2(Math.clamp(pos.x, hitbox.x, hitbox.z), Math.clamp(pos.y, hitbox.y, hitbox.w));
                return point.dst(pos) > radius;
            });
        }
        objects.remove(exclude);

        if(GameData.DEBUGGING && Debuggers.isEnabled("showBetweenBoxes")) {
            Debuggers.customLevelRenderers.put((v) -> {
                GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GameData.shapeRenderer.setColor(Color.BLUE);
                GameData.shapeRenderer.circle(pos.x, pos.y, radius, (int) (40*radius));
                GameData.shapeRenderer.end();
            }, new ValueHolder<>(GameData.TICKS_PER_SECOND));
        }
        return objects;
    }

    public <T extends AbstractObject> List<T> getObjectsBetweenClass(Vector2 start, Vector2 end, Class<T> clazz, boolean physics) {
        if(end.x < start.x || end.y < start.y) throw new IllegalArgumentException("End is before start");
        List<T> objects = new ArrayList<>();
        if(physics) {
            QueryCallback callback = fixture -> {
                if (clazz.isInstance(fixture.getBody().getUserData())) //noinspection unchecked
                    objects.add((T) fixture.getBody().getUserData());
                return true;
            };

            this.getBox2dWorld().QueryAABB(callback, start.x, start.y, end.x, end.y);
        } else {
            for (AbstractObject allObject : this.getAllObjects()) {
                Vector2 pos = allObject.getPos();
                if(clazz.isInstance(allObject) && pos.x >= start.x && pos.x <= end.x && pos.y >= start.y && pos.y <= end.y) //noinspection unchecked
                    objects.add((T) allObject);
            }
        }

        if(GameData.DEBUGGING && Debuggers.isEnabled("showBetweenBoxes")) {
            Debuggers.customLevelRenderers.put((v) -> {
                GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GameData.shapeRenderer.setColor(Color.CYAN);
                GameData.shapeRenderer.rect(start.x, start.y, end.x - start.x, end.y - start.y);
                GameData.shapeRenderer.end();
            }, new ValueHolder<>(GameData.TICKS_PER_SECOND));
        }

        return objects;
    }

    public List<AbstractObject> getObjectsBetween(Vector2 start, Vector2 end) {
        return this.getObjectsBetweenClass(start, end, AbstractObject.class, true);
    }

    public void stopEmitting(ParticleEffect particle) {
        particle.setDuration(0);
    }

    public void removeParticleImmediately(ParticleEffect particle) {
        stopEmitting(particle);
        particles.remove(particle);
        particle.dispose();
    }
}
