package com.mygdx.game.Hazards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

public class BirdHazard extends HazardModel{

    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    /** The shape of this bird's sensor */
    private CircleShape sensorShape;

    /** A list of points which represent this bird's flight path.
     * Invariant: length >=4 and length is even.
     */
    private float[] path;

    /** The index of the birds current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    private int moveSpeed;

    /** Whether this bird sees its target.
     * If true, moves in a straight line towards initial sighting position.
     * If false, moves along its path.
     */
    public boolean seesTarget;

    /**
     * Returns the name of this bird's sensor
     *
     * @return the name of this bird's sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    public BirdHazard(JsonValue data) {
        super(data);
        //get path from json here
        currentPathIndex = 0;
        //get movespeed from json here
        sensorName = "birdSensor";
        seesTarget = false;

        //create sensor
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(10);
        sensorDef.shape = sensorShape;
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());
    }

    public void move(){
        float targX = path[currentPathIndex];
        float targY = path[currentPathIndex+1];
        float moveX = targX-getX();
        float moveY = targY-getY();
        if(moveX == 0 && moveY == 0){
            currentPathIndex += 2;
        }
        else{
            setX(getX()+(moveX/moveSpeed));
            setY(getY()+(moveY/moveSpeed));
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }


}
