 package com.eggwall.BabyMusic;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 
 import java.io.IOException;
 
 /**
  * Runs the music in the background and holds a wake lock during the duration of music playing.
  */
 public class AudioService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
     private MediaPlayer mPlayer;
     private static final int PLAYING_NOTHING = 0;
     private static final int MUSIC = 1;
     private static final int WHITE_NOISE = 2;
     /** Set to MUSIC or WHITE_NOISE */
     private int mTypePlaying = 0;
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra) {
         if (mPlayer != null) {
             mPlayer.stop();
             mPlayer.release();
             mPlayer = null;
         }
         Log.e("AudioService", "BabyMusic.AudioService encountered onError");
         // Propagate the error up.
         return false;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         startPlayingResource(0, MUSIC);
         return 0;
     }
 
     /**
      * Start playing the resource specified here.
      * @param id A resource like R.raw.music_file
      * @param type Either MUSIC, or WHITE_NOISE. Passing the same ID twice
      *             is a signal to stop playing music altogether.
      */
     private void startPlayingResource(int id, int type) {
         releasePlayer();
         // If the user hits the same button twice, just stop playing anything.
         if (mTypePlaying != type) {
             mTypePlaying = type;
             mPlayer = new MediaPlayer();
             // Keep the CPU awake while playing music.
             mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
             mPlayer.setOnPreparedListener(this);
             AssetFileDescriptor d = getApplicationContext().getResources().openRawResourceFd(R.raw.how_deep_is_the_ocean);
             try {
                 mPlayer.setDataSource(d.getFileDescriptor());
                 d.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             mPlayer.prepareAsync();
             mPlayer.setLooping(true);
         } else {
            if (mPlayer != null) {
                mPlayer.stop();
            }
             mTypePlaying = PLAYING_NOTHING;
         }
     }
 
     private void setForegroundService() {
         PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                 new Intent(getApplicationContext(), BabyActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
         Notification notification = new Notification();
         notification.tickerText = "Baby Music";
         notification.icon = R.drawable.ic_launcher;
         notification.flags |= Notification.FLAG_ONGOING_EVENT;
         notification.setLatestEventInfo(getApplicationContext(), "BabyMusic", "Playing", pi);
     }
 
     private void releasePlayer() {
         if (mPlayer != null) {
             mPlayer.stop();
             mPlayer.release();
             mPlayer = null;
         }
     }
 
     @Override
     public void onDestroy() {
         releasePlayer();
         super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         setForegroundService();
         mPlayer.start();
     }
 }
