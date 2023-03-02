/*
 * EffectFactory.java
 *
 * This is the interface for the factory class for creating effect filters
 *
 * @author Barry Lyu
 * @date   6/25/22
 */

package com.mygdx.game.audio;

/**
 * This is a factory class for creating {@link EffectFilter} Objects
 *
 * The following openAL effects are supported,
 * for more details on effect parameters, see openAL
 *
 *  AL_EFFECT_REVERB
 *  AL_EFFECT_CHORUS
 *  AL_EFFECT_DISTORTION
 *  AL_EFFECT_ECHO
 *  AL_EFFECT_FLANGER
 *  AL_EFFECT_FREQUENCY_SHIFTER
 *  AL_EFFECT_VOCAL_MORPHER
 *  AL_EFFECT_PITCH_SHIFTER
 *  AL_EFFECT_RING_MODULATOR
 *  AL_EFFECT_AUTOWAH
 *  AL_EFFECT_EQUALIZER
 *  AL_EFFECT_EAXREVERB
 *
 * */
public interface EffectFactory {
    /**
     * This class contains definitions for constructing a reverberation sound effect
     *
     * Reverb lets you transport a listener to a concert hall, a cave, a cathedral, or an intimate performance space.
     * It also allows for natural (or added) harmonics of a sound source to shine through and gives your mix extra
     * warmth and space
     *
     * Edit properties of a ReverbDef object then use {@link #createReverb()} or {@link #updateReverb(EffectFilter, ReverbDef)}
     * to transfer properties from ReverbDef to the effect object.
     */
    public class ReverbDef {

        // Below are the default Settings for reverb effects

        /**
         * Reverb density controls the coloration of the late reverb.
         * Lowering the value adds more coloration to the late reverb.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        public float REVERB_DENSITY = 1.0f,

        /**
         * Reverb diffusion controls the echo density in the reverberation decay.
         * Reducing diffusion gives the reverberation a more “grainy” character that is especially noticeable with
         * percussive sound sources.
         * Setting the diffusion to 0.0f will make reverberation sound like a succession of distinct echoes.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        REVERB_DIFFUSION = 1.0f,

        /**
         * The Reverb Gain property is the master volume control for the reflected sound (both early reflections and
         * reverberation) that the reverb effect adds to all sound sources. It sets the maximum amount of reflections
         * and reverberation added to the final sound mix.
         *
         * MAX: 1.0f (0db)
         * MIN: 0.0f (-100db)
         */
        REVERB_GAIN = 0.32f,

        /**
         * The Reverb Gain HF property further tweaks reflected sound by attenuating it at high frequencies. It controls
         * a low-pass filter that applies globally to the reflected sound of all sound sources feeding the particular
         * instance of the reverb effect.
         *
         * MAX: 1.0f (0db)
         * MIN: 0.0f (-100db)
         */
        REVERB_GAINHF = 0.89f,
        /**
         * The Decay Time property sets the reverberation decay time.
         *
         * MAX: 20.0f
         * MIN: 0.1f
         */
        REVERB_DECAY_TIME = 1.49f,

        /**
         * The Decay HF Ratio property sets the spectral quality of the Decay Time parameter. It is the ratio of
         * high-frequency decay time relative to the time set by Decay Time.
         *
         * The Decay HF Ratio value 1.0 is neutral: the decay time is equal for all frequencies. As Decay HF Ratio
         * increases above 1.0, the high-frequency decay time increases, so it’s longer than the decay time at low
         * frequencies. You hear a more brilliant reverberation with a longer decay at high frequencies. As the Decay
         * HF Ratio value decreases below 1.0, the high-frequency decay time decreases, so it’s shorter than the decay
         * time of the low frequencies. You hear a more natural reverberation.
         *
         * MAX: 2.0f
         * MIN: 0.1f
         */
        REVERB_DECAY_HFRATIO = 0.83f,

        /**
         * The Reflections Gain property controls the overall amount of initial reflections relative to the Gain
         * property.
         *
         * You can increase the amount of initial reflections to simulate a more narrow space or closer
         * walls, especially effective if you associate the initial reflections increase with a reduction in
         * reflections delays by lowering the value of the Reflection Delay property. To simulate open or semi-open
         * environments, you can maintain the amount of early reflections while reducing the value of the Late Reverb
         * Gain property, which controls later reflections.
         *
         * MAX: 3.16f (+10db)
         * MIN: 0.0f (-100db)
         */
        REVERB_REFLECTIONS_GAIN = 0.05f,

        /**
         * The Reflections Delay property is the amount of delay between the arrival time of the direct path from the
         * source to the first reflection from the source.
         *
         * You can reduce or increase Reflections Delay to simulate closer or more distant reflective surfaces—and
         * therefore control the perceived size of the room.
         *
         * MAX: 0.3f (300ms)
         * MIN: 0.0f (0ms)
         */
        REVERB_REFLECTIONS_DELAY = 0.007f,

        /**
         * The Late Reverb Gain property controls the overall amount of later reverberation relative to the Gain
         * property. (The Gain property sets the overall amount of both initial reflections and later reverberation.)
         *
         * MAX: 10.0f (+20db)
         * MIN: 0.0f (-100db)
         */
        REVERB_LATE_REVERB_GAIN = 1.26f,

