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
import com.mygdx.game.utility.obstacle.ComplexObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;

/**
 * A multi-hit-box bird hazard.
 */
public class BirdHazard extends ComplexObstacle implements HazardModel, Drawable {

    public enum BirdColor {
        RED,
        BLUE,
        GREEN,
        BROWN
    }

    /**
     * @param variant a bird's color
     */
    public BirdColor convertToColor(String variant){
        switch (variant){
            case "blue": return BirdColor.BLUE;
            case "green": return BirdColor.GREEN;
            case "brown": return BirdColor.BROWN;
            case "red": return BirdColor.RED;
            default: return null;
        }
    }

    /**
     * A bird is either looping, moving forward, or reverse movement.
     * Birds that don't follow a path are said to be stationary.
     */
    private enum MoveBehavior {
        LOOP,
        FORWARD,
        REVERSE,
        STATIONARY,
    }

    private static final int ATTACK_WAIT_TIME = 80;

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
    private float[] path;

    /**
     * The index of the birds current targeted x-coordinate in path
     * Invariant: currentPath index is even
     */
    private int currentPathIndex;

    /** if valid, this is the path index that follows the very last path point. */
    private int loopTo;

    /**
     * the bird's current way of moving around
     */
    private MoveBehavior patrol;

    /**
     * The color of this bird. Determines the bird's behavior.
     * Red: Patrolling Passively.
     * Green: Stationary Ambush
     * Blue: Nested Repeated Spawn.
     * Brown: Patrols Aggressively.
     */
    private final BirdColor color;

    /**
     * Move speed of this bird
     */
    private final int moveSpeed;

    /**
     * The amount (in physics units) this bird is currently trying to move
     */
    private final Vector2 move = new Vector2();

    /**
     * Which direction is the bird facing
     */
    private boolean faceRight;

    private final boolean attack;

    private final int damage;

    private final float knockBackScl;

    private final Vector2 knockBackVec = new Vector2();
    /** Whether this bird's kockback vector should be set. Upon initial collision, set to false.
     * Once the collision is resolved, set to true */
    private boolean setKB;

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    /** the dimensions of a single animation frame */
    private final Vector2 filmStripSize = new Vector2();

    private final Vector2 temp = new Vector2();

    /** the bird's draw depth */
    private final int depth;

    // <=============================== Animation objects start here ===============================>

    /** Bird flap animation*/
    private Animation<TextureRegion> flapAnimation;

    /** Still Frame */
    private TextureRegion stillFrame;

    /** Bird flap animation elapsed time */
    float flapElapsedTime;

    /** Bird warning animation filmstrip texture */
    private final Texture warningTex;


    /** Bird warning animation frames */
    private TextureRegion[][] warningTmpFrames;

    /** Bird warning animation frames */
    private TextureRegion[] warningAnimationFrames;

    /** Bird warning animation*/
    private Animation<TextureRegion> warningAnimation;

    /** Bird waning animation elapsed time */
    float warningElapsedTime;


    /**
     * Whether this bird sees its target.
     * If true, moves in a straight line towards initial sighting position.
     * If false, moves along its path.
     */
    public boolean seesTarget;

    public int attackWait;

    public boolean warning;

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

