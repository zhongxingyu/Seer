 package com.shingrus.myplayer;
 
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.MediaController.MediaPlayerControl;
 import android.media.*;
 
 
 
 public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
 		MediaPlayer.OnCompletionListener {
 
 	
 	private static final int NOTIFICATION_ID = 100500;
 	MediaPlayer mp;
 	TrackList trackList;
 //	MusicTrack currentTrack;
 	String currentTitle;
 	private final IBinder mBinder = new LocalBinder();
 	boolean isPaused = false;
 	NotificationManager nm;
 	Notification notification;
 	
 	public class LocalBinder extends Binder {
 		MusicPlayerService getService() {
 			return MusicPlayerService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		mp = new MediaPlayer();
 		currentTitle = "";
 		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		trackList = TrackList.getInstance();
 		nm  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		super.onCreate();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d("shingrus", "Destroy: MusicPlayerService");
 		if (mp != null) {
 			nm.cancel(NOTIFICATION_ID);
 			if (mp.isPlaying())
 				mp.stop();
 			mp.release();
 		}
 		
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		nm.cancel(NOTIFICATION_ID);
 		return Service.START_STICKY;
 	}
 
 	/**
 	 * Method from OnPreparedListener
 	 */
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		mp.start();
 		isPaused=false;
		notification = new Notification(R.drawable.ringtone,currentTitle,System.currentTimeMillis());
 		Intent i = new Intent(this, MyPlayerActivity.class);
 		//i.setFlags(Intent.FLAG_ACTIVITY_SIN GLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent pi  = PendingIntent.getActivity(this,0, i, 0);
 		notification.setLatestEventInfo(this, currentTitle, "Playing", pi);
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		nm.notify(NOTIFICATION_ID, notification);
 	}
 
 	/**
 	 * Method from OnErrorListener
 	 */
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.i("shingrus", "MP error: " + what);
 		return false;
 	}
 
 	private void playMusic(MusicTrack mt) {
 		if (mt != null && mt.filename.length() > 0) {
 			mp.reset();
 			nm.cancel(NOTIFICATION_ID);
 			isPaused = false;
 			try {
 				mp.setDataSource(mt.filename);
 				currentTitle = mt.getTitle();
 				mp.setOnPreparedListener(this);
 				mp.setOnCompletionListener(this);
 				mp.prepareAsync();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else if (mt == null)
 			Log.d("shingrus", "MusicPlayerService: mt is null");
 	}
 
 	/**
 	 * Method from OnCompletionListener
 	 */
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		playNext();
 	}
 
 	// my methods
 
 	public void startPlayFrom(int position) {
 		playMusic(trackList.startIterateFrom(position));
 
 	}
 
 	public void playNext() {
 		MusicTrack mt = trackList.getNextTrack();
 		playMusic(mt);
 
 	}
 
 	public void playPause() {
 		if(mp.isPlaying()){
 			mp.pause();
 			isPaused = true;
 		}
 		else if (isPaused){
 			mp.start();
 			isPaused=false;
 		}
 	}
 	
 	public void stopMusic() {
 		mp.stop();
 		nm.cancel(NOTIFICATION_ID);
 		isPaused = false;
 	}
 
 }
