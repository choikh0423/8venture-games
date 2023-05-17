package com.mygdx.game.model.hazard;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.ComplexObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;
import com.mygdx.game.utility.util.FilmStrip;

public class AnimatedLightningHazard extends ComplexObstacle implements HazardModel, Drawable {

    private static final int DEFAULT_STRIKE_DURATION = 100;

    private final Vector2 temp = new Vector2();

    /** the layer depth of this animated object when drawn */
    private final int drawDepth;

    /** whether object is horizontally flipped */
    private final boolean flippedX;

    /** the active frame/hit-box index must be [0 ... frame count- 1]*/
    private int activeFrameIndex;

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    /** the animation texture region */
    private final FilmStrip frames;

    /** the duration count for each frame of the animation */
    private final int[] frameCounts = new int[]{2, 2, 3, 3, 4, 0};

    /** the number of display frames remaining for the current frame of the animation */
    private int frameCounter;

    /** the number of frames to wait until next strike cycle */
    private final int waitFrameCount;

    /** the current remaining number of frames to wait until next strike cycle */
    private int waitCounter;

    private final int damage;

    private final float knockBack;

    private Sound sfx;
    private float sfxVol;
    public void setSfxVol(float vol){sfxVol = vol;}

    /**
     * Creates an animating lightning whose properties are contained within the given data.
     * @param data JSON data with properties: position, dimensions, filmstrip size
     * @param animationTexture the filmstrip containing each frame of the animation
     */
    public AnimatedLightningHazard(JsonValue data, Texture animationTexture, int dmg, float knockBack, Sound sfx){
        super(data.getFloat("x"), data.getFloat("y"));
        drawDepth = data.getInt("depth");
        flippedX = data.getBoolean("flipped");
        // this is the bounding box dimensions of the texture that contains all animation frames.
        // aabb = [x,y, width, height] where x,y is relative to lightning asset coordinate
        float[] aabb = data.get("AABB").asFloatArray();
        boxCoordinate.set(aabb[0], aabb[1]);
        dimensions.set(aabb[2], aabb[3]);
        int cols = animationTexture.getWidth() / data.getInt("filmStripWidth");
        int rows = animationTexture.getHeight() / data.getInt("filmStripHeight");
        frames = new FilmStrip(animationTexture, rows, cols);
        this.sfx=sfx;

        // set duration of final frame
        int growDuration = 0;
        for (Integer d : frameCounts){
            growDuration += d;
        }
        frameCounts[5] = data.getInt("strike_duration", DEFAULT_STRIKE_DURATION) - growDuration;
        activeFrameIndex = 0;

        waitFrameCount = data.getInt("strike_timer", DEFAULT_STRIKE_DURATION) - growDuration;
        waitCounter = waitFrameCount + data.getInt("initial_timer_offset");

        float x = data.getFloat("x");
        float y = data.getFloat("y");
        for (JsonValue hitBoxData : data.get("hitboxes")){
            float[] points = hitBoxData.asFloatArray();
            Obstacle o = new PolygonObstacle(points, x, y);
            o.setBodyType(BodyDef.BodyType.StaticBody);
            o.setDensity(0);
            o.setFriction(0);
            o.setRestitution(0);
            bodies.add(o);
        }
        this.damage = dmg;
        this.knockBack = knockBack;
    }

    @Override
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        // NOTE: this is an N hit-box model.
        // At most one of the hit-box is active at any given time.
        // initially, no hit-box is active.
        for (Obstacle hitBox : bodies){
            // all hit-boxes of this lightning should reference the same lightning hazard
            hitBox.getBody().setUserData(this);
            hitBox.setActive(false);
        }
        return true;
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (bodies.get(activeFrameIndex).isActive()) {
            int effect = flippedX ? -1 : 1;
            frames.setFrame(activeFrameIndex);
            canvas.draw(frames, Color.WHITE, frames.getRegionWidth() / 2f, frames.getRegionHeight() / 2f,
                    getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                    effect * dimensions.x / frames.getRegionWidth() * drawScale.x,
                    dimensions.y / frames.getRegionHeight() * drawScale.y);
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        for(Obstacle obj : bodies) {
            if (obj.isActive()){
                obj.drawDebug(canvas);
            }
        }
    }

    /** switches current hit-box to the next frame's hit-box. <br>
     * The frame counter also gets reset to the next frame's counter. <br>
     * If at the end of the animation, the next hit-box will be the first.
     */
    private void switchFrames(){
        bodies.get(activeFrameIndex).setActive(false);
        activeFrameIndex++;
        activeFrameIndex %= frames.getSize();
        frameCounter = frameCounts[activeFrameIndex];
        bodies.get(activeFrameIndex).setActive(true);
    }

    @Override
    public void update(float delta) {
        if (waitCounter < 0){
            // no more waiting, strike cycle should be ongoing
            if (frameCounter <= 0){
                if (activeFrameIndex < frames.getSize() - 1){
                    // not last frame
                    switchFrames();
                }
                else {
                    // hide last frame, move onto wait cycle
                    bodies.get(activeFrameIndex).setActive(false);
                    waitCounter = waitFrameCount;
                }
            }
            else {
                frameCounter--;
            }
        }
        else if (waitCounter == 0){
            // finished waiting, transition to strike cycle
            sfx.play(sfxVol*.2f);
            switchFrames();
            waitCounter--;
        }
        else {
            waitCounter--;
        }
    }

    // HAZARD INTERFACE

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public float getKnockBackScl() {
        return knockBack;
    }

    @Override
    public Vector2 getKnockBackForce() {
        return temp.set(0,-1);
    }

    @Override
    public void setKnockBackForce(Vector2 in) {
        // no need to update
    }

    // COMPLEX OBSTACLE
    @Override
    protected boolean createJoints(World world) {
        // no joints needed for multi-hit-box object
        return true;
    }

    // DRAWABLE INTERFACE

    @Override
    public Vector2 getDimensions() {
        return temp.set(dimensions);
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(boxCoordinate).add(getPosition());
    }

    @Override
    public int getDepth() {
        return this.drawDepth;
    }
}
