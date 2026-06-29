package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.renderable.Renderable;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.util.*;

/** First screen of the application. Displayed after the application is created. */
public class CharacterCreatorScreen extends UiScreen {
    Texture backgroundTexture;
    float fade = 1;
    User user = User.getUser(1);
    CostumePart costumePart = CostumePart.HAT;

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
            .set("x", "95vw-50")
            .set("y", "50")
            .set("xSize", "5vw")
            .set("ySize", "999")
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
            .build()
        );
        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
        this.addRenderable(new Renderable("up") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }

            @Override
            public void onControllerSelect() {
                super.onControllerSelect();
                this.getScreen().setControllerSelected("player");
                costumePart = Looper.previousValue(costumePart);
            }
        });

        this.addRenderable(new Renderable("down") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }

            @Override
            public void onControllerSelect() {
                super.onControllerSelect();
                this.getScreen().setControllerSelected("player");
                costumePart = Looper.nextValue(costumePart);
            }
        });

        this.addRenderable(new Renderable("left") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }

            @Override
            public void onControllerSelect() {
                super.onControllerSelect();
                user.setWearing(costumePart, Looper.previousValue(Costumes.getVariants(costumePart), user.getWearing(costumePart)));
                this.getScreen().setControllerSelected("player");
            }
        });

        this.addRenderable(new Renderable("right") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }

            @Override
            public void onControllerSelect() {
                super.onControllerSelect();
                user.setWearing(costumePart, Looper.nextValue(Costumes.getVariants(costumePart), user.getWearing(costumePart)));
                this.getScreen().setControllerSelected("player");
            }
        });

        this.addRenderable(new Renderable("player") {
            @Override
            public void setScreen(UiScreen screen) {
                super.setScreen(screen);
                this.usesMouse = true;
            }
        });
        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
        //this.addRenderable(new Text("text", "<colour:red>The end."));
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

        GameData.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if(fade > 0) fade -= delta;
        fade = Math.max(fade, 0);
        RenderUtil.enableBlending();
        GameData.shapeRenderer.setColor(new Color(0, 0, 0, fade));
        GameData.shapeRenderer.rect(0, 0, 10000, 10000);
        GameData.shapeRenderer.end();

        GameData.spriteBatch.begin();
        GameData.spriteBatch.setColor(Color.WHITE);
        for (CostumePart value : CostumePart.values()) {
            if((value.shouldRender() || (costumePart.equals(CostumePart.COLOUR) && value.equals(CostumePart.COLOUR))) && (!costumePart.equals(CostumePart.COLOUR) || value.equals(CostumePart.COLOUR))) GameData.spriteBatch.draw(ImageUtil.get("costumes/"+this.user.getWearing(value)), this.getStyle().get("player").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());
        }

        GameData.spriteBatch.setColor(new Color(1, 1, 1, 0.5f));
        GameData.spriteBatch.draw(ImageUtil.get("costumes/"+Looper.previousValue(Costumes.getVariants(costumePart), this.user.getWearing(costumePart))), this.getStyle().get("left").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());
        GameData.spriteBatch.draw(ImageUtil.get("costumes/"+Looper.nextValue(Costumes.getVariants(costumePart), this.user.getWearing(costumePart))), this.getStyle().get("right").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());

        RenderUtil.renderText(costumePart.name(), 100, 100, 1, 100, 0, false);
        RenderUtil.renderText("<icon:b> Exit\n<icon:left_stick_leftright> Swap costume\n<icon:left_stick_updown> Swap editing", this.getStyle().get("hint"));
        GameData.spriteBatch.end();

        if(Controllers.getCurrent() != null && ((ControllerUtil) Controllers.getCurrent()).wasJustPressed(ControllerInput.B)) {
            ToRun.run(() -> {
                GameData.MAIN_INSTANCE.setScreen(new MenuScreen());
                ((UiScreen) GameData.MAIN_INSTANCE.getScreen()).setControllerSelected("creator");
            });
            User.saveUsers();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }
}
