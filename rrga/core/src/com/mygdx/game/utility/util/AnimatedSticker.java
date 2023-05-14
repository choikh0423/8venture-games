package com.mygdx.game.utility.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

public class AnimatedSticker extends Sticker{

    private final Animation<TextureRegion> animation;

    private float elapsedTime;

    public AnimatedSticker(float x, float y, float angle, int depth, JsonValue AABB, TextureInfo textureInfo,
                           boolean flipX, boolean flipY)
    {
        super(x, y, angle, depth, AABB, null);
        TextureRegion[][] frameMat = TextureRegion.split(textureInfo.getTexture(),
                textureInfo.getRegionWidth(), textureInfo.getRegionHeight());
        TextureRegion[] frames = new TextureRegion[textureInfo.getRows() * textureInfo.getCols()];
        // Placing animation frames in order
        int index = 0;
        for (TextureRegion[] textureRegions : frameMat) {
            for (int j = 0; j < frameMat[0].length; j++) {
                frames[index] = textureRegions[j];
                frames[index].flip(flipX, flipY); //apply flipping on frames
                index++;
            }
        }
        this.animation = new Animation<>(textureInfo.getFrameDuration(), frames);
        elapsedTime = 0;
    }

    @Override
    public void draw(GameCanvas canvas) {
        elapsedTime += Gdx.graphics.getDeltaTime();
        this.texture = animation.getKeyFrame(elapsedTime, true);
        super.draw(canvas);
    }
}
