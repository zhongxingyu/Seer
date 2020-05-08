 package strain.sound;
 
 /**
  * Combines the functionality of a MusicEngine and an EffectEngine into one
  * class.
  */
 public class SoundEngine {
 	
 	/**
 	 * The location of sound effect files.
 	 */
 	public static final String EFFECT_LOCATION = "sfx/";
 	
 	/**
 	 * The location of music resource files.
 	 */
 	public static final String MUSIC_LOCATION = "bgm/";
 	
 	/**
 	 * The location of sound resource files.
 	 */
	public static final String RESOURCE_LOCATION = "/yuuki/resource/audio/";
 	
 	/**
 	 * Keeps track of sound index to sound file mappings.
 	 */
 	private SoundDatabase database;
 	
 	/**
 	 * Handles sound effects.
 	 */
 	private EffectEngine effectEngine;
 	
 	/**
 	 * Handles background music.
 	 */
 	private MusicEngine musicEngine;
 	
 	/**
 	 * Creates a new SoundEngine.
 	 */
 	public SoundEngine() {
 		musicEngine = new MusicEngine();
 		effectEngine = new EffectEngine();
 		database = new SoundDatabase();
 	}
 	
 	/**
 	 * Gets the sound effect volume.
 	 * 
 	 * @return The current volume of sound effects.
 	 */
 	public int getEffectVolume() {
 		return effectEngine.getVolume();
 	}
 	
 	/**
 	 * Gets the music volume.
 	 * 
 	 * @return The current volume of music.
 	 */
 	public int getMusicVolume() {
 		return musicEngine.getVolume();
 	}
 	
 	/**
 	 * Plays the sound effect file associated with a music index. The file is
 	 * loaded if it hasn't yet been loaded, and then it is played.
 	 * 
 	 * @param musicIndex The index of the sound effect file.
 	 */
 	public void playEffect(String effectIndex) {
 		String file = database.getSound(effectIndex);
 		playEffectFile(file);
 	}
 	
 	/**
 	 * Plays the music file associated with a music index. The file is loaded
 	 * if it hasn't yet been loaded, and then it is played. If a music file is
 	 * already playing when this method is called, it is stopped.
 	 * 
 	 * @param musicIndex The index of the music file.
 	 */
 	public void playMusic(String musicIndex) {
 		playMusic(musicIndex, true);
 	}
 	
 	/**
 	 * Plays the music file associated with a music index. The file is loaded
 	 * if it hasn't yet been loaded, and then it is played. If a music file is
 	 * already playing when this method is called, it is stopped.
 	 * 
 	 * @param musicIndex The index of the music file.
 	 * @param restart Whether to restart the music track if the music is
 	 * already playing.
 	 */
 	public void playMusic(String musicIndex, boolean restart) {
 		String file = database.getSound(musicIndex);
 		playMusicFile(file, restart);
 	}
 	
 	/**
 	 * Preloads the sound effect file associated with a sound effect index. If
 	 * the sound effect file has already been loaded, this method has no
 	 * effect.
 	 * 
 	 * @param effectIndex The index of the sound effect file.
 	 */
 	public void preloadEffect(String effectIndex) {
 		String file = database.getSound(effectIndex);
 		preloadEffectFile(file);
 	}
 	
 	/**
 	 * Preloads the music file associated with a music index. If the music file
 	 * has already been loaded, this method has no effect.
 	 * 
 	 * @param musicIndex The index of the music file.
 	 */
 	public void preloadMusic(String musicIndex) {
 		String file = database.getSound(musicIndex);
 		preloadMusicFile(file);
 	}
 	
 	/**
 	 * Sets the sound effect volume.
 	 * 
 	 * @param volume The new volume for sound effects.
 	 */
 	public void setEffectVolume(int volume) {
 		effectEngine.setVolume(volume);
 	}
 	
 	/**
 	 * Sets the music volume.
 	 * 
 	 * @param volume The new volume for music.
 	 */
 	public void setMusicVolume(int volume) {
 		musicEngine.setVolume(volume);
 	}
 	
 	/**
 	 * Stops the currently playing music.
 	 */
 	public void stopMusic() {
 		musicEngine.stopSound();
 	}
 	
 	/**
 	 * Gets the package path of an effect file.
 	 * 
 	 * @param soundFile The name of the effect file.
 	 * 
 	 * @return The actual location of the file.
 	 */
 	private String getEffectLocation(String soundFile) {
 		String directory = RESOURCE_LOCATION + EFFECT_LOCATION;
 		String actualLocation = directory + soundFile;
 		return actualLocation;
 	}
 	
 	/**
 	 * Gets the package path of a music file.
 	 * 
 	 * @param musicFile The name of the music file.
 	 * 
 	 * @return The actual location of the file.
 	 */
 	private String getMusicLocation(String musicFile) {
 		String directory = RESOURCE_LOCATION + MUSIC_LOCATION;
 		String actualLocation = directory + musicFile;
 		return actualLocation;
 	}
 	
 	/**
 	 * Plays a sound effect. The file is loaded if it hasn't yet been loaded,
 	 * and then it is played.
 	 * 
 	 * @param soundFile The location of the sound effect file, relative to the
 	 * sound resource location.
 	 */
 	private void playEffectFile(String soundFile) {
 		String actualLocation = getEffectLocation(soundFile);
 		effectEngine.playSound(actualLocation);
 	}
 	
 	/**
 	 * Plays a music effect. The file is loaded if it hasn't yet been loaded,
 	 * and then it is played. If a music file is already playing when this
 	 * method is called, it is stopped.
 	 * 
 	 * @param musicFile The location of the music file, relative to the sound
 	 * resource location.
 	 * @param restart Whether to restart the music if the requested track is
 	 * already playing.
 	 */
 	private void playMusicFile(String musicFile, boolean restart) {
 		String actualLocation = getMusicLocation(musicFile);
 		musicEngine.playSound(actualLocation, restart);
 	}
 	
 	/**
 	 * Preloads a sound effect file. If the sound effect file has already been
 	 * loaded, this method has no effect.
 	 * 
 	 * @param soundFile The location of the sound effect file, relative to the
 	 * sound resource location.
 	 */
 	private void preloadEffectFile(String soundFile) {
 		String actualLocation = getEffectLocation(soundFile);
 		effectEngine.preload(actualLocation);
 	}
 	
 	/**
 	 * Preloads a music file. If the music file has already been loaded, this
 	 * method has no effect.
 	 * 
 	 * @param musicFile The location of the music file, relative to the sound
 	 * resource location.
 	 */
 	private void preloadMusicFile(String musicFile) {
 		String actualLocation = getMusicLocation(musicFile);
 		musicEngine.preload(actualLocation);
 	}
 	
 }
