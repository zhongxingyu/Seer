 package com.acm.dijkstrasden;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Vibrator;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 
 
 /**
  * Handles all user notifications of all sorts.
  */
 public class Notification implements TextToSpeech.OnInitListener {
 	public Vibrator mVibrator;
 	public long[] vib_pattern_bump = new long[] { 0, 100, 50, 100, 50, 100 };
 	public long[] vib_pattern_atnode = new long[] { 0, 30, 30, 40 };
 	public long[] vib_pattern_leveldone = new long[] { 0, 50, 100, 50, 50, 50, 50, 50, 50, 100 };
	public long[] vib_pattern_orient = new long[] { 0, 30, 30, 40 };
 		
 	/** Sound variables */
 	public SoundPool sounds;
 	public int sBump;
 	public int sNode;
 	public int sOrient;
 	public int sMove;
 	public int stopMove;
 	public int sLevel;
 	public int sGameDone;
 	public int sLevelStart;
 	
 	/** Notify types */
 	public NotifyTypeEnum do_notify = NotifyTypeEnum.NOTIFY_NONE;
 	
 	/** TTS types */
 	private TextToSpeech mTts;
 	//private static final int MY_DATA_CHECK_CODE = 1234;
 	
 	Context myContext;
 
 	/** Initialize notification types, wav files, etc */
 	public Notification(Context context) {
 		// Set up sounds
 		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
 		sBump = sounds.load(context, R.raw.bump, 1);
 		sNode = sounds.load(context, R.raw.node, 1);
 		sOrient = sounds.load(context, R.raw.orient, 1);
 		sMove = sounds.load(context, R.raw.move, 1);
 		sLevel = sounds.load(context, R.raw.level, 1);
 		sGameDone = sounds.load(context, R.raw.gamedone, 1);
 		sLevelStart = sounds.load(context, R.raw.levelstart, 1);
 	
         // Fire off an intent to check if a TTS engine is installed
         Intent checkIntent = new Intent();
         checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         
         myContext=context;
         
         Log.d("Game", "Starting TTS act");
         
         // create the TTS instance
         mTts = new TextToSpeech(myContext, this);
 	
 	}
 	
 	/**
 	 * Provide the necessary feedback
 	 */
 	public void giveFeedback() {
 		if (do_notify != NotifyTypeEnum.NOTIFY_NONE) {
 			notifyEvent(do_notify);
 			do_notify = NotifyTypeEnum.NOTIFY_NONE;
 		}
 	}
 	
 	/** Speak the score at the end of the level */
 	public void sayScore(long l) {
 		Log.d("Game", "sayScore");
 		mTts.speak("You finished this level in " + l + " seconds!",
                 TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
                 null);
 	}
 
 	/** Generate a specific notification */
 	public void notifyEvent(NotifyTypeEnum type) {
 		switch (type) {
 		case NOTIFY_LEVELSTART:
 			mVibrator.vibrate(vib_pattern_atnode, -1);
 			sounds.play(sLevelStart, 1.0f, 1.0f, 0, 0, 1.0f);
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			break;
 			
 		case NOTIFY_BUMP:
 			mVibrator.vibrate(vib_pattern_bump, -1);
 			sounds.play(sBump, 1.0f, 1.0f, 0, 0, 1.0f);
 			try {
 				Thread.sleep(300);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			break;
 		case NOTIFY_ATNODE:
 			Log.d("Game", "Stopping movement");
 			sounds.stop(stopMove);
 			mVibrator.vibrate(vib_pattern_atnode, -1);
 			sounds.play(sNode, 1.0f, 1.0f, 0, 0, 1.0f);
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			break;
 		case NOTIFY_LEVELDONE:
 			Log.d("Game", "Level done");
 			sounds.stop(stopMove);
 			mVibrator.vibrate(vib_pattern_leveldone, -1);
 			sounds.play(sLevel, 1.0f, 1.0f, 0, 0, 1.0f);
 			try {
 				Thread.sleep(400);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			break;
 		case NOTIFY_ORIENT:
 			sounds.play(sOrient, 1.0f, 1.0f, 0, 0, 1.0f);
			mVibrator.vibrate(vib_pattern_orient, -1);
			
 			break;
 		case NOTIFY_MOVE:
 			stopMove = sounds.play(sMove, 1.0f, 1.0f, 0, -1, 1.0f);
 			break;
 		case NOTIFY_STOPSOUND:
 			sounds.stop(stopMove);
 			break;
 		case NOTIFY_GAMEDONE:
 			Log.d("Game", "Game done");
 			sounds.stop(stopMove);
 			mVibrator.vibrate(vib_pattern_leveldone, -1);
 			sounds.play(sGameDone, 1.0f, 1.0f, 0, 0, 1.0f);
 			try {
 				Thread.sleep(400);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 	
 	/** Handle the UI being paused */
 	protected void pause() {
 		notifyEvent(NotifyTypeEnum.NOTIFY_STOPSOUND);
 	    //Close the Text to Speech Library
 	    if(mTts != null) {
 	        mTts.stop();
 	        mTts.shutdown();
 	    }
 	}
 
 	public void onInit(int arg0) {		
 	}
 	
 }
