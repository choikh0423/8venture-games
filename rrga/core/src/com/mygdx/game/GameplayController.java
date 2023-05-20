package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.*;
import com.mygdx.game.model.*;
import com.mygdx.game.model.hazard.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.util.Drawable;
import com.mygdx.game.utility.util.PooledList;

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
     * Number of position iterations for the constrain solvers s
     */
    public static final int WORLD_POSIT = 2;

    /**
     * the iframes effect duration
     */
    public static final int NUM_I_FRAMES = 120;

    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue = new PooledList<>();

    /**
     * The Box2D world
     */
    protected World world;

    /**
     * The boundary of the world
     */
    protected Rectangle bounds;

    /**
     * The world draw scale (world coordinates to screen coordinates)
     */
    protected Vector2 scale;

    /**
     * scaling factors for drag force
     */
    private Vector2 dragScale;

    /**
     * whether the player has won
     */
    private boolean completed;

    /**
     * whether the player has lost
     */
    private boolean failed;

    /**
     * Strong Wind Sound Effect
     */
    private Sound windStrongSFX;
    private Sound birdAlertSFX;
    private Music birdFlapSFX;
    private Sound lightningSFX;
    private Music walkingSFX;
    private Music cloudWalkingSFX;

    /** Strong Wind Sound Effect Current Frame*/
    private int windStrongFrame = 0;

    /**
     * Strong Wind Sound Effect Duration Frame
     */
    //TODO: This needs to be meticulously calculated later
    private final int WIND_STRONG_DURATION = 60;
    /**
     * Boolean to check if previously was in wind
     */
    //TODO: This needs to be meticulously calculated later
    private boolean prevInWind = false;


    // <=============================== Physics objects for the game BEGINS here ===============================>
    /**
     * Physics constants for global
     */
    private JsonValue globalConstants;

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

    protected ObjectSet<Fixture> contactNewWindFix = new ObjectSet<>();
    /**
     * The set of all wind bodies that umbrella in contact with
     */
    protected ObjectSet<WindModel> contactWindBod = new ObjectSet<>();
    protected ObjectSet<NewWindModel> contactNewWindBod = new ObjectSet<>();

    /**
     * The set of all nests currently in the level
     */
