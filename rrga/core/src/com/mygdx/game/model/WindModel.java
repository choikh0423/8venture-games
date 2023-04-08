package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.obstacle.*;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.GameCanvas;

import java.util.Arrays;


/**
 * A model for wind objects.
 * Currently extends PolygonObstacle to allow for different shaped wind gusts, but may want to change later
 * to make drawing manageable/easier
 */
public class WindModel extends PolygonObstacle {

    private static final float STRONG_WIND_SPEED = 20.0f;
    private static final float MED_WIND_SPEED = 15.0f;
    private static final float WEAK_WIND_SPEED = 10.0f;

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

    private PolygonRegion drawRegion;
    private float xOffset;
    private float yOffset;

    public WindModel(JsonValue data) {
        super(data.get("dimensions").asFloatArray(), data.get("pos").getFloat(0), data.get("pos").getFloat(1));
        direction = data.getFloat("direction", 0);
        magnitude = data.getFloat("magnitude");
        //The following commented out code can be uncommented to implement 3 discrete wind speeds.
        //Also would need to delete line above
        /*int mag  = data.getInt("magnitude");
        float set_mag;
        switch (mag) {
            case 1:
                set_mag = WEAK_WIND_SPEED;
                break;
            case 2:
                set_mag = MED_WIND_SPEED;
                break;
            case 3:
                set_mag = STRONG_WIND_SPEED;
                break;
            default:
                throw new Error("invalid wind speed in JSON");
        }
        magnitude = set_mag;
        magnitude = data.getFloat("magnitude", 0);*/

        //setAngle(direction-((float) Math.PI/2));
        setBodyType(BodyDef.BodyType.DynamicBody);
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        fixture.isSensor = true;
        this.data = data;
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        float angle = -direction+((float) Math.PI/2);
        float[] verts = new float[region.getVertices().length];
        for(int i = 0; i<region.getVertices().length; i+=2){
            float rotatedX = (float) Math.cos(angle) * region.getVertices()[i]
                    - (float) Math.sin(angle) * region.getVertices()[i+1];
            float rotatedY = (float) Math.sin(angle) * region.getVertices()[i]
                    + (float) Math.cos(angle) * region.getVertices()[i+1];
            verts[i] = rotatedX;
            verts[i+1] = rotatedY;
        }
        for(int i = 0; i<verts.length; i++){
            if(i%2==0 && verts[i]<xOffset){
                xOffset = verts[i];
            }
            if(i%2==1 && verts[i]<yOffset){
                yOffset = verts[i];
            }
        }
        for(int i = 0; i<verts.length; i++){
            if(i%2==0){
                verts[i] -= xOffset;
            }
            if(i%2==1){
                verts[i] -= yOffset;
            }
        }
        drawRegion = new PolygonRegion(texture, verts, region.getTriangles());
        return true;
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

    /**
     * Draws the wind object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (region != null) {
            canvas.draw(drawRegion, Color.WHITE, -xOffset, -yOffset,getX()*drawScale.x + xOffset,getY()*drawScale.y + yOffset,
                    direction-((float) Math.PI/2),1,1);
            //direction-((float) Math.PI/2)
        }
    }

}