package com.mygdx.game.hazard;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.obstacle.PolygonObstacle;

/**
 * A model for hazard objects.
 */
public abstract class HazardModel extends PolygonObstacle {
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;

    /**
     * The damage done to the player when colliding with this hazard
     */
    private int damage;

    /**
     * Returns this hazard's damage
     * @return This hazard's damage
     */
    public int getDamage(){
        return damage;
    }

    public HazardModel(JsonValue data) {
        super(data.get("points").asFloatArray(), data.getFloat("x"), data.getFloat("y"));
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        this.data = data;
    }
}

