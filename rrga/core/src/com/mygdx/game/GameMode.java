package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.model.PlayerModel;
import com.mygdx.game.utility.obstacle.*;
import com.mygdx.game.utility.util.*;
import com.mygdx.game.utility.assets.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.util.PooledList;
import com.mygdx.game.utility.util.ScreenListener;

public class GameMode implements Screen {
    /** Texture asset for background image */
    private TextureRegion backgroundTexture;
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;

    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;

    /** Exit code for pausing the game */
    public static final int EXIT_PAUSE = 1;

    /** Exit code for victory screen */
    public static final int EXIT_VICTORY = 2;

    /** Exit code for fail screen */
    public static final int EXIT_FAIL = 3;

    /** Current Width of the game world in Box2d units */
    private float physicsWidth;

    /** Current Height of the game world in Box2d units */
    private float physicsHeight;

    /** Current Width of the canvas in Box2d units */
    private float displayWidth;

    /** Current Height of the canvas in Box2d units */
    private float displayHeight;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** Placeholder gravity value */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether we have completed this level */

    private boolean debug;
    /** Whether or not the game is paused */
    private boolean paused;

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/displayWidth;
        this.scale.y = canvas.getHeight()/displayHeight;
        gameplayController.setScale(this.scale);
    }

    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameMode() {
        this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
                new Vector2(0,DEFAULT_GRAVITY));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width  	The width in Box2d coordinates
     * @param height	The height in Box2d coordinates
     * @param gravity	The downward gravity
     */
    protected GameMode(float width, float height, float gravity) {
        this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds	The game bounds in Box2d coordinates
     * @param gravity	The gravitational force on this Box2d world
     */
    protected GameMode(Rectangle bounds, Vector2 gravity) {
        debug  = false;
        active = false;
        this.bounds = bounds;
        this.scale = new Vector2(1,1);

        // Create the controllers.
        inputController = new InputController();
        gameplayController = new GameplayController(bounds, gravity, 0);
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        // Dispose Controllers
        gameplayController.dispose();
        inputController = null;
        gameplayController = null;

        bounds = null;
        scale  = null;
        canvas = null;
    }

    /**
     * Gather the assets for this controller.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        gameplayController.setLevelParser(new LevelParser(directory));
        gameplayController.gatherAssets(directory);

        this.backgroundTexture = gameplayController.getBackgroundTexture();

        float[] physicsDim = gameplayController.getPhysicsDims();
        this.physicsWidth = physicsDim[0];
        this.physicsHeight = physicsDim[1];

        float[] displayDim = gameplayController.getDisplayDims();
        this.displayWidth = displayDim[0];
        this.displayHeight = displayDim[1];

        // TESTING
//        LevelParser parser = new LevelParser(directory);
//        parser.parseLevel(directory.getEntry("tiled:level1", JsonValue.class));
//        for (JsonValue jv : parser.getBirdData()){
//            System.out.println(jv);
//        }
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        this.bounds.set(0,0, physicsWidth, physicsHeight);
        gameplayController.setBounds(this.bounds);
        gameplayController.reset();
    };

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt	Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (listener == null) {
            return true;
        }

        // TODO: maybe this field is unnecessary since GameMode's update() and draw() are public.
        //  screen modes that use GameMode as background can just avoid render()...
        if (paused){
            return false;
        }

        if (gameplayController.isCompleted()) {
            listener.exitScreen(this, EXIT_VICTORY);
            return false;
        } else if (gameplayController.isFailed()) {
            listener.exitScreen(this, EXIT_FAIL);
            return false;
        }

        inputController.readInput(bounds, scale);

        // Toggle debug
        if (inputController.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (inputController.didReset()) {
            reset();
            return true;
        }

        // Now it is time to maybe switch screens.
        if (inputController.didExit()) {
            pause();
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }

        // Pause button pressed, no changes to internal state of game world
        if (inputController.didPause()) {
            listener.exitScreen(this, EXIT_PAUSE);
            return false;
        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code. It does not handle collisions,
     * as those are managed by gameplay controller through Box2D.
     * This method is called after input is read, in which case a decision has been
     * made about updating the game state. This method should be followed by a call
     * to draw the elements of the world.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        gameplayController.update(inputController, dt);
        gameplayController.postUpdate(dt);
    };

    /**
     * Draw the physics objects to the canvas
     *
     * The method draws all objects in the order that they were added.
     * Heads-up display (HUD) content is drawn on top of these physics objects.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void draw(float dt) {
        canvas.clear();

        // focus camera on player
        float px = gameplayController.getPlayerScreenX();
        float py = gameplayController.getPlayerScreenY();

        canvas.setCameraDynamic();
        canvas.translateCameraToPoint(px,py);
        canvas.begin();

        // center a background on player
        // TODO: make sure to get the right rectangle of the full background.
        //  For efficiency, DO NOT render the entire background.
        //  ox and oy denotes the origin of the texture that we sample a rectangle from.
        canvas.draw(backgroundTexture, Color.WHITE, 0,0,px - canvas.getWidth()/2f,
                py - canvas.getHeight()/2f,canvas.getWidth(),canvas.getHeight());

        // draw all game objects, these objects are "dynamic"
        // a change in player's position should yield a different perspective.
        PooledList<Obstacle> objects = gameplayController.getObjects();
        for(Obstacle obj : gameplayController.getObjects()) {
            obj.draw(canvas);
        }
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        // Draw all HUD content
        canvas.setCameraHUD();
        PlayerModel p = gameplayController.getPlayer();
        canvas.begin();
        p.drawInfo(canvas);
        canvas.end();
    }




    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId) {
        return playSound( sound, soundId, 1.0f );
    }


    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     * @param volume	The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
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
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
            }
            draw(delta);
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // does nothing
        // this only gets called when we minimize the application, we can switch to pause mode that way.
        paused = true;
        listener.exitScreen(this, GameMode.EXIT_PAUSE);
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // does nothing
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
        paused = false;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        paused = true;
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
     * Sets current level of the game
     */
    public void setLevel(int level){
        gameplayController.setLevel(level);
    }

}
