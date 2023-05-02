package com.mygdx.game.mode;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Cursor;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.utility.assets.*;

/**
 * A PauseMode is a pause menu screen. User can interact with this screen
 * through keyboard or mouse cursor.
 */
public class PauseMode extends MenuScreen {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private GameMode gameScreen;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** overlay texture */
    private TextureRegion foregroundTexture;

    /** The background tinting color cache */
    private Color overlayTint;

    /** A reference to a text font (changes to any of its properties will be global) */
    private BitmapFont bigFont;

    private BitmapFont smallFont;

    /** exit code to toggle pause state */
    public static final int EXIT_RESUME = 1;

    /** exit code to restart game */
    public static final int EXIT_RESTART = 2;
    /** exit code to restart game */
    public static final int EXIT_MENU = 3;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;


    /** The current state of the level menu button */
    private int menuPressState;
    /** The current state of the restart button */
    private int restartPressState;
    /** The current state of the back button */
    private int backPressState;

    /** exit button*/
    private MenuButton menuButton;
    /** start button */
    private MenuButton restartButton;
    /** back button */
    private MenuButton backButton;

    /** Height of the button */
    private static float BUTTON_SCALE  = 1.0f;
    /** Touch range constant */
    private static float TOUCH_AREA_RATIO = 0.95f;
    private float TAG_SCL = 1;
    /** Scaling factor for when the player changes the resolution. */
    private float scale;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1024;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 576;

    /** Pause text related variables */
    private TextureRegion pauseTag;
    private static float PAUSE_TAG_X_RATIO = .5f;
    private static float PAUSE_TAG_Y_RATIO = .65f;
    private int pauseTagX;
    private int pauseTagY;
    /** Texture for the cursor */
    private TextureRegion cursorTexture;
    public PauseMode(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(1,1,1,0.9f);
        currentExitCode = Integer.MIN_VALUE;

        this.menuButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.37f, 0.25f, 0);
        this.restartButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.63f, 0.25f, 0);
        this.backButton = new MenuButton(MenuMode.ButtonShape.CIRCLE, 0.05f, 0.93f, 0);
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
        foregroundTexture = new TextureRegion(directory.getEntry( "menu:background2", Texture.class ));

        TextureRegion menuTexture = new TextureRegion(directory.getEntry("pause:menu_button", Texture.class));
        TextureRegion restartTexture = new TextureRegion(directory.getEntry("pause:restart_button", Texture.class));
        TextureRegion backButtonTexture = new TextureRegion(directory.getEntry("menu:back_button", Texture.class));

        pauseTag = new TextureRegion(directory.getEntry("pause:pause_tag", Texture.class));

        cursorTexture = new TextureRegion(directory.getEntry("game:cursor", Texture.class));

        menuButton.setTexture(menuTexture);
        restartButton.setTexture(restartTexture);
        backButton.setTexture(backButtonTexture);
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        // Setup
        screenY = canvas.getHeight()-screenY;
        boolean menuPressed = checkClicked2(screenX, screenY, menuButton);
        boolean restartPressed = checkClicked2(screenX, screenY, restartButton);
        boolean backPressed =checkCircleClicked2(screenX, screenY, backButton, BUTTON_SCALE);

        if (menuPressed) {
            menuPressState = 1;
        } else if (restartPressed) {
            restartPressState = 1;
        } else if (backPressed) {
            backPressState = 1;
        }

        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (menuPressState == 1){
            currentExitCode = EXIT_MENU;
            menuPressState = 2;
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        } else if (restartPressState == 1) {
            currentExitCode = EXIT_RESTART;
            restartPressState = 2;
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        } else if (backPressState == 1) {
            currentExitCode = EXIT_RESUME;
            backPressState = 2;
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        }
        return true;
    }

    /**
     * Checks if click was in bound for rectangular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked2(int screenX, int screenY,  MenuButton button) {

        // TODO: TEMPORARY touch range to make it smaller than button
        // Gets positional data of button
        float buttonX = button.getX();
        float buttonY = button.getY();
        float angle = button.getAngle();

        // Gives linear translation for tilted buttons
        float buttonTX = buttonX * (float)Math.cos(angle) + buttonY * (float)Math.sin(angle);
        float buttonTY = -buttonX * (float)Math.sin(angle) + buttonY * (float)Math.cos(angle);
        float screenTX = screenX * (float)Math.cos(angle) + screenY * (float)Math.sin(angle);
        float screenTY = -screenX * (float)Math.sin(angle) + screenY * (float)Math.cos(angle);

        // Checks if appropriate area was clicked
        boolean buttonPressedX = buttonTX - TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2 <= screenTX &&
                screenTX <= buttonTX + TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2;
        boolean buttonPressedY = buttonTY - BUTTON_SCALE*scale*button.getRegionHeight()/2 <= screenTY &&
                screenTY <= buttonTY + BUTTON_SCALE*scale*button.getRegionHeight()/2;

        return buttonPressedX && buttonPressedY;
    }

    /**
     * Checks if click was in bound for circular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkCircleClicked2(float screenX, float screenY, MenuButton button, float scl) {

        float buttonX = button.getX();
        float buttonY = button.getY();
        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);

        // Checks if space inside the circle has been clicked
        return dist < radius*radius;
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
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        gameScreen.draw(delta);
        draw(delta);
    }

    /**
     * Draws static pause menu
     * @param delta The time in seconds since the last render
     */
    private void draw(float delta){
        canvas.setCameraHUD();
        canvas.begin();
        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());


        canvas.draw(pauseTag, Color.WHITE, pauseTag.getRegionWidth()/2f, pauseTag.getRegionHeight()/2f,
                pauseTagX, pauseTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);

        menuButton.draw(canvas, menuPressState, BUTTON_SCALE);
        restartButton.draw(canvas, restartPressState, BUTTON_SCALE);
        backButton.draw(canvas, backPressState, BUTTON_SCALE);

        //draw mouse
        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        canvas.draw(cursorTexture, Color.ORANGE, 0, cursorTexture.getRegionHeight(),
                mx, my, 0, .1f, .1f);

        canvas.end();
    }

    public void resize(int width, int height) {

        // Scaling code from Professor White's code
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        menuButton.setPos(width, height, scale);
        restartButton.setPos(width, height, scale);
        backButton.setPos(width, height, scale);

        pauseTagY = (int)(PAUSE_TAG_Y_RATIO * height);
        pauseTagX = (int)(PAUSE_TAG_X_RATIO * width);
    }

    public void dispose() {
        listener = null;
        gameScreen = null;
        canvas = null;
        foregroundTexture = null;
        overlayTint = null;
        bigFont = null;
        smallFont = null;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.P){
            currentExitCode = EXIT_RESUME;
        }
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        if (currentExitCode > 0) {
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
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

    public void reset() {
        overlayTint = new Color(1,1,1,0.9f);
        currentExitCode = Integer.MIN_VALUE;

    }
}

