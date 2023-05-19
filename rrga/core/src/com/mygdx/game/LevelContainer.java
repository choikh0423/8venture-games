package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.mygdx.game.model.*;
import com.mygdx.game.model.hazard.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;
import com.mygdx.game.utility.util.PooledList;
import com.mygdx.game.utility.util.Sticker;
import com.mygdx.game.utility.util.TiledLayer;

import java.util.*;

public class LevelContainer{
    /**
     * The default value of gravity (going down)
     */
    protected static final float DEFAULT_GRAVITY = -4.9f;

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
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects;
    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue;

    /** A sorted list of drawbles by depth. */
    protected PooledList<Drawable> drawables;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * The set of all birds currently in the level
     */
    private PooledList<BirdHazard> birds;

    /**
     * The set of all winds currently in the level
     */
    private ObjectSet<NewWindModel> winds;


    /**
     * The set of all moving platforms currently in the level
     */
    private final ObjectSet<MovingPlatformModel> movingPlats;

    /**
     * The set of all nests currently in the level
     */
    private ObjectSet<NestHazard> nests;


    /**
     * The texture for walls and platforms
     */
    protected TextureRegion platformTile;

    /**
     * The textures for movable cloud platforms
     */
    private TextureRegion[] cloudPlatformTextures;

    /**
     * The textures for animated lightning
     */
    private Texture[] animatedLightningTextures;

    private HashMap<String, TextureRegion> logTextures;

    /**
     * Texture asset for character front avatar
     */
    private TextureRegion avatarFrontTexture;
    /**
     * Texture asset for character side avatar
     */
    private TextureRegion avatarSideTexture;
    /**
     * Texture asset for character idle animation
     */
    private Texture avatarIdleAnimationTexture;
    /**
     * Texture asset for character look animation
     */
    private Texture avatarLookAnimationTexture;
    /**
     * Texture asset for character takeoff animation
     */
    private Texture avatarTakeoffAnimationTexture;
    /**
     * Texture asset for character land animation
     */
    private Texture avatarLandAnimationTexture;
    /**
     * Texture asset for character flip animation
     */
    private Texture avatarFlipAnimationTexture;
    /**
     * Texture asset for the wind gust
     */
    private TextureRegion windTexture;
    /**
     * Texture assets for the wind animation
     */
    private TextureRegion[] windAnimation = new TextureRegion[18];

    /**
     * Texture assets for the wind animation
     */
    private Texture particleWindAnimation1;

    /**
     * Texture assets for the wind animation
     */
    private Texture particleWindAnimation2;

    /**
     * Texture assets for the wind animation
     */
    private Texture particleWindAnimation3;

    /**
     * Texture assets for the wind animation
     */
    private Texture[] particleWindAnimationList;

    /**
     * Texture assets for the wind animation
     */
    private Texture particleLeafAnimation1;

    /**
     * Texture assets for the wind animation
     */
    private Texture particleLeafAnimation2;

    /**
     * Texture assets for the wind animation
     */
    private Texture particleLeafAnimation3;

    /**
     * Texture assets for the wind animation
     */
    private Texture[] particleLeafAnimationList;
    /**
     * Texture asset for opened umbrella
     */
    private TextureRegion umbrellaOpenTexture;

    /** Texture asset for closed umbrella */
    private TextureRegion umbrellaClosedTexture;

    /** Texture asset for red bird animation */
    private Texture redBirdAnimationTexture;

    /** Texture asset for blue bird animation */
    private Texture blueBirdAnimationTexture;

    /** Texture asset for green bird animation */
    private Texture greenBirdAnimationTexture;

    /** Texture asset for brown bird animation */

    private Texture brownBirdAnimationTexture;

    /** Texture asset for goal */
    private TextureRegion goalTexture;

    /**
     * Texture asset for hp
     */
    private Texture hpTexture;
    /**
     * Texture asset for boost timer
     */
    private Texture boostTexture;

    /**
     * Texture for directional indicator to scarf
     */
    private TextureRegion indicatorTexture;
    /**
     * Fill Texture asset for lightning
     */
    private TextureRegion fillLightningTexture;

    /**
     * Fill Texture asset for brambles
     */
    private TextureRegion fillBrambleTexture;

    /**
     * Texture asset for rocks
     */
    private TextureRegion rockTexture;


    /**
     * Texture asset for nests
     */
    private TextureRegion nestTexture;

