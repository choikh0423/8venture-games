package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.Sticker;
import java.util.ArrayList;
import java.util.HashMap;

public class LevelParser {

    /** the Tiled-JSON raw value that was previously parsed.
     * This saves parsing time when a level is played immediately again.
     */
    private JsonValue prevParsed;

    /** reference to game global default values */
    private final JsonValue globalConstants;

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

    /** the texture data of the tile layers
     * Invariant: front layers are stored last in list
     */
    private ArrayList<TextureRegion[]> layers = new ArrayList<>();

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

    /** the default JSON properties of path point. */
    private final JsonValue pointDefault;

    /** the default JSON properties of lightning object */
    private final JsonValue lightningDefault;

    /** the default JSON polygon of lightning object */
    private final JsonValue lightningDefaultPoly;

    /** the default JSON polygon of a platform */
    private final JsonValue platformDefaultPoly;

    /** the default JSON polygon of a static hazard */
    private final JsonValue staticHazardDefaultPoly;

    /** the default JSON properties of a wind object */
    private final JsonValue windDefault;

    /** the default JSON polygon of a wind object */
    private final JsonValue windDefaultPoly;

    /** the default JSONs of movable cloud objects */
    private final JsonValue[] cloudDefaultObjects;

    /** the default JSONs of animated lightning objects */
    private final JsonValue[] lightningDefaultObjects;

    /** the default direction of a wind object */
    private static final float windDirDefault = 0;

    /** maps from tileset name (bushes, cliffs, etc) to its undivided texture */
    private final HashMap<String, Texture> textureMap;

    /** maps from tileset name (bushes, cliffs, etc) to its JSON data */
    private final HashMap<String, JsonValue> tileSetJsonMap;

    /** all objects in game that needs asset information can be found in an objects.json */
    private final JsonValue gameObjectTiles;

    /** the list of texture region cutters, one for each tileset */
    private ArrayList<TileSetMaker> tileSetMakers;

    /** the texture producer for stickers */
    private TileSetMaker stickerMaker;

    /** list of all sticker textures in THE ORDER as given by sticker.json in levels/tilesets/ */
    private final Texture[] stickerTextures;

    private static final int LOWER28BITMASK = 0xFFFFFFF;

    /** map used to track visited trajectory */
    private final IntIntMap seen = new IntIntMap(16);

    /** the depth of the current layer being parsed*/
    private int currentObjectDepth;

    /** drawing depth of Gale */
    private int playerDepth;

    /** drawing depth of scarf */
    private int goalDepth;

    /**
     * @return tile texture layers
     */
    public ArrayList<TextureRegion[]> getLayers() {
        return layers;
    }

