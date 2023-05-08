package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

public class RockHazard extends StaticHazard{

    /** the physics dimensions of object's AABB */
    private final Vector2 dimensions = new Vector2();

    /** the top left corner coordinate of object AABB (coordinate is relative to entity) */
    private final Vector2 boxCoordinate = new Vector2();

    /** the dimensions of the un-rotated AABB (this is equal to dimensions if no rotation) */
    private final Vector2 size = new Vector2(); //this is also texture dimensions

    private final Vector2 temp = new Vector2();

    private final float angle;

    private final boolean flipX;

    private final boolean flipY;

    public RockHazard(JsonValue data, int dmg, float knockBack){
        super(data, dmg, knockBack);
        this.angle = data.getFloat("angle");
        float[] aabb = data.get("AABB").asFloatArray();
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
        flipX = data.getBoolean("flipX");
        flipY = data.getBoolean("flipY");
    }

    @Override
    public Vector2 getDimensions() {
        return temp.set(this.dimensions);
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(getPosition()).add(boxCoordinate);
    }

    @Override
    public void draw(GameCanvas canvas) {
        int effectX = flipX ? -1 : 1;
        int effectY = flipY ? -1 : 1;
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth() / 2f, texture.getRegionHeight() / 2f,
                getX() * drawScale.x, getY() * drawScale.y, angle,
                effectX * size.x / texture.getRegionWidth() * drawScale.x,
                effectY * size.y / texture.getRegionHeight() * drawScale.y);
    }
}
