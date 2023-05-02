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
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;


/**
 * A model for wind objects.
 * Currently extends PolygonObstacle to allow for different shaped wind gusts, but may want to change later
 * to make drawing manageable/easier
 */
public class NewWindModel extends PolygonObstacle implements Drawable {

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

    /** Particle Width */
    private float partWidth = 5f;
    /** Particle Heigth */
    private float partHeight = 5f;

    private Animation<TextureRegion> animation;
    private float elapsedTime;

    private PolygonRegion drawRegion;
    private float xOffset;
    private float yOffset;

    /** draw depth */
    private final int depth;

    /** particle radius */
    private float partRadius;

    private final Vector2 temp = new Vector2();

    /** (x,y) offset of the AABB top corner from polygon origin */
    private Vector2 boxCoordinate;


    /** Particle System Queue */
    private ParticleModel[] queue;

    /** Number of particles per wind*/
    private int NUM_PARTICLES = 30;

    /** Actual Number of particles per wind*/
    private int particleCount;


    public NewWindModel(JsonValue data) {
        super(data.get("dimensions").asFloatArray(), data.get("pos").getFloat(0), data.get("pos").getFloat(1));
        direction = data.getFloat("direction", 0);
        magnitude = data.getFloat("magnitude");

        float centerX = data.get("pos").getFloat(0);
        float centerY = data.get("pos").getFloat(1);

        // Particle Spawn Radius
//        partRadius = 0;
//        for(int i = 0; i<region.getVertices().length; i+=2) {
//            float distX = region.getVertices()[i]-centerX;
//            float distY = region.getVertices()[i+1]-centerY;
//            partRadius = Math.min(partRadius, (float)Math.sqrt(distX * distX + distY * distY));
//            System.out.println(partRadius);
//        }
        setBodyType(BodyDef.BodyType.DynamicBody);
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);

        fixture.isSensor = true;
        this.data = data;
        this.depth = data.getInt("depth");

        // compute tight AABB top right corner
        boxCoordinate = new Vector2();
        float[] points = data.get("dimensions").asFloatArray();
        float minx = points[0];
        float maxx = points[0];
        float miny = points[1];
        float maxy = points[1];

        for(int ii = 2; ii < points.length; ii += 2) {
            if (points[ii] < minx) {
                minx = points[ii];
            } else if (points[ii] > maxx) {
                maxx = points[ii];
            }
            if (points[ii+1] > maxy) {
                maxy = points[ii+1];
            } else if (points[ii+1] < miny) {
                miny = points[ii+1];
            }
        }
        boxCoordinate.set(minx, maxy);

        // INITIALIZE WIND PARTICLES
        queue = new ParticleModel[NUM_PARTICLES];
        float width = maxx - minx;
        float height = maxy - miny;
        float ratio = height/width;

        float area = width * height;
        float distX = (float)Math.sqrt((area/NUM_PARTICLES) / ratio);
        float distY = (float)Math.sqrt((area/NUM_PARTICLES) * ratio);

        particleCount = 0;

        // Revisit for precision

        for (int i=1; i <=(int)width/distX; i++) {
           for (int j=1; j <= (int)height/distY; j++) {
                float partPosX = centerX + distX * i;
                float partPosY = centerY + distY * j;

                queue[particleCount] = new ParticleModel(partPosX, partPosY, partWidth, partHeight, this.direction, this.magnitude, this.depth);

                particleCount += 1;

           }
       }

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

    public ParticleModel[] getParticles() {
        return queue;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setAnimation(TextureRegion[] frames){
        this.animation = new Animation<>(1f/8f, frames);
    }

    public void setParticleTexture(TextureRegion t) {
        for (int i = 0; i < particleCount; i++) {
            queue[i].setTexture(t);
        }
    }

    /**
     * Draws the wind object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        //TODO fix wrapping issue
        elapsedTime += Gdx.graphics.getDeltaTime();

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
        TextureRegion t = animation.getKeyFrame(elapsedTime, true);

        PolygonRegion p = new PolygonRegion(t, verts,region.getTriangles());

        canvas.draw(p, Color.WHITE, -xOffset, -yOffset,getX()*drawScale.x + xOffset,getY()*drawScale.y + yOffset,
                    direction-((float) Math.PI/2),1,1);

        for (int i = 0; i < particleCount; i++) {
            queue[i].draw(canvas, drawScale, xOffset, yOffset);
        }
    }

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
}