        /**
         * The Late Reverb Delay property defines the begin time of the late reverberation relative to the time of the
         * initial reflection (the first of the early reflections).
         *
         * MAX: 0.1f (100ms)
         * MIN: 0.0f (0ms)
         */
        REVERB_LATE_REVERB_DELAY = 0.011f,

        /**
         * The Air Absorption Gain HF property controls the distance-dependent attenuation at high frequencies caused
         * by the propagation medium. It applies to reflected sound only.
         *
         * You can use Air Absorption Gain HF to simulate sound transmission through foggy air, dry air, smoky
         * atmosphere, and so on. The default value is 0.994 (-0.05 dB) per meter, which roughly corresponds to typical
         * condition of atmospheric humidity, temperature, and so on. Lowering the value simulates a more absorbent
         * medium (more humidity in the air, for example); raising the value simulates a less absorbent medium
         * (dry desert air, for example).
         *
         * MAX: 1.0f
         * MIN: 0.892f
         */
        REVERB_AIR_ABSORPTION_GAINHF = 0.994f,

        /**
         * The Room Rolloff Factor property is one of two methods available to attenuate the reflected sound
         * (containing both reflections and reverberation) according to source-listener distance.
         *
         * Setting the Room Rolloff Factor value to 1.0 specifies that the reflected sound will decay by 6 dB every
         * time the distance doubles. Any value other than 1.0 is equivalent to a scaling factor applied to the
         * quantity specified by ((Source listener distance) - (Reference Distance)). Reference Distance is an OpenAL
         * source parameter that specifies the inner border for distance rolloff effects: if the source comes closer to
         * the listener than the reference distance, the direct-path sound isn’t increased as the source comes closer
         * to the listener, and neither is the reflected sound.
         *
         * MAX: 10.0f
         * MIN: 0.0f
         */
        REVERB_ROOM_ROLLOFF_FACTOR = 0.0f;
    }

    /**
     * Create a reverb effect object based on the ReverbDef object
     *
     * @param def the ReverbDef object to create from
     */
    public EffectFilter createReverb(ReverbDef def);

    /**
     * Create default reverb object
     */
    public EffectFilter createReverb();

    /**
     * Update an existing reverb effect based on the ReverbDef
     *
     * @param reverb the reverb effect to edit
     * @param def    the ReverbDef object to create from
     */
    public void updateReverb(EffectFilter reverb, ReverbDef def);

    /**
     * This class contains definitions for constructing a EAXReverb object
     *
     * EAXReverb is a superset of the standard reverb effect with additional control over the reverb tone,
     * reverb directivity, and reverb granularity.
     *
     * The EAX Reverb is natively supported on any devices that support the EAX 3.0 or above standard, including:
     *   SoundBlaster Audigy series soundcards
     *   SoundBlaster X-Fi series soundcards
     *
     * The EAX Reverb will be emulated on devices that only support EAX 2.0. Note: The “Generic Software” device falls
     * into this category as the software mixer supports the EAX 2.0 Reverb effect.
     *
     * Edit properties of a EAXReverbDef object then use {@link #createEAXReverb()} or {@link #updateEAXReverb(EffectFilter, EAXReverbDef)}
     * to transfer properties from EAXReverbDef to the effect object.
     */
    public class EAXReverbDef {
        // Default Settings for EAX reverb effects
        /** These settings are the same as their standard reverb counterparts, refer to {@link ReverbDef} */
        public float                                        // [] denote the ranges of the values
                EAXREVERB_DENSITY = 1.0f,                   // [0.0f,1.0f]
                EAXREVERB_DIFFUSION = 1.0f,                 // [0.0f,1.0f]
                EAXREVERB_GAIN = 0.32f,                     // [0.0f,1.0f]
                EAXREVERB_GAINHF = 0.89f,                   // [0.0f,1.0f]
                EAXREVERB_DECAY_TIME = 1.49f,               // [0.1f,20.0f]
                EAXREVERB_DECAY_HFRATIO = 0.83f,            // [0.1f,2.0f]
                EAXREVERB_REFLECTIONS_GAIN = 0.05f,         // [0.0f,3.16f]
                EAXREVERB_REFLECTIONS_DELAY = 0.007f,       // [0.0f,0.3f]
                EAXREVERB_LATE_REVERB_GAIN = 1.26f,         // [0.0f,10.0f]
                EAXREVERB_LATE_REVERB_DELAY = 0.011f,       // [0.0f,0.1f]
                EAXREVERB_AIR_ABSORPTION_GAINHF = 0.994f,   // [0.892f,1.0f]
                EAXREVERB_ROOM_ROLLOFF_FACTOR = 0.0f;       // [0.0f,10.0f]

        /**
         * The Reverb Gain LF property further tweaks reflected sound by attenuating it at low frequencies.
         *
         * It controls a high-pass filter that applies globally to the reflected sound of all sound sources feeding the
         * particular instance of the reverb effect.
         * ({@link #EAXREVERB_LFREFERENCE } sets the frequency at which the value of this property is measured.)
         *
         * MAX: 1.0f (0db)
         * MIN: 0.0f (-100db)
         */
        public float EAXREVERB_GAINLF = 1.0f,

