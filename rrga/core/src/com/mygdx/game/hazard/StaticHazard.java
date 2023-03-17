package com.mygdx.game.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class StaticHazard extends HazardModel{

    /** Damage of a static hazard */
    private static final int STATIC_DAMAGE = 1;

    /** Knockback of a static hazard */
    private static final float STATIC_KNOCKBACK = 3;
    public StaticHazard(JsonValue data) {
        super(data, STATIC_DAMAGE, STATIC_KNOCKBACK);
    }

    @Override
    public Vector2 getKnockbackForce() {
        return new Vector2(0,1);
    }
}
