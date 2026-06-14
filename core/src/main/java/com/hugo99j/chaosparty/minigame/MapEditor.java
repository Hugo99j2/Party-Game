package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;

import java.util.List;

public class MapEditor extends AbstractMinigame {
    private int startMiddleX, startMiddleY;
    private float zoom;

    public MapEditor(String name) {
        super(name);
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) && startMiddleX == -1) {
            startMiddleX = Gdx.input.getX();
            startMiddleY = Gdx.input.getY();
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position.x = Gdx.input.getX() - startMiddleX;
            GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position.y = Gdx.input.getY() - startMiddleY;
        } else {
            startMiddleX = -1;
            startMiddleY = -1;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public MinigameScreenLayout getLayout() {
        return MinigameScreenLayout.SINGLE;
    }

    @Override
    public void setupViews(List<MatchView> matchViews) {
        matchViews.add(new MatchView());
    }
}
