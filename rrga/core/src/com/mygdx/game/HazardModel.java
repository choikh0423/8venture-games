package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.obstacle.PolygonObstacle;

/**
 * A model for hazard objects.
 */
public class HazardModel extends PolygonObstacle {
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;

    public HazardModel(JsonValue data) {
        super(data.get(0).asFloatArray(), data.getFloat(1), data.getFloat(2));
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        this.data = data;
    }
}

