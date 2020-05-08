 /**
  *   Copyright 2012 Francesco Balducci
  *
  *   This file is part of FakeDawn.
  *
  *   FakeDawn is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   FakeDawn is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.balau.fakedawn;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.util.Log;
 
 /**
  * @author francesco
  *
  */
 public class DawnSound extends Service implements OnPreparedListener, OnCompletionListener, OnErrorListener {
 
 	public static final String EXTRA_SOUND_URI = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_URI";
 	public static final String EXTRA_SOUND_MILLIS = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_MILLIS";
 	public static final String EXTRA_SOUND_VOLUME = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_VOLUME";
 	public static final String EXTRA_VIBRATE = "org.balau.fakedawn.DawnSound.EXTRA_VIBRATE";
	
 	private Timer m_timer = null;
 	private long m_soundStartMillis;
 	private MediaPlayer m_player = new MediaPlayer();
 	private boolean m_soundInitialized = false;
	
 	private Vibrator m_vibrator = null;
 	private boolean m_vibrate = false;
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(m_soundInitialized)
 		{
 			m_soundInitialized = false;
 			if(m_player.isPlaying())
 			{
 				m_player.stop();
 			}
 		}
 		if(m_timer != null)
 			m_timer.cancel();
 		if(m_vibrate)
 		{
 			m_vibrate = false;
 			m_vibrator.cancel();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
				
 		if(!m_soundInitialized)
 		{
			m_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			
 			m_player.setOnPreparedListener(this);
 			m_player.setOnCompletionListener(this);
 			m_player.setOnErrorListener(this);
 			m_player.setAudioStreamType(AudioManager.STREAM_ALARM);
 			m_player.reset();
			
 			m_soundStartMillis = intent.getLongExtra(EXTRA_SOUND_MILLIS, 0);
 
 			String sound = intent.getStringExtra(EXTRA_SOUND_URI);
 			if(sound.isEmpty())
 			{
 				Log.d("FakeDawn", "Silent.");
 			}
 			else
 			{
 				Uri soundUri = Uri.parse(sound);
 
 				if(soundUri != null)
 				{
 					AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
 					int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM); 
 					int volume = intent.getIntExtra(EXTRA_SOUND_VOLUME, maxVolume/2); 
 					if(volume < 0) volume = 0;
 					if(volume > maxVolume) volume = maxVolume;
 					am.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
 					try {
 						m_player.setDataSource(this, soundUri);
 						m_soundInitialized = true;
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					} catch (SecurityException e) {
 						e.printStackTrace();
 					} catch (IllegalStateException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 
				if(m_vibrator != null)
 				{
					m_vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, false);
 				}
 
 				m_timer = new Timer();
 				m_timer.schedule(
 						new TimerTask() {
 
 							@Override
 							public void run() {
 								if(m_soundInitialized)
 								{
 									if(!m_player.isPlaying())
 									{
 										m_player.prepareAsync();
 									}
 								}
 								if(m_vibrate)
 								{
									m_vibrator.vibrate(
											new long[] {0, 1000},
											Integer.MAX_VALUE);
 								}
 							}
 						}, new Date(m_soundStartMillis));
 				Log.d("FakeDawn", "Sound scheduled.");
 			}
 		}
 		return START_STICKY;
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		m_player.setLooping(true);
 		m_player.start();
 	}
	
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.e("FakeDawn", String.format("MediaPlayer error. what: %d, extra: %d", what, extra));
 		m_player.reset();
 		m_soundInitialized = false;
 		return true;
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		Log.w("FakeDawn", "Sound completed even if looping.");
 		m_player.stop();
 	}	
 }
