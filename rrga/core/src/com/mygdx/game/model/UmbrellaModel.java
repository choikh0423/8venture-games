package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.BoxObstacle;

public class UmbrellaModel extends BoxObstacle {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The factor to multiply by the input */
    private final float force;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;
    /** Which direction is the umbrella facing */
    public boolean faceRight;
    /** Whether the umbrella is open or closed */
    private boolean open;
    /** The current angular rotation of the umbrella */
    private float turning;
    /** The scale to multiply the texture by for drawing */
    private float textureScale;
    /** Ratio of horizontal speed to conserve when closing the umbrella */
    private float closedMomentum = 0;

    /** texture asset for open umbrella */
    private TextureRegion openTexture;

    /** texture asset for closed umbrella */
    private TextureRegion closedTexture;

    public UmbrellaModel(JsonValue data, JsonValue pos, float width, float height) {
        super(	pos.getFloat(0),
                pos.getFloat(1),
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(false);
        //if we don't do this, the umbrella doesn't stay in position
        setGravityScale(0);
        //makes sure the umbrella doesn't collide with platforms
        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        force = data.getFloat("force", 0);
        textureScale = data.getFloat("texturescale", 1.0f);
        sensorName = "UmbrellaSensor";
        this.data = data;
        faceRight = true;
        setName("umbrella");
        open = true;
    }

    /** Returns the angle away from the x-axis of the umbrella in radians.
     * THIS IS THE VALUE YOU SHOULD USE FOR WIND INTERACTIONS, ETC. */
    public float getRotation(){return getAngle()+(float)Math.PI/2;}

    /** Returns whether the umbrella is open */
    public boolean isOpen(){return open;}
    /** Sets if this umbrella is open or closed
     * @param open whether the umbrella is open */
    public void setOpen(boolean open){this.open = open;}
    public float getForce(){return force;}
    /** Returns the ratio of horizontal speed to return when closing the umbrella */
    public float getClosedMomentum(){return closedMomentum;}
    /** Sets the ratio of horizontal speed to return when closing the umbrella */
    public void setClosedMomentum(float closedMomentum){this.closedMomentum = closedMomentum;}

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

    /**
     * sets the texture used to draw an opened umbrella
     * @param openTexture texture asset for open umbrella
     */
    public void setOpenTexture(TextureRegion openTexture) {
        this.openTexture = openTexture;
    }

    /**
     * sets the texture used to draw a closed umbrella
     * @param closedTexture texture asset for closed umbrella
     */
    public void setClosedTexture(TextureRegion closedTexture) {
        this.closedTexture = closedTexture;
    }

    /**
     * sets the texture to be opened umbrella for drawing purposes.
     *
     * No update occurs if the current texture is already the opened texture.
     */
    public void useOpenedTexture(){
        if (texture != openTexture){
            setTexture(openTexture);
        }
    }

    /**
     * sets the texture to be closed umbrella for drawing purposes.
     *
     * No update occurs if the current texture is already the closed texture
     */
    public void useClosedTexture(){
        if (texture != closedTexture){
            setTexture(closedTexture);
        }
    }
}
