package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.utility.obstacle.PolygonObstacle;

/**
 * A polygon hazard is any hazard with the shape of a polygon.
 */
public class PolygonHazard extends PolygonObstacle implements HazardModel {

    /**
     * The damage done to the player when colliding with this hazard
     */
    private final int damage;

    /** The scale of this hazard's knock-back */
    private final float knockBackScl;

    /** The knock-back force vector */
    private Vector2 knockBackForce = new Vector2();

    @Override
    public int getDamage() { return damage; }

    @Override
    public float getKnockBackScl() { return knockBackScl; }

    @Override
    public Vector2 getKnockBackForce() { return knockBackForce; }

    @Override
    public void setKnockBackForce(Vector2 kbForce) { knockBackForce.set(kbForce.nor()); }

    /**
     * constructs a polygonal hazard with the given shape, damage and knock-back values.
     * The position of the hazard must be retrievable from data as "x" and "y" attributes.
     * The shape of the hazard is retrieved from data["points"]. This object keeps reference
     * to kbForce (if not null), otherwise a new force vector is instantiated.
     * @param data the object data
     * @param dam the damage that the hazard will do on each contact
     * @param kb the knock-back scale factor
     * @param kbForce the knock-back (normalized) force vector
     */
    public PolygonHazard(JsonValue data, int dam, float kb, Vector2 kbForce) {
        this(data.getFloat("x"), data.getFloat("y"), data.get("points").asFloatArray(), dam, kb, kbForce);
    }

    /**
     * constructs a polygonal hazard with the given shape, damage and knock-back values.
     * The position of the hazard must be retrievable from data as "x" and "y" attributes.
     * The shape of the hazard is retrieved from data["points"].
     * @param data the object data
     * @param dam the damage that the hazard will do on each contact
     * @param kb the knock-back scale factor
     */
    public PolygonHazard(JsonValue data, int dam, float kb) {
        this(data, dam, kb, null);
    }


    /**
     * constructs a polygonal hazard with the given shape, damage and knock-back values at
     * the given position (x,y). This object keeps reference to kbForce (if not null),
     * otherwise a new force vector is instantiated.
     * @param x the object x-position
     * @param y the object y-position
     * @param points the object shape
     * @param dam the damage that the hazard will do on each contact
     * @param kb the knock-back scale factor
     * @param kbForce the knock-back (normalized) force vector
     */
    public PolygonHazard(float x, float y, float[] points, int dam, float kb, Vector2 kbForce){
        super(points, x, y);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        //fixture.isSensor = true;
        this.damage = dam;
        this.knockBackScl = kb;
        // ideally, if the constructor was called without knock-back force vector,
        // the subclass should override the knock-back force method. Otherwise, default (no knock-back).
        if (kbForce == null){
            kbForce = new Vector2();
        }
        this.knockBackForce = kbForce;
    }
}
