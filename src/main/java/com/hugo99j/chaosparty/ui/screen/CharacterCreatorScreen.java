package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.GenericValuesHolder;
import com.daniel99j.djutil.NumberUtils;
import com.daniel99j.djutil.maths.MathsContext;
import com.hugo99j.chaosparty.sounds.SoundManager;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.ui.element.TextInput;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.*;

import java.util.ArrayList;
import java.util.List;

import static com.hugo99j.chaosparty.ui.ScreenSSFunctions.*;

/** First screen of the application. Displayed after the application is created. */
public class CharacterCreatorScreen extends UiScreen {
    float fade = 1;
    User user = User.getUser(1);
    CostumePart costumePart = CostumePart.HAT;
    boolean editing = false;
    TextInput textInput;
    private int hintHeight = 0;
    private final ArrayList<SlidingClothes> slidingClothes = new ArrayList<>();
    private final ArrayList<FutureSlidingClothes> futureSlidingClothes = new ArrayList<>();
    private UiElement playerElement;

    public CharacterCreatorScreen() {
    }

    @Override
    public void show() {
        super.show();
        playerElement = this.addElement(new UiElement("player") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }

            @Override
            public float getX() {
                return centerX(this, GameData.width/2f);
            }

            @Override
            public float getY() {
                return centerY(this, GameData.height/2f);
            }

            @Override
            public float getWidth() {
                return 100;
            }

            @Override
            public float getHeight() {
                return 100;
            }
        });
        this.addElement(new UiElement("player_selected") {
            @Override
            public void render(RenderState state) {
                if(!editing) {
                    GameData.spriteBatch.setColor(Color.WHITE);
                    GameData.spriteBatch.draw(ImageUtil.get("ui/select"), this.getX(), this.getY(), this.getWidth() ,this.getHeight());
                }
            }

            @Override
            public float getX() {
                return 0;
            }

            @Override
            public float getY() {
                return 0;
            }

            @Override
            public float getWidth() {
                return 0;
            }

            @Override
            public float getHeight() {
                return 0;
            }
        });
        textInput = new TextInput("name_user", 16 , false, "Name: ", user.getName(), "", "") {
            @Override
            public void render(RenderState state) {
                if(!editing) {
                    super.render(state);
                }
            }

            @Override
            public float getX() {
                return 0;
            }

            @Override
            public float getY() {
                return 0;
            }

            @Override
            public float getWidth() {
                return 0;
            }

            @Override
            public float getHeight() {
                return 0;
            }
        };
        this.addElement(textInput);
    }

    @Override
    protected void controllerStick(Vector2 change) {
        List<GenericValuesHolder<Vector2, Runnable, Object, Object, Object>> options;
        if(editing) {
             options = new ArrayList<>(List.of(
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(1, 0), () -> {
                    slidingClothes.clear();
                    futureSlidingClothes.clear();
                    String newClothing = Looper.previousValue(Costumes.getVariants(costumePart), user.getWearing(costumePart));
                    String oldClothing = user.getWearing(costumePart);
                    String nextClothingOption = Looper.nextValue(Costumes.getVariants(costumePart), oldClothing);
                    String previousClothingOption = Looper.previousValue(Costumes.getVariants(costumePart), newClothing);

                    user.setWearing(costumePart, newClothing);
                    //slide new from left onto center
                    slidingClothes.add(new SlidingClothes(newClothing, GameData.time, 0.4f, true, playerElement.getX()-100, playerElement.getX(), Interpolation.pow4, 0.5f, 1f));
                    //slide center to right
                    slidingClothes.add(new SlidingClothes(oldClothing, GameData.time, 0.4f, true, playerElement.getX(), playerElement.getX()+100, Interpolation.pow4, 1f, 0.5f));
                    //fade put old selector on left
                    futureSlidingClothes.add(new FutureSlidingClothes(new SlidingClothes(previousClothingOption, GameData.time+0.2f, 0.2f, true, playerElement.getX()-100, playerElement.getX()-100, Interpolation.linear, 0f, 0.5f), GameData.time+0.2f));
                    //fade in new selector on right
                    slidingClothes.add(new SlidingClothes(nextClothingOption, GameData.time, 0.2f, true, playerElement.getX()+100, playerElement.getX()+100, Interpolation.linear, 0.5f, 0));
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(-1, 0), () -> {
                    slidingClothes.clear();
                    futureSlidingClothes.clear();
                    String newClothing = Looper.nextValue(Costumes.getVariants(costumePart), user.getWearing(costumePart));
                    String oldClothing = user.getWearing(costumePart);
                    String nextClothingOption = Looper.nextValue(Costumes.getVariants(costumePart), newClothing);
                    String previousClothingOption = Looper.previousValue(Costumes.getVariants(costumePart), oldClothing);

                    user.setWearing(costumePart, newClothing);
                    //slide new from right onto center
                    slidingClothes.add(new SlidingClothes(newClothing, GameData.time, 0.4f, true, playerElement.getX()+100, playerElement.getX(), Interpolation.pow4, 0.5f, 1f));
                    //slide center to left
                    slidingClothes.add(new SlidingClothes(oldClothing, GameData.time, 0.4f, true, playerElement.getX(), playerElement.getX()-100, Interpolation.pow4, 1f, 0.5f));
                    //fade put old selector on left
                    slidingClothes.add(new SlidingClothes(previousClothingOption, GameData.time, 0.2f, true, playerElement.getX()-100, playerElement.getX()-100, Interpolation.linear, 0.5f, 0));
                    //fade in new selector on right
                    futureSlidingClothes.add(new FutureSlidingClothes(new SlidingClothes(nextClothingOption, GameData.time+0.2f, 0.2f, true, playerElement.getX()+100, playerElement.getX()+100, Interpolation.linear, 0, 0.5f), GameData.time+0.2f));
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(0, -1), () -> {
                    costumePart = Looper.nextValue(costumePart);
                    addCurrentWearing();
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(0, 1), () -> {
                    costumePart = Looper.previousValue(costumePart);
                    addCurrentWearing();
                })
            ));
        } else {
            options = new ArrayList<>(List.of(
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(-1, 0), () -> {
                    user = Looper.previousValue(User.getLoadedUsers(), user);
                    this.setControllerSelected("player");
                    textInput.setValue(user.getName());
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(1, 0), () -> {
                    user = Looper.nextValue(User.getLoadedUsers(), user);
                    this.setControllerSelected("player");
                    textInput.setValue(user.getName());
                }),
                new GenericValuesHolder<>(new Vector2(0, -1), null),
                new GenericValuesHolder<>(new Vector2(0, 1), null)
            ));
        }

        float best = Float.MAX_VALUE;
        Runnable bestRunnable = null;

        for (GenericValuesHolder<Vector2, Runnable, Object, Object, Object> option : options) {
            float distance = option.a().dst(change);
            if (distance < best) {
                best = distance;
                bestRunnable = option.b();
                //no same distance controls
            } else if(distance == best && bestRunnable != null) {
                bestRunnable = null;
            }
        }

        if(bestRunnable != null) {
            bestRunnable.run();
            if(editing) SoundManager.getSound("swap_clothes").play(1);
            else SoundManager.getSound("select").play(1);
        }
    }

    private void addCurrentWearing() {
        slidingClothes.clear();
        futureSlidingClothes.clear();

        String current = user.getWearing(costumePart);
        String next = Looper.nextValue(Costumes.getVariants(costumePart), current);
        String previous = Looper.previousValue(Costumes.getVariants(costumePart), current);

        //right
        slidingClothes.add(new SlidingClothes(next, GameData.time, 0, true, playerElement.getX()+100, playerElement.getX()+100, Interpolation.linear, 0.5f, 0.5f));
        //center
        slidingClothes.add(new SlidingClothes(current, GameData.time, 0, true, playerElement.getX(), playerElement.getX(), Interpolation.linear, 1f, 1f));
        //left
        slidingClothes.add(new SlidingClothes(previous, GameData.time, 0, true, playerElement.getX()-100, playerElement.getX()-100, Interpolation.linear, 0.5f, 0.5f));
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        super.render(delta);
        GameData.spriteBatch.end();

        ControllerUtil controller = ControllerUtil.getCurrent();

        if(!editing && controller != null && controller.wasJustPressed(ControllerInput.A)) {
            editing = true;
        }
        if(controller != null && controller.wasJustPressed(ControllerInput.B)) {
            if (editing) editing = false;
            else {
                if (this.getControllerSelectedId().equals("name_user")) {
                    this.setControllerSelected("player");
                } else {
                    ToRun.run(() -> {
                        GameData.MAIN_INSTANCE.setScreen(new MenuScreen());
                        ((UiScreen) GameData.MAIN_INSTANCE.getScreen()).setControllerSelected("creator");
                    });
                    User.saveUsers();
                }
            }
        }

        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if(fade > 0) fade -= delta;
        fade = Math.max(fade, 0);
        RenderUtil.enableBlending();
        GameData.shapeRenderer.setColor(new Color(0, 0, 0, fade));
        GameData.shapeRenderer.rect(0, 0, 10000, 10000);
        GameData.shapeRenderer.end();

        GameData.spriteBatch.begin();
        GameData.spriteBatch.setColor(Color.WHITE);
        if(editing) {
            if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.X)) {
                user.setWearing(costumePart, Costumes.getVariants(costumePart).get(NumberUtils.getRandomInt(0, Costumes.getVariants(costumePart).size()-1)));
                addCurrentWearing();
            }

            if(slidingClothes.isEmpty()) {
                addCurrentWearing();
            }

            if(costumePart != CostumePart.COLOUR) GameData.spriteBatch.draw(ImageUtil.get("player_blank"), playerElement.getX(), playerElement.getY(), playerElement.getWidth(), playerElement.getHeight());

            Color old = GameData.spriteBatch.getColor().cpy();
            for (SlidingClothes slidingClothing : slidingClothes) {
                //float currentPos = slidingClothing.startX+(slidingClothing.destination-slidingClothing.startX)*((slidingClothing.startTime+slidingClothing.maxTime)/GameData.time);
                float currentPos = slidingClothing.interpolation.apply(slidingClothing.startX, slidingClothing.destination, Math.clamp((GameData.time-slidingClothing.startTime)/slidingClothing.maxTime, 0, 1));

                GameData.spriteBatch.setColor(Color.WHITE.cpy().mul(1, 1, 1, Interpolation.smooth.apply(slidingClothing.startAlpha, slidingClothing.endAlpha, Math.clamp((GameData.time-slidingClothing.startTime)/slidingClothing.maxTime, 0, 1))));

                GameData.spriteBatch.draw(ImageUtil.get("costumes/" + slidingClothing.sprite), currentPos, playerElement.getY(), playerElement.getWidth(), playerElement.getHeight());
            }
            GameData.spriteBatch.setColor(old);

            if(costumePart != CostumePart.COLOUR) drawUser(playerElement.getX(), playerElement.getY(), playerElement.getWidth(), playerElement.getHeight(), user, costumePart);

            //slidingClothes.removeIf((s) -> (s.maxTime+s.startTime < GameData.time));

            futureSlidingClothes.removeIf((s) -> {
                if(s.start <= GameData.time) {
                    slidingClothes.add(s.clothes);
                    return true;
                }
                return false;
            });

        } else {
            slidingClothes.clear();
            futureSlidingClothes.clear();

            if(ControllerUtil.getCurrent().wasJustPressed(ControllerInput.Y)) {
                this.setControllerSelected("name_user");
            }

            user.setName(textInput.getValue());

            drawUser(playerElement.getX(), playerElement.getY(), playerElement.getWidth(), playerElement.getHeight(), user);

            GameData.spriteBatch.setColor(new Color(1, 1, 1, 0.5f));
            drawUser(playerElement.getX()-100, playerElement.getY(), playerElement.getWidth(), playerElement.getHeight(), Looper.previousValue(User.getLoadedUsers(), user));
            drawUser(playerElement.getX()+100, playerElement.getY(), playerElement.getWidth(), playerElement.getHeight(), Looper.nextValue(User.getLoadedUsers(), user));
        }
        String hint;
        if (editing) hint = """
            <icon:left_stick_leftright> Swap costume
            <icon:left_stick_updown> Swap part
            <icon:x> Randomise
            <icon:b> Exit""";
        else {
            hint = "<icon:a> Edit character\n";
            hint += "<icon:left_stick_leftright> Choose character\n";
            if(this.getControllerSelectedId().equals("name_user")) {
                hint += "<icon:b> Exit editing\n";
            } else {
                hint += "<icon:y> Edit name\n";
                hint += "<icon:b> Exit menu";
            }
        }

        //RenderUtil.renderText(hint, this.getStyle().get("hint"));
        this.hintHeight = RenderUtil.getHeight(hint);
        GameData.spriteBatch.end();
    }

    private void drawUser(float x, float y, float xSize, float ySize, User user) {
        drawUser(x, y, xSize, ySize, user, null);
    }

    private void drawUser(float x, float y, float xSize, float ySize, User user, CostumePart exclude) {
        for (CostumePart value : CostumePart.values()) {
            if (value.shouldRender() && value != exclude) GameData.spriteBatch.draw(ImageUtil.get("costumes/" + user.getWearing(value)), x, y, xSize, ySize);
        }
    }

    @Override
    public void editSSContext(MathsContext context) {
        super.editSSContext(context);
        context.withVariable("hint_height", String.valueOf(this.hintHeight));
    }

    private record FutureSlidingClothes(SlidingClothes clothes, float start) {}


    private record SlidingClothes(String sprite, float startTime, float maxTime, boolean fade, float startX, float destination, Interpolation interpolation, float startAlpha, float endAlpha) {}
}
