 package com.michalkazior.simplemusicplayer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.provider.MediaStore;
 import android.telephony.TelephonyManager;
 import android.widget.Toast;
 
 /**
  * Player backend.
  * 
  * It manages the playlist, handles operations, sends events to UI client(s).
  * 
  * @author kazik
  */
 public class Player extends Service {
 	enum Event {
 		EnqueuedSongsChanged, StateChanged,
 	};
 
 	/**
 	 * Proxy Binder class for direct remote communication with Player class.
 	 */
 	public class Proxy extends Binder {
 		Player getPlayer() {
 			return Player.this;
 		}
 	};
 
 	/**
 	 * Possible Player states.
 	 */
 	public enum State {
 		IS_STOPPED, IS_PLAYING, IS_PAUSED, IS_ON_HOLD_BY_HEADSET, IS_ON_HOLD_BY_CALL,
 	};
 
 	private ArrayList<Song> enqueuedSongs = new ArrayList<Song>();
 	private MediaPlayer mp = null;
 	private State state = State.IS_STOPPED;
 	private Song playing = null;
 	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return new Proxy();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return START_STICKY;
 	}
 
 	public synchronized void registerHandler(Messenger m) {
 		clients.add(m);
 	}
 
 	public synchronized Song getPlaying() {
 		return playing;
 	}
 
 	public synchronized void setPlaying(Song song) {
 		playing = song;
 	}
 
 	public synchronized State getState() {
 		return state;
 	}
 
 	private synchronized void setState(State s) {
 		state = s;
 		emit(Event.StateChanged);
 	}
 
 	/**
 	 * Get current song duration in msecs.
 	 */
 	public synchronized int getDuration() {
 		switch (state) {
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PLAYING:
 			case IS_PAUSED:
 				return mp.getDuration();
 			case IS_STOPPED:
 				return 0;
 		}
 		return 0;
 	}
 
 	/**
 	 * Get current song position in msecs.
 	 */
 	public synchronized int getPosition() {
 		switch (state) {
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PLAYING:
 			case IS_PAUSED:
 				return mp.getCurrentPosition();
 			case IS_STOPPED:
 				return 0;
 		}
 		return 0;
 	}
 
 	/**
 	 * Get a list for currently enqueued songs.
 	 * 
 	 * @return
 	 */
 	public synchronized Song[] getEnqueuedSongs() {
 		return enqueuedSongs.toArray(new Song[] {});
 	}
 
 	/**
 	 * Get a list of all available songs.
 	 * 
 	 * 
 	 * The function returns a list of songs stored in the media database from an
 	 * external storage (i.e. memory card).
 	 */
 	public synchronized Song[] getAllSongs() {
 		/*
 		 * Doing a query() would result in a fatal error when external storage
 		 * is missing.
 		 * 
 		 * So instead, return an empty list when external storage isn't present.
 		 */
 		if (!isExternalStorageMounted()) {
 			Toast.makeText(this, R.string.msg_err_notmounted, Toast.LENGTH_LONG).show();
 			return new Song[] {};
 		}
 
 		/*
 		 * Fixme: Should a Song have more info?
 		 */
 		ArrayList<Song> list = new ArrayList<Song>();
 		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
 		String[] columns = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME };
 		Cursor c = getContentResolver().query(uri, columns, null, null,
 				MediaStore.Audio.Media.DATA + " ASC");
 
 		int dataIndex = c.getColumnIndex(MediaStore.Audio.Media.DATA);
 		int displayIndex = c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
 
 		if (c.moveToFirst()) {
 			do {
 				list.add(new Song(c.getString(dataIndex), c.getString(displayIndex)));
 			} while (c.moveToNext());
 		}
 		c.close();
 
 		return list.toArray(new Song[] {});
 	}
 
 	/**
 	 * Enqueue a song at a given index.
 	 * 
 	 * @param song
 	 * @param index
 	 *            value less than 0 appends
 	 */
 	public synchronized void enqueueSong(Song song, int index) {
 		if (index >= 0) {
 			if (index > enqueuedSongs.size()) index = 0;
 			enqueuedSongs.add(index, song);
 		}
 		else {
 			enqueuedSongs.add(song);
 		}
 
 		emit(Event.EnqueuedSongsChanged);
 
 		/*
 		 * Start the playback after adding a first song.
 		 */
 		if (enqueuedSongs.size() == 1) {
 			play();
 		}
 	}
 
 	/**
 	 * Move a song by an offset.
 	 * 
 	 * Moving an only song yields no effect.
 	 * 
 	 * Moving a song beyond enqueued songs list is done by cutting offset to
 	 * list size accordingly.
 	 * 
 	 * @param song
 	 * @param offset
 	 */
 	public synchronized void moveSong(Song song, int offset) {
 		int index = enqueuedSongs.indexOf(song) + offset;
 
 		if (index < 0) index = 0;
 		if (index >= enqueuedSongs.size()) index = enqueuedSongs.size() - 1;
 
 		enqueuedSongs.remove(song);
 		enqueuedSongs.add(index, song);
 		emit(Event.EnqueuedSongsChanged);
 	}
 
 	/**
 	 * Remove a song.
 	 * 
 	 * Removing a now playing song (i.e. the first) restarts playback with a new
 	 * song at index 0.
 	 * 
 	 * Removing a non-existing song may send Reply.enqueuedSongs
 	 * 
 	 * @param song
 	 */
 	public synchronized void removeSong(Song song) {
 		if (playing == song) {
 			playNext();
 		}
 		else {
 			enqueuedSongs.remove(song);
 			emit(Event.EnqueuedSongsChanged);
 		}
 	}
 
 	/**
 	 * Make sure the playback is on.
 	 * 
 	 * This call is valid in any state.
 	 */
 	public synchronized void play() {
 		switch (state) {
 			case IS_STOPPED:
 				validate();
 				if (playing != null) {
 					try {
 						mp = new MediaPlayer();
 						mp.setOnCompletionListener(new OnCompletionListener() {
 							@Override
 							public void onCompletion(MediaPlayer mp) {
 								playNext();
 							}
 						});
 						mp.setOnErrorListener(new OnErrorListener() {
 							@Override
 							public boolean onError(MediaPlayer mp, int what, int extra) {
 								Toast.makeText(getApplicationContext(), R.string.msg_mp_error,
 										Toast.LENGTH_LONG).show();
 								mp.reset();
 								mp.release();
 								setState(State.IS_STOPPED);
 								return false;
 							}
 						});
 						mp.setDataSource(playing.getPath());
 						mp.prepare();
 						mp.start();
 						setState(State.IS_PLAYING);
 					}
 					catch (Exception e) {
 						Toast.makeText(
 								this,
 								String.format(getText(R.string.msg_mp_error_info).toString(),
 										e.getMessage()), Toast.LENGTH_LONG).show();
 						mp.reset();
 						mp.release();
 						setState(State.IS_STOPPED);
 					}
 				}
 				break;
 
 			case IS_PLAYING:
 				/* ignore */
 				break;
 
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PAUSED:
 				mp.start();
 				setState(State.IS_PLAYING);
 				break;
 		}
 	}
 
 	/**
 	 * Remove the currently playing song and play the next one.
 	 * 
 	 * This call is valid in any state.
 	 */
 	public synchronized void playNext() {
 		if (playing != null) {
 			int idx = enqueuedSongs.indexOf(playing);
 			enqueuedSongs.remove(playing);
 			emit(Event.EnqueuedSongsChanged);
 			reset();
 
 			/*
 			 * Idx now point to the next song (since the previous nowPlaying has
 			 * been removed thus shifting array items).
 			 */
 			if (idx < enqueuedSongs.size()) {
 				playing = enqueuedSongs.get(idx);
 			}
 			play();
 		}
 	}
 
 	/**
 	 * Stop playback.
 	 * 
 	 * Yields effect only when IS_PLAYING.
 	 */
 	public synchronized void stop() {
 		switch (state) {
 			case IS_PLAYING:
 				mp.pause();
 				setState(State.IS_PAUSED);
 				break;
 		}
 	}
 
 	/**
 	 * Seek to a position.
 	 * 
 	 * Yields effect always but when IS_STOPPED.
 	 * 
 	 * @param position
 	 */
 	public synchronized void seek(int position) {
 		switch (state) {
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PLAYING:
 			case IS_PAUSED:
 				mp.seekTo(position);
 				break;
 		}
 	}
 
 	/**
 	 * Reset player state.
 	 * 
 	 * This call changes the state to IS_STOPPED.
 	 * 
 	 * Yields effect always but when IS_STOPPED.
 	 */
 	public synchronized void reset() {
 		switch (state) {
 			case IS_PLAYING:
 				mp.stop();
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PAUSED:
 				mp.reset();
 				mp.release();
 				mp = null;
 				playing = null;
 				setState(State.IS_STOPPED);
 				break;
 		}
 	}
 
 	/**
 	 * Validate if nowPlaying is at index 0.
 	 * 
 	 * This is a helper function.
 	 */
 	private synchronized void validate() {
 		if (enqueuedSongs.size() == 0) {
 			reset();
 		}
 		else {
 			if (playing == null) {
 				playing = enqueuedSongs.get(0);
 			}
 		}
 	}
 
 	/**
 	 * Hold the playback.
 	 * 
 	 * This function is used when a headset is disconnected while playing, or a
 	 * call occurs.
 	 * 
 	 * @param reason
 	 */
 	public synchronized void hold(State reason) {
 		switch (state) {
 			case IS_PLAYING:
 				stop();
 				setState(reason);
 		}
 	}
 
 	/**
 	 * Unhold the playback.
 	 * 
 	 * Yields effect only when current state matches reason.
 	 * 
 	 * @see hold()
 	 * @param reason
 	 */
 	public synchronized void unhold(State reason) {
 		if (state == reason) {
 			play();
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		Notification n = new Notification(
 				R.drawable.icon,
 				getText(R.string.msg_service_started),
 				System.currentTimeMillis());
		PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, SongQueue.class)
				.setAction("android.intent.action.MAIN")
				.addCategory("android.intent.category.LAUNCHER"), 0);
 		n.setLatestEventInfo(this, getText(R.string.app_name), "", i);
 		startForeground(this.hashCode(), n);
 
 		/*
 		 * Handle incomming and outcomming calls.
 		 * 
 		 * When the playback is on and there's an incomming call, or an outgoing
 		 * is being dialed we want to hold the playback. Finishing the call will
 		 * resume the playback.
 		 */
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
 				if (state.compareTo(TelephonyManager.EXTRA_STATE_IDLE) == 0) {
 					unhold(State.IS_ON_HOLD_BY_CALL);
 				}
 				else {
 					hold(State.IS_ON_HOLD_BY_CALL);
 				}
 			}
 		}, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
 
 		/*
 		 * Handle (un)plugging a headset
 		 * 
 		 * When the playback is on and a connected headset is unplugged we want
 		 * to hold the playback. Plugging back a headset back will resume the
 		 * playback.
 		 * 
 		 * The AudioManager.ACTION_AUDIO_BECOMING_NOISY is for unplugging only
 		 * and reacts instantenously.
 		 * 
 		 * The Intent.ACTION_HEADSET_PLUG has a hardcoded polling time (in the
 		 * Android framework) and would lag for about 1s before stopping. The
 		 * lag is acceptable when plugging in though.
 		 */
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				switch (intent.getIntExtra("state", -1)) {
 					case 0: /* unplugged */
 						hold(State.IS_ON_HOLD_BY_HEADSET);
 						break;
 					case 1: /* plugged */
 						unhold(State.IS_ON_HOLD_BY_HEADSET);
 						break;
 				}
 			}
 		}, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
 
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				hold(State.IS_ON_HOLD_BY_HEADSET);
 			}
 		}, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
 
 		/*
 		 * Handle external storage removal
 		 */
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				stop();
 				reset();
 				enqueuedSongs.clear();
 				Toast.makeText(getApplicationContext(), R.string.msg_err_ejected, Toast.LENGTH_LONG)
 						.show();
 			}
 		}, new IntentFilter(Intent.ACTION_MEDIA_EJECT));
 
 		Toast.makeText(this, R.string.msg_service_started, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onDestroy() {
 		stopForeground(true);
 		Toast.makeText(this, R.string.msg_service_stopped, Toast.LENGTH_LONG).show();
 		reset();
 		super.onDestroy();
 	}
 
 	/**
 	 * Emit a signal that is propagated to listening clients.
 	 * 
 	 * @param e
 	 */
 	private void emit(Event e) {
 		for (Messenger m : clients) {
 			try {
 				m.send(Message.obtain(null, e.ordinal()));
 			}
 			catch (RemoteException exception) {
 				/* The client must've died */
 				clients.remove(m);
 			}
 		}
 	}
 
 	/**
 	 * Check whether external storage is mounted or not.
 	 */
 	public static boolean isExternalStorageMounted() {
 		return android.os.Environment.getExternalStorageState().compareTo(
 				android.os.Environment.MEDIA_MOUNTED) == 0;
 	}
 }
