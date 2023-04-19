package com.mygdx.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.PolygonObstacle;

public class MovingPlatformModel extends PolygonObstacle {
    /**
     * A list of points which represent this bird's flight path.
     * Invariant: length >=2 and length is even.
     */
    private float[] path;

    /**
     * The index of the platform's current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    /**
     * If loop is true, bird will go from last point in path to first.
     * If loop is false, bird will turn around after last point and reverse its path
     */
    private boolean loop;

    /**
     * Move speed of this platform
     */
    private int moveSpeed;

    /**
     * The coordinates this bird is currently moving to
     */
    private Vector2 move = new Vector2();

    /**
     * Direction of the birds movement
     */
    private Vector2 moveDir = new Vector2();

    private JsonValue data;

    public MovingPlatformModel(JsonValue data, float[] points, float x, float y) {
        super(points, x, y);
        this.data = data;
        path = data.get("path").asFloatArray();
        moveSpeed = data.getInt("movespeed");
        loop = data.getBoolean("loop");
        currentPathIndex = 0;
        fixture.isSensor = false;
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        return super.activatePhysics(world);
    }

    public void move() {
        if(moveSpeed > 0) {
            float pathX = path[currentPathIndex];
            float pathY = path[currentPathIndex + 1];
            float moveX = pathX - getX();
            float moveY = pathY - getY();
            //if platform's path is > 1 point
            if (path.length > 2) {
                //if at next point in path
                if (Math.abs(moveX) < .001 && Math.abs(moveY) < .001) {
                    //if at end of path
                    if (currentPathIndex == path.length - 2) {
                        if (!loop) {
                            for (int i = 0; i < path.length / 2; i += 2) {
                                float temp1 = path[i];
                                float temp2 = path[i + 1];
                                path[i] = path[path.length - i - 2];
                                path[i + 1] = path[path.length - i - 1];
                                path[path.length - i - 2] = temp1;
                                path[path.length - i - 1] = temp2;
                            }
                        }
                        currentPathIndex = 0;
                    }
                    //else not at end of path
                    else {
                        currentPathIndex += 2;
                    }
                }
                //else not yet at next point in path
                else {
                    move.set(moveX, moveY);
                    move.nor();
                    move.scl(moveSpeed);
                    if (Math.abs((move.x / 100)) > Math.abs(moveX)) setX(pathX);
                    else setX(getX() + (move.x / 100));
                    if (Math.abs((move.y / 100)) > Math.abs(moveY)) setY(pathY);
                    else setY(getY() + (move.y / 100));
                }
            }
            //else path is 1 point
            //no movement
            moveDir.set(moveX, moveY);
        }
    }

//    /**
//     * Draws the physics object.
//     *
//     * @param canvas Drawing context
//     */
//    public void draw(GameCanvas canvas) {
//        canvas.draw(texture, Color.WHITE, origin.x, origin.y,
//                    (getX()) * drawScale.x, (getY()) * drawScale.y,
//                    getAngle(), 1, 1);
//    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
    }
}
