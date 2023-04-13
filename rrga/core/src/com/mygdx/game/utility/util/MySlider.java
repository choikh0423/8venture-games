package com.mygdx.game.utility.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;

public class MySlider {
    /** Texture for slider bar */
    public TextureRegion barTexture;
    /** Texture for slider knob */
    public TextureRegion knobTexture;
    /** Number of notches on this slider */
    public final int numNotches;
    /** Current notch of this slider. 0 based slider */
    public int curNotch;
    /** Ratio of current notch to total number of notches */
    public float ratio;
    /** x position of the center of the slider bar */
    public float barX;
    /** y position of the center of the slider bar */
    public float barY;
    /** x position of center of knob */
    public float knobX;
    /** y position of center of knob */
    public float knobY;
    /** x scale */
    public float sx;
    /** y scale */
    public float sy;
    /** Whether the knob should follow the x position of the cursor */
    public boolean knobFollow;


    public MySlider(TextureRegion barTex, TextureRegion knobTex, int numNotches, int x, int y, float sx, float sy){
        barTexture = barTex;
        knobTexture = knobTex;
        barX = x;
        barY = y;
        this.numNotches = numNotches;
        curNotch = numNotches / 2;
        ratio = (float) curNotch / numNotches;
        knobX = barX-barTexture.getRegionWidth()/2f + ratio * barTexture.getRegionWidth();
        knobY = y;
        this.sx = sx;
        this.sy = sy;
        knobFollow = false;
    }

    public void updateKnob(float x, float y){
        if(x <= barX-barTexture.getRegionWidth()/2f){
            knobX = barX-barTexture.getRegionWidth()/2f;
            curNotch = 0;
        }
        else if(x >= barX+barTexture.getRegionWidth()/2f){
            knobX = barX+barTexture.getRegionWidth()/2f;
            curNotch = numNotches;
        }
        else{
            //commented out is for continous progress bar
            //float prog = (x - barX + barTexture.getRegionWidth()/2f) / barTexture.getRegionWidth();
            int prog = (int) ((x - barX + barTexture.getRegionWidth()/2f) / barTexture.getRegionWidth() * numNotches);
            //knobX = (barX - barTexture.getRegionWidth()/2f)  + (prog * barTexture.getRegionWidth());
            knobX = (barX - barTexture.getRegionWidth()/2f)  + (prog * barTexture.getRegionWidth()/numNotches);
            //curNotch = (prog * numNotches);
            curNotch = prog;
        }
        ratio = (float) curNotch / numNotches;
    }

    public void draw(GameCanvas canvas){
        canvas.draw(barTexture, Color.WHITE, barTexture.getRegionWidth() / 2f, barTexture.getRegionHeight() / 2f,
                barX, barY, 0, sx, sy);
        Color tint = (knobFollow ? Color.GRAY : Color.WHITE);
        canvas.draw(knobTexture, tint, knobTexture.getRegionWidth() / 2f, knobTexture.getRegionHeight() / 2f,
               knobX , knobY, 0, sx, sy);
    }
}
