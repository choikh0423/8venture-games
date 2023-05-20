package com.mygdx.game.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.CameraController;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;

/**
 * A PauseMode is a pause menu screen. User can interact with this screen
 * through keyboard or mouse cursor.
 */
public class CutSceneMode extends MenuScreen {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private GameMode gameScreen;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** The background tinting color cache */
    private Color overlayTint;

    /** exit code for returning to game */
    public static final int EXIT_RESUME = 1;
    /** exit code to toggle pause state */
    public static final int EXIT_MENU = 2;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

    private float TAG_SCL = 0.6f;
    /** Scaling factor for when the player changes the resolution. */
    private float scale;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1024;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 576;

    /** Pause text related variables */
    private TextureRegion skipTag;
    private static float SKIP_X_RATIO = .8f;
    private static float SKIP_Y_RATIO = .07f;
    private int skipTagX;
    private int skipTagY;
    /** Texture for the cursor */
    private TextureRegion cursorTexture;
    /** true until the first call to render*/
    public boolean first;
    /** Current level in game*/
    public int currentLevel;

    /** Number of frames for each cutscene*/
    private final int[] cutsceneFrameCount = new int[]{4, 10, 5, 6, 4, 0, 2, 44, 8, 2, 15, 10};
    /** Cutscene animation elapsed time */
    private float sceneElapsedTime;
    /** Remaining number of scenes to play */
    private int numScenes;
    /** currentSceneNumber*/
    private int currentSceneNumber;
    /** Cutscene animation */
    private Animation<TextureRegion> sceneAnimation;

    private Array<TextureRegion[]> textureList = new Array<>(12);
    public CutSceneMode(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(1,1,1,1f);
        currentExitCode = Integer.MIN_VALUE;
        first = true;

        int width = (int) canvas.getCamera().getViewWidth();
        int height = (int) canvas.getCamera().getViewHeight();
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = Math.min(sx, sy);

        skipTagY = (int)(SKIP_Y_RATIO * height);
        skipTagX = (int)(SKIP_X_RATIO * width);

        sceneElapsedTime = 0;
    }

    /**
     * Gather the assets for this controller.
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory    Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        //TODO: texture is unnecessary, use shapes (see prof White's lectures on drawing shapes without textures)
        skipTag= new TextureRegion(directory.getEntry("cutscene:skip", Texture.class));

        // List of cutscene textures
        for (int i = 0; i < cutsceneFrameCount.length; i++) {
            textureList.add(new TextureRegion[cutsceneFrameCount[i]]);
        }

        // Populating cutscenes
        for (int i = 1; i <= cutsceneFrameCount.length; i++) {
            TextureRegion[] tempFrame = new TextureRegion[cutsceneFrameCount[i-1]];
            for (int j = 1; j <= cutsceneFrameCount[i-1]; j++) {
                tempFrame[j-1] = new TextureRegion(directory.getEntry("cutscene:scene"+i+"_frame"+j, Texture.class));
            }
            textureList.set(i-1, tempFrame);
        }

        cursorTexture = new TextureRegion(directory.getEntry("menu:cursor_menu", Texture.class));

    }

    /**
     * Draw the Pause menu and exit pause mode if possible.
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
//        if (background != null){
//            background.render(delta);
//        }

        //Gdx.input.setCursorCatched(false);
//        int x=0, y=0;
//        if(first) {
//            x = Gdx.input.getX();
//            y = Gdx.input.getY();
//        }
//        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
//        if(first){
//            Gdx.input.setCursorPosition(x, y);
//            first = false;
//        }

        //gameScreen.draw(delta);
        draw(delta);
    }

    /**
     * Draws static pause menu
     * @param delta The time in seconds since the last render
     */
    private void draw(float delta){
        canvas.begin();
//        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());
        CameraController camera = canvas.getCamera();

        setAnimation();
        sceneElapsedTime += Gdx.graphics.getDeltaTime();
        TextureRegion t;
        t = sceneAnimation.getKeyFrame(sceneElapsedTime, true);
        canvas.draw(t, overlayTint, 0, 0, camera.getViewWidth(), camera.getViewHeight());

        if (sceneElapsedTime > 2) {
            canvas.draw(skipTag, Color.WHITE, skipTag.getRegionWidth() / 2f, skipTag.getRegionHeight() / 2f,
                    skipTagX, skipTagY, 0, TAG_SCL * scale, TAG_SCL * scale);
        }

        //draw mouse texture
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        // retrieve the viewport coordinate to draw cursor
        Vector2 pos = camera.unproject(mx, my);
        if(pos.x <= camera.getViewWidth() && pos.x>= 0 && pos.y < camera.getViewHeight() && pos.y >0) {
            canvas.draw(cursorTexture, Color.WHITE, 0, cursorTexture.getRegionHeight(),
                    pos.x, pos.y, 0, .4f, .4f);
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        }
        else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }

        canvas.end();
    }

    public void resize(int width, int height) {
        // resizing done through viewport
    }

    public void dispose() {
        listener = null;
        gameScreen = null;
        canvas = null;
        overlayTint = null;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE){
            //TODO: Change this if total number of levels change
            if (sceneElapsedTime > 2) {
                if (numScenes <= 1) {
                    if (currentLevel != 30) {
                        currentExitCode = EXIT_RESUME;
                    } else {
                        currentExitCode = EXIT_MENU;
                    }
                } else {
                    sceneElapsedTime = 0;
                    numScenes -= 1;
                    currentSceneNumber += 1;
                    setAnimation();
                }
            }
        }
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        if (currentExitCode > 0) {
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
            sceneElapsedTime = 0;
        }

        return false;
    }

    /**
     * Sets the ScreenListener for this mode.
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener){
        this.listener = listener;
    }

    public void setBackgroundScreen(GameMode gameScreen){
        this.gameScreen = gameScreen;
    }

    public GameMode getBackgroundScreen(){
        return this.gameScreen;
    }

    public void setCurrentScene(int sceneNumber) {
        this.currentSceneNumber = sceneNumber;
        if (sceneNumber == 1) {
            numScenes = 2;
        } else if(sceneNumber == 8) {
            numScenes = 5;
        } else {
            numScenes = 1;
        }
    }

    public void setAnimation() {
        TextureRegion[] frames = textureList.get(currentSceneNumber-1);

        // Adjust idle animation speed here
        if(currentSceneNumber == 1 || currentSceneNumber == 2) {
            sceneAnimation = new Animation<>(1f / 5f, textureList.get(currentSceneNumber - 1));
        } else if(currentSceneNumber == 3) {
            sceneAnimation = new Animation<>(1f / 7f, textureList.get(currentSceneNumber - 1));
        }  else if (currentSceneNumber == 4) {
            sceneAnimation = new Animation<>(1f / 1f, textureList.get(currentSceneNumber - 1));
        } else if (currentSceneNumber == 5) {
            sceneAnimation = new Animation<>(1f / 7f, textureList.get(currentSceneNumber - 1));
        } else if (currentSceneNumber == 8) {
            sceneAnimation = new Animation<>(1f / 7f, textureList.get(currentSceneNumber - 1));
        } else if (currentSceneNumber == 9 || currentSceneNumber == 11 || currentSceneNumber == 12) {
            sceneAnimation = new Animation<>(1f / 4f, textureList.get(currentSceneNumber - 1));
        } else {
            sceneAnimation = new Animation<>(1f / 2f, textureList.get(currentSceneNumber - 1));
        }
    }

    public void setCurrentLevel(int level) {
        currentLevel = level;
    }

    public void reset() {
        overlayTint = new Color(1,1,1,0.9f);
        currentExitCode = Integer.MIN_VALUE;
    }
}
