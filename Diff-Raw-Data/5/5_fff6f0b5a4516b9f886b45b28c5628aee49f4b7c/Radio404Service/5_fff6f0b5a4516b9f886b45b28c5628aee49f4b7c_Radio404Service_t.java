 package com.geoke.error404;
 
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.IBinder;
 
 public class Radio404Service extends Service implements OnPreparedListener {
 
 	// audio stream
 	private final String f = "http://radio404.org:8000";
 	// instance of the MediaPlayer
 	MediaPlayer mp = new MediaPlayer();
 	// indicator
 	private Boolean isPlaying = false;
 	private Notification myNotification;
 	
 	// references to strings for notification
 	private int idInfo;
 	private int idTitleInfo;
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		startPlayer();
 		return START_NOT_STICKY;
 	}
 
 	private void startPlayer() {
 		idInfo = R.string.not_playing;
 		idTitleInfo = R.string.title_not_playing;
 
 		try {
 			mp.setDataSource(f);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			mp.prepare();
			idInfo = R.string.now_playing;
			idTitleInfo = R.string.title_now_playing;
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {			
 			e.printStackTrace();
 		}
 		mp.setOnPreparedListener(this);
 		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
 
 		myNotification = new Notification(android.R.drawable.ic_dialog_info,
 				getString(idTitleInfo),
 				System.currentTimeMillis());
 
 		Intent i = new Intent(this, Radio404Player.class);
 
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
 				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
 		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
 		
 		myNotification.setLatestEventInfo(this, "Radio404",
 				getString(idInfo),
 				pi);
 		myNotification.flags|=Notification.FLAG_NO_CLEAR;
 
 		startForeground(R.string.app_name, myNotification);
 
 	}
 
 	private void stopPlayer() {
 		if (mp != null)
 			mp.stop();
 		isPlaying = false;
 		stopForeground(true);
 	}
 
 	@Override
 	public void onDestroy() {
 		stopPlayer();
 		super.onDestroy();
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void onPrepared(MediaPlayer mp) {
 		startAudio();
 	}
 
 	private void startAudio() {
 		mp.start();
 	}
 
 }
