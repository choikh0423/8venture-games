package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameCanvas;
import static java.lang.Math.*;


/**
 * A model for particle effect
 */
public class ParticleModel {
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private final Vector2 pos = new Vector2();
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private final Vector2 velocity = new Vector2();

    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private final Vector2 tempdelt = new Vector2();
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private final Vector2 originalVel;

    /**
     * The magnitude of this wind's force. Invariant: magnitude > 0.
     */
    private final float magnitude;
    /**
     * The direction of this wind's force in radians. Value within [0, 2pi).
     */
    private final float direction;

    private final Array<Animation<TextureRegion>> animation;

    private float elapsedTime;

//    /** draw depth */
//    private final int depth;

    private int life;

    private boolean isalive;

//    private final Vector2 temp = new Vector2();
    private final Vector2 drawScale = new Vector2();
    private final Vector2 partSize = new Vector2();
    /** Decides when particles get start drawn - gives different start time */
    private int startOffset;
    private int countOffset;
    private static final int MAX_LIFE = 10;
    private static final int OFFSET_CONST = 50;
    private static final float VELOCITY_SCALE = 1/15f;

    private TextureRegion texture;

    private int textureIndex;


    public ParticleModel(float posX, float posY, float direction, float magnitude, int depth, int offset) {

        this.pos.x = posX;
        this.pos.y = posY;

        this.direction = direction;
        this.magnitude = magnitude;

        velocity.x =  magnitude * (float) Math.cos(direction) * VELOCITY_SCALE;
        velocity.y =  magnitude * (float) Math.sin(direction) * VELOCITY_SCALE;

        this.startOffset = offset * OFFSET_CONST;
        this.countOffset = offset * OFFSET_CONST;


        originalVel = new Vector2(
                magnitude * (float) Math.cos(direction) * VELOCITY_SCALE,
                magnitude * (float) Math.sin(direction) * VELOCITY_SCALE
        );

        // initialize array of animations, pad the animation with null animations until animations are set
        this.animation = new Array<>(3); //array of 3 animations
        animation.add(null);
        animation.add(null);
        animation.add(null);
        // this.depth = depth;

    }

    /** Sets particle animation */
    public void setAnimation(Texture[] t){
        for (int i = 0; i < 3; i++ ) {
            TextureRegion[][] tempFrames = TextureRegion.split(t[i], 64, 64);
            TextureRegion[] frames = new TextureRegion[8];

            // Placing animation frames in order
            int index = 0;
            for (TextureRegion[] tempFrame : tempFrames) {
                for (int k = 0; k < tempFrames[0].length; k++) {
                    frames[index] = tempFrame[k];
                    index++;
                }
            }

            // Adjust animation frame here
            animation.set(i, new Animation<>(1f / 8f, frames));
        }
    }

    /**
     * Draws the particle object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        // Adjusts alpha value according to particle life
        Color particleColor = new Color(1, 1, 1, (float)life/MAX_LIFE);

        elapsedTime += Gdx.graphics.getDeltaTime();

        TextureRegion t = animation.get(textureIndex).getKeyFrame(elapsedTime, true);

        float ox = t.getRegionWidth()/2.0f;
        float oy = t.getRegionHeight()/2.0f;

        canvas.draw(t, particleColor, ox, oy, this.pos.x * drawScale.x, this.pos.y * drawScale.y, 0, partSize.x / t.getRegionWidth(),
                partSize.y / t.getRegionHeight());
    }

    /** Updates particle: velocity, position, and life */
    public void update(float dt, float velScale, float velDirection) {

        // Applies random velocity change in direction perpendicular to wind direction
        // TODO: NEEDS MORE RANDOMNESS
        float randVel = (float) Math.random() * 0.5f - 0.25f;
        tempdelt.x = randVel * (float)Math.sin(direction);
        tempdelt.y = randVel * (float)Math.cos(direction);
        velocity.set(originalVel.x + tempdelt.x, originalVel.y + tempdelt.y);

        // Deducts start offset
        if (this.countOffset > 0) {
            this.life = 0;
            this.countOffset -= 1;
        } else {
            // Updates particle life
            if (!isalive) {
                life = max(0, life-1);

            } else {
                life = min(MAX_LIFE, life+1);
            }

            // Update velocity w/ inward velocity
            velocity.x = velocity.x + velScale * velDirection * (float)Math.sin(direction);
            velocity.y = velocity.y - velScale * velDirection * (float)Math.cos(direction);

            // Update position
            pos.x = pos.x + velocity.x * dt;
            pos.y = pos.y + velocity.y * dt;
        }



    }

    // TODO: Drawable override methods may not be correctly implemented
    //  UPDATE: particles do not have to implement drawable (they are owned by wind not by world) --zhi

    //    @Override
//    public void drawDebug(GameCanvas canvas) {
//    }

//    @Override
//    public int getDepth() {
//        return this.depth;
//    }

//    @Override
//    public Vector2 getDimensions() {
//        return this.pos;
//    }

//    @Override
//    public Vector2 getBoxCorner() {
//        return this.pos;
//    }

    public void setDrawScale(Vector2 scale) {
        drawScale.set(scale);
    }

    public void setTexture(TextureRegion texture) { this.texture = texture; }

    public void setParticleSize(float partWidth, float partHeight) { this.partSize.set(partWidth, partHeight); }

    public Vector2 getPos() { return this.pos; }
    public void setPos(Vector2 pos) { this.pos.set(pos); }

    public void setDead() { this.isalive = false; }

    public void setAlive() { this.isalive = true; }

    public void setLife(int life) { this.life = life; }

    public void setStartOffset() { this.countOffset = this.startOffset; }

    public Boolean getIsAlive() { return isalive; }

    public void setTextureIndex(int textureIndex) { this.textureIndex = textureIndex; }

}