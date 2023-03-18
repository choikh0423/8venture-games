package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.hazard.BirdHazard;
import com.mygdx.game.hazard.HazardModel;
import com.mygdx.game.hazard.LightningHazard;
import com.mygdx.game.obstacle.*;
import com.mygdx.game.util.*;
import com.mygdx.game.assets.*;

import java.util.Iterator;

public class GameplayController implements ContactListener {
    /**
     * The amount of time for a physics engine step.
     */
    public static final float WORLD_STEP = 1 / 60.0f;
    /**
     * Number of velocity iterations for the constrain solvers
     */
    public static final int WORLD_VELOC = 6;
    /**
     * Number of position iterations for the constrain solvers
     */
    public static final int WORLD_POSIT = 2;

    /**
     * Width of the game world in Box2d units
     */
    protected static final float DEFAULT_WIDTH = 32.0f;
    /**
     * Height of the game world in Box2d units
     */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /**
     * The default value of gravity (going down)
     */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    public static final int NUM_I_FRAMES = 120;


    /**
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects = new PooledList<Obstacle>();
    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    /**
     * The Box2D world
     */
    protected World world;
    /**
     * The boundary of the world
     */
    protected Rectangle bounds;
    /**
     * The world scale
     */
    protected Vector2 scale;

    /**
     * whether the player has won
     */
    private boolean completed;

    /**
     * whether the player has lost
     */
    private boolean failed;

    /**
     * Countdown active for winning or losing
     */
    private int countdown;

    /**
     * The texture for walls and platforms
     */
    protected TextureRegion platformTile;
    /**
     * Texture asset for character avatar
     */
    private TextureRegion avatarTexture;
    /**
     * Texture asset for front-facing player
     * */
    private TextureRegion avatarFront;
    /**
     * Texture asset for the wind gust
     */
    private TextureRegion windTexture;
    /**
     * Texture asset for umbrella
     */
    private TextureRegion umbrellaTexture;
    /**
     * Texture asset for closed umbrella
     */
    private TextureRegion closedTexture;

    /**
     * Texture asset for a bird
     */
    private TextureRegion birdTexture;

    /**
     * Texture asset for goal
     */
    private TextureRegion goalTexture;
    
    /**
     * Texture asset for lightning
     */
    private TextureRegion lightningTexture;

    /**
     * The jump sound.  We only want to play once.
     */
    private Sound jumpSound;
    private long jumpId = -1;
    /**
     * The weapon fire sound.  We only want to play once.
     */
    private Sound fireSound;
    private long fireId = -1;
    /**
     * The weapon pop sound.  We only want to play once.
     */
    private Sound plopSound;
    private long plopId = -1;
    /**
     * The default sound volume
     */
    private float volume;

    // Physics objects for the game
    /**
     * Physics constants for initialization
     */
    private JsonValue constants;
    /**
     * Reference to the character avatar
     */
    private PlayerModel avatar;
    /**
     * Reference to the umbrella
     */
    private UmbrellaModel umbrella;
    /**
     * Reference to the goalDoor (for collision detection)
     */
    private BoxObstacle goalDoor;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * The set of all wind fixtures that umbrella in contact with
     */
    protected ObjectSet<Fixture> contactWindFix = new ObjectSet<>();

    /**
     * The set of all wind bodies that umbrella in contact with
     */
    protected ObjectSet<WindModel> contactWindBod = new ObjectSet<>();
    //font for writing player health. temporary solution until a proper health asset is added
    private BitmapFont avatarHealthFont;
    //cache for vector computations
    Vector2 cache = new Vector2();
    //THESE ARE USED FOR MAKING THE UMBRELLA FOLLOW THE MOUSE POINTER
    //difference in initial position between umbrella and player
    private Vector2 diff = new Vector2();
    //center of the screen in canvas coordinates
    public Vector2 center = new Vector2();
    //the upward-pointing unit vector
    private Vector2 up = new Vector2(0,1);
    //current mouse position
    //should not be updated except when making the umbrella follow the mouse
    private Vector2 mousePos = new Vector2();
    //umbrella's last valid angle
    private float lastValidAng;
    //whether the mouse angle is allowed
    private boolean angInBounds = true;

