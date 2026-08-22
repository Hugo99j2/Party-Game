package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.daniel99j.dungeongame.sounds.SoundManager;
import com.hugo99j.chaosparty.minigame.*;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.ui.element.Button;
import com.hugo99j.chaosparty.ui.element.DivElement;
import com.hugo99j.chaosparty.ui.element.Text;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.match.User;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.PathUtil;
import com.hugo99j.chaosparty.util.ToRun;
import com.hugo99j.chaosparty.GameData;

import java.util.ArrayList;
import java.util.List;

import static com.hugo99j.chaosparty.ui.ScreenSSFunctions.*;

/** First screen of the application. Displayed after the application is created. */
public class MenuScreen extends UiScreen {
    Texture backgroundTexture;

    public MenuScreen() {
        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
    }

    @Override
    public void show() {
        super.show();
        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)

        DivElement mainBody = this.addElement(new DivElement("main_body") {
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
                return ((getHeight()/this.getChildren().size())-this.getPadding())*5;
            }

            @Override
            public float getHeight() {
                return smartFitHeight(this, this.getChildren().size()*96+this.getPadding()*this.getChildren().size(), 0, GameData.height);
            }

            @Override
            public float getPadding() {
                return 20;
            }
        });

        this.addElement(new Text("text", "<colour:blue>CHAOS PARTY!") {
            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });

        this.addElement(new Button("play", "button", "Play a game") {
            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new SelectMinigameScreen()));
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });

        this.addElement(new Button("sheep", "button", "Herd Sheep") {
            @Override
            public void onClick() {
                super.onClick();
                start(new HerdSheepMinigame());
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });
        this.addElement(new Button("fire", "button", "FIRE IN THE HOLE!") {
            @Override
            public void onClick() {
                super.onClick();
                start(new HotPotatoMinigame());
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });
        this.addElement(new Button("fall", "button", "Start Falling!") {
            @Override
            public void onClick() {
                super.onClick();
                start(new FallingFloorMinigame());
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });
        this.addElement(new Button("counting", "button", "Counting") {
            @Override
            public void onClick() {
                super.onClick();
                start(new CountingMinigame());
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });

        this.addElement(new Button("creator", "button", "Character Creator") {
            @Override
            public void onClick() {
                SoundManager.getSound("click").playSingle(1);
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new CharacterCreatorScreen()));
            }

            @Override
            public float getX() {
                return parentX(this, 0);
            }

            @Override
            public float getY() {
                return parentY(this, 0)+autoRowsYPos(this);
            }

            @Override
            public float getWidth() {
                return parentWidth(this, 100);
            }

            @Override
            public float getHeight() {
                return autoRowsHeight(this);
            }

            @Override
            public UiElement getParent() {
                return mainBody;
            }
        });
    }

    public static void start(AbstractMinigame minigame) {
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
