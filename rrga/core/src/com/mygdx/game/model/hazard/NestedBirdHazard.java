package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.util.FilmStrip;

/**
 * A nested bird is one that will respawn at their spawner (nest) after it leaves the game screen.
 */
public class NestedBirdHazard extends BirdHazard{

    /**
     * the nest that this bird belongs to
     */
    private final NestHazard spawner;

    /** the number of remaining frames in spawning mode/blinking */
    private int spawningCountDown;

    /** the total number of frames in spawning mode/blinking */
    private final int spawningDuration;

    /** the spawn animation */
    private FilmStrip spawnFilmStrip;
    private int totalFrameCount;
    private int currentFrame;

    public NestedBirdHazard(NestHazard spawner, int damage, int birdSensorRadius, float birdKnockBack){
        super(spawner.getBirdInitializerData(), damage, birdSensorRadius, birdKnockBack);
        this.spawner = spawner;
        spawningDuration = spawner.getSpawnDelay();
        spawningCountDown = 0;
        setFaceRight(spawner.getPath().length > 2 && spawner.getPath()[2] - getX() > 0);
    }

    private void beginSpawnTimer(){
        spawningCountDown = spawningDuration;
        currentFrame = 0;
    }

    public void setSpawning(){
        beginSpawnTimer();
        setMoveSpeed(0);
        setPath(spawner.getPath());
        super.reset();
    }

    private void setPath(float[] path) {
        super.setPath(path, -1);
    }

    public void setSpawnAnimation(Texture texture, int rows, int columns){
        this.spawnFilmStrip = new FilmStrip(texture, rows, columns);
        totalFrameCount = rows * columns;
        currentFrame = 0;
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (spawningCountDown > 0) {
            if (spawningCountDown <= 3 * totalFrameCount){
                spawnFilmStrip.setFrame(totalFrameCount - (int) Math.ceil(spawningCountDown/3.0));
                TextureRegion birdRegion = spawnFilmStrip;
                Vector2 dimensions = getDimensions();
                int effect = isFaceRight() ? -1 : 1;
                canvas.draw(birdRegion, Color.WHITE, birdRegion.getRegionWidth() / 2f, birdRegion.getRegionHeight() / 2f,
                        (getX()) * drawScale.x, (getY()) * drawScale.y, getAngle(),
                        effect * dimensions.x / birdRegion.getRegionWidth() * drawScale.x,
                        dimensions.y / birdRegion.getRegionHeight() * drawScale.y);
            }
            return;
        }
        // default bird animation
        super.draw(canvas);
    }

    @Override
    public void update(float delta) {
        if (spawningCountDown > 0){
            spawningCountDown -= 1;
        }
        else if (spawningCountDown == 0){
            setMoveSpeed(spawner.getBirdSpeed());
            spawningCountDown -= 1;
        }
        super.update(delta);
    }
}
