package com.mygdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

/**
 * A MenuScreen is any non-game screen where the user can click on
 * menu buttons or use keyboard/controller input to transition to another screen.
 */
public abstract class MenuScreen implements Screen, InputProcessor, ControllerListener {

    /**
     * a clickable button is always in one of 3 states,
     * either waiting to be clicked, being pressed/held or released.
     */
    public enum ClickableButtonState {
        HELD,
        RELEASED,
        WAITING
    }

    // Some interface methods are omitted but all subclasses must implement them
    // - dispose
    // - render
    // - resize

    // A collection of methods follows to shorten the need to include all such methods in subclasses
    // If a subclass does not override the following methods, the functionality is unsupported (by default).

    // SCREEN INTERFACE
    public void pause(){}

    public void resume(){}

    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    // INPUT PROCESSOR INTERFACE
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean keyDown(int keycode) {return false;}

    public boolean keyTyped(char character) {
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean keyUp(int keycode) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    // CONTROLLER LISTENER INTERFACE
    public void connected(Controller controller) {}

    public void disconnected(Controller controller) {}

    public boolean buttonDown(Controller controller, int buttonCode) { return false;}

    public boolean buttonUp(Controller controller, int buttonCode) {return false;}

    public boolean axisMoved(Controller controller, int axisCode, float value) {return false;}
}
