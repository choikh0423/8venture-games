package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
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

    private final Vector2 temp = new Vector2();

    public LightningHazard(JsonValue data, int dmg, float knockBack) {
        super(data, dmg, knockBack);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        waitDuration = data.getInt("strike_timer");
        waitTimer = waitDuration + data.getInt("initial_timer_offset");
        strikeDuration = data.getInt("strike_duration", DEFAULT_STRIKE_DURATION);
        strikeTimer = -1;
        depth = data.getInt("depth");
    }

    @Override
    public boolean activatePhysics(World world) {
        boolean result = super.activatePhysics(world);
        setActive(false);
        return result;
    }

    public void draw(GameCanvas canvas) {
        if(isActive()) {
            super.draw(canvas);
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

    // DRAWABLE INTERFACE
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

    // HAZARD INTERFACE
    @Override
    public Vector2 getKnockBackForce() {
        return temp.set(0,-1);
    }

    @Override
    public void setKnockBackForce(Vector2 in) {
        // no need to update
    }
}
