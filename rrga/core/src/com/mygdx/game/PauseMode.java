package com.mygdx.game;

import com.badlogic.gdx.*;
import com.mygdx.game.util.ScreenListener;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.assets.*;

/**
 * A PauseMode is a pause menu screen. User can interact with this screen
 * through keyboard or mouse cursor.
 */
public class PauseMode extends MenuScreen {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private Screen background;

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

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

    public PauseMode(GameCanvas canvas) {
        this.canvas = canvas;
        overlayTint = new Color(0,0,0,0.9f);
        currentExitCode = Integer.MIN_VALUE;
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
        foregroundTexture = new TextureRegion(directory.getEntry( "placeholder:platform", Texture.class ));
        bigFont = directory.getEntry("shared:retro", BitmapFont.class);
        smallFont = directory.getEntry("shared:minecraft", BitmapFont.class);
    }

    /**
     * Draw the Pause menu and exit pause mode if possible.
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        if (background != null){
            background.render(delta);
        }
        draw(delta);
        if (currentExitCode >= 0){
            listener.exitScreen(this, currentExitCode);
            currentExitCode = Integer.MIN_VALUE;
        }
    }

    /**
     * Draws static pause menu
     * @param delta The time in seconds since the last render
     */
    private void draw(float delta){
        canvas.setCameraHUD();
        canvas.begin();
        canvas.draw(foregroundTexture, overlayTint, 0, 0, canvas.getWidth(), canvas.getHeight());
        bigFont.setColor(Color.WHITE);
        canvas.drawTextCentered("Paused", bigFont, 0);
        smallFont.setColor(Color.WHITE);
        canvas.drawTextCentered("Press  P  to  Unpause", smallFont, -canvas.getHeight()/5f);
        canvas.end();
    }

    public void resize(int width, int height) {}

    public void dispose() {
        listener = null;
        background = null;
        canvas = null;
        foregroundTexture = null;
        overlayTint = null;
        bigFont = null;
        smallFont = null;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.R){
            currentExitCode = EXIT_RESTART;
        }
        else if (keycode == Input.Keys.P){
            currentExitCode = EXIT_RESUME;
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

    public void setBackgroundScreen(Screen backgroundScreen){
        this.background = backgroundScreen;
    }

    public Screen getBackgroundScreen(){
        return this.background;
    }
}