        /**
         * The Decay LF Ratio property adjusts the spectral quality of the Decay Time parameter.
         *
         * It is the ratio of low-frequency decay time relative to the time set by Decay Time.
         * The Decay LF Ratio value 1.0 is neutral: the decay time is equal for all frequencies.
         * As Decay LF Ratio increases above 1.0, the low-frequency decay time increases so it’s longer than the decay
         * time at mid frequencies. You hear a more booming reverberation with a longer decay at low frequencies.
         * As the Decay LF Ratio value decreases below 1.0, the low-frequency decay time decreases so it’s shorter
         * than the decay time of the mid frequencies. You hear a more tinny reverberation.
         *
         * MAX: 2.0f
         * MIN: 0.1f
         */
        EAXREVERB_DECAY_LFRATIO = 1.0f,

        /**
         * Echo Depth introduces a cyclic echo in the reverberation decay, which will be noticeable with transient or
         * percussive sounds. A larger value of Echo Depth will make this effect more prominent.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        EAXREVERB_ECHO_DEPTH = 0.0f,

        /**
         * Echo Time controls the rate at which the cyclic echo repeats itself along the reverberation decay.
         * For example, the default setting for Echo Time is 250 ms. causing the echo to occur 4 times per second.
         * Therefore, if you were to clap your hands in this type of environment, you will hear four repetitions of
         * clap per second.
         *
         * MAX: 0.25f
         * MIN: 0.075f
         */
        EAXREVERB_ECHO_TIME = 0.25f,

        /**
         * Using Modulation time and Modulation Depth, you can create a pitch modulation in the reverberant sound.
         * This will be most noticeable applied to sources that have tonal color or pitch. You can use this to make
         * some trippy effects!
         *
         * Modulation Time controls the speed of the vibrato (rate of periodic changes in pitch).
         *
         * MAX: 4.0f
         * MIN: 0.04f
         */
        EAXREVERB_MODULATION_TIME = 0.25f,

        /**
         * Modulation Depth controls the amount of pitch change. Low values of Diffusion will contribute to reinforcing
         * the perceived effect by reducing the mixing of overlapping reflections in the reverberation decay.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        EAXREVERB_MODULATION_DEPTH = 0.0f,

        /**
         * The properties HF Reference and LF Reference determine respectively the frequencies at which the
         * high-frequency effects and the low-frequency effects created by EAX Reverb properties are measured,
         * for example Decay HF Ratio and Decay LF Ratio.  Note that it is necessary to maintain a factor of at least
         * 10 between these two reference frequencies so that low frequency and high frequency properties can be
         * accurately controlled and will produce independent effects. In other words, the LF Reference value should
         * be less than 1/10 of the HF Reference value.
         *
         * HF MAX: 20000.0f
         * HF MIN: 1000.0f
         *
         * LF MAX: 1000.0f
         * LF MIN: 20.0f
         */
        EAXREVERB_HFREFERENCE = 5000.0f, EAXREVERB_LFREFERENCE = 250.0f;


        /**
         * The Reflections Pan property is a 3D vector that controls the spatial distribution of the cluster of early
         * reflections.
         *
         * The direction of this vector controls the global direction of the reflections, while its magnitude controls
         * how focused the reflections are towards this direction.  It is important to note that the direction of the
         * vector is interpreted in the coordinate system of the user, without taking into account the orientation of
         * the virtual listener. For instance, assuming a four-point loudspeaker playback system, setting Reflections
         * Pan to (0., 0., 0.7) means that the reflections are panned to the front speaker pair, whereas as setting
         * of (0., 0., −0.7) pans the reflections towards the rear speakers. These vectors follow the a left-handed
         * co-ordinate system, unlike OpenAL uses a right-handed co-ordinate system.  If the magnitude of Reflections
         * Pan is zero (the default setting), the early reflections come evenly from all directions. As the magnitude
         * increases, the reflections become more focused in the direction pointed to by the vector. A magnitude of
         * 1.0 would represent the extreme case, where all reflections come from a single direction.
         *
         * THE VECTOR SHOULD HAVE A MAGNITUDE BETWEEN 0 AND 1
         *
         * MAX: [1.0f,1.0f,1.0f]
         * MIN: [-1.0f,-1.0f,-1.0f]
         */
        public float[] EAXREVERB_REFLECTIONS_PAN = {0.0f,0.0f,0.0f},

        /**
         * The Late Reverb Pan property is a 3D vector that controls the spatial distribution of the late
         * reverb. T
         *
         * The direction of this vector controls the global direction of the reverb, while its magnitude
         * controls how focused the reverb are towards this direction.
         *
         * The details under {@link #EAXREVERB_REFLECTIONS_PAN} also apply to Late Reverb Pan.
         */
        EAXREVERB_LATE_REVERB_PAN = {0.0f,0.0f,0.0f};
    }

    /**
     * Create a EAXReverb effect object based on the EAXReverbDef object
     *
     * @param def the EAXReverbDef object to create from
     */
    public EffectFilter createEAXReverb(EAXReverbDef def);

    /**
     * Create default EAXReverb object
     */
    public EffectFilter createEAXReverb();

    /**
     * Update an existing EAXReverb effect based on the EAXReverbDef
     *
     * @param eaxReverb the EaxReverb effect to edit
     * @param def the EAxReverbDef object to create from
     */
    public void updateEAXReverb(EffectFilter eaxReverb, EAXReverbDef def);

