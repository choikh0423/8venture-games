package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.obstacle.BoxObstacle;

public class UmbrellaModel extends BoxObstacle {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The factor to multiply by the input */
    private final float force;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;
    /** Which direction is the umbrella facing */
    public boolean faceRight;
    /** The current angular rotation of the umbrella */
    private float turning;
    /** The scale to multiply the texture by for drawing */
    private float textureScale;

    public UmbrellaModel(JsonValue data, float width, float height) {
        super(	data.get("pos").getFloat(0),
                data.get("pos").getFloat(1),
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(false);
        setGravityScale(0);

        force = data.getFloat("force", 0);
        textureScale = data.getFloat("texturescale", 1.0f);
        sensorName = "UmbrellaSensor";
        this.data = data;
        faceRight = true;
        setName("umbrella");
    }

    /** Returns the angle away from the x-axis of the umbrella in radians.
     * THIS IS THE VALUE YOU SHOULD USE FOR WIND INTERACTIONS, ETC. */
    public float getRotation(){return getAngle()+(float)Math.PI/2;}

    public float getForce(){return force;}

    /** Sets how much this umbrella is turning
     * @param value left/right turning of this umbrella.*/
    public void setTurning(float value){turning = value;}

    /** Applies the force to the body of this umbrella */
    public void applyForce(){
        if (!isActive()) {
            return;
        }

        //stop turning if not turning
        if (turning == 0f) setAngularVelocity(0);
        //clamp angular velocity
        if (Math.abs(getAngularVelocity()) > (float)2*Math.PI)
            setAngularVelocity(Math.signum(getAngularVelocity())*2*(float)Math.PI);
        else body.applyTorque(turning, true);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*textureScale,textureScale);
    }
}
