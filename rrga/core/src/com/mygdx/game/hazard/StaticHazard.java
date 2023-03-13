package com.mygdx.game.hazard;

import com.badlogic.gdx.utils.JsonValue;

public class StaticHazard extends HazardModel{

    /** Damage of a static hazard */
    private static final int STATIC_DAMAGE = 1;
    public StaticHazard(JsonValue data) {
        super(data, STATIC_DAMAGE);
    }
}
