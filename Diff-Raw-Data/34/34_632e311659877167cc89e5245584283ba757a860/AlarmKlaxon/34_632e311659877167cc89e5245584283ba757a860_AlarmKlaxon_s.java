 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.hlidskialf.android.alarmclock;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 
 
 /**
  * Manages alarms and vibe.  Singleton, so it can be initiated in
  * AlarmReceiver and shut down in the AlarmAlert activity
  */
 class AlarmKlaxon implements Alarms.AlarmSettings {
 
     interface KillerCallback {
         public void onKilled();
     }
 
     /** Play alarm up to 10 minutes before silencing */
     final static int ALARM_TIMEOUT_SECONDS = 10 * 60;
     final static String ICICLE_PLAYING = "IciclePlaying";
     final static String ICICLE_ALARMID = "IcicleAlarmId";
 
     private static long[] sVibratePattern = new long[] { 500, 500 };
 
     private static AlarmKlaxon sInstance;
 
     private int mAlarmId;
     private String mAlert;
     private Alarms.DaysOfWeek mDaysOfWeek;
     private boolean mVibrate;
     private int mSnooze;
     private int mDelay;
     private int mDuration;
     private boolean mVibrateOnly;
     private int mVolume;
     private int mCrescendo;
     private String mName;
     private float mCurVolume,mMaxVolume;
 
     private boolean mPlaying = false;
 
     private Vibrator mVibrator;
     private MediaPlayer mMediaPlayer;
 
     private Handler mTimeout;
     private KillerCallback mKillerCallback;
 
 
     static synchronized AlarmKlaxon getInstance(Context context) {
         if (sInstance == null) sInstance = new AlarmKlaxon(context);
         return sInstance;
     }
 
     private AlarmKlaxon(Context context) {
         mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
     }
 
     public void reportAlarm(
             int idx, boolean enabled, int hour, int minutes,
             Alarms.DaysOfWeek daysOfWeek, boolean vibrate, 
             int snooze, int duration, int delay, boolean vibrate_only , int volume, int crescendo, String name,
             String message, String alert) {
         if (Log.LOGV) Log.v("AlarmKlaxon.reportAlarm: " + idx + " " + hour +
                             " " + minutes + " dow " + daysOfWeek);
         mAlert = alert;
         mDaysOfWeek = daysOfWeek;
         mVibrate = vibrate;
         mSnooze = snooze;
         mDuration = duration;
         mDelay = delay;
         mVibrateOnly = vibrate_only;
         mVolume = volume;
         mCrescendo = crescendo;
         mName = name;
     }
 
     public int getSnooze() { return mSnooze; }
     public String getName() { return mName; }
     public int getDuration() { return mDuration; }
 
     synchronized void play(Context context, int alarmId) {
         ContentResolver contentResolver = context.getContentResolver();
 
         if (mPlaying) stop(context, false);
 
         mAlarmId = alarmId;
 
         /* this will call reportAlarm() callback */
         Alarms.getAlarm(contentResolver, this, mAlarmId);
 
         if (Log.LOGV) Log.v("AlarmKlaxon.play() " + mAlarmId + " alert " + mAlert);
 
         if (mVibrate || mVibrateOnly) {
             mVibrator.vibrate(sVibratePattern, 0);
         } else {
             mVibrator.cancel();
         }
 
         /* play audio alert */
         if (mAlert == null) {
             Log.e("Unable to play alarm: no audio file available");
         } else if (!mVibrateOnly && mVolume > 0) {
 
             /* we need a new MediaPlayer when we change media URLs */
             mMediaPlayer = new MediaPlayer();
             if (mMediaPlayer == null) {
                 Log.e("Unable to instantiate MediaPlayer");
             } else {
                 mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                         public boolean onError(MediaPlayer mp, int what, int extra) {
                             Log.e("Error occurred while playing audio.");
                             mMediaPlayer.stop();
                             mMediaPlayer.release();
                             mMediaPlayer = null;
                             return true;
                         }
                     });
 
 
                 try {
                     mMaxVolume = (float)mVolume/(float)100;
                    mCurVolume = mCrescendo > 0 ? 0 : mMaxVolume; 
                     mMediaPlayer.setDataSource(context, Uri.parse(mAlert));
                     mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                     mMediaPlayer.setVolume(mCurVolume,mCurVolume);
                     mMediaPlayer.setLooping(false);
                     mMediaPlayer.prepare();
 
                     mCrescendo *= 1000;
                    final float delta = mMaxVolume / ((float)mCrescendo / (float)(mMediaPlayer.getDuration()+mDelay));
 
                     mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                       public void onCompletion(MediaPlayer mp)
                       {
                        if (mCurVolume < mMaxVolume) {
                            mCurVolume += delta;
                            mp.setVolume(mCurVolume,mCurVolume);
                        }

                         if (mDelay > 0) {
                            Handler h = new Handler();
                             h.postDelayed(new Runnable() {
                                     public void run() { mMediaPlayer.start(); }
                                 }, mDelay);
                         }
                         else {
                             mMediaPlayer.start();
                         }
                       }
                     });
 
                 } catch (Exception ex) {
                     Log.e("Error playing alarm: " + mAlert, ex);
                     return;
                 }
                 mMediaPlayer.start();
             }
         }
         enableKiller();
         mPlaying = true;
     }
 
 
     /**
      * Stops alarm audio and disables alarm if it not snoozed and not
      * repeating
      */
     synchronized void stop(Context context, boolean snoozed) {
         if (Log.LOGV) Log.v("AlarmKlaxon.stop() " + mAlarmId);
         if (mPlaying) {
             mPlaying = false;
 
             // Stop audio playing
             if (mMediaPlayer != null) mMediaPlayer.stop();
 
             // Stop vibrator
             mVibrator.cancel();
 
             /* disable alarm only if it is not set to repeat */
             if (!snoozed && ((mDaysOfWeek == null || !mDaysOfWeek.isRepeatSet()))) {
                 Alarms.enableAlarm(context, mAlarmId, false);
             }
         }
         disableKiller();
     }
 
     /**
      * This callback called when alarm killer times out unattended
      * alarm
      */
     void setKillerCallback(KillerCallback killerCallback) {
         mKillerCallback = killerCallback;
     }
 
 
     /**
      * Called by the AlarmAlert activity on configuration change
      */
     protected void onSaveInstanceState(Bundle icicle) {
         icicle.putBoolean(ICICLE_PLAYING, mPlaying);
         icicle.putInt(ICICLE_ALARMID, mAlarmId);
     }
 
     /**
      * Restores alarm playback state on configuration change
      */
     void restoreInstanceState(Context context, Bundle icicle) {
         if (!mPlaying &&
             icicle != null &&
             icicle.containsKey(ICICLE_PLAYING) &&
             icicle.getBoolean(ICICLE_PLAYING)) {
             play(context, icicle.getInt(ICICLE_ALARMID));
         }
     }
 
     /**
      * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
      * won't run all day.
      *
      * This just cancels the audio, but leaves the notification
      * popped, so the user will know that the alarm tripped.
      */
     private void enableKiller() {
         if (mDuration == 0) return;
         mTimeout = new Handler();
         mTimeout.postDelayed(new Runnable() {
                 public void run() {
                     if (Log.LOGV) Log.v("*********** Alarm killer triggered *************");
                     if (mKillerCallback != null) mKillerCallback.onKilled();
                 }
             }, 1000 * mDuration * 60);
     }
 
     private void disableKiller() {
         if (mTimeout != null) {
             mTimeout.removeCallbacksAndMessages(null);
             mTimeout = null;
         }
     }
 
 
 }
