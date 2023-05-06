package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

/**
 * A sticker is a texture tile that has no associated object. <br>
 * Stickers can be rotated as part of initialization, but no rotational update is supported.
 */
public class Sticker implements Drawable {
    /** the physics dimensions of sticker's AABB */
    private final Vector2 boxDimensions = new Vector2();

    /** the physics world position of top left corner coordinate of sticker AABB */
    private final Vector2 corner = new Vector2();

    /** the physics dimensions of sticker (actual dimensions when axis-aligned) */
    private final Vector2 size = new Vector2();

    private final Vector2 pos = new Vector2();

    private final int depth;

    private final float angle;

    private final TextureRegion texture;

    private final Vector2 drawScale;

    private final Vector2 cache;

    /** shape of bounding box in debug mode*/
    private final PolygonShape shape;

    /**
     * creates a sticker at position (x,y) whose AABB top left corner is (x+bx, y+by) and has given width and height.<br>
     * a sticker is simply a texture (flipping enabled) and rotated if angle is non-zero.
     * @param x position x coordinate
     * @param y position y coordinate
     * @param angle rotational angle in radians
     * @param depth sticker draw depth
     * @param AABB the JSON data containing box data
     * @param texture the texture (with flipping already applied)
     */
    public Sticker(float x, float y, float angle, int depth, JsonValue AABB, TextureRegion texture){
        pos.set(x,y);
        // AABB[0 ... 3] = {corner bx, corner by, AABB physics width, AABB physics height}
        float[] aabb = AABB.asFloatArray();
        this.angle = angle;
        float width = aabb[2];
        float height = aabb[3];
        size.set(width, height);
        if (this.angle != 0){
            // need to compute new AABB (preferable rotational angles are pi/2, pi, 3pi/2)
            double psin = Math.abs(Math.sin(angle));
            double pcos = Math.abs(Math.cos(angle));
            // set new box dimensions
            aabb[2] = (float) (height * psin + width * pcos);
            aabb[3] = (float) (width * psin + height * pcos);
            // new box left corner (cartesian coordinate, relative to center of sticker)
            aabb[0] = -aabb[2]/2f;
            aabb[1] = aabb[3]/2f;
        }
        boxDimensions.set(aabb[2], aabb[3]);
        corner.set(aabb[0],aabb[1]).add(pos);
        this.texture = texture;
        this.depth = depth;
        this.drawScale = new Vector2(1,1);
        this.cache = new Vector2();
        shape = new PolygonShape();
        float[] vertices = new float[]{
                -aabb[2]/2f, -aabb[3]/2f, -aabb[2]/2f, aabb[3]/2f,
                aabb[2]/2f, aabb[3]/2f, aabb[2]/2f,-aabb[3]/2f
        };
        shape.set(vertices);
    }

    // implementations for DRAWABLE Interface

    @Override
    public Vector2 getDimensions() {
        return cache.set(boxDimensions);
    }

    @Override
    public Vector2 getBoxCorner() {
        return cache.set(corner);
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public void setDrawScale(Vector2 scale) {
        drawScale.set(scale);
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth() / 2f, texture.getRegionHeight() / 2f,
                pos.x * drawScale.x, pos.y * drawScale.y, angle,
                size.x / texture.getRegionWidth() * drawScale.x,
                size.y / texture.getRegionHeight() * drawScale.y);
    }

    @Override
    public void drawDebug(GameCanvas canvas){
        canvas.drawPhysics(shape,Color.FOREST,pos.x, pos.y,0,drawScale.x,drawScale.y);
    }
}
