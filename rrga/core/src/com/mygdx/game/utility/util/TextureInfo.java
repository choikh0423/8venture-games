package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A TextureInfo can be used as a container to hold texture information (to distinguish whether a texture
 * should be used as an animation or to be used as a single image, ie: a texture region).
 */
public class TextureInfo {

    private TextureRegion textureRegion;

    private Texture texture;
    private final int rows;
    private final int cols;

    private final float frameDuration;

    /** record this is a texture filmstrip (many frames) */
    public TextureInfo(Texture texture, int rows, int cols, float frameDuration){
        this.texture = texture;
        this.rows = rows;
        this.cols = cols;
        this.frameDuration = frameDuration;
    }

    /** record this is a texture region (single frame) */
    public TextureInfo(TextureRegion textureRegion){
        this.textureRegion = textureRegion;
        this.rows = 1;
        this.cols = 1;
        this.frameDuration = 0;
    }

    public boolean isAnimated(){
        return  rows * cols > 1;
    }

    /**
     * if this texture is a filmstrip, returns width of one frame.
     * @return width of a single frame
     */
    public int getRegionWidth(){
        if (texture != null){
            return texture.getWidth() / cols;
        }
        else {
            return textureRegion.getRegionWidth();
        }
    }

    /**
     * if this texture is a filmstrip, returns height of one frame
     * @return height of a single frame
     */
    public int getRegionHeight(){
        if (texture != null){
            return texture.getHeight() / rows;
        }
        else {
            return textureRegion.getRegionHeight();
        }
    }

    public Texture getTexture(){
        assert texture != null;
        return texture;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public float getFrameDuration() {
        return frameDuration;
    }

    /**
     * @return a copy of the texture region stored in this texture data container
     */
    public TextureRegion getTextureRegion() {
        assert textureRegion != null;
        return new TextureRegion(textureRegion);
    }

}
