package com.mygdx.game.model.hazard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.ComplexObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;

public class AnimatedLightningHazard extends ComplexObstacle implements HazardModel, Drawable {

    /** Damage of a lightning hazard */
    private static final int LIGHTNING_DAMAGE = 1;

    /** Knockback of a lightning hazard */
    private static final float LIGHTNING_KNOCKBACK = 0;

    private final Vector2 temp = new Vector2();

    /** the layer depth of this animated object when drawn */
    private final int drawDepth;
    private final boolean flipped;

    /** the active hit-box index must be [0 ... body count - 1]*/
    private int activeHitBoxIndex;

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    ///** the dimensions of a single animation frame */
//    private final Vector2 filmStripSize = new Vector2();

    private final TextureRegion[] frames;

    private TextureRegion prevFrame;

    private final Animation<TextureRegion> animation;
    private float strikeElapsedTime;

    /**
     * Creates an animating lightning whose properties are contained within the given data.
     * @param data JSON data with properties: position, dimensions, filmstrip size
     * @param animationTexture the filmstrip containing each frame of the animation
     */
    public AnimatedLightningHazard(JsonValue data, Texture animationTexture){
        super(data.getFloat("x"), data.getFloat("y"));
        this.drawDepth = data.getInt("depth");
        // this is the bounding box dimensions of the texture that contains all animation frames.
        // aabb = [x,y, width, height] where x,y is relative to lightning asset coordinate
        float[] aabb = data.get("AABB").asFloatArray();
        boxCoordinate.set(aabb[0], aabb[1]);
        dimensions.set(aabb[2], aabb[3]);
        activeHitBoxIndex = 0;
        TextureRegion[][] frameGrid = TextureRegion.split(animationTexture,
                data.getInt("filmStripWidth"),
                data.getInt("filmStripHeight"));
        frames = new TextureRegion[frameGrid[0].length * frameGrid.length];
        int idx = 0;
        for (TextureRegion[] column : frameGrid){
            for (TextureRegion frame : column){
                frames[idx] = frame;
                idx++;
            }
        }
        animation = new Animation<>(0.15f, frames);
        prevFrame = frames[0];
        flipped = data.getBoolean("flipped");

        float x = data.getFloat("x");
        float y = data.getFloat("y");
        for (JsonValue hitBoxData : data.get("hitboxes")){
            float[] points = hitBoxData.asFloatArray();
            // could also use PolygonHazard, but trying this to see if any difference.
            Obstacle o = new PolygonObstacle(points, x, y);
            bodies.add(o);
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

        for (Obstacle hitBox : bodies){
            // all hit-boxes of this bird should reference the same bird hazard
            hitBox.getBody().setUserData(this);
            hitBox.setActive(false);
        }
        // NOTE: this is a N hit-box model.
        // Only one of the hit-box is active at any given time.
        bodies.get(activeHitBoxIndex).setActive(true);

        return true;
    }

    /** switches current hit-box to the next frame's hit-box. <br>
     * If at the end of the animation, the next hit-box will be the first.
     */
    private void switchHitBox(){
        bodies.get(activeHitBoxIndex).setActive(false);
        activeHitBoxIndex++;
        activeHitBoxIndex %= frames.length;
        bodies.get(activeHitBoxIndex).setActive(true);
    }

    @Override
    public void draw(GameCanvas canvas) {
        int effect = flipped ? -1 : 1;
        strikeElapsedTime += Gdx.graphics.getDeltaTime();
        TextureRegion texture = animation.getKeyFrame(strikeElapsedTime, true);
        if (texture != prevFrame){
            prevFrame = texture;
            switchHitBox();
        }
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth()/2f, texture.getRegionHeight()/2f,
                getX() * drawScale.x,getY() * drawScale.y , getAngle(),
                effect * dimensions.x/texture.getRegionWidth() * drawScale.x,
                dimensions.y/texture.getRegionHeight() * drawScale.y);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        for(Obstacle obj : bodies) {
            if (obj.isActive()){
                obj.drawDebug(canvas);
            }
        }
    }

    // HAZARD INTERFACE

    @Override
    public int getDamage() {
        return LIGHTNING_DAMAGE;
    }

    @Override
    public float getKnockBackScl() {
        // no knock back
        return LIGHTNING_KNOCKBACK;
    }

    @Override
    public Vector2 getKnockBackForce() {
        // no knock back
        return temp.set(0,0);
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