    /**
     * This class contains definitions for constructing a chorus effect filter object
     *
     * The chorus effect essentially replays the input audio accompanied by another slightly delayed version of the
     * signal, creating a ‘doubling’ effect. This was originally intended to emulate the effect of several musicians
     * playing the same notes simultaneously, to create a thicker, more satisfying sound.
     *
     * To add some variation to the effect, the delay time of the delayed versions of the input signal is modulated by
     * an adjustable oscillating waveform. This causes subtle shifts in the pitch of the delayed signals, emphasizing
     * the thickening effect.
     *
     * Edit properties of a ChorusDef object then use {@link #createChorus()} or {@link #updateChorus(EffectFilter, ChorusDef)}
     * to transfer properties from ChorusDef to the effect object.
     */
    public class ChorusDef {

        // Default Settings for Chorus effects
        /**
         * This property sets the waveform shape of the LFO that controls the delay time of the delayed signals.
         * THIS IS EITHER 0 or 1
         *
         * 0: Sin waveform
         * 1: Triangle waveform
         */
        public int CHORUS_WAVEFORM = 1,

        /**
         * This property controls the phase difference between the left and right LFO’s. At zero degrees the two LFOs
         * are synchronized. Use this parameter to create the illusion of an expanded stereo field of the output
         * signal.
         *
         * MAX: 180
         * MIN: -180
         */
        CHORUS_PHASE = 90;

        /**
         * This property sets the modulation rate of the LFO that controls the delay time of the delayed signals.
         *
         * MAX: 10.0f
         * MIN: 0.0f
         */
        public float CHORUS_RATE = 1.1f,

        /**
         * This property controls the amount by which the delay time is modulated by the LFO.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        CHORUS_DEPTH = 0.1f,

        /**
         * This property controls the amount of processed signal that is fed back to the input of the chorus effect.
         *
         * Negative values will reverse the phase of the feedback signal. At full magnitude the identical sample will
         * repeat endlessly. At lower magnitudes the sample will repeat and fade out over time. Use this parameter to
         * create a “cascading” chorus effect.
         *
         * MAX: 1.0f
         * MIN: -1.0f
         */
        CHORUS_FEEDBACK = 0.25f,

        /**
         * This property controls the average amount of time the sample is delayed before it is played back, and with
         * feedback, the amount of time between iterations of the sample.
         *
         * Larger values lower the pitch. Smaller values make the chorus sound like a {@link FlangerDef}, but with
         * different frequency characteristics.
         *
         * MAX: 0.016f
         * MIN: 0.0f
         */
        CHORUS_DELAY = 0.016f;
    }

    /**
     * Create a Chorus effect object based on the ChorusDef object
     *
     * @param def the ChorusDef object to create from
     */
    public EffectFilter createChorus(ChorusDef def);

    /**
     * Create default Chorus object
     */
    public EffectFilter createChorus();

    /**
     * Update an existing chorus effect based on the ChorusDef
     *
     * @param chorus the chorus effect to edit
     * @param def the ChorusDef object to create from
     */
    public void updateChorus(EffectFilter chorus, ChorusDef def);

    /**
     * This class contains definitions for constructing a distortion object
     *
     * The distortion effect simulates turning up (overdriving) the gain stage on a guitar amplifier or adding a
     * distortion pedal to an instrument’s output.  It is achieved by clipping the signal (adding more square wave-like
     * components) and adding rich harmonics.  The distortion effect could be very useful for adding extra dynamics to
     * engine sounds in a driving simulator, or modifying samples such as vocal communications.
     *
     * Edit properties of a DistortionDef object then use {@link #createDistortion()} or {@link #updateDistortion(EffectFilter, DistortionDef)} )}
     * to transfer properties from DistortionDef to the effect object.
     */
    public class DistortionDef {
        //Default Settings for Distortion effects

        /**
         * This property controls the shape of the distortion. The higher the value for Edge, the ‘dirtier’ and
         * ‘fuzzier’ the effect.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        public float DISTORTION_EDGE = 0.2f,

        /**
         *  This property allows you to attenuate the distorted sound
         *
         *  MAX: 1.0f
         *  MIN: 0.01f
         */
        DISTORTION_GAIN = 0.05f,

        /**
         * Input signal can have a low pass filter applied, to limit the amount of high frequency signal feeding into
         * the distortion effect.
         *
         * MAX: 24000.0f
         * MIN: 80.0f
         */
        DISTORTION_LOWPASS_CUTOFF = 8000.0f,

        /**
         * This property controls the frequency at which the post-distortion attenuation (Distortion Gain) is active.
         *
         * MAX: 24000.0f
         * MIN: 80.0f
         */
        DISTORTION_EQCENTER = 3600.0f,

        /**
         * This property controls the bandwidth of the post-distortion attenuation.
         *
         * MAX: 24000.0f
         * MIN: 80.0f
         */
        DISTORTION_EQBANDWIDTH = 3600.0f;
    }

    /**
     * Create a Distortion effect object based on the DistortionDef object
     *
     * @param def the DistortionDef object to create from
     */
    public EffectFilter createDistortion(DistortionDef def);

    /**
     * Create default Distortion object
     */
    public EffectFilter createDistortion();