//    private final ObjectSet<NestHazard> nests = new ObjectSet<>();

    protected final ObjectSet<HazardModel> contactHazards = new ObjectSet<>();

    /**
     * The set of all hazard fixtures that umbrella in contact with
     */
    protected ObjectSet<Fixture> contactHazardFixtures = new ObjectSet<>();

    /**
     * weld joint definition struct
     */
    private final WeldJointDef weldJointDef = new WeldJointDef();

    /**
     * the avatar-cloud joint
     */
    private WeldJoint avatarWeldJoint;

    /**
     * whether avatar is touching a movable cloud.
     */
    private boolean touchingMovingCloud;

    /**
     * the cloud body that the avatar is touching (there may be several, take FIRST)
     */
    private Body contactedCloudBody;

    /**
     * whether to destroy
     */
    private boolean destroyWeldJoint;

    // <=============================== Physics objects for the game ENDS here ===============================>

    /**
     * cache for vector computations
     */
    Vector2 cache = new Vector2();

    /**
     * second cache for vector computations
     */
    Vector2 temp = new Vector2();

    //THESE ARE USED FOR MAKING THE UMBRELLA FOLLOW THE MOUSE POINTER

    /**
     * center of the screen in canvas coordinates
     */
    private final Vector2 center = new Vector2();

    /**
     * the upward-pointing unit vector
     */
    private final Vector2 up = new Vector2(0, 1);

    /**
     * current mouse position
     * <br>
     * should not be updated except when making the umbrella follow the mouse
     */
    private final Vector2 mousePos = new Vector2();

    /**
     * umbrella's last valid angle
     */
    private float lastValidAng;

    /**
     * whether the mouse angle is allowed
     */
    private boolean angInBounds = true;

    private boolean wasOpen;

    /**
     * The level container for GameplayController
     */
    private LevelContainer levelContainer;

    // ====================== (BEGIN) SOUND-related fields =============================

    /**
     * The sound effects volume
     */
    private float SFXVolume = 0.0f;

    // ====================== (END) SOUND-related fields =============================


    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayController(Rectangle bounds, Vector2 gravity) {
        world = new World(gravity, false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1, 1);
        this.dragScale = new Vector2(1, 1);

        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();

        // Initialize level container
        levelContainer = new LevelContainer(world, this.bounds, this.scale);
    }

    /**
     * Gather the assets for this controller.
     * <br>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Setting up Constant/Asset Path for different levels
        String constantPath = "global:constants";

        globalConstants = directory.getEntry(constantPath, JsonValue.class);

        // Level container gather assets
        levelContainer.gatherAssets(directory);
        windStrongSFX = directory.getEntry("sound:wind_strong", Sound.class);
        birdAlertSFX = directory.getEntry("sound:bird_alert", Sound.class);
        birdFlapSFX = directory.getEntry("music:bird_flap", Music.class);
        lightningSFX = directory.getEntry("sound:lightning", Sound.class);
        walkingSFX = directory.getEntry("music:walking", Music.class);
        cloudWalkingSFX = directory.getEntry("music:walking_clouds", Music.class);

        dragScale.x = globalConstants.get("player").getFloat("drag_x", 1);
        dragScale.y = globalConstants.get("player").getFloat("drag_y", 1);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {

        if (avatarWeldJoint != null) {
            world.destroyJoint(avatarWeldJoint);
        }
        avatarWeldJoint = null;
        touchingMovingCloud = false;

        Vector2 gravity = new Vector2(world.getGravity());

        for (Obstacle obj : levelContainer.getObjects()) {
            obj.deactivatePhysics(world);
        }

        world.dispose();
        world = new World(gravity, false);
        world.setContactListener(this);

        // game status reset
        failed = false;
        completed = false;

        // empty LevelContainer and update its world.
        levelContainer.reset();
        levelContainer.setWorld(world);

        // Populate LevelContainer w/ same level
        levelContainer.populateLevel();
        goalDoor = levelContainer.getGoalDoor();
        avatar = levelContainer.getAvatar();
        umbrella = levelContainer.getUmbrella();

        stopSFX();
    }

    // track updates to player
    /**
     * whether there is input to move player
     */
    boolean moveInputted = false;

    /**
     * whether umbrella is in boost
     */
    boolean umbrellaBoosted = false;

    /**
     * whether Gale's affected by wind
     */
    boolean windPushed = false;

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
        if (!failed && avatar.getPosition().y <= -0.01f) {
            avatar.setHealth(0);
            avatar.setiFrames(NUM_I_FRAMES);
            setFailed();
            return;
        }

        //UMBRELLA
        umbrella.canBoost = avatar.canBoost();
        //only allow control when not zooming and not showing goal
        if ((!input.didZoom() || (avatar.isMoving() || !avatar.isGrounded() || avatar.getLinearVelocity().len() > 0.0001f))) {
            // Check for whether the player toggled the umbrella being open/closed
            if (!input.secondaryControlMode) {
                if (input.didToggle() && !umbrella.isBoosting()) {
                    umbrella.setOpen(!umbrella.isOpen());
                    if (umbrella.isOpen()) {
                        umbrella.useOpenedTexture();
                        //TODO: apply some force to the player so the floatiness comes back
                    } else {
                        umbrella.useClosedTexture();
                        Body body = avatar.getBody();
                        body.setLinearVelocity(body.getLinearVelocity().x * umbrella.getClosedMomentumX(), body.getLinearVelocity().y * umbrella.getClosedMomentumY());
                    }
                }
            } else {
                if (input.isToggleHeld() || umbrella.isBoosting()) {
                    umbrella.setOpen(true);
                    umbrella.useOpenedTexture();
                    wasOpen = true;
                } else {
                    umbrella.setOpen(false);
                    umbrella.useClosedTexture();
                    Body body = avatar.getBody();
                    if (wasOpen)
                        body.setLinearVelocity(body.getLinearVelocity().x * umbrella.getClosedMomentumX(), body.getLinearVelocity().y * umbrella.getClosedMomentumY());
                    wasOpen = false;
                }
            }

            //umbrella points towards mouse pointer
            center.x = Gdx.graphics.getWidth() / 2f;
            center.y = Gdx.graphics.getHeight() / 2f;
            mousePos.x = input.getMousePos().x;
            mousePos.y = input.getMousePos().y;
            //convert from screen coordinates to canvas coordinates
            mousePos.y = Gdx.graphics.getHeight() - mousePos.y;
            //convert to player coordinates
            mousePos.sub(center);
            //normalize manually because Vector2.nor() is less accurate
            float l = mousePos.len();
            mousePos.x /= l;
            mousePos.y /= l;
            //compute new angle
            float mouseAng = (float) Math.acos(mousePos.dot(up));
            if (input.getMousePos().x > center.x) mouseAng *= -1;
            //angInBounds = mouseAng <= (float) Math.PI / 2 && mouseAng >= -(float) Math.PI / 2;
            if (angInBounds) {
                umbrella.setAngle(mouseAng);
                lastValidAng = mouseAng;
                //Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            } else if (lastValidAng >= 0) {
                umbrella.setAngle((float) Math.PI / 2);
                mousePos.x = -1;
                mousePos.y = 0;
                //Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NotAllowed);
            } else {
                umbrella.setAngle(-(float) Math.PI / 2);
                mousePos.x = 1;
                mousePos.y = 0;
                //Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NotAllowed);
            }
        }

        //make player face forward in air
        if (avatar.isGrounded()) avatar.useSideTexture();
        else avatar.useFrontTexture();

        avatar.setZooming(input.didZoom());

        //average the force of touched winds
        boolean touching_wind = contactWindFix.size > 0 || contactNewWindFix.size > 0;
        float ang = umbrella.getRotation();
        float umbrellaX = (float) Math.cos(ang);
        float umbrellaY = (float) Math.sin(ang);
        int count = 0;
        cache.set(0, 0);
        for (Fixture w : contactWindFix) {
            WindModel bod = (WindModel) w.getBody().getUserData();
            float f = bod.getWindForce(ang);
            if (!contactWindBod.contains(bod) && umbrella.isOpen()) {
                count++;
                cache.add(umbrellaX * f, umbrellaY * f);
                contactWindBod.add(bod);
            }
        }

        for (Fixture w : contactNewWindFix) {
            NewWindModel bod = (NewWindModel) w.getBody().getUserData();
            float f = bod.getWindForce(ang);
            if (!contactNewWindBod.contains(bod) && umbrella.isOpen()) {
                count++;
                cache.add(umbrellaX * f, umbrellaY * f);
                contactNewWindBod.add(bod);
            }
        }

        if (count != 0) {
            // TODO: We might want to make a separate update loop for sounds
            // Play Strong Wind SFX
            if (windStrongFrame < 0 && !prevInWind) {
                windStrongSFX.stop();
                windStrongSFX.play(SFXVolume*15);
                windStrongFrame = WIND_STRONG_DURATION;

                // To prevent repeat all the time - only if you go out and come back in
                prevInWind = true;
            } else {
                windStrongFrame--;
            }
            windPushed = cache.len2() > 0;
            if (windPushed) {
                avatar.applyWindForce(cache.x / count, cache.y / count);
            }

        } else {
            // Gradually Reset Strong Wind SFX
            if (windStrongFrame > 0) {
                windStrongFrame--;
            }
            windStrongSFX.stop();
            prevInWind = false;
        }


        // Process player movement
        float angle = umbrella.getRotation();
        moveInputted = input.getHorizontal() != 0;
        if (avatar.isGrounded() && (!input.didZoom() || (avatar.isMoving() || avatar.getLinearVelocity().len() > 0.0001f))) {
            // frame N: Gale on ground, press boost
            // frame N+1: Gale still on ground, do not apply dampening.
            if (!umbrella.isBoosting()) {
                avatar.setMovement(input.getHorizontal() * avatar.getForce());
                avatar.applyWalkingForce();
                if(moveInputted){
                    if (avatar.onCloud){
                        cloudWalkingSFX.setVolume(0.2f*SFXVolume);
                        cloudWalkingSFX.play();
                    } else {
                        walkingSFX.setVolume(0.2f*SFXVolume);
                        walkingSFX.play();
                    }
                }
            }
        } else if (!windPushed && umbrella.isOpen() && angle < Math.PI && avatar.getVY() < 0) {
            // player must be falling through AIR
            // apply horizontal force based on rotation, and upward drag.
            avatar.applyDragForce(dragScale.x * (float) Math.sin(2 * angle));
        } else if (!umbrella.isOpen()) {
            avatar.dampAirHoriz();
        }

        if ((umbrella.isOpen() && angle < Math.PI) && avatar.getVY() < avatar.getMaxSpeedDownOpen()) {
            avatar.setVY(avatar.getMaxSpeedDownOpen());
        }
        if ((!umbrella.isOpen() || angle > Math.PI) && avatar.getVY() < avatar.getMaxSpeedDownClosed()) {
            avatar.setVY(avatar.getMaxSpeedDownClosed());
        }

        // Process Lighter Force
        if (input.getLighter() && umbrella.isOpen() && !input.didZoom()) {
            if (avatar.applyLighterForce(ang)) umbrella.startBoost();
        }
        if (avatar.isGrounded()) {
            avatar.refillLighter();
        }

        contactHazards.clear();
        for (Fixture f : contactHazardFixtures) {
            HazardModel bod = (HazardModel) f.getBody().getUserData();
            if (!contactHazards.contains(bod)) {
                contactHazards.add(bod);
            }
        }

        //Process Hazard Collisions
        for (HazardModel h : contactHazards) {
            int dam = h.getDamage();
            // player is only vulnerable to further damage and effects if the level is still ongoing
            boolean vulnerable = !failed && !completed;
            if (avatar.getiFrames() == 0 && vulnerable) {
                float avatarSpeed = avatar.getLinearVelocity().len();
                if (h.getKnockBackScl() != 0) {
                    cache.set(h.getKnockBackForce());
                    // apply scaling to provide speed
                    // at bare minimum, our speed should not really decrease since we need to get player out of contact.
                    cache.scl(Math.max(h.getKnockBackScl(), avatarSpeed));
                    avatar.getBody().setLinearVelocity(cache);
                }
                if (avatar.getHealth() - dam > 0) {
                    avatar.setHealth(avatar.getHealth() - dam);
                    avatar.setiFrames(NUM_I_FRAMES);
                } else {
                    avatar.setHealth(0);
                    // start iframes even when we die, otherwise player being damaged is not so apparent.
                    avatar.setiFrames(NUM_I_FRAMES);
                    setFailed();
                }
            }
        }

        //move moving platforms
        for (MovingPlatformModel mp : levelContainer.getMovingPlats()) {
            mp.move();
        }

        //Bird Updates
        int birdRays = 5;
        BirdRayCastCallback rccb = new BirdRayCastCallback();
        // vector reference (alias to make code more readable)
        Vector2 pos = cache;
        Vector2 target = temp;

        //loop through birds
        for (BirdHazard bird : levelContainer.getBirds()) {
            bird.setSfxVol(SFXVolume);
            //If sees target, wait before attacking
            if (bird.seesTarget) {
                if (bird.attackWait == 0) {
                    bird.setTargetDir(avatar.getX(), avatar.getY(), avatar.getVX(), avatar.getVY());
                    bird.attackWait--;
                    bird.warning = false;
                } else if (bird.attackWait > 0) {
                    bird.attackWait--;
                    bird.setFaceRight(!(avatar.getX() - bird.getX() < 0));
                }
            }

            //move the birds
            bird.move();

            if (bird.getAABBx() >= bounds.width + 20 || bird.getAABBy() < 0
                    || bird.getAABBx() + bird.getWidth() <= -20
                    || bird.getAABBy() - bird.getHeight() > bounds.height * bounds.height ) {
                //(brown/green/red) mark removed so that it is garbage collected at end of update loop
                // blue: set spawning again
                if (bird.getColor().equals(BirdHazard.BirdColor.BLUE)){
                    ((NestedBirdHazard) bird).setSpawning();
                }
                else {
                    bird.markRemoved(true);
                }
                continue;
            }

            float bx = bird.getX();
            float by = bird.getY();
            float px = avatar.getX();
            float py = avatar.getY();
            temp.set(px, py);
            temp.sub(bx, by);
            temp.nor();

            //adapted from https://stackoverflow.com/questions/6247153/angle-from-2d-unit-vector
            if (temp.x == 0) {
                angle = (temp.y > 0) ? (float) Math.PI / 2 : (temp.y == 0) ? 0 : 3 * (float) Math.PI / 2;
            } else if (temp.y == 0) {
                angle = (temp.x >= 0) ? 0 : (float) Math.PI;
            } else {
                angle = (float) Math.atan(temp.y / temp.x);
                if (temp.x < 0 && temp.y < 0) // quadrant Ⅲ
                    angle += Math.PI;
                else if (temp.x < 0) // quadrant Ⅱ
                    angle += Math.PI;
                else if (temp.y < 0) // quadrant Ⅳ
                    angle += 2 * Math.PI;
            }

            float dist = (float) Math.sqrt(Math.pow(px - bx, 2) + Math.pow(py - by, 2));
            boolean check = dist < bird.getSensorRadius();
            //send out rays and check for collisions with player
            if (bird.getAttack() && check) {
                // load position into cache
                pos.set(bx, by);
                for (int i = 0; i < birdRays; i++) {
                    rccb.collisions.clear();
                    float minDist = Integer.MAX_VALUE;
                    // load ray target into temporary cache
                    target.set(bx + bird.getSensorRadius(), by).rotateAroundRad(pos, angle - (float) (Math.PI / 16) + (float) (Math.PI / 8) * i / birdRays);
                    //DEPRECIATED
                    // target.set(bx, by + bird.getSensorRadius()).rotateAroundDeg(pos, 360f * i / birdRays);
                    world.rayCast(rccb, pos, target);
                    for (ObjectMap.Entry<Fixture, Float> e : rccb.collisions.entries()) {
                        if (e.value < minDist) {
                            minDist = e.value;
                        }
                    }
                    for (ObjectMap.Entry<Fixture, Float> e : rccb.collisions.entries()) {
                        if ((e.key).getBody().getUserData() == avatar) {
                            if (Math.abs(e.value - minDist) < .001) {
                                if (!bird.seesTarget) {
                                    bird.seesTarget = true;
                                    bird.setFaceRight(!(px - bx < 0));
                                    birdAlertSFX.play(SFXVolume);
                                    bird.warning = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        //criterion to disconnect player from moving platform when ANY of the following holds
        // - player can move (on platform) and tries to move
        // - player comes into contact with hazards
        // - player gets force from wind
        // - player boosts with umbrella
        destroyWeldJoint = moveInputted || contactHazards.size > 0 || windPushed || umbrella.isBoosting();

        if (destroyWeldJoint && avatarWeldJoint != null) {
            world.destroyJoint(avatarWeldJoint);
            avatarWeldJoint = null;
        }

        // attach player to cloud platform when ALL of the following holds
        // - player is standing, not pressing move
        // - touching moving platform.
        // - not using boost
        // - no force from wind
        // - no hit from hazard
        if (avatar.isGrounded() && touchingMovingCloud && !destroyWeldJoint && avatarWeldJoint == null) {
            avatar.setLinearVelocity(temp.set(0, 0));
            weldJointDef.initialize(avatar.getBody(), contactedCloudBody,
                    temp.set(avatar.getX(), avatar.getY() - avatar.getHeight() / 2)
            );
            weldJointDef.collideConnected = true;
            avatarWeldJoint = (WeldJoint) world.createJoint(weldJointDef);
        }
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
        contactWindBod.clear(); //todo: remove old wind
        contactNewWindBod.clear();
        umbrellaBoosted = false;
        moveInputted = false;
        windPushed = false;
        destroyWeldJoint = false;

        // Add any objects created by actions
        while (!levelContainer.addQueue.isEmpty()) {
            levelContainer.addObject(levelContainer.addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
        //make umbrella follow player position. since it is a static body, we update
        //its position after the world step so that it properly follows the player
        umbrella.setPosition(avatar.getX(), avatar.getY());

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = levelContainer.getObjects().entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                if (obj instanceof AnimatedLightningHazard) ((AnimatedLightningHazard) obj).setSfxVol(SFXVolume);
                obj.update(dt);
            }
        }

        // clean-up the list of active birds
        Iterator<PooledList<BirdHazard>.Entry> birdIterator = levelContainer.getBirds().entryIterator();
        while (iterator.hasNext()) {
            PooledList<BirdHazard>.Entry entry = birdIterator.next();
            BirdHazard bird = entry.getValue();
            if (bird.isRemoved()) {
                entry.remove();
            }
        }

        // delete from drawables if some object has been deleted
        // INVARIANT: sorted list after removals is still sorted.
        Iterator<PooledList<Drawable>.Entry> iterator2 = levelContainer.getDrawables().entryIterator();
        while (iterator2.hasNext()) {
            PooledList<Drawable>.Entry entry = iterator2.next();
            Drawable drawable = entry.getValue();
            if (drawable instanceof Obstacle && ((Obstacle) drawable).isRemoved()) {
                entry.remove();
            }
        }

        // TODO: TEMPORARY SCROLL
        if (levelContainer.getShowGoal().getPosition().dst(avatar.getPosition()) > 0.0001)
            levelContainer.getShowGoal().move();
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
            boolean isAvatarSensor = avatar.getSensorName().equals(fd2) || avatar.getSensorName().equals(fd1);
            if ((isAvatarSensor && bd1.getName().contains("platform")) ||
                    (isAvatarSensor && bd2.getName().contains("platform"))
                    // ||(isAvatarSensor && bd1 instanceof RockHazard) || (isAvatarSensor && bd2 instanceof RockHazard)
                ) {
                boolean prev = avatar.isGrounded();
                if(bd1.getName().contains("moving_platform") || bd2.getName().contains("moving_platform")) avatar.onCloud = true;
                avatar.setGrounded(true);
                if (avatar.isGrounded() != prev) {
                    avatar.startLand();
                }
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground

                // TODO : cloud platforms are named "moving_platform" hence they have the "platform" part in their name.
                //  it might be better to have platform class. Using debug name is not safe for all obstacles (sometimes
                //  we might forget to assign object names and get unnecessary nullptr.
                Body cloudBody = null;
                MovingPlatformModel cloud = null;
                if (bd1 instanceof MovingPlatformModel) {
                    cloudBody = body1;
                    cloud = (MovingPlatformModel) bd1;
                } else if (bd2 instanceof MovingPlatformModel) {
                    cloudBody = body2;
                    cloud = (MovingPlatformModel) bd2;
                }
                // TODO (revisit this choice): the FIRST cloud touched is the one Gale sticks to.
                //  (revisit again): second edit, updated to the LAST CLOUD touched
                //  To optimize joint-create-destroy time, non-movable clouds of course don't need joints with avatar.
                if (cloudBody != null && cloud.getMoveSpeed() > 0) {
                    touchingMovingCloud = true;
                    contactedCloudBody = cloudBody;
                }
            }

            // See if umbrella touches wind
            if ((fd2 == "umbrellaSensor" && (bd1.getClass() == WindModel.class)) ||
                    (fd1 == "umbrellaSensor" && (bd2.getClass() == WindModel.class))) {
                Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
                contactWindFix.add(windFix);
            }

            if ((fd2 == "umbrellaSensor" && (bd1.getClass() == NewWindModel.class)) ||
                    (fd1 == "umbrellaSensor" && (bd2.getClass() == NewWindModel.class))) {
                Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
                contactNewWindFix.add(windFix);
            }

            // Check for hazard collision
            // Is there any way to add fixture data to all fixtures in a polygon obstacle without changing the
            // implementation? If so, want to change to fd1 == "damage"
            if (((fd2 == "umbrellaSensor" || avatar == bd2) && (bd1 instanceof HazardModel && fd1 == null) ||
                    ((fd1 == "umbrellaSensor" || avatar == bd1) && (bd2 instanceof HazardModel && fd2 == null)))) {
                HazardModel h = (HazardModel) (bd1 instanceof HazardModel ? bd1 : bd2);

                //norm from a to b
                WorldManifold wm = contact.getWorldManifold();
                Vector2 norm = cache.set(wm.getNormal());
                if (norm.len() != 0.0f) {
                    norm.nor();
                    float flip = (bd1 instanceof HazardModel ? 1 : -1);
                    h.setKnockBackForce(norm.scl(flip));
                    contactHazardFixtures.add(bd1 instanceof HazardModel ? fix1 : fix2);
                }
                else if (h.getDamage() == avatar.getMaxHealth()){
                    // death zone
                    contactHazardFixtures.add(bd1 instanceof HazardModel ? fix1 : fix2);
                }

            }

            // Check for win condition
            if ((bd1 == avatar && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                // player wins
                if (!failed && !completed) {
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
                boolean prev = avatar.isGrounded();
                avatar.setGrounded(false);
                if (avatar.isGrounded() != prev) {
                    avatar.startTakeoff();
                }
            }
            boolean isCloud1 = bd1 instanceof MovingPlatformModel;
            boolean isCloud2 = bd2 instanceof MovingPlatformModel;
            Body cloudBody = isCloud1 ? body1 : isCloud2 ? body2 : null;
            if (cloudBody == contactedCloudBody) {
                touchingMovingCloud = false;
                contactedCloudBody = null;
                destroyWeldJoint = true;
            }
        }

        if ((umbrella == bd2 && bd1 instanceof WindModel) ||
                (umbrella == bd1 && bd2 instanceof WindModel)) {
            Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
            contactWindFix.remove(windFix);
        }

        if ((umbrella == bd2 && bd1 instanceof NewWindModel) ||
                (umbrella == bd1 && bd2 instanceof NewWindModel)) {
            Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
            contactNewWindFix.remove(windFix);
        }

        if (((umbrella == bd2 || avatar == bd2) && (bd1 instanceof HazardModel && fd1 == null) ||
                ((umbrella == bd1 || avatar == bd1) && (bd2 instanceof HazardModel && fd2 == null)))) {
            HazardModel h = (HazardModel) (bd1 instanceof HazardModel ? bd1 : bd2);
//            if (h instanceof BirdHazard) ((BirdHazard) h).setSetKB(true);
            contactHazardFixtures.remove(bd1 instanceof HazardModel ? fix1 : fix2);
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
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            if (umbrella == bd2 || umbrella == bd1) {
                contact.setEnabled(false);
            }

            if (((umbrella == bd2 || avatar == bd2) && (bd1 instanceof HazardModel && !(bd1 instanceof StaticHazard)) ||
                    ((umbrella == bd1 || avatar == bd1) && (bd2 instanceof HazardModel && !(bd2 instanceof StaticHazard))))) {
                contact.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the gameplay controller is paused.
     * <p>
     * We need this method to stop all sounds when we pause.
     * Pausing happens when we switch game modes.
     */
    public void pauseSFX() {
        windStrongSFX.pause();
        lightningSFX.pause();
        birdAlertSFX.pause();
        birdFlapSFX.pause();
        walkingSFX.pause();
    }

    public void stopSFX(){
        windStrongSFX.stop();
        lightningSFX.stop();
        birdAlertSFX.stop();
        birdFlapSFX.stop();
        walkingSFX.stop();
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
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        // deactivate physics from remaining live objects
        for (Obstacle obj : levelContainer.getObjects()) {
            obj.deactivatePhysics(world);
        }
        // empty out of level container and world
        levelContainer.dispose();
        world.dispose();

        levelContainer = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        dragScale = null;
    }

    /**
     * Gets all the objects in objects
     */
    public PooledList<Obstacle> getObjects() {
        return levelContainer.getObjects();
    }

    public PooledList<Drawable> getDrawables() {
        return levelContainer.getDrawables();
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
        levelContainer.setScale(scale);
    }

    /**
     * @return game object container
     */
    public LevelContainer getLevelContainer() {
        return levelContainer;
    }

//    private final int TEARING_VEL = 6;

    /**
     * @return player x-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenX() {
        float out = avatar != null ? avatar.getDrawScale().x * avatar.getX() : -1;
        //only round to adjust for tearing if velocity is high - otherwise, you can
        // get some vibration-like effects on Gale for what should be smooth movement
//        return avatar.getLinearVelocity().len() > TEARING_VEL ? Math.round(out) : out;
        return out;
    }

    /**
     * @return player y-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenY() {
        float out = avatar != null ? avatar.getDrawScale().y * avatar.getY() : -1;
        //only round to adjust for tearing if velocity is high - otherwise, you can
        // get some vibration-like effects on Gale for what should be smooth movement
//        return avatar.getLinearVelocity().len() > TEARING_VEL ? Math.round(out) : out;
        return out;
    }

    /**
     * @return reference to player model
     */
    public PlayerModel getPlayer() {
        return avatar;
    }


    /**
     * set world bounds to be the given rectangle dimensions.
     * This should be followed with a reset of the game.
     *
     * @param rect the bounds
     */
    public void setBounds(Rectangle rect) {
        this.bounds.set(rect.x, rect.y, rect.getWidth(), rect.getHeight());
    }


    /**
     * @return whether player finished level
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @return whether player failed
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * set player level status to completed
     */
    private void setCompleted() {
        completed = true;
    }

    /**
     * set player level status to failed
     */
    private void setFailed() {
        failed = true;
    }

    /**
     * Sets SFX Volume
     */
    public void setSFXVolume(float sfxVolume) {
        this.SFXVolume = sfxVolume;
    }

    /**
     * @return whether avatar can zoom out
     */
    public boolean canAvatarZoom() {
        return avatar.isGrounded() && !avatar.isMoving() && avatar.getLinearVelocity().epsilonEquals(0,0);
    }
}
