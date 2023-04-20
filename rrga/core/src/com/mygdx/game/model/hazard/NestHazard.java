package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import org.w3c.dom.Text;
import sun.util.resources.cldr.ext.TimeZoneNames_en_ZA;

public class NestHazard {
    private int x;
    private int y;
    private Vector2 birdDir;
    private float birdSpeed;
    private final int spawnDelay;
    private int countdown;
    private final int birdDamage;
    private final float birdKnockback;
    private final Vector2 scale;
    private final Texture birdTex;

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public NestHazard(int x, int y, Vector2 dir, float spd, int delay, int dam, float kb, Vector2 scl, Texture tex){
        this.x = x;
        this.y = y;
        birdDir = dir.nor();
        birdSpeed = spd;
        spawnDelay = delay;
        countdown = spawnDelay;

        birdDamage = dam;
        birdKnockback = kb;
        scale = scl;
        birdTex = tex;
    }

    public BirdHazard update(){
        if(countdown == 0){
            countdown = spawnDelay;
            BirdHazard obj;
            JsonValue data = new JsonValue(JsonValue.ValueType.object);
            data.addChild("color", new JsonValue("blue"));
            data.addChild("attack", new JsonValue(false));
            data.addChild("facing_right", new JsonValue(false));
            data.addChild("x", new JsonValue(getX()));
            data.addChild("y", new JsonValue(getY()));
            //filmstripwdith
            //filmstripheight
            //aabb
            //points
            JsonValue path = new JsonValue(JsonValue.ValueType.array);
            float dirX = birdDir.x*Integer.MAX_VALUE;
            float dirY = birdDir.y*Integer.MAX_VALUE;
            path.addChild(new JsonValue(dirX));
            path.addChild(new JsonValue(dirY));
            data.addChild("points", path);
            data.addChild("loop", new JsonValue(false));
            data.addChild("movespeed", new JsonValue(birdSpeed));
            data.addChild("atkspeed", new JsonValue(0));
            obj = new BirdHazard(data, birdDamage, 0, birdKnockback, null);
            obj.setDrawScale(scale);
            obj.setFlapAnimation(birdTex);

            return null;
        }
        else{
            countdown--;
            return null;
        }
    }
}