    /**
     * The set of all birds currently in the level
     */
    private ObjectSet<BirdHazard> birds = new ObjectSet<>();

    /**
     * The set of all lightning currently in the level
     */
    private ObjectSet<LightningHazard> lightning = new ObjectSet<>();


    /**
     * the delay after game is lost before we transition to new screen.
     */
    private static final int LOSE_COUNTDOWN_TIMER = 15;

    /**
     * the delay after game is won before we transition to new screen.
     */
    private static final int WIN_COUNTDOWN_TIMER = 30;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayController(Rectangle bounds, Vector2 gravity) {
        world = new World(gravity, false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1, 1);
        countdown = -1;

        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
    }

    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        platformTile = new TextureRegion(directory.getEntry("shared:earth", Texture.class));
        avatarTexture = new TextureRegion(directory.getEntry("placeholder:player", Texture.class));
        avatarFront = new TextureRegion(directory.getEntry("placeholder:front", Texture.class));
        umbrellaTexture = new TextureRegion(directory.getEntry("placeholder:umbrella", Texture.class));
        windTexture = new TextureRegion(directory.getEntry("placeholder:wind", Texture.class));
        birdTexture = new TextureRegion(directory.getEntry("placeholder:bird", Texture.class));
        lightningTexture = new TextureRegion(directory.getEntry("placeholder:bird", Texture.class));
        closedTexture = new TextureRegion(directory.getEntry("placeholder:closed", Texture.class));
        goalTexture = new TextureRegion(directory.getEntry("placeholder:goal", Texture.class));

        jumpSound = directory.getEntry("platform:jump", Sound.class);
        fireSound = directory.getEntry("platform:pew", Sound.class);
        plopSound = directory.getEntry("platform:plop", Sound.class);

        constants = directory.getEntry("platform:constants", JsonValue.class);

