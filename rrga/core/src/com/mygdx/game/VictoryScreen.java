package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.assets.AssetDirectory;
import com.mygdx.game.util.ScreenListener;

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

    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;

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
        canvas.drawTextCentered("Press  R  to  Try  Again", smallFont, -canvas.getHeight()/5f);
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
        //TODO: texture is unnecessary, use shapes (see prof White's lectures on drawing shapes without textures)
        foregroundTexture = new TextureRegion(directory.getEntry( "placeholder:platform", Texture.class ));
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
        if (keycode == Input.Keys.R) {
            currentExitCode = VictoryScreen.EXIT_RESTART;
            return true;
        }
        return false;
    }
}
