package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

/**
 * A sticker is a texture tile that has no associated object.
 */
public class Sticker implements Drawable {
    /** the physics dimensions of sticker's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the physics world position of top left corner coordinate of sticker AABB */
    private final Vector2 corner = new Vector2();

    private final Vector2 pos = new Vector2();

    private final int depth;

    private final TextureRegion texture;

    private final Vector2 drawScale;

    private final Vector2 cache;

    /**
     * creates a sticker at position (x,y) whose AABB top left corner is (x+bx, y+by) and has given width and height.
     * a sticker is simply a texture.
     * @param x position x coordinate
     * @param y position y coordinate
     * @param depth sticker draw depth
     * @param AABB the JSON data containing box data
     * @param texture the texture (with flipping already applied)
     */
    public Sticker(float x, float y, int depth, JsonValue AABB, TextureRegion texture){
        pos.set(x,y);
        // AABB[0 ... 3] = {corner bx, corner by, AABB physics width, AABB physics height}
        float[] aabb = AABB.asFloatArray();
        dimensions.set(aabb[2], aabb[3]);
        corner.set(aabb[0],aabb[1]).add(pos);
        this.texture = texture;
        this.depth = depth;
        this.drawScale = new Vector2(1,1);
        this.cache = new Vector2();
    }

    // implementations for DRAWABLE Interface

    @Override
    public Vector2 getDimensions() {
        return cache.set(dimensions);
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
                pos.x * drawScale.x, pos.y * drawScale.y, 0,
                dimensions.x / texture.getRegionWidth() * drawScale.x,
                dimensions.y / texture.getRegionHeight() * drawScale.y);
    }
}
