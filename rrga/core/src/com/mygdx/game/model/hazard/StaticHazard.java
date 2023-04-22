package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class StaticHazard extends PolygonHazard{

    /** Damage of a static hazard */
    private static final int STATIC_DAMAGE = 1;

    /** Knock-back of a static hazard */
    private static final float STATIC_KNOCKBACK = 3;

    public StaticHazard(JsonValue data) {
        super(data, STATIC_DAMAGE, STATIC_KNOCKBACK);
    }

}
