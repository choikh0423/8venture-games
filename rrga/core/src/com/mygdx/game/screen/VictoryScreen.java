package com.mygdx.game.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;

public class VictoryScreen extends MenuScreen{

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** The Screen to draw underneath the victory screen*/
    private GameMode gameScreen;

    /** overlay texture */
    private TextureRegion foregroundTexture;

    /** The background tinting color cache */
    private Color overlayTint;

    /** A reference to a text font (changes to any of its properties will be global) */
    private BitmapFont bigFont;

    private BitmapFont smallFont;

    /** exit code to restart game */
    public static final int EXIT_RESTART = 0;
    /** exit code to next level */
    public static final int EXIT_NEXT = 1;

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

    private int nextPressed;

    public VictoryScreen(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(0,0,0,0.8f);
        currentExitCode = Integer.MIN_VALUE;
    }


    /**
     * Draw the victory screen. Proceed to new screen if possible.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        gameScreen.update(delta);
        gameScreen.draw(delta);
        canvas.setCameraHUD();
        canvas.begin();
        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());
        bigFont.setColor(Color.YELLOW);
        canvas.drawTextCentered("Victory!", bigFont, 0);
        smallFont.setColor(Color.WHITE);
        canvas.drawTextCentered("Press  N  for Next Level", smallFont, -canvas.getHeight()/5f);
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
        gameScreen = null;
        canvas = null;
        foregroundTexture = null;
        bigFont = null;
        smallFont = null;
    }

    @Override
    public void resize(int width, int height) {

    }

    /**
     * Gather the assets for this controller.
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        foregroundTexture = new TextureRegion(directory.getEntry( "game:platform", Texture.class ));
        bigFont = directory.getEntry("shared:retro", BitmapFont.class);
        smallFont = directory.getEntry("shared:minecraft", BitmapFont.class);
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
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.N) {
            nextPressed = 1;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (nextPressed == 1) {
            nextPressed = 0;
            currentExitCode = VictoryScreen.EXIT_NEXT;
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
            return true;
        }

        return false;
    }

}
