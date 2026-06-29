package com.hugo99j.chaosparty.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.match.MatchView;

import java.util.List;

public class MapEditor extends AbstractMinigame {
    private int lastMiddleX, lastMiddleY;

    public MapEditor(String name) {
        super(name);
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE) && lastMiddleX == -1) {
            lastMiddleX = Gdx.input.getX();
            lastMiddleY = Gdx.input.getY();
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            float zoom = GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom;
            Vector2 pos = new Vector2((float) (lastMiddleX - Gdx.input.getX()) /GameData.width*20*zoom, (float) (lastMiddleY - Gdx.input.getY()) /GameData.height*-10*zoom);
            //GameData.getCurrentMatch().getMatchViews().getFirst().gameViewport.unproject
            GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position.x += pos.x;
            GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.position.y += pos.y;

            lastMiddleX = Gdx.input.getX();
            lastMiddleY = Gdx.input.getY();
        } else {
            lastMiddleX = -1;
            lastMiddleY = -1;
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
        matchViews.add(new MatchView(16, 9, null, false));
    }

    public void scroll(float amountY) {
        GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom += amountY/5*GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom;
        GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom = Math.clamp(GameData.getCurrentMatch().getMatchViews().getFirst().gameCamera.zoom, 0.1f, 10);
    }
}
