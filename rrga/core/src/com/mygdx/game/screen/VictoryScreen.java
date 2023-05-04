package com.mygdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.mode.MenuButton;
import com.mygdx.game.mode.MenuMode;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;

public class VictoryScreen extends MenuScreen{

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** overlay texture */
    private TextureRegion foregroundTexture;

    /** The background tinting color cache */
    private Color overlayTint;

    /** true until the first call to render*/
    public boolean first;
    /** The Screen to draw underneath the pause screen*/
    private GameMode gameScreen;

    /////////////////////DRAWING BUTTONS AND TAGS/////////////////////////
    /** menu button*/
    private MenuButton menuButton;
    /** try again button */
    private MenuButton nextButton;
    /** Lose text */
    private TextureRegion winTag;
    private static final float WIN_TAG_X_RATIO = .5f;
    private static final float WIN_TAG_Y_RATIO = .65f;
    private int winTagX;
    private int winTagY;
    private final float TAG_SCL = .8f;
    /** Texture for the cursor */
    private TextureRegion cursorTexture;
    /** Height of the button */
    private static final float BUTTON_SCALE  = 1.0f;
    /** Touch range constant */
    private static final float TOUCH_AREA_RATIO = 0.95f;
    /** Scaling factor for when the player changes the resolution. */
    private float scale;
    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 1024;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 576;

    ////////////BUTTON STATES/////////////////
    /** exit code to go to menu */
    public static final int EXIT_MENU = 1;
    /** exit code to try again */
    public static final int EXIT_NEXT = 2;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

    /** The current state of the level menu button */
    private int menuPressState;
    /** The current state of the restart button */
    private int nextPressState;

    public VictoryScreen(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(1,1,1,0.9f);
        currentExitCode = Integer.MIN_VALUE;
        first = true;

        this.menuButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.37f, 0.25f, 0);
        this.nextButton = new MenuButton(MenuMode.ButtonShape.RECTANGLE, 0.63f, 0.25f, 0);
    }

    /**
     * Draw the victory screen. Proceed to new screen if possible.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        //Gdx.input.setCursorCatched(false);
        int x=0, y=0;
        if(first) {
            x = Gdx.input.getX();
            y = Gdx.input.getY();
        }
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        if(first){
            Gdx.input.setCursorPosition(x, y);
            first = false;
        }

        //comment this out if opaque foreground
        gameScreen.draw(delta, false);

        canvas.setCameraHUD();
        canvas.begin();

        //canvas.draw(foregroundTexture, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
        //above line for opaque foreground, below line for transparent foreground
        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.draw(winTag, Color.WHITE, winTag.getRegionWidth()/2f, winTag.getRegionHeight()/2f,
                winTagX, winTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);

        menuButton.draw(canvas, menuPressState, BUTTON_SCALE, Color.WHITE);
        nextButton.draw(canvas, nextPressState, BUTTON_SCALE, Color.WHITE);

        //draw mouse
        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if(mx<Gdx.graphics.getWidth() && mx>0 && my<Gdx.graphics.getHeight() && my>0) {
            canvas.draw(cursorTexture, Color.WHITE, 0, cursorTexture.getRegionHeight(),
                    mx, my, 0, .5f, .5f);
        }

        canvas.end();

        // transition
        if (currentExitCode >= 0){
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        }
    }

    @Override
    public void dispose() {
        listener = null;
        canvas = null;
        foregroundTexture = null;
        overlayTint = null;
    }

    @Override
    public void resize(int width, int height) {
        // Scaling code from Professor White's code
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        menuButton.setPos(width, height, scale);
        nextButton.setPos(width, height, scale);

        winTagY = (int)(WIN_TAG_Y_RATIO * height);
        winTagX = (int)(WIN_TAG_X_RATIO * width);
    }

    /**
     * Gather the assets for this controller.
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        //TODO: texture is unnecessary, use shapes (see prof White's lectures on drawing shapes without textures)
        foregroundTexture = new TextureRegion(directory.getEntry( "menu:background2", Texture.class ));

        TextureRegion menuTexture = new TextureRegion(directory.getEntry("menu:menu_button", Texture.class));
        TextureRegion nextTexture = new TextureRegion(directory.getEntry("menu:next_button", Texture.class));
        menuButton.setTexture(menuTexture);
        nextButton.setTexture(nextTexture);

        winTag = new TextureRegion(directory.getEntry("menu:win_text", Texture.class));
        cursorTexture = new TextureRegion(directory.getEntry("menu:cursor_menu", Texture.class));
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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = canvas.getHeight()-screenY;
        boolean menuPressed = checkClicked2(screenX, screenY, menuButton);
        boolean nextPressed = checkClicked2(screenX, screenY, nextButton);

        if (menuPressed) {
            menuPressState = 1;
        } else if (nextPressed) {
            nextPressState = 1;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (menuPressState == 1){
            currentExitCode = EXIT_MENU;
            menuPressState = 2;
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        } else if (nextPressState == 1) {
            currentExitCode = EXIT_NEXT;
            nextPressState = 2;
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

    public void reset() {
        overlayTint = new Color(1,1,1,0.9f);
        currentExitCode = Integer.MIN_VALUE;
    }
}

