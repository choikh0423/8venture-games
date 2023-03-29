package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

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

    /** vector position cache for player */
    private Vector2 playerPos = new Vector2();

    /** vector position cache for goal */
    private Vector2 goalPos = new Vector2();

    /** vector cache for world size (width, height) */
    private Vector2 worldSize = new Vector2();

    /** vector cache for tile scaling (x_scale, y_scale) */
    private Vector2 tileScale = new Vector2();

    private Vector2 temp = new Vector2();

    private JsonValue redBirdTemplate;
    private JsonValue blueBirdTemplate;
    private JsonValue brownBirdTemplate;

    // add more templates

    public LevelParser(JsonValue constants, JsonValue assets){
        globalConstants = constants;
        globalAssets = assets;
        // TODO: ideally load the templates here... for now, we will hard code.
        blueBirdTemplate = new JsonValue(JsonValue.ValueType.object);
        redBirdTemplate = new JsonValue(JsonValue.ValueType.object);
        brownBirdTemplate = new JsonValue(JsonValue.ValueType.object);
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

        JsonValue layers = levelData.get("layers");
        // flatten all layers (all object layers are put together)
        // - hazards and obstacle data are placed into their respective containers
        // - processing begins after all data is collected.
        for (JsonValue layer : layers) {
            String layerName = layer.getString("type", "");
            if (layerName.equals("objectgroup")){
                parseObjectLayer(layer, trajectory, birdRawData, lightningRawData, platformRawData, windRawData);
            }
            else if (layerName.equals("PLACEHOLDER")){
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
    }

    /**
     * parse all relevant object data in the given object layer by categorizing/grouping raw data.
     */
    private void parseObjectLayer(JsonValue layer, HashMap<Integer, JsonValue> trajectory,
                                  ArrayList<JsonValue> birdRawData,
                                  ArrayList<JsonValue> lightningRawData,
                                  ArrayList<JsonValue> platformRawData,
                                  ArrayList<JsonValue> windRawData)
    {
        JsonValue objs = layer.get("objects");
        for (JsonValue obj : objs) {
            if (obj.getString("template").contains("bird.tx")) {
                birdRawData.add(obj);
            } else if (obj.getString("template").contains("platform.tx")) {
                platformRawData.add(obj);
            } else if (obj.getString("template").contains("lightning.tx")) {
                lightningRawData.add(obj);
            } else if (obj.getString("template").contains("path_point.tx")) {
                trajectory.put(obj.getInt("id"), obj);
            } else if (obj.getString("template").contains("spawn.tx")) {
                playerPos.x = obj.getFloat("x");
                playerPos.y = obj.getFloat("y");
            } else if (obj.getString("template").contains("goal.tx")) {
                goalPos.x = obj.getFloat("x");
                goalPos.y = obj.getFloat("y");
            }
            //TODO: get wind and put into windRawData
        }
    }

    /**
     * Convert raw bird JSON into level-container expected JSON format.
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
            addPosition(data, temp );
            // the resulting path should be stored as a list of floats which is Json array of Json floats.
            JsonValue pathJson = new JsonValue(JsonValue.ValueType.array);
            // implicitly, the bird's location is the FIRST point on their path.
            pathJson.addChild(new JsonValue(temp.x));
            pathJson.addChild(new JsonValue(temp.y));

            if (variant.equals("blue_bird.tx") || variant.equals("brown_bird.tx")){
                // using custom properties to find rest of path
                JsonValue properties = b.get("properties");
                // custom properties should exist for patrolling birds
                // if not, then grab default values
                if (properties == null) {
                    properties = getBirdDefaults(color, "properties");
                }

                HashSet<Integer> seen = new HashSet<>();
                JsonValue jsonId = getFromProperties(properties, "starting_point");
                // (null) maybe the bird wasn't linked to a starting point, use defaults for safety.
                int next = jsonId == null ? getBirdDefaults(color, "starting_point").asInt() : jsonId.asInt();
                while (next != 0 && !seen.contains(next) && trajectory.get(next) != null) {
                    seen.add(next);
                    JsonValue nodeData = trajectory.get(next);
                    // put path point (x,y) into vector cache and perform conversion
                    readPositionAndConvert(nodeData, temp);
                    // add this node to bird's path
                    pathJson.addChild(new JsonValue(temp.x));
                    pathJson.addChild(new JsonValue(temp.y));
                    // get next
                    jsonId = getFromProperties(nodeData.get("properties"), "next_trajectory");
                    next = jsonId == null ? 0 : jsonId.asInt();
                }
            }
            data.addChild("path", pathJson);
            birdData[ii] = data;
        }
    }

    private void processLightning(ArrayList<JsonValue> rawData){
        lightningData = new JsonValue[rawData.size()];
        for (JsonValue l : rawData) {
            //TODO: process lightning and put in this.lightnings
        }
    }

    private void processPlatforms(ArrayList<JsonValue> rawData){
        platformData = new JsonValue[rawData.size()];
        for (JsonValue p : rawData) {
            //TODO: process platforms and put in this.objects
            // DO THIS ONE
            JsonValue j = new JsonValue(JsonValue.ValueType.object);
        }
    }

    private void parseTileLayer(JsonValue layer){
        //TODO: parse tiles/tilesets
    }

    /**
     * loads into p an (x,y) pair that are direct properties of the given JsonValue into vector cache
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
        switch (variant){
            case "blue_bird.tx":
                return "blue";
            case "brown_bird.tx":
                return "brown";
            case "red_bird.tx":
                return "red";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * returns a default value (custom properties)
     * @param color the color of the bird
     * @param key the requested valid property
     * @return default JsonValue for the requested property and the given bird variant
     */
    private JsonValue getBirdDefaults(String color, String key){
        JsonValue properties = null;
        switch (color){
            case "blue":
                properties = blueBirdTemplate.get("properties");
                break;
            case "brown":
                properties = brownBirdTemplate.get("properties");
                break;
            default:
                // should be red
                properties = redBirdTemplate.get("properties");
                break;
        }
        // whether the entire collection of default properties or a specific one?
        if (key.equals("properties")){
            return properties;
        }
        return getFromProperties(properties, key);
    }
}

