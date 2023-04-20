package com.mygdx.game.mode;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.mode.MenuMode.ButtonShape;

import java.awt.*;

public class MenuButton {
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** Button Texture */
    private TextureRegion texture;

    /** Height of the button */
    private static float BUTTON_SCALE  = 1.2f;
    /** The width of the button */
    private int buttonWidth;

    /** Ration of the button height to the screen */
    private static float X_RATIO = 0.63f;
    /** Ration of the button height to the screen */
    private static float Y_RATIO = 0.2f;
    /** Ration of the button angle */
    private static float ANGLE = -0.05f * 3.14f;

    /** Enumerator for button shapes*/


    /** The type of button */
    private ButtonShape type;
    /** The y-coordinate of the center of the button */
    private int buttonY;
    /** The x-coordinate of the center of the button */
    private int buttonX;
    /** Ration of the button height to the screen */
    private float ratioY;
    /** Ration of the button width to the screen */
    private float ratioX;
    /** The rotation angle of the button */
    private float angle;
    /** The button XY Scale */
    private float scale;


    /** Returns the x-coordinate of the center of the button */
    public int getX() { return buttonX; }

    /** Returns the y-coordinate of the center of the button */
    public int getY() { return buttonY; }
    /** Returns the angle of the button */
    public float getAngle() { return angle; }

    /** Returns ration of x position of button on screen */
    public float getRatioX() { return ratioX; }

    /** Returns the y-coordinate of the center of the button */
    public float getRatioY() { return ratioY; }

    /** Returns the regional width of button texture */
    public int getRegionWidth() { return texture.getRegionWidth(); }
    /** Returns the regional height of button texture */
    public int getRegionHeight() { return texture.getRegionHeight(); }

    /** Set XY Coordinate of the center of the button */
    public void setXY(int width, int height) {
        buttonY = (int)(ratioY * height);
        buttonX = (int)(ratioX * width);
    }

    /** Set XY scale of the button */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /** Set texture of the button */
    public void setTexture(TextureRegion texture) { this.texture = texture; }



    /**
     * Creates a new menu mode button
     */
    public MenuButton(ButtonShape type, float x_ratio, float y_ratio, float angle) {
        this.type = type;
        this.ratioX = x_ratio;
        this.ratioY = y_ratio;
        this.angle = angle;

        this.texture = texture;

    }

    public void draw(GameCanvas canvas, int pressState) {
        // Draw Exit Button
        com.badlogic.gdx.graphics.Color exitTint = (pressState == 1 ? com.badlogic.gdx.graphics.Color.GRAY : Color.WHITE);
        canvas.draw(texture, exitTint, texture.getRegionWidth() / 2, texture.getRegionHeight() / 2,
                buttonX, buttonY, angle, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
    }

}
