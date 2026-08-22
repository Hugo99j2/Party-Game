package com.hugo99j.chaosparty.ui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.minigame.*;
import com.hugo99j.chaosparty.ui.element.DivElement;
import com.hugo99j.chaosparty.ui.renderable.ClickType;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.ImageUtil;
import com.hugo99j.chaosparty.util.InterpolatedValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hugo99j.chaosparty.ui.ScreenSSFunctions.*;

public class SelectMinigameScreen extends UiScreen {
    private final List<Runnable> inScissorRendering = new ArrayList<>();
    private DivElement mainBody;
    private final InterpolatedValue scrollY = new InterpolatedValue(Interpolation.smooth, 0, 0.1f);

    public SelectMinigameScreen() {
    }

    @Override
    public void show() {
        super.show();
        List<MenuMinigame> games = new ArrayList<>(List.of(
            new MenuMinigame("Sheep", "herd_sheep", "Herd the sheepies and get points!", Color.LIME, HerdSheepMinigame::new),
            new MenuMinigame("Counting", "counting", "Count the clowns and get points!", Color.RED, CountingMinigame::new),
            new MenuMinigame("Disco Floor", "falling_floor", "Don't make a wrong move!!", Color.PURPLE, FallingFloorMinigame::new),
            new MenuMinigame("Hot Potato", "hot_potato", "FIRE! BOOM! POTATOES!", Color.ORANGE, HotPotatoMinigame::new)
        ));

        for (int i = 0; i < 20; i++) {
            games.add(new MenuMinigame("Sheep"+i, "herd_sheep", "Herd the sheepies and get points!", Color.LIME, HerdSheepMinigame::new));
        }

        mainBody = this.addElement(new DivElement("main_body") {
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
                return GameData.width/4f;
            }

            @Override
            public float getHeight() {
                return getWidth();
            }

            @Override
            public float getPadding() {
                return 2;
            }
        });

        DivElement[] columns = {null, null, null};
        for (int i = 0; i < 3; i++) {
            columns[i] = this.addElement(new DivElement("column_"+i) {
                @Override
                public float getX() {
                    return parentX(this, 0)+autoColumnsXPos(this);
                }

                @Override
                public float getY() {
                    return parentY(this, 0)+scrollY.get();
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
                    return 2;
                }

                @Override
                public UiElement getParent() {
                    return mainBody;
                }
            });
        }

        for (MenuMinigame game : games) {
            int myColumn = games.indexOf(game) % 3;
            UiElement column = columns[myColumn];
            this.addElement(new UiElement(game.title) {
                @Override
                public void setScreen(UiScreen screen) {
                    super.setScreen(screen);
                    this.usesMouse = true;
                }

                @Override
                public float getX() {
                    return parentX(this, 0);
                }

                @Override
                public float getY() {
                    return parentY(this, 100)-getYNonParented();
                }

                private float getYNonParented() {
                    return this.getHeight()*(float)Math.floor(1 + (games.indexOf(game) / 3f));
                }

                @Override
                public float getWidth() {
                    return parentWidth(this, 100);
                }

                @Override
                public float getHeight() {
                    return getWidth();
                }

                @Override
                public UiElement getParent() {
                    return column;
                }

                @Override
                public void render(RenderState state) {
                    inScissorRendering.add(() -> {
                        super.render(state);
                        GameData.spriteBatch.draw(ImageUtil.get("ui/minigame_selector/deselected"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                        GameData.spriteBatch.draw(ImageUtil.get("ui/minigame_selector/" + game.icon), this.getX(), this.getY() + this.getHeight() / 2f, this.getWidth(), this.getHeight() / 2f);
                        GameData.spriteBatch.draw(ImageUtil.get("ui/minigame_selector/overlay"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                        Color old = GameData.spriteBatch.getColor().cpy();
                        GameData.spriteBatch.setColor(game.colour);
                        if (this.isSelected())
                            GameData.spriteBatch.draw(ImageUtil.get("ui/minigame_selector/selected"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                        GameData.spriteBatch.setColor(old);
                    });
                    if(this.isSelected() && this.getY() < mainBody.getY() && scrollY.isDone()) {
                        scrollY.goTo(scrollY.get()+this.getHeight());
                    }
                    if(this.isSelected() && this.getY()+this.getHeight() > mainBody.getY()+mainBody.getHeight() && scrollY.isDone()) {
                        scrollY.goTo(scrollY.get()-this.getHeight());
                    }
                }

                @Override
                public void onDown(int relativeX, int relativeY, ClickType type) {
                    super.onDown(relativeX, relativeY, type);
                    MenuScreen.start(game.run.get());
                }

                @Override
                public void onControllerSelect() {
                    super.onControllerSelect();
                    //if(this.isSelected() && !isInsideArea(this.getX(), this.getY(), this.getX()+this.getWidth(), this.getY()+this.getHeight())) {
                    //206
                    //}
                }
            });
        }
    }

    public boolean isInsideArea(float x, float y, float maxX, float maxY) {
        return x >= mainBody.getX() && maxX <= mainBody.getX()+mainBody.getWidth() && maxY >= mainBody.getY() && y <= mainBody.getY()+mainBody.getHeight();
    }

    @Override
    public void render(float delta) {
        GameData.spriteBatch.begin();
        inScissorRendering.clear();
        super.render(delta);
        GameData.spriteBatch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clip = new Rectangle(mainBody.getX(), mainBody.getY(), mainBody.getWidth(), mainBody.getHeight());
        ScissorStack.calculateScissors(GameData.uiCamera, GameData.spriteBatch.getTransformMatrix(), clip, scissors);
        if(ScissorStack.pushScissors(scissors)) {
            inScissorRendering.forEach(Runnable::run);
            GameData.spriteBatch.flush();
            ScissorStack.popScissors();
        }
        GameData.spriteBatch.end();
    }

    private record MenuMinigame(String title, String icon, String desc, Color colour, Supplier<AbstractMinigame> run) {}
}