    @Override
    public Vector2 getDimensions() {
        return temp.set(dimensions);
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(boxCoordinate).add(getX(), getY());
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public float getAABBx(){ return boxCoordinate.x + getX(); }

    public float getAABBy(){ return boxCoordinate.y + getY(); }

    /** the radius of player detection */
    public int getSensorRadius() {return sensorRadius;}

    @Override
    public Vector2 getKnockBackForce() {
        return knockBackVec;
    }
    @Override
    public void setKnockBackForce(Vector2 in) {
        if(setKB) {
            knockBackVec.set(in.nor());
            setKB = false;
        }
    }

    public void setSetKB(boolean b){
        setKB = b;
    }

    public BirdColor getColor() {
        return color;
    }

    @Override
    public int getDamage() { return damage; }

    @Override
    public float getKnockBackScl() { return knockBackScl; }

    /**
     * Sets bird flapping animation
     */
    public void setFlapAnimation(Texture flapTexture, int stillFrameIndex) {
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
        this.stillFrame = flapAnimationFrames[stillFrameIndex];
    }

    public void setWarningAnimation(Texture warningTexture){
        if (warningTexture == null) {
            return;
        }

        warningTmpFrames = TextureRegion.split(warningTexture, warningTexture.getWidth()/4, warningTexture.getHeight());
        int columns = warningTmpFrames.length == 0? 0 : warningTmpFrames[0].length;
        warningAnimationFrames = new TextureRegion[warningTmpFrames.length * columns];

        // PLacing animation frames in order
        int index = 0;
        for (TextureRegion[] warningTmpFrame : warningTmpFrames) {
            for (TextureRegion textureRegion : warningTmpFrame) {
                warningAnimationFrames[index] = textureRegion;
                index++;
            }
        }
        // Adjust frame duration here
        this.warningAnimation = new Animation<>(1f/10f, warningAnimationFrames);
    }

    /**
     * Sets the direction of the target using the targets x and y coordinates
     */
    public void setTargetDir(float tx, float ty, float tvx, float tvy) {
        //Right now using euler's method to determine target direction
        //In the future might want to switch to tracking player's location up to a certain point
        //and incrementally adjusting direction.
        float dist = (float) Math.sqrt(Math.pow((tx-getX()), 2) + Math.pow((ty-getY()), 2));
        float timeStep = dist / attackSpeed;
        float moveX = tx - getX() + (tvx * timeStep);
        float moveY = ty - getY() + (tvy * timeStep);

        move.set(moveX, moveY).nor().scl(attackSpeed);
        targetDir.set(move);
    }
    
    /**
     * sets the birds path to the given valid path and loopTo index. The bird's patrolling pattern
     * is also modified so that the bird will remain stationary if the path only contains 1 point (2 coordinates).
     * @param path list of coordinates, length(path) must be even and at least 2.
     * @param loopTo the index [0] <= [idx] <= [length(path)/2 - 1] to connect last point on path with. This parameter
     *               can be set to any other value if the bird does not intend to loop.
     */
    public void setPath(float[] path, int loopTo){
        this.path = path;
        this.loopTo = loopTo;
        if (path.length <= 2){
            patrol = MoveBehavior.STATIONARY;
            return;
        }
        if (0 <= loopTo && loopTo <= path.length/2 - 1){
            patrol = MoveBehavior.LOOP;
        }
        else {
            patrol = MoveBehavior.FORWARD;
        }
    }

    public BirdHazard(JsonValue data, int birdDamage, int birdSensorRadius, float birdKnockBack, Texture warningTex) {
        super(data.getFloat("x"), data.getFloat("y"));

        // this is the bounding box dimensions of the texture that contains all animation frames.
        // aabb = [x,y, width, height] where x,y is relative to bird coordinate
        float[] aabb = data.get("AABB").asFloatArray();
        boxCoordinate.x = aabb[0];
        boxCoordinate.y = aabb[1];
        dimensions.x = aabb[2];
        dimensions.y = aabb[3];
        filmStripSize.x = data.getInt("filmStripWidth");
        filmStripSize.y = data.getInt("filmStripHeight");

        // set remaining properties
        depth = data.getInt("depth");
        path = data.get("path").asFloatArray();
        setPath(data.get("path").asFloatArray(), data.getInt("loopTo", -1));
        attack = data.getBoolean("attack");
        moveSpeed = data.getInt("movespeed");

        color = convertToColor(data.getString("color"));
        faceRight = data.getBoolean("facing_right");
        attackSpeed = data.getFloat("atkspeed");
        sensorRadius = birdSensorRadius;
        currentPathIndex = 0;
        attackWait = ATTACK_WAIT_TIME;
        seesTarget = false;
        damage = birdDamage;
        knockBackScl = birdKnockBack;
        setKB = true;
        warning = false;
        this.warningTex = warningTex;

        // make hit-box objects
        float x = data.getFloat("x");
        float y = data.getFloat("y");
        float[] shape = data.get("points").asFloatArray();
        PolygonObstacle hit1 = new PolygonObstacle( shape, x, y);
        // flip points and make hit-box #2:
        for (int idx = 0; idx < shape.length; idx+=2){
            shape[idx] = -shape[idx];
        }
        PolygonObstacle hit2 = new PolygonObstacle(shape, x, y);
        // now figure out which of the above is left/right hitbox
        if (faceRight){
            bodies.add(hit2);   //left facing
            bodies.add(hit1);   //right facing
        }
        else {
            bodies.add(hit1);   //left facing
            bodies.add(hit2);   //right facing
        }
        for (Obstacle o : bodies){
            o.setBodyType(BodyDef.BodyType.StaticBody);
            o.setDensity(0);
            o.setFriction(0);
            o.setRestitution(0);
        }
    }

    @Override
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        for (Obstacle hitbox : bodies){
            // all hit-boxes of this bird should reference the same bird hazard
            hitbox.getBody().setUserData(this);
        }
        // NOTE: this is a 2 hit-box model.
        // keep one of the sided hit-box inactive at all times.
        if (faceRight){
            bodies.get(0).setActive(false);
        }
        else {
            bodies.get(1).setActive(false);
        }

        return true;
    }

