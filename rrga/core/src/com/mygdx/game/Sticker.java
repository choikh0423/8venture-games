package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

/**
 * A sticker is a texture tile that has no associated object.
 */
public class Sticker {
    /** the physics dimensions of sticker's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the physics world position of top left corner coordinate of sticker AABB */
    private final Vector2 corner = new Vector2();

    private final Vector2 pos = new Vector2();

    private final TextureRegion texture;

    public float getWidth(){ return dimensions.x; }

    public float getHeight(){ return dimensions.y; }

    /**
     * @return world x-coordinate of AABB top left corner
     */
    public float getAABBx(){ return corner.x; }

    /**
     * @return world y-coordinate of AABB top left corner
     */
    public float getAABBy(){ return corner.y; }

    /**
     * creates a sticker at position (x,y) whose AABB top left corner is (x+bx, y+by) and has given width and height.
     * a sticker is simply a texture.
     * @param x position x coordinate
     * @param y position y coordinate
     * @param AABB the JSON data containing box data
     * @param texture the texture (with flipping already applied)
     */
    public Sticker(float x, float y, JsonValue AABB, TextureRegion texture){
        pos.set(x,y);
        // AABB[0 ... 3] = {corner bx, corner by, AABB physics width, AABB physics height}
        float[] aabb = AABB.asFloatArray();
        dimensions.set(aabb[2], aabb[3]);
        corner.set(aabb[0],aabb[1]).add(pos);
        this.texture = texture;
    }

    public void draw(GameCanvas canvas, Vector2 drawScale) {
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth() / 2f, texture.getRegionHeight() / 2f,
                pos.x * drawScale.x, pos.y * drawScale.y, 0,
                dimensions.x / texture.getRegionWidth() * drawScale.x,
                dimensions.y / texture.getRegionHeight() * drawScale.y);
    }
}
