package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private final Texture birdFlapAnimation;
    private TextureRegion birdTex;
    private PooledList<BirdHazard> birdList;
    private int spawnInCountdown;
    private boolean drawSpawnIn;
    private JsonValue blueData;
    private float[] blueAABB;

    public NestHazard(float[] points, float x, float y, float[] path, float spd, int delay, int dam, float kb,
                      Vector2 scl, Texture birdAnimation, JsonValue blueData){
        super(points, x, y);
        setGravityScale(0);
        setDensity(0);
        setFriction(0);
        setRestitution(0);
        setSensor(true);

        this.path = path;
        birdSpeed = spd;
        spawnDelay = delay;
        countdown = spawnDelay;
        System.out.println("countdown initial: " + countdown );

        birdDamage = dam;
        birdKnockback = kb;
        scale = scl;
        this.birdFlapAnimation = birdAnimation;
        // TODO: better memory allocation with PooledList
        birdList = new PooledList<>();
        this.blueData = blueData;
        blueAABB = blueData.get("AABB").asFloatArray();

        spawnInCountdown = 0;
        drawSpawnIn = false;
        TextureRegion[][] flapTmpFrames = TextureRegion.split(birdAnimation, blueData.getInt("filmStripWidth"),
                blueData.getInt("filmStripHeight"));
        birdTex = flapTmpFrames[0][0];
    }

    public BirdHazard update(){
        if(countdown == 0){
            countdown = spawnDelay;
            BirdHazard obj;
            JsonValue data = blueData;

            data.remove("x");
            data.remove("y");
            data.addChild("x", new JsonValue(getX()));
            data.addChild("y", new JsonValue(getY()));

            data.remove("facing_right");
            boolean right = path[2] - getX() > 0;
            data.addChild("facing_right", new JsonValue(right));

            data.remove("movespeed");
            data.addChild("movespeed", new JsonValue(birdSpeed));

            obj = new BirdHazard(data, birdDamage, 0, birdKnockback, null);
            obj.setDrawScale(scale);
            obj.setFlapAnimation(birdFlapAnimation);
            obj.setPath(path, -1);
            obj.setName("blue_bird");
            return obj;
        }
        else{
            countdown--;
            return null;
        }
    }

    public void draw(GameCanvas canvas){
        //TODO size nests automatically
        canvas.draw(texture, Color.WHITE, texture.getRegionWidth()/2f, texture.getRegionHeight()/2f,
                getX() * drawScale.x, getY() * drawScale.y, getAngle(), .1f, .1f);

        float effect = path[2] - getX() > 0 ? -1.0f : 1.0f;
        int duration = 20;
        int num_flashes = 3;
        if(countdown < duration * num_flashes * 2 + 1 && countdown>0){
            if (spawnInCountdown == 0){
                spawnInCountdown = duration;
                drawSpawnIn = !drawSpawnIn;
            }
            if(drawSpawnIn){
                canvas.draw(birdTex, Color.WHITE, birdTex.getRegionWidth()/2f, birdTex.getRegionHeight()/2f,
                        getX() * scale.x, getY() * scale.y, getAngle(),
                        effect * blueAABB[4] * drawScale.x, blueAABB[5] * drawScale.y);
            }
            spawnInCountdown--;
        }
    }
}
