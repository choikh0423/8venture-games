package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

public class LightningHazard extends HazardModel{

    /** Damage of a lightning hazard */
    private static final int LIGHTNING_DAMAGE = 1;

    /** Knockback of a lightning hazard */
    private static final float LIGHTNING_KNOCKBACK = 0;

    /** How long a lightning strike lasts */
    private static final int STRIKE_DURATION = 100;

    /** The number of ticks between this lightning's strikes */
    private final int waitDuration;

    /** How much longer the lightning will wait for until striking */
    private int waitTimer;

    /** How much longer the lightning will strike for.
     * -1 if not currently striking */
    private int strikeTimer;

    public LightningHazard(JsonValue data) {
        super(data, LIGHTNING_DAMAGE, LIGHTNING_KNOCKBACK);
        waitDuration = data.getInt("strike_timer");
        waitTimer = waitDuration + data.getInt("strike_timer_offset");
        strikeTimer = -1;
        fixture.isSensor = true;
        setActive(false);
    }

    public boolean isStriking(){
        return strikeTimer != -1;
    }

    public void strike(){
        //if not striking, waiting
        if(!isStriking()){
            setActive(false);
            //if not at end of wait, wait longer
            if(waitTimer!=-1){
                waitTimer--;
            }
            //else at end of wait, start strike
            else{
                strikeTimer = STRIKE_DURATION;
                waitTimer = waitDuration;
            }
        }
        //else striking
        else{
            setActive(true);
            strikeTimer--;
        }

    }

    @Override
    public Vector2 getKnockbackForce() {
        return new Vector2(0,1);
    }

    public void draw(GameCanvas canvas) {
        if(isActive()) {
            canvas.draw(region, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 1, 1);
        }
    }
}
