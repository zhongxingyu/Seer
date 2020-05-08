 package no.bouvet.kpro.renderer.audio;
 
 import no.bouvet.kpro.renderer.OldRenderer;
 import no.lau.vdvil.instruction.SuperInstruction;
 
 /**
  * The AudioInstruction class is a specialization of the AbstractInstruction class. It
  * represents an instruction to the AudioRenderer.
  * 
  * An AudioInstruction instructs the AudioRenderer to mix a section of audio
  * from an AudioSource into its output. It may also contain rate or volume
  * adjustments, with either or both of these being linearly interpolated across
  * the audio section.
  * 
  * @author Michael Stokes
  * @author Stig Lau
  */
 public class AudioInstruction extends SuperInstruction {
 	protected AudioSource _source;
 	protected int _cue;
 	protected int _duration;
 
 	protected int _rate1;
 	protected long _rated;
 	protected int _volume1;
 	protected long _volumed;
 
 	protected int _cacheExternal = -1;
 	protected int _cacheInternal = -1;
 
 	/**
 	 * Construct a new AudioInstruction.
 	 * 
 	 * @param start
 	 *            the start time of the instruction in samples (super)
 	 * @param end
 	 *            the end time of the instruction in samples (super)
 	 * @param source
 	 *            the AudioSource to use
 	 * @param cue
 	 *            the cue point (start point) within source in samples
 	 * @param duration
 	 *            the duration within source in samples
 	 */
 	public AudioInstruction(int start, int end, AudioSource source, int cue, int duration) {
        super(start, end);
 
 		_source = source;
 		_cue = cue;
 		_duration = duration;
 
 		_rate1 = OldRenderer.RATE;
 		_rated = 0;
 		_volume1 = 127;
 		_volumed = 0;
 	}
 
     /**
 	 * Get the AudioSource.
 	 * 
 	 * @return the AudioSource
 	 */
 	public AudioSource getSource() {
 		return _source;
 	}
 
 	/**
 	 * Get the cuepoint within source in samples.
 	 *
 	 * @return the cuepoint within source in samples
 	 */
 	public int getCue() {
 		return _cue;
 	}
 
 	/**
 	 * Get the duration within source in samples.
 	 * 
 	 * @return the duration within source in samples
 	 */
 	public int getSourceDuration() {
 		return _duration;
 	}
 
 	/**
 	 * Set a constant playback rate, where 1.0 is the original rate.
 	 * 
 	 * @param rate
 	 *            the constant rate
 	 */
 	public void setConstantRate(float rate) {
 		_rate1 = (int) (rate * OldRenderer.RATE);
 		_rated = 0;
 	}
 
 	/**
 	 * Set a linearly interpolated playback rate, where 1.0 is the original
 	 * rate.
 	 * 
 	 * @param rate1
 	 *            the rate at the start of the section
 	 * @param rate2
 	 *            the rate at the end of the section
 	 */
 	public void setInterpolatedRate(float rate1, float rate2) {
 		_rate1 = (int) (rate1 * OldRenderer.RATE);
 		_rated = (long) (rate2 * OldRenderer.RATE) - _rate1;
 	}
 
 	/**
 	 * Get the rate at a given point within the section.
 	 * 
 	 * @param time
 	 *            the time in samples within the section, where 0 is the start
 	 *            of the section
 	 * @return the rate in integer form relative to OldRenderer.RATE
 	 */
 	public int getInterpolatedRate(int time) {
 		return _rate1 + (int) (_rated * time / _duration);
 	}
 
 	/**
 	 * Set a constant playback volume, where 1.0 is the original volume.
 	 * 
 	 * @param volume
 	 *            the constant volume
 	 */
 	public void setConstantVolume(float volume) {
 		_volume1 = (int) (volume * 127);
 		_volumed = 0;
 	}
 
 	/**
 	 * Set a linearly interpolated playback volume, where 1.0 is the original
 	 * volume.
 	 * 
 	 * @param volume1
 	 *            the volume at the start of the section
 	 * @param volume2
 	 *            the volume at the end of the section
 	 */
 	public void setInterpolatedVolume(float volume1, float volume2) {
 		_volume1 = (int) (volume1 * 127);
 		_volumed = (long) (volume2 * 127) - _volume1;
 	}
 
 	/**
 	 * Get the volume at a given point within the section.
 	 * 
 	 * @param time
 	 *            the time in samples within the section, where 0 is the start
 	 *            of the section
 	 * @return the volume in integer form where 127 is natural volume
 	 */
 	public int getInterpolatedVolume(int time) {
 		return _volume1 + (int) (_volumed * time / _duration);
 	}
 
 	public int getCacheExternal() {
 		return _cacheExternal;
 	}
 
 	public int getCacheInternal() {
 		return _cacheInternal;
 	}
 
 	public void setCacheExternal(int cacheExternal) {
 		_cacheExternal = cacheExternal;
 	}
 
 	public void setCacheInternal(int cacheInternal) {
 		_cacheInternal = cacheInternal;
 	}
 
 	public void advanceCache(int external, int internal) {
 		_cacheExternal += external;
 		_cacheInternal += internal;
 	}
 }
