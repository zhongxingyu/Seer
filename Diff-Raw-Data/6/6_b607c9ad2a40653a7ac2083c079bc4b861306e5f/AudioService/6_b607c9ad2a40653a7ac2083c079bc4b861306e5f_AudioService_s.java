 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.audio;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 
 /**
  * Service for handling local audio playback using a MediaPlayer.
  * 
  * <p>
  * Audio played will go through {@link AudioManager#STREAM_ALARM} and played
  * until a {@link #ACTION_STOP} is sent to the service. Both single tracks and
  * playlists can be played through the service, see {@link #ACTION_PLAY_TRACK} and
  * {@link #ACTION_PLAY_PLAYLIST} for details.
  * </p>
  */
 public class AudioService extends Service implements OnPreparedListener,
 		OnErrorListener, OnCompletionListener {
 
 	private static final String TAG = "AudioService";
 
 	/**
 	 * Action for starting playback of a single audio track.
 	 * <p>Required extras:</p>
 	 * <ul>
 	 * <li>A {@link Uri}, using key defined by {@link #BUNDLE_URI} - the Uri for
 	 * the track to be played</li>
 	 * <li>A float (0-1), using key defined by {@link #BUNDLE_FLOAT_VOLUME} -
 	 * the initial volume</li>
 	 * </ul>
 	 */
 	public static final String ACTION_PLAY_TRACK = "se.chalmers.dat255.sleepfighter.audio.AudioService.PLAY_TRACK";
 
 	/**
 	 * Action for stopping any playback.
 	 */
 	public static final String ACTION_STOP = "se.chalmers.dat255.sleepfighter.audio.AudioService.STOP";
 
 	/**
 	 * Action for modifying the volume of what's currently playing. 
 	 * <p>Required extras:</p>
 	 * <ul>
 	 * <li>A float, using key defined by {@link #BUNDLE_FLOAT_VOLUME} - the
 	 * volume (0-1) for the playing audio</li>
 	 * </ul>
 	 */
 	public static final String ACTION_VOLUME = "se.chalmers.dat255.sleepfighter.audio.AudioService.VOLUME";
 
 	/**
 	 * Action for starting playback of a playlist.
 	 * <p>Required extras:</p>
 	 * <ul>
 	 * <li>A {@code String[]}, using key defined by {@link #BUNDLE_PLAYLIST} -
 	 * paths to the tracks to be played</li>
 	 * <li>A float (0-1), using key defined by {@link #BUNDLE_FLOAT_VOLUME} -
 	 * the initial volume</li>
 	 * </ul>
 	 */
 	public static final String ACTION_PLAY_PLAYLIST = "se.chalmers.dat255.sleepfighter.audio.AudioService.PLAY_PLAYLIST";
 
 	public static final String BUNDLE_URI = "audio_uri";
 	public static final String BUNDLE_FLOAT_VOLUME = "audio_volume";
 	public static final String BUNDLE_PLAYLIST = "audio_playlist";
 
 	/**
 	 * The states the MediaPlayer can be in.
 	 */
 	private enum State {
 		PREPARING,
 		PLAYING,
 		STOPPED
 	}
 
 	/**
 	 * The available sources this can play from.
 	 */
 	private enum SourceType {
 		SINGLE,
 		PLAYLIST
 	}
 
 	private MediaPlayer player;
 	private State state;
 	private SourceType sourceType;
 
 	// Variables only relevant when source is playlist
 	// Might be better if handled elsewhere
 	private String[] tracks;
 	private int track;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		this.player = new MediaPlayer();
 		this.player.setAudioStreamType(AudioManager.STREAM_ALARM);
 		this.player.setOnPreparedListener(this);
 		this.player.setOnErrorListener(this);
 		this.player.setOnCompletionListener(this);
 
 		// Makes MediaPlayer hold a wake lock while playing
 		this.player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
 		
 		this.state = State.STOPPED;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		String action = intent.getAction();
 		Log.d(TAG, "start action " + action);
 
 		if (ACTION_PLAY_TRACK.equals(action)) {
 			handleVolumeAction(intent);
 			handlePlayAction(intent);
 		} else if (ACTION_STOP.equals(action)) {
 			handleStopAction();
 		} else if (ACTION_VOLUME.equals(action)) {
 			handleVolumeAction(intent);
 		} else if (ACTION_PLAY_PLAYLIST.equals(action)) {
 			handleVolumeAction(intent);
 			handlePlaylistPlay(intent);
 		}
 		return START_NOT_STICKY;
 	}
 
 	private void handlePlayAction(Intent intent) {
 		Object o = intent.getParcelableExtra(BUNDLE_URI);
 		if (!(o instanceof Uri)) {
 			throw new IllegalArgumentException(
 					"No Uri bundled with PLAY action");
 		}
 		Uri uri = (Uri) o;
 
 		// Stop if previously playing
 		if (this.state != State.STOPPED) {
 			stopPlayback();
 		}
 
 		setSourceType(SourceType.SINGLE);
 
 		// Loop if only one track
 		this.player.setLooping(true);
 		try {
 			this.player.setDataSource(this, uri);
 		} catch (Exception e) {
 			handleException(e);
 			return;
 		}
 		prepareAndPlay();
 	}
 
 	private void handlePlaylistPlay(Intent intent) {
		Object o = intent.getStringArrayExtra(BUNDLE_PLAYLIST);
		if (!(o instanceof String[])) {
 			throw new IllegalArgumentException(
 					"No String[] bundled with PLAY_PLAYLIST action");
 		}
		String[] tracks = (String[]) o;
 		if (tracks.length == 0) {
 			throw new IllegalArgumentException(
 					"Empty String[] bundled with PLAY_PLAYLIST action");
 		}
 
 		// Stop if previously playing
 		if (this.state != State.STOPPED) {
 			stopPlayback();
 		}
 
 		setSourceType(SourceType.PLAYLIST);
 		this.tracks = tracks;
 
 		// Don't let player loop, handle ourselves in onCompletion
 		this.player.setLooping(false);
 
 		setPlaylistDataSource();
 		prepareAndPlay();
 	}
 
 	private void handleStopAction() {
 		stopPlayback();
 		stopSelf();
 	}
 
 	private void handleVolumeAction(Intent intent) {
 		float volume = intent.getFloatExtra(BUNDLE_FLOAT_VOLUME, -1f);
 		setVolume(volume);
 	}
 
 	/**
 	 * Prepares playback of the data source set on MediaPlayer asynchronously
 	 * and starts playback when prepared.
 	 */
 	private void prepareAndPlay() {
 		try {
 			this.player.prepareAsync();
 			this.state = State.PREPARING;
 		} catch (Exception e) {
 			handleException(e);
 		}
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		// Don't start if state no longer is preparing
 		if (this.state == State.PREPARING) {
 			mp.start();
 			this.state = State.PLAYING;
 		}
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		this.state = State.STOPPED;
 		mp.reset();
 		if (this.sourceType == SourceType.PLAYLIST) {
 			incTrack();
 			prepareAndPlay();
 		} else {
 			throw new RuntimeException("Non playlist complete");
 		}
 	}
 
 	/**
 	 * Increases the playlist track.
 	 */
 	private void incTrack() {
 		if (this.sourceType != SourceType.PLAYLIST) {
 			throw new RuntimeException(
 					"Non playlist source type can not use incTrack");
 		}
 		// Increase track by one and wrap around to first if at last
 		this.track = (this.track + 1) % this.tracks.length;
 		setPlaylistDataSource();
 	}
 
 	/**
 	 * Sets the data source of the player to the current track in the playlist,
 	 * defined by {@link #track}.
 	 */
 	private void setPlaylistDataSource() {
 		try {
 			this.player.setDataSource(this.tracks[this.track]);
 		} catch (Exception e) {
 			handleException(e);
 		}
 	}
 
 	/**
 	 * Stops any ongoing audio playback.
 	 */
 	private void stopPlayback() {
 		if (this.state == State.PLAYING) {
 			this.player.stop();
 		}
 		this.player.reset();
 
 		if (this.sourceType == SourceType.PLAYLIST) {
 			// Reset playlist-only variables
 			this.tracks = null;
 			this.track = 0;
 		}
 
 		this.state = State.STOPPED;
 
 		setSourceType(null);
 	}
 
 	/**
 	 * Sets the source type for the player.
 	 * 
 	 * @param sourceType
 	 *            the source type
 	 */
 	private void setSourceType(SourceType sourceType) {
 		this.sourceType = sourceType;
 	}
 
 	/**
 	 * Sets the volume for the audio played through the MediaPlayer.
 	 * 
 	 * @param volume
 	 *            the volume as a float 0-1
 	 */
 	private void setVolume(float volume) {
 		if (volume > 1 || volume < 0) {
 			throw new IllegalArgumentException("No valid volume bundled");
 		}
 
 		this.player.setVolume(volume, volume);
 	}
 
 	@Override
 	public void onDestroy() {
 		stopPlayback();
 		this.player.release();
 		super.onDestroy();
 	}
 
 	/**
 	 * Handle an exception by stopping and logging.
 	 * 
 	 * @param exception
 	 *            the exception
 	 */
 	private void handleException(Exception exception) {
 		stopPlayback();
 		Log.e(TAG, "AudioService exception", exception);
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		stopPlayback();
 		Log.e(TAG, "MediaPlayer error = (" + what + ", " + extra + ")");
 		return true;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
