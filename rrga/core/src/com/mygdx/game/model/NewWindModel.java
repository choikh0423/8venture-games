package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;
import java.util.Random;

import static com.badlogic.gdx.math.Intersector.isPointInPolygon;


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
    /** The local centroid coordinate (x,y) of the wind */
    private final Vector2 centroid = new Vector2();
    /** The global center coordinate (x,y) of the wind */
    private final Vector2 center = new Vector2();
    /**
     * The magnitude of this wind's force. Invariant: magnitude > 0.
     */
    private final float magnitude;
    /**
     * The direction of this wind's force in radians. Value within [0, 2pi).
     */
    private final float direction;
    /** Particle Width */
    private static final float partWidth = 50f;
    /** Particle Height */
    private static final float partHeight = 50f;

    /** Wind Animation polygon draw regions */
    private Animation<PolygonRegion> animation;

    /** Wind Animation Elapsed Time */
    private float elapsedTime;

    private float xOffset;
    private float yOffset;

    /** Minimum Y coordinate (in body frame) at which particles will spawn */
    private float relMinY;
    /** Maximum Y coordinate (in body frame) at which particles will spawn */
    private float relMaxY;

    /** draw depth */
    private final int depth;

    /** particle radius */
    private float partRadius;
    /** wind physical area (in reality, it is the area of bounding box) */
    private float area;

    private final Vector2 temp = new Vector2();

    /** (x,y) offset of the AABB top corner from polygon origin */
    private final Vector2 boxCoordinate;
    /** Vector array of wind polygon points for checking if particle is inside polygon*/
    private final Array<Vector2> polygonPoints;


    /** Particle System Queue */
    private final ParticleModel[] queue;
    /** density: # of particles per area */
    private static final float PARTICLE_DENSITY = 1f;
    /** Inward force applied to keep particles inside the wind */
    private static final float INWARD_VELOCITY = 0.000025f;
    /** Start offset to particles */
    private static final int PARTICLE_OFFSET = 10;
    /** Actual Number of particles per wind*/
    private final int numParticles;

    /** Randomizing animation texture used for particles*/
    private final Random rand;


    public NewWindModel(JsonValue data, Vector2 scale) {
        super(data.get("dimensions").asFloatArray(), data.get("pos").getFloat(0), data.get("pos").getFloat(1));
        direction = data.getFloat("direction", 0);
        magnitude = data.getFloat("magnitude");
        rand = new Random();

        // Defining the wind object origin (x,y)
        float originX = data.get("pos").getFloat(0);
        float originY = data.get("pos").getFloat(1);

        // Defining wind object body
        setBodyType(BodyDef.BodyType.DynamicBody);
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        fixture.isSensor = true;

        this.data = data;
        this.depth = data.getInt("depth");

        // compute tight AABB top left corner
        boxCoordinate = new Vector2();
        float[] points = data.get("dimensions").asFloatArray();
        float minx = points[0];
        float maxx = points[0];
        float miny = points[1];
        float maxy = points[1];

        // Computing wind center/centroid
        float centerX = 0;
        float centerY = 0;

        for(int ii = 2; ii < points.length; ii += 2) {
            // Computing center/centroid
            centerX += points[ii];
            centerY += points[ii+1];

            // Finding max, min points in global coordinate
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

        partRadius = 0;
        centroid.set(centerX / (points.length/2f), centerY / (points.length/2f));
        center.set(originX + centroid.x, originY + centroid.y);


        // For finding Radius + checking in polygon
        polygonPoints = new Array<Vector2>(points.length/2);

        // For transforming global coordinate into wind body frame
        float transformX = -(float)Math.sin(direction);
        float transformY = (float)Math.cos(direction);
        float transformAng = -center.y*(float)Math.cos(direction) + center.x*(float)Math.sin(direction);

        relMaxY = Float.NEGATIVE_INFINITY;
        relMinY = Float.POSITIVE_INFINITY;
        for(int ii = 0; ii < points.length; ii += 2) {

            // Relative Minimum and Maximum in wind body frame
            float relY = transformX*(originX + points[ii]) + transformY*(originY + points[ii+1]) + transformAng;
            relMaxY = Math.max(relY, relMaxY);
            relMinY = Math.min(relY, relMinY);

            // For Checking In Polygon
            polygonPoints.add(new Vector2(originX + points[ii], originY + points[ii+1]));

            // Finding Radius
            float distX = points[ii] - centroid.x;
            float distY = points[ii+1] - centroid.y;
            this.partRadius = Math.max(partRadius, (float)Math.sqrt(distX * distX + distY * distY));
        }

        // Computing width and height of bounding box - currently inaccurate representation of actual wind area
        float width = (maxx - minx);
        float height = (maxy - miny);
        area = width * height;

        // Computing total number of particles & sampling particles
        numParticles = (int) (area * PARTICLE_DENSITY);
        queue = new ParticleModel[numParticles];
        int particleCount = 0;

        while (particleCount < numParticles) {

            // Random Sampling that goes across the wind area (WILL NOT SPAWN IN AREA THAT DOES NOT GO ACROSS THE WIND)
            Vector2 sample = particleRandomSample();

            float sampleX = sample.x;
            float sampleY = sample.y;

            int offset = particleCount % PARTICLE_OFFSET;

            queue[particleCount] = new ParticleModel(sampleX, sampleY, this.direction, this.magnitude, this.depth, offset);
            queue[particleCount].setParticleSize(partWidth, partHeight);
            particleCount += 1;
        }
    }

    /**
     * Samples wind particles in the wind
     * NOTE: particles will be sampled in the "start area" of wind and will not be spawned in locations that are
     * not visible.
     *
     * @return Vector2 (x,y) sampled point (this is not an allocator, same vector returned every time)
     */
    private Vector2 particleRandomSample() {
        float sampleY = (float)Math.random() * (relMaxY-relMinY) + relMinY;
        float sampleX = -(float)Math.sqrt(partRadius*partRadius - sampleY*sampleY);

        float sampleGlobX = (float)Math.cos(direction) * sampleX - (float)Math.sin(direction) * sampleY + center.x;
        float sampleGlobY = (float)Math.sin(direction) * sampleX + (float)Math.cos(direction) * sampleY + center.y;
        return temp.set(sampleGlobX, sampleGlobY);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

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

    /** Sets wind animation */
    public void setAnimation(TextureRegion[] frames){

        xOffset = 0;
        yOffset = 0;
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

        PolygonRegion[] drawRegions = new PolygonRegion[frames.length];
        for (int ii = 0; ii < frames.length; ii++){
            PolygonRegion p = new PolygonRegion(frames[ii], verts,region.getTriangles());
            drawRegions[ii] = p;
        }

        this.animation = new Animation<>(1f/8f, drawRegions);
    }

    /** Sets particle texture(still image) with alternating leaf and cloud texture
     * NOTE: used for previous implementation and is remains for future usage */
    public void setParticleTexture(TextureRegion t_cloud, TextureRegion t_leaf) {
        int leaf_counter = 0;
        for (int i = 0; i < numParticles; i++) {
            if (leaf_counter > 7) {
                queue[i].setTexture(t_leaf);
                leaf_counter = 0;
            } else {
                queue[i].setTexture(t_cloud);
                leaf_counter += 1;
            }
        }
    }

    /** Sets particle animation */
    public void setParticleAnimation(Texture[] t, int i) {
        queue[i].setAnimation(t);
    }

    /** Update loop for wind: currently updates particles */
    public void update(float dt) {
        for (int i = 0; i < numParticles; i++) {

            Vector2 partPos = queue[i].getPos();

            // Applying inward velocity that pushes particles towards center and prevents them from falling out
            float velScale = INWARD_VELOCITY * ((partPos.x * (float) Math.cos(direction) + partPos.y * (float) Math.sin(direction)) / partRadius);
            float transformX = -(float)Math.sin(direction);
            float transformY = (float)Math.cos(direction);
            float transformAng = -center.y*(float)Math.cos(direction) + center.x*(float)Math.sin(direction);

            float velDirection = Math.signum(transformX*partPos.x + transformY*partPos.y + transformAng);

            // Update particles and check if inbound
            queue[i].update(dt, velScale, velDirection);
            checkInBound(queue[i]);

            if (!checkInBound(queue[i])) {
                // Take new sample in the start region of the wind
                Vector2 newSample = particleRandomSample();

                // Make particle invisible once out of region
                queue[i].setLife(0);
                queue[i].setPos(newSample);
                queue[i].setTextureIndex(rand.nextInt(3));

                // Random offset for particle start
                queue[i].setStartOffset(rand.nextInt(100));
            }
        }
    }

    /** Find reflected point over the line perpendicular to direction of wind (remains for future usage)
     * NOTE: this code can be used for wrapping around certain objects */
    public Vector2 findReflectedPoint(Vector2 pos) {


        // WILL CRASH IF TANGENT THETA IS ZERO
        float B = -1 / (float)Math.tan(direction);
        float A = -1;
        float C = -A * center.y - B * center.x;

        float x2 =( pos.x * (A*A - B*B) - 2*B*(A*pos.y + C)) / (A*A + B*B);
        float y2 = ( pos.y * (B*B - A*A) - 2*A*(B*pos.x + C)) / (A*A + B*B);

        return new Vector2(x2, y2);
    }

    /** Checks if the particle is in bound - within the circle that bounds the wind object*/
    public boolean checkInBound(ParticleModel p) {
        Vector2 partPos = p.getPos();
        float distX = partPos.x - this.center.x;
        float distY = partPos.y - this.center.y;
        float dist = (float) Math.sqrt(distX * distX + distY * distY);

        return dist <= partRadius + 0.5;
    }

    /**
     * Draws the wind object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        //TODO fix wrapping issue (zhi: ????)

        // draw background fill-texture animation
        elapsedTime += Gdx.graphics.getDeltaTime();
        PolygonRegion p = animation.getKeyFrame(elapsedTime, true);

        Color tint = new Color(1,1,1,0.5f);
        canvas.draw(p, tint, -xOffset, -yOffset,getX()*drawScale.x + xOffset,getY()*drawScale.y + yOffset,
                    direction-((float) Math.PI/2),1,1);

        // Draw Particles
        for (int i = 0; i < numParticles; i++) {

            // Updating Particles - if left wind area, particle fades out
            if (!isPointInPolygon(polygonPoints, queue[i].getPos())) {
                queue[i].setDead();
            } else if (!queue[i].getIsAlive()) {
                queue[i].setAlive();
            }

            // Draw particles
            queue[i].setDrawScale(drawScale);
            queue[i].draw(canvas);
        }
    }

    public int getNumParticles() {
        return this.numParticles;
    }

    @Override
    public Vector2 getDimensions() {
        return temp.set(super.getDimension());
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(getPosition()).add(boxCoordinate);
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

}