package com.hugo99j.chaosparty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.daniel99j.djutil.pathfinder.PathfindDebugType;
import com.daniel99j.djutil.pathfinder.PathfinderOptions;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.match.Match;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.ui.Debuggers;
import com.hugo99j.chaosparty.util.PathUtil;
import com.daniel99j.dungeongame.level.Level;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("GDXJavaStaticResource")
public class GameData {
    public static @Nullable Level level;
    public static final int TICKS_PER_SECOND = 40;
    public static final float SECONDS_PER_TICK = 1.0f/TICKS_PER_SECOND;
    public static final float SECONDS_PER_PHYSICS_TICK = 1.0f/TICKS_PER_SECOND/10.0f;
    public static final SpriteBatch spriteBatch = new SpriteBatch();
    public static final OrthographicCamera uiCamera = new OrthographicCamera();
    public static Viewport uiViewport = new ScreenViewport(uiCamera);
    public static final TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(PathUtil.generated("atlases/main.atlas")));
    public static final boolean DEBUGGING = Objects.equals(System.getenv("DEBUGGING_GAME"), "1") || Files.exists(Path.of("force_debug_game.txt"));
    public static final ShapeRenderer shapeRenderer = new ShapeRenderer();
    public static float time = 0L;
    public static BitmapFont FONT;
    public static BitmapFontCache FONT_CACHE;
    public static Main MAIN_INSTANCE;
    public static int width, height;

    private static Match currentMatch;

    public static Match getCurrentMatch() {
        return currentMatch;
    }

    public static PathfinderOptions.Builder createPathfinding(AbstractObject from) {
        String name = String.valueOf(from.hashCode());
        return PathfinderOptions.builder().diagonalNeighbourProvider().maxIterations(500).debugRenderConsumer(DEBUGGING ? pathfindDebugPos -> {
            Debuggers.pathfindDebuggerTimers.put(name, GameData.TICKS_PER_SECOND*5);
            if (pathfindDebugPos.type().equals(PathfindDebugType.BEGIN_MARKER_NOTREAL)) {
                Debuggers.pathfindDebuggers.put(name, new ArrayList<>());
            } else if (!pathfindDebugPos.type().equals(PathfindDebugType.END_MARKER_NOTREAL)) {
                Debuggers.pathfindDebuggers.get(name).add(pathfindDebugPos);
            }
        } : null);
    }

    public static Level getLevelOrThrow() {
        if(level != null) return level;
        throw new IllegalStateException("World is null");
    }

    protected static void init(Main main) {
        MAIN_INSTANCE = main;

        GameData.spriteBatch.enableBlending();
        GameData.spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        //https://www.1001fonts.com/born2bsporty-fs-font.html
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(PathUtil.asset("font.tff")));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameters.size = 48;
        fontParameters.color = Color.WHITE;
        fontParameters.borderWidth = 2;
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderStraight = true;
        fontParameters.minFilter = Texture.TextureFilter.Nearest;
        fontParameters.magFilter = Texture.TextureFilter.Nearest;

        FONT = fontGenerator.generateFont(fontParameters);
        FONT.getData().markupEnabled = true;
        FONT_CACHE = new BitmapFontCache(FONT);
    }

    public static float px(int pixels) {
        return pixels*0.0625f;
    }

    public static Match startMatch(List<MatchPlayer> players) {
        Match match = new Match(players);
        currentMatch = match;
        return match;
    }
}
