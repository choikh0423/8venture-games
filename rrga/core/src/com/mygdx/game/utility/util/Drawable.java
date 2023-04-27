package com.mygdx.game.utility.util;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.GameCanvas;

/**
 * A Drawable is a texture/filmstrip/animation/etc contained in a bounding box. <br>
 * If this bounding box is not within the bounding box of the screen, the drawing cannot be seen and thus should not be
 * drawn.<br>
 * A collection of methods are provided to assist layered drawing.
 */
public interface Drawable{

    /** width and height (standardized units) <br>
     * NOTE: vector should not be used as an allocator because the same vector is returned each time.
     */
    Vector2 getDimensions();

    /** position of top left bounding box corner. <br>
     * NOTE: vector should not be used as an allocator because the same vector is returned each time.
     * */
    Vector2 getBoxCorner();

    /** the drawing depth (a greater depth means further away from the camera, depth of 1 is usually first layer.) */
    int getDepth();

    /**
     * set the draw scale of this drawing (by copying contents)
     * @param scale a reference to a scale vector.
     */
    void setDrawScale(Vector2 scale);

    void draw(GameCanvas canvas);
}