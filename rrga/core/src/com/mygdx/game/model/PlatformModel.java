package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.Drawable;

/**
 * A platform is any still polygonal block that is safe to stand on.
 */
public class PlatformModel extends PolygonObstacle implements Drawable {

    /** the (x,y) offset from origin of AABB top left corner*/
    private final Vector2  boxCoordinate = new Vector2();

    /** bounding box dimensions */
    private final Vector2 dimensions = new Vector2();

    /** size of texture in game units */
    private final Vector2 size = new Vector2();

    private final Vector2 temp = new Vector2();

    private float angle = 0;

    private boolean flipX = false;

    private boolean flipY = false;

    private int depth;

    /**
     * constructs an invisible platform
     * @param points shape
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public PlatformModel(float x, float y, float[] points, int depth) {
        super(points, x, y);
        float minx = points[0];
        float maxy = points[1];
        for(int ii = 2; ii < points.length; ii += 2) {
            if (points[ii] < minx) {
                minx = points[ii];
            }
            if (points[ii+1] > maxy) {
                maxy = points[ii+1];
            }
        }
        boxCoordinate.set(minx, maxy);
        dimensions.set(super.getDimension());
        size.set(dimensions);
        this.depth = depth;
    }

    /**
     * constructs a platform with the given texture
     * @param texturedPlatformData JSON data for platforms with texture (ex. tree logs)
     * @param texture object texture
     */
    public PlatformModel(JsonValue texturedPlatformData, TextureRegion texture, int depth){
        super(texturedPlatformData.get("points").asFloatArray(), texturedPlatformData.getFloat("x", 0),
                texturedPlatformData.getFloat("y", 0));
        this.texture = texture;
        this.depth = depth;
        flipX = texturedPlatformData.getBoolean("flipX");
        flipY = texturedPlatformData.getBoolean("flipY");
        angle = texturedPlatformData.getFloat("angle", 0);
        float[] aabb = texturedPlatformData.get("AABB").asFloatArray();
        size.set(aabb[2], aabb[3]);
        temp.set(size);
        if (this.angle != 0){
            // need to compute new AABB (preferable rotational angles are pi/2, pi, 3pi/2)
            double psin = Math.abs(Math.sin(angle));
            double pcos = Math.abs(Math.cos(angle));
            // set new box dimensions
            aabb[2] = (float) (temp.y * psin + temp.x * pcos);
            aabb[3] = (float) (temp.x * psin + temp.y * pcos);
            // new box left corner (cartesian coordinate, relative to center of sticker)
            aabb[0] = -aabb[2]/2f;
            aabb[1] = aabb[3]/2f;
        }
        dimensions.set(aabb[2], aabb[3]);
        boxCoordinate.set(aabb[0],aabb[1]);
        setAngle(angle);
    }

    // DRAWABLE INTERFACE
    @Override
    public Vector2 getBoxCorner() {
        return temp.set(getPosition()).add(boxCoordinate);
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public Vector2 getDimensions() {
        return temp.set(dimensions);
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null){
            int effectX = flipX ? -1 : 1;
            int effectY = flipY ? -1 : 1;
            canvas.draw(texture, Color.WHITE, texture.getRegionWidth() / 2f, texture.getRegionHeight() / 2f,
                    getX() * drawScale.x, getY() * drawScale.y, angle,
                    effectX * size.x / texture.getRegionWidth() * drawScale.x,
                    effectY * size.y / texture.getRegionHeight() * drawScale.y);
        }
    }
}
