package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.obstacle.*;


/**
 * A model for wind objects.
 * Currently extends PolygonObstacle to allow for different shaped wind gusts, but may want to change later
 * to make drawing manageable/easier
 */
public class WindModel extends PolygonObstacle {
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;

    /**
     * The magnitude of this wind's force. Invariant: magnitude > 0.
     */
    private float magnitude;

    /**
     * The direction of this wind's force in radians. Value within [0, 2pi).
     */
    private float direction;

    public WindModel(JsonValue data) {
        super(data.get(0).asFloatArray(), data.getFloat(1), data.getFloat(2));
        magnitude = data.getFloat(3);
        direction = data.getFloat(4);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        fixture.isSensor = true;
        this.data = data;
    }

    /**
     * Returns a value which gives the magnitude of the force on the umbrella from the wind. value is >=0.
     */
    public float getWindForce(float umbrellaAngle){
        //may need to change the umbrella angle based up the value returned by umbrella.getRotation.
        //for now assuming value is within[0, 2pi).
        float windx = (float) Math.cos(direction);
        float windy = (float) Math.sin(direction);
        float umbrellax = (float) Math.cos(umbrellaAngle);
        float umbrellay = (float) Math.sin(umbrellaAngle);
        float dot = Vector2.dot(windx, windy, umbrellax, umbrellay);
        if (dot<0) return 0;
        else return dot*magnitude;
    }


}
