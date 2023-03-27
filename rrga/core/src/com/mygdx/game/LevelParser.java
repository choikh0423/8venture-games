package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.mygdx.game.hazard.BirdHazard;
import com.mygdx.game.hazard.LightningHazard;
import com.mygdx.game.obstacle.Obstacle;
import com.mygdx.game.util.PooledList;

import java.util.ArrayList;
import java.util.HashMap;

public class LevelParser {
    private JsonValue globalConstants;
    private JsonValue globalAssets;
    private ObjectSet<BirdHazard> birds;
    private ObjectSet<LightningHazard> lightnings;
    private PooledList<Obstacle> objects;
    private Vector2 playerPos = new Vector2();
    private Vector2 goalPos = new Vector2();
    public LevelParser(JsonValue constants, JsonValue assets){
        globalConstants = constants;
        globalAssets = assets;
    }
    //TODO: getters
    public void parseObjects(JsonValue dir) {
        birds.clear();
        lightnings.clear();
        objects.clear();
        JsonValue layers = dir.get("layers");
        JsonValue objs;
        ArrayList<JsonValue> birds = new ArrayList<JsonValue>();
        ArrayList<JsonValue> plats = new ArrayList<JsonValue>();
        ArrayList<JsonValue> lightnings = new ArrayList<JsonValue>();
        HashMap<Integer, JsonValue> trajectory = new HashMap<Integer, JsonValue>();
        //get objects
        for (JsonValue layer : layers) {
            if (!layer.get("type").equals("objectgroup")) continue; //only get objects from object layers
            objs = layer.get("objects");
            for (JsonValue obj : objs) {
                if (obj.getString("template").contains("bird.tx")) {
                    birds.add(obj);
                } else if (obj.getString("template").contains("platform.tx")) {
                    plats.add(obj);
                } else if (obj.getString("template").contains("lightning.tx")) {
                    lightnings.add(obj);
                } else if (obj.getString("template").contains("path_point.tx")) {
                    trajectory.put(obj.getInt("id"), obj);
                } else if (obj.getString("template").contains("spawn.tx")) {
                    playerPos.x = obj.getFloat("x");
                    playerPos.y = obj.getFloat("y");
                } else if (obj.getString("template").contains("goal.tx")) {
                    goalPos.x = obj.getFloat("x");
                    goalPos.y = obj.getFloat("y");
                }
                //TODO: get wind
            }
        }
        //process birds
        int next = 0;
        JsonValue start;
        for (JsonValue b : birds) {
            //TODO: initialize bird
            //get bird's path
            if (b.getString("template").equals("blue_bird.tx")) {
                if (b.get("properties") != null) {
                    next = b.get("properties").getInt("starting_point");
                    while (next != 0) {
                        start = trajectory.get(next);
                        //TODO: add this node to bird's path
                        next = start.getInt("next_trajectory");
                    }
                }
            } else if (b.getString("template").equals("red_bird.tx")) {
                if (b.get("properties") != null) {
                    start = trajectory.get(b.get("properties").getInt("starting_point"));
                    //TODO: add this node to bird's path
                }
            } else if (b.getString("template").equals("brown_bird.tx")) {
                if (b.get("properties") != null) {
                    next = b.get("properties").getInt("starting_point");
                    while (next != 0) {
                        start = trajectory.get(next);
                        //TODO: add this node to bird's path
                        next = start.getInt("next_trajectory");
                    }
                }
            }
            //TODO: put bird in this.birds
        }
        //process platforms
        for (JsonValue p : plats) {
            //TODO: process platforms and put in this.objects
        }
        //process lightning
        for (JsonValue l : lightnings) {
            //TODO: process lightning and put in this.lightnings
        }
    }
    public void parseTiles(){
        //TODO: parse tiles/tilesets
    }
}