    /**
     * @return processed bird data that is ready for consumption
     */
    public JsonValue[] getBirdData() {
        return birdData;
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

    public LevelParser(AssetDirectory directory){
        globalConstants = directory.getEntry("global:constants", JsonValue.class);

        JsonValue redBirdTemplate = directory.getEntry("red_bird:template", JsonValue.class);
        JsonValue blueBirdTemplate = directory.getEntry("blue_bird:template", JsonValue.class);
        JsonValue greenBirdTemplate = directory.getEntry("green_bird:template", JsonValue.class);
        JsonValue brownBirdTemplate = directory.getEntry("brown_bird:template", JsonValue.class);
        JsonValue pathPointTemplate = directory.getEntry("path_point:template", JsonValue.class);
        JsonValue lightningTemplate = directory.getEntry("lightning:template", JsonValue.class);
        JsonValue platformTemplate = directory.getEntry("platform:template", JsonValue.class);
        JsonValue staticHazardTemplate = directory.getEntry("static_hazard:template", JsonValue.class);
        JsonValue windTemplate = directory.getEntry("wind:template", JsonValue.class);
        // cloud templates
        JsonValue cloud0Template = directory.getEntry("cloud0:template", JsonValue.class);
        JsonValue cloud1Template = directory.getEntry("cloud1:template", JsonValue.class);
        JsonValue cloud2Template = directory.getEntry("cloud2:template", JsonValue.class);
        JsonValue cloud3Template = directory.getEntry("cloud3:template", JsonValue.class);
        // growing lightning templates
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
        lightningDefault = lightningTemplate.get("object").get("properties");
        lightningDefaultPoly = lightningTemplate.get("object").get("polygon");
        platformDefaultPoly = platformTemplate.get("object").get("polygon");
        staticHazardDefaultPoly = staticHazardTemplate.get("object").get("polygon");
        windDefault = windTemplate.get("object").get("properties");
        windDefaultPoly = windTemplate.get("object").get("polygon");
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

        // save tileset textures and their corresponding JSON tileset data
        textureMap = new HashMap<>();
        textureMap.put("bushes", directory.getEntry( "tileset:bushes", Texture.class ));
        textureMap.put("bushes_flipped", directory.getEntry("tileset:bushes_flipped", Texture.class));
        textureMap.put("trees", directory.getEntry( "tileset:trees", Texture.class ));
        textureMap.put("trees_flipped", directory.getEntry("tileset:trees_flipped", Texture.class));
        textureMap.put("cliffs", directory.getEntry( "tileset:cliffs", Texture.class ));
        textureMap.put("cliffs_flipped", directory.getEntry("tileset:cliffs_flipped", Texture.class));

        // add tile layer tile-sets (artwork)
        tileSetJsonMap = new HashMap<>();
        tileSetJsonMap.put("bushes", directory.getEntry("data:bushes", JsonValue.class));
        tileSetJsonMap.put("trees", directory.getEntry("data:trees", JsonValue.class));
        tileSetJsonMap.put("cliffs", directory.getEntry("data:cliffs", JsonValue.class));

        // add object json
        gameObjectTiles = directory.getEntry("data:objects", JsonValue.class).get("tiles");

        // load all stickers
        stickerTextures = new Texture[]{
                directory.getEntry("stickers:dcloud0", Texture.class),
                directory.getEntry("stickers:dcloud1", Texture.class),
                directory.getEntry("stickers:dcloud2", Texture.class),
                directory.getEntry("stickers:dcloud3", Texture.class)
        };
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

        // prepare texture/tileset parsing, get all tilesets used by current level
        // properly formatted raw data should have tilesets ordered by IDs so this guarantees sorted order.
        tileSetMakers = new ArrayList<>();
        stickerMaker = null;
        JsonValue tileSets = levelData.get("tilesets");
        for (JsonValue ts : tileSets){
            String source = ts.getString("source");
            if (source.endsWith("stickers.json")){
                stickerMaker = new CollectionTileSetMaker(stickerTextures, ts.getInt("firstgid"));
                continue;
            }
            JsonValue j = getTileLayerTileSetJson(source);
            if (j == null){
                continue;
            }
            tileSetMakers.add(
                    new ImageTileSetMaker(j, ts.getInt("firstgid"))
            );
        }
        stickers.clear();
        layers.clear();

        // containers for unprocessed JSON data
        HashMap<Integer, JsonValue> trajectory = new HashMap<>();
        ArrayList<JsonValue> birdRawData = new ArrayList<>();
        ArrayList<JsonValue> platformRawData = new ArrayList<>();
        ArrayList<JsonValue> lightningRawData = new ArrayList<>();
        ArrayList<JsonValue> windRawData = new ArrayList<>();
        HashMap<Integer, JsonValue> windDirs = new HashMap<>();
        ArrayList<JsonValue> staticHazardRawData = new ArrayList<>();
        ArrayList<JsonValue> movingPlatRawData = new ArrayList<>();

        JsonValue rawLayers = levelData.get("layers");
        // flatten all layers (all object layers are put together WITH depth considered)
        // - hazards and obstacle data are placed into their respective containers
        // - processing begins after all data is collected.
        currentObjectDepth = rawLayers.size;
        for (JsonValue layer : rawLayers) {
            String layerName = layer.getString("type", "");
            if (layerName.equals("objectgroup")){
                parseObjectLayer(layer, trajectory, birdRawData, lightningRawData, platformRawData, windRawData,
                        windDirs, movingPlatRawData, staticHazardRawData);
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
        processNewLightning(lightningRawData);
        processPlatforms(platformRawData);
        processStaticHazards(staticHazardRawData);
        processWind(windRawData, windDirs);
        processMovingPlats(movingPlatRawData, trajectory);
    }

    /**
     * parse all relevant object data in the given object layer by categorizing/grouping raw data.
     */
    private void parseObjectLayer(JsonValue layer, HashMap<Integer, JsonValue> trajectory,
                                  ArrayList<JsonValue> birdRawData,
                                  ArrayList<JsonValue> lightningRawData,
                                  ArrayList<JsonValue> platformRawData,
                                  ArrayList<JsonValue> windRawData,
                                  HashMap<Integer, JsonValue> windDirs,
                                  ArrayList<JsonValue> movingPlatRawData,
                                  ArrayList<JsonValue> staticHazardRawData)
    {
        JsonValue objs = layer.get("objects");
        for (JsonValue obj : objs) {
            obj.addChild("__DEPTH__", new JsonValue(currentObjectDepth));
            String template = obj.getString("template", "IGNORE");
            if (template.contains("bird.json")) {
                birdRawData.add(obj);
            } else if (template.contains("platform.json") || (obj.has("type") && obj.getString("type").equals("platform"))) {
                platformRawData.add(obj);
            } else if (template.contains("lightning.json")) {
                lightningRawData.add(obj);
            } else if (template.contains("path_point.json")) {
                trajectory.put(obj.getInt("id"), obj);
            } else if (template.contains("spawn.json")) {
                readPositionAndConvert(obj, playerPos);
                playerDepth = currentObjectDepth;
            } else if (template.contains("goal.json")) {
                readPositionAndConvert(obj, goalPos);
                goalDepth = currentObjectDepth;
            } else if (template.contains("static_hazard.json")){
                staticHazardRawData.add(obj);
            } else if (template.contains("wind.json")){
                windRawData.add(obj);
            } else if (template.contains("wind_dir.json")){
                windDirs.put(obj.getInt("id"), obj);
            } else if (template.contains("cloud.json")){
                movingPlatRawData.add(obj);
            } else if (obj.has("gid")){
                // treat as possibly a sticker, process it
                parseSticker(obj);
            }
        }
    }

    private void parseSticker(JsonValue obj) {
        int gid = obj.getInt("gid");
        TextureRegion texture = getTileFromImages(gid);
        if (texture == null){
            if (stickerMaker != null){
                int id = (int) (gid & LOWER28BITMASK);
                boolean flipX = (gid & (1L << 31)) != 0;
                boolean flipY = (gid & (1L << 30)) != 0;
                texture = stickerMaker.getRegionFromId(id, false, flipX, flipY);
            }
            else {
                return;
            }
        }
        readPositionAndConvert(obj, temp);
        float x = temp.x;
        float y = temp.y;
        JsonValue AABB = processTileObjectAABB(obj, null, texture.getRegionWidth(), texture.getRegionHeight());
        stickers.add(new Sticker(x,y, obj.getInt("__DEPTH__", -1), AABB, texture));
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
     * This computes the AABB of a tile object (an object that has an associated tile).
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

        // the AABB is specified entirely in game coordinates relative to the bird's position
        // AABB[0 ... 3] = {corner x, corner y, AABB physics width, AABB physics height}
        JsonValue AABB = new JsonValue(JsonValue.ValueType.array);
        AABB.addChild(new JsonValue(temp.x * scalars.x));
        AABB.addChild(new JsonValue(temp.y * scalars.y));
        AABB.addChild(new JsonValue(assetWidth * scalars.x));
        AABB.addChild(new JsonValue(assetHeight * scalars.y));
        return AABB;
    }

    /**
     * processes the hit-box information given.<br>
     * NOTE: the polygon origin is specified in asset coordinates. This is not Tiled world coordinates
     * nor is this origin specified in Box2D world coordinates. This origin is a coordinate within the
     * collider tool.
     * @param vertices the collection of points [{x,y}] that describes the shape of hitbox
     * @param origin a vector cache containing the origin of the polygon in asset-space.
     * @param scalars a vector cache with the proper scaling factors (ideally loaded from computing AABB)
     * @param horizontalFlipped whether the hitbox should be flipped horizontally about the texture origin.
     * @param assetWidth the width of the source asset
     * @param assetHeight the height of the source asset
     * @return a JSON array consisting of the points of the hitbox polygon relative to the center of the entity.
     * The center is defined to be the location where the texture (default) origin is drawn.
     */
    private JsonValue processAssetHitBox(JsonValue vertices, Vector2 origin, Vector2 scalars,
                                         boolean horizontalFlipped, int assetWidth, int assetHeight){
        JsonValue shape = new JsonValue(JsonValue.ValueType.array);
        float ox = origin.x;
        float oy = origin.y;
        float sx = scalars.x * (horizontalFlipped ? -1 : 1);
        float sy = scalars.y;
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
     * load into vector cache the dimensions of the tile asset.
     * @param tileJson the JSON for the tile
     * @param tileSetJson the JSON for the entire tileset in which the given tile belongs to.
     */
    private void loadAssetTileDimensions(JsonValue tileJson, JsonValue tileSetJson){
        // implementation detai: tilesets with consistent tile sizes (same throughout) will only have
        // "tilewidth" and "tileheight" property in the tileset json. For tilesets with varying tile sizes,
        // each individual tile will have their "imagewidth" and "imageheight" properties. These tilesets are
        // collections of images instead of coming from a single image.
        temp.set(tileJson.getInt("imagewidth", tileSetJson.getInt("tilewidth")),
                tileJson.getInt("imageheight", tileSetJson.getInt("tileheight")));
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
     * coordinate system.
     *
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

    /**
     * @param object game object (templates/tile objects contain gid(s))
     * @return the indexing portion of a tile Gid (excludes flip bits).
     */
    private int getProcessedGid(JsonValue object){
        return (int) (object.getLong("gid", 0) & LOWER28BITMASK);
    }

    /**
     * Convert raw bird JSON into game-expected JSON format.
     * @param rawData the unprocessed bird object data
     * @param trajectory map of path node Ids to raw JSON
     */
    private void processBirds(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory){
        birdData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < birdData.length; ii++) {
            // b = raw bird data
            JsonValue b = rawData.get(ii);
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
            boolean facingRight = isBirdInitiallyFacingRight(color);
            boolean horizontalFlipped = isObjectHorizontallyFlipped(b);
            // XOR(flip, facingRight) => if flip then !facingRight else facingRight
            facingRight = horizontalFlipped ^ facingRight;
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
                    horizontalFlipped, assetWidth, assetHeight);
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
            birdData[ii] = data;
        }
    }

    private void processNewLightning(ArrayList<JsonValue> rawData){
        lightningData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < lightningData.length; ii++) {
            lightningData[ii] = processNewLightning(rawData.get(ii));
        }
    }

    /**
     * processes a single animated lightning object into JSON data
     * @param rawData unproecessed
     */
    private JsonValue processNewLightning(JsonValue rawData){
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
        data.addChild("strike_duration", new JsonValue(getFromProperties(props, "strike_timer", lightningProps).asInt()));
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
        int frameCount = getFrameCount(tileJson,"lightning");
        boolean horizontalFlipped = isObjectHorizontallyFlipped(rawData);
        data.addChild("flipped", new JsonValue(horizontalFlipped));
        JsonValue hitboxes = new JsonValue(JsonValue.ValueType.array);
        for (int ii = gid - frameCount; ii < gid; ii++){
            JsonValue hitBoxPoints = gameObjectTiles.get(ii).get("objectgroup").get("objects").get(0);
            float ox = hitBoxPoints.getFloat("x");
            float oy = hitBoxPoints.getFloat("y");
            JsonValue shape = processAssetHitBox( hitBoxPoints.get("polygon"), temp.set(ox,oy), scalars,
                    horizontalFlipped, assetWidth, assetHeight);
            hitboxes.addChild(shape);
        }
        data.addChild("hitboxes", hitboxes);
        return data;
    }

    /**
     * deprecated
     * @param rawData deprecated
     */
    private void processLightningOld(ArrayList<JsonValue> rawData){
        lightningData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < lightningData.length; ii++) {
            //data we pass in to lightning constructor
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //lightning raw data
            JsonValue l = rawData.get(ii);
            // set position data
            readPositionAndConvert(l, temp);
            addPosition(data, temp);
            JsonValue props = l.get("properties");
            data.addChild("points", polyPoints(l.get("polygon"), lightningDefaultPoly));
            data.addChild("strike_timer", new JsonValue(getFromProperties(props, "strike_timer", lightningDefault).asInt()));
            data.addChild("strike_timer_offset", new JsonValue(getFromProperties(props, "strike_timer_offset", lightningDefault).asInt()));
            data.addChild("depth", new JsonValue(l.getInt("__DEPTH__", -1)));
            lightningData[ii] = data;
        }
    }

    private void processPlatforms(ArrayList<JsonValue> rawData){
        platformData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < platformData.length; ii++) {
            //data we pass in to platform constructor
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //platform raw data
            JsonValue p = rawData.get(ii);
            // set position data
            readPositionAndConvert(p, temp);
            addPosition(data, temp);
            data.addChild("points", polyPoints(p.get("polygon"), platformDefaultPoly));
            data.addChild("depth", new JsonValue(p.getInt("__DEPTH__", -1)));
            platformData[ii] = data;
        }
    }

