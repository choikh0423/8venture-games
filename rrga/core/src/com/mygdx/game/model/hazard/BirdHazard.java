package com.mygdx.game.model.hazard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

/**
 * A multi-hit-box bird hazard.
 */
public class BirdHazard extends HazardModel {

    private final int ATTACK_WAIT_TIME = 50;

    /**
     * Attack speed of this bird
     */
    private final float attackSpeed;

    /**
     * Radius of a bird's sensor
     */
    private final int sensorRadius;

    /**
     * A list of points which represent this bird's flight path.
     * Invariant: length >=2 and length is even.
     */
    private final float[] path;

    /**
     * The index of the birds current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    /**
     * If loop is true, bird will go from last point in path to first.
     * If loop is false, bird will turn around after last point and reverse its path
     */
    private final boolean loop;

    /**
     * The color of this bird. Determines the bird's behavior.
     * Red: Stationary, then attacks.
     * Blue: Patrols, doesn't attacks.
     * Brown: Patrols, then attacks.
     * Invariant: Must be one of "red", "blue", or "brown"
     */
    private final String color;

    /**
     * Move speed of this bird
     */
    private final int moveSpeed;

    /**
     * The coordinates this bird is currently moving to
     */
    private final Vector2 move = new Vector2();

    /**
     * Which direction is the bird facing
     */
    private boolean faceRight;

    private final boolean attack;

    // the dimensions of filmstrip AABB
    private final Vector2 textureAABB = new Vector2();

    // the dimensions of object AABB
    private final Vector2 dimensions = new Vector2();

    // the top left corner coordinate of object AABB
    private final Vector2 boxCoordinate = new Vector2();

    private final Vector2 filmStripSize = new Vector2();

    private final Vector2 temp = new Vector2();

    // <=============================== Animation objects start here ===============================>

    /** Bird flap animation*/
    private Animation<TextureRegion> flapAnimation;

    /** Bird flap animation elapsed time */
    float flapElapsedTime;

    /** secondary hit-box */
    private final HazardModel hit2;


    /**
     * Whether this bird sees its target.
     * If true, moves in a straight line towards initial sighting position.
     * If false, moves along its path.
     */
    public boolean seesTarget;

    public int attackWait;

    /**
     * Direction of the target
     */
    private final Vector2 targetDir = new Vector2();

    /**
     * Direction of the birds movement
     */
    private final Vector2 moveDir = new Vector2();

    public boolean getAttack(){
        return attack;
    }

    public float getWidth(){ return dimensions.x; }

    public float getHeight(){
        return dimensions.y;
    }

    public float getAABBx(){ return boxCoordinate.x; }

    public float getAABBy(){ return boxCoordinate.y; }

    /** the radius of player detection */
    public int getSensorRadius() {return sensorRadius;}

    @Override
    public Vector2 getKnockbackForce() {
        return temp.set(moveDir.x, moveDir.y).nor();
    }

    /**
     * Sets bird flapping animation
     */
    public void setFlapAnimation(Texture flapTexture) {
        if (flapTexture == null) {
            return;
        }

        TextureRegion[][] flapTmpFrames = TextureRegion.split(flapTexture, (int) filmStripSize.x, (int) filmStripSize.y);
        int columns = flapTmpFrames.length == 0? 0 : flapTmpFrames[0].length;
        // Bird flap animation frames
        TextureRegion[] flapAnimationFrames = new TextureRegion[flapTmpFrames.length * columns];

        // PLacing animation frames in order
        int index = 0;
        for (TextureRegion[] flapTmpFrame : flapTmpFrames) {
            for (TextureRegion textureRegion : flapTmpFrame) {
                flapAnimationFrames[index] = textureRegion;
                index++;
            }
        }
        // Adjust frame duration here
        this.flapAnimation = new Animation<>(1f/10f, flapAnimationFrames);
    }

    /**
     * Sets the direction of the target using the targets x and y coordinates
     */
    public void setTargetDir(float tx, float ty, float tvx, float tvy) {
        //Right now using euler's method to determine target direction
        //In the future might want to switch to tracking player's location up to a certain point
        //and incrementally adjusting direction.
        float timeStep = sensorRadius / attackSpeed;
        float moveX = tx - getX() + (tvx * timeStep);
        float moveY = ty - getY() + (tvy * timeStep);
        move.set(moveX, moveY);
        move.nor();
        move.scl(attackSpeed);
        targetDir.set(move);
    }

