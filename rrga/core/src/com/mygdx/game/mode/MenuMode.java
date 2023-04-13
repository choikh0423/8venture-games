package com.mygdx.game.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.MySlider;
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
    /** Level Selector Background texture */
    private TextureRegion backgroundTexture2;
    /** level button (TEMPORARY) */
    private TextureRegion levelSelectButton;
    /** back button */
    private TextureRegion backButton;
    /** exit button */
    private TextureRegion exitButton;
    /** start button */
    // Temporary implementation of UI
    private TextureRegion startButton;
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
    /** Music volume */
    private float musicVolume;
    /** SFX volume */
    private float sfxVolume;

    /** exit code to terminate game */
    public static final int EXIT_QUIT = 0;

    /** exit code to play game */
    public static final int EXIT_PLAY = 1;

    /** exit code to game settings */
    public static final int EXIT_SETTINGS = 2;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;
    /** Tracker for checking which screen the menu screen is showing
     *  1: main menu
     *  2: level selector
     *  3: settings
     * */
    private int screenMode;

    /** The current state of the level select button */
    private int selectPressState;
    /** The current state of the settings button */
    private int settingsPressState;
    /** The current state of the exit button */
    private int exitPressState;
    /** The current state of the exit button */
    private int startPressState;
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
    /** Ration of the exit button width to the screen */
    private static float EXIT_X_RATIO = 0.05f;
    /** Ration of the exit button height to the screen */
    private static float EXIT_Y_RATIO = 0.93f;
    /** The exit button angle */
    private static float EXIT_ANGLE = 0.05f * 3.14f;
    /** The y-coordinate of the center of the settings button */
    private int exitY;
    /** The x-coordinate of the center of the settings button */
    private int exitX;
    /** Ration of the start button width to the screen */
    private static float START_X_RATIO = 0.37f;
    /** Ration of the start button height to the screen */
    private static float START_Y_RATIO = 0.2f;
    /** Ration of the start button angle */
    private static float START_ANGLE = 0.05f * 3.14f;
    /** The y-coordinate of the center of the start  button */
    private int startY;
    /** The x-coordinate of the center of the start button */
    private int startX;

    /** The y-coordinate of the center of the level 1 button */
    private int levelY1;
    /** The x-coordinate of the center of the level 1 button */
    private int levelX1;
    /** The y-coordinate of the center of the level 2 button */
    private int levelY2;
    /** The x-coordinate of the center of the level 2 button */
    private int levelX2;

    /** Stage for setting features */
    private Stage settingStage;
    /** Music volume slider bar texture */
    private TextureRegion musicSliderBar;
    /** Music volume slider knob texture */
    private TextureRegion musicSliderKnob;
    /** The y-coordinate of the center of the music slider */
    private int musicSliderY;
    /** The x-coordinate of the center of the music slider */
    private int musicSliderX;
    /** Ratio of the music slider height to the screen */
    private static float MUSIC_Y_RATIO = 0.6f;
    /** Ratio of the music slider width to the screen */
    private static float MUSIC_X_RATIO = 0.2f;
    /** SFX volume slider bar texture */
    private TextureRegion sfxSliderBar;
    /** SFX volume slider knob texture */
    private TextureRegion sfxSliderKnob;
    /** The y-coordinate of the center of the sfx slider */
    private int sfxSliderY;
    /** The x-coordinate of the center of the sfx slider */
    private int sfxSliderX;
    /** Ration of the sfx slider height to the screen */
    private static float SFX_Y_RATIO = 0.2f;
    /** Ration of the sfx slider width to the screen */
    private static float SFX_X_RATIO = 0.2f;
    /** Slider width */
    private float sliderWidth;
    /** Slider height */
    private float sliderHeight;
    /** Slider width ratio */
    private static float SLIDER_WIDTH_RATIO = 0.7f;
    /** Slider height ratio */
    private static float SLIDER_HEIGHT_RATIO = 0.2f;
    /** Touch range constant */
    private static float TOUCH_AREA_RATIO = 0.95f;
    /** The music slider */
    private MySlider musicSlider;
    /** The sfx slider */
    private MySlider sfxSlider;

    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
        currentExitCode = Integer.MIN_VALUE;
        this.screenMode = 1;

    }

    public void gatherAssets(AssetDirectory directory) {
        //TODO: texture is unnecessary, use shapes (see prof White's lectures on drawing shapes without textures)
        backgroundTexture = new TextureRegion(directory.getEntry( "menu:background", Texture.class ));
        backgroundTexture2 = new TextureRegion(directory.getEntry( "menu:background2", Texture.class ));
        levelSelectButton = new TextureRegion(directory.getEntry("menu:level_select_button", Texture.class));
        settingsButton = new TextureRegion(directory.getEntry("menu:settings_button", Texture.class));
        exitButton = new TextureRegion(directory.getEntry("menu:exit_button", Texture.class));
        backButton = new TextureRegion(directory.getEntry("menu:back_button", Texture.class));
        startButton = new TextureRegion(directory.getEntry("menu:start_button", Texture.class));

        levelButton1 = new TextureRegion(directory.getEntry("menu:level1_button", Texture.class));
        levelButton2 = new TextureRegion(directory.getEntry("menu:level2_button", Texture.class));

        backgroundMusic = directory.getEntry("music:menu", Music.class);

        // TODO: We have to import volumes that are saved by the user
        musicVolume = 1.0f;
        sfxVolume = 0.0f;

        musicSliderBar = new TextureRegion(directory.getEntry("menu:sliderBar", Texture.class));
        musicSliderKnob = new TextureRegion(directory.getEntry("menu:sliderKnob", Texture.class));
        musicSliderX = canvas.getWidth()/2;
        musicSliderY = canvas.getHeight()/2;
        musicSlider = new MySlider(musicSliderBar, musicSliderKnob, 20, musicSliderX, musicSliderY, 1, 1);
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        screenY = heightY-screenY;

        if (screenMode == 1) {
            boolean selectPressed = checkClicked(screenX, screenY, selectX, selectY, levelSelectButton, SELECT_ANGLE);
            boolean settingsPressed = checkCircleClicked(screenX, screenY, settingsX, settingsY, settingsButton, BUTTON_SCALE);
            boolean exitPressed = checkCircleClicked(screenX, screenY, exitX, exitY, exitButton, BUTTON_SCALE);
            boolean startPressed = checkClicked(screenX, screenY, startX, startY, startButton, START_ANGLE);

            if (selectPressed) {
                selectPressState = 1;
            } else if (settingsPressed) {
                settingsPressState = 1;
            } else if (exitPressed) {
                exitPressState = 1;
            } else if (startPressed) {
                startPressState = 1;
            }
        } else if (screenMode == 2) {
            boolean exitPressed = checkCircleClicked(screenX, screenY, exitX, exitY, exitButton, BUTTON_SCALE);
            boolean levelPressed1 = checkCircleClicked(screenX, screenY, levelX1, levelY1, levelButton1, BUTTON_SCALE);
            boolean levelPressed2 = checkCircleClicked(screenX, screenY, levelX2, levelY2, levelButton2, BUTTON_SCALE);

            if (levelPressed1) {
                levelPressState1 = 1;
            } else if (levelPressed2) {
                levelPressState2 = 1;
            } else if (exitPressed) {
                exitPressState = 1;
            }
        } else if (screenMode == 3) {
            boolean exitPressed = checkCircleClicked(screenX, screenY, exitX, exitY, exitButton, BUTTON_SCALE);
            if (exitPressed) {
                exitPressState = 1;
            }

            boolean musicKnobPressed = checkCircleClicked(screenX, screenY, musicSlider.knobX, musicSlider.knobY, musicSliderKnob, musicSlider.sx);
            if(musicKnobPressed){
                musicSlider.knobFollow = true;
            }
        }

        return false;
    }

    /**
     * Checks if click was in bound for rectangular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked(int screenX, int screenY, int buttonX, int buttonY, TextureRegion button, float angle) {

        // TODO: TEMPORARY touch range to make it smaller than button


        float buttonTX = buttonX * (float)Math.cos(angle) + buttonY * (float)Math.sin(angle);
        float buttonTY = -buttonX * (float)Math.sin(angle) + buttonY * (float)Math.cos(angle);
        float screenTX = screenX * (float)Math.cos(angle) + screenY * (float)Math.sin(angle);
        float screenTY = -screenX * (float)Math.sin(angle) + screenY * (float)Math.cos(angle);

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
    private boolean checkCircleClicked(float screenX, float screenY, float buttonX, float buttonY, TextureRegion button, float scl) {
        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);
        if (dist < radius*radius) {
            return true;
        } else {
            return false;
        }
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (screenMode == 1) {
            if (selectPressState == 1) {
                selectPressState = 2;
                screenMode = 2;
                return false;
            } else if (settingsPressState == 1) {
                settingsPressState = 2;
                screenMode = 3;
                return false;
            } else if (exitPressState == 1) {
                // Main Menu: Exit Game
                exitPressState = 2;
                currentExitCode = EXIT_QUIT;
                listener.exitScreen(this, currentExitCode);
            } else if (startPressState == 1) {
                currentLevel = 0;
                levelPressState1 = 2;
                currentExitCode = EXIT_PLAY;
                listener.exitScreen(this, currentExitCode);
            }
        } else if (screenMode == 2) {
            if (exitPressState == 1) {
                // Level Selector: Back to main screen
                screenMode = 1;
                exitPressState = 2;
            } else if (levelPressState1 == 1) {
                // TODO: TEMPORARY NEED CHANGE - Level Selector needs to be a list of levels
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
        } else if (screenMode == 3) {
            if (exitPressState == 1) {
                // Level Selector: Back to main screen
                screenMode = 1;
                exitPressState = 2;
            }
            if(musicSlider.knobFollow = true) musicSlider.knobFollow = false;
        }
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer){
        if(screenMode == 3){
            if(musicSlider.knobFollow){
                musicSlider.updateKnob(screenX, screenY);
            }
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
        if (screenMode == 1) {
            canvas.draw(backgroundTexture, 0, 0);
        } else if (screenMode == 2 || screenMode == 3) {
            canvas.draw(backgroundTexture2, 0, 0);
        }

        if (screenMode == 1) {
            // Draw Level Select Button
            Color selectTint = (selectPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelSelectButton, selectTint, levelSelectButton.getRegionWidth() / 2, levelSelectButton.getRegionHeight() / 2,
                    selectX, selectY, SELECT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Settings Button
            Color settingsTint = (settingsPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(settingsButton, settingsTint, settingsButton.getRegionWidth() / 2, settingsButton.getRegionHeight() / 2,
                    settingsX, settingsY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Exit Button
            Color exitTint = (exitPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(exitButton, exitTint, exitButton.getRegionWidth() / 2, exitButton.getRegionHeight() / 2,
                    exitX, exitY, EXIT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Draw Start Button
            Color startTint = (startPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(startButton, startTint, startButton.getRegionWidth() / 2, startButton.getRegionHeight() / 2,
                    startX, startY, START_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        } else if (screenMode == 2){
            // Draw Back Button
            Color exitTint = (exitPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(backButton, exitTint, backButton.getRegionWidth() / 2, backButton.getRegionHeight() / 2,
                    exitX, exitY, EXIT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            // Temporary Implementation - Will change to iterables once we get proper textures
            Color levelTint1 = (levelPressState1 == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelButton1, levelTint1, levelButton1.getRegionWidth() / 2, levelButton1.getRegionHeight() / 2,
                    levelX1, levelY1, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

            Color levelTint2 = (levelPressState2 == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(levelButton2, levelTint2, levelButton2.getRegionWidth() / 2, levelButton2.getRegionHeight() / 2,
                    levelX2, levelY2, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        } else if (screenMode == 3) {
            // Draw Back Button
            Color exitTint = (exitPressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(backButton, exitTint, backButton.getRegionWidth() / 2, backButton.getRegionHeight() / 2,
                    exitX, exitY, EXIT_ANGLE, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

            // Draw sliders
            musicSlider.draw(canvas);
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
        // TODO: Move this if necessary
        musicVolume = musicSlider.ratio;
        backgroundMusic.play();
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.setLooping(true);

        draw();

        // Screen Transition
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
        startY = (int)(START_Y_RATIO * height);
        startX = (int)(START_X_RATIO * width);

        sliderHeight = (int)(SLIDER_HEIGHT_RATIO * height);
        sliderWidth = (int)(SLIDER_WIDTH_RATIO * width);
        musicSliderY = (int)(MUSIC_Y_RATIO * height);
        musicSliderX = (int)(MUSIC_X_RATIO * width);
        sfxSliderY = (int)(SFX_Y_RATIO * height);
        sfxSliderX = (int)(SFX_X_RATIO * width);

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
