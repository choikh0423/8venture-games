package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;


/**
 * A model for wind objects.
 * Currently extends PolygonObstacle to allow for different shaped wind gusts, but may want to change later
 * to make drawing manageable/easier
 */
public class ParticleModel extends BoxObstacle implements Drawable {

    /** Physical Coordnate X */
    private float physX;

    /** Physical Coordnate Y */
    private float physY;

    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private Vector2 velocity = new Vector2();
    /**
     * The magnitude of this wind's force. Invariant: magnitude > 0.
     */
    private float magnitude;
    /**
     * The direction of this wind's force in radians. Value within [0, 2pi).
     */
    private float direction;

    private Animation<TextureRegion> animation;
    private float elapsedTime;

    private PolygonRegion drawRegion;
    private float xOffset;
    private float yOffset;

    /** draw depth */
    private final int depth;

    private final Vector2 temp = new Vector2();

    /** (x,y) offset of the AABB top corner from polygon origin */
    private Vector2 boxCoordinate;

    public ParticleModel(float posX, float posY, float width, float height, float physX, float physY, float direction, float magnitude, int depth) {
        super(posX, posY, width, height);

        this.direction = direction;
        this.magnitude = magnitude;
        this.physX = physX;
        this.physY = physY;

        velocity.x =  magnitude * (float) Math.cos(direction);
        velocity.y =  magnitude * (float) Math.sin(direction);

        this.setVX(velocity.x);
        this.setVY(velocity.y);

        //setAngle(direction-((float) Math.PI/2));
        setBodyType(BodyDef.BodyType.DynamicBody);
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        fixture.isSensor = true;
        this.depth = depth;
        boxCoordinate = new Vector2(posX, posY);

    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }


        return true;
    }
    public void setAnimation(TextureRegion[] frames){
        this.animation = new Animation<>(1f/8f, frames);
    }

    /**
     * Draws the wind object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, Vector2 drawScale, float xOffset, float yOffset) {

        float ox = texture.getRegionWidth()/2.0f;
        float oy = texture.getRegionHeight()/2.0f;

        Color particleColor = new Color(1, 1, 1, 1);
        canvas.draw(texture,particleColor,ox,oy,getX(),getY(),0,1f/texture.getRegionWidth() * drawScale.x,
                1f/texture.getRegionHeight() * drawScale.y);
    }

    public void setTexture(TextureRegion t) { this.texture = t;}

    @Override
    public Vector2 getDimensions() {
        return temp.set(super.getDimension());
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(boxCoordinate).add(getX(), getY());
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    public Vector2 getPhysPos() { return temp.set(physX, physY); }
}