        avatarHealthFont = directory.getEntry("shared:retro", BitmapFont.class);
        // NO (at least in this context, there is no gain in doing so because the cache font is accessible from all classes that load this entry.
        // avatarHealthFont.setColor(Color.RED);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity());

        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity, false);
        world.setContactListener(this);
        // game status reset
        failed = false;
        completed = false;
        // load level
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        JsonValue goal =  constants.get("goal");
        JsonValue goalpos = goal.get("pos");
        float dwidth = goal.getFloat("width");
        float dheight = goal.getFloat("height");
        goalDoor = new BoxObstacle(goalpos.getFloat(0), goalpos.getFloat(1),dwidth, dheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(goal.getFloat("density", 0));
        goalDoor.setFriction(goal.getFloat("friction", 0));
        goalDoor.setRestitution(goal.getFloat("restitution", 0));
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTexture);
        // doing so fits the texture onto the specified size of the object
        goalDoor.setTextureScale(
                dwidth * scale.x/goalTexture.getRegionWidth(),
                dheight * scale.y/goalTexture.getRegionHeight());
        goalDoor.setName("goal");
        addObject(goalDoor);

        // Setting Gravity on World
        JsonValue defaults = constants.get("defaults");
        world.setGravity(new Vector2(0, defaults.getFloat("gravity", DEFAULT_GRAVITY)));

        //TODO: explicit walls do not exist, consider deleting.
        // ============================================================================
        String wname = "wall";
        JsonValue walljv = constants.get("walls");
        for (int ii = 0; ii < walljv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(platformTile);
            obj.setName(wname + ii);
            addObject(obj);
        }
        // TODO maybe delete above =========================================================

        String pname = "platform";
        JsonValue platjv = constants.get("platforms");
        for (int ii = 0; ii < platjv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(platformTile);
            obj.setName(pname + ii);
            addObject(obj);
        }

        // Create player
        float scl = constants.get("player").getFloat("texturescale");
        dwidth = avatarTexture.getRegionWidth() / scale.x * scl;
        dheight = avatarTexture.getRegionHeight() / scale.y * scl;
        avatar = new PlayerModel(constants.get("player"), dwidth, dheight, constants.get("player").getInt("maxhealth"));
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        avatar.setHpTexture(avatarTexture);
        avatar.healthFont = avatarHealthFont;
        addObject(avatar);
        scl = constants.get("umbrella").getFloat("texturescale");
        dwidth = umbrellaTexture.getRegionWidth() / scale.x * scl;
        dheight = umbrellaTexture.getRegionHeight() / scale.y * scl;
        umbrella = new UmbrellaModel(constants.get("umbrella"), dwidth, dheight);
        umbrella.setDrawScale(scale);
        umbrella.setTexture(umbrellaTexture);
        umbrella.setClosedMomentum(constants.get("umbrella").getFloat("closedmomentum"));
        umbrella.setPosition(constants.get("umbrella").get("pos").getFloat(0), constants.get("umbrella").get("pos").getFloat(1));
        addObject(umbrella);
        diff.x = umbrella.getX()-avatar.getX();
        diff.y = umbrella.getY()-avatar.getY();

       

        // Create wind gusts
        String windName = "wind";
        JsonValue windjv = constants.get("wind");
        for (int ii = 0; ii < windjv.size; ii++) {
            WindModel obj;
            obj = new WindModel(windjv.get(ii));
            obj.setDrawScale(scale);
            obj.setTexture(windTexture);
            obj.setName(windName + ii);
            addObject(obj);
        }

        //create hazards
        JsonValue hazardsjv = constants.get("hazards");

        //create birds
        String birdName = "bird";
        JsonValue birdjv = hazardsjv.get("birds");
        int birdDamage = hazardsjv.getInt("birdDamage");
        int birdSensorRadius = hazardsjv.getInt("birdSensorRadius");
        int birdAttackSpeed = hazardsjv.getInt("birdAttackSpeed");
        float birdKnockback = hazardsjv.getInt("birdKnockback");
        for (int ii = 0; ii < birdjv.size; ii++) {
            BirdHazard obj;
            obj = new BirdHazard(birdjv.get(ii), birdDamage, birdSensorRadius, birdAttackSpeed, birdKnockback);
            obj.setDrawScale(scale);
            obj.setTexture(birdTexture);
            obj.setName(birdName + ii);
            addObject(obj);
            birds.add(obj);
        }

        String lightningName = "lightning";
        JsonValue lightningjv = hazardsjv.get("lightning");
        for (int ii = 0; ii < lightningjv.size; ii++) {
            LightningHazard obj;
            obj = new LightningHazard(lightningjv.get(ii));
            obj.setDrawScale(scale);
            obj.setTexture(lightningTexture);
            obj.setName(lightningName + ii);
            addObject(obj);
            lightning.add(obj);
        }

        volume = constants.getFloat("volume", 1.0f);

        // Create invisible |_| shaped world boundaries so player is within bounds.
        dwidth = bounds.width;
        dheight = bounds.height;
        String wallName = "barrier";

        // TODO: create some loop, too much duplication.
        // Create the left wall
        BoxObstacle wall = new BoxObstacle(-0.5f, dheight/2f, 1, 2*dheight);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);

        // Create the right wall
        wall = new BoxObstacle(dwidth-0.5f, dheight/2f, 1, 2*dheight);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);

        // Create the bottom wall
        // TODO: if ground is y-level 0, the wall's y-position should be around [-0.5, -2].
        wall = new BoxObstacle(dwidth/2f, -dheight/2f, dwidth, 1);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(InputController input, float dt) {
        // Process actions in object model

        // player dies if falling through void
        if (!failed && avatar.getPosition().y <= -0.5f){
            avatar.setHealth(0);
            setFailed();
            return;
        }

        // decrement countdown towards rendering victory/fail screen
        if (countdown > 0){
            countdown--;
        }

        // Check for whether the player toggled the umbrella being open/closed
        if (input.didToggle()) {
            umbrella.setOpen(!umbrella.isOpen());
            if (umbrella.isOpen()) {
                umbrella.setTexture(umbrellaTexture);
                //TODO: apply some force to the player so the floatiness comes back
            }
            else {
                umbrella.setTexture(closedTexture);
                Body body = avatar.getBody();
                body.setLinearVelocity(body.getLinearVelocity().x*umbrella.getClosedMomentum(), body.getLinearVelocity().y);
            }
        }
        //make player face forward in air
        if (avatar.isGrounded() && avatar.getTexture() != avatarTexture) avatar.setTexture(avatarTexture);
        else if (!avatar.isGrounded() && avatar.getTexture() != avatarFront) avatar.setTexture(avatarFront);

        //umbrella points towards mouse pointer
        mousePos.x = input.getMousePos().x;
        mousePos.y = input.getMousePos().y;
        //convert from screen coordinates to canvas coordinates
        mousePos.y=2*center.y-mousePos.y;
        //convert to player coordinates
        mousePos.sub(center);
        //normalize manually because Vector2.nor() is less accurate
        float l = mousePos.len();
        mousePos.x/=l;
        mousePos.y/=l;
        //compute new angle
        float mouseAng = (float) Math.acos(mousePos.dot(up));
        if (input.getMousePos().x > center.x) mouseAng*=-1;
        angInBounds = mouseAng <= (float) Math.PI/2 && mouseAng >= -(float) Math.PI/2;
        if (angInBounds){
            umbrella.setAngle(mouseAng);
            lastValidAng = mouseAng;
        } else if (lastValidAng >= 0) {
            umbrella.setAngle((float) Math.PI/2);
            mousePos.x = -1;
            mousePos.y = 0;
        }
        else {
            umbrella.setAngle(-(float) Math.PI/2);
            mousePos.x = 1;
            mousePos.y = 0;
        }

        boolean touching_wind = contactWindFix.size > 0;
        float ang = umbrella.getRotation();
        float umbrellaX = (float) Math.cos(ang);
        float umbrellaY = (float) Math.sin(ang);
        for (Fixture w : contactWindFix) {
            WindModel bod = (WindModel) w.getBody().getUserData();
            float f = bod.getWindForce(ang);
            if (!contactWindBod.contains(bod) && umbrella.isOpen()) {
                avatar.applyExternalForce(umbrellaX * f, umbrellaY * f);
                contactWindBod.add(bod);
            }
        }
        contactWindBod.clear();

        // Process actions in object model
        if (avatar.isGrounded()) {
            avatar.setMovement(input.getHorizontal() * avatar.getForce());
            avatar.applyInputForce();
        } else if (!touching_wind && umbrella.isOpen() && avatar.getVY() < 0) {
            // player must be falling through AIR
            // apply horizontal force based on rotation, and upward drag.
            float angle = umbrella.getRotation() % ((float) Math.PI * 2);
            if (angle < Math.PI) {
                int sclx = 6;
                int scly = 5;
                avatar.applyExternalForce(sclx * (float) Math.sin(2 * angle), scly * (float) Math.sin(angle));
            }
        }

        // enable this and put it in a conditional statement if we decide to still have an arrow key mode
//        umbrella.setTurning(input.getMouseMovement() * umbrella.getForce());
//        umbrella.applyForce();

        //move the birds
        for (BirdHazard bird : birds) {
            bird.move();
        }

        //update the lightnings
        for (LightningHazard light : lightning){
            light.strike();
        }
    }

    /**
     * Callback method for the start of a collision
     * <p>
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
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && bd1.getName().contains("platform")) ||
                    (avatar.getSensorName().equals(fd1) && bd2.getName().contains("platform"))) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // See if umbrella touches wind
            if ((umbrella == bd2 && (bd1.getClass() == WindModel.class)) ||
                    (umbrella == bd1 && (bd2.getClass() == WindModel.class))) {
                Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
                contactWindFix.add(windFix);
            }

            // Check for hazard collision
            // Is there any way to add fixture data to all fixtures in a polygon obstacle without changing the
            // implementation? If so, want to change to fd1 == "damage"
            if (((umbrella == bd2 || avatar == bd2) && (bd1 instanceof HazardModel && fd1 == null) ||
                    ((umbrella == bd1 || avatar == bd1) && (bd2 instanceof HazardModel && fd2 == null)))) {
                HazardModel h = (HazardModel) (bd1 instanceof HazardModel ? bd1 : bd2);
                    int dam = h.getDamage();
                    if (avatar.getiFrames() == 0) {
                        if (avatar.getHealth() - dam > 0) {
                            Vector2 knockback = h.getKnockbackForce().scl(h.getKnockbackScl());
                            avatar.getBody().applyLinearImpulse(knockback, avatar.getPosition(), true);
                            avatar.setHealth(avatar.getHealth() - dam);
                            avatar.setiFrames(NUM_I_FRAMES);
                        } else {
                          avatar.setHealth(0);
                          setFailed();
                        }
                    }
            }

            // check for bird sensor collision
            if ((avatar == bd1 && fd2 == "birdSensor") ||
                    (avatar == bd2 && fd1 == "birdSensor")) {
                BirdHazard bird = (BirdHazard) ("birdSensor" == fd1 ? bd1 : bd2);
                if (!bird.seesTarget) {
                    bird.seesTarget = true;
                    bird.setTargetDir(avatar.getX(), avatar.getY(), avatar.getVX(), avatar.getVY());
                }
            }

            // Check for win condition
            if ((bd1 == avatar && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                // player wins
                if (!failed && !completed){
                    setCompleted();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     * <p>
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

        Obstacle bd1 = (Obstacle) body1.getUserData();
        Obstacle bd2 = (Obstacle) body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

        if ((umbrella == bd2 && bd1.getName().contains("wind")) ||
                (umbrella == bd1 && bd2.getName().contains("wind"))) {
            Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
            contactWindFix.remove(windFix);
        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /**
     * Called when the Screen is paused.
     * <p>
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
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    /**
     * Immediately adds the object to the physics world
     * <p>
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Adds a physics object in to the insertion queue.
     * <p>
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     * <p>
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Processes physics
     * <p>
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
        //make umbrella follow player position. since it is a static body, we update
        //its position after the world step so that it properly follows the player
        cache.x = avatar.getX()+mousePos.x*diff.len();
        cache.y = avatar.getY()+mousePos.y*diff.len();
        umbrella.setPosition(cache.x, cache.y);

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
                if (obj.getClass() == BirdHazard.class) birds.remove((BirdHazard) obj);
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
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
    }

    /**
     * Gets all the objects in objects
     */
    public PooledList<Obstacle> getObjects() {
        return objects;
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    /**
     *
     * @return player x-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenX(){

        return avatar != null ? avatar.getDrawScale().x * avatar.getX() : -1;
    }

    /**
     *
     * @return player y-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenY(){
        return avatar != null ? avatar.getDrawScale().y * avatar.getY() : -1;
    }

    /**
     *
     * @return reference to player model
     */
    public PlayerModel getPlayer(){
        return avatar;
    }

    /**
     * set world bounds to be the given rectangle dimensions.
     * This should be followed with a reset of the game.
     * @param rect the bounds
     */
    public void setBounds(Rectangle rect){
        this.bounds.set(rect.x, rect.y, rect.getWidth(), rect.getHeight());
    }


    /**
     * player officially wins if they finished the level and
     * a small countdown is over.
     * @return whether player finished level
     */
    public boolean isCompleted(){
        return completed && countdown <= 0;
    }

    /**
     * player officially fails if they failed the level and
     * a small countdown is over.
     * @return whether player failed
     */
    public boolean isFailed(){
        return failed && countdown <= 0;
    }

    /**
     * set player level status to completed and start a countdown timer
     */
    private void setCompleted(){
        completed = true;
        countdown = WIN_COUNTDOWN_TIMER;
    }

    /**
     * set player level status to failed and start a countdown timer
     */
    private void setFailed(){
        failed = true;
        countdown = LOSE_COUNTDOWN_TIMER;
    }
}
