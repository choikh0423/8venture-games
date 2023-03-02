package com.mygdx.game.audio;

/**
 * This class represents external sound effects in OpenAL,
 * they can be applied to a sound or music instance to generate additional effects.
 *
 * Effects need to be disposed with {@link #dispose()} when done using.
 *
 **/

public interface EffectFilter {
    /**
     * Dispose the sound effect and release its resources
     * */
    void dispose();
}
