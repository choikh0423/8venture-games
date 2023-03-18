package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.mygdx.game.assets.AssetDirectory;
import com.mygdx.game.hazard.BirdHazard;
import com.mygdx.game.hazard.LightningHazard;
import com.mygdx.game.obstacle.BoxObstacle;
import com.mygdx.game.obstacle.Obstacle;
import com.mygdx.game.obstacle.PolygonObstacle;
import com.mygdx.game.util.PooledList;
import org.w3c.dom.css.Rect;

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
    protected PooledList<Obstacle> objects = new PooledList<Obstacle>();
    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
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
    /**
     * The set of all birds currently in the level
     */
    private ObjectSet<BirdHazard> birds = new ObjectSet<>();

    /**
     * The set of all lightning currently in the level
     */
    private ObjectSet<LightningHazard> lightnings = new ObjectSet<>();



    /**
     * The texture for walls and platforms
     */
    protected TextureRegion platformTile;
    /**
     * Texture asset for character avatar
     */
    private TextureRegion avatarTexture;
    /**
     * Texture asset for the wind gust
     */
    private TextureRegion windTexture;
    /**
     * Texture asset for umbrella
     */
    private TextureRegion umbrellaTexture;
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
    //font for writing player health. temporary solution until a proper health asset is added
    private BitmapFont avatarHealthFont;


    // Physics objects for the game
    /**
     * Physics constants for current level
     */
    private JsonValue levelConstants;
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
     * Currently selected level
     */
    private int currentLevel = 0;


    /**
     * Creates and initialize a new instance of Level Container
     * <p>
     * The game has default gravity and other settings
     */
    public LevelContainer(World world, Rectangle bounds, Vector2 scale, int level) {
        this.currentLevel = level;

        this.world = world;
        this.bounds = bounds;
        this.scale = scale;

        sensorFixtures = new ObjectSet<Fixture>();
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
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
        // Setting up Constant/Asset Path for different levels
        String constantPath = "level" + this.currentLevel + ":constants";
        String assetPath = "level" + this.currentLevel + ":assets";

        levelConstants = directory.getEntry(constantPath, JsonValue.class);
        // Level Assets to be implemented
        // levelAssets = directory.getEntry(assetPath, JsonValue.class);
        platformTile = new TextureRegion(directory.getEntry("placeholder:newplatform", Texture.class));
        avatarTexture = new TextureRegion(directory.getEntry("placeholder:player", Texture.class));
        umbrellaTexture = new TextureRegion(directory.getEntry("placeholder:umbrella", Texture.class));
        windTexture = new TextureRegion(directory.getEntry("placeholder:wind", Texture.class));
        birdTexture = new TextureRegion(directory.getEntry("placeholder:bird", Texture.class));
        lightningTexture = new TextureRegion(directory.getEntry("placeholder:bird", Texture.class));
        goalTexture = new TextureRegion(directory.getEntry("placeholder:goal", Texture.class));

        avatarHealthFont = directory.getEntry("shared:retro", BitmapFont.class);
        // NO (at least in this context, there is no gain in doing so because the cache font is accessible from all classes that load this entry.
        // avatarHealthFont.setColor(Color.RED);
    }
    /**
     * Resets the level container
     */
    public void reset() {
        objects.clear();
        addQueue.clear();

    }

    /**
     * Lays out the game geography.
     */
    public void populateLevel() {
        // Add level goal
        JsonValue goal = levelConstants.get("goal");
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
        JsonValue defaults = levelConstants.get("defaults");
        world.setGravity(new Vector2(0, defaults.getFloat("gravity", DEFAULT_GRAVITY)));

        //TODO: explicit walls do not exist, consider deleting.
        // ============================================================================
        String wname = "wall";
        JsonValue walljv = levelConstants.get("walls");
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
        JsonValue platjv = levelConstants.get("platforms");
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
        float scl = levelConstants.get("player").getFloat("texturescale");
        dwidth = avatarTexture.getRegionWidth() / scale.x * scl;
        dheight = avatarTexture.getRegionHeight() / scale.y * scl;
        avatar = new PlayerModel(levelConstants.get("player"), dwidth, dheight, levelConstants.get("player").getInt("maxhealth"));
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        avatar.setHpTexture(avatarTexture);
        avatar.healthFont = avatarHealthFont;
        addObject(avatar);
        scl = levelConstants.get("umbrella").getFloat("texturescale");
        dwidth = umbrellaTexture.getRegionWidth() / scale.x * scl;
        dheight = umbrellaTexture.getRegionHeight() / scale.y * scl;
        umbrella = new UmbrellaModel(levelConstants.get("umbrella"), dwidth, dheight);
        umbrella.setDrawScale(scale);
        umbrella.setTexture(umbrellaTexture);
        umbrella.setClosedMomentum(levelConstants.get("umbrella").getFloat("closedmomentum"));
        umbrella.setPosition(levelConstants.get("umbrella").get("pos").getFloat(0), levelConstants.get("umbrella").get("pos").getFloat(1));
        addObject(umbrella);


        // Create wind gusts
        String windName = "wind";
        JsonValue windjv = levelConstants.get("wind");
        for (int ii = 0; ii < windjv.size; ii++) {
            WindModel obj;
            obj = new WindModel(windjv.get(ii));
            obj.setDrawScale(scale);
            obj.setTexture(windTexture);
            obj.setName(windName + ii);
            addObject(obj);
        }

        //create hazards
        JsonValue hazardsjv = levelConstants.get("hazards");

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
            lightnings.add(obj);
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
     */
    public void dispose() {
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
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
    public ObjectSet<BirdHazard> getBirds() {
        return birds;
    }
    /**
     * Get lightnings
     * @return lightnings
     */
    public ObjectSet<LightningHazard> getLightenings() {
        return lightnings;
    }

    /**
     * Set world
     * @return world
     */
    public void setWorld(World worldObj) { world = worldObj; }
    /**
     * Set player object
     * @return avatar
     */
    public void setAvatar(PlayerModel avatarObj) {
        avatar = avatarObj;
    }
    /**
     * Set umbrella object
     * @return umbrella
     */
    public void setUmbrella(UmbrellaModel umbrellaObj) {
        umbrella = umbrellaObj;
    }
    /**
     * Set objects
     * @return objects
     */
    public void setObjects(PooledList<Obstacle> allObjects) {
        objects = allObjects;
    }
    /**
     * Set birds
     * @return birds
     */
    public void setBirds(ObjectSet<BirdHazard> birdsObj) {
        birds = birdsObj;
    }
    /**
     * Set lightnings
     * @return lightnings
     */
    public void setLightnings(ObjectSet<LightningHazard> lightningsObj) {
        lightnings = lightningsObj;
    }

}
