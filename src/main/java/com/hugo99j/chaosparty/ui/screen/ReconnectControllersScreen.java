package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.hugo99j.chaosparty.ui.debugger.DebugController;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.ui.element.Button;
import com.hugo99j.chaosparty.ui.element.Text;
import com.hugo99j.chaosparty.util.DummyController;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.ToRun;

/** First screen of the application. Displayed after the application is created. */
public class ReconnectControllersScreen extends UiScreen {
    public ReconnectControllersScreen() {
    }

    @Override
    public void show() {
        super.show();
        Text text = this.addElement(new Text("text", "<colour:red>Please reconnect controllers!") {
            @Override
            public float getX() {
                return GameData.width/2f;
            }

            @Override
            public float getY() {
                return GameData.height/2f;
            }

            @Override
            public float getWidth() {
                return GameData.width/100f;
            }

            @Override
            public float getHeight() {
                return getWidth()/5;
            }
        });
        this.addElement(new Button("menu", "button", "Done") {
            @Override
            public float getX() {
                return text.getX();
            }

            @Override
            public float getY() {
                return text.getY()+10+text.getHeight();
            }

            @Override
            public float getWidth() {
                return text.getWidth();
            }

            @Override
            public float getHeight() {
                return text.getHeight();
            }

            @Override
            public void onClick() {
                super.onClick();
                reconnect();
            }
        });
        reconnect();
    }

    private void reconnect() {
        boolean connected = true;
        int i = 0;
        int fakeControllers = 0;
        if(GameData.DEBUGGING && Debuggers.isEnabled("fakeControllers+1")) fakeControllers+=1;
        if(GameData.DEBUGGING && Debuggers.isEnabled("fakeControllers+2")) fakeControllers+=2;
        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
            if(i == 0 && DebugController.INSTANCE != null) {
                player.controller = (Controller) DebugController.INSTANCE;
            } else {
                if (Controllers.getControllers().size <= i) {
                    if (fakeControllers > 0) {
                        player.controller = new DummyController();
                        fakeControllers--;
                        continue;
                    }
                    connected = false;
                    break;
                }
                player.controller = Controllers.getControllers().get(i);
            }
            i++;
        }
        if(connected) ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new PlayScreen()));
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        GameData.spriteBatch.setColor(Color.RED);
        GameData.spriteBatch.draw(ImageUtil.get("gameyay"), 0, 0, GameData.width, GameData.height);
        GameData.spriteBatch.setColor(Color.WHITE);

        super.render(delta);
        GameData.spriteBatch.end();
    }
}
