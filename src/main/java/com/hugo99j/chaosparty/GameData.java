package com.hugo99j.chaosparty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.daniel99j.djutil.pathfinder.PathfindDebugType;
import com.daniel99j.djutil.pathfinder.PathfinderOptions;
import com.daniel99j.dungeongame.entity.AbstractObject;
import com.hugo99j.chaosparty.match.Match;
import com.hugo99j.chaosparty.match.MatchPlayer;
import com.hugo99j.chaosparty.minigame.AbstractMinigame;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.PathUtil;
import com.daniel99j.dungeongame.level.Level;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("GDXJavaStaticResource")
public class GameData {
    public static @Nullable Level level;
    public static final int TICKS_PER_SECOND = 40;
    public static final float SECONDS_PER_TICK = 1.0f/TICKS_PER_SECOND;
    public static final int PHYSICS_TICKS_PER_TICK = 10;
    public static final float SECONDS_PER_PHYSICS_TICK = 1.0f/TICKS_PER_SECOND/PHYSICS_TICKS_PER_TICK;
    public static final SpriteBatch spriteBatch = new SpriteBatch();
    public static final OrthographicCamera uiCamera = new OrthographicCamera();
    public static Viewport uiViewport = new ScreenViewport(uiCamera);
    public static final TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(PathUtil.generated("atlases/main.atlas")));
    public static final boolean DEBUGGING = Objects.equals(System.getenv("DEBUGGING_GAME"), "1") || Files.exists(Path.of("force_debug_game.txt"));
    public static final ShapeRenderer shapeRenderer = new ShapeRenderer();
    public static float time = 0L;
    public static BitmapFont FONT;
    public static Main MAIN_INSTANCE;
    public static int width, height;

    private static final Map<String, Character> icons = new HashMap<>();
    private static final Map<Integer, Character> spaces = new HashMap<>();

    private static Match currentMatch;

    //Settings
    public static float VIBRATE_INTERVAL = 0.02f;

    public static Match getCurrentMatch() {
        return currentMatch;
    }

    public static AbstractMinigame getCurrentMinigame() {
        if(getCurrentMatch() == null) return null;
        return getCurrentMatch().getCurrentMinigame();
    }

    public static PathfinderOptions.Builder createPathfinding(AbstractObject from) {
        return PathfinderOptions.builder().diagonalNeighbourProvider().heuristicFunction((f, t) -> (double)(Math.abs(f.getX() - t.getX()) + Math.abs(f.getY() - t.getY()))*3).maxIterations(1000).debugRenderConsumer(DEBUGGING ? pathfindDebugPos -> {
            Debuggers.pathfindDebuggerTimers.put(from.getUUID(), GameData.TICKS_PER_SECOND*5);
            if (pathfindDebugPos.type().equals(PathfindDebugType.BEGIN_MARKER_NOTREAL)) {
                Debuggers.pathfindDebuggers.put(from.getUUID(), new ArrayList<>());
            } else if (!pathfindDebugPos.type().equals(PathfindDebugType.END_MARKER_NOTREAL)) {
                Debuggers.pathfindDebuggers.get(from.getUUID()).add(pathfindDebugPos);
            }
        } : null);
    }

    public static Level getLevelOrThrow() {
        if(level != null) return level;
        throw new IllegalStateException("World is null");
    }

    public static Map<String, Character> getIcons() {
        return icons;
    }

//    public static String appendSpace(String text, int space) {
//        //((BitmapFont) GameData.FONT).getCache().addText()
//    }

    public static String getSpace(int space) {
        //GlyphLayout#getGlyphWidth
        StringBuilder out = new StringBuilder();
        while (space != 0) {
            if(space <= -100) {
                out.append(spaces.get(-100));
                space += 100;
            } else if(space <= -50) {
                out.append(spaces.get(-50));
                space += 50;
            } else if(space <= -10) {
                out.append(spaces.get(-10));
                space += 10;
            } else if(space <= -1) {
                out.append(spaces.get(-1));
                space += 1000;
            } else if(space >= 100) {
                out.append(spaces.get(100));
                space -= 100;
            } else if(space >= 50) {
                out.append(spaces.get(50));
                space -= 50;
            } else if(space >= 10) {
                out.append(spaces.get(10));
                space -= 10;
            } else //noinspection ConstantValue
                if(space >= 1) {
                out.append(spaces.get(1));
                space -= 1;
            }
        }
        return out.toString();
    }

    protected static void init(Main main) {
        MAIN_INSTANCE = main;

        char current = '\uE000';
        for (String s : PathUtil.getFilesIn(PathUtil.texture("ui/icon"))) {
            icons.put(s.replace("assets/textures/ui/icon/", "").replace(".png", ""), current);
            current++;
        }
        List<Integer> spacing = new ArrayList<>(List.of(-100, -50, -10, -1, 1, 10, 50, 100));
        for (Integer i : spacing) {
            icons.put("__space__"+i, current);
            spaces.put(i, current);
            current++;
        }

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


        FreeTypeFontGenerator.FreeTypeBitmapFontData data = new FreeTypeFontGenerator.FreeTypeBitmapFontData();
        FONT = fontGenerator.generateFont(fontParameters, data);
        FONT.getData().markupEnabled = true;
        FONT.setUseIntegerPositions(false);
    }

    public static float px(int pixels) {
        return pixels*0.0625f;
    }

    public static float px(Number pixels) {
        return px(pixels.intValue());
    }

    public static Match startMatch(List<MatchPlayer> players) {
        Match match = new Match(players);
        currentMatch = match;
        return match;
    }
}
