package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.obstacle.*;
import com.mygdx.game.util.*;
import com.mygdx.game.assets.*;

import java.util.Iterator;

public class GameplayController implements ContactListener {
    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = -4.9f;


    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** Queue for adding objects */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    /** Countdown active for winning or losing */
    private int countdown;

    /** The texture for walls and platforms */
    protected TextureRegion platformTile;
    /** Texture asset for character avatar */
    private TextureRegion avatarTexture;
    /** Texture asset for umbrella */
    private TextureRegion umbrellaTexture;

    /** The jump sound.  We only want to play once. */
    private Sound jumpSound;
    private long jumpId = -1;
    /** The weapon fire sound.  We only want to play once. */
    private Sound fireSound;
    private long fireId = -1;
    /** The weapon pop sound.  We only want to play once. */
    private Sound plopSound;
    private long plopId = -1;
    /** The default sound volume */
    private float volume;

    // Physics objects for the game
    /** Physics constants for initialization */
    private JsonValue constants;
    /** Reference to the character avatar */
    private PlayerModel avatar;
    /**Reference to the umbrella*/
    private UmbrellaModel umbrella;
    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public GameplayController(Rectangle bounds, Vector2 gravity) {
        world = new World(gravity,false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        countdown = -1;

        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
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
        platformTile = new TextureRegion(directory.getEntry( "shared:earth", Texture.class ));
        avatarTexture  = new TextureRegion(directory.getEntry("placeholder:player", Texture.class));
        umbrellaTexture = new TextureRegion(directory.getEntry("placeholder:umbrella", Texture.class));

        jumpSound = directory.getEntry( "platform:jump", Sound.class );
        fireSound = directory.getEntry( "platform:pew", Sound.class );
        plopSound = directory.getEntry( "platform:plop", Sound.class );

        constants = directory.getEntry( "platform:constants", JsonValue.class );
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal - Commented Out for Now, but we will need this in future implementation
        float dwidth  = platformTile.getRegionWidth()/scale.x;
        float dheight = platformTile.getRegionHeight()/scale.y;

//        JsonValue goal = constants.get("goal");
//        JsonValue goalpos = goal.get("pos");
//        goalDoor = new BoxObstacle(goalpos.getFloat(0),goalpos.getFloat(1),dwidth,dheight);
//        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
//        goalDoor.setDensity(goal.getFloat("density", 0));
//        goalDoor.setFriction(goal.getFloat("friction", 0));
//        goalDoor.setRestitution(goal.getFloat("restitution", 0));
//        goalDoor.setSensor(true);
//        goalDoor.setDrawScale(scale);
//        goalDoor.setTexture(platformTile);
//        goalDoor.setName("goal");
//        addObject(goalDoor);

        String wname = "wall";
        JsonValue walljv = constants.get("walls");
        JsonValue defaults = constants.get("defaults");
        for (int ii = 0; ii < walljv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat( "density", 0.0f ));
            obj.setFriction(defaults.getFloat( "friction", 0.0f ));
            obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
            obj.setDrawScale(scale);
            obj.setTexture(platformTile);
            obj.setName(wname+ii);
            addObject(obj);
        }

        String pname = "platform";
        JsonValue platjv = constants.get("platforms");
        for (int ii = 0; ii < platjv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat( "density", 0.0f ));
            obj.setFriction(defaults.getFloat( "friction", 0.0f ));
            obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
            obj.setDrawScale(scale);
            obj.setTexture(platformTile);
            obj.setName(pname+ii);
            addObject(obj);
        }

        // This world is heavier
        world.setGravity( new Vector2(0,defaults.getFloat("gravity",0)) );

        // Create player
        float scl = constants.get("player").getFloat("texturescale");
        dwidth  = avatarTexture.getRegionWidth()/scale.x*scl;
        dheight = avatarTexture.getRegionHeight()/scale.y*scl;
        avatar = new PlayerModel(constants.get("player"), dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        addObject(avatar);
        scl = constants.get("umbrella").getFloat("texturescale");
        dwidth = umbrellaTexture.getRegionWidth()/scale.x*scl;
        dheight = umbrellaTexture.getRegionHeight()/scale.y*scl;
        umbrella = new UmbrellaModel(constants.get("umbrella"), dwidth, dheight);
        umbrella.setDrawScale(scale);
        umbrella.setTexture(umbrellaTexture);
        addObject(umbrella);
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.collideConnected = false;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = (float) (-Math.PI);
        jointDef.upperAngle = (float) (Math.PI);
        jointDef.bodyA = avatar.getBody();
        jointDef.bodyB = umbrella.getBody();
        jointDef.localAnchorA.set(0,0);
        jointDef.localAnchorB.set(0,constants.get("player").get("pos").getFloat(1)-constants.get("umbrella").get("pos").getFloat(1));
        world.createJoint(jointDef);

        volume = constants.getFloat("volume", 1.0f);
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(InputController input, float dt) {
        // Process actions in object model
        avatar.setMovement(input.getHorizontal() *avatar.getForce());
        umbrella.setTurning(input.getMouseMovement() *umbrella.getForce());
        boolean right = umbrella.faceRight;
        umbrella.faceRight = avatar.isFacingRight();
        if (right != umbrella.faceRight) umbrella.setAngle(umbrella.getAngle()*-1);

        avatar.applyForce();
        umbrella.applyForce();
        if (avatar.isJumping()) {
//            jumpId = playSound( jumpSound, jumpId, volume );
        }


    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == avatar   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
//                setComplete(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    /**
     * Called when the Screen is paused.
     *
     * We need this method to stop all sounds when we pause.
     * Pausing happens when we switch game modes.
     */
    public void pause() {
        jumpSound.stop(jumpId);
        plopSound.stop(plopId);
        fireSound.stop(fireId);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     *
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
    }

    /**
     *
     * Gets all the objects in objects
     */
    public PooledList<Obstacle> getObjects() {
        return objects;
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }


}
