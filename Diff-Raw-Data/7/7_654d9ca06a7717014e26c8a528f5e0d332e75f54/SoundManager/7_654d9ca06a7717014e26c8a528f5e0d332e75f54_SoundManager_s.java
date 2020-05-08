 package sparta.workout.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import sparta.workout.application.R;
 import sparta.workout.models.Exercise;
 import sparta.workout.models.IVoiceTheme;
 import sparta.workout.models.IWorkoutListener;
 import sparta.workout.models.SoundResource;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.AsyncTask;
 
 public class SoundManager implements IWorkoutListener {
 	
 	SoundPool soundPool;
 	AudioManager audioManager;
 	Context context;
 	HashMap<Integer, SoundResource> soundIdToSoundResourceMap;
 	int SOUND_STREAM_ONE = 1;
 	
 	Boolean initialised = false;
 	
 	ArrayList<Integer> tauntPlaylist;
 	
 	IVoiceTheme theme;
 	
 	public SoundManager(AudioManager aMgr, Context contexta, IVoiceTheme voice) {
 		audioManager = aMgr;
 		context = contexta;
 		theme = voice;
 	}
 	
 	/* SOUND */
 	public void Initialise() {
 		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
 		soundIdToSoundResourceMap = new HashMap();
 		
 		ArrayList<Integer> samples = new ArrayList<Integer>();
 		int[] a = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30 };
 		
 		for (int i = 0; i < a.length; i++) {
 			samples.add(a[i]);
 		}
 		
 		samples.add(SoundResource.control_halfway);
 		samples.add(SoundResource.control_restfor);
 		samples.add(SoundResource.control_seconds);
 		samples.add(SoundResource.control_upnext);
 		samples.add(SoundResource.exercise_dumbbelllungeandrotate);
 		samples.add(SoundResource.exercise_tpushup);
 		samples.add(SoundResource.exercise_dumbbellpushpress);
 		samples.add(SoundResource.exercise_dumbbellrow);
 		samples.add(SoundResource.exercise_dumbbellswing);
 		samples.add(SoundResource.exercise_goblet);
 		samples.add(SoundResource.exercise_mountain);
 		samples.add(SoundResource.exercise_pushpositionrow);
 		samples.add(SoundResource.exercise_sidelunge);
 		samples.add(SoundResource.exercise_splitjump);
 		
 		for (Integer ii : samples) {
 			
 			int resId = theme.getSoundresourceIdFor(ii);
 			// Log.d("SOUND", "loading up sample rsource id " + resId);
 			loadUpASample(resId, SOUND_STREAM_ONE);
 		}
 		loadUpASample(R.raw.control_bell, SOUND_STREAM_ONE);
 		
 		fillTaunts();
 		loadUpATaunt();
 		
 		initialised = true;
 	}
 	
 	private void fillTaunts() {
 		
 		tauntPlaylist = theme.getShortTaunts();
 		
 		loadUpASample(theme.getCompletedTaunt(), SOUND_STREAM_ONE);
 		loadUpASample(theme.getOpeningTaunt(), SOUND_STREAM_ONE);
 		loadUpASample(theme.getQuitterTaunt(), SOUND_STREAM_ONE);
 	}
 	
 	public void playResourceInSoundPool(int resId, int priority) {
 		if (!initialised)
 			throw new RuntimeException("Not initialised", null);
 		
 		if (soundIdToSoundResourceMap.containsKey(resId)) {
 			
 			SoundResource soundId = soundIdToSoundResourceMap.get(resId);
 			
 			float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 			float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 			float leftVolume = curVolume / maxVolume;
 			float rightVolume = curVolume / maxVolume;
 			int no_loop = 0;
 			float normal_playback_rate = 1f;
 			soundPool.stop(SOUND_STREAM_ONE);
 			soundPool.play(soundId.soundPoolHandle, leftVolume, rightVolume, priority, no_loop, normal_playback_rate);
 			
 		}
 		
 	}
 	
 	public void playNumber(int i) {
 		if (!initialised)
 			throw new RuntimeException("Not initialised", null);
 		
 		int resIdFromTheme = theme.getSoundresourceIdFor(i);
 		playResourceInSoundPool(resIdFromTheme, 1);
 		
 	}
 	
 	private void loadUpASample(int resId, int streamId) {
 		
 		SoundResource sRes = new SoundResource(resId);
 		sRes.soundPoolHandle = soundPool.load(context, resId, streamId);
		sRes.duration = sRes.getDuration(context.getResources().openRawResourceFd(resId).getFileDescriptor());
 		// Log.d("SOUND", "duration for" + sRes.resourceId + " = " +
 		// sRes.duration);
 		soundIdToSoundResourceMap.put(resId, sRes);
 		
 	}
 	
 	private void unloadASample(int resId) {
 		
 		if (soundIdToSoundResourceMap.containsKey(resId)) {
 			
 			soundPool.unload(soundIdToSoundResourceMap.get(resId).soundPoolHandle);
 			
 			soundIdToSoundResourceMap.remove(resId);
 			
 		}
 	}
 	
 	public void AnnounceNextExercise(Exercise e, int restInterval) {
 		
 		int residRestfor = theme.getSoundresourceIdFor(SoundResource.control_restfor);
 		int residrestinterval = theme.getSoundresourceIdFor(restInterval);
 		int residSeconds = theme.getSoundresourceIdFor(SoundResource.control_seconds);
 		
 		SoundResource restfor = soundIdToSoundResourceMap.get(residRestfor);
 		SoundResource n = soundIdToSoundResourceMap.get(residrestinterval);
 		SoundResource secs = soundIdToSoundResourceMap.get(residSeconds);
 		
 		if (e != null) {
 			int rawExerciseSoundId = theme.getSoundresourceIdFor(SoundResource.GetExerciseSound(e.soundResourceName));
 			int residUpNext = theme.getSoundresourceIdFor(SoundResource.control_upnext);
 			
 			SoundResource upnext = soundIdToSoundResourceMap.get(residUpNext);
 			SoundResource ne = soundIdToSoundResourceMap.get(rawExerciseSoundId);
 			
 			new PlaySoundQueueAsyncTask().execute(restfor, n, secs, upnext, ne);
 		} else {
 			new PlaySoundQueueAsyncTask().execute(restfor, n, secs);
 		}
 	}
 	
 	public void AnnounceCurrentExercise(Exercise exercise) {
 		
 		// Log.d("SOUND", "Announcing current exercise: " + exercise.Name);
 		if (exercise != null) {
 			int templateSoundId = SoundResource.GetExerciseSound(exercise.soundResourceName);
 			int rawExerciseSoundId = theme.getSoundresourceIdFor(templateSoundId);
 			SoundResource ne = soundIdToSoundResourceMap.get(rawExerciseSoundId);
 			SoundResource bell = soundIdToSoundResourceMap.get(R.raw.control_bell);
 			new PlaySoundQueueAsyncTask().execute(bell, ne);
 		}
 	}
 	
 	int loadedTaunt = 0;
 	
 	public void PlayATaunt() {
 		
 		playResourceInSoundPool(loadedTaunt, 1);
 		
 		loadUpATaunt();
 	}
 	
 	private void loadUpATaunt() {
 		
 		unloadASample(loadedTaunt);
 		
 		if (tauntPlaylist.isEmpty())
 			fillTaunts();
 		
 		int i = 0;
 		
 		if (tauntPlaylist.size() > 1) {
 			Random rand = new Random();
 			i = rand.nextInt(tauntPlaylist.size() - 1);
 		}
 		
 		loadedTaunt = tauntPlaylist.remove(i);
 		
 		loadUpASample(loadedTaunt, SOUND_STREAM_ONE);
 		
 	}
 	
 	public class PlaySoundQueueAsyncTask extends AsyncTask<SoundResource, Void, Void> {
 		
 		@Override
 		protected Void doInBackground(SoundResource... resId) {
 			
 			for (SoundResource s : resId) {
 				try {
 					if (s != null) {
 						playResourceInSoundPool(s.resourceId, 1);
 						Thread.sleep(1000);
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			return null;
 		}
 	}
 	
 	private void processTimeRemaining(int secondsLeft) {
 		
 		playNumber(secondsLeft);
 	}
 	
 	public void destroy() {
		soundPool.release();
 		soundPool = null;
 	}
 	
 	// listener events
 	@Override
 	public void onTick(int secondsLeft, Boolean isResting) {
 		
 		if (secondsLeft > 11)
 			return;
 		
 		if (isResting && secondsLeft > 5)
 			return;
 		else
 			processTimeRemaining(secondsLeft);
 		
 	}
 	
 	@Override
 	public void onWorkoutFinished() {
 		// TODO Auto-generated method stub
 		int resId = theme.getCompletedTaunt();
 		playResourceInSoundPool(resId, 1);
 	}
 	
 	@Override
 	public void onWorkoutStarted() {
 		int resId = theme.getOpeningTaunt();
 		playResourceInSoundPool(resId, 1);
 	}
 	
 	@Override
 	public void onWorkoutPaused() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onExerciseStarted(Exercise exercise) {
 		AnnounceCurrentExercise(exercise);
 	}
 	
 	@Override
 	public void onHalfwayThroughExercise() {
 		int resTheme = theme.getSoundresourceIdFor(SoundResource.control_halfway);
 		playResourceInSoundPool(resTheme, 1);
 	}
 	
 	@Override
 	public void onPlayATaunt() {
 		// TODO Auto-generated method stub
 		PlayATaunt();
 		
 	}
 	
 	@Override
 	public void onRestStarted(Exercise upNext, int restInterval) {
 		
 		AnnounceNextExercise(upNext, restInterval);
 		
 	}
 	
 	public void onQuit() {
 		int resId = theme.getQuitterTaunt();
 		playResourceInSoundPool(resId, 1);
 	}
 }
