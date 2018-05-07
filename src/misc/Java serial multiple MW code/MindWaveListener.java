package c3nl.eeg;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gps99
 */
public interface MindWaveListener {

    /**
     *
     * Returns the current attention level [0, 100]. Values in [1, 20] are
     * considered strongly lowered. Values in [20, 40] are considered reduced
     * levels. Values in [40, 60] are considered neutral. Values in [60, 80] are
     * considered slightly elevated. Values in [80, 100] are considered
     * elevated.
     */
    public void attentionEvent(int attentionLevel);

    
    public void blinkEvent(int blinkStrength);
    /**
     ** Returns the current meditation level [0, 100]. The interpretation of
     * the values is the same as for the attentionLevel.
     *
     *
     */
    public void meditationEvent(int meditationLevel);

    /**
     * * Returns the signal level [0, 200]. The greater the value, the more
     * noise is detected in the signal. 200 is a special value that means that
     * the ThinkGear contacts are not touching the skin.
     *
     *
     */
    public void poorSignalEvent(int signalLevel);

    /**
     * * Returns the EEG data. The values have no units.
     *
     */
    public void eegEvent(int delta, int theta, int low_alpha, int high_alpha, int low_beta, int high_beta, int low_gamma, int mid_gamma);

    /**
     * Returns the the current 512 raw signal samples [-32768, 32767].
     */
    public void rawEvent(int[] raw);
}
