package com.mygdx.game.mode;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;
import org.w3c.dom.Text;

public class MenuMode extends MenuScreen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private GameMode gameScreen;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** Background texture */
    private TextureRegion backgroundTexture;
    /** level button (TEMPORARY) */
    private TextureRegion levelSelectButton;
    /** exit button */
    private TextureRegion exitButton;
    /** exit button 2 */
    // Temporary implementation of UI
    private TextureRegion exitButton2;
    /** settings button */
    private TextureRegion settingsButton;
    /** level 1 button */
    private TextureRegion levelButton1;
    /** level 2 button */
    private TextureRegion levelButton2;
    /** current selected level */
    private int currentLevel;



    /** Background music */
    private Music backgroundMusic;

    /** exit code to terminate game */
    public static final int EXIT_QUIT = 0;

    /** exit code to play game */
    public static final int EXIT_PLAY = 1;

    /** exit code to game settings */
    public static final int EXIT_SETTINGS = 2;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;
    /** Tracker for checking if menu is in levelSelect Mode  */
    private int levelSelectMode;

    /** The current state of the level select button */
    private int selectPressState;
    /** The current state of the settings button */
    private int settingsPressState;
    /** The current state of the exit button */
    private int exitPressState;
    /** The current state of the exit button */
    private int exitPressState2;
    /** The current state of the exit button */
    private int levelPressState1;
    /** The current state of the exit button */
    private int levelPressState2;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Scaling factor for when the player changes the resolution. */
    private float scale;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Height of the button */
    private static float BUTTON_SCALE  = 1.2f;
    /** The width of the button */
    private int buttonWidth;
    /** Ratio of the button width to the screen */
    private static float BUTTON_WIDTH_RATIO  = 0.66f;
    /** Ration of the button height to the screen */
    private static float Button_X_RATIO = 0.2f;
    /** Ration of the level select height to the screen */
    private static float SELECT_X_RATIO = 0.63f;
    /** Ration of the level select button height to the screen */
    private static float SELECT_Y_RATIO = 0.2f;
    /** Ration of the level select button angle */
    private static float SELECT_ANGLE = -0.05f * 3.14f;
    /** The y-coordinate of the center of the level select button */
    private int selectY;
    /** The x-coordinate of the center of the level select button */
    private int selectX;
    /** Ration of the settings button height to the screen */
    private static float SETTINGS_X_RATIO = 0.95f;
    /** Ration of the settings button height to the screen */
    private static float SETTINGS_Y_RATIO = 0.07f;
    /** The y-coordinate of the center of the settings button */
    private int settingsY;
    /** The x-coordinate of the center of the settings button */
    private int settingsX;
    /** Ration of the button height to the screen */
    private static float EXIT_X_RATIO = 0.05f;
    /** Ration of the exit button height to the screen */
    private static float EXIT_Y_RATIO = 0.93f;
    /** The exit button angle */
    private static float EXIT_ANGLE = 0.05f * 3.14f;
    /** The y-coordinate of the center of the settings button */
    private int exitY;
    /** The x-coordinate of the center of the settings button */
    private int exitX;

    // TEMPORARY IMPLEMENTATION
    /** Ration of the button height to the screen */
    private static float EXIT_X_RATIO2 = 0.37f;
    /** Ration of the exit button height to the screen */
    private static float EXIT_Y_RATIO2 = 0.2f;
    /** Ration of the exit button angle */
    private static float EXIT_ANGLE2 = 0.05f * 3.14f;

    /** The y-coordinate of the center of the settings button */
    private int exitY2;
    /** The x-coordinate of the center of the settings button */
    private int exitX2;


    /** The y-coordinate of the center of the level 1 button */
    private int levelY1;
    /** The x-coordinate of the center of the level 1 button */
    private int levelX1;
    /** The y-coordinate of the center of the level 2 button */
    private int levelY2;
    /** The x-coordinate of the center of the level 2 button */
    private int levelX2;
    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
        currentExitCode = Integer.MIN_VALUE;
        this.levelSelectMode = 1;
    }

    public void gatherAssets(AssetDirectory directory) {
        //TODO: texture is unnecessary, use shapes (see prof White's lectures on drawing shapes without textures)
        backgroundTexture = new TextureRegion(directory.getEntry( "menu:background", Texture.class ));
        levelSelectButton = new TextureRegion(directory.getEntry("menu:level_select_button", Texture.class));
        settingsButton = new TextureRegion(directory.getEntry("menu:settings_button", Texture.class));
        exitButton = new TextureRegion(directory.getEntry("menu:exit_button", Texture.class));
        exitButton2 = new TextureRegion(directory.getEntry("menu:level_select_button", Texture.class));

        levelButton1 = new TextureRegion(directory.getEntry("menu:level1_button", Texture.class));
        levelButton2 = new TextureRegion(directory.getEntry("menu:level2_button", Texture.class));

        backgroundMusic = directory.getEntry("music:menu", Music.class);
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        screenY = heightY-screenY;

        if (levelSelectMode == 1) {
            boolean selectPressed = checkClicked(screenX, screenY, selectX, selectY, levelSelectButton);
            boolean settingsPressed = checkClicked(screenX, screenY, settingsX, settingsY, settingsButton);
            boolean exitPressed = checkClicked(screenX, screenY, exitX, exitY, exitButton);
            boolean exitPressed2 = checkClicked(screenX, screenY, exitX, exitY, exitButton2);


            if (selectPressed) {
                selectPressState = 1;
            } else if (settingsPressed) {
                settingsPressState = 1;
            } else if (exitPressed) {
                exitPressState = 1;
            } else if (exitPressed2) {
                exitPressState = 1;
            }
        } else if (levelSelectMode == 2) {
            boolean levelPressed1 = checkClicked(screenX, screenY, levelX1, levelY1, levelButton1);
            boolean levelPressed2 = checkClicked(screenX, screenY, levelX2, levelY2, levelButton2);

            if (levelPressed1) {
                levelPressState1 = 1;
            } else if (levelPressed2) {
                levelPressState2 = 1;
            }

        }

        return false;
    }
    /**
     * Checks if click was in bound for buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked(int screenX, int screenY, int buttonX, int buttonY, TextureRegion button) {
        boolean buttonPressedX = buttonX - BUTTON_SCALE*scale*button.getRegionWidth()/2 <= screenX &&
                screenX <= buttonX + BUTTON_SCALE*scale*button.getRegionWidth()/2;
        boolean buttonPressedY = buttonY - BUTTON_SCALE*scale*button.getRegionHeight()/2 <= screenY &&
                screenY <= buttonY + BUTTON_SCALE*scale*button.getRegionHeight()/2;

        return buttonPressedX && buttonPressedY;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        System.out.println("Touch up");

        if (selectPressState == 1) {
            selectPressState = 2;
            levelSelectMode = 2;
            return false;
        } else if (settingsPressState == 1) {
            settingsPressState = 2;
            currentExitCode = EXIT_SETTINGS;
            listener.exitScreen(this, currentExitCode);
        } else if (exitPressState == 1) {
            exitPressState = 2;
            currentExitCode = EXIT_QUIT;
            listener.exitScreen(this, currentExitCode);
        } else if (exitPressState2 == 1) {
            exitPressState2 = 2;
            currentExitCode = EXIT_QUIT;
            listener.exitScreen(this, currentExitCode);
        }

        // TEMPORARY NEED CHANGE
        if (levelPressState1 == 1) {
            currentLevel = 0;
            levelPressState1 = 2;
            currentExitCode = EXIT_PLAY;
            listener.exitScreen(this, currentExitCode);
        } else if (levelPressState2 == 1) {
            currentLevel = 1;
            levelPressState2 = 2;
            currentExitCode = EXIT_PLAY;
            listener.exitScreen(this, currentExitCode);
        }
        return true;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(backgroundTexture, 0, 0);

        if (levelSelectMode == 1) {
            // Draw Level Select Button
            Color selectTint = (selectPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelSelectButton, selectTint, levelSelectButton.getRegionWidth() / 2, levelSelectButton.getRegionHeight() / 2,
                    selectX, selectY, SELECT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Settings Button
            Color settingsTint = (settingsPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(settingsButton, settingsTint, settingsButton.getRegionWidth() / 2, settingsButton.getRegionHeight() / 2,
                    settingsX, settingsY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Exit Button 1
            Color exitTint = (exitPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(exitButton, exitTint, exitButton.getRegionWidth() / 2, exitButton.getRegionHeight() / 2,
                    exitX, exitY, EXIT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Exit Button 2
            Color exitTint2 = (exitPressState2 == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(exitButton2, exitTint2, exitButton2.getRegionWidth() / 2, exitButton2.getRegionHeight() / 2,
                    exitX2, exitY2, EXIT_ANGLE2, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        } else if (levelSelectMode == 2){
            // Temporary Implementation - Will change to iterables once we get proper textures
            Color levelTint1 = (levelPressState1 == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelButton1, levelTint1, levelButton1.getRegionWidth() / 2, levelButton1.getRegionHeight() / 2,
                    levelX1, levelY1, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

            Color levelTint2 = (levelPressState2 == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelButton2, levelTint2, levelButton2.getRegionWidth() / 2, levelButton2.getRegionHeight() / 2,
                    levelX2, levelY2, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        }
        canvas.end();
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        // TO DO: Move this if necessary
//        backgroundMusic.play();
        backgroundMusic.setLooping(true);

        draw();
        // transition
        if (currentExitCode >= 0){
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Scaling code from Professor White's code
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        this.buttonWidth = (int)(BUTTON_WIDTH_RATIO*width);
        heightY = height;
        selectY = (int)(SELECT_Y_RATIO * height);
        selectX = (int)(SELECT_X_RATIO * width);
        settingsY = (int)(SETTINGS_Y_RATIO * height);
        settingsX = (int)(SETTINGS_X_RATIO * width);
        exitY = (int)(EXIT_Y_RATIO * height);
        exitX = (int)(EXIT_X_RATIO * width);
        exitY2 = (int)(EXIT_Y_RATIO2 * height);
        exitX2 = (int)(EXIT_X_RATIO2 * width);

        // TEMPORARY
        levelY1 = (int)(0.5 * height);
        levelX1 = (int)(0.25 * width);
        levelY2 = (int)(0.5 * height);
        levelX2 = (int)(0.75 * width);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public void dispose() {
        listener = null;
        canvas = null;
        backgroundMusic.stop();
        // NEED TO ADD
    }
}
