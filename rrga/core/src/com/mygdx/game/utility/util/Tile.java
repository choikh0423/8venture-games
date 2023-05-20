package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A tile is a square texture region cut from a tileset.
 * To render a tile correctly, retrieve the rotation and flipping properties to be used by a drawing context.
 */
public class Tile {

    private boolean flipX;

    private boolean flipY;

    private float angle;

    /** this should be a reference to the filmstrip (tileset but optimized for memory) */
    private final FilmStrip textureFilmStrip;

    private final int frameId;

    public Tile(FilmStrip textureFilmStrip, int frameId){
        this.textureFilmStrip = textureFilmStrip;
        this.frameId = frameId;
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

    public void setFlip(boolean flipX, boolean flipY){
        this.flipX = flipX;
        this.flipY = flipY;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    /**
     * @return reference to tile texture region selected from tileset
     */
    public TextureRegion getRegion(){
        textureFilmStrip.setFrame(this.frameId);
        // cut out region starting from second left corner of each 130x130 tile
        int x = textureFilmStrip.getRegionX() + 1;
        int y = textureFilmStrip.getRegionY() + 1;
        int width = textureFilmStrip.getRegionWidth() - 2;
        int height = textureFilmStrip.getRegionHeight() - 2;
        textureFilmStrip.setRegion(x, y, width, height);
        return textureFilmStrip;
    }

    /**
     * allocates an texture region object
     * @return deep copy of reference to tile texture region selected from tileset
     */
    public TextureRegion getRegionCopy(){
        return new TextureRegion(this.getRegion());
    }

}