    @Override
    protected boolean createJoints(World world) {
        // no joints needed for multi-hitbox
        return true;
    }

    /**
     * update the current position on a patrolling path
     * based on a variety of factors (path, patrolling behavior, etc).
     */
    private void patrol(){
        // update direction, using next (X,Y) coordinate on path and computing distances
        float pathX = path[currentPathIndex];
        float pathY = path[currentPathIndex + 1];
        float deltaX = pathX - getX();
        float deltaY = pathY - getY();
        move.set(deltaX, deltaY).nor().scl(moveSpeed / 100f);
        if (Math.abs(move.x) > Math.abs(deltaX)) setX(pathX);
        else setX(getX() + move.x);
        if (Math.abs(move.y) > Math.abs(deltaY)) setY(pathY);
        else setY(getY() + move.y);
        if (move.x != 0){
            setFaceRight(move.x > 0);
        }
        if (Math.abs(deltaX) < .001 && Math.abs(deltaY) < .001){
            // determine next point to move to
            switch (patrol){
                case FORWARD:
                    // end of forward path, time to reverse
                    if (currentPathIndex == path.length - 2){
                        patrol = MoveBehavior.REVERSE;
                        currentPathIndex -= 2;
                    }
                    else currentPathIndex += 2;
                    break;
                case REVERSE:
                    // end of backwards path
                    if (currentPathIndex == 0){
                        patrol = MoveBehavior.FORWARD;
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
        }
    }

    public void move() {
        //if target not seen
        if (!seesTarget) {
            if(moveSpeed > 0) {
                patrol();
                moveDir.set(move);

                //Uncomment for rotating birds
                /*for (Obstacle o : bodies){
                    o.setAngle(getAngleFromVec(moveDir));
                }
                setAngle(getAngleFromVec(moveDir));*/
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
            }
        }
    }

    /** Returns the angle above/below x axis */
    private float getAngleFromVec(Vector2 vec){
        float angle;
        //adapted from https://stackoverflow.com/questions/6247153/angle-from-2d-unit-vector
        if (vec.x == 0) {
            angle = (vec.y > 0) ? (float) Math.PI / 2 : (vec.y == 0) ? 0 : 3 * (float) Math.PI / 2;
        } else if (vec.y == 0) {
            angle = (vec.x >= 0) ? 0 : (float) Math.PI;
        } else {
            angle = (float) Math.atan(vec.y / vec.x);
        }
        return angle;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;

        // this fixes inconsistency with blue/brown bird assets
        if (color.equals(BirdColor.BROWN) || color.equals(BirdColor.BLUE)){
            effect = faceRight ? -1.0f : 1f;
        }

        TextureRegion birdRegion = stillFrame;
        if (!seesTarget && moveSpeed == 0){
            // not angry + not moving => still
            canvas.draw(birdRegion, Color.WHITE, stillFrame.getRegionWidth() / 2f, birdRegion.getRegionHeight() / 2f,
                    (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                    effect * dimensions.x / birdRegion.getRegionWidth() * drawScale.x,
                    dimensions.y / birdRegion.getRegionHeight() * drawScale.y);
        }
        else {
            // moving/angry => flapping
            flapElapsedTime += Gdx.graphics.getDeltaTime();
            birdRegion = flapAnimation.getKeyFrame(flapElapsedTime, true);

            canvas.draw(birdRegion, Color.WHITE, birdRegion.getRegionWidth() / 2f, birdRegion.getRegionHeight() / 2f,
                    (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                    effect * dimensions.x / birdRegion.getRegionWidth() * drawScale.x,
                    dimensions.y / birdRegion.getRegionHeight() * drawScale.y);
        }

        if(warning){
            warningElapsedTime += Gdx.graphics.getDeltaTime();
            TextureRegion warningRegion = warningAnimation.getKeyFrame(warningElapsedTime, true);

            int flip = faceRight ? 1 : -1;
            float eye = color == BirdColor.BLUE ? 7.5f : 6f;
            canvas.draw(warningRegion, Color.WHITE, warningRegion.getRegionWidth()/2f, warningRegion.getRegionHeight()/2f,
                    (getX()) * drawScale.x + flip*birdRegion.getRegionWidth()/eye, (getY()) * drawScale.y, getAngle(),
                    dimensions.x/birdRegion.getRegionWidth() * drawScale.x,
                    dimensions.y/birdRegion.getRegionHeight() * drawScale.y);
            }
    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    @Override
    public void drawDebug(GameCanvas canvas) {
        for(Obstacle obj : bodies) {
            if (obj.isActive()){
                obj.drawDebug(canvas);
            }
        }


        if (attack) {
            CircleShape sensorShape = new CircleShape();
            sensorShape.setRadius(sensorRadius);
            canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
            //CAN CRASH THE GAME
            //JUST FOR VISUALIZATION
            /*
            Vector2 targ = new Vector2();
            Vector2 third = new Vector2();
            Vector2 pos = new Vector2();
            float x = getX();
            float y = getY();
            pos.set(x, y);

            //need px. py
            temp.set(0, 0);
            temp.sub(x, y);
            temp.nor();
            float angle;

            //adapted from https://stackoverflow.com/questions/6247153/angle-from-2d-unit-vector
            if (temp.x == 0) {
                angle =  (temp.y > 0) ? (float) Math.PI/2 : (temp. y == 0) ? 0 : 3 * (float) Math.PI/2;
            }
            else if (temp.y == 0){
                angle = (temp.x >= 0) ? 0 : (float) Math.PI;
            }
            else {
                angle = (float) Math.atan(temp.y / temp.x);
                if (temp.x < 0 && temp.y < 0) // quadrant Ⅲ
                    angle += Math.PI;
                else if (temp.x < 0) // quadrant Ⅱ
                    angle += Math.PI;
                else if (temp.y < 0) // quadrant Ⅳ
                    angle += 2*Math.PI;
            }

            for (int i = 0; i < 5; i++) {
                targ.set(x + getSensorRadius(), y).rotateAroundRad(pos, angle - (float) (Math.PI/8) + (float) (Math.PI/4) * i / 5);
                third.set(targ).add(.01f, .01f);;
                PolygonShape line = new PolygonShape();
                line.set(new Vector2[]{pos, targ, third});
                canvas.drawPhysics(line, Color.RED, 0, 0, 0, drawScale.x, drawScale.y);
            }
            */
        }
    }

    /**
     * swaps the active states of the two hit-box bodies
     */
    private void swapActive(){
        if (bodies.get(0).isActive()){
            bodies.get(0).setActive(false);
            bodies.get(1).setActive(true);
        }
        else {
            bodies.get(0).setActive(true);
            bodies.get(1).setActive(false);
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
        for (Obstacle o : bodies){
            o.setName(value + "_hitbox");
        }
    }

    @Override
    public void setX(float value){
        super.setX(value);
        for (Obstacle o : bodies){
            o.setX(value);
        }
    }

    @Override
    public void setY(float value){
        super.setY(value);
        for (Obstacle o : bodies){
            o.setY(value);
        }
    }

}
