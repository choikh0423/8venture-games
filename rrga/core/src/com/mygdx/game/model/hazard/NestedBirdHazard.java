package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.Obstacle;

/**
 * A nested bird is one that will respawn at their spawner (nest) after it leaves the game screen.
 */
public class NestedBirdHazard extends BirdHazard{

    /**
     * the nest that this bird belongs to
     */
    private final NestHazard spawner;

    /** the number of remaining frames in spawning mode/blinking */
    private int blinkingCountDown;

    /** the total number of frames in spawning mode/blinking */
    private final int blinkingDuration;

    /** the number of frames to draw per blink */
    private static final int DRAW_DURATION_BLINKING = 10;

    public NestedBirdHazard(NestHazard spawner, int damage, int birdSensorRadius, float birdKnockBack){
        super(spawner.getBirdInitializerData(), damage, birdSensorRadius, birdKnockBack);
        this.spawner = spawner;
        blinkingDuration = spawner.getSpawnDelay();
        blinkingCountDown = 0;
        setFaceRight(spawner.getPath().length > 2 && spawner.getPath()[2] - getX() > 0);
    }

    private void setBlinking(){
        blinkingCountDown = blinkingDuration;
    }

    public void setSpawning(){
        setBlinking();
        setMoveSpeed(0);
        setPath(spawner.getPath());
        super.reset();
    }

    private void setPath(float[] path) {
        super.setPath(path, -1);
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (blinkingCountDown > 0) {
            float effect = isFaceRight() ? -1f : 1f;
            float scl = 1 - 1.0f*blinkingCountDown / blinkingDuration;
            TextureRegion birdRegion = getStillFrame();
            Vector2 dimensions = getDimensions();
            // not angry + not moving => still
            canvas.draw(birdRegion, Color.WHITE, birdRegion.getRegionWidth() / 2f, birdRegion.getRegionHeight() / 2f,
                    (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                    scl * effect * dimensions.x / birdRegion.getRegionWidth() * drawScale.x,
                    scl * dimensions.y / birdRegion.getRegionHeight() * drawScale.y);
            return;
        }
        // default bird animation
        super.draw(canvas);
    }

    @Override
    public void update(float delta) {
        if (blinkingCountDown > 0){
            blinkingCountDown -= 1;
        }
        else if (blinkingCountDown == 0){
            setMoveSpeed(spawner.getBirdSpeed());
            blinkingCountDown -= 1;
        }
        super.update(delta);
    }
}
