 package com.sonrlabs.test.sonr;
 
 //import org.acra.ErrorReporter;
 
 //import com.flurry.android.FlurryAgent;
 import com.sonrlabs.test.sonr.common.Common;
 import com.sonrlabs.test.sonr.signal.SpuriousSignalException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.KeyEvent;
 
 class UserActionHandler
       implements IUserActionHandler {
    
    private static final String TAG = "SONR audio processor";
 
    
    // SONR commands ******************
    private static final int PLAY_PAUSE = 0x1e;
    private static final int FAST_FORWARD = -2;
    private static final int REWIND = -3;
    private static final int NEXT_TRACK = 0x1d;
    private static final int PREVIOUS_TRACK = 0x21;
    private static final int VOLUME_UP = 0x17;
    private static final int VOLUME_DOWN = 0x18;
    private static final int MUTE = 0x1b;
    private static final int THUMBS_UP = 0x9;
    private static final int THUMBS_DOWN = 0xa;
    private static final int FAVORITE = 0x6;
    private static final int UP = 0xc;
    private static final int DOWN = 0xf;
    private static final int LEFT = 0x11;
    private static final int RIGHT = 0x12;
    private static final int SELECT = 0x14;
    private static final int POWER_ON = 0x1;
    private static final int POWER_OFF = 0x5;
    private static final int SONR_HOME = 0x22;
    private static final int SEARCH = 0x24;
    // end SONR commands
    // ****************************************************************************************************************
 
    // for button repeats, in  milliseconds
    private static final int REPEAT_TIME = 500; 
    private static final int SKIP_TIME = 300;
    private static final int BACK_TIME = 300;
    private static final int VOL_TIME = 100;
 
    private final AudioManager manager;
    private final Context context;
    
    private long lastPlayTime = 0;
    private long lastMuteTime = 0;
    private long lastSkipTime = 0;
    private long lastVolumeTime = 0;
    private long lastBackTime = 0;
    private int volume = -1;
    private boolean muted = false;
    
    UserActionHandler(AudioManager manager, Context ctx) {
       this.manager = manager;
       this.context = ctx;
    }
 
    @Override
    public void processAction(int receivedByte)
          throws SpuriousSignalException {
       try {
          processUserCommand(receivedByte);
       } catch (RuntimeException e) {
          e.printStackTrace();
          //ErrorReporter.getInstance().handleException(e);
       }
    }
 
    private void processUserCommand(int receivedByte)
          throws SpuriousSignalException {
       checkAutoUnmute(receivedByte);
       int key = Integer.MIN_VALUE;
       switch (receivedByte) {
          case PLAY_PAUSE:
             if (lastPlayTime < SystemClock.elapsedRealtime() - REPEAT_TIME) {
                key = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                lastPlayTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "PLAY");
                //FlurryAgent.logEvent("PLAY_PRESSED");
             }
             break;
          case FAST_FORWARD:
             key = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
             Log.d(TAG, "FAST_FORWARD");
             //FlurryAgent.logEvent("FAST_FORWARD_PRESSED");
             break;
          case REWIND:
             key = KeyEvent.KEYCODE_MEDIA_REWIND;
             Log.d(TAG, "REWIND");
             //FlurryAgent.logEvent("REWIND_PRESSED");
             break;
          case NEXT_TRACK:
             if (lastSkipTime < SystemClock.elapsedRealtime() - SKIP_TIME) {
                key = KeyEvent.KEYCODE_MEDIA_NEXT;
                lastSkipTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "NEXT_TRACK");
                //FlurryAgent.logEvent("NEXT_TRACK_PRESSED");
             }
             break;
          case PREVIOUS_TRACK:
             if (lastBackTime < SystemClock.elapsedRealtime() - BACK_TIME) {
                key = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                lastBackTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "PREVIOUS_TRACK");
                //FlurryAgent.logEvent("PREVIOUS_TRACK_PRESSED");
             }
             break;
          case VOLUME_UP:
             if (lastVolumeTime < SystemClock.elapsedRealtime() - VOL_TIME) {
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                                                   AudioManager.FLAG_SHOW_UI);
                lastVolumeTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "VOLUME_UP");
                //FlurryAgent.logEvent("VOLUME_UP_PRESSED");
             }
             break;
          case VOLUME_DOWN:
             if (lastVolumeTime < SystemClock.elapsedRealtime() - VOL_TIME) {
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                                                   AudioManager.FLAG_SHOW_UI);
                lastVolumeTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "VOLUME_DOWN");
                //FlurryAgent.logEvent("VOLUME_DOWN_PRESSED");
             }
             break;
          case MUTE:
             if (lastMuteTime < SystemClock.elapsedRealtime() - REPEAT_TIME) {
                if (muted) {
                   volume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
                   manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                   muted = false;
                } else {
                   // volume = theAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                   volume = 0;
                   manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                   muted = true;
                }
                lastMuteTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "MUTE");
                //FlurryAgent.logEvent("MUTE_PRESSED");
             }
             break;
          case THUMBS_UP:
             Log.d(TAG, "THUMBS_UP");
             //FlurryAgent.logEvent("THUMBS_UP_PRESSED");
             break;
          case THUMBS_DOWN:
             Log.d(TAG, "THUMBS_DOWN");
             //FlurryAgent.logEvent("THUMBS_DOWN_PRESSED");
             break;
          case FAVORITE:
             Log.d(TAG, "FAVORITE");
             //FlurryAgent.logEvent("FAVORITE_PRESSED");
             break;
          case UP:
             key = KeyEvent.KEYCODE_DPAD_UP;
             Log.d(TAG, "UP");
             //FlurryAgent.logEvent("KEYCODE_DPAD_UP_PRESSED");
             break;
          case DOWN:
             key = KeyEvent.KEYCODE_DPAD_DOWN;
             Log.d(TAG, "DOWN");
             //FlurryAgent.logEvent("KEYCODE_DPAD_DOWN_PRESSED");
             break;
          case LEFT:
             key = KeyEvent.KEYCODE_DPAD_LEFT;
             Log.d(TAG, "LEFT");
             //FlurryAgent.logEvent("KEYCODE_DPAD_LEFT_PRESSED");
             break;
          case RIGHT:
             key = KeyEvent.KEYCODE_DPAD_RIGHT;
             Log.d(TAG, "RIGHT");
             //FlurryAgent.logEvent("KEYCODE_DPAD_RIGHT_PRESSED");
             break;
          case SELECT:
             key = KeyEvent.KEYCODE_DPAD_CENTER;
             Log.d(TAG, "SELECT");
             //FlurryAgent.logEvent("KEYCODE_DPAD_CENTER_PRESSED");
             break;
          case POWER_ON:
             Log.d(TAG, "POWER_ON");
             //FlurryAgent.logEvent("POWER_ON_PRESSED");
             break;
          case POWER_OFF:
             Log.d(TAG, "POWER_OFF");
             //FlurryAgent.logEvent("POWER_OFF_PRESSED");
             break;
          case SONR_HOME:
             Log.d(TAG, "HOME");
             //FlurryAgent.logEvent("HOME_PRESSED");
             Intent i = new Intent(context, SONR.class);
             Log.d("CONTEXT", context.getPackageName());
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             context.getApplicationContext().startActivity(i);
             break;
          case SEARCH:
             Log.d(TAG, "SEARCH");
             break;
             
          case 0:
             // consider this a no-op for now.
             break;
             
          default:
             throw new SpuriousSignalException(receivedByte);
       }
 
       if (key != Integer.MIN_VALUE) {
          sendbroadcast(key);
       }
    }
 
    /*
     * If currently muted, unmute on any action other mute itself, but leave the
     * volume at 0.
     */
    private void checkAutoUnmute(int receivedByte) {
       if (muted) {
          if (receivedByte != MUTE) {
             volume = 0;
             muted = false;
          }
       }
    }
 
    private void sendbroadcast(int keyEvent) {
      
       synchronized (this) {
          Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);     
          String selectedMediaPlayer =  Common.get(context, SONR.APP_PACKAGE_NAME, null);
 
          if (selectedMediaPlayer != null) {
             Log.d("BROADCAST PLAYER", selectedMediaPlayer);
             i.setPackage(selectedMediaPlayer);
 
             i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
            context.sendOrderedBroadcast(i, null);
 
             i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyEvent));
            context.sendOrderedBroadcast(i, null);
 
          }
       }
       
    }
 }