    public BirdHazard(JsonValue data, int birdDamage, int birdSensorRadius, float birdKnockback) {
        super(data, data.get("points").asFloatArray(),birdDamage, birdKnockback);

        //this is the bounding box dimensions of the texture that contains all animation frames.
        // aabb = [x,y, width, height] where x,y is relative to bird coordinate
        float[] aabb = data.get("AABB").asFloatArray();
        boxCoordinate.x = aabb[0];
        boxCoordinate.y = aabb[1];
        textureAABB.x = aabb[2];
        textureAABB.y = aabb[3];
        dimensions.x = textureAABB.x * aabb[4];
        dimensions.y = textureAABB.y * aabb[5];
        filmStripSize.x = data.getInt("filmStripWidth");
        filmStripSize.y = data.getInt("filmStripHeight");

        // make hit-box objects
        // first, current object is hit-box 1.
        // make hit-box #2:
        float x = data.getFloat("x");
        float y = data.getFloat("y");
        float[] shape = data.get("points").asFloatArray();
        for (int idx = 0; idx < shape.length; idx+=2){
            shape[idx] = -shape[idx];
        }
        hit2 = new HazardModel(data, shape, birdDamage, birdKnockback) {
            @Override
            public Vector2 getKnockbackForce() {
                return temp.set(moveDir.x, moveDir.y).nor();
            }
        };

        path = data.get("path").asFloatArray();
        attack = data.getBoolean("attack");
        moveSpeed = data.getInt("movespeed");
        loop = data.getBoolean("loop");
        color = data.getString("color");
        faceRight = data.getBoolean("facing_right");
        attackSpeed = data.getFloat("atkspeed");
        sensorRadius = birdSensorRadius;
        currentPathIndex = 0;
        attackWait = ATTACK_WAIT_TIME;
        seesTarget = false;
        //fixture.isSensor = true;
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        hit2.activatePhysics(world);
        hit2.getBody().setUserData(this);
        return true;
    }

    @Override
    public void deactivatePhysics(World world){
        super.deactivatePhysics(world);
        hit2.deactivatePhysics(world);
    }

    public void move() {
        //if target not seen
        if (!seesTarget) {
            if(moveSpeed > 0) {
                float pathX = path[currentPathIndex];
                float pathY = path[currentPathIndex + 1];
                float moveX = pathX - getX();
                float moveY = pathY - getY();
                //if bird's path is > 1 point
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
                        setFaceRight(move.x > 0);
                    }
                }
                //else path is 1 point
                //no movement
                moveDir.set(moveX, moveY);
            }
        }
        //else target is seen
        else {
            if(attackWait == -1) {
                //move in direction of targetCoords until offscreen
                setX(getX() + (targetDir.x / 100));
                setY(getY() + (targetDir.y / 100));
                moveDir.set(targetDir);
                // targetDir is the direction of target relative to bird's location
                setFaceRight(targetDir.x > 0);
                //TODO: Need some way to delete when offscreen, should be handled by gamecontroller
                //TODO: use AABB to determine off screen
            }
        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;

        // this fixes inconsistency with blue bird asset
        if (color.equals("blue")){
            effect = faceRight ? -1.0f : 1f;
        }

        flapElapsedTime += Gdx.graphics.getDeltaTime();
        TextureRegion region = flapAnimation.getKeyFrame(flapElapsedTime, true);

        canvas.draw(region, Color.WHITE, region.getRegionWidth()/2f, region.getRegionHeight()/2f,
                (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                effect * dimensions.x/textureAABB.x * drawScale.x,
                dimensions.y/textureAABB.y * drawScale.y);
    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        if (this.isActive()){
            super.drawDebug(canvas);
        }
        else{
            hit2.drawDebug(canvas);
        }

        /*
        if (attack) {
            //canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
            //CAN CRASH THE GAME
            //JUST FOR VISUALIZATION
            Vector2 targ = new Vector2();
            Vector2 third = new Vector2();
            Vector2 pos = new Vector2();
            float x = getX();
            float y = getY();
            pos.set(x, y);
            for (int i = 0; i < 60; i++) {
                targ.set(x, y + 7).rotateAroundDeg(pos, 360 / 60f * i);;
                third.set(targ).add(.01f, .01f);;
                PolygonShape line = new PolygonShape();
                line.set(new Vector2[]{pos, targ, third});
                canvas.drawPhysics(line, Color.RED, 0, 0, 0, drawScale.x, drawScale.y);
            }
        }
        */

    }

    /**
     * swaps the active states of the two hit-box bodies
     */
    private void swapActive(){
        if (this.isActive()){
            this.setActive(false);
            hit2.setActive(true);
        }
        else {
            this.setActive(true);
            hit2.setActive(false);
        }
    }

    /**
     * sets the bird to face right based on given boolean.
     * If bird changes directions, hit-box also switches.
     * @param value whether to face right
     */
    public void setFaceRight(boolean value) {
        // switch active bodies if direction changes
        boolean old = this.faceRight;
        this.faceRight = value;
        if (old != this.faceRight){
            swapActive();
        }
    }

    @Override
    public void setName(String value) {
        super.setName(value);
        hit2.setName(value + "_2");
    }

    @Override
    public void setDrawScale(Vector2 value) {
        super.setDrawScale(value);
        hit2.setDrawScale(value);
    }

    @Override
    public void setX(float value){
        super.setX(value);
        hit2.setX(value);
    }

    @Override
    public void setY(float value){
        super.setY(value);
        hit2.setY(value);
    }

}
