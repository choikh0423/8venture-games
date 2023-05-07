package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A tile is a square texture region with flipping already applied.
 * To render a tile correctly, retrieve the rotation to be used by a SpriteBatch.
 */
public class Tile extends TextureRegion {

    private float angle;

    public Tile(Texture t){
        super(t);
        angle = 0;
    }

    /**
     * set the COUNTER-CLOCKWISE rotation of this tile in RADIANS
     * @param angle one of 0, PI/2, PI, 3PI/2
     */
    public void setRotation(float angle){
        this.angle = angle;
    }

    /**
     * get the COUNTER-CLOCKWISE rotation of this tile in RADIANS
     */
    public float getRotationRad(){
        return this.angle;
    }

}
