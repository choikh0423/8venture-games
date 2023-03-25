package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.obstacle.PolygonObstacle;

/**
 * A model for hazard objects.
 */
public abstract class HazardModel extends PolygonObstacle {
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;

    /**
     * The damage done to the player when colliding with this hazard
     */
    private int damage;

    /** The scale of this hazard's knockback */
    private float knockback;

    /**
     * Returns this hazard's damage
     * @return This hazard's damage
     */
    public int getDamage(){
        return damage;
    }

    /**
     * Returns this hazard's knockback scale
     * @return This hazard's knockback scale
     */
    public float getKnockbackScl(){
        return knockback;
    }

    public HazardModel(JsonValue data, int dam, float kb) {
        super(data.get("points").asFloatArray(), data.getFloat("x"), data.getFloat("y"));
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        this.data = data;
        this.damage = dam;
        this.knockback = kb;
    }

    /**
     * Gives the normalized vector of the knockback force in the x and y direction
     * getKnockbackForce()[1] = x knockback force
     * getKnockbackForce()[1] = y knockback force
     * */
    public abstract Vector2 getKnockbackForce();
}

