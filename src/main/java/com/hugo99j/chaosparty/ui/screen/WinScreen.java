package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.daniel99j.djutil.NumberUtils;
import com.hugo99j.chaosparty.ui.element.Button;
import com.hugo99j.chaosparty.ui.element.DivElement;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.util.*;
import com.hugo99j.chaosparty.GameData;

import java.util.List;
import java.util.Map;

import static com.hugo99j.chaosparty.ui.ScreenSSFunctions.*;

/** First screen of the application. Displayed after the application is created. */
public class WinScreen extends UiScreen {
    Texture backgroundTexture;
    ParticleEffect firework;
    private Map<String, Map<MatchPlayer, Integer>> scores;
    List<MatchPlayer> players;

    public WinScreen(Map<String, Map<MatchPlayer, Integer>> scores) {
        //super(createSS());
        firework = new ParticleEffect();
        firework.load(Gdx.files.internal(PathUtil.asset("particles/w.p")), GameData.atlas);
        firework.setEmittersCleanUpBlendFunction(false);
        firework.scaleEffect(1);
        firework.setDuration(1000000);
        firework.start();

        backgroundTexture = new Texture(PathUtil.texture("gameyay.png"));
        this.scores = scores;
        players = scores.values().stream().toList().getFirst().keySet().stream().toList();
    }

    @Override
    public void show() {
        super.show();

        int columns = 8;
        int rows = 5;
        float sizePerBox = 80;

        DivElement columnHolder = this.addElement(new DivElement("main_div") {
            @Override
            public float getPadding() {
                return 10;
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
                return smartFitWidth(this, sizePerBox*columns, 0, GameData.width*0.9f);
            }

            @Override
            public float getHeight() {
                return getWidth()/columns*rows;
            }
        });
        for (int column = 0; column < columns; column++) {
            int finalColumn = column;
            DivElement rowHolder = this.addElement(new DivElement("row_holder_"+column) {
                @Override
                public float getX() {
                    return parentXAdd(this, autoColumnsXPos(this));
                }

                @Override
                public float getY() {
                    return parentY(this, 0);
                }

                @Override
                public float getWidth() {
                    return autoColumnsWidth(this);
                }

                @Override
                public float getHeight() {
                    return parentHeight(this, 100);
                }

                @Override
                public float getPadding() {
                    return 10;
                }

                @Override
                public UiElement getParent() {
                    return columnHolder;
                }
            });
            for (int row = 0; row < rows; row++) {
                int finalRow = row;
                this.addElement(new UiElement("pos_"+finalRow+"_"+column) {
                    @Override
                    public float getX() {
                        return parentX(this, 0);
                    }

                    @Override
                    public float getY() {
                        return parentYAdd(this, autoRowsYPos(this));
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
                        return rowHolder;
                    }

                    @Override
                    public void render(RenderState state) {
                        super.render(state);
                        //Game icons
                        if(finalRow == 0 && finalColumn > 0) {
                            GameData.spriteBatch.draw(ImageUtil.get("ui/icon/dpad_up"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                        }
                        //Player icons
                        else if(finalRow > 0 && finalColumn == 0) {
                            int index = finalRow-1;
                            if(index < players.size()) {
                                for (CostumePart value : CostumePart.values()) {
                                    if(value.shouldRender()) GameData.spriteBatch.draw(ImageUtil.get("costumes/"+players.get(index).getUser().getWearing(value)), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                                };
                            }
                        }
                        //Totals
                        else if(finalColumn == columns-1) {
                            int index = finalRow-1;
                            if(index < players.size()) {
                                RenderUtil.renderText(String.valueOf(scores.values().stream().toList().get(0).get(players.get(index))), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                            }
                        }
                        //Scores
                        else {
                            int index = finalRow-1;
                            if(index >= 0 && index < players.size() && finalColumn-1 < scores.size()) {
                                RenderUtil.renderText(String.valueOf(scores.values().stream().toList().get(finalColumn-1).get(players.get(index))), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                            }
                            //GameData.spriteBatch.draw(ImageUtil.get("ui/icon/dpad_right"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                        }
                    }
                });
            }
        }

        this.addElement(new Button("menu", "button", "Next game ->") {
            @Override
            public float getX() {
                return GameData.width-getWidth()-10;
            }

            @Override
            public float getY() {
                return getHeight()+10;
            }

            @Override
            public float getWidth() {
                return 360;
            }

            @Override
            public float getHeight() {
                return 72;
            }

            @Override
            public void onClick() {
                super.onClick();
                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new MenuScreen()));
            }
        });

        //syncViewport(GameConstants.width, GameConstants.height);
        //new ScreenSS("0.5vw", "0.5vh", "320", "32", "5", true)
//        this.addElement(new Text("text", "<colour:green>YOU WON!"));
//        this.addElement(new Button("menu2", "button", "Back to menu") {
//            @Override
//            public void onClick() {
//                super.onClick();
//                ToRun.run(() -> GameData.MAIN_INSTANCE.setScreen(new MenuScreen()));
//            }
//        });
//        int i = 0;
//        for (MatchPlayer player : GameData.getCurrentMatch().getPlayers()) {
//            this.addElement(new Text("score"+i, player.getName()+": "+scores.get(player)));
//            i++;
//        }
        //new ScreenSS("0.5vw", "0.7vh", "1", "1", "1", false)
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        // Draw your screen here. "delta" is the time since last render in seconds.
        float worldWidth = GameData.uiViewport.getWorldWidth();
        float worldHeight = GameData.uiViewport.getWorldHeight();

        GameData.spriteBatch.setColor(Color.LIME);
        GameData.spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        GameData.spriteBatch.setColor(Color.WHITE);
        super.render(delta);


        if(firework.isComplete()) {
            firework.dispose();
        } else {
            firework.setPosition(NumberUtils.getRandomFloat(0, GameData.width), NumberUtils.getRandomFloat(-30, 30));
            firework.draw(GameData.spriteBatch, Gdx.graphics.getDeltaTime());
        }
        GameData.spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        GameData.spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        /////font.dispose();
        firework.dispose();
    }
}
