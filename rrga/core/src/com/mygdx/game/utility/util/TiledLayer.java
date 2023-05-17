package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.GameCanvas;

/**
 * A TiledLayer is a grid of tiles, represented by a tiled layer in Tiled editor.
 */
public class TiledLayer implements Drawable {

    private final int drawDepth;

    /** the number of tiles drawn in the previous draw call */
    private int tilesDrawn;

    private final Vector2 drawScale = new Vector2();
    private final Vector2 temp = new Vector2();

    /** width of this grid (also width of world) */
    private final int width;

    /** height of this grid (also height of world) */
    private final int height;

    private final Tile[] tiles;

    public TiledLayer(Tile[] tiles, int drawDepth, int width, int height){
        this.tiles = tiles;
        this.drawDepth = drawDepth;
        this.width = width;
        this.height = height;
    }

    @Override
    public Vector2 getDimensions() {
        return temp.set(width, height);
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(0,height);
    }

    @Override
    public int getDepth() {
        return this.drawDepth;
    }

    @Override
    public void setDrawScale(Vector2 scale) {
        drawScale.set(scale);
    }

    /**
     * NOTE: this is HIGHLY UNOPTIMIZED. Consider using draw(canvas, cx, cy, rx, ry).
     * @param canvas the game canvas
     */
    @Override
    public void draw(GameCanvas canvas) {
        draw(canvas, width/2f, height/2f, width/2f, height/2f);
    }

    /**
     * draws a rectangular subregion of the layer. This subregion has dimensions (2 rx, 2 ry) rounded to the next
     * largest integers.
     * @param canvas the game canvas
     * @param cx camera x in game coordinates
     * @param cy camera y in game coordinates
     * @param rx x-radius of visible width
     * @param ry y-radius of visible height
     */
    public void draw(GameCanvas canvas, float cx, float cy, float rx, float ry){
        tilesDrawn = 0;
        int centerTileX = (int) (cx);
        int centerTileY = (int) (cy);
        int minX = (int) Math.max(0, Math.floor(centerTileX - rx));
        int maxX = (int) Math.min(width- 1, Math.ceil(centerTileX + rx));
        int minY = (int) Math.max(0, Math.floor(centerTileY - ry));
        int maxY = (int) Math.min(height - 1, Math.ceil(centerTileY + ry));
        // draw a grid of tiles around the camera's tile
        // O(dw * dh)
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                int idx = j * width + i;
                Tile t = tiles[idx];
                if (t != null) {
                    canvas.draw(t, Color.WHITE, (int)(t.getRegionWidth()/2f), (int)(t.getRegionHeight()/2f),
                            (int)((i + 0.5f) * drawScale.x), (int) ((j + 0.5f) * drawScale.y), t.getRotationRad(),
                            drawScale.x/t.getRegionWidth(), drawScale.y/t.getRegionHeight()
                    );
                    tilesDrawn++;
                }
            }
        }
    }

    /** returns the number of tiles drawn in the last call to draw() */
    public int lastDrawn(){
        return tilesDrawn;
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        // nothing happens, too inefficient to draw box for every tile.
    }
}
