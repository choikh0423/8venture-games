package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.assets.AssetDirectory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

    /** the texture data of the tile layers
     * Invariant: front layers are stored last in list
     */
    private ArrayList<TextureRegion[]> layers;

    /** vector position cache for player */
    private final Vector2 playerPos = new Vector2();

    /** vector position cache for goal */
    private final Vector2 goalPos = new Vector2();

    /** vector cache for world size (width, height) */
    private final Vector2 worldSize = new Vector2();

    /** vector cache for tile scaling (x_scale, y_scale) */
    private final Vector2 tileScale = new Vector2();

    private final Vector2 temp = new Vector2();

    /** default values for colored birds*/
    private final JsonValue redBirdDefaults;
    private final JsonValue blueBirdDefaults;
    private final JsonValue brownBirdDefaults;

    /** the default JSON of path point. */
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

    /** the default direction of a wind object */
    private final float windDirDefault = 0;

    private JsonValue birdPoints = new JsonValue(JsonValue.ValueType.array);

    private final HashMap<String, Texture> textureMap;

    private final HashMap<String, JsonValue> tileSetJsonMap;

    /** the list of texture region cutters, one for each tileset */
    private ArrayList<TileSetMaker> tileSetMakers;

    private static final int LOWER28BITMASK = 0xFFFFFFF;


    /**
     * A TileSetMaker produces texture regions upon request.
     *
     * This class is useful when converting Tile IDs into textures.
     */
    private class TileSetMaker {

        /** the tile ID assigned to the FIRST tile in this set*/
        public int minId;
        /** the tile ID assigned to the LAST tile in this set*/
        public int maxId;
        private int columns;
        private Texture texture;

        private Texture texture_variant;
        private int width;
        private int height;

        TileSetMaker(JsonValue tileSetJson, int firstGid){
            if (tileSetJson == null){
                System.err.println("Unrecognized Tileset");
                return;
            }
            minId = firstGid;
            maxId = tileSetJson.getInt("tilecount") - 1 + minId;
            String name = tileSetJson.getString("name");
            texture = textureMap.get(name);
            texture_variant = textureMap.get(name + "_flipped");
            // removes flickering on square tiles
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (texture_variant != null){
                texture_variant.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            width = tileSetJson.getInt("tilewidth");
            height = tileSetJson.getInt("tileheight");
            columns = tileSetJson.getInt("columns");
        }

        /**
         * cuts out a region of the texture tileset
         * @param id the associated Id of the desired Tile, where minId <= id <= maxId
         * @param flipD whether to flip the resulting region anti-diagonally
         * @param flipX whether to flip the resulting region horizontally
         * @param flipY whether to flip the resulting region vertically
         * @return a region of the entire texture corresponding to the given Id
         */
        TextureRegion getRegionFromId(int id, boolean flipD, boolean flipX, boolean flipY){
            int index = id - minId;
            int row = index / columns;
            int col = index % columns;
            TextureRegion tile;
            if (flipD){
                tile = new TextureRegion(texture_variant, col * width, row * height, width, height);
            }
            else {
                tile = new TextureRegion(texture, col * width, row * height, width, height);
            }
            tile.flip(flipX, flipY);
            return tile;
        }
    }

    // add more template defaults

    // TODO: add getter/setters


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

    public LevelParser(AssetDirectory directory){
        globalConstants = directory.getEntry("global:constants", JsonValue.class);

        JsonValue redBirdTemplate = directory.getEntry("red_bird:template", JsonValue.class);
        JsonValue blueBirdTemplate = directory.getEntry("blue_bird:template", JsonValue.class);
        JsonValue brownBirdTemplate = directory.getEntry("brown_bird:template", JsonValue.class);
        JsonValue pathPointTemplate = directory.getEntry("path_point:template", JsonValue.class);
        JsonValue lightningTemplate = directory.getEntry("lightning:template", JsonValue.class);
        JsonValue platformTemplate = directory.getEntry("platform:template", JsonValue.class);
        JsonValue staticHazardTemplate = directory.getEntry("static_hazard:template", JsonValue.class);
        JsonValue windTemplate = directory.getEntry("wind:template", JsonValue.class);

        redBirdDefaults = redBirdTemplate.get("object").get("properties");
        blueBirdDefaults = blueBirdTemplate.get("object").get("properties");
        brownBirdDefaults = brownBirdTemplate.get("object").get("properties");
        pointDefault = pathPointTemplate.get("object").get("properties");
        lightningDefault = lightningTemplate.get("object").get("properties");
        lightningDefaultPoly = lightningTemplate.get("object").get("polygon");
        platformDefaultPoly = platformTemplate.get("object").get("polygon");
        staticHazardDefaultPoly = staticHazardTemplate.get("object").get("polygon");
        windDefault = windTemplate.get("object").get("properties");
        windDefaultPoly = windTemplate.get("object").get("polygon");

        //temporarily made every bird have the same hitbox
        birdPoints.addChild(new JsonValue(0.0f));
        birdPoints.addChild(new JsonValue(0.0f));
        birdPoints.addChild(new JsonValue(0.5f));
        birdPoints.addChild(new JsonValue(0.0f));
        birdPoints.addChild(new JsonValue(0.5f));
        birdPoints.addChild(new JsonValue(0.5f));
        birdPoints.addChild(new JsonValue(0.0f));
        birdPoints.addChild(new JsonValue(0.5f));

        // save tileset textures and their corresponding JSON tileset data
        textureMap = new HashMap<>();
        tileSetJsonMap = new HashMap<>();
        textureMap.put("bushes", directory.getEntry( "tileset:bushes", Texture.class ));
        textureMap.put("bushes_flipped", directory.getEntry("tileset:bushes_flipped", Texture.class));
        textureMap.put("trees", directory.getEntry( "tileset:trees", Texture.class ));
        textureMap.put("trees_flipped", directory.getEntry("tileset:trees_flipped", Texture.class));
        textureMap.put("cliffs", directory.getEntry( "tileset:cliffs", Texture.class ));
        textureMap.put("cliffs_flipped", directory.getEntry("tileset:cliffs_flipped", Texture.class));
        tileSetJsonMap.put("bushes", directory.getEntry("data:bushes", JsonValue.class));
        tileSetJsonMap.put("trees", directory.getEntry("data:trees", JsonValue.class));
        tileSetJsonMap.put("cliffs", directory.getEntry("data:cliffs", JsonValue.class));
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

        layers = new ArrayList<>();
        // prepare texture/tileset parsing, get all tilesets used by current level
        // properly formatted raw data should have tilesets ordered by IDs so this guarantees sorted order.
        tileSetMakers = new ArrayList<>();
        JsonValue tileSets = levelData.get("tilesets");
        for (JsonValue ts : tileSets){
            String source = ts.getString("source");
            if (source.contains("objs.json")){
                continue;
            }
            tileSetMakers.add(
                    new TileSetMaker(getTileSetJson(source), ts.getInt("firstgid"))
            );
        }

        // containers for unprocessed JSON data
        HashMap<Integer, JsonValue> trajectory = new HashMap<>();
        ArrayList<JsonValue> birdRawData = new ArrayList<>();
        ArrayList<JsonValue> platformRawData = new ArrayList<>();
        ArrayList<JsonValue> lightningRawData = new ArrayList<>();
        ArrayList<JsonValue> windRawData = new ArrayList<>();
        HashMap<Integer, JsonValue> windDirs = new HashMap<>();
        ArrayList<JsonValue> staticHazardRawData = new ArrayList<>();

        JsonValue rawLayers = levelData.get("layers");
        // flatten all layers (all object layers are put together)
        // - hazards and obstacle data are placed into their respective containers
        // - processing begins after all data is collected.
        for (JsonValue layer : rawLayers) {
            String layerName = layer.getString("type", "");
            if (layerName.equals("objectgroup")){
                parseObjectLayer(layer, trajectory, birdRawData, lightningRawData, platformRawData, windRawData, windDirs, staticHazardRawData);
            }
            else if (layerName.equals("tilelayer")){
                parseTileLayer(layer);
            }
            else {
                System.err.println("LEVEL DATA JSON FORMATTING VIOLATED");
            }
        }

        // begin object processing
        processBirds(birdRawData, trajectory);
        processLightning(lightningRawData);
        processPlatforms(platformRawData);
        processStaticHazards(staticHazardRawData);
        processWind(windRawData, windDirs);
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
                                  ArrayList<JsonValue> staticHazardRawData)
    {
        JsonValue objs = layer.get("objects");
        for (JsonValue obj : objs) {
            String template = obj.getString("template", "IGNORE");
            if (template.equals("IGNORE")){
                continue;
            }
            if (template.contains("bird.json")) {
                birdRawData.add(obj);
            } else if (template.contains("platform.json")) {
                platformRawData.add(obj);
            } else if (template.contains("lightning.json")) {
                lightningRawData.add(obj);
            } else if (template.contains("path_point.json")) {
                trajectory.put(obj.getInt("id"), obj);
            } else if (template.contains("spawn.json")) {
                readPositionAndConvert(obj, playerPos);
            } else if (template.contains("goal.json")) {
                readPositionAndConvert(obj, goalPos);
            } else if (template.contains("static_hazard.json")){
                staticHazardRawData.add(obj);
            } else if (template.contains("wind.json")){
                windRawData.add(obj);
            } else if (template.contains("wind_dir.json")){
                windDirs.put(obj.getInt("id"), obj);
            }
        }
    }

    /**
     * Convert raw bird JSON into game-expected JSON format.
     * @param rawData the unprocessed bird object data
     * @param trajectory map of path node Ids to raw JSON
     */
    private void processBirds(ArrayList<JsonValue> rawData, HashMap<Integer, JsonValue> trajectory){
        birdData = new JsonValue[rawData.size()];
        for (int ii = 0; ii < birdData.length; ii++) {
            // data = the bird JSON that game will read
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            // b = raw bird data
            JsonValue b = rawData.get(ii);
            String variant = b.getString("template", "UNKNOWN");
            String color = computeColor(variant);
            // set deterministic trivial properties
            data.addChild("color", new JsonValue(color));
            data.addChild("attack", new JsonValue(color.equals("brown") || color.equals("red")));
            // set position data
            readPositionAndConvert(b, temp);
            addPosition(data, temp);
            // the resulting path should be stored as a list of floats which is Json array of Json floats.
            JsonValue pathJson = new JsonValue(JsonValue.ValueType.array);
            // implicitly, the bird's location is the FIRST point on their path.
            pathJson.addChild(new JsonValue(temp.x));
            pathJson.addChild(new JsonValue(temp.y));

            JsonValue properties = b.get("properties");
            JsonValue defaults = getBirdDefaults(color);
            boolean loop = false;
            float movespeed = 0;
            float atkspeed = 0;
            // add whether facing right
            data.addChild("facing_right", new JsonValue(getFromProperties(properties, "facing_right", defaults).asBoolean()));

            if (color.equals("blue") || color.equals("brown")){
                // update properties
                loop = getFromProperties(properties, "loop", defaults).asBoolean();
                movespeed = getFromProperties(properties, "move_speed", defaults).asFloat();
                // using custom properties to find rest of path
                HashSet<Integer> seen = new HashSet<>();
                // this takes either the bird's next point along its path or take from default (which should be 0)
                JsonValue jsonId = getFromProperties(properties, "path", defaults);
                int next = jsonId.asInt();
                while (next != 0 && !seen.contains(next) && trajectory.get(next) != null) {
                    seen.add(next);
                    JsonValue nodeData = trajectory.get(next);
                    // put path point (x,y) into vector cache and perform conversion
                    readPositionAndConvert(nodeData, temp);
                    // add this node to bird's path
                    pathJson.addChild(new JsonValue(temp.x));
                    pathJson.addChild(new JsonValue(temp.y));
                    // get next
                    nodeData = nodeData.get("properties");
                    jsonId = getFromProperties(nodeData, "next_trajectory", pointDefault);
                    next = jsonId.asInt();
                }
            }
            if (color.equals("brown") || color.equals("red")){
                atkspeed = getFromProperties(properties, "atk_speed", defaults).asFloat();
            }
            data.addChild("path", pathJson);
            data.addChild("loop", new JsonValue(loop));
            data.addChild("movespeed", new JsonValue(movespeed));
            data.addChild("atkspeed", new JsonValue(atkspeed));
            data.addChild("points", birdPoints);
            birdData[ii] = data;
        }
    }

    private void processLightning(ArrayList<JsonValue> rawData){
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
            windData[ii] = data;
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
     * maps bird template name to color
     * @param variant the template name of a colored bird
     * @return the bird's color
     */
    private String computeColor(String variant){
        if (variant.contains("blue_bird.json")) return "blue";
        else if (variant.contains("brown_bird.json")) return "brown";
        else if (variant.contains("red_bird.json")) return "red";
        else return "UNKNOWN";
    }

    /**
     * returns the defaults (the entire custom properties)
     * @param color the color of the bird
     * @return default JsonValue for the given bird variant
     */
    private JsonValue getBirdDefaults(String color){
        switch (color){
            case "blue":
                return blueBirdDefaults;
            case "brown":
                return brownBirdDefaults;
            default:
                // should be red
                return redBirdDefaults;
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

    // ============================= BEGIN TILED LAYER PARSING HELPERS =============================
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

            // actual ID is the lower 28 bits of the Tiled ID
            int id = (int) (rawId & LOWER28BITMASK);
            // Bit 32 is used for storing whether the tile is horizontally flipped
            // Bit 31 is used for the vertically flipped tiles
            boolean flipX = (rawId & (1L << 31)) != 0;
            boolean flipY = (rawId & (1L << 30)) != 0;
            boolean flipD = (rawId & (1L << 29)) != 0;

            // this loop should be fast with small number of tilesets
            for (TileSetMaker tsm : tileSetMakers) {
                if (id <= tsm.maxId && id >= tsm.minId) {
                    int col = i % (int) worldSize.x;
                    int row = (int) worldSize.y - 1 -  i / (int) worldSize.x;
                    int idx = row * (int) worldSize.x + col;
                    textures[idx] = tsm.getRegionFromId(id, flipD, flipX, flipY);
                    break;
                }
            }
        }
        layers.add(textures);
    }

    /**
     * Given the source name of a tileset, which is a relative path, find the Json Data that corresponds to the tileset
     * used. Example: level data contains "source":"tilesets\/bushes.json" so bushes JSON is returned.
     * @param name the source path of a tileset
     * @return the tileset JSON
     */
    private JsonValue getTileSetJson(String name){
        if (name.contains("bushes")){
            return tileSetJsonMap.get("bushes");
        }
        else if (name.contains("trees")){
            return tileSetJsonMap.get("trees");
        }
        else if (name.contains("cliffs")){
            return tileSetJsonMap.get("cliffs");
        }
        return null;
    }


    // ========================================== END ==============================================
}