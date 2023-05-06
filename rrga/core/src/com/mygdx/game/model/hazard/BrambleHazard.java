package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.util.Drawable;

/**
 * A BrambleHazard is an area covered in brambles that deals damage to the player on contact.<br>
 */
public class BrambleHazard extends PolygonHazard implements Drawable {

    private final int drawDepth;

    public BrambleHazard(JsonValue data, int dmg, float knockBack) {
        super(data, dmg, knockBack);
        drawDepth = data.getInt("depth");
    }

    // DRAWABLE INTERFACE

    @Override
    public Vector2 getDimensions() {
        return super.getDimension();
    }

    @Override
    public Vector2 getBoxCorner() {
        return super.getBoxCoordinate();
    }

    @Override
    public int getDepth() {
        return drawDepth;
    }

    public void draw(GameCanvas canvas) {
        // method unnecessary
        // polygon obstacle already does not draw without a texture to be used as a fill texture.
        super.draw(canvas);
    }
}
