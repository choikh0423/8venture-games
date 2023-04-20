package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.BoxObstacle;

public class GoalDoor extends BoxObstacle {

    private Animation<TextureRegion> animation;
    private float elapsedTime;
    public GoalDoor(JsonValue data, float x, float y, float w, float h){
        super(x,y,w,h);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));
        setRestitution(data.getFloat("restitution", 0));
        setSensor(true);
        setName("goal");
    }

    public void setAnimation(Texture texture){
        TextureRegion[][] frames = TextureRegion.split(texture, 228, 228);
        TextureRegion[] animation = new TextureRegion[12];

        int index = 0;
        for (int i=0; i<frames.length; i++) {
            for (int j=0; j<frames[0].length; j++) {
                animation[index] = frames[i][j];
                index++;
            }
        }

         this.animation = new Animation<>(1f/12f, animation);
    }
    public void draw(GameCanvas canvas){
        elapsedTime += Gdx.graphics.getDeltaTime();
        TextureRegion t = animation.getKeyFrame(elapsedTime, true);
        canvas.draw(t, Color.WHITE, t.getRegionWidth()/2f, t.getRegionHeight()/2f,
                getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                getWidth()*drawScale.x/ t.getRegionWidth(), getHeight()*drawScale.y/t.getRegionHeight());
    }
}
