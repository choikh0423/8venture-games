package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.obstacle.SimpleObstacle;
import com.mygdx.game.utility.util.PooledList;

public class NestHazard extends PolygonObstacle {
    private float[] path;
    private float birdSpeed;
    private final int spawnDelay;
    private int countdown;
    private final int birdDamage;
    private final float birdKnockback;
    private final Vector2 scale;
    private final Texture birdTex;
    private PooledList<BirdHazard> birdList;

    public NestHazard(float[] points, int x, int y, float[] path, float spd, int delay, int dam, float kb, Vector2 scl, Texture birdTex){
        super(points, x, y);

        this.path = path;
        birdSpeed = spd;
        spawnDelay = delay;
        countdown = spawnDelay;

        birdDamage = dam;
        birdKnockback = kb;
        scale = scl;
        this.birdTex = birdTex;
        // TODO: better memory allocation with PooledList
        birdList = new PooledList<>();
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
            JsonValue p = new JsonValue(JsonValue.ValueType.array);
            for(int i=0; i<path.length; i++) {
                p.addChild(new JsonValue(path[i]));
            }
            data.addChild("path", p);
            data.addChild("loop", new JsonValue(false));
            data.addChild("movespeed", new JsonValue(birdSpeed));
            data.addChild("atkspeed", new JsonValue(0));
            obj = new BirdHazard(data, birdDamage, 0, birdKnockback, null);
            obj.setDrawScale(scale);
            obj.setFlapAnimation(birdTex);
            return obj;
        }
        else{
            countdown--;
            return null;
        }
    }

    public void draw(GameCanvas canvas){
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 1, 1);
    }
}
