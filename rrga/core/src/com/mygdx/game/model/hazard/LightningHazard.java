package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.util.Drawable;

/**
 * 1-frame lightning bolt
 */
public class LightningHazard extends PolygonHazard implements Drawable {

    private static final int DEFAULT_STRIKE_DURATION = 100;

    /** How long a lightning strike lasts */
    private final int strikeDuration;

    /** The number of ticks between this lightning's strikes */
    private final int waitDuration;

    /** How much longer the lightning will wait for until striking */
    private int waitTimer;

    /** How much longer the lightning will strike for.
     * -1 if not currently striking */
    private int strikeTimer;

    /** draw depth */
    private final int depth;

    public LightningHazard(JsonValue data, int dmg, float knockBack) {
        super(data, dmg, knockBack);
        waitDuration = data.getInt("strike_timer");
        waitTimer = waitDuration + data.getInt("initial_timer_offset");
        strikeDuration = data.getInt("strike_duration", DEFAULT_STRIKE_DURATION);
        strikeTimer = -1;
        depth = data.getInt("depth");
        setActive(false);
    }

    public void draw(GameCanvas canvas) {
        if(isActive()) {
            canvas.draw(region, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 1, 1);
        }
    }

    @Override
    public void update(float delta) {
        if (waitTimer < 0){
            // no more waiting, strike cycle should be ongoing
            if (strikeTimer <= 0){
                // transition to wait mode
                setActive(false);
                waitTimer = waitDuration;
            }
            else {
                strikeTimer--;
            }
        }
        else if (waitTimer == 0){
            // finished waiting, transition to strike cycle
            setActive(true);
            strikeTimer = strikeDuration;
            waitTimer--;
        }
        else {
            waitTimer--;
        }
    }

    @Override
    public Vector2 getDimensions() {
        return super.getDimension();
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public Vector2 getBoxCorner() {
        return super.getBoxCoordinate();
    }
}
