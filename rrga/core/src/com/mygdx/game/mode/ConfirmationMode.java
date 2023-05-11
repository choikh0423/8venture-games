package com.mygdx.game.mode;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A PauseMode is a pause menu screen. User can interact with this screen
 * through keyboard or mouse cursor.
 */
public class ConfirmationMode extends MenuScreen {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private MenuScreen gameScreen;

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

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

    private int prevModeExitCode;

    /** The current state of the yes button */
    private int yesPressState;
    /** The current state of the no button */
    private int noPressState;

    /** yes button*/
    private MenuButton yesButton;
    /** no button */
    private MenuButton noButton;

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
    private TextureRegion confirmationTag;
    private TextureRegion popup;
    private static float CONFIRMATION_TAG_X_RATIO = .5f;
    private static float CONFIRMATION_TAG_Y_RATIO = .65f;
    private int confirmationTagX;
    private int confirmationTagY;
    private int popupX;
    private int popupY;

    public static final int EXIT_LEVEL = 2;
    public static final int EXIT_SETTINGS = 3;
    public static final int EXIT_PAUSE = 4;

    private Music music;
    private float volume;
    private TextureRegion cursorTexture;

    public void setMusic(Music music){this.music=music;}
    public void setVolume(float vol){volume=vol;}

    public void setPreviousExitCode(int code){
        prevModeExitCode = code;
    }

    public ConfirmationMode(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(0,0,0,0.6f);
        currentExitCode = Integer.MIN_VALUE;

        this.yesButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.37f, 0.4f, 0);
        this.noButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.63f, 0.4f, 0);
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
        foregroundTexture = new TextureRegion(directory.getEntry("game:platform", Texture.class));
        cursorTexture = new TextureRegion(directory.getEntry( "menu:cursor_menu", Texture.class ));

        popup = new TextureRegion(directory.getEntry("menu:popup", Texture.class));
        TextureRegion yesTexture = new TextureRegion(directory.getEntry("menu:checkmark", Texture.class));
        TextureRegion noTexture = new TextureRegion(directory.getEntry("menu:x", Texture.class));

        confirmationTag = new TextureRegion(directory.getEntry("menu:confirm_text", Texture.class));

        yesButton.setTexture(yesTexture);
        noButton.setTexture(noTexture);
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        // Setup
        screenY = canvas.getHeight()-screenY;
        boolean yesPressed = checkClicked2(screenX, screenY, yesButton);
        boolean noPressed = checkClicked2(screenX, screenY, noButton);

        if (yesPressed) {
            yesPressState = 1;
        } else if (noPressed) {
            noPressState = 1;
        }

        return false;
    }

    /** preferences object to store user settings */
    Preferences settings = Gdx.app.getPreferences("settings");
    /** preferences object to store which levels the user has unlocked */
    Preferences unlocked = Gdx.app.getPreferences("unlocked");
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (yesPressState == 1){
            currentExitCode = prevModeExitCode;
            yesPressState = 2;
            if (prevModeExitCode == EXIT_PAUSE || prevModeExitCode == EXIT_SETTINGS){
                settings.putFloat("musicVolume", 0.5f);
                settings.putFloat("sfxVolume", 0.5f);
                settings.putBoolean("toggle", false);
                settings.flush();
            } else if (prevModeExitCode == EXIT_LEVEL){
                for (int i = 2; i <= MenuMode.LEVEL_COUNT; i++){
                    unlocked.putBoolean(i+"unlocked", false);
                }
                unlocked.flush();
            }
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        } else if (noPressState == 1) {
            currentExitCode = prevModeExitCode;
            noPressState = 2;
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
        gameScreen.draw();
        draw(delta);
    }

    /**
     * Draws static pause menu
     * @param delta The time in seconds since the last render
     */
    private void draw(float delta){
        canvas.begin();
        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.draw(popup, Color.WHITE, popup.getRegionWidth()/2f, popup.getRegionHeight()/2f,
                popupX, popupY, 0, scale, scale);
        canvas.draw(confirmationTag, Color.WHITE, confirmationTag.getRegionWidth()/2f, confirmationTag.getRegionHeight()/2f,
                confirmationTagX, confirmationTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);

        yesButton.draw(canvas, yesPressState, BUTTON_SCALE, Color.WHITE);
        noButton.draw(canvas, noPressState, 1.3f, Color.WHITE);

        //draw cursor
        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if(mx<Gdx.graphics.getWidth() && mx>0 && my<Gdx.graphics.getHeight() && my>0) {
            canvas.draw(cursorTexture, Color.WHITE, 0, cursorTexture.getRegionHeight(),
                    mx, my, 0, .4f, .4f);
        }

        canvas.end();
    }

    public void resize(int width, int height) {

        // Scaling code from Professor White's code
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        yesButton.setPos(width, height, scale);
        noButton.setPos(width, height, scale);

        confirmationTagY = (int)(CONFIRMATION_TAG_Y_RATIO * height);
        confirmationTagX = (int)(CONFIRMATION_TAG_X_RATIO * width);
        popupY = (int)((CONFIRMATION_TAG_Y_RATIO-0.05f) * height);
        popupX = (int)(CONFIRMATION_TAG_X_RATIO * width);
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

    public void setBackgroundScreen(MenuScreen gameScreen){
        this.gameScreen = gameScreen;
    }

    public MenuScreen getBackgroundScreen(){
        return this.gameScreen;
    }

    public void reset() {
        overlayTint = new Color(0,0,0,0.6f);
        currentExitCode = Integer.MIN_VALUE;
        music.setVolume(volume);
        music.play();
    }
}