    /**
     * Update an existing distortion effect based on the DistortionDef
     *
     * @param distortion    the distortion effect to edit
     * @param def       the distortion object to create from
     */
    public void updateDistortion(EffectFilter distortion, DistortionDef def);

    /**
     * This class contains definitions for constructing an echo object
     *
     * The echo effect generates discrete, delayed instances of the input signal. The amount of delay and feedback is
     * controllable. The delay is ‘two tap’ – you can control the interaction between two separate instances of echoes.
     *
     * Edit properties of a EchoDef object then use {@link #createEcho()} or {@link #updateEcho(EffectFilter, EchoDef)} )}
     * to transfer properties from EchoDef to the effect object.
     */
    public class EchoDef {
        // Default Settings for Echo effects
        /**
         * This property controls the delay between the original sound and the first ‘tap’, or echo instance.
         * Subsequently, the value for Echo Delay is used to determine the time delay between each ‘second tap’ and
         * the next ‘first tap’.
         *
         * MAX: 0.207f
         * MIN: 0.0f
         */
        public float ECHO_DELAY = 0.1f,

        /**
         * This property controls the delay between the first ‘tap’ and the second ‘tap’. Subsequently, the value for
         * Echo LR Delay is used to determine the time delay between each ‘first tap’ and the next ‘second tap’.
         *
         * MAX: 0.404f
         * MIN: 0.0f
         */
        ECHO_LRDELAY = 0.1f,

        /**
         * This property controls the amount of high frequency damping applied to each echo. As the sound is
         * subsequently fed back for further echoes, damping results in an echo which progressively gets softer in tone
         * as well as intensity
         *
         * MAX: 0.99f
         * MIN: 0.0f
         */
        ECHO_DAMPING = 0.5f,

        /**
         * This property controls the amount of feedback the output signal fed back into the input. Use this parameter
         * to create “cascading” echoes. At full magnitude, the identical sample will repeat endlessly. Below full
         * magnitude, the sample will repeat and fade.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        ECHO_FEEDBACK = 0.5f,

        /**
         * This property controls how hard panned the individual echoes are. With a value of 1.0, the first ‘tap’ will
         * be panned hard left, and the second tap hard right.  A value of –1.0 gives the opposite result. Settings
         * nearer to 0.0 result in less emphasized panning.
         *
         * MAX: 1.0f
         * MIN: -1.0f
         */
        ECHO_SPREAD = -1.0f;
    }

    /**
     * Create an Echo effect object based on the EchoDef object
     *
     * @param def the EchoDef object to create from
     */
    public EffectFilter createEcho(EchoDef def) ;

    /**
     * Create default Echo object
     */
    public EffectFilter createEcho();

    /**
     * Update an existing echo effect based on the EchoDef
     *
     * @param echo the echo effect to edit
     * @param def the echo object to create from
     */
    public void updateEcho(EffectFilter echo, EchoDef def);

    /**
     * This class contains definitions for constructing a flanger object
     *
     * The flanger effect creates a “tearing” or “whooshing” sound (like a jet flying overhead). It works by sampling
     * a portion of the input signal, delaying it by a period modulated between 0 and 4ms by a low-frequency
     * oscillator, and then mixing it with the source signal.
     *
     * Edit properties of a FlangerDef object then use {@link #createFlanger()} or {@link #updateFlanger(EffectFilter, FlangerDef)} )}
     * to transfer properties from FlangerDef to the effect object.
     */
    public class FlangerDef {
        // Default Settings for Flanger effects
        /**
         * Selects the shape of the LFO waveform that controls the amount of the delay of the sampled signal.
         *
         * 0: Sinusoid Waveform
         * 1: Triangle Waveform
         */
        public int FLANGER_WAVEFORM = 1,

        /**
         * This changes the phase difference between the left and right LFO’s.
         * At zero degrees the two LFOs are synchronized.
         *
         * MAX: 180
         * MIN: -180
         */
        FLANGER_PHASE = 0;

        /**
         * The number of times per second the LFO controlling the amount of delay repeats.
         * Higher values increase the pitch modulation.
         *
         * MAX: 10.0f
         * MIN: 0.0f
         */
        public float FLANGER_RATE = 0.27f,

        /**
         * The ratio by which the delay time is modulated by the LFO.
         * Use this parameter to increase the pitch modulation.
         *
         * MAX: 1.0f
         * MIN: 0.0f
         */
        FLANGER_DEPTH = 1.0f,

        /**
         * This is the amount of the output signal level fed back into the effect’s input. A negative value will
         * reverse the phase of the feedback signal. Use this parameter to create an “intense metallic” effect.
         * At full magnitude, the identical sample will repeat endlessly. At less than full magnitude, the sample
         * will repeat and fade out over time.
         *
         * MAX: 1.0f
         * MIN: -1.0f
         */
        FLANGER_FEEDBACK = -0.5f,

        /**
         * The average amount of time the sample is delayed before it is played back; with feedback, the amount of time
         * between iterations of the sample.
         *
         * MAX: 0.004f
         * MIN: 0.0f
         */
        FLANGER_DELAY = 0.002f;
    }

    /**
     * Create a Flanger effect object based on the FlangerDef object
     *
     * @param def the FlangerDef object to create from
     */
    public EffectFilter createFlanger(FlangerDef def);

    /**
     * Create default Flanger object
     */
    public EffectFilter createFlanger();

