package com.mygdx.game;

import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.obstacle.CapsuleObstacle;

public class UmbrellaModel extends CapsuleObstacle {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;
    /** Which direction is the umbrella facing */
    private boolean faceRight;
    /**The angle of the umbrella*/
    private float angle;

    public UmbrellaModel(JsonValue data, float width, float height) {
        super(	data.get("pos").getFloat(0),
                data.get("pos").getFloat(1),
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(false);

        sensorName = "UmbrellaSensor";
        this.data = data;
        setOrientation(Orientation.TOP);
        faceRight = true;
        setName("umbrella");
    }

    /**Returns the angle away from the x-axis of the umbrella*/
    public float getAngle(){
        return angle;
    }
}
