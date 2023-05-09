package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A camera controller provides a dual-camera abstraction, where the camera can switch
 * between following a target (ie: dynamic mode) or centering on screen (ie: HUD mode).
 */
public class CameraController {

    private final OrthographicCamera camera;

    private final FitViewport viewport;

    private final Vector3 pos = new Vector3();

    private final Vector2 temp = new Vector2();

    private static final float ALPHA = 0.05f;

    /**
     * Constructs a camera whose viewport is of the given dimensions.
     * @param viewportWidth width
     * @param viewportHeight height
     */
    public CameraController(int viewportWidth, int viewportHeight){
        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.setToOrtho(false);
        viewport = new FitViewport(viewportWidth, viewportHeight, camera);
        viewport.apply(true);
    }

    /**
     * updates the camera to adapt to the new screen dimension
     * @param screenWidth canvas/screen width
     * @param screenHeight canvas/screen height
     */
    public void update(int screenWidth, int screenHeight){
        viewport.update(screenWidth, screenHeight);
        viewport.apply();
    }

    /**
     * @return the camera's associated viewport
     */
    public Viewport getViewport(){ return this.viewport;}

    public Matrix4 combined(){ return this.camera.combined; }

    public float getViewWidth(){ return viewport.getWorldWidth();}

    public float getViewHeight(){ return viewport.getWorldHeight();}

    /**
     * sets the camera's zoom factor (allows zooming in or out)
     * @param zoom the zoom factor
     */
    public void setZoom(float zoom){
        camera.zoom = zoom;
        camera.update();
    }

//    public void followTargetPoint(float tx, float ty){
//        camera.position.lerp(pos.set(tx, ty, 0), ALPHA);
//        camera.update();
//    }

    /**
     * sets camera position to given point (tx, ty)
     * @param tx x-coordinate
     * @param ty y-coordinate
     */
    public void setPosition(float tx, float ty){
        camera.translate(tx - camera.position.x, ty - camera.position.y);
        camera.update();
    }

    /**
     * sets camera position to be center of viewport
     */
    public void setViewCenter(){
        camera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight());
        camera.update();
    }

    /**
     * converts a screen coordinate (x,y) to (x', y') coordinate within the camera's viewport.<br>
     * The coordinate system in which (x,y) belongs to is where (0,0) is top left corner of window screen.
     * @param screenX application screen x coordinate
     * @param screenY applcation screen y coordinate
     * @return (non-allocator) vector cache containing (x',y')
     */
    public Vector2 unproject(float screenX, float screenY){
        temp.set(screenX, screenY);
        return viewport.unproject(temp);
    }

    /**
     * converts a viewport coordinate (x',y') to (x, y) screen coordinate.<br>
     * @param viewX viewport world coordinate x
     * @param viewY viewport world coordinate y
     * @return (non-allocator) vector cache containing (x,y)
     */
    public Vector2 project(float viewX, float viewY){
        temp.set(viewX, viewY);
        return viewport.project(temp);
    }

}