    // Start of animation texture
    /**
     * Texture asset for avatar walking animation
     */
    private Texture avatarWalkAnimationTexture;
    /**
     * Texture asset for avatar falling animation
     */
    private Texture avatarFallingAnimationTexture;
    /**
     * Texture asset for umbrella open animation
     */
    private Texture umbrellaOpenAnimationTexture;
    /**
     * Texture asset for umbrella open animation when depleted
     */
    private Texture umbrellaOpenEmptyAnimationTexture;

    /**
     * Texture asset for umbrella boost animation
     */
    private Texture umbrellaBoostAnimationTexture;
    /**

     * Texture asset for a bird warning
     */
    private Texture warningTexture;

    /**
     * Texture asset for goal animation
     */
    private Texture goalAnimationTexture;
    /**
     * Texture asset for wind animation
     */
    private Texture windAnimationTexture;

    //font for writing player health. temporary solution until a proper health asset is added
    private BitmapFont avatarHealthFont;


    /** Global Physics constants */
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
    private GoalDoor goalDoor;

    /** reference to the JSON parser */
    private LevelParser parser;


    /**
     * Creates and initialize a new instance of Level Container
     * <p>
     * The game has default gravity and other settings
     */
    public LevelContainer(World world, Rectangle bounds, Vector2 scale) {
        this.world = world;
        this.bounds = bounds;
        this.scale = scale;

        sensorFixtures = new ObjectSet<Fixture>();
        birds = new PooledList<>();
        winds = new ObjectSet<>();
        movingPlats = new ObjectSet<>();
        nests = new ObjectSet<>();

        objects = new PooledList<Obstacle>();
        drawables = new PooledList<Drawable>();
        addQueue = new PooledList<Obstacle>();
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    /**
     * Note: Null texture is returned when color is invalid.
     * @param color the color of the bird
     * @return texture of bird for the given value color.
     */
    private Texture getFlapAnimationTexture(BirdHazard.BirdColor color){
        switch(color){
            case RED: return redBirdAnimationTexture;
            case BLUE: return blueBirdAnimationTexture;
            case GREEN: return greenBirdAnimationTexture;
            case BROWN: return brownBirdAnimationTexture;
            default: return null;
        }
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
        globalConstants = directory.getEntry( "global:constants", JsonValue.class);

        // Player Component Textures
        platformTile = new TextureRegion(directory.getEntry("game:newplatform", Texture.class));
        avatarSideTexture = new TextureRegion(directory.getEntry("game:player", Texture.class));
        avatarFrontTexture = new TextureRegion(directory.getEntry("game:front", Texture.class));
        umbrellaOpenTexture = new TextureRegion(directory.getEntry("game:umbrella", Texture.class));
        umbrellaClosedTexture = new TextureRegion(directory.getEntry("game:closed", Texture.class));
        windTexture = new TextureRegion(directory.getEntry("game:wind", Texture.class));
        goalTexture = new TextureRegion(directory.getEntry("game:goal", Texture.class));
        hpTexture = directory.getEntry("game:hp_indicator", Texture.class);
        boostTexture = directory.getEntry("game:boost", Texture.class);
        indicatorTexture = new TextureRegion(directory.getEntry("game:player_indicator", Texture.class));

        // Hazard Textures
        redBirdAnimationTexture = directory.getEntry("game:red_bird_flapping", Texture.class);
        blueBirdAnimationTexture = directory.getEntry("game:blue_bird_flapping", Texture.class);
        greenBirdAnimationTexture = directory.getEntry("game:green_bird_flapping", Texture.class);
        brownBirdAnimationTexture = directory.getEntry("game:brown_bird_flapping", Texture.class);
        
        warningTexture = directory.getEntry("game:bird_warning", Texture.class);
        nestTexture = new TextureRegion(directory.getEntry("game:nest", Texture.class));

        fillLightningTexture = new TextureRegion(directory.getEntry("game:lightning", Texture.class));
        fillBrambleTexture = new TextureRegion(directory.getEntry("game:brambles_fill", Texture.class));
        rockTexture = new TextureRegion(directory.getEntry("game:rock", Texture.class));

        // Animation Textures
        avatarWalkAnimationTexture = directory.getEntry("game:player_walk_animation", Texture.class);
        avatarFallingAnimationTexture = directory.getEntry("game:player_falling_animation", Texture.class);
        umbrellaOpenAnimationTexture = directory.getEntry("game:umbrella_open_animation", Texture.class);
        umbrellaOpenEmptyAnimationTexture = directory.getEntry("game:umbrella_open_empty_animation", Texture.class);
        umbrellaBoostAnimationTexture =  directory.getEntry("game:umbrella_dodge_animation", Texture.class);
        goalAnimationTexture = directory.getEntry("game:goal_animation", Texture.class);
        for(int i = 0; i < 18; i++){
            windAnimation[i] = new TextureRegion(directory.getEntry("game:wind_frame"+i, Texture.class));
        }
        particleWindAnimation1 = directory.getEntry("game:wind_particle_filmstrip1", Texture.class);
        particleWindAnimation2 = directory.getEntry("game:wind_particle_filmstrip2", Texture.class);
        particleWindAnimation3 = directory.getEntry("game:wind_particle_filmstrip3", Texture.class);

        particleWindAnimationList = new Texture[] {
                particleWindAnimation1,
                particleWindAnimation2,
                particleWindAnimation3
        };

        particleLeafAnimation1 = directory.getEntry("game:leaf_particle_filmstrip1", Texture.class);
        particleLeafAnimation2 = directory.getEntry("game:leaf_particle_filmstrip2", Texture.class);
        particleLeafAnimation3 = directory.getEntry("game:leaf_particle_filmstrip3", Texture.class);

        particleLeafAnimationList = new Texture[] {
                particleLeafAnimation1,
                particleLeafAnimation2,
                particleLeafAnimation3
        };
        avatarIdleAnimationTexture = directory.getEntry("game:player_idle_animation", Texture.class);
        avatarLookAnimationTexture = directory.getEntry("game:player_look_animation", Texture.class);
        avatarTakeoffAnimationTexture = directory.getEntry("game:player_takeoff_animation", Texture.class);
        avatarLandAnimationTexture = directory.getEntry("game:player_land_animation", Texture.class);
        avatarFlipAnimationTexture = directory.getEntry("game:player_flip_animation", Texture.class);

        // Fonts
        avatarHealthFont = directory.getEntry("shared:retro", BitmapFont.class);

        // Movable Platforms (clouds)
        cloudPlatformTextures = new TextureRegion[]{
                new TextureRegion(directory.getEntry("game:cloud0", Texture.class)),
                new TextureRegion(directory.getEntry("game:cloud1", Texture.class)),
                new TextureRegion(directory.getEntry("game:cloud2", Texture.class)),
                new TextureRegion(directory.getEntry("game:cloud3", Texture.class))
        };

        // animated lightning
        animatedLightningTextures = new Texture[]{
                directory.getEntry("game:lightning0", Texture.class),
                directory.getEntry("game:lightning1", Texture.class),
                directory.getEntry("game:lightning2", Texture.class),
                directory.getEntry("game:lightning3", Texture.class),
                directory.getEntry("game:lightning4", Texture.class)
        };

        // load all branch/log textures by name (this is better approach than hard coding all textures)
        logTextures = new HashMap<>();
        for (String fileName : globalConstants.get("textures").get("tree_logs").asStringArray()){
            logTextures.put(fileName, new TextureRegion(directory.getEntry("game:" + fileName, Texture.class)));
        }

    }
    /**
     * Resets the level container (emptying the container)
     */
    public void reset() {
        objects.clear();
        addQueue.clear();
        birds.clear();
        movingPlats.clear();
        nests.clear();
        drawables.clear();
        winds.clear();
    }

    private MovingPlatformModel showGoal;
    public MovingPlatformModel getShowGoal(){return showGoal;}

    /**
     * Lays out the game geography.
     */
    public void populateLevel() {
        // Add level goal
        JsonValue goalconst = globalConstants.get("goal");

        Vector2 goalPos = parser.getGoalPos();
        float dwidth = goalconst.getFloat("width");
        float dheight = goalconst.getFloat("height");
        goalDoor = new GoalDoor(goalconst, goalPos.x, goalPos.y,dwidth, dheight, parser.getGoalDrawDepth());
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTexture);
        goalDoor.setAnimation(goalAnimationTexture);
        addObject(goalDoor);
        drawables.add(goalDoor);

        // Setting Gravity on World
        JsonValue defaults = globalConstants.get("defaults");
        world.setGravity(new Vector2(0, defaults.getFloat("gravity", DEFAULT_GRAVITY)));

        JsonValue[] plats = parser.getPlatformData();
        for (int ii = 0; ii < plats.length; ii++) {
            JsonValue cur = plats[ii];
            PlatformModel obj;
            if (cur.getBoolean("textured")){
                // this platform has an asset (branch, log, etc)
                obj = new PlatformModel(cur, logTextures.get(cur.getString("texture")), cur.getInt("depth"));
            }
            else {
                // this platform is an invisible object
                obj = new PlatformModel(cur.getFloat("x"), cur.getFloat("y"), cur.get("points").asFloatArray(),
                        cur.getInt("depth"));
            }
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setName("platform" + ii);
            addObject(obj);
            drawables.add(obj);
        }

        JsonValue[] mPlats = parser.getMovingPlatformData();
        for (int ii = 0; ii < mPlats.length; ii++) {
            JsonValue cur = mPlats[ii];
            MovingPlatformModel obj = new MovingPlatformModel( cur, cur.get("points").asFloatArray(),
                    cur.getFloat("x"), cur.getFloat("y")
            );
            obj.setBodyType(BodyDef.BodyType.KinematicBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(cloudPlatformTextures[cur.getInt("tileIndex")]);
            obj.setName("moving_platform" + ii);
            addObject(obj);
            drawables.add(obj);
            movingPlats.add(obj);
        }

        // Create wind gusts
        String windName = "wind";
        JsonValue[] windjv = parser.getWindData();
        for (int ii = 0; ii < windjv.length; ii++) {
            NewWindModel obj;
            obj = new NewWindModel(windjv[ii], scale);
            obj.setDrawScale(scale);
            obj.setTexture(windTexture);
            obj.setAnimation(windAnimation);
            for (int i = 0; i < obj.getNumParticles(); i++) {
                // Populates particle with 2/3 winds, 1/3 leaf
                int particleNum = (i % 3);
                if (particleNum < 2) {
                    obj.setParticleAnimation(particleWindAnimationList, i);
                } else {
                    obj.setParticleAnimation(particleLeafAnimationList, i);
                }
            }
            obj.setName(windName + ii);
            addObject(obj);
            drawables.add(obj);
            winds.add(obj);
        }



        JsonValue hazardsjv = globalConstants.get("hazards");

        //create invisible/bramble/rock hazards
        JsonValue[] hazardData = parser.getStaticHazardData();
        int staticDmg = hazardsjv.getInt("staticHazardDamage");
        float staticKnockBack = hazardsjv.getFloat("staticHazardKnockBack");
        for(int ii = 0; ii < hazardData.length; ii++){
            PolygonObstacle obj;
            JsonValue jv = hazardData[ii];
            String type = jv.getString("type");
            if (type.equals("rock")){
                obj = new RockHazard(jv, staticDmg, staticKnockBack);
                obj.setTexture(rockTexture);
            }
            else {
                obj = new StaticHazard(jv, staticDmg, staticKnockBack);
                if (type.equals("fill")){
                    obj.setTexture(fillBrambleTexture);
                }
            }
            obj.setDrawScale(scale);
            obj.setName("static_hazard"+ii);
            addObject(obj);
            drawables.add((Drawable) obj);
        }

        // create death zone (using static hazard with 0 knockback)
        JsonValue[] deathZones = parser.getDeathZoneData();
        for(int ii = 0; ii < deathZones.length; ii++){
            JsonValue jv = deathZones[ii];
            StaticHazard obj = new StaticHazard(jv, globalConstants.get("player").getInt("maxhealth"), 0);
            obj.setName("death_zone"+ii);
            obj.setDrawScale(scale);
            obj.setSensor(true);
            addObject(obj);
        }

        //create birds
        JsonValue[] birdData = parser.getBirdData();
        int birdDamage = hazardsjv.getInt("birdDamage");
        int birdSensorRadius = hazardsjv.getInt("birdSensorRadius");
        float birdKnockBack = hazardsjv.getInt("birdKnockBack");
        // indices for each bird type indicating the preferred still frame.
        int[] indices = hazardsjv.get("birdStillFrames").asIntArray();
        int birdCount = 0;
        for (int ii = 0; ii < birdData.length; ii++) {
            BirdHazard obj;
            JsonValue jv = birdData[ii];
            obj = new BirdHazard(jv, birdDamage, birdSensorRadius, birdKnockBack);
            obj.setDrawScale(scale);
            obj.setFlapAnimation(getFlapAnimationTexture(obj.getColor()), indices[obj.getColor().ordinal()]);
            obj.setWarningAnimation(warningTexture);
            obj.setName("bird" + ii);
            addObject(obj);
            birds.add(obj);
            drawables.add(obj);
            birdCount++;
        }

        //create nests and their bird
        String nestName = "nest";
        JsonValue[] nestData = parser.getNestData();
        for(int ii = 0; ii<nestData.length; ii++){
            NestHazard nest = new NestHazard(nestData[ii], parser.getBlueBirdData());
            nest.setDrawScale(scale);
            nest.setTexture(nestTexture);
            nest.setName("nest" + ii);
            addObject(nest);
            NestedBirdHazard bird = new NestedBirdHazard(nest, birdDamage, birdSensorRadius, birdKnockBack);
            bird.setDrawScale(scale);
            bird.setFlapAnimation(getFlapAnimationTexture(BirdHazard.BirdColor.BLUE), indices[BirdHazard.BirdColor.BLUE.ordinal()]);
            // bird.setWarningAnimation(warningTexture);
            bird.setName("bird" + (birdCount + ii));
            addObject(bird);
            bird.setSpawning();
            birds.add(bird);
            // nests.add(obj);
            drawables.add(nest);
            drawables.add(bird);
        }

        //create lightning (animated lightning bolts and still-frame lightning bolts)
        String lightningName = "lightning";
        JsonValue[] lightningData = parser.getLightningData();
        int lightningDmg = hazardsjv.getInt("lightningDamage");
        float lightningKnockBackScl = hazardsjv.getFloat("lightningKnockBack");
        for (int ii = 0; ii < lightningData.length; ii++) {
            Obstacle obj;
            JsonValue data = lightningData[ii];
            if (data.getBoolean("fill_texture")){
                LightningHazard lightning = new LightningHazard(data, lightningDmg, lightningKnockBackScl);
                lightning.setTexture(fillLightningTexture);
                obj = lightning;
            }
            else {
                obj = new AnimatedLightningHazard(data, animatedLightningTextures[data.getInt("tileIndex")],
                        lightningDmg, lightningKnockBackScl);
            }
            obj.setDrawScale(scale);
            obj.setName(lightningName + ii);
            addObject(obj);
            drawables.add((Drawable) obj);
        }

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

        // Create player
        dwidth = globalConstants.get("player").get("size").getFloat(0);
        dheight = globalConstants.get("player").get("size").getFloat(1);
        avatar = new PlayerModel(globalConstants.get("player"), parser.getPlayerPos(),
                dwidth, dheight, globalConstants.get("player").getInt("maxhealth"), parser.getPlayerDrawDepth());
        avatar.setDrawScale(scale);
        avatar.setFrontTexture(avatarFrontTexture);
        avatar.setSideTexture(avatarSideTexture);
        avatar.useSideTexture();
        avatar.setHpTexture(hpTexture);
        avatar.setBoostTexture(boostTexture);
        avatar.setWalkAnimation(avatarWalkAnimationTexture);
        avatar.setFallingAnimation(avatarFallingAnimationTexture);
        avatar.setIdleAnimation(avatarIdleAnimationTexture);
        avatar.setLookAnimation(avatarLookAnimationTexture);
        avatar.setTakeoffAnimation(avatarTakeoffAnimationTexture);
        avatar.setLandAnimation(avatarLandAnimationTexture);
        avatar.setFlipAnimation(avatarFlipAnimationTexture);
        avatar.setIndicatorTexture(indicatorTexture);

        avatar.healthFont = avatarHealthFont;
        addObject(avatar);
        drawables.add(avatar);

        //initialize the invisible object the camera follows to move from goal
        //to player when first entering level
        JsonValue showGoalData = new JsonValue(JsonValue.ValueType.object);
        showGoalData.addChild("movespeed", new JsonValue(5f));
        showGoalData.addChild("flipped", new JsonValue(false));
        showGoalData.addChild("depth", new JsonValue(0));
        showGoalData.addChild("path", new JsonValue(JsonValue.ValueType.array));
        showGoalData.get("path").addChild(new JsonValue(goalDoor.getX()));
        showGoalData.get("path").addChild(new JsonValue(goalDoor.getY()));
        showGoalData.get("path").addChild(new JsonValue(avatar.getX()));
        showGoalData.get("path").addChild(new JsonValue(avatar.getY()));
        showGoalData.addChild("AABB", new JsonValue(JsonValue.ValueType.array));
        showGoalData.get("AABB").addChild(new JsonValue(-0.1f));
        showGoalData.get("AABB").addChild(new JsonValue(0.1f));
        showGoalData.get("AABB").addChild(new JsonValue(0.2f));
        showGoalData.get("AABB").addChild(new JsonValue(0.2f));
        float[] p = {-0.1f,-0.1f,0.1f,-0.1f,-0.1f,0.1f,0.1f,0.1f};
        showGoal = new MovingPlatformModel(showGoalData, p, goalDoor.getX(), goalDoor.getY());
        showGoal.setSensor(true);
        showGoal.setName("show goal");
        addObject(showGoal);

        // Create the umbrella
        dwidth = globalConstants.get("umbrella").get("size").getFloat(0);
        dheight = globalConstants.get("umbrella").get("size").getFloat(1);
        umbrella = new UmbrellaModel(
                globalConstants.get("umbrella"),
                new Vector2(parser.getPlayerPos().x, parser.getPlayerPos().y), dwidth, dheight
        );
        umbrella.setDrawScale(scale);
        umbrella.setOpenTexture(umbrellaOpenTexture);
        umbrella.setClosedTexture(umbrellaClosedTexture);
        umbrella.useClosedTexture();
        umbrella.setOpenAnimation(umbrellaOpenAnimationTexture, umbrellaOpenEmptyAnimationTexture);
        umbrella.setBoostAnimation(umbrellaBoostAnimationTexture);
        umbrella.setClosedMomentumX(globalConstants.get("umbrella").getFloat("closedmomentumX"));
        umbrella.setClosedMomentumY(globalConstants.get("umbrella").getFloat("closedmomentumY"));
        addObject(umbrella);
        // drawables.add(umbrella); unnecessary because player+umbrella always drawn together.

        // Include Stickers + Tiled Layers and Sort all drawables
        for (Sticker s : parser.getStickers()){
            s.setDrawScale(scale);
            drawables.add(s);
        }
        for (TiledLayer t : parser.getLayers()){
            t.setDrawScale(scale);
            drawables.add(t);
        }
        Collections.sort(drawables, Collections.reverseOrder(new Comparator<Drawable>(){
            @Override
            public int compare(Drawable o1, Drawable o2) { return o1.getDepth() - o2.getDepth();
            }
        }));

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
     * Dispose of all (non-static) resources allocated to this container
     *
     * empties all game objects.
     */
    public void dispose() {
        objects.clear();
        addQueue.clear();
        birds.clear();
        nests.clear();

        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        birds = null;
        nests = null;
    }
    /**
     * Get world object
     * @return world
     */
    public World getWorld() {
        return world;
    }
    /**
     * Get player object
     * @return avatar
     */
    public PlayerModel getAvatar() {
        return avatar;
    }
    /**
     * Get umbrella object
     * @return umbrella
     */
    public UmbrellaModel getUmbrella() {
        return umbrella;
    }
    /**
     * Get goalDoor object
     * @return goalDoor
     */
    public BoxObstacle getGoalDoor() {
        return goalDoor;
    }
    /**
     * Get objects
     * @return objects
     */
    public PooledList<Obstacle> getObjects() {
        return objects;
    }
    /**
     * Get birds
     * @return birds
     */
    public PooledList<BirdHazard> getBirds() {
        return birds;
    }
    /**
     * Get winds
     * @return winds
     */
    public ObjectSet<NewWindModel> getWinds() {
        return winds;
    }

    /**
     * Get nests
     * @return nests
     */
    public ObjectSet<NestHazard> getNests() {
        return nests;
    }
    /**
     * Get moving platforms
     * @return movingPlats
     */
    public ObjectSet<MovingPlatformModel> getMovingPlats(){return movingPlats;}


    /**
     * @return sorted list of drawables to be (possibly) drawn to game.
     */
    public PooledList<Drawable> getDrawables() { return drawables;}


    public void setParser(LevelParser parser) { this.parser = parser; }

    /**
     * Set world
     */
    public void setWorld(World worldObj) { world = worldObj; }
    /**
     * Set player object
     */
    public void setAvatar(PlayerModel avatarObj) {
        avatar = avatarObj;
    }
    /**
     * Set umbrella object
     */
    public void setUmbrella(UmbrellaModel umbrellaObj) {
        umbrella = umbrellaObj;
    }
    /**
     * Set objects
     */
    public void setObjects(PooledList<Obstacle> allObjects) {
        objects = allObjects;
    }
    /**
     * Set birds
     */
    public void setBirds(PooledList<BirdHazard> birdsObj) {
        birds = birdsObj;
    }

}
