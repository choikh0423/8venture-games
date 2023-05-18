package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;
import com.mygdx.game.utility.util.PooledList;
import com.sun.org.apache.bcel.internal.generic.DADD;

public class NestHazard extends PolygonObstacle implements Drawable {

    private final float[] path;
    private final int spawnDelay;

    /** blue bird json data for bird initializer*/
    private final JsonValue blueBirdData;

    private final float birdSpeed;

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    private final Vector2 temp = new Vector2();
    private final int drawDepth;

    public NestHazard(JsonValue nestData, JsonValue blueBirdData) {
        super(nestData.get("points").asFloatArray(), nestData.getFloat("x"), nestData.getFloat("y"));
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        setSensor(true);
        this.blueBirdData = blueBirdData;
        this.path = nestData.get("path").asFloatArray();
        birdSpeed = nestData.getFloat("bird_speed");
        spawnDelay = nestData.getInt("spawn_delay");

        // load Drawable-necessary information
        float[] aabb = nestData.get("AABB").asFloatArray();
        boxCoordinate.x = aabb[0];
        boxCoordinate.y = aabb[1];
        dimensions.x = aabb[2];
        dimensions.y = aabb[3];
        drawDepth = nestData.getInt("depth");

        // offset bird upwards so bird rests on nests
        this.path[1] = getY() + 0.5f * dimensions.y;
    }

    /**
     * NOTE: for correctness, this data needs to be consumed immediately because there's shared data between nests.
     * @return blue nested bird initializer data
     */
    public JsonValue getBirdInitializerData(){
        // update blue bird data with essential properties before returning
        // no need to set real speed. bird will be stationary until spawn is over.
        blueBirdData.remove("movespeed");
        blueBirdData.addChild("movespeed", new JsonValue(birdSpeed));
        blueBirdData.remove("x");
        blueBirdData.remove("y");
        blueBirdData.addChild("x", new JsonValue(path[0]));
        blueBirdData.addChild("y", new JsonValue(path[1]));
        blueBirdData.remove("depth");
        blueBirdData.addChild("depth", new JsonValue(drawDepth));
        return blueBirdData;
    }

    public float getBirdSpeed(){
        return this.birdSpeed;
    }

    /**
     * @return number of frames of immobility for bird
     */
    public int getSpawnDelay() {
        return spawnDelay;
    }

    /**
     * NOTE: this is a reference to path data. This is not an allocator.
     * @return path that the bird should follow, if at end of path: bird continues in the same direction.
     */
    public float[] getPath() {
        return path;
    }

    // DRAWABLE INTERFACE

    @Override
    public Vector2 getDimensions() {
        return temp.set(dimensions);
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(boxCoordinate).add(getX(), getY());
    }

    @Override
    public int getDepth() {
        return drawDepth;
    }

    public void draw(GameCanvas canvas){
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth() / 2f, texture.getRegionHeight() / 2f,
                getX() * drawScale.x, getY() * drawScale.y, 0,
                dimensions.x / texture.getRegionWidth() * drawScale.x,
                dimensions.y / texture.getRegionHeight() * drawScale.y);
    }
}
