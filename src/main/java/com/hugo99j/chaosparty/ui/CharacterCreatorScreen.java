package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.daniel99j.djutil.GenericValuesHolder;
import com.daniel99j.djutil.maths.MathsContext;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.renderable.RenderState;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.ui.element.TextInput;
import com.hugo99j.chaosparty.util.*;

import java.util.ArrayList;
import java.util.List;

/** First screen of the application. Displayed after the application is created. */
public class CharacterCreatorScreen extends UiScreen {
    Texture backgroundTexture;
    float fade = 1;
    User user = User.getUser(1);
    CostumePart costumePart = CostumePart.HAT;
    boolean editing = false;
    TextInput textInput;
    private int hintHeight = 0;

    public CharacterCreatorScreen() {
        super(ScreenSSBuilder.create()
            .set("x", "0vw")
            .set("y", "0vh")
            .set("xSize", "100vw")
            .set("ySize", "100vh")
            .newChild("respawn")
                .set("x", "83.5vw")
                .set("y", "91vh")
                .set("xSize", 320)
                .set("ySize", 64)
                .set("center", true)
                .set("scale", 2)
            .finishChild()
            .newChild("text")
                .set("x", "50vw")
                .set("y", "70vh")
                .set("xSize", 1)
                .set("ySize", 1)
            .finishChild()
            .newChild("hint")
            .set("x", "100vw-8vw-50")
            .set("y", "50")
            .set("xSize", "8vw")
            .set("ySize", "${hint_height}")
            .finishChild()
            .newChild("up")
            .set("x", "50vw")
            .set("y", "60vh")
            .set("xSize", 400)
            .set("ySize", 100)
            .set("center", true)
            .finishChild()
            .newChild("down")
            .set("x", "50vw")
            .set("y", "40vh")
            .set("xSize", 400)
            .set("ySize", 100)
            .set("center", true)
            .finishChild()
            .newChild("left")
            .set("x", "40vw")
            .set("y", "50vh")
            .set("xSize", 100)
            .set("ySize", 400)
            .set("center", true)
            .finishChild()
            .newChild("right")
            .set("x", "60vw")
            .set("y", "50vh")
            .set("xSize", 100)
            .set("ySize", 400)
            .set("center", true)
            .finishChild()
            .newChild("player")
            .set("x", "50vw")
            .set("y", "50vh")
            .set("xSize", 100)
            .set("ySize", 100)
            .set("center", true)
            .finishChild()
            .newChild("name_user")
            .set("x", "50vw")
            .set("y", "40vh")
            .set("xSize", 300)
            .set("ySize", 100)
            .set("center", true)
            .finishChild()
            .newChild("player_selected")
            .set("x", "50vw")
            .set("y", "50vh")
            .set("xSize", "100+sin(time*5)*10")
            .set("ySize", "100+sin(time*5)*10")
            .set("center", true)
            .finishChild()
            .build()
        );
        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
    }

