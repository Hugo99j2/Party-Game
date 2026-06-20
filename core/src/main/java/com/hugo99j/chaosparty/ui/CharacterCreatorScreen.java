package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.daniel99j.dungeongame.sounds.SoundManager;
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
    boolean wasDpadPressed = false;

    public CharacterCreatorScreen() {
        super(ScreenSSBuilder.create()
            .set("x", "0vw")
            .set("y", "0vh")
            .set("xSize", "100vw")
            .set("ySize", "100vh")
            .newChild("respawn")
                .set("x", "0.5vw")
                .set("y", "0.5vh")
                .set("xSize", 320)
                .set("ySize", 64)
                .set("center", true)
                .set("scale", 2)
            .finishChild()
            .newChild("text")
                .set("x", "0.5vw")
                .set("y", "0.7vh")
                .set("xSize", 1)
                .set("ySize", 1)
            .finishChild()
            .newChild("player")
            .set("x", "0.5vw")
            .set("y", "0.7vh")
            .set("xSize", 100)
            .set("ySize", 100)
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
        this.addRenderable(new Button("respawn", "button", "Return to menu") {
            @Override
            public void onClick() {
                SoundManager.getSound("click").playSingle(1);
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new MenuScreen()));
                User.saveUsers();
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
        for (CostumePart value : CostumePart.values()) {
            GameData.spriteBatch.draw(ImageUtil.get("costumes/"+this.user.getWearing(value)), this.getStyle().get("player").getX(), this.getStyle().get("player").getY(), this.getStyle().get("player").getXSize(), this.getStyle().get("player").getYSize());
        }
        RenderUtil.renderText(costumePart.name(), 100, 100, 1, 100, 0, false);
        GameData.spriteBatch.end();

        if(Controllers.getCurrent() != null) {
            Controller current = Controllers.getCurrent();
            boolean wasAnyPressed = false;
            if(current.getButton(current.getMapping().buttonDpadUp)) {
                wasAnyPressed = true;
                if(!wasDpadPressed) {
                    if(costumePart.ordinal() > 0) costumePart = CostumePart.values()[costumePart.ordinal()-1];
                    else costumePart = CostumePart.values()[CostumePart.values().length-1];
                }
            }
            if(current.getButton(current.getMapping().buttonDpadDown)) {
                wasAnyPressed = true;
                if(!wasDpadPressed) {
                    if(costumePart.ordinal() < CostumePart.values().length-1) costumePart = CostumePart.values()[costumePart.ordinal()+1];
                    else costumePart = CostumePart.values()[0];
                }
            }
            if(current.getButton(current.getMapping().buttonDpadLeft)) {
                wasAnyPressed = true;
                if(!wasDpadPressed) {
                    int index = Costumes.getVariants(costumePart).indexOf(user.getWearing(costumePart));
                    String newWearing;
                    if(index > 0) newWearing = Costumes.getVariants(costumePart).get(index-1);
                    else newWearing = Costumes.getVariants(costumePart).getLast();
                    user.setWearing(costumePart, newWearing);
                }
            }
            if(current.getButton(current.getMapping().buttonDpadRight)) {
                wasAnyPressed = true;
                if(!wasDpadPressed) {
                    int index = Costumes.getVariants(costumePart).indexOf(user.getWearing(costumePart));
                    String newWearing;
                    if(index < Costumes.getVariants(costumePart).size()-1) newWearing = Costumes.getVariants(costumePart).get(index+1);
                    else newWearing = Costumes.getVariants(costumePart).getFirst();
                    user.setWearing(costumePart, newWearing);
                }
            }

            wasDpadPressed = wasAnyPressed;
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
