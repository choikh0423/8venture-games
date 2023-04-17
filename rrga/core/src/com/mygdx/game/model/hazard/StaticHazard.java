package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class StaticHazard extends HazardModel{

    /** Damage of a static hazard */
    private static final int STATIC_DAMAGE = 1;

    /** Knockback of a static hazard */
    private static final float STATIC_KNOCKBACK = 3;

    private final Vector2 knockBackForce = new Vector2(0,1);
    public StaticHazard(JsonValue data) {
        super(data.getFloat("x"), data.getFloat("y"),
                data.get("points").asFloatArray(), STATIC_DAMAGE, STATIC_KNOCKBACK);
    }

    @Override
    public Vector2 getKnockbackForce() {
        return knockBackForce;
    }
}
