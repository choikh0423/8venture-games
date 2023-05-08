package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;

import java.util.Random;

import static java.lang.Math.*;


/**
 * A model for wind objects.
 * Currently extends PolygonObstacle to allow for different shaped wind gusts, but may want to change later
 * to make drawing manageable/easier
 */
public class ParticleModel implements Drawable {
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private Vector2 pos = new Vector2();
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private Vector2 velocity = new Vector2();

    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private Vector2 tempdelt = new Vector2();
    /**
     * The velocity of this wind particle. Value: [vx, vy];
     */
    private Vector2 originalVel;

    /**
     * The magnitude of this wind's force. Invariant: magnitude > 0.
     */
    private float magnitude;
    /**
     * The direction of this wind's force in radians. Value within [0, 2pi).
     */
    private float direction;

    private Animation<TextureRegion>[] animation = new Animation[3];

    private float elapsedTime;

    private PolygonRegion drawRegion;
    private float xOffset;
    private float yOffset;

    /** draw depth */
    private final int depth;

    private int life;

    private int MAX_LIFE = 10;

    private boolean isalive;

    private final Vector2 temp = new Vector2();
    private Vector2 drawScale = new Vector2();
    private Vector2 offset = new Vector2();
    private Vector2 partSize = new Vector2();
    /** Decides when particles get start drawn - gives different start time */
    private int startOffset;
    private int OFFSET_CONST = 10;

    private TextureRegion texture;

    private int textureIndex;




    public ParticleModel(float posX, float posY, float direction, float magnitude, int depth, int offset) {

        this.pos.x = posX;
        this.pos.y = posY;

        this.direction = direction;
        this.magnitude = magnitude;

        velocity.x =  magnitude * (float) Math.cos(direction) / 10;
        velocity.y =  magnitude * (float) Math.sin(direction) / 10;

        this.startOffset = offset * OFFSET_CONST;


        originalVel = new Vector2(magnitude * (float) Math.cos(direction) / 10, magnitude * (float) Math.sin(direction) / 10);

        this.depth = depth;

    }

    /** Sets particle animation */
    public void setAnimation(Texture[] t){
        for (int i = 0; i < 3; i++ ) {
            TextureRegion[][] tempFrames = TextureRegion.split(t[i], 64, 64);
            TextureRegion[] frames = new TextureRegion[8];

            // Placing animation frames in order
            int index = 0;
            for (int j = 0; j < tempFrames.length; j++) {
                for (int k = 0; k < tempFrames[0].length; k++) {
                    frames[index] = tempFrames[j][k];
                    index++;
                }
            }

            // Adjust animation frame here
            animation[i] = new Animation<>(1f / 8f, frames);
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

        TextureRegion t = animation[textureIndex].getKeyFrame(elapsedTime, true);

        float ox = t.getRegionWidth()/2.0f;
        float oy = t.getRegionHeight()/2.0f;

        canvas.draw(t, particleColor, ox, oy, this.pos.x * drawScale.x, this.pos.y * drawScale.y, 0, partSize.x / t.getRegionWidth(),
                partSize.y / t.getRegionHeight());
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
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
        if (this.startOffset > 0) {
            this.life = 0;
            this.startOffset -= 1;
            System.out.println(startOffset);
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
    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public Vector2 getDimensions() {
        return this.pos;
    }

    @Override
    public Vector2 getBoxCorner() {
        return this.pos;
    }

    @Override
    public void setDrawScale(Vector2 scale) {
        drawScale.set(scale);
    }

    public void setTexture(TextureRegion texture) { this.texture = texture; }

    public void setOffset(float xOffset, float yOffset) { this.offset.set(xOffset, yOffset); }

    public void setParticleSize(float partWidth, float partHeight) { this.partSize.set(partWidth, partHeight); }

    public Vector2 getPos() { return this.pos; }
    public Vector2 setPos(Vector2 pos) { return this.pos = pos; }

    public void setDead() { this.isalive = false; }

    public void setAlive() { this.isalive = true; }

    public void setLife(int life) { this.life = life; }

    public void setStartOffset(int offset) { this.startOffset = offset; }

    public Boolean getIsAlive() { return isalive; }

    public void setTextureIndex(int textureIndex) { this.textureIndex = textureIndex; }

}