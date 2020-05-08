 package be.fbousson.voicecontroller;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ToggleButton;
 import be.fbousson.voicecontroller.recorder.ExtAudioRecorder;
 
 public class SoundRecorderActivity extends Activity {
 
 	private static final String TAG = SoundRecorderActivity.class.getName();
 
 	private ToggleButton recordStartSoundButton;
 	private Button playStartSoundButton;
 	private ExtAudioRecorder audioRecorder;
 	private static final String STARTSOUND_FILENAME = "startSound.wav";
 
 	private SoundPool soundPool;
 	private int soundID;
 	boolean loaded = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.soundrecorder);
 		initControls();
 		initBehaviour();
 		audioRecorder = ExtAudioRecorder.getInstanse(false);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		// Load the sound
 		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
 		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
 			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
 				loaded = true;
 				playStartSoundButton.setEnabled(true);
 				Log.d(TAG, "Loading of sound completed with sampleId " + sampleId + " and status " + status);
 				soundID = sampleId;
 
 			}
 		});
 		if (fileExists(getStartSoundFileName())) {
 			soundID = soundPool.load(getStartSoundFileName(), 1);
 			Log.d(TAG, "loading directly : " + soundID);
 		}
 	}
 
 	private void initBehaviour() {
 		recordStartSoundButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked) {
 					startRecording();
 				} else {
 					stopRecording();
 				}
 			}
 
 		});
 
 		playStartSoundButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Log.d(TAG, "playStartSoundButton clicked");
 				if (loaded) {
 					playSound(soundID);
 				}
 
 			}
 
 		});
 
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		audioRecorder.release();
 	}
 
 	private void playSound(int soundID) {
 		// Getting the user sound settings
 		Log.d(TAG, "started playing sound " + soundID);
 		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
 		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		soundPool.play(soundID, volume, volume, 1, 0, 1f);
 
 	}
 
 	private void initControls() {
 		recordStartSoundButton = (ToggleButton) findViewById(R.id.recordStartSoundButton);
 		playStartSoundButton = (Button) findViewById(R.id.playStartSoundButton);
 
 	}
 
 	public File getSoundFilesCacheDir() {
 		if (android.os.Build.VERSION.SDK_INT >= 8) {
 			File externalCacheDir = getExternalCacheDir();
 			if (externalCacheDir != null && externalCacheDir.exists()) {
 				return externalCacheDir;
 			} else {
 				return getCacheDir();
 			}
 		} else {
 			return getCacheDir();
 		}
 
 	}
 
 	private void startRecording() {
 		Log.d(TAG, "start recording");
 		playStartSoundButton.setEnabled(false);
 		if (loaded) {
 			boolean succesfullyUnloaded = soundPool.unload(soundID);
 			Log.d(TAG, "succesfullyUnloaded " + succesfullyUnloaded);
 		}
 		deleteIfFileExists(getStartSoundFileName());
 		logState();
 		audioRecorder.setOutputFile(getStartSoundFileName());
 		audioRecorder.prepare();
 		audioRecorder.start();
 	}
 
 	private boolean fileExists(String fileName) {
 		return new File(fileName).exists();
 	}
 
 	private void deleteIfFileExists(String startSoundFileName) {
 		File file = new File(startSoundFileName);
 		if (file.exists()) {
 			file.delete();
 		}
 	}
 
 	public String getStartSoundFileName() {
 		File cacheFolder = getSoundFilesCacheDir();
 		String startSoundFile = cacheFolder.getAbsolutePath() + File.separator + STARTSOUND_FILENAME;
 		Log.d(TAG, startSoundFile);
 		return startSoundFile;
 	}
 
 	private void stopRecording() {
 		Log.d(TAG, "stop recording");
 		logState();
 		audioRecorder.reset();
 		logState();
 		// audioRecorder.reset();
 		// soundPool.unload(soundID);
 		loaded = false;
 		soundID = soundPool.load(getStartSoundFileName(), 1);
 	}
 
 	private void logState() {
 		Log.d(TAG, "audio recorder state " + audioRecorder.getState());
 
 	}
 
 }
