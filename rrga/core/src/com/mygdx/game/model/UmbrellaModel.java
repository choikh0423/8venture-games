package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.BoxObstacle;

public class UmbrellaModel extends BoxObstacle {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The factor to multiply by the input */
    private final float force;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;
    private PolygonShape sensorShape;
    /** Which direction is the umbrella facing */
    public boolean faceRight;
    /** Whether the umbrella is open or closed */
    private boolean open;
    /** The current angular rotation of the umbrella */
    private float turning;
    /** The scale to multiply the texture by for drawing */
    private float textureScale;
    /** Ratio of horizontal speed to conserve when closing the umbrella */
    private float closedMomentum = 0;

    /** texture asset for open umbrella */
    private TextureRegion openTexture;

    /** texture asset for closed umbrella */
    private TextureRegion closedTexture;

    // <=============================== Animation objects start here ===============================>
    /** checks if the umbrella was opened or closed
     *  -1: closed, 0: stationary, 1: opened */
    private int openMode = 0;

    /** Umbrella open film texture */
    private Texture openAnimationTexture;

    /** Umbrella open animation frames */
    private TextureRegion[][] openTmpFrames;

    /** Umbrella open animation frames */
    private TextureRegion[] openAnimationFrames;

    /** Umbrella open animation*/
    private Animation<TextureRegion> openAnimation;

    /** Umbrella close animation: Reversed open animation frames */
    private Animation<TextureRegion> closeAnimation;

    /** Umbrella open animation elapsed time */
    float openElapsedTime;

    /** Current remaining frame count for animation */
    private int currentFrameCount;

    /** Default Animation Frame Count
     *  NOTE: This needs to change if animation frame duration changes */
    private int OPEN_ANIMATION_FRAMECOUNT = 18;


    public UmbrellaModel(JsonValue data, Vector2 pos, float width, float height) {
        super(	pos.x, pos.y,
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(false);
        //if we don't do this, the umbrella doesn't stay in position
        setGravityScale(0);
        //makes sure the umbrella doesn't collide with platforms. Handled in gamplaycontroller presolve
        //setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        force = data.getFloat("force", 0);
        textureScale = data.getFloat("texturescale", 1.0f);
        sensorName = "umbrellaSensor";
        this.data = data;
        faceRight = true;
        setName("umbrella");
        open = false;
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        FixtureDef sensorDef = new FixtureDef();
        Vector2 sensorCenter = new Vector2(0, 1*getHeight()/8);
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(getWidth()/2, getHeight()/8, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(sensorName);
        return true;
    }

    /** Returns the angle away from the x-axis of the umbrella in radians.
     * THIS IS THE VALUE YOU SHOULD USE FOR WIND INTERACTIONS, ETC. */
    public float getRotation(){return getAngle()+(float)Math.PI/2;}

    /** Returns whether the umbrella is open */
    public boolean isOpen(){return open;}
    /** Sets if this umbrella is open or closed
     * @param open whether the umbrella is open */
    public void setOpen(boolean open){this.open = open;}
    public float getForce(){return force;}
    /** Returns the ratio of horizontal speed to return when closing the umbrella */
    public float getClosedMomentum(){return closedMomentum;}
    /** Sets the ratio of horizontal speed to return when closing the umbrella */
    public void setClosedMomentum(float closedMomentum){this.closedMomentum = closedMomentum;}

    /** Sets how much this umbrella is turning
     * @param value left/right turning of this umbrella.*/
    public void setTurning(float value){turning = value;}

    /**
     * This method is used to count the remaining animation frame count
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns
        if(currentFrameCount!=0) currentFrameCount--;

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        //canvas.setBlendState(GameCanvas.BlendState.OPAQUE);
        if (openMode == -1) {
            // Playing umbrella close animation
            openElapsedTime += Gdx.graphics.getDeltaTime();
            canvas.draw(closeAnimation.getKeyFrame(openElapsedTime, false), Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),
                    effect*textureScale/32f * drawScale.x,
                    textureScale/32f * drawScale.y);

            // Reset to default openMode
            if (currentFrameCount == 0) {
                openMode = 0;
            }
        } else if (openMode == 1) {
            // Playing umbrella open animation
            openElapsedTime += Gdx.graphics.getDeltaTime();
            canvas.draw(openAnimation.getKeyFrame(openElapsedTime, false), Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),
                    effect*textureScale/32f * drawScale.x,
                    textureScale/32f * drawScale.y);

            // Reset to default openMode
            if (currentFrameCount == 0) {
                openMode = 0;
            }
        } else if (openMode == 0) {
            if (texture == openTexture){
                canvas.draw(openAnimationFrames[openAnimationFrames.length - 1], Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*textureScale/32f * drawScale.x,textureScale/32f * drawScale.y);
            }else {
                canvas.draw(openAnimationFrames[0], Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect * textureScale / 32f * drawScale.x, textureScale / 32f * drawScale.y);
            }
        }
        canvas.draw(texture, Color.BLUE,  getX()*drawScale.x,getY()*drawScale.y, 1, 1);
        canvas.setBlendState(GameCanvas.BlendState.NO_PREMULT);
    }

    /**
     * Draws the outline of the umbrella.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    /**
     * sets the texture used to draw an opened umbrella
     * @param openTexture texture asset for open umbrella
     */
    public void setOpenTexture(TextureRegion openTexture) {
        this.openTexture = openTexture;
    }

    /**
     * sets the texture used to draw a closed umbrella
     * @param closedTexture texture asset for closed umbrella
     */
    public void setClosedTexture(TextureRegion closedTexture) {
        this.closedTexture = closedTexture;
    }

    /**
     * Sets umbrella open animation
     * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
     * */
    public void setOpenAnimation(Texture texture) {
        this.openAnimationTexture = texture;
        this.openTmpFrames = TextureRegion.split(openAnimationTexture, 469, 469 );
        this.openAnimationFrames = new TextureRegion[6];

        // Setting animation frames
        int index = 0;
        for (int i=0; i<1; i++) {
            for (int j=0; j<6; j++) {
                this.openAnimationFrames[index] = openTmpFrames[i][j];
                index++;
            }
        }

        // NOTE: If changing frameDuration, make sure to change OPEN_ANIMATION_FRAMECOUNT accordingly.
        this.openAnimation = new Animation<>(1f/20f, openAnimationFrames);
        openAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        this.closeAnimation = new Animation<>(1f/20f, openAnimationFrames);
        closeAnimation.setPlayMode(Animation.PlayMode.REVERSED);
    }

    /**
     * sets the texture to be opened umbrella for drawing purposes.
     *
     * No update occurs if the current texture is already the opened texture.
     */
    public void useOpenedTexture(){
        if (texture != openTexture){
            setTexture(openTexture);
            currentFrameCount = OPEN_ANIMATION_FRAMECOUNT;
            openMode = 1;
            openElapsedTime = 0;
        }
    }

    /**
     * sets the texture to be closed umbrella for drawing purposes.
     *
     * No update occurs if the current texture is already the closed texture
     */
    public void useClosedTexture(){
        if (texture != closedTexture){
            setTexture(closedTexture);
            currentFrameCount = OPEN_ANIMATION_FRAMECOUNT;
            openMode = -1;
            openElapsedTime = 0;
        }
    }
}