    /**
     * Update an existing flanger effect based on the FlangerDef
     *
     * @param flanger the flanger effect to edit
     * @param def the FlangerDef object to create from
     */
    public void updateFlanger(EffectFilter flanger, FlangerDef def);

    /**
     * This class contains definitions for constructing a FrequencyShifter object
     *
     * The frequency shifter is a single-sideband modulator, which translates all the component frequencies of the
     * input signal by an equal amount. Unlike the pitch shifter, which attempts to maintain harmonic relationships
     * in the signal, the frequency shifter disrupts harmonic relationships and radically alters the sonic qualities
     * of the signal. Applications of the frequency shifter include the creation of bizarre distortion, phaser, stereo
     * widening and rotating speaker effects.
     *
     * Edit properties of a FreqShiftDef object then use {@link #createFreqShift()} or {@link #updateFreqShift(EffectFilter, FreqShiftDef)} )}
     * to transfer properties from FreqShiftDef to the effect object.
     */
    public class FreqShiftDef {
        // Default Settings for frequency shifter effects

        /**
         * This is the carrier frequency. For carrier frequencies below the audible range, the single-sideband
         * modulator may produce phaser effects, spatial effects or a slight pitch-shift. As the carrier frequency
         * increases, the timbre of the sound is affected; a piano or guitar note becomes like a bell's chime, and a
         * human voice sounds extraterrestrial!
         *
         * MAX: 24000.0f
         * MIN: 0.0f
         */
        public float FREQUENCY_SHIFTER_FREQUENCY = 0.0f;

        /**
         * These select which internal signals are added together to produce the output. Different combinations of
         * values will produce slightly different tonal and spatial effects.
         *
         * 0: Down
         * 1: Up
         * 2: Off
         */
        public int FREQUENCY_SHIFTER_LEFT_DIRECTION  = 0, FREQUENCY_SHIFTER_RIGHT_DIRECTION = 0;
    }

    /**
     * Create an FreqShift effect object based on the FreqShiftDef object
     *
     * @param def the FreqShiftDef object to create from
     */
    public EffectFilter createFreqShift(FreqShiftDef def);

    /**
     * Create default FreqShift object
     */
    public EffectFilter createFreqShift();

    /**
     * Update an existing freqShift effect based on the FreqShiftDef
     *
     * @param freqShift the freqShift effect to edit
     * @param def the FreqShiftDef object to create from
     */
    public void updateFreqShift(EffectFilter freqShift, FreqShiftDef def);

    /**
     * This class contains definitions for constructing a VocalMorpher object
     *
     * The vocal morpher consists of a pair of 4-band formant filters, used to impose vocal tract effects upon the
     * input signal. If the input signal is a broadband sound such as pink noise or a car engine, the vocal morpher
     * can provide a wide variety of filtering effects. A low-frequency oscillator can be used to morph the filtering
     * effect between two different phonemes. The vocal morpher is not necessarily intended for use on voice signals;
     * it is primarily intended for pitched noise effects, vocal-like wind effects, etc.
     *
     * Edit properties of a VocalMorpherDef object then use {@link #createVocalMorpher()} or {@link #updateVocalMorpher(EffectFilter, VocalMorpherDef)} )}
     * to transfer properties from VocalMorpherDef to the effect object.
     */
    public class VocalMorpherDef {
        // Default Settings for vocal morpher effects

        /**
         * This controls the frequency of the low-frequency oscillator used to morph between the two phoneme filters
         *
         * MAX: 10.0f
         * MIN: 0.0f
         */
        public float VOCAL_MORPHER_RATE = 1.41f;

        /**
         * If both parameters are set to the same phoneme, that determines the filtering effect that will be heard.
         * If these two parameters are set to different phonemes, the filtering effect will morph between the two
         * settings at a rate specified by AL_VOCAL_MORPHER_RATE.
         *
         *
         * Below are the available phoneme types:
         * A = 0, E = 1, I = 2, O = 3, U = 4, AA = 5,AE = 6, AH = 7, AO = 8, EH = 9, ER = 10, IH = 11, IY = 12,
         * UH = 13, UW = 14, B = 15, D = 16, F = 17, G = 18, J = 19, K = 20, L = 21, M = 22, N = 23, P = 24, R = 25,
         * S = 26, T = 27, V = 28, Z = 29
         */
        public int VOCAL_MORPHER_PHONEMEA = 0, VOCAL_MORPHER_PHONEMEB = 10;

        /**
         * These are used to adjust the pitch of phoneme filters A and B in 1-semitone increments.
         *
         * MAX: 24
         * MIN: -24
         * */
        public int VOCAL_MORPHER_PHONEMEA_COARSE_TUNING = 0, VOCAL_MORPHER_PHONEMEB_COARSE_TUNING = 0;

        /**
         * This controls the shape of the low-frequency oscillator used to morph between the two phoneme filters.
         * By selecting a saw tooth wave and a slow AL_VOCAL_MORPHER_RATE, one can create a filtering effect that
         * slowly increases or decreases in pitch (depending on which of the two phoneme filters A or B is perceived
         * as being higher-pitched).
         *
         * 0: Sin
         * 1: Triangle
         * 2: Saw
         */
         public int VOCAL_MORPHER_WAVEFORM = 0;
    }