    private void processStaticHazards(ArrayList<JsonValue> rawData){
        staticHazardData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < staticHazardData.length; ii++) {
            //data we pass in to static hazard constructor
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            //static hazard raw data
            JsonValue sh = rawData.get(ii);
            // set position data
            readPositionAndConvert(sh, temp);
            addPosition(data, temp);
            data.addChild("points", polyPoints(sh.get("polygon"), staticHazardDefaultPoly));
            data.addChild("depth", new JsonValue(sh.getInt("__DEPTH__", -1)));
            staticHazardData[ii] = data;
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
            //points
            data.addChild("dimensions", polyPoints(w.get("polygon"), windDefaultPoly));
            //magnitude and direction
            JsonValue props = w.get("properties");
            data.addChild("magnitude", new JsonValue(getFromProperties(props, "magnitude", windDefault).asFloat()));
            data.addChild("direction", computeWindDirection(props, windDirs));
            data.addChild("depth", new JsonValue(w.getInt("__DEPTH__", -1)));
            windData[ii] = data;
        }
    }

    private void processMovingPlats(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory){
        movingPlatformData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < movingPlatformData.length; ii++) {
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
                    horizontalFlipped, assetWidth, assetHeight);
            data.addChild("points", shape);
            data.addChild("flipped", new JsonValue(horizontalFlipped));
            movingPlatformData[ii] = data;
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
        pos.y /= tileScale.y;;
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

    // ============================= BEGIN TILED PARSING HELPERS =============================

    /**
     * @param gid raw grid tile id (possibly with flipping bits enabled)
     * @return texture (possibly null) for the corresponding gid
     */
    private TextureRegion getTileFromImages(long gid){
        // the Tiled ID is a 32-bit UNSIGNED integer
        // actual ID is the lower 28 bits of the Tiled ID
        int id = (int) (gid & LOWER28BITMASK);
        // Bit 32 is used for storing whether the tile is horizontally flipped
        // Bit 31 is used for storing whether the tile is vertically flipped
        // Bit 30 is used for storing whether the tile is diagonally flipped
        boolean flipX = (gid & (1L << 31)) != 0;
        boolean flipY = (gid & (1L << 30)) != 0;
        boolean flipD = (gid & (1L << 29)) != 0;
        // this loop should be fast with small number of tilesets
        for (TileSetMaker tsm : tileSetMakers) {
            if (tsm.contains(id)) return tsm.getRegionFromId(id, flipD, flipX, flipY);
        }
        return null;
    }

    private void parseTileLayer(JsonValue layer){
        // loop over array data and make texture regions
        JsonValue data = layer.get("data");
        TextureRegion[] textures = new TextureRegion[data.size];
        for (int i = 0; i < textures.length; i++){
            // the Tiled ID is a 32-bit UNSIGNED integer
            long rawId = data.get(i).asLong();
            if (rawId == 0){
                continue;
            }
            int col = i % (int) worldSize.x;
            int row = (int) worldSize.y - 1 -  i / (int) worldSize.x;
            int idx = row * (int) worldSize.x + col;
            textures[idx] = getTileFromImages(rawId);
        }
        layers.add(textures);
    }

    /**
     * Given the relative path of a tileset (that can be used for tile layers), find the Json Data that corresponds to
     * the tileset used. Example: level data contains "source":"tilesets\/bushes.json" so bushes JSON is returned.
     * @param name the source path of a tileset
     * @return the tileset JSON (possibly null)
     */
    private JsonValue getTileLayerTileSetJson(String name){
        if (name.endsWith("bushes.json")){
            return tileSetJsonMap.get("bushes");
        }
        else if (name.endsWith("trees.json")){
            return tileSetJsonMap.get("trees");
        }
        else if (name.endsWith("cliffs.json")){
            return tileSetJsonMap.get("cliffs");
        }
        return null;
    }

    /**
     * A TileSetMaker produces texture regions upon request.
     * This class is useful when converting Tile IDs into textures.
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

        /**
         * for single image tilesets: returns a subregion of the texture<br>
         * for collection-based tilesets: returns a complete texture from set
         * @param id the associated id of the desired Tile, where contains(id) is true.
         * @param flipD whether to flip the resulting region anti-diagonally (not necessarily supported)
         * @param flipX whether to flip the resulting region horizontally
         * @param flipY whether to flip the resulting region vertically
         * @return a texture from the tile set corresponding to the given id
         */
        public abstract TextureRegion getRegionFromId(int id, boolean flipD, boolean flipX, boolean flipY);
    }

    /**
     * A ImageTileSetMaker produces texture regions upon request by cutting texture regions from a single texture.
     */
    private class ImageTileSetMaker extends TileSetMaker {
        private final int columns;
        private final Texture texture;

        /** flipped variant */
        private final Texture textureVariant;
        private final  int width;
        private final int height;

        ImageTileSetMaker(JsonValue tileSetJson, int firstGid){
            minId = firstGid;
            maxId = tileSetJson.getInt("tilecount") - 1 + minId;
            String name = tileSetJson.getString("name");
            texture = textureMap.get(name);
            textureVariant = textureMap.get(name + "_flipped");
            // removes flickering on square tiles
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textureVariant.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            width = tileSetJson.getInt("tilewidth");
            height = tileSetJson.getInt("tileheight");
            columns = tileSetJson.getInt("columns");
        }

        public TextureRegion getRegionFromId(int id, boolean flipD, boolean flipX, boolean flipY){
            int index = id - minId;
            int row = index / columns;
            int col = index % columns;
            TextureRegion tile;
            if (flipD){
                tile = new TextureRegion(textureVariant, col * width, row * height, width, height);
            }
            else {
                tile = new TextureRegion(texture, col * width, row * height, width, height);
            }
            tile.flip(flipX, flipY);
            return tile;
        }
    }

    /**
     * A CollectionTileSetMaker produces texture regions upon request by retrieving texture regions from a list of
     * textures. This is particularly useful for retrieving unrelated textures (stickers).
     */
    private static class CollectionTileSetMaker extends TileSetMaker {

        private final Texture[] collection;
        CollectionTileSetMaker(Texture[] collection, int firstGid){
            minId = firstGid;
            maxId = collection.length - 1 + minId;
            this.collection = collection;
        }

        public TextureRegion getRegionFromId(int id, boolean flipD, boolean flipX, boolean flipY) {
            TextureRegion tile = new TextureRegion(collection[id - minId]);
            tile.flip(flipX, flipY);
            return tile;
        }
    }

    // ========================== END of FUNCTIONS for TILE PARSING =================================

    /**
     *
     * @param tile the tile of the game object
     * @param type the type of game object (bird, lightning, etc)
     * @return the number of frames this object has for animations
     */
    private int getFrameCount(JsonValue tile, String type) {
        JsonValue animation = tile.get("animation");
        if (animation == null){
            return 0;
        }
        if (type.equals("lightning")){
            // an additional frame is included to allow easy tracing of still frame
            return animation.size - 1;
        }
        return animation.size;
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
        System.out.println(templateName);
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