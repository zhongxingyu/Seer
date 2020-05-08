 // Copyright 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 // dbartists - Douban artists client for Android
 // Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); you may not
 // use this file except in compliance with the License.  You may obtain a copy
 // of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 // WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 // License for the specific language governing permissions and limitations
 // under the License.
 //
 //
 //                           ___====-_  _-====___
 //                     _--^^^#####//      \\#####^^^--_
 //                  _-^##########// (    ) \\##########^-_
 //                 -############//  |\^^/|  \\############-
 //               _/############//   (@::@)   \\############\_
 //              /#############((     \\//     ))#############\
 //             -###############\\    (oo)    //###############-
 //            -#################\\  / VV \  //#################-
 //           -###################\\/      \//###################-
 //          _#/|##########/\######(   /\   )######/\##########|\#_
 //          |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 //          `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 //             `   `  `      `   / | |  | | \   '      '  '   '
 //                              (  | |  | |  )
 //                             __\ | |  | | /__
 //                            (vvv(VVV)(VVV)vvv)
 //
 //                             HERE BE DRAGONS
 
 package org.dbartists;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.dbartists.utils.PlaylistEntry;
 import org.dbartists.utils.PlaylistProvider;
 import org.dbartists.utils.PlaylistProvider.Items;
 
 import com.flurry.android.FlurryAgent;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnInfoListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.provider.BaseColumns;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class PlaybackService extends Service implements OnPreparedListener,
 		OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
 		OnInfoListener {
 
 	public class ListenBinder extends Binder {
 
 		public PlaybackService getService() {
 			return PlaybackService.this;
 		}
 	}
 
 	private static final String LOG_TAG = PlaybackService.class.getName();
 	private static final String SERVICE_PREFIX = "org.dbartists.";
 	public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
 	public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
 
 	public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";
 	public static final String EXTRA_TITLE = "title";
 	public static final String EXTRA_DOWNLOADED = "downloaded";
 	public static final String EXTRA_DURATION = "duration";
 
 	public static final String EXTRA_POSITION = "position";
 	private MediaPlayer mediaPlayer;
 
 	private boolean isPrepared = false;
 	private StreamProxy proxy;
 	private NotificationManager notificationManager;
 	private static final int NOTIFICATION_ID = 1;
 	private int bindCount = 0;
 
 	private PlaylistEntry current = null;
 	private TelephonyManager telephonyManager;
 	private PhoneStateListener listener;
 	private boolean isPausedInCall = false;
 	private Intent lastChangeBroadcast;
 	private Intent lastUpdateBroadcast;
 	private int lastBufferPercent = 0;
 
 	private Thread updateProgressThread;
 
 	private MusicDownloader md;
 
 	// Amount of time to rewind playback when resuming after call
 	private final static int RESUME_REWIND_TIME = 3000;
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 
 			String title = current.title;
 			int file_size = msg.arg1;
 			boolean stream = current.isStream;
 			String url = current.url;

 			File f = new File(StreamProxy.getFileName(title));
 
 			Log.d(LOG_TAG, "title: " + title + " remote size: " + file_size);
 
 			// From 2.2 on (SDK ver 8), the local mediaplayer can handle
 			// Shoutcast
 			// streams natively. Let's detect that, and not proxy.
 			Log.d(LOG_TAG, "SDK Version " + Build.VERSION.SDK);
 			int sdkVersion = 0;
 			try {
 				sdkVersion = Integer.parseInt(Build.VERSION.SDK);
 			} catch (NumberFormatException e) {
 			}
 
 			if (f.exists() && file_size != -1
 					&& Math.abs(f.length() - file_size) < 100 * 1024) {
 				url = f.getAbsolutePath();
 				stream = false;
 			} else if (sdkVersion < 8) {
 				if (proxy == null) {
 					proxy = new StreamProxy();
 					proxy.init();
 					proxy.start();
 				}
 
 				proxy.setTitle(title);
 				String proxyUrl = String.format("http://127.0.0.1:%d/%s",
 						proxy.getPort(), url);
 				url = proxyUrl;
 				stream = true;
 			} else {
 				stream = true;
 				md.download(url, f.getAbsolutePath());
 			}
 
 			synchronized (this) {
 				try {
 					Log.d(LOG_TAG, "reset: " + url);
 					mediaPlayer.reset();
 
 					mediaPlayer.setDataSource(url);
 					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 					Log.d(LOG_TAG, "Preparing: " + url);
 
 					if (stream)
 						mediaPlayer.prepareAsync();
 					else {
 						mediaPlayer.prepare();
 						lastBufferPercent = 100;
 						updateProgress();
 					}
 					Log.d(LOG_TAG, "Waiting for prepare");
 				} catch (IllegalArgumentException e) {
 					Log.e(LOG_TAG, "", e);
 				} catch (IllegalStateException e) {
 					Log.e(LOG_TAG, "", e);
 				} catch (IOException e) {
 					Log.e(LOG_TAG, "", e);
 				}
 			}
 		}
 	};
 
 	private OnCompletionListener onCompletionListener;
 
 	private OnPreparedListener onPreparedListener;
 
 	/**
 	 * Remove all intents and notifications about the last media.
 	 */
 	private void cleanup() {
 		notificationManager.cancel(NOTIFICATION_ID);
 		if (lastChangeBroadcast != null) {
 			getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
 		}
 		if (lastUpdateBroadcast != null) {
 			getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
 		}
 		getApplicationContext().sendBroadcast(new Intent(SERVICE_CLOSE_NAME));
 	}
 
 	public PlaylistEntry getCurrentEntry() {
 		PlaylistEntry c = current;
 		return c;
 	}
 
 	synchronized public int getCurrentPosition() {
 		if (isPrepared) {
 			return mediaPlayer.getCurrentPosition();
 		}
 		return 0;
 	}
 
 	synchronized public int getDuration() {
 		if (isPrepared) {
 			return mediaPlayer.getDuration();
 		}
 		return 0;
 	}
 
 	private PlaylistEntry getFromCursor(Cursor c) {
 		String title = null, url = null;
 		long id;
 		int order;
 		if (!c.moveToFirst()) {
 			c.close();
 			return null;
 		}
 		do {
 			if (c.getInt(c.getColumnIndex(PlaylistProvider.Items.IS_PLAYING)) != 0)
 				break;
 		} while (c.moveToNext());
 		// XXX: FIX THIS PLEASE
 		if (c.moveToNext()) {
 			id = c.getInt(c.getColumnIndex(BaseColumns._ID));
 			title = c.getString(c.getColumnIndex(PlaylistProvider.Items.NAME));
 			url = c.getString(c.getColumnIndex(PlaylistProvider.Items.URL));
 			order = c.getInt(c
 					.getColumnIndex(PlaylistProvider.Items.PLAY_ORDER));
 			c.close();
 			return new PlaylistEntry(id, url, title, false, order);
 		} else if (c.moveToFirst()) {
 			id = c.getInt(c.getColumnIndex(BaseColumns._ID));
 			title = c.getString(c.getColumnIndex(PlaylistProvider.Items.NAME));
 			url = c.getString(c.getColumnIndex(PlaylistProvider.Items.URL));
 			order = c.getInt(c
 					.getColumnIndex(PlaylistProvider.Items.PLAY_ORDER));
 			c.close();
 			return new PlaylistEntry(id, url, title, false, order);
 		}
 		c.close();
 		return null;
 	}
 
 	private PlaylistEntry getNextPlaylistItem(int current) {
 		return retrievePlaylistItem(current, true);
 	}
 
 	synchronized public int getPosition() {
 		if (isPrepared) {
 			return mediaPlayer.getCurrentPosition();
 		}
 		return 0;
 	}
 
 	synchronized public boolean isPlaying() {
 		if (isPrepared) {
 			return mediaPlayer.isPlaying();
 		}
 		return false;
 	}
 
 	/**
 	 * Start listening to the given URL.
 	 */
 	public void listen(final String url, boolean stream)
 			throws IllegalArgumentException, IllegalStateException, IOException {
 		// First, clean up any existing audio.
 		if (isPlaying()) {
 			stop();
 		}
 
 		Log.d(LOG_TAG, "listening to " + url + " stream=" + stream);
 		// // From 2.2 on (SDK ver 8), the local mediaplayer can handle
 		// Shoutcast
 		// // streams natively. Let's detect that, and not proxy.
 		// Log.d(LOG_TAG, "SDK Version " + Build.VERSION.SDK);
 		// int sdkVersion = 0;
 		// try {
 		// sdkVersion = Integer.parseInt(Build.VERSION.SDK);
 		// } catch (NumberFormatException e) {
 		// }
 
 		markAsPlaying(current.id);
 
 		new Thread() {
 			@Override
 			public void run() {
 				int file_size;
 				try {
 					URL remote = new URL(url);
 					URLConnection urlConnection = remote.openConnection();
 					urlConnection.connect();
 					file_size = urlConnection.getContentLength();
 				} catch (IOException e) {
 					file_size = -1;
 				}
 				Message msg = new Message();
 				msg.arg1 = file_size;
 				handler.sendMessage(msg);
 			}
 		}.start();
 
 	}
 
 	private void markAsPlaying(long id) {
 
 		Log.d(LOG_TAG, "mark as playing: " + id);
 
 		ContentValues values = new ContentValues();
 		values.put(Items.IS_PLAYING, false);
 		getContentResolver().update(PlaylistProvider.CONTENT_URI, values, null,
 				null);
 
 		Uri update = ContentUris.withAppendedId(PlaylistProvider.CONTENT_URI,
 				id);
 		values = new ContentValues();
 		values.put(Items.IS_PLAYING, true);
 		getContentResolver().update(update, values, null, null);
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		bindCount++;
 		Log.d(LOG_TAG, "Bound PlaybackService, count " + bindCount);
 		return new ListenBinder();
 	}
 
 	@Override
 	public void onBufferingUpdate(MediaPlayer mp, int progress) {
 		if (isPrepared) {
 			lastBufferPercent = progress;
 			updateProgress();
 		}
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		Log.w(LOG_TAG, "onComplete()");
 
 		synchronized (this) {
 			if (!isPrepared) {
 				// This file was not good and MediaPlayer quit
 				Log.w(LOG_TAG,
 						"MediaPlayer refused to play current item. Bailing on prepare.");
 			}
 		}
 
 		cleanup();
 
 		if (onCompletionListener != null) {
 			onCompletionListener.onCompletion(mp);
 		}
 
 		playNext();
 		if (bindCount == 0 && !isPlaying()) {
 			stopSelf();
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		mediaPlayer = new MediaPlayer();
 		mediaPlayer.setOnBufferingUpdateListener(this);
 		mediaPlayer.setOnCompletionListener(this);
 		mediaPlayer.setOnErrorListener(this);
 		mediaPlayer.setOnInfoListener(this);
 		mediaPlayer.setOnPreparedListener(this);
 		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		Log.w(LOG_TAG, "Playback service created");
 
 		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		// Create a PhoneStateListener to watch for offhook and idle events
 		listener = new PhoneStateListener() {
 			@Override
 			public void onCallStateChanged(int state, String incomingNumber) {
 				switch (state) {
 				case TelephonyManager.CALL_STATE_OFFHOOK:
 				case TelephonyManager.CALL_STATE_RINGING:
 					// Phone going offhook or ringing, pause the player.
 					if (isPlaying()) {
 						pause();
 						isPausedInCall = true;
 					}
 					break;
 				case TelephonyManager.CALL_STATE_IDLE:
 					// Phone idle. Rewind a couple of seconds and start playing.
 					if (isPausedInCall) {
 						seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
 						play();
 					}
 					break;
 				}
 			}
 		};
 
 		// Register the listener with the telephony manager.
 		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
 
 		md = new MusicDownloader(this);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.w(LOG_TAG, "Service exiting");
 
 		if (updateProgressThread != null) {
 			updateProgressThread.interrupt();
 			try {
 				updateProgressThread.join(3000);
 			} catch (InterruptedException e) {
 				Log.e(LOG_TAG, "", e);
 			}
 		}
 
 		md.stopThread();
 
 		stop();
 		synchronized (this) {
 			if (mediaPlayer != null) {
 				mediaPlayer.release();
 				mediaPlayer = null;
 			}
 		}
 
 		telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
 		synchronized (this) {
 			if (!isPrepared) {
 				// This file was not good and MediaPlayer quit
 				Log.w(LOG_TAG,
 						"MediaPlayer refused to play current item. Bailing on prepare.");
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
 		Log.w(LOG_TAG, "onInfo(" + arg1 + ", " + arg2 + ")");
 		return false;
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		Log.d(LOG_TAG, "Prepared");
 		synchronized (this) {
 			if (mediaPlayer != null) {
 				isPrepared = true;
 			}
 		}
 		play();
 		if (onPreparedListener != null) {
 			onPreparedListener.onPrepared(mp);
 		}
 
 		updateProgressThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				// Initially, don't send any updates, since it takes a while for
 				// the
 				// media player to settle down.
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					return;
 				}
 				while (true) {
 					updateProgress();
 					try {
 						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						break;
 					}
 				}
 			}
 		});
 		updateProgressThread.start();
 	}
 
 	@Override
 	public boolean onUnbind(Intent arg0) {
 		bindCount--;
 		Log.d(LOG_TAG, "Unbinding PlaybackService, count " + bindCount);
 		if (!isPlaying() && bindCount == 0) {
 			Log.w(LOG_TAG, "Will stop self");
 			stopSelf();
 		} else {
 			Log.d(LOG_TAG, "Will not stop self");
 		}
 		return false;
 	}
 
 	synchronized public void pause() {
 		Log.d(LOG_TAG, "pause");
 		if (isPrepared) {
 			mediaPlayer.pause();
 		}
 		notificationManager.cancel(NOTIFICATION_ID);
 	}
 
 	synchronized public void play() {
 		if (!isPrepared || current == null) {
 			Log.e(LOG_TAG, "play - not prepared");
 			return;
 		}
 		Log.d(LOG_TAG, "play " + current.id);
 		mediaPlayer.start();
 
 		int icon = R.drawable.stat_notify_musicplayer;
 		CharSequence contentText = current.title;
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, contentText, when);
 		notification.flags = Notification.FLAG_NO_CLEAR
 				| Notification.FLAG_ONGOING_EVENT;
 		Context c = getApplicationContext();
 		CharSequence title = getString(R.string.app_name);
 		Intent notificationIntent;
 		if (current.artist != null) {
 			notificationIntent = new Intent(this, TrackListActivity.class);
 			notificationIntent.putExtra(Constants.EXTRA_ARTIST_NAME,
 					current.artist.getName());
 			notificationIntent.putExtra(Constants.EXTRA_ARTIST_URL,
 					current.artist.getUrl());
 			notificationIntent.putExtra(Constants.EXTRA_ARTIST_IMG,
 					current.artist.getImg());
 		} else {
 			notificationIntent = new Intent(this, Main.class);
 		}
 		notificationIntent.setAction(Intent.ACTION_VIEW);
 		notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
 		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
 				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 		notification.setLatestEventInfo(c, title, contentText, contentIntent);
 		notificationManager.notify(NOTIFICATION_ID, notification);
 
 		// Change broadcasts are sticky, so when a new receiver connects, it
 		// will
 		// have the data without polling.
 		if (lastChangeBroadcast != null) {
 			getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
 		}
 		lastChangeBroadcast = new Intent(SERVICE_CHANGE_NAME);
 		lastChangeBroadcast.putExtra(EXTRA_TITLE, current.title);
 		getApplicationContext().sendStickyBroadcast(lastChangeBroadcast);
 	}
 
 	private void playNext() {
 		Log.w(LOG_TAG, "Playing next track");
 		if (current != null) {
 			PlaylistEntry entry = getNextPlaylistItem(current.order);
 			if (entry != null) {
 				current = entry;
 				String url = current.url;
 				try {
 					if (url == null || url.equals("")) {
 						Log.d(LOG_TAG, "no url");
 						// Do nothing.
 					} else {
 						listen(url, current.isStream);
 					}
 				} catch (IllegalArgumentException e) {
 					Log.e(LOG_TAG, "", e);
 					e.printStackTrace();
 				} catch (IllegalStateException e) {
 					Log.e(LOG_TAG, "", e);
 				} catch (IOException e) {
 					Log.e(LOG_TAG, "", e);
 				}
 				Log.d(LOG_TAG, "playing commenced");
 			}
 		}
 	}
 
 	private PlaylistEntry retrievePlaylistItem(int current, boolean next) {
 		String sort = PlaylistProvider.Items.PLAY_ORDER
 				+ (next ? " asc" : " desc");
 		return retrievePlaylistItem(null, null, sort);
 	}
 
 	private PlaylistEntry retrievePlaylistItem(String selection,
 			String[] selectionArgs, String sort) {
 		Cursor cursor = getContentResolver().query(
 				PlaylistProvider.CONTENT_URI, null, selection, selectionArgs,
 				sort);
 		return getFromCursor(cursor);
 	}
 
 	synchronized public void seekTo(int pos) {
 		if (isPrepared) {
 			mediaPlayer.seekTo(pos);
 		}
 	}
 
 	public void setCurrent(PlaylistEntry c) {
 		current = c;
 	}
 
 	// -----------
 	// Some stuff added for inspection when testing
 
 	/**
 	 * Allows a class to be notified when the currently playing track is
 	 * completed. Mostly used for testing the service
 	 * 
 	 * @param listener
 	 */
 	public void setOnCompletionListener(OnCompletionListener listener) {
 		onCompletionListener = listener;
 	}
 
 	/**
 	 * Allows a class to be notified when the currently selected track has been
 	 * prepared to start playing. Mostly used for testing.
 	 * 
 	 * @param listener
 	 */
 	public void setOnPreparedListener(OnPreparedListener listener) {
 		onPreparedListener = listener;
 	}
 
 	synchronized public void stop() {
 		Log.d(LOG_TAG, "stop");
 		if (isPrepared) {
 			if (proxy != null) {
 				proxy.stop();
 				proxy = null;
 			}
 			mediaPlayer.stop();
 			isPrepared = false;
 		}
 		cleanup();
 	}
 
 	/**
 	 * Sends an UPDATE broadcast with the latest info.
 	 */
 	private synchronized void updateProgress() {
 		if (isPrepared && mediaPlayer != null && mediaPlayer.isPlaying()) {
 			// Update broadcasts are sticky, so when a new receiver connects, it
 			// will
 			// have the data without polling.
 			if (lastUpdateBroadcast != null) {
 				getApplicationContext().removeStickyBroadcast(
 						lastUpdateBroadcast);
 			}
 			lastUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
 			int position = mediaPlayer.getCurrentPosition();
 			int duration = mediaPlayer.getDuration();
 			lastUpdateBroadcast.putExtra(EXTRA_DURATION,
 					mediaPlayer.getDuration());
 			lastUpdateBroadcast.putExtra(EXTRA_DOWNLOADED,
 					(int) ((lastBufferPercent / 100.0) * mediaPlayer
 							.getDuration()));
 			lastUpdateBroadcast.putExtra(EXTRA_POSITION,
 					mediaPlayer.getCurrentPosition());
 			getApplicationContext().sendStickyBroadcast(lastUpdateBroadcast);
 		}
 	}
 }
