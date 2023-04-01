package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.assets.AssetDirectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LevelParser {

    /** the Tiled-JSON raw value that was previously parsed.
     * This saves parsing time when a level is played immediately again.
     */
    private JsonValue prevParsed;

    /** reference to game global default values */
    private JsonValue globalConstants;

    /** something we will use*/
    private JsonValue globalAssets;

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

    /** vector position cache for player */
    private Vector2 playerPos = new Vector2();

    /** vector position cache for goal */
    private Vector2 goalPos = new Vector2();

    /** vector cache for world size (width, height) */
    private Vector2 worldSize = new Vector2();

    /** vector cache for tile scaling (x_scale, y_scale) */
    private Vector2 tileScale = new Vector2();

    private Vector2 temp = new Vector2();

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
    // add more template defaults

    // TODO: add getter/setters

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

    public LevelParser(AssetDirectory directory){
        globalConstants = directory.getEntry("global:constants", JsonValue.class);
        // TODO: assets?
        globalAssets = null;

        JsonValue redBirdTemplate = directory.getEntry("red_bird:template", JsonValue.class);
        JsonValue blueBirdTemplate = directory.getEntry("blue_bird:template", JsonValue.class);
        JsonValue brownBirdTemplate = directory.getEntry("brown_bird:template", JsonValue.class);
        JsonValue pathPointTemplate = directory.getEntry("path_point:template", JsonValue.class);
        JsonValue lightningTemplate = directory.getEntry("lightning:template", JsonValue.class);
        JsonValue platformTemplate = directory.getEntry("platform:template", JsonValue.class);
        JsonValue staticHazardTemplate = directory.getEntry("static_hazard:template", JsonValue.class);

        redBirdDefaults = redBirdTemplate.get("object").get("properties");
        blueBirdDefaults = blueBirdTemplate.get("object").get("properties");
        brownBirdDefaults = brownBirdTemplate.get("object").get("properties");
        pointDefault = pathPointTemplate.get("object").get("properties");
        lightningDefault = lightningTemplate.get("object").get("properties");
        lightningDefaultPoly = lightningTemplate.get("object").get("polygon");
        platformDefaultPoly = platformTemplate.get("object").get("polygon");
        staticHazardDefaultPoly = staticHazardTemplate.get("object").get("polygon");
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

        // containers for unprocessed JSON data
        HashMap<Integer, JsonValue> trajectory = new HashMap<>();
        ArrayList<JsonValue> birdRawData = new ArrayList<>();
        ArrayList<JsonValue> platformRawData = new ArrayList<>();
        ArrayList<JsonValue> lightningRawData = new ArrayList<>();
        ArrayList<JsonValue> windRawData = new ArrayList<>();
        ArrayList<JsonValue> staticHazardRawData = new ArrayList<>();

        JsonValue layers = levelData.get("layers");
        // flatten all layers (all object layers are put together)
        // - hazards and obstacle data are placed into their respective containers
        // - processing begins after all data is collected.
        for (JsonValue layer : layers) {
            String layerName = layer.getString("type", "");
            if (layerName.equals("objectgroup")){
                parseObjectLayer(layer, trajectory, birdRawData, lightningRawData, platformRawData, windRawData, staticHazardRawData);
            }
            else if (layerName.equals("tilelayer")){
                parseTileLayer(layer);
            }
            else {
                System.err.println("LEVEL DATA JSON FORMATTING VIOLATED");
            }
        }

        // begin processing
        processBirds(birdRawData, trajectory);
        processLightning(lightningRawData);
        processPlatforms(platformRawData);
        processStaticHazards(staticHazardRawData);
    }

    /**
     * parse all relevant object data in the given object layer by categorizing/grouping raw data.
     */
    private void parseObjectLayer(JsonValue layer, HashMap<Integer, JsonValue> trajectory,
                                  ArrayList<JsonValue> birdRawData,
                                  ArrayList<JsonValue> lightningRawData,
                                  ArrayList<JsonValue> platformRawData,
                                  ArrayList<JsonValue> windRawData,
                                  ArrayList<JsonValue> staticHazardRawData)
    {
        JsonValue objs = layer.get("objects");
        for (JsonValue obj : objs) {
            if (obj.getString("template").contains("bird.json")) {
                birdRawData.add(obj);
            } else if (obj.getString("template").contains("platform.json")) {
                platformRawData.add(obj);
            } else if (obj.getString("template").contains("lightning.json")) {
                lightningRawData.add(obj);
            } else if (obj.getString("template").contains("path_point.json")) {
                trajectory.put(obj.getInt("id"), obj);
            } else if (obj.getString("template").contains("spawn.json")) {
                readPositionAndConvert(obj, playerPos);
            } else if (obj.getString("template").contains("goal.json")) {
                readPositionAndConvert(obj, goalPos);
            } else if (obj.getString("template").contains("static_hazard.json")){
                staticHazardRawData.add(obj);
            }
            //TODO: get wind and put into windRawData
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
            // add whether facing right
            data.addChild("facing_right", new JsonValue(getFromProperties(properties, "facing_right", defaults).asBoolean()));

            if (color.equals("blue") || color.equals("brown")){
                // add whether to loop
                data.addChild("loop", getFromProperties(properties, "loop", defaults));
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
            data.addChild("path", pathJson);
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
            platformData[ii] = data;
        }
    }

    private void parseTileLayer(JsonValue layer){
        //TODO: parse tiles/tilesets
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
            temp.x = j.getFloat("x", 0);
            temp.y = -j.getFloat("y", 0);
            points.addChild(new JsonValue(temp.x));
            points.addChild(new JsonValue(temp.y));
        }
        return points;
    }
}