package com.mygdx.game.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.CameraController;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.MySlider;
import com.mygdx.game.utility.util.ScreenListener;
import org.w3c.dom.Text;

import java.awt.*;
import java.util.ArrayList;

public class MenuMode extends MenuScreen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** Background texture */
    private TextureRegion backgroundTexture;
    /** Level Selector Background texture */
    private TextureRegion backgroundTexture2;


    /** Types of button by shape */
    public enum ButtonShape {
        RECTANGLE,
        CIRCLE
    }
    /** exit button*/
    private final MenuButton exitButton;
    /** start button */
    private final MenuButton startButton;
    /** settings button */
    private final MenuButton settingsButton;
    /** level select button */

    private final MenuButton levelSelectButton;

    /** back button */
    private final MenuButton backButton;

    /** reset button */
    private final MenuButton resetButton;

    /** The current state of the level select button */
    private int selectPressState;
    /** The current state of the settings button */
    private int settingsPressState;
    /** The current state of the exit button */
    private int exitPressState;
    /** The current state of the exit button */
    private int startPressState;
    /** The current states of the level buttons. 0 = not clicked, 1 = clicked */
    private int[] levelPressStates;
    /** The current state of the umbrella toggle button */
    private int togglePressState;
    private int resetLevelPressState;
    private int resetSettingsPressState;

    /** Background music */
    private Music backgroundMusic;
    public Music getMusic(){return backgroundMusic;}
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
    public static final int EXIT_CONFIRM = 3;
    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;
    /** Tracker for checking which screen the menu screen is showing
     *  1: main menu
     *  2: level selector
     *  3: settings
     * */
    private int screenMode;

    /////////// LEVEL BUTTONS /////////////
    private ArrayList<MenuButton> levelButtons;
    /** number of levels in the game. NEED TO CHANGE THIS AS WE ADD MORE LEVELS */
    public static final int LEVEL_COUNT = 30;
    private boolean[] levelUnlocked = new boolean[LEVEL_COUNT+1];

    //////////////////

    public int getScreenMode(){return screenMode;}
    public void setScreenMode(int mode){screenMode = mode;}

    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 700;
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

    /** Music volume slider bar texture */
    private TextureRegion musicSliderBar;
    /** Music volume slider knob texture */
    private TextureRegion musicSliderKnob;

    /** The x-coordinate of the center of the music slider */
    private int musicSliderX;
    /** The y-coordinate of the center of the music slider */
    private int musicSliderY;
    /** Ratio of the music slider width to the screen */
    private static float MUSIC_X_RATIO = 0.5f;
    /** Ratio of the music slider height to the screen */
    private static float MUSIC_Y_RATIO = 0.5f;

    /** SFX volume slider bar texture */
    private TextureRegion sfxSliderBar;
    /** SFX volume slider knob texture */
    private TextureRegion sfxSliderKnob;

    /** The x-coordinate of the center of the sfx slider */
    private int sfxSliderX;
    /** The y-coordinate of the center of the sfx slider */
    private int sfxSliderY;
    /** Ratio of the sfx slider width to the screen */
    private static float SFX_X_RATIO = 0.5f;
    /** Ratio of the sfx slider height to the screen */
    private static float SFX_Y_RATIO = 0.35f;
    private float SLIDER_SCL_X = 1;
    private float SLIDER_SCL_Y = 1;
    /** Touch range constant */
    private static float TOUCH_AREA_RATIO = 0.95f;
    /** The music slider */
    private MySlider musicSlider;
    /** The sfx slider */
    private MySlider sfxSlider;
    private TextureRegion musicTag;
    private static float MUSIC_TAG_X_RATIO = .2f;
    private static float MUSIC_TAG_Y_RATIO = .50f;
    private int musicTagX;
    private int musicTagY;
    private TextureRegion sfxTag;
    private static float SFX_TAG_X_RATIO = .2f;
    private static float SFX_TAG_Y_RATIO = .35f;
    private int sfxTagX;
    private int sfxTagY;
    private float TAG_SCL = 1;

    private TextureRegion settingTag;
    private static float SETTING_TAG_X_RATIO = .5f;
    private static float SETTING_TAG_Y_RATIO = .77f;
    private int settingTagX;
    private int settingTagY;

    private TextureRegion toggleTag;
    private static float TOGGLE_TAG_X_RATIO = .35f;
    private static float TOGGLE_TAG_Y_RATIO = .15f;
    private int toggleTagX;
    private int toggleTagY;


    /** toggle button texture */
    private TextureRegion toggleButton;
    private static float TOGGLE_BUTTON_X_RATIO = .75f;
    private static float TOGGLE_BUTTON_Y_RATIO = .15f;
    private int toggleButtonX;
    private int toggleButtonY;

    /** toggle check texture */
    private TextureRegion toggleCheck;

    private boolean toggleOn;

    /** current selected level */
    private int currentLevel;

    /** texture for the cursor */
    private TextureRegion cursorTexture;
    /** pixmap for the cursor */
    private Cursor newCursor;

    /** preferences object to store user settings */
    Preferences settings = Gdx.app.getPreferences("settings");
    /** preferences object to store which levels the user has unlocked */
    Preferences unlocked = Gdx.app.getPreferences("unlocked");
    /** a list containing whether each level is unlocked.
     * size is 30 to allow for room for more levels.
     * 0th element is whether to unlock all levels at start of game
     * (true for developers and final submission, false for publicly distributed version)*/

    /** viewport width used for computing the UI scale*/
    private final int viewWidth;

    /** viewport height used for computing the UI scale*/
    private final int viewHeight;

    public MenuMode(GameCanvas canvas) {
        //TODO: CHANGE TO FALSE FOR PUBLIC RELEASE (or to test unlocking of levels)
        levelUnlocked[0] = true;

        this.canvas = canvas;
        currentExitCode = Integer.MIN_VALUE;
        this.screenMode = 1;

        // TODO: All the ratios are hard coded - these can be extracted to JSON
        this.exitButton = new MenuButton(ButtonShape.CIRCLE, 0.05f, 0.93f, 0.05f * 3.14f);
        this.startButton = new MenuButton(ButtonShape.RECTANGLE, 0.37f, 0.2f, 0.05f * 3.14f);
        this.settingsButton = new MenuButton(ButtonShape.RECTANGLE, 0.95f, 0.07f, 0);
        this.levelSelectButton = new MenuButton(ButtonShape.RECTANGLE, 0.63f, 0.2f, -0.05f * 3.14f);
        this.backButton = new MenuButton(ButtonShape.CIRCLE, 0.05f, 0.93f, 0);
        resetButton = new MenuButton(ButtonShape.RECTANGLE, 0.85f,0.1f,0);

        CameraController camera = canvas.getCamera();
        viewWidth = (int) camera.getViewWidth();
        viewHeight = (int) camera.getViewHeight();

        int width = viewWidth;
        int height = viewHeight;
        float sx = 1.0f * width /STANDARD_WIDTH;
        float sy = 1.0f * height /STANDARD_HEIGHT;
        scale = (Math.min(sx, sy));

        exitButton.setPos(width, height, scale);
        startButton.setPos(width, height, scale);
        settingsButton.setPos(width, height, scale);
        levelSelectButton.setPos(width, height, scale);
        backButton.setPos(width, height, scale);
        resetButton.setPos(width, height, scale);

        this.buttonWidth = (int)(BUTTON_WIDTH_RATIO*width);
        heightY = height;

        musicTagX = (int)(MUSIC_TAG_X_RATIO * width);
        musicTagY = (int)(MUSIC_TAG_Y_RATIO * height);
        sfxTagX = (int)(SFX_TAG_X_RATIO * width);
        sfxTagY = (int)(SFX_TAG_Y_RATIO * height);
        settingTagY = (int)(SETTING_TAG_Y_RATIO * height);
        settingTagX = (int)(SETTING_TAG_X_RATIO * width);
        toggleTagY = (int)(TOGGLE_TAG_Y_RATIO * height);
        toggleTagX = (int)(TOGGLE_TAG_X_RATIO * width);
        toggleButtonY = (int)(TOGGLE_BUTTON_Y_RATIO * height);
        toggleButtonX = (int)(TOGGLE_BUTTON_X_RATIO * width);

        levelButtons = new ArrayList<>();
        float num_row = 5;
        float num_col = 6;
        for(int i=1; i<LEVEL_COUNT+1; i++){
            MenuButton button = new MenuButton(ButtonShape.RECTANGLE, .15f+.7f*(((i-1)%num_col)/(num_col-1)),
                    .875f-.6f*(((i-1)/(int)num_col)/(num_row-1)), 0, i);
            button.setPos(width, height, scale);
            levelButtons.add(button);
        }
        levelPressStates = new int[LEVEL_COUNT];

    }

    public void gatherAssets(AssetDirectory directory) {

        backgroundTexture = new TextureRegion(directory.getEntry( "menu:background", Texture.class ));
        backgroundTexture2 = new TextureRegion(directory.getEntry( "menu:background2", Texture.class ));

        cursorTexture = new TextureRegion(directory.getEntry( "menu:cursor_menu", Texture.class ));
        Pixmap pm = new Pixmap(Gdx.files.internal("game/goal.png"));
        newCursor = Gdx.graphics.newCursor(pm, 0, 0);
        pm.dispose();


        // TODO: To reduce global variables, made temporary texture region variables, Let me know if this is too much of a bad practice
        // MENU COMPONENTS
        TextureRegion exitTexture = new TextureRegion(directory.getEntry("menu:exit_button", Texture.class));
        TextureRegion startTexture = new TextureRegion(directory.getEntry("menu:start_button", Texture.class));
        TextureRegion settingsTexture = new TextureRegion(directory.getEntry("menu:settings_button", Texture.class));
        TextureRegion levelSelectTexture = new TextureRegion(directory.getEntry("menu:level_select_button", Texture.class));
        TextureRegion backButtonTexture = new TextureRegion(directory.getEntry("menu:back_button", Texture.class));

        exitButton.setTexture(exitTexture);
        startButton.setTexture(startTexture);
        settingsButton.setTexture(settingsTexture);
        levelSelectButton.setTexture(levelSelectTexture);
        backButton.setTexture(backButtonTexture);
        resetButton.setTexture(new TextureRegion(directory.getEntry("menu:reset_button", Texture.class)));

        // LEVEL SELECT COMPONENTS
        Texture tex = directory.getEntry("menu:level_buttons", Texture.class);
        TextureRegion[][] tempLevels = TextureRegion.split(tex, tex.getWidth()/6, tex.getHeight()/5);
        TextureRegion[] levels = new TextureRegion[30];

        // Placing level buttons in order
        int index = 0;
        for (int i=0; i<tempLevels.length; i++) {
            for (int j=0; j<tempLevels[0].length; j++) {
                levels[index] = tempLevels[i][j];
                index++;
            }
        }

        for(int i=0; i<LEVEL_COUNT; i++){
            levelButtons.get(i).setTexture(levels[i]);
        }

        // SETTINGS COMPONENT
        musicTag = new TextureRegion(directory.getEntry("menu:music_tag", Texture.class));
        sfxTag = new TextureRegion(directory.getEntry("menu:sfx_tag", Texture.class));
        settingTag = new TextureRegion(directory.getEntry("menu:setting_tag", Texture.class));
        toggleTag = new TextureRegion(directory.getEntry("menu:toggle_tag", Texture.class));
        toggleButton = new TextureRegion(directory.getEntry("menu:toggle_button", Texture.class));
        toggleCheck = new TextureRegion(directory.getEntry("menu:toggle_check", Texture.class));

        // TODO: Scale slider bars (??)
        musicSliderBar = new TextureRegion(directory.getEntry("menu:sliderBar", Texture.class));
        musicSliderKnob = new TextureRegion(directory.getEntry("menu:sliderKnob", Texture.class));
        musicSlider = new MySlider(musicSliderBar, musicSliderKnob, 20, musicSliderX, musicSliderY, SLIDER_SCL_X, SLIDER_SCL_Y);

        sfxSliderBar = new TextureRegion(directory.getEntry("menu:sliderBar", Texture.class));
        sfxSliderKnob = new TextureRegion(directory.getEntry("menu:sliderKnob", Texture.class));
        sfxSlider = new MySlider(sfxSliderBar, sfxSliderKnob, 20, sfxSliderX, sfxSliderY, SLIDER_SCL_X, SLIDER_SCL_Y);

        musicSlider.setY(MUSIC_Y_RATIO * viewHeight);
        musicSlider.setX(MUSIC_X_RATIO * viewWidth);
        sfxSlider.setY(SFX_Y_RATIO * viewHeight);
        sfxSlider.setX(SFX_X_RATIO * viewWidth);

        backgroundMusic = directory.getEntry("music:menu", Music.class);

        //load in user settings
        musicVolume = settings.getFloat("musicVolume", 0.5f);
        musicSlider.ratio = musicVolume;
        sfxVolume = settings.getFloat("sfxVolume", 0.5f);
        sfxSlider.ratio = sfxVolume;
        toggleOn = settings.getBoolean("toggle", false);

        //load in whether player has unlocked each level
        for (int i = 0; i <= LEVEL_COUNT; i++){
            //level 1 always starts unlocked
            if (i==1) levelUnlocked[i] = true;
            //if we have unlocked all levels, unlock all levels
            else if (levelUnlocked[0]) levelUnlocked[i] = true;
            //otherwise load in whether player has unlocked each level
            else levelUnlocked[i] = unlocked.getBoolean(i+"unlocked", false);
        }
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (screenMode == 1) {
            // Checks which button was clicked
            boolean selectPressed = checkClicked2(screenX, screenY, levelSelectButton);
            boolean settingsPressed = checkCircleClicked2(screenX, screenY, settingsButton, BUTTON_SCALE);
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean startPressed = checkClicked2(screenX, screenY, startButton);

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
            // Checks which button was clicked in Level Selector Screen
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean resetPressed = checkClicked2(screenX, screenY, resetButton);

            for(int i=0; i<LEVEL_COUNT; i++){
                levelPressStates[i] = checkClicked2(screenX, screenY, levelButtons.get(i)) ? 1 : 0;
            }

            if (exitPressed) {
                exitPressState = 1;
            } else if (resetPressed){
                resetLevelPressState = 1;
            }
        } else if (screenMode == 3) {
            // Checks which button was clicked in Settings Screen
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean musicKnobPressed = checkCircleClicked(screenX, screenY, musicSlider.getKnobX(), musicSlider.getKnobY(), musicSliderKnob, musicSlider.sx);
            boolean sfxKnobPressed = checkCircleClicked(screenX, screenY, sfxSlider.getKnobX(), sfxSlider.getKnobY(), sfxSliderKnob, sfxSlider.sx);
            boolean togglePressed = checkClicked(screenX, screenY, toggleButtonX, toggleButtonY, toggleButton, BUTTON_SCALE);
            boolean resetPressed = checkClicked2(screenX, screenY, resetButton);

            if (exitPressed) {
                exitPressState = 1;
            } else if(musicKnobPressed){
                musicSlider.knobFollow = true;
            } else if(sfxKnobPressed){
                sfxSlider.knobFollow = true;
            } else if (togglePressed) {
                togglePressState = 1;
            } else if (resetPressed){
                resetSettingsPressState = 1;
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
        CameraController camera = canvas.getCamera();
        Vector2 temp = camera.unproject(screenX, screenY);
        screenX = (int) temp.x;
        screenY = (int) temp.y;

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
     * Checks if click was in bound for rectangular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked2(int screenX, int screenY,  MenuButton button) {

        CameraController camera = canvas.getCamera();
        Vector2 temp = camera.unproject(screenX, screenY);
        screenX = (int) temp.x;
        screenY = (int) temp.y;

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
    private boolean checkCircleClicked(float screenX, float screenY, float buttonX, float buttonY, TextureRegion button, float scl) {

        CameraController camera = canvas.getCamera();
        Vector2 temp = camera.unproject(screenX, screenY);
        screenX = (int) temp.x;
        screenY = (int) temp.y;

        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);

        // Checks if space inside the circle has been clicked
        return dist < radius*radius;
    }

    /**
     * Checks if click was in bound for circular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkCircleClicked2(float screenX, float screenY, MenuButton button, float scl) {
        CameraController camera = canvas.getCamera();
        Vector2 temp = camera.unproject(screenX, screenY);
        screenX = (int) temp.x;
        screenY = (int) temp.y;

        float buttonX = button.getX();
        float buttonY = button.getY();
        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);

        // Checks if space inside the circle has been clicked
        return dist < radius*radius;
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
                currentExitCode = Integer.MIN_VALUE;
            } else if (startPressState == 1) {
                currentLevel = 1;
                startPressState = 2;
                currentExitCode = EXIT_PLAY;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            }
        } else if (screenMode == 2) {
            if (exitPressState == 1) {
                // Level Selector: Back to main screen
                screenMode = 1;
                exitPressState = 2;
            } else if (resetLevelPressState==1){
                //TODO: popup
                resetLevelPressState = 2;
                currentExitCode = EXIT_CONFIRM;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            }
            else {
                for (int i = 0; i < LEVEL_COUNT; i++) {
                    if (levelPressStates[i] == 1) {
                        levelPressStates[i] = 0;
                        if(levelUnlocked[i+1]){
                            currentLevel = levelButtons.get(i).getID();
                            currentExitCode = EXIT_PLAY;
                            listener.exitScreen(this, currentExitCode);
                            currentExitCode = Integer.MIN_VALUE;
                        }
                    }
                }
            }
        } else if (screenMode == 3) {
            if (exitPressState == 1) {
                // Level Selector: Back to main screen
                screenMode = 1;
                exitPressState = 2;
            } else if (togglePressState == 1) {
                toggleOn = !toggleOn;
                togglePressState = 2;
            } else if (resetSettingsPressState == 1) {
                //TODO: popup
                resetSettingsPressState = 2;
                currentExitCode = EXIT_CONFIRM;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            }
            if(musicSlider.knobFollow) musicSlider.knobFollow = false;
            if(sfxSlider.knobFollow) sfxSlider.knobFollow = false;
            //save user settings
            settings.putFloat("musicVolume", musicVolume);
            settings.putFloat("sfxVolume", sfxVolume);
            settings.putBoolean("toggle", toggleOn);
            settings.flush();
        }
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer){

        if(screenMode == 3){
            Vector2 temp = canvas.getCamera().unproject(screenX, screenY);
            screenX = (int) temp.x;
            screenY = (int) temp.y;
            if(musicSlider.knobFollow){
                musicSlider.updateKnob(screenX, screenY);
            }
            if(sfxSlider.knobFollow){
                sfxSlider.updateKnob(screenX, screenY);
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
    public void draw() {
        canvas.begin();
        CameraController camera = canvas.getCamera();
        if (screenMode == 1) {
            canvas.draw(backgroundTexture, Color.WHITE, 0, 0, camera.getViewWidth(), camera.getViewHeight());
        } else if (screenMode == 2 || screenMode == 3) {
            canvas.draw(backgroundTexture2, Color.WHITE, 0, 0, camera.getViewWidth(), camera.getViewHeight());
        }

        if (screenMode == 1) {
            // Draw Level Select Button
            levelSelectButton.draw(canvas, selectPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Settings Button
            settingsButton.draw(canvas, settingsPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Exit Button
            exitButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Start Button
            startButton.draw(canvas, startPressState, BUTTON_SCALE, Color.WHITE);
        } else if (screenMode == 2){
            // Draw Back Button
            backButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);
            resetButton.draw(canvas, resetLevelPressState, BUTTON_SCALE, Color.WHITE);
            // Temporary Implementation - Will change to iterables once we get proper textures

            for(int i=0; i<LEVEL_COUNT; i++){
                levelButtons.get(i).draw(canvas, levelPressStates[i], BUTTON_SCALE, levelUnlocked[i+1] ? Color.WHITE : Color.LIGHT_GRAY);
            }
        } else if (screenMode == 3) {
            // Draw Back Button
            backButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);

            // Draw sliders
            musicSlider.draw(canvas);
            sfxSlider.draw(canvas);

            canvas.draw(settingTag, Color.WHITE, settingTag.getRegionWidth()/2f, settingTag.getRegionHeight()/2f,
                    settingTagX, settingTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(musicTag, Color.WHITE, musicTag.getRegionWidth()/2f, musicTag.getRegionHeight()/2f,
                    musicTagX, musicTagY, 0,  TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(sfxTag, Color.WHITE, sfxTag.getRegionWidth()/2f, sfxTag.getRegionHeight()/2f,
                    sfxTagX, sfxTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(toggleTag, Color.WHITE, toggleTag.getRegionWidth()/2f, toggleTag.getRegionHeight()/2f,
                    toggleTagX, toggleTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);

            canvas.draw(toggleButton, Color.WHITE, toggleButton.getRegionWidth()/2f, toggleButton.getRegionHeight()/2f,
                    toggleButtonX, toggleButtonY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            if (toggleOn) {
                canvas.draw(toggleCheck, Color.WHITE, toggleCheck.getRegionWidth()/2f, toggleCheck.getRegionHeight()/2f,
                        toggleButtonX, toggleButtonY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            }
            resetButton.draw(canvas, resetSettingsPressState, BUTTON_SCALE, Color.WHITE);
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
        sfxVolume = sfxSlider.ratio;
        backgroundMusic.play();
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.setLooping(true);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        // do nothing, camera takes care of resizing.
    }

    /** Returns current level selected */
    public int getCurrentLevel() {
        return currentLevel;
    }
    /** Returns sfx volume */
    public float getSfxVolume() {
        return sfxVolume;
    }
    /** Returns music volume */
    public float getMusicVolume() {
        return musicVolume;
    }
    /** Returns control toggle on/off boolean */
    public boolean getControlToggle() {
        return toggleOn;
    }


    @Override
    public void dispose() {
        //TODO: Need legitimate disposing
        listener = null;
        if (backgroundMusic != null){
            backgroundMusic.stop();
        }
        // NEED TO ADD
    }

    public void pause() {
        if (backgroundMusic != null){
            backgroundMusic.stop();
        }
    }

    /** Reset is for transitioning from other mode to current mode*/
    public void reset() {
        musicVolume = settings.getFloat("musicVolume", 0.5f);
        sfxVolume = settings.getFloat("sfxVolume", 0.5f);
        toggleOn = settings.getBoolean("toggle", false);
        musicSlider.ratio = musicVolume;
        sfxSlider.ratio = sfxVolume;
        backgroundMusic.play();
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.setLooping(true);

        this.screenMode = 1;

        if (!levelUnlocked[0]){
            for(int i = 2; i <= LEVEL_COUNT; i++){
                levelUnlocked[i] = unlocked.getBoolean(i+"unlocked", levelUnlocked[i]);
            }
        }
    }
}
