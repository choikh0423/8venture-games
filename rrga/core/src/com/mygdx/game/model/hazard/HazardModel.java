package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.obstacle.PolygonObstacle;

/**
 * A model for hazard objects.
 */
public interface HazardModel {

    /**
     * @return This hazard's damage
     */
    int getDamage();

    /**
     * @return This hazard's knock back scale
     */
    float getKnockBackScl();

    /**
     * Gives the normalized vector of the knock-back force in the x and y direction
     */
    Vector2 getKnockBackForce();

}
