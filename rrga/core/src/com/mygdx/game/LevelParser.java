package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class LevelParser {

    /** the Tiled-JSON raw value that was previously parsed.
     * This saves parsing time when a level is played immediately again.
     */
    private JsonValue prevParsed;

    /** list of bird json data.
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] birdData;

    /** list of lightning json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] lightningData;

    /** list of platform json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] platformData;

    /** list of static hazard json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] staticHazardData;

    /** list of wind json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] windData;

    /** list of moving platform json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] movingPlatformData;

    /** list of nest json data
     * Invariant: JSON is in the format used by level-container
     */
    private JsonValue[] nestData;

    /** the texture data of the tile layers
     * Invariant: front layers are stored last in list
     */
    private final ArrayList<TiledLayer> layers = new ArrayList<>();

    /** the list of Sticker objects */
    private final ArrayList<Sticker> stickers = new ArrayList<>();

    /** vector position cache for player */
    private final Vector2 playerPos = new Vector2();

    /** vector position cache for goal */
    private final Vector2 goalPos = new Vector2();

    /** vector cache for world size (width, height) */
    private final Vector2 worldSize = new Vector2();

    /** vector cache for tile scaling (x_scale, y_scale) */
    private final Vector2 tileScale = new Vector2();

    private final Vector2 temp = new Vector2();

    /** vector cache specifically for holding temporary scale factors */
    private final Vector2 scalars = new Vector2();

    /** template object with defaults for red birds*/
    private final JsonValue redBirdDefaultObj;

    /** template object with defaults for blue birds*/
    private final JsonValue blueBirdDefaultObj;

    /** template object with defaults for green birds*/
    private final JsonValue greenBirdDefaultObj;

    /** template object with defaults for brown birds*/
    private final JsonValue brownBirdDefaultObj;

    /** blue bird template data that is parsed */
    private JsonValue blueBirdData;

    /** the default JSON properties of path point. */
    private final JsonValue pointDefault;

    /** the default JSON properties of lightning object */
    private final JsonValue lightningDefault;

    /** the default JSON polygon of lightning object */
    private final JsonValue lightningDefaultPoly;

    /** the default JSON polygon of a platform */
    private final JsonValue platformDefaultPoly;

    /** the default JSON properties of bramble hazard */
    private final JsonValue staticHazardDefault;

    /** the default JSON polygon of a bramble hazard */
    private final JsonValue staticHazardPoly;

    /** the default JSON object of a rock hazard*/
    private final JsonValue rockDefaultObj;

    /** the default JSON properties of a wind object */
    private final JsonValue windDefault;

    /** the default JSON polygon of a wind object */
    private final JsonValue windDefaultPoly;

    /** the default JSONs of movable cloud objects */
    private final JsonValue[] cloudDefaultObjects;

    /** the default JSON object of a nest object */
    private final JsonValue nestDefault;

    /** the default JSONs of animated lightning objects */
    private final JsonValue[] lightningDefaultObjects;

    /** the default JSONs of branches/logs platforms*/
    private final HashMap<String, JsonValue> logDefaultObjects;

    /** the default direction of a wind object */
    private static final float windDirDefault = 0;

    /** maps from tileset name (bushes, cliffs, .etc) to its undivided texture */
    private final HashMap<String, Texture> tileSetTextureMap;

    /** maps from tileset name (bushes, cliffs, .etc) to its JSON data */
    private final HashMap<String, JsonValue> tileSetJsonMap;

    /** all objects in game that needs asset information can be found in an objects.json */
    private final JsonValue gameObjectTiles;

    /** map of (id -> tileset cutters / texture region cutters) where integer IDs are grid IDs */
    private final IntMap<ImageTileSetMaker> tileSetMakers = new IntMap<>();

    /** the texture producer for stickers */
    private CollectionTileSetMaker stickerMaker;

    /** The max id in a sticker set is not necessarily size() - 1 due to deletions.*/
    private int maxStickerSetId;

    /** (name -> texture data) map of all stickers as specified by sticker.json in levels/tilesets/ */
    private final HashMap<String, TextureInfo> stickerTextureInfoMap;

    /** (id -> name) map of all stickers as specified by sticker.json in levels/tilesets/ */
    private final IntMap<String> stickerNameMap;

    private static final int LOWER28BITMASK = 0xFFFFFFF;

    /** map used to track visited trajectory */
    private final IntIntMap seen = new IntIntMap(16);

    /** the depth of the current layer being parsed*/
    private int currentObjectDepth;

    /** drawing depth of Gale */
    private int playerDepth;

    /** drawing depth of scarf */
    private int goalDepth;
    private JsonValue[] deathZoneData;

    /**
     * @return tile texture layers
     */
    public ArrayList<TiledLayer> getLayers() {
        return layers;
    }

    /**
     * @return processed bird data that is ready for consumption
     */
    public JsonValue[] getBirdData() {
        return birdData;
    }

    public JsonValue getBlueBirdData(){
        return blueBirdData;
    }

    /**
     * @return processed nest data that is ready for consumption
     */
    public JsonValue[] getNestData(){
        return nestData;
    }

    /**
     * @return processed lightning data that is ready for consumption
     */
    public JsonValue[] getLightningData() {
        return lightningData;
    }

    /**
     * @return processed platform data that is ready for consumption
     */
    public JsonValue[] getPlatformData() {
        return platformData;
    }

    /**
     * @return processed moving platform data that is ready for consumption
     */
    public JsonValue[] getMovingPlatformData() {
        return movingPlatformData;
    }

    /**
     * @return processed static hazard data that is ready for consumption
     */
    public JsonValue[] getStaticHazardData() {
        return staticHazardData;
    }

    /**
     * @return processed wind data that is ready for consumption
     */
    public JsonValue[] getWindData(){
        return windData;
    }

    public JsonValue[] getDeathZoneData() { return deathZoneData; }

    public Vector2 getGoalPos() {
        return goalPos;
    }
    public Vector2 getPlayerPos() {
        return playerPos;
    }

    public Vector2 getWorldSize(){ return worldSize; }

    public ArrayList<Sticker> getStickers(){ return stickers; }

    public int getPlayerDrawDepth(){ return playerDepth; }

    public int getGoalDrawDepth(){ return goalDepth; }

    // containers for unprocessed JSON data
    HashMap<Integer, JsonValue> trajectory = new HashMap<>();
    ArrayList<JsonValue> birdRawData = new ArrayList<>();
    ArrayList<JsonValue> platformRawData = new ArrayList<>();
    ArrayList<JsonValue> lightningRawData = new ArrayList<>();
    ArrayList<JsonValue> windRawData = new ArrayList<>();
    HashMap<Integer, JsonValue> windDirs = new HashMap<>();
    ArrayList<JsonValue> staticHazardRawData = new ArrayList<>();
    ArrayList<JsonValue> movingPlatRawData = new ArrayList<>();
    ArrayList<JsonValue> nestRawData = new ArrayList<>();

    ArrayList<JsonValue> deathZoneRawData = new ArrayList<>();

    public LevelParser(AssetDirectory directory){
        JsonValue globalConstants = directory.getEntry("global:constants", JsonValue.class);

        JsonValue redBirdTemplate = directory.getEntry("red_bird:template", JsonValue.class);
        JsonValue blueBirdTemplate = directory.getEntry("blue_bird:template", JsonValue.class);
        JsonValue greenBirdTemplate = directory.getEntry("green_bird:template", JsonValue.class);
        JsonValue brownBirdTemplate = directory.getEntry("brown_bird:template", JsonValue.class);
        JsonValue pathPointTemplate = directory.getEntry("path_point:template", JsonValue.class);

        JsonValue platformTemplate = directory.getEntry("platform:template", JsonValue.class);
        JsonValue staticHazardTemplate = directory.getEntry("static_hazard:template", JsonValue.class);
        JsonValue rockHazardTemplate = directory.getEntry("rock:template", JsonValue.class);
        JsonValue windTemplate = directory.getEntry("wind:template", JsonValue.class);
        JsonValue nestTemplate = directory.getEntry("nest:template", JsonValue.class);

        // cloud templates
        JsonValue cloud0Template = directory.getEntry("cloud0:template", JsonValue.class);
        JsonValue cloud1Template = directory.getEntry("cloud1:template", JsonValue.class);
        JsonValue cloud2Template = directory.getEntry("cloud2:template", JsonValue.class);
        JsonValue cloud3Template = directory.getEntry("cloud3:template", JsonValue.class);

        // growing lightning templates
        JsonValue fillLightningTemplate = directory.getEntry("fill_lightning:template", JsonValue.class);
        JsonValue lightning0Template = directory.getEntry("lightning0:template", JsonValue.class);
        JsonValue lightning1Template = directory.getEntry("lightning1:template", JsonValue.class);
        JsonValue lightning2Template = directory.getEntry("lightning2:template", JsonValue.class);
        JsonValue lightning3Template = directory.getEntry("lightning3:template", JsonValue.class);
        JsonValue lightning4Template = directory.getEntry("lightning4:template", JsonValue.class);

        redBirdDefaultObj = redBirdTemplate.get("object");
        blueBirdDefaultObj = blueBirdTemplate.get("object");
        greenBirdDefaultObj = greenBirdTemplate.get("object");
        brownBirdDefaultObj = brownBirdTemplate.get("object");

        pointDefault = pathPointTemplate.get("object").get("properties");
        lightningDefault = fillLightningTemplate.get("object").get("properties");
        lightningDefaultPoly = fillLightningTemplate.get("object").get("polygon");
        platformDefaultPoly = platformTemplate.get("object").get("polygon");
        staticHazardDefault = staticHazardTemplate.get("object").get("properties");
        staticHazardPoly = staticHazardTemplate.get("object").get("polygon");
        rockDefaultObj = rockHazardTemplate.get("object");
        windDefault = windTemplate.get("object").get("properties");
        windDefaultPoly = windTemplate.get("object").get("polygon");

        nestDefault = nestTemplate.get("object");

        cloudDefaultObjects = new JsonValue[]{
                cloud0Template.get("object"),
                cloud1Template.get("object"),
                cloud2Template.get("object"),
                cloud3Template.get("object")
        };
        lightningDefaultObjects = new JsonValue[]{
                lightning0Template.get("object"),
                lightning1Template.get("object"),
                lightning2Template.get("object"),
                lightning3Template.get("object"),
                lightning4Template.get("object")
        };

        // map from branch_log#.json -> json data
        logDefaultObjects = new HashMap<>();
        for (String fileName : globalConstants.get("textures").get("tree_logs").asStringArray()){
            logDefaultObjects.put(fileName + ".json",
                    directory.getEntry("template:" + fileName, JsonValue.class).get("object"));
        }

        // save tileset textures and tileset JSON data
        tileSetTextureMap = new HashMap<>();
        tileSetJsonMap = new HashMap<>();
        String[] tileSetFileNames = globalConstants.get("textures").get("tilesets").asStringArray();
        for (String tileSetName : tileSetFileNames){
            tileSetTextureMap.put(tileSetName, directory.getEntry( "tileset:" + tileSetName, Texture.class));
            tileSetJsonMap.put(tileSetName, directory.getEntry("data:"+tileSetName, JsonValue.class));
        }

        // add object json
        gameObjectTiles = directory.getEntry("data:objects", JsonValue.class).get("tiles");

        // load all sticker textures (according to atlas file)
        stickerTextureInfoMap = new HashMap<>();
        JsonValue stickerData = directory.getEntry("data:stickers_atlas", JsonValue.class);
        for (JsonValue textureData : stickerData){
            String textureName = textureData.name;
            Texture texture = directory.getEntry("stickers:"+textureName, Texture.class);
            if (textureData.getBoolean("animated", false)){
                // asset is filmstrip
                stickerTextureInfoMap.put(textureName,
                        new TextureInfo(texture, textureData.getInt("rows"),
                                textureData.getInt("columns"), textureData.getFloat("frameDuration"))
                );
            }
            else if (!textureData.getBoolean("atlas", true)){
                // not an atlas, so asset is a single texture
                stickerTextureInfoMap.put(textureName, new TextureInfo(new TextureRegion(texture)));
            }
            else {
                // asset should be broken up according to atlas
                for (JsonValue region : textureData) {
                    String regionName = region.name;
                    int[] arr = region.asIntArray();
                    stickerTextureInfoMap.put(textureName + "_" + regionName,
                            new TextureInfo( new TextureRegion(texture, arr[0], arr[1], arr[2], arr[3]))
                    );
                }
            }
        }
        stickerNameMap = new IntMap<String>();
        JsonValue stickerJson = directory.getEntry("data:stickers", JsonValue.class);
        maxStickerSetId = 0;
        for (JsonValue stickerTile : stickerJson.get("tiles")){
            int id = stickerTile.getInt("id");
            maxStickerSetId = Math.max(maxStickerSetId, id);
            String[] sourcePath = stickerTile.getString("image").split("/");
            String sourceImageName = sourcePath[sourcePath.length - 1];
            // save (id, name) without extensions
            stickerNameMap.put(id, sourceImageName.split("\\.")[0]);
        }
    }

    /**
     * parses raw level data into user-friendly JSON data.
     * @param levelData raw Tiled-formatted JSON
     */
    public void parseLevel(JsonValue levelData){
        // TODO (consideration): depending on how much memory we can use, it's probably possible to keep all parsed data in
        //  memory rather than keeping only one copy at any moment (assume Garbage-collected).
        assert levelData != null;
        // no parsing needed if data in memory
        if (prevParsed == levelData){ return; }
        prevParsed = levelData;

        // set world size and size of each tile in TILED for conversion of coordinates into game coordinates.
        worldSize.x = levelData.getInt("width", 32);
        worldSize.y = levelData.getInt("height", 18);
        tileScale.x = levelData.getInt("tilewidth", 32);
        tileScale.y = levelData.getInt("tileheight", 32);
        //get blue bird data for nests
        blueBirdData = processBird(getBirdDefaultObj("blue"), null);

        // prepare texture/tileset parsing, get all tilesets used by current level
        // properly formatted raw data should have tilesets ordered by IDs so this guarantees sorted order.
        tileSetMakers.clear();
        stickerMaker = null;
        JsonValue tileSets = levelData.get("tilesets");
        for (JsonValue ts : tileSets){
            String source = ts.getString("source");
            String[] pathNames = source.split("/");
            String tileSetName = pathNames[pathNames.length - 1].split("\\.")[0];
            if (tileSetName.equals("stickers")){
                stickerMaker = new CollectionTileSetMaker(stickerTextureInfoMap, stickerNameMap, ts.getInt("firstgid"));
                continue;
            }
            JsonValue j = tileSetJsonMap.get(tileSetName);
            if (j == null){
                continue;
            }
            ImageTileSetMaker tileSetMaker = new ImageTileSetMaker(j, ts.getInt("firstgid"));
            int minId = ts.getInt("firstgid");
            int maxId = j.getInt("tilecount") - 1 + minId;
            for (int i = minId; i <= maxId; i++){
                tileSetMakers.put(i, tileSetMaker);
            }

        }
        stickers.clear();
        layers.clear();

        // clear raw data containers
        trajectory.clear();
        birdRawData.clear();
        lightningRawData.clear();
        platformRawData.clear();
        windRawData.clear();
        windDirs.clear();
        movingPlatRawData.clear();
        staticHazardRawData.clear();
        nestRawData.clear();
        deathZoneRawData.clear();

        JsonValue rawLayers = levelData.get("layers");
        // flatten all layers (all object layers are put together WITH depth considered)
        // - hazards and obstacle data are placed into their respective containers
        // - processing begins after all data is collected.
        currentObjectDepth = rawLayers.size;
        for (JsonValue layer : rawLayers) {
            String layerName = layer.getString("type", "");
            if (layerName.equals("objectgroup")){
                parseObjectLayer(layer);
            }
            else if (layerName.equals("tilelayer")){
                parseTileLayer(layer);
            }
            else {
                System.err.println("LEVEL DATA JSON FORMATTING VIOLATED");
            }
            currentObjectDepth--;
        }

        // begin object processing
        processBirds(birdRawData, trajectory);
        processLightning(lightningRawData);
        processPlatforms(platformRawData);
        processStaticHazards(staticHazardRawData);
        processWind(windRawData, windDirs);
        processMovingPlats(movingPlatRawData, trajectory);
        processNests(nestRawData, trajectory);
        processDeathZone(deathZoneRawData);
    }

    /**
     * parse all relevant object data in the given object layer by categorizing/grouping raw data.
     */
    private void parseObjectLayer(JsonValue layer) {
        JsonValue objs = layer.get("objects");
        for (JsonValue obj : objs) {
            obj.addChild("__DEPTH__", new JsonValue(currentObjectDepth));
            String template = obj.getString("template", "UNKNOWN");
            String[] pathNames = template.split("/");
            template = pathNames[pathNames.length - 1];
            obj.remove("template");
            obj.addChild("template", new JsonValue(template));
            if (template.endsWith("bird.json")) {
                birdRawData.add(obj);
            } else if (template.equals("platform.json") || (obj.has("type") && obj.getString("type").equals("platform"))) {
                platformRawData.add(obj);
            } else if (template.endsWith("lightning.json")) {
                lightningRawData.add(obj);
            } else if (template.equals("path_point.json")) {
                trajectory.put(obj.getInt("id"), obj);
            } else if (template.equals("spawn.json")) {
                readPositionAndConvert(obj, playerPos);
                playerDepth = currentObjectDepth;
            } else if (template.equals("goal.json")) {
                readPositionAndConvert(obj, goalPos);
                goalDepth = currentObjectDepth;
            } else if (template.equals("hazard.json") || obj.getString("type", "UNKNOWN").equals("static")){
                obj.addChild("hazard", new JsonValue("unspecified"));
                staticHazardRawData.add(obj);
            } else if (template.equals("rock.json")){
                obj.addChild("hazard", new JsonValue("rock"));
                staticHazardRawData.add(obj);
            } else if (template.equals("wind.json")){
                windRawData.add(obj);
            } else if (template.equals("wind_dir.json")){
                windDirs.put(obj.getInt("id"), obj);
            } else if (template.endsWith("cloud.json")){
                movingPlatRawData.add(obj);
            } else if (template.equals("nest.json")) {
                nestRawData.add(obj);
            } else if (logDefaultObjects.containsKey(template)){
                platformRawData.add(obj);
            } else if (obj.getString("type", "UNKNOWN").equals("death") ||
                    obj.getString("name", "UNKNOWN").equals("death") && obj.has("polygon")) {
                deathZoneRawData.add(obj);
            } else if (obj.has("gid")){
                // treat as possibly a sticker, process it
                parseSticker(obj);
            }
        }
    }

    private void parseSticker(JsonValue obj) {
        long gid = obj.getInt("gid");
        readPositionAndConvert(obj, temp);
        float x = temp.x;
        float y = temp.y;
        float angle = convertAngle(obj.getFloat("rotation", 0));
        int depth = obj.getInt("__DEPTH__", -1);
        // see if the sticker is coming from a tileset...
        Tile tile = getTileFromImages(gid);
        if (tile != null){
            TextureRegion tileRegion = tile.getRegionCopy();
            JsonValue AABB = processTileObjectAABB(obj, null, tileRegion.getRegionWidth(), tileRegion.getRegionHeight());
            tileRegion.flip(tile.isFlipX(), tile.isFlipY());
            stickers.add(new Sticker(x, y, angle, depth, AABB, tileRegion));
            return;
        }
        // see if the sticker is from stickers.json
        if (stickerMaker != null){
            int id = (int) (gid & LOWER28BITMASK);
            if (!stickerMaker.contains(id)){
                return;
            }
            TextureInfo textureInfo = stickerMaker.getTextureDataFromId(id);
            boolean flipX = (gid & (1L << 31)) != 0;
            boolean flipY = (gid & (1L << 30)) != 0;
            JsonValue AABB = processTileObjectAABB(obj, null, textureInfo.getRegionWidth(), textureInfo.getRegionHeight());
            if (textureInfo.isAnimated()){
                // make animated sticker
                stickers.add(new AnimatedSticker(x, y, angle, depth, AABB, textureInfo, flipX, flipY));
            }
            else {
                // make still-frame sticker
                TextureRegion textureRegion = textureInfo.getTextureRegion();
                textureRegion.flip(flipX, flipY);
                stickers.add(new Sticker(x, y, angle, depth, AABB, textureRegion));
            }
        }
    }

    /**
     * processes the trajectory starting from the given node represented by the next point ID.
     * Note: this modifies the given path JSON in place.
     *
     * @param pathJson the json array to store list of {x:value, y:value}. There should be a point (x,y) in this json
     *                 already because every object's first point on their path is their initial position.
     * @param trajectory the map of all path points
     * @param next the next point on the bird's path (the first point following the bird's position).
     * @param id the unique ID of the object
     * @return an index denoting which node the last point loops to. This index will be invalid if there is no loop on path.
     */
    private int processPath(JsonValue pathJson, HashMap<Integer,JsonValue> trajectory, int next, int id){
        // pathJson is already [{x:, y:}]. Hence next point to be added is index 1 on the path of points.
        int idx = 1;
        seen.clear();
        seen.put(id, 0);
        if (trajectory == null){
            return -1;
        }
        while (next != 0 && !seen.containsKey(next) && trajectory.get(next) != null) {
            seen.put(next, idx);
            idx++;
            JsonValue nodeData = trajectory.get(next);
            // put path point (x,y) into vector cache and perform conversion
            readPositionAndConvert(nodeData, temp);
            // add this node to bird's path
            pathJson.addChild(new JsonValue(temp.x));
            pathJson.addChild(new JsonValue(temp.y));
            // get next
            nodeData = nodeData.get("properties");
            next = getFromProperties(nodeData, "next_trajectory", pointDefault).asInt();
        }
        return seen.get(next, -1);
    }

    /**
     * This computes the AABB of an NON-ROTATED tile object (an object that has an associated tile).
     * The scalars cache is updated with the coefficients that can be used to convert an asset size to its Box2D size
     * under the scaling of the processed object.
     * @param rawData the entity's unprocessed json object data (this should be from level files)
     * @param defaultObj the entity's default object json (this should come from templates)
     * @param assetWidth the entity's corresponding tile asset width (original unscaled)
     * @param assetHeight the entity's corresponding tile asset height (original unscaled)
     * @return an AABB json consisting of {top corner x (relative), top corner y (relative), width, height}
     */
    private JsonValue processTileObjectAABB(JsonValue rawData, JsonValue defaultObj,
                                            int assetWidth, int assetHeight){
        // load the AABB top left corner position and then convert it to have origin centered on entity's position
        // CHOICE: the AABB top left corner will NOW be the asset's origin.
        // the asset's origin is the asset's top corner which is exactly half of the texture to the left and up.
        changeOrigins(temp.set(0,0), -0.5f * assetWidth, 0.5f * assetHeight);
        // the dimension of the entity (in pixel coordinates), which is a scaled version of the original
        float tileWidth = getFromObject(rawData, "width", defaultObj).asFloat();
        float tileHeight = getFromObject(rawData, "height", defaultObj).asFloat();
        // compute the scale factors of both dimensions to yield correct AABB starting location and dimensions
        scalars.set(tileWidth/assetWidth/tileScale.x, tileHeight/assetHeight/tileScale.y);

        // the AABB is specified entirely in game coordinates relative to the object's position
        // AABB[0 ... 3] = {corner x, corner y, AABB physics width, AABB physics height}
        JsonValue AABB = new JsonValue(JsonValue.ValueType.array);
        AABB.addChild(new JsonValue(temp.x * scalars.x));
        AABB.addChild(new JsonValue(temp.y * scalars.y));
        AABB.addChild(new JsonValue(assetWidth * scalars.x));
        AABB.addChild(new JsonValue(assetHeight * scalars.y));
        return AABB;
    }

    /**
     * processes the hit-box information given (without rotation).<br>
     * NOTE: the polygon origin is specified in asset coordinates. This is not Tiled world coordinates
     * nor is this origin specified in Box2D world coordinates. This origin is a coordinate within the
     * collider tool.
     * @param vertices the collection of points [{x,y}] that describes the shape of hitbox
     * @param origin a vector cache containing the origin of the polygon in asset-space.
     * @param scalars a vector cache with the proper scaling factors (ideally loaded from computing AABB)
     * @param flipX whether the hit-box should be flipped horizontally about the texture origin.
     * @param flipY whether the hit-box should be flipped vertically about the texture origin.
     * @param assetWidth the width of the source asset
     * @param assetHeight the height of the source asset
     * @return a JSON array consisting of the points of the hit-box polygon relative to the center of the entity.
     * The center is defined to be the location where the texture (default) origin is drawn.
     */
    private JsonValue processAssetHitBox(JsonValue vertices, Vector2 origin, Vector2 scalars,
                                         boolean flipX, boolean flipY, int assetWidth, int assetHeight){
        JsonValue shape = new JsonValue(JsonValue.ValueType.array);
        float ox = origin.x;
        float oy = origin.y;
        float sx = scalars.x * (flipX? -1 : 1);
        float sy = scalars.y * (flipY? -1 : 1);
        for (int idx = 0; idx < vertices.size; idx++){
            JsonValue jv = vertices.get(idx);
            readPosition(jv, temp);
            // adding the points of polygon onto polygon origin converts the vertex position to asset coordinates
            temp.add(ox, oy);
            // now change to cartesian coordinates centered on bird
            changeOrigins(temp,-0.5f * assetWidth, 0.5f * assetHeight);
            temp.scl(sx, sy);
            shape.addChild(new JsonValue(temp.x));
            shape.addChild(new JsonValue(temp.y));
        }
        return shape;
    }

    /**
     * red birds face to the right, all others to the left.
     * @param color the color {"red", "blue", "brown", "green"}
     * @return whether the bird asset is facing to the right
     */
    private boolean isBirdInitiallyFacingRight(String color){
        return color.equals("red") || color.equals("green");
    }

    private boolean doesBirdAttack(String color){
        return color.equals("green") || color.equals("brown");
    }

    /**
     * This method is specifically used to convert a given point p whose current origin point is the point (cx,cy)
     * where (cx,cy) is already expressed in the new coordinate system. The original coordinate system where
     * (0,0) -> (cx,cy) is a standard graphics coordinate system and the new coordinate system is standard cartesian
     * coordinate system.<br>
     * Note: p is modified directly.
     * @param p (x,y) expressed in relation to (cx,cy) being the origin
     * @param cx the x-offset of the old origin from the new
     * @param cy the y-offset of the old origin from the new
     */
    private void changeOrigins(Vector2 p, float cx, float cy){
        p.x += cx;
        p.y = cy - p.y;
    }

    private boolean isObjectHorizontallyFlipped(JsonValue object){
        return (object.getLong("gid", 0) & 1L << 31) != 0;
    }

    private boolean isObjectVerticallyFlipped(JsonValue object){
        return (object.getLong("gid", 0) & 1L << 30) != 0;
    }

    /**
     * @param object game object (templates/tile objects contain gid(s))
     * @return the indexing portion of a tile Gid (excludes flip bits).
     */
    private int getProcessedGid(JsonValue object){
        return (int) (object.getLong("gid", 0) & LOWER28BITMASK);
    }

    /**
     * Convert all raw bird JSON into game-expected JSON format.
     * @param rawData the unprocessed list of bird object data
     * @param trajectory map of path node Ids to raw JSON
     */
    private void processBirds(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory) {
        birdData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < birdData.length; ii++) {
            JsonValue data = processBird(rawData.get(ii), trajectory);
            birdData[ii] = data;
        }
    }

    /**
     * Convert a single raw bird JSON into game-expected JSON object
     * @param b the unprocessed bird object data
     * @param trajectory map of path node Ids to raw JSON
     * @return game-formatted bird JSON
     */
    private JsonValue processBird(JsonValue b, HashMap<Integer, JsonValue> trajectory){
        // data = the bird JSON that game will read
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        String color = computeColor(b.getString("template", "blue_bird.json"));
        JsonValue properties = b.get("properties");
        JsonValue defaultObj = getBirdDefaultObj(color);
        JsonValue defaults = defaultObj.get("properties");
        // set deterministic trivial properties
        data.addChild("depth", new JsonValue(b.getInt("__DEPTH__", -1)));
        data.addChild("color", new JsonValue(color));
        data.addChild("attack", new JsonValue(doesBirdAttack(color)));
        // add whether facing right
        boolean horizontalFlipped = isObjectHorizontallyFlipped(b);
        // XOR(flip, ?facingRight) => if flip then !(?facingRight) else (?facingRight)
        boolean facingRight = horizontalFlipped ^ isBirdInitiallyFacingRight(color);
        data.addChild("facing_right", new JsonValue(facingRight));

        // The following is procedure to: set position, hit-box, AABB data
        readPositionAndConvert(b, temp);
        addPosition(data, temp);
        JsonValue pathJson = new JsonValue(JsonValue.ValueType.array);
        // implicitly, the bird's location is the FIRST point on their path.
        pathJson.addChild(new JsonValue(temp.x));
        pathJson.addChild(new JsonValue(temp.y));

        // get dimension of a single filmstrip of the original animated asset (pixel coordinates)
        // using the first tile in the set is sufficient for birds, unless we want multi-hitbox.
        JsonValue tileJson = gameObjectTiles.get(getProcessedGid(defaultObj) - 1);
        int assetWidth = tileJson.getInt("imagewidth");
        int assetHeight = tileJson.getInt("imageheight");
        data.addChild("filmStripWidth", new JsonValue(assetWidth));
        data.addChild("filmStripHeight", new JsonValue(assetHeight));

        // add AABB
        data.addChild("AABB", processTileObjectAABB(b, defaultObj, assetWidth, assetHeight));

        // the hitbox information for birds is stored in one tile (the one animated), in its objectgroup, which we then
        // look at the first of its objects list.
        // Now, load hit-box, convert to asset coordinates then to cartesian coordinates relative to bird's pos
        JsonValue hitBoxPoints = tileJson.get("objectgroup").get("objects").get(0);
        float ox = hitBoxPoints.getFloat("x");
        float oy = hitBoxPoints.getFloat("y");
        JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                horizontalFlipped, false, assetWidth, assetHeight);
        data.addChild("points", shape);

        // Remaining: set bird properties and complete their path
        // the resulting path should be stored as a list of floats which is Json array of Json floats.
        float moveSpeed = 0;
        float atkSpeed = 0;
        // path birds are red and brown
        if (color.equals("red") || color.equals("brown")){
            // update properties
            moveSpeed = getFromProperties(properties, "move_speed", defaults).asFloat();
            // using custom properties to find rest of path
            // this takes either the bird's next point along its path or take from default (which should be 0)
            int next = getFromProperties(properties, "path", defaults).asInt();
            int loopTo = processPath(pathJson, trajectory, next, b.getInt("id"));
            data.addChild("loopTo", new JsonValue(loopTo));
        }
        if (doesBirdAttack(color)){
            atkSpeed = getFromProperties(properties, "atk_speed", defaults).asFloat();
        }
        data.addChild("path", pathJson);
        data.addChild("movespeed", new JsonValue(moveSpeed));
        data.addChild("atkspeed", new JsonValue(atkSpeed));
        return data;
    }

    private void processLightning(ArrayList<JsonValue> rawData){
        lightningData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < lightningData.length; ii++) {
            JsonValue rawLightning = rawData.get(ii);
            String lightningTemplateName = rawLightning.getString("template", "lightning.json");
            if (lightningTemplateName.endsWith("fill_lightning.json")){
                lightningData[ii] = processStillLightning(rawLightning);
            }
            else{
                // one of the growing lightning
                lightningData[ii] = processAnimatedLightning(rawLightning);
            }
        }
    }

    /**
     * processes a single animated lightning object into JSON data
     * @param rawData unprocessed lightning
     */
    private JsonValue processAnimatedLightning(JsonValue rawData){
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        readPositionAndConvert(rawData, temp);
        addPosition(data, temp);
        data.addChild("depth", new JsonValue(rawData.getInt("__DEPTH__", -1)));
        int tileIndex = getLightningTileIndex(rawData.getString("template"));
        data.addChild("tileIndex", new JsonValue(tileIndex));
        JsonValue props = rawData.get("properties");
        JsonValue lightningDefaultObj = lightningDefaultObjects[tileIndex];
        JsonValue lightningProps = lightningDefaultObj.get("properties");
        data.addChild("strike_timer", new JsonValue(getFromProperties(props, "strike_timer", lightningProps).asInt()));
        data.addChild("strike_duration", new JsonValue(getFromProperties(props, "strike_duration", lightningProps).asInt()));
        data.addChild("initial_timer_offset",
                new JsonValue(getFromProperties(props, "initial_timer_offset", lightningProps).asInt()));
        // get unscaled-size data from lightning.json file (collection of all ligntning bolts)
        // get the AABB for the given lightning
        int gid = getProcessedGid(lightningDefaultObj);
        JsonValue tileJson = gameObjectTiles.get(gid - 1);
        int assetWidth = tileJson.getInt("imagewidth");
        int assetHeight = tileJson.getInt("imageheight");
        data.addChild("filmStripWidth", new JsonValue(assetWidth));
        data.addChild("filmStripHeight", new JsonValue(assetHeight));
        data.addChild("AABB", processTileObjectAABB(rawData, lightningDefaultObj, assetWidth, assetHeight));

        // add all the hit-boxes (loop over number of frames)
        // INVARIANT: the selected tile is the last tile of the animation, so iterate ids: gid-length through gid-1
        int frameCount = getFrameCount(tileJson);
        boolean horizontalFlipped = isObjectHorizontallyFlipped(rawData);
        data.addChild("flipped", new JsonValue(horizontalFlipped));
        JsonValue hitboxes = new JsonValue(JsonValue.ValueType.array);
        for (int ii = gid - frameCount; ii < gid; ii++){
            JsonValue hitBoxPoints = gameObjectTiles.get(ii).get("objectgroup").get("objects").get(0);
            float ox = hitBoxPoints.getFloat("x");
            float oy = hitBoxPoints.getFloat("y");
            JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                    horizontalFlipped, false, assetWidth, assetHeight);
            hitboxes.addChild(shape);
        }
        data.addChild("hitboxes", hitboxes);
        data.addChild("fill_texture", new JsonValue(false));
        return data;
    }

    /**
     * processes a single still lightning object into JSON data
     * @param l unprocessed lightning data
     */
    private JsonValue processStillLightning(JsonValue l){
        // processed data
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        // set position
        readPositionAndConvert(l, temp);
        addPosition(data, temp);
        JsonValue props = l.get("properties");
        data.addChild("points", polyPoints(l.get("polygon"), lightningDefaultPoly));
        data.addChild("strike_timer", new JsonValue(getFromProperties(props, "strike_timer", lightningDefault).asInt()));
        data.addChild("strike_duration", new JsonValue(getFromProperties(props, "strike_duration", lightningDefault).asInt()));
        data.addChild("initial_timer_offset",
                new JsonValue(getFromProperties(props, "initial_timer_offset", lightningDefault).asInt()));
        data.addChild("depth", new JsonValue(l.getInt("__DEPTH__", -1)));
        data.addChild("fill_texture", new JsonValue(true));
        return data;
    }

    private void processPlatforms(ArrayList<JsonValue> rawData){
        platformData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < platformData.length; ii++) {
            //platform raw data
            JsonValue p = rawData.get(ii);
            String templateName = p.getString("template");
            JsonValue data;
            if (logDefaultObjects.containsKey(templateName)){
                data = processLog(p, logDefaultObjects.get(templateName));
                data.addChild("textured", new JsonValue(true));
            }
            else {
                //data we pass in to platform constructor
                data = new JsonValue(JsonValue.ValueType.object);
                // set position data
                readPositionAndConvert(p, temp);
                addPosition(data, temp);
                data.addChild("points", polyPoints(p.get("polygon"), platformDefaultPoly));
                data.addChild("depth", new JsonValue(p.getInt("__DEPTH__", -1)));
                data.addChild("textured", new JsonValue(false));
            }
            platformData[ii] = data;
        }
    }

    private JsonValue processLog(JsonValue log, JsonValue logDefaultObj){
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        readPositionAndConvert(log, temp);
        addPosition(data, temp);
        data.addChild("depth", new JsonValue(log.getInt("__DEPTH__", -1)));
        JsonValue tileJson = gameObjectTiles.get(getProcessedGid(logDefaultObj) - 1);
        int assetWidth = tileJson.getInt("imagewidth");
        int assetHeight = tileJson.getInt("imageheight");
        data.addChild("AABB", processTileObjectAABB(log, logDefaultObj, assetWidth, assetHeight));
        // add hit-box
        JsonValue hitBoxPoints = tileJson.get("objectgroup").get("objects").get(0);
        float ox = hitBoxPoints.getFloat("x");
        float oy = hitBoxPoints.getFloat("y");
        boolean flipX = isObjectHorizontallyFlipped(log);
        boolean flipY = isObjectVerticallyFlipped(log);
        JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                flipX, flipY, assetWidth, assetHeight);
        data.addChild("points", shape);
        data.addChild("flipX", new JsonValue(flipX));
        data.addChild("flipY", new JsonValue(flipY));
        data.addChild("angle", new JsonValue(convertAngle(log.getFloat("rotation",0))));
        // save asset name to allow quick retrieval of textures (ie: log.png -> texture(log))
        String[] sourcePath = tileJson.getString("image").split("/");
        String sourceImageName = sourcePath[sourcePath.length - 1];
        data.addChild("texture", new JsonValue(sourceImageName.split("\\.")[0]));
        return data;
    }

    private void processStaticHazards(ArrayList<JsonValue> rawData){
        staticHazardData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < staticHazardData.length; ii++) {
            JsonValue rawHazard = rawData.get(ii);
            if (rawHazard.getString("hazard").equals("unspecified")){
                staticHazardData[ii] = processStaticPolyHazard(rawHazard);
            }
            else {
                staticHazardData[ii] = processRock(rawHazard);
            }
        }
    }

    private JsonValue processStaticPolyHazard(JsonValue polyHazard){
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        readPositionAndConvert(polyHazard, temp);
        addPosition(data, temp);
        boolean fill = getFromProperties(polyHazard.get("properties"), "fill_texture", staticHazardDefault).asBoolean();
        data.addChild("type", new JsonValue(fill ? "fill" : "no_fill"));
        data.addChild("points", polyPoints(polyHazard.get("polygon"), staticHazardPoly));
        data.addChild("depth", new JsonValue(polyHazard.getInt("__DEPTH__", -1)));
        return data;
    }

    private JsonValue processRock(JsonValue rock){
        JsonValue data = new JsonValue(JsonValue.ValueType.object);
        readPositionAndConvert(rock, temp);
        addPosition(data, temp);
        data.addChild("type", new JsonValue("rock"));
        data.addChild("depth", new JsonValue(rock.getInt("__DEPTH__", -1)));
        // find this rock's corresponding tile
        JsonValue tileJson = gameObjectTiles.get(getProcessedGid(rockDefaultObj) - 1);
        int assetWidth = tileJson.getInt("imagewidth");
        int assetHeight = tileJson.getInt("imageheight");
        data.addChild("AABB", processTileObjectAABB(rock, rockDefaultObj, assetWidth, assetHeight));
        // add hit-box
        JsonValue hitBoxPoints = tileJson.get("objectgroup").get("objects").get(0);
        float ox = hitBoxPoints.getFloat("x");
        float oy = hitBoxPoints.getFloat("y");
        boolean flipX = isObjectHorizontallyFlipped(rock);
        boolean flipY = isObjectVerticallyFlipped(rock);
        JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                flipX, flipY, assetWidth, assetHeight);
        data.addChild("points", shape);
        data.addChild("flipX", new JsonValue(flipX));
        data.addChild("flipY", new JsonValue(flipY));
        data.addChild("angle", new JsonValue(convertAngle(rock.getFloat("rotation",0))));
        return data;
    }

    private void processDeathZone(ArrayList<JsonValue> deathZoneRawData) {
        deathZoneData = new JsonValue[deathZoneRawData.size()];
        for (int ii = 0; ii < deathZoneData.length; ii++) {
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            JsonValue rawData = deathZoneRawData.get(ii);
            readPositionAndConvert(rawData, temp);
            addPosition(data, temp);
            data.addChild("points", polyPoints(rawData.get("polygon")));
            data.addChild("depth", new JsonValue(rawData.getInt("__DEPTH__", -1)));
            deathZoneData[ii] =  data;
        }

    }

    private void processWind(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> windDirs){
        windData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < rawData.size(); ii++){
            //data of this wind
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //wind raw data
            JsonValue w = rawData.get(ii);
            //position
            readPositionAndConvert(w, temp);
            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(temp.x));
            pos.addChild(new JsonValue(temp.y));
            data.addChild("pos", pos);
            data.addChild("dimensions", polyPoints(w.get("polygon"), windDefaultPoly));
            //magnitude and direction
            JsonValue props = w.get("properties");
            data.addChild("magnitude", new JsonValue(getFromProperties(props, "magnitude", windDefault).asFloat()));
            data.addChild("direction", computeWindDirection(props, windDirs));
            data.addChild("depth", new JsonValue(w.getInt("__DEPTH__", -1)));
            data.addChild("particle", new JsonValue(getFromProperties(props, "particle", windDefault).asString()));
            windData[ii] = data;
        }
    }

    private void processMovingPlats(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory){
        movingPlatformData = new JsonValue[rawData.size()];
        IntIntMap seen = new IntIntMap(16);
        for (int ii = 0; ii < movingPlatformData.length; ii++) {
            seen.clear();
            //data we pass in to platform constructor
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //moving platform raw data
            JsonValue mp = rawData.get(ii);
            data.addChild("depth", new JsonValue(mp.getInt("__DEPTH__", -1)));
            //set position and load position into path.
            readPositionAndConvert(mp, temp);
            addPosition(data, temp);
            JsonValue pathJson = new JsonValue(JsonValue.ValueType.array);
            pathJson.addChild(new JsonValue(temp.x));
            pathJson.addChild(new JsonValue(temp.y));
            JsonValue props = mp.get("properties");
            int tileIndex = getCloudTileIndex(mp.getString("template"));
            data.addChild("tileIndex", new JsonValue(tileIndex));
            JsonValue cloudDefaultObj = cloudDefaultObjects[tileIndex];
            JsonValue cloudDefaultProps = cloudDefaultObj.get("properties");

            // update properties
            float moveSpeed = getFromProperties(props, "move_speed", cloudDefaultProps).asFloat();
            // using custom properties to find rest of path
            int nextPointID = getFromProperties(props, "path", cloudDefaultProps).asInt();
            int loopTo = processPath(pathJson, trajectory, nextPointID, mp.getInt("id"));
            data.addChild("path", pathJson);
            data.addChild("loopTo", new JsonValue(loopTo));
            data.addChild("movespeed", new JsonValue(moveSpeed));

            // find this cloud's corresponding tile and get the AABB for the given cloud
            int idx = getProcessedGid(cloudDefaultObj);
            JsonValue tileJson = gameObjectTiles.get(idx-1);
            int assetWidth = tileJson.getInt("imagewidth");
            int assetHeight = tileJson.getInt("imageheight");
            data.addChild("AABB", processTileObjectAABB(mp, cloudDefaultObj, assetWidth, assetHeight));
            // add hit-box
            JsonValue hitBoxPoints = tileJson.get("objectgroup").get("objects").get(0);
            float ox = hitBoxPoints.getFloat("x");
            float oy = hitBoxPoints.getFloat("y");
            boolean horizontalFlipped = isObjectHorizontallyFlipped(mp);
            JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                    horizontalFlipped, false, assetWidth, assetHeight);
            data.addChild("points", shape);
            data.addChild("flipped", new JsonValue(horizontalFlipped));
            movingPlatformData[ii] = data;
        }
    }

    private void processNests(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory){
        nestData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < nestData.length; ii++) {
            //data we pass in to nest constructor
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //nest raw data
            JsonValue n = rawData.get(ii);
            JsonValue props = n.get("properties");
            // set position data
            readPositionAndConvert(n, temp);
            addPosition(data, temp);
            // the resulting path should be stored as a list of floats which is Json array of Json floats.
            JsonValue pathJson = new JsonValue(JsonValue.ValueType.array);
            pathJson.addChild(new JsonValue(temp.x));
            pathJson.addChild(new JsonValue(temp.y));
            JsonValue defaultProps = nestDefault.get("properties");
            int nextPointID = getFromProperties(props, "path", defaultProps).asInt();
            processPath(pathJson, trajectory, nextPointID, n.getInt("id") );
            data.addChild("path", pathJson);
            data.addChild("bird_speed", new JsonValue(getFromProperties(props, "bird_speed", defaultProps).asFloat()));
            data.addChild("spawn_delay", new JsonValue(getFromProperties(props, "spawn_delay", defaultProps).asFloat()));

            boolean horizontalFlipped = isObjectHorizontallyFlipped(n);
            boolean verticalFlipped = isObjectVerticallyFlipped(n);
            data.addChild("flipX", new JsonValue(horizontalFlipped));
            // data.addChild("flipY", new JsonValue(verticalFlipped));

            // add AABB and hitbox
            JsonValue tileJson = gameObjectTiles.get(getProcessedGid(nestDefault) - 1);
            int assetWidth = tileJson.getInt("imagewidth");
            int assetHeight = tileJson.getInt("imageheight");
            data.addChild("filmStripWidth", new JsonValue(assetWidth));
            data.addChild("filmStripHeight", new JsonValue(assetHeight));
            data.addChild("AABB", processTileObjectAABB(nestDefault, nestDefault, assetWidth, assetHeight));
            JsonValue hitBoxPoints = tileJson.get("objectgroup").get("objects").get(0);
            float ox = hitBoxPoints.getFloat("x");
            float oy = hitBoxPoints.getFloat("y");
            JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                    horizontalFlipped, verticalFlipped, assetWidth, assetHeight);
            data.addChild("points", shape);
            data.addChild("depth", new JsonValue(n.getInt("__DEPTH__", -1)));
            nestData[ii] = data;
        }
    }


    /**
     * loads into p an (x,y) pair that are direct properties of the given JsonValue into vector cache
     * Note: this pair (x,y) is still raw data and is not game coordinates.
     * @param v JSON of format {... "x": float, "y":float }
     * @param p cache vector
     */
    private void readPosition(JsonValue v, Vector2 p){
        p.x = v.getFloat("x", -1);
        p.y = v.getFloat("y", -1);
    }

    /**
     * loads into p an (x,y) pair that are direct properties of the given JsonValue into vector cache.
     * The (x,y) pair is then converted into game coordinates.
     * @param v JSON of format {... "x": float, "y":float }
     */
    private void readPositionAndConvert(JsonValue v, Vector2 p){
        readPosition(v,p);
        convertPos(p);
    }

    /**
     * adds x:float, y:float into given JSON. This should be used to add parsed data to self-created JSON.
     * @param v JSON value to be exported (read by game)
     * @param p position vector
     */
    private void addPosition(JsonValue v, Vector2 p){
        v.addChild("x", new JsonValue(p.x));
        v.addChild("y", new JsonValue(p.y));
    }

    /** converts Tiled screen coordinate to game coordinate
     * this modifies the given vector in place.
     */
    private void convertPos(Vector2 pos){
        pos.x /= tileScale.x;
        pos.y /= tileScale.y;
        pos.y = worldSize.y - pos.y;
    }

    /**
     * returns the value associated with the unique key from within an array of properties.
     * This method, when given the correct corresponding default properties Json, will guarantee non-Null returns.
     * @param properties an array JsonValue consisting of JsonValues in which the requested key can be found.
     * @param key property key
     * @return the results of the query (non-NULL)
     */
    private JsonValue getFromProperties(JsonValue properties, String key, JsonValue defaultProperties){
        JsonValue queryResult = null;
        if (properties != null){
            queryResult = getFromProperties(properties, key);
        }
        if (queryResult == null){
            return getFromProperties(defaultProperties, key);
        }
        return queryResult;
    }

    /**
     * returns the value associated with the unique key from within an array of properties.
     * @param properties an array JsonValue consisting of JsonValues in which the requested key can be found.
     * @param key property key
     * @return the results of the query, possibly NULL
     */
    private JsonValue getFromProperties(JsonValue properties, String key){
        JsonValue v = null;
        for (JsonValue jv : properties){
            if (jv.getString("name", "UNKNOWN").equals(key)){
                v = jv.get("value");
            }
        }
        return v;
    }

    /**
     * given non-null objects, return the requested property's value.
     * The property is a direct children ie: {..., "key": value, ...}
     * @param object the given object
     * @param key the key
     * @param defaultObject default object if given object is missing given key
     * @return the non-Null value associated with given key
     */
    private JsonValue getFromObject(JsonValue object, String key, JsonValue defaultObject){
        JsonValue queryResult = object.get(key);
        if (queryResult == null){
            return defaultObject.get(key);
        }
        return queryResult;
    }

    /**
     * returns the bird default object
     * @param color the color of the bird
     * @return default JsonValue for the given bird variant
     */
    private JsonValue getBirdDefaultObj(String color){
        switch (color){
            case "blue":
                return blueBirdDefaultObj;
            case "green":
                return greenBirdDefaultObj;
            case "brown":
                return brownBirdDefaultObj;
            default:
                // should be red
                return redBirdDefaultObj;
        }
    }

    /**
     * returns a JsonValue containing the list of points (in game coordinates) that make up a polygon
     * @param polygon the polygon to get points for
     * @param defaultPoly the default polygon for this object
     * @return a JsonValue of type array containing the list of points that make up this polygon
     *         (in the format used in PolygonObstacle, etc.)
     */
    private JsonValue polyPoints(JsonValue polygon, JsonValue defaultPoly){
        JsonValue points = null;
        if (polygon != null){
            points = polyPoints(polygon);
        }
        if (points == null){
            return polyPoints(defaultPoly);
        }
        return points;
    }

    /**
     * returns a JsonValue containing the list of points (in game coordinates) that make up a polygon
     * @param polygon the polygon to get points for
     * @return a JsonValue of type array containing the list of points that make up this polygon
     *         (in the format used in SimpleObstacle, etc.)
     */
    private JsonValue polyPoints(JsonValue polygon){
        JsonValue points = new JsonValue(JsonValue.ValueType.array);
        for (JsonValue j : polygon){
            temp.x = j.getFloat("x", 0)/tileScale.x;
            temp.y = -j.getFloat("y", 0)/tileScale.y;
            points.addChild(new JsonValue(temp.x));
            points.addChild(new JsonValue(temp.y));
        }
        return points;
    }

    /**
     * returns a JsonValue containing the direction that the given wind blows in
     * @param wind the wind to find the angle of
     * @param windDirs the set of wind direction objects (so we can find the one the wind points to)
     * @return a JsonValue of type doubleValue containing this wind's direction
     */
    private JsonValue computeWindDirection(JsonValue wind, HashMap<Integer, JsonValue> windDirs){
        int key = getFromProperties(wind, "dir", windDefault).asInt();
        int defKey = getFromProperties(windDefault, "dir").asInt();
        float ang = windDirDefault;
        if (key != defKey){
            ang = windDirs.get(key).getFloat("rotation", windDirDefault);
            //subtract from 360 since tiled gives clockwise rotation but WindModel expects counterclockwise
            ang = 360-ang;
            //convert from deg to rad
            ang *= (float) (Math.PI/180);
        }
        //convert to being in rads from right instead of rads from top
        ang += (float) (Math.PI/2);
        if (ang >= Math.PI*2) ang -= Math.PI*2;
        return new JsonValue(ang);
    }

    /**
     * convert clock-wise angle from Tiled to counterclock-wise angle. <br>
     * The reference angle for 0 degree is y-axis/12pm
     * @param tiledAngle clock-wise angle (degrees)
     * @return counterclock-wise angle
     */
    private float convertAngle(float tiledAngle){
        //subtract from 360 since tiled gives clockwise rotation, but we need counterclockwise
        //positive modulo 360 because given angle may be negative.
        tiledAngle = ((tiledAngle % 360) + 360) % 360;
        float angle = 360 - tiledAngle;
        //convert from deg to rad
        return angle * (float) (Math.PI/180);
    }

    // ============================= BEGIN TILED PARSING HELPERS =============================

    /**
     * @param gid raw grid tile id (possibly with flipping bits enabled)
     * @return tile (possibly null) for the corresponding gid
     */
    private Tile getTileFromImages(long gid){
        // the Tiled ID is a 32-bit UNSIGNED integer
        // actual ID is the lower 28 bits of the Tiled ID
        int id = (int) (gid & LOWER28BITMASK);
        // Bit 32 is used for storing whether the tile is horizontally flipped
        // Bit 31 is used for storing whether the tile is vertically flipped
        // Bit 30 is used for storing whether the tile is diagonally flipped
        boolean flipX = (gid & (1L << 31)) != 0;
        boolean flipY = (gid & (1L << 30)) != 0;
        boolean flipD = (gid & (1L << 29)) != 0;
        ImageTileSetMaker tileSetMaker = tileSetMakers.get(id);
        if (tileSetMaker != null){
            return tileSetMaker.getTileFromId(id, flipD, flipX, flipY);
        }
        return null;
    }

    private void parseTileLayer(JsonValue layer){
        // loop over array data and make tiles
        long[] data = layer.get("data").asLongArray();
        Tile[] tiles = new Tile[data.length];
        int worldWidth = (int) worldSize.x;
        int worldHeight = (int) worldSize.y;
        for (int i = 0; i < tiles.length; i++){
            // the Tiled ID is a 32-bit UNSIGNED integer
            long rawId = data[i];
            if (rawId == 0){
                continue;
            }
            int col = i % worldWidth;
            int row = worldHeight - 1 -  i / worldWidth;
            int idx = row * worldWidth + col;
            tiles[idx] = getTileFromImages(rawId);
        }
        layers.add(new TiledLayer(tiles, currentObjectDepth, worldWidth, worldHeight));
    }

    /**
     * A TileSetMaker produces textures upon request.
     * This class is useful when associating Tile IDs with textures.
     */
    private abstract static class TileSetMaker {

        /** the tile ID assigned to the FIRST tile in this set*/
        public int minId;
        /** the tile ID assigned to the LAST tile in this set*/
        public int maxId;

        /**
         * @param gid tile grid id
         * @return whether this tileset contains the given tile gid
         */
        public boolean contains(int gid){
            return gid <= maxId && gid >= minId;
        }
    }

    /**
     * A ImageTileSetMaker produces texture regions upon request by cutting texture regions from a single texture.
     */
    private class ImageTileSetMaker extends TileSetMaker {
        private final FilmStrip tileset;
//        private final  int width;
//        private final int height;
//        private String tileSetName;

        ImageTileSetMaker(JsonValue tileSetJson, int firstGid){
            double tileCount = tileSetJson.getInt("tilecount");
            minId = firstGid;
            maxId = tileSetJson.getInt("tilecount") - 1 + minId;
            String name = tileSetJson.getString("name");
            //this.tileSetName = name;
            Texture texture = tileSetTextureMap.get(name);
            // removes flickering on square tiles
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            //width = tileSetJson.getInt("tilewidth");
            //height = tileSetJson.getInt("tileheight");
            int columns = tileSetJson.getInt("columns");
            tileset = new FilmStrip(texture, (int) Math.ceil(tileCount/ columns), columns);
        }

        /**
         * returns a subregion of the tileset texture <br>
         * @param id the associated id of the desired Tile, where contains(id) is true.
         * @param flipD whether to flip the resulting region anti-diagonally (not necessarily supported)
         * @param flipX whether to flip the resulting region horizontally
         * @param flipY whether to flip the resulting region vertically
         * @return a tile from the tile set corresponding to the given id
         */
        public Tile getTileFromId(int id, boolean flipD, boolean flipX, boolean flipY){
            int index = id - minId;
            Tile tile = new Tile(tileset, index);
            // enumerate all 8 possible cases
            if (flipD && flipY && flipX){
                // 30, 31, 32 => flip x THEN counter-clock-wise rotate 270 deg
                tile.setFlip(true, false);
                tile.setRotation((float) Math.PI * 1.5f);
            }
            else if (flipY && flipX){
                // 31, 32 => rotate 180 (flip both axes)
                tile.setFlip(true, true);
            }
            else if (flipD && flipX){
                // 30, 32 => counter-clock-wise rotate 270 deg
                tile.setRotation((float) Math.PI * 1.5f);
            }
            else if (flipD && flipY){
                // 30, 31 => counter-clock-wise rotate 90 deg
                tile.setRotation((float) Math.PI / 2f);
            }
            else if (flipX){
                // 32 => flip x
                tile.setFlip(true, false);
            }
            else if (flipY){
                // 31 => flip y
                tile.setFlip(false, true);
            }
            else if (flipD){
                // 30 => flip x THEN counter-clock-wise rotate 90 deg
                tile.setFlip(true, false);
                tile.setRotation((float) Math.PI /2f);
            }
            return tile;
        }
    }

    /**
     * A CollectionTileSetMaker produces texture regions upon request by retrieving texture regions from a list of
     * textures. This is particularly useful for retrieving unrelated textures (stickers).
     */
    private class CollectionTileSetMaker extends TileSetMaker{

        private final HashMap<String, TextureInfo> collection;
        private final IntMap<String> idNameMap;
        CollectionTileSetMaker(HashMap<String, TextureInfo> collection, IntMap<String> idNameMap, int firstGid){
            minId = firstGid;
            maxId = firstGid + maxStickerSetId;
            this.collection = collection;
            this.idNameMap = idNameMap;
        }

        /**
         * @param id the associated id of the desired Tile, where contains(id) is true.
         * @return texture data from the collection set corresponding to the given id
         */
        public TextureInfo getTextureDataFromId(int id) {
            return collection.get(idNameMap.get(id - minId));
        }
    }

    // ========================== END of FUNCTIONS for TILE PARSING =================================

    /**
     * @param tile the tile of the game object
     * @return the number of frames this object has for animations
     */
    private int getFrameCount(JsonValue tile) {
        JsonValue animation = tile.get("animation");
        if (animation == null){
            return 0;
        }
        // an additional frame is included to allow easy tracing of still frame
        return animation.size - 1;
    }


    // BIRD TEMPLATES ==================================================================================================

    /**
     * maps bird template name to color
     * @param template the template name of a colored bird
     * @return the bird's color
     */
    public static String computeColor(String template){
        if (template.endsWith("blue_bird.json")) return "blue";
        else if (template.endsWith("brown_bird.json")) return "brown";
        else if (template.endsWith("red_bird.json")) return "red";
        else if (template.endsWith("green_bird.json")) return "green";
        else return "UNKNOWN";
    }


    // CLOUD TEMPLATES =================================================================================================
    /**
     * @param templateName cloud template name
     * @return the asset index in the list of loaded cloud assets
     */
    public static int getCloudTileIndex(String templateName){
        if (templateName.endsWith("large_cloud.json")){
            return 0;
        }
        else if (templateName.endsWith("medium_cloud.json")){
            return 1;
        }
        else if (templateName.endsWith("small_cloud.json")){
            return 2;
        }
        else if (templateName.endsWith("smaller_cloud.json")){
            return 3;
        }
        return -1;
    }

    // LIGHTNING TEMPLATES =============================================================================================
    /**
     * @param templateName lightning template name
     * @return the asset index in the list of loaded animated lightning assets
     */
    public static int getLightningTileIndex(String templateName){
        if (templateName.endsWith("out_lightning.json")){
            return 0;
        }
        else if (templateName.endsWith("two_lightning.json")){
            return 1;
        }
        else if (templateName.endsWith("in_lightning.json")){
            return 2;
        }
        else if (templateName.endsWith("one_lightning.json")){
            return 3;
        }
        else if (templateName.endsWith("left_lightning.json")){
            return 4;
        }
        return -1;
    }
}