    /**
     * Create an VocalMorpher effect object based on the VocalMorpher object
     *
     * @param def the VocalMorpherDef object to create from
     */
    public EffectFilter createVocalMorpher(VocalMorpherDef def);

    /**
     * Create default VocalMorpher object
     */
    public EffectFilter createVocalMorpher();

    /**
     * Update an existing vocalMorpher effect based on the VocalMorpherDef
     *
     * @param vocalMorpher the vocalMorpher effect to edit
     * @param def the VocalMorpherDef object to create from
     */
    public void updateVocalMorpher(EffectFilter vocalMorpher, VocalMorpherDef def);

    /**
     * This class contains definitions for constructing a Pitch Shifter object
     *
     * The pitch shifter applies time-invariant pitch shifting to the input signal, over a one octave range and
     * controllable at a semi-tone and cent resolution.
     *
     * Edit properties of a PitchShifterDef object then use {@link #createPitchShifter()} or {@link #updatePitchShifter(EffectFilter, PitchShifterDef)} )}
     * to transfer properties from PitchShifterDef to the effect object.
     */
    public class PitchShifterDef {
        // Default Settings for pitch shifter effects
        /**
         * This sets the number of semitones by which the pitch is shifted. There are 12 semitones per octave.
         * Negative values create a downwards shift in pitch, positive values pitch the sound upwards.
         *
         * MAX: 12
         * MIN: -12
         */
        public int AL_PITCH_SHIFTER_DEFAULT_COARSE_TUNE = 12,

        /**
         * This sets the number of cents between Semitones a pitch is shifted. A Cent is 1/100th of a Semitone.
         * Negative values create a downwards shift in pitch, positive values pitch the sound upwards.
         *
         * MAX: 50
         * MIN: -50
         */
        AL_PITCH_SHIFTER_DEFAULT_FINE_TUNE   = 0;
    }

    /**
     * Create an PitchShifter effect object based on the PitchShifter object
     *
     * @param def the PitchShifterDef object to create from
     */
    public EffectFilter createPitchShifter(PitchShifterDef def);

    /**
     * Create default PitchShifter object
     */
    public EffectFilter createPitchShifter();

    /**
     * Update an existing PitchShifter effect based on the PitchShifterDef
     *
     * @param pitchShifter the pitchShifter effect to edit
     * @param def the PitchShifterDef object to create from
     */
    public void updatePitchShifter(EffectFilter pitchShifter, PitchShifterDef def);

    /**
     * This class contains definitions for constructing a Ring Modulator object
     *
     * The ring modulator multiplies an input signal by a carrier signal in the time domain,
     * resulting in tremolo or inharmonic effects.
     *
     * Edit properties of a RingModDef object then use {@link #createRingMod()} or {@link #updateRingMod(EffectFilter, RingModDef)} )}
     * to transfer properties from RingModDef to the effect object.
     */
    public class RingModDef {
        // Default Settings for ring modulator effects
        /**
         * This is the frequency of the carrier signal. If the carrier signal is slowly varying (less than 20 Hz),
         * the result is a tremolo (slow amplitude variation) effect. If the carrier signal is in the audio range,
         * audible upper and lower sidebands begin to appear, causing an inharmonic effect. The carrier signal itself
         * is not heard in the output.
         *
         * MAX: 8000.0f
         * MIN: 0.0f
         */
        public float RING_MODULATOR_FREQUENCY = 440.0f,

        /**
         * This controls the cutoff frequency at which the input signal is high-pass filtered before being ring
         * modulated. If the cutoff frequency is 0, the entire signal will be ring modulated. If the cutoff frequency
         * is high, very little of the signal (only those parts above the cutoff) will be ring modulated.
         *
         * MAX: 24000.0f
         * MIN: 0.0f
         */
        RING_MODULATOR_HIGHPASS_CUTOFF = 800.0f;

        /**
         * This controls which waveform is used as the carrier signal. Traditional ring modulator and tremolo effects
         * generally use a sinusoidal carrier. Sawtooth and square waveforms are may cause unpleasant aliasing.
         *
         * 0: Sin
         * 1: Triangle
         * 2: Square
         */
        public int RING_MODULATOR_WAVEFORM = 0;
    }

    /**
     * Create a RingModulator effect object based on the RingModDef object
     *
     * @param def the RingModDef object to create from
     */
    public EffectFilter createRingMod(RingModDef def);

    /**
     * Create default Ring Modulator object
     */
    public EffectFilter createRingMod();

    /**
     * Update an existing RingModulator effect based on the RingModDef
     *
     * @param ringMod the RingMod effect to edit
     * @param def the RingModDef object to create from
     */
    public void updateRingMod(EffectFilter ringMod, RingModDef def);


    /**
     * This class contains definitions for constructing a AutoWAH object
     *
     * The Auto-wah effect emulates the sound of a wah-wah pedal used with an electric guitar, or a mute on a brass
     * instrument. Such effects allow a musician to control the tone of their instrument by varying the point at which
     * high frequencies are cut off. This OpenAL Effects Extension effect is called Auto-wah because there is no user
     * input for modulating the cut-off point. Instead the effect is achieved by analysing the input signal, and
     * applying a band-pass filter according the intensity of the incoming audio.
     *
     * Edit properties of a AutoWAHDef object then use {@link #createAutoWAH()} or {@link #updateAutoWAH(EffectFilter, AutoWAHDef)} )}
     * to transfer properties from AutoWAHDef to the effect object.
     */
    public class AutoWAHDef {
        // Default Settings for AutoWAH effects

