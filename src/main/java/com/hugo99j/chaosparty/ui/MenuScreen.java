package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.daniel99j.dungeongame.ui.screenss.ScreenSSBuilder;
import com.daniel99j.dungeongame.ui.types.Button;
import com.daniel99j.dungeongame.ui.types.Text;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.minigame.AbstractMinigame;
import com.hugo99j.chaosparty.minigame.HotPotatoMinigame;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.minigame.DevMinigame;
import com.hugo99j.chaosparty.minigame.HerdSheepMinigame;

import java.util.ArrayList;
import java.util.List;

/** First screen of the application. Displayed after the application is created. */
public class MenuScreen extends UiScreen {
    Texture backgroundTexture;

    public MenuScreen() {
        super(ScreenSSBuilder.create()
            .set("x", "0vw")
            .set("y", "0vh")
            .set("xSize", "100vw")
            .set("ySize", "100vh")
            .set("display", "flex")
            .set("flexDirection", "column")
            .set("justifyContent", "center")
            .set("alignItems", "center")
            .set("padding", 48)
            .set("gap", 24)
            .newChild("text")
            .set("x", "auto")
            .set("y", "auto")
            .set("xSize", 320)
            .set("ySize", 64)
            .finishChild()
            .newChild("play")
            .set("x", "auto")
            .set("y", "auto")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("scale", 2)
            .finishChild()
            .newChild("sheep")
            .set("x", "auto")
            .set("y", "auto")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("scale", 2)
            .finishChild()
            .newChild("fire")
            .set("x", "auto")
            .set("y", "auto")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("scale", 2)
            .finishChild()
            .newChild("creator")
            .set("x", "auto")
            .set("y", "auto")
            .set("xSize", 320)
            .set("ySize", 64)
            .set("scale", 2)
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
        this.addRenderable(new Button("play", "button", "Test game") {
            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.startMatch(List.of(new MatchPlayer(User.getUser(1)))).setCurrentMinigame(new DevMinigame()));
            }
        });

        this.addRenderable(new Button("sheep", "button", "Herd Sheep") {
            @Override
            public void onClick() {
                super.onClick();
                start(new HerdSheepMinigame());
            }
        });
        this.addRenderable(new Button("fire", "button", "FIRE IN THE HOLE!") {
            @Override
            public void onClick() {
                super.onClick();
                start(new HotPotatoMinigame());
            }
        });


        this.addRenderable(new Button("creator", "button", "Character Creator") {
            @Override
            public void onClick() {
                SoundManager.getSound("click").playSingle(1);
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new CharacterCreatorScreen()));
            }
        });

        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
        this.addRenderable(new Text("text", "<colour:blue>Hello world"));
    }

    private void start(AbstractMinigame minigame) {
        List<MatchPlayer> players = new ArrayList<>();
        int amount = 0;
        if(GameData.DEBUGGING && Debuggers.isEnabled("fakeControllers+1")) amount+=1;
        if(GameData.DEBUGGING && Debuggers.isEnabled("fakeControllers+2")) amount+=2;
        amount += Controllers.getControllers().size;
        amount = Math.min(amount, 4);
        for (int i = 0; i < amount; i++) {
            players.add(new MatchPlayer(User.getUser(i+1)));

        }
        ToRun.run(() -> GameData.startMatch(players).setCurrentMinigame(minigame));
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        // Draw your screen here. "delta" is the time since last render in seconds.
        float worldWidth = GameData.uiViewport.getWorldWidth();
        float worldHeight = GameData.uiViewport.getWorldHeight();

        GameData.spriteBatch.setColor(Color.ORANGE);

        GameData.spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        super.render(delta);
        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
    }
}
