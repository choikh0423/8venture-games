package com.mygdx.game.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class StaticHazard extends HazardModel{

    /** Damage of a static hazard */
    private static final int STATIC_DAMAGE = 1;

    private static final float STATIC_KNOCKBACK_SCL = 1;
    public StaticHazard(JsonValue data) {
        super(data, STATIC_DAMAGE, STATIC_KNOCKBACK_SCL);
    }

    @Override
    public Vector2 getKnockbackForce() {
        return new Vector2(0,0);
    }
}