        /**
         * This property controls the time the filtering effect takes to sweep from minimum to maximum center frequency
         * when it is triggered by input signal.
         *
         * MAX: 1.0f
         * MIN: 0.0001f
         */
        public float AUTOWAH_ATTACK_TIME  = 0.06f,

        /**
         * This property controls the time the filtering effect takes to sweep from maximum back to base center
         * frequency, when the input signal ends.
         *
         * MAX: 1.0f
         * MIN: 0.0001f
         */
        AUTOWAH_RELEASE_TIME = 0.06f,

        /**
         * This property controls the resonant peak, sometimes known as emphasis or Q, of the auto-wah band-pass
         * filter. Resonance occurs when the effect boosts the frequency content of the sound around the point at which
         * the filter is working.  A high value promotes a highly resonant, sharp sounding effect.
         *
         * MAX: 1000.0
         * MIN: 2.0
         */
        AUTOWAH_RESONANCE = 1000.0f,

        /**
         * This property controls the input signal level at which the band-pass filter will be fully opened.
         *
         * MAX: 31621.0
         * MIN: 0.00003
         */
        AUTOWAH_PEAK_GAIN = 11.22f;
    }

    /**
     * Create a AutoWAH effect object based on the AutoWAHDef object
     *
     * @param def the AutoWAHDef object to create from
     */
    public EffectFilter createAutoWAH(AutoWAHDef def);

    /**
     * Create default AutoWAH object
     */
    public EffectFilter createAutoWAH();

    /**
     * Update an existing AutoWAH effect based on the AutoWAHDef
     *
     * @param AutoWAH the AutoWAH effect to edit
     * @param def the AutoWAHDef object to create from
     */
    public void updateAutoWAH(EffectFilter AutoWAH, AutoWAHDef def);

    /**
     * This class contains definitions for constructing an Equalizer object
     *
     * The OpenAL Effects Extension EQ is very flexible, providing tonal control over four different adjustable
     * frequency ranges. The lowest frequency range is called “low.” The middle ranges are called “mid1” and “mid2.”
     * The high range is called “high.”
     *
     * Edit properties of a EqualizerDef object then use {@link #createEqualizer()} or {@link #updateEqualizer(EffectFilter, EqualizerDef)} )}
     * to transfer properties from EqualizerDef to the effect object.
     */
    public class EqualizerDef {
        // Default Settings for Equalizer effects
        /**
         * This property controls amount of cut or boost on the low frequency range.
         *
         * MAX: 7.943f
         * MIN: 0.126f
         */
        public float EQUALIZER_LOW_GAIN = 1.0f,

        /**
         * This property controls the low frequency below which signal will be cut off.
         *
         * MAX: 800.0f
         * MIN: 50.0f
         */
        EQUALIZER_LOW_CUTOFF = 200.0f,

        /**
         * This property allows you to cut / boost signal on the “mid1” range.
         *
         * MAX: 7.943f
         * MIN: 0.126f
         */
        EQUALIZER_MID1_GAIN = 1.0f,

        /**
         * This property sets the center frequency for the “mid1” range.
         *
         * MAX: 3000.0f
         * MIN: 200.0f
         */
        EQUALIZER_MID1_CENTER = 500.0f,

        /**
         * This property controls the width of the “mid1” range.
         *
         * MAX: 1.0f
         * MIN: 0.01f
         */
        EQUALIZER_MID1_WIDTH = 1.0f,

        /**
         * This property allows you to cut / boost signal on the “mid2” range.
         *
         * MAX: 7.943f
         * MIN: 0.126f
         */
        EQUALIZER_MID2_GAIN = 1.0f,

        /**
         * This property sets the center frequency for the “mid2” range.
         *
         * MAX: 8000.0f
         * MIN: 1000.0f
         */
        EQUALIZER_MID2_CENTER = 3000.0f,

        /**
         * This property controls the width of the “mid2” range.
         *
         * MAX: 1.0f
         * MIN: 0.01f
         */
        EQUALIZER_MID2_WIDTH = 1.0f,

        /**
         * This property allows you to cut / boost the signal at high frequencies.
         *
         * MAX: 7.943f
         * MIN: 0.126f
         */
        EQUALIZER_HIGH_GAIN = 1.0f,

        /**
         * This property controls the high frequency above which signal will be cut off.
         *
         * MAX: 16000.0f
         * MIN: 4000.0f
         */
        EQUALIZER_HIGH_CUTOFF = 6000.0f;
    }

    /**
     * Create an Equalizer effect object based on the EqualizerDef object
     *
     * @param def the EqualizerDef object to create from
     */
    public EffectFilter createEqualizer(EqualizerDef def);

    /**
     * Create default Equalizer object
     */
    public EffectFilter createEqualizer();

    /**
     * Update an existing Equalizer effect based on the EqualizerDef
     *
     * @param Equalizer the Equalizer effect to edit
     * @param def the EqualizerDef object to create from
     */
    public void updateEqualizer(EffectFilter Equalizer, EqualizerDef def);
}
