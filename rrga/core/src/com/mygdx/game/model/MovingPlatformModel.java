package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.PolygonObstacle;

import java.util.Arrays;

public class MovingPlatformModel extends PolygonObstacle {

    // TODO: redesign class hierarchy. MovingPlatform is nothing but a REAL passive moving bird
    // TODO: too much code duplication from Bird class.

    /**
     * A platform is either looping, moving forward, or reverse movement.
     * platforms that don't follow a path are said to be stationary.
     */
    private enum MoveBehavior {
        LOOP,
        FORWARD,
        REVERSE,
        STATIONARY
    }

    private MoveBehavior patrol;

    private int loopTo;

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    public float getWidth(){ return dimensions.x; }

    public float getHeight(){
        return dimensions.y;
    }

    public float getAABBx(){ return boxCoordinate.x; }

    public float getAABBy(){ return boxCoordinate.y; }

    /** whether to horizontally flip */
    private final boolean flipped;

    /**
     * A list of points which represent this platform's path.
     * Invariant: length >=2 and length is even.
     */
    private float[] path;

    /**
     * The index of the platform's current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    /**
     * Move speed of this platform
     */
    private final int moveSpeed;

    /** (squared) distances traveled and distance to travel*/
    private final Vector2 distanceCache = new Vector2();

    private final Vector2 prevPos = new Vector2();

    /**
     * Previous Direction of the platform movement
     */
    private final Vector2 prevMoveDir = new Vector2();

    /**
     * Direction of the platform movement
     */
    private final Vector2 moveDir = new Vector2();


    public MovingPlatformModel(JsonValue data, float[] points, float x, float y) {
        super(points, x, y);
        setPath(data.get("path").asFloatArray(), data.getInt("loopTo", -1));
        moveSpeed = data.getInt("movespeed");
        currentPathIndex = 0;
        flipped = data.getBoolean("flipped");
        prevPos.set(getX(), getY());

        // this is the bounding box dimensions of the cloud.
        // aabb = [x,y, width, height] where x,y is relative to bird coordinate
        float[] aabb = data.get("AABB").asFloatArray();
        boxCoordinate.x = aabb[0];
        boxCoordinate.y = aabb[1];
        dimensions.x = aabb[2];
        dimensions.y = aabb[3];
    }

    public void setPath(float[] path, int loopTo){
        this.path = path;
        this.loopTo = loopTo;
        if (path.length <= 2){
            patrol = MovingPlatformModel.MoveBehavior.STATIONARY;
            return;
        }
        if (0 <= loopTo && loopTo <= path.length/2 - 1){
            patrol = MovingPlatformModel.MoveBehavior.LOOP;
        }
        else {
            patrol = MovingPlatformModel.MoveBehavior.FORWARD;
        }
    }

    public void move(){
        // update direction, using next (X,Y) coordinate on path and computing distances
        float travelX = getX() - prevPos.x;
        float travelY = getY() - prevPos.y;
        distanceCache.x = moveDir.set(travelX, travelY).len2();
        // reached the next point on path
        if (distanceCache.x >= distanceCache.y){
            // determine next point to move to
            switch (patrol){
                case FORWARD:
                    // end of forward path, time to reverse
                    if (currentPathIndex == path.length - 2){
                        patrol = MovingPlatformModel.MoveBehavior.REVERSE;
                        currentPathIndex -= 2;
                    }
                    else currentPathIndex += 2;
                    break;
                case REVERSE:
                    // end of backwards path
                    if (currentPathIndex == 0){
                        patrol = MovingPlatformModel.MoveBehavior.FORWARD;
                        currentPathIndex += 2;
                    }
                    else currentPathIndex -= 2;
                    break;
                case LOOP:
                    if (currentPathIndex == path.length - 2 ){
                        currentPathIndex = 2 * loopTo;
                    }
                    else currentPathIndex += 2;
                    break;
                case STATIONARY:
                    break;
            }
            float pathX = path[currentPathIndex];
            float pathY = path[currentPathIndex + 1];
            distanceCache.y = moveDir.set(pathX - getX(), pathY - getY()).len2();
            distanceCache.x = 0;
            prevPos.set(getX(), getY());
            prevMoveDir.set(pathX - getX(), pathY - getY()).nor();

        }
        moveDir.set(prevMoveDir).scl(moveSpeed);
        setLinearVelocity(moveDir);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = flipped ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth()/2f, texture.getRegionHeight()/2f,
                (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                effect * dimensions.x/texture.getRegionWidth() * drawScale.x,
                dimensions.y/texture.getRegionHeight() * drawScale.y);
    }

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
