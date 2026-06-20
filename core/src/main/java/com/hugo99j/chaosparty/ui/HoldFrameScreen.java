package com.hugo99j.chaosparty.ui;

import com.badlogic.gdx.graphics.Texture;
import com.daniel99j.dungeongame.ui.screenss.CombinedScreenSS;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.RenderUtil;

public abstract class HoldFrameScreen extends UiScreen {
    private final Texture frame;

    public HoldFrameScreen(CombinedScreenSS combinedScreenSS) {
        super(combinedScreenSS);
        frame = RenderUtil.getCurrentFrameBuffer();
    }

    @Override
    public void render(float delta) {
        RenderUtil.getBlurProgram().setUniformf("u_resolution", GameData.width, GameData.height);
        GameData.spriteBatch.setShader(RenderUtil.getBlurProgram());
        GameData.spriteBatch.begin();
        GameData.spriteBatch.draw(frame, 0, GameData.height, GameData.width, -GameData.height);
        GameData.spriteBatch.end();
        GameData.spriteBatch.setShader(null);
        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        frame.dispose();
    }
}