    @Override
    public void show() {
        super.show();
        this.addRenderable(new Renderable("player") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }
        });
        this.addRenderable(new Renderable("player_selected") {
            @Override
            public void render(RenderState state) {
                if(!editing) {
                    GameData.spriteBatch.setColor(Color.WHITE);
                    GameData.spriteBatch.draw(ImageUtil.get("ui/select"), this.getX(), this.getY(), this.getStyle().getXSize() ,this.getStyle().getYSize());
                }
            }
        });
        textInput = new TextInput("name_user", 16 , false, "Name: ", user.getName(), "", "") {
            @Override
            public void render(RenderState state) {
                if(!editing) {
                    super.render(state);
                }
            }
        };
        this.addRenderable(textInput);
    }

    @Override
    protected void controllerStick(Vector2 change) {
        List<GenericValuesHolder<Vector2, Runnable, Object, Object, Object>> options;
        if(editing) {
             options = new ArrayList<>(List.of(
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(-1, 0), () -> {
                    user.setWearing(costumePart, Looper.previousValue(Costumes.getVariants(costumePart), user.getWearing(costumePart)));
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(1, 0), () -> {
                    user.setWearing(costumePart, Looper.nextValue(Costumes.getVariants(costumePart), user.getWearing(costumePart)));
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(0, -1), () -> {
                    costumePart = Looper.nextValue(costumePart);
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(0, 1), () -> {
                    costumePart = Looper.previousValue(costumePart);
                })
            ));
        } else {
            options = new ArrayList<>(List.of(
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(-1, 0), () -> {
                    user = Looper.previousValue(User.getLoadedUsers(), user);
                    textInput.setValue(user.getName());
                }),
                new GenericValuesHolder<Vector2, Runnable, Object, Object, Object>(new Vector2(1, 0), () -> {
                    user = Looper.nextValue(User.getLoadedUsers(), user);
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
        } else {
            super.controllerStick(change);
        }
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        // Draw your screen here. "delta" is the time since last render in seconds.
        float worldWidth = GameData.uiViewport.getWorldWidth();
        float worldHeight = GameData.uiViewport.getWorldHeight();

        GameData.spriteBatch.setColor(Color.RED);

        GameData.spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        super.render(delta);
        GameData.spriteBatch.end();

        ControllerUtil controller = ((ControllerUtil) Controllers.getCurrent());

        if(!editing && controller != null && controller.wasJustPressed(ControllerInput.A)) {
            editing = true;
        }
        if(controller != null && controller.wasJustPressed(ControllerInput.B)) {
            if (editing) editing = false;
            else {
                ToRun.run(() -> {
                    GameData.MAIN_INSTANCE.setScreen(new MenuScreen());
                    ((UiScreen) GameData.MAIN_INSTANCE.getScreen()).setControllerSelected("creator");
                });
                User.saveUsers();
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
            for (CostumePart value : CostumePart.values()) {
                if ((value.shouldRender() || (costumePart.equals(CostumePart.COLOUR) && value.equals(CostumePart.COLOUR))) && (!costumePart.equals(CostumePart.COLOUR) || value.equals(CostumePart.COLOUR)))
                    GameData.spriteBatch.draw(ImageUtil.get("costumes/" + this.user.getWearing(value)), this.getStyle().get("player").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());
            }

            GameData.spriteBatch.setColor(new Color(1, 1, 1, 0.5f));
            GameData.spriteBatch.draw(ImageUtil.get("costumes/" + Looper.previousValue(Costumes.getVariants(costumePart), this.user.getWearing(costumePart))), this.getStyle().get("left").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());
            GameData.spriteBatch.draw(ImageUtil.get("costumes/" + Looper.nextValue(Costumes.getVariants(costumePart), this.user.getWearing(costumePart))), this.getStyle().get("right").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());

            //RenderUtil.renderText(costumePart.name(), 100, 100, 1, 100, 0, false)
        } else {
            user.setName(textInput.getValue());

            drawUser(this.getStyle().get("player").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize(), user);

            GameData.spriteBatch.setColor(new Color(1, 1, 1, 0.5f));
            drawUser(this.getStyle().get("left").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize(), Looper.previousValue(User.getLoadedUsers(), user));
            drawUser(this.getStyle().get("right").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize(), Looper.nextValue(User.getLoadedUsers(), user));
        }
        String hint = """

                <icon:left_stick_leftright> Swap costume
                <icon:left_stick_updown> Swap editing
                """;

        if (editing) hint = "<icon:b> Exit" + hint;
        else hint = "<icon:a> Choose character\n<icon:b> Exit menu" + hint;

        RenderUtil.renderText(hint, this.getStyle().get("hint"));
        this.hintHeight = RenderUtil.getHeight(hint);
        GameData.spriteBatch.end();
    }

    private void drawUser(int x, int y, float xSize, float ySize, User user) {
        for (CostumePart value : CostumePart.values()) {
            if (value.shouldRender()) GameData.spriteBatch.draw(ImageUtil.get("costumes/" + user.getWearing(value)), x, y, xSize, ySize);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }

    @Override
    public void editSSContext(MathsContext context) {
        super.editSSContext(context);
        context.withVariable("hint_height", String.valueOf(this.hintHeight));
    }
}
