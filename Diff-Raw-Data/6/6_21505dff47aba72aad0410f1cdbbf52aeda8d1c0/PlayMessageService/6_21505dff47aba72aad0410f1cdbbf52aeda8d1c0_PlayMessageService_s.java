 package com.ultivox.uvoxplayer;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class PlayMessageService extends Service {
 
 	private static final String TAG = "PlayMessageService";
 	private MediaPlayer mPlayer;
 	BroadcastReceiver brPlay;
 	String pathS = null;
 
 
 	public static Queue<String> messQueue = new LinkedList<String>();
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mPlayer = MediaPlayer.create(this, R.raw.everybody);
 		mPlayer.setLooping(false);
 		mPlayer.reset();
 
 		brPlay = new BroadcastReceiver() {
 			// �������� ��� ��������� ���������
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				if (mPlayer.isPlaying()) {
 					mPlayer.setVolume(UVoxPlayer.volumeMessage,
 							UVoxPlayer.volumeMessage);
 				}
 			}
 		};
 		IntentFilter volFilt = new IntentFilter(UVoxPlayer.BROADCAST_MESSINFO);
 		// ������������ (��������) BroadcastReceiver
 		registerReceiver(brPlay, volFilt);
 		Log.d(TAG, "Create service");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(TAG, "Start service");
 		mPlayer.setOnCompletionListener(new OnCompletionListener() {
 			public void onCompletion(MediaPlayer mp) {
 				Log.d(TAG, "File message ended.");
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_END);
 				mPlayer.stop();
 				mPlayer.reset();
 				if (playNextMessage()) {
 					Log.d(TAG, "Play next file.");
 				} else {
 					Log.d(TAG, "All set files played.");
 					mPlayer.release();
 					Intent mesint = new Intent(UVoxPlayer.BROADCAST_ACT_SCH);
 					mesint.putExtra(UVoxPlayer.PARAM_RESULT,
                             UVoxPlayer.STATUS_MESSAGE_STOP);
 					sendBroadcast(mesint);
 					stopSelf();
 				}
 			}
 		});
 
 		if (playNextMessage()) {
 			Log.d(TAG, "Play first file.");
 		} else {
 			Log.d(TAG, "No files to play.");
 			Intent mesint = new Intent(UVoxPlayer.BROADCAST_ACT_SCH);
 			mesint.putExtra(UVoxPlayer.PARAM_RESULT,
                     UVoxPlayer.STATUS_MESSAGE_STOP);
 			sendBroadcast(mesint);
 			stopSelf();
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mPlayer.release();
 		unregisterReceiver(brPlay);
 		Log.d(TAG, "Message service stoped");
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private boolean playNextMessage() {
 
 		if (messQueue.size()>0) {
 			pathS = UVoxEnvironment.getExternalStorageDirectory().toString()
 					+ UVoxPlayer.STORAGE + UVoxPlayer.MESSAGES + File.separator + messQueue.poll();
 			Log.d(TAG, "Now play message file: " + pathS);
 			try {
 				// mPlayer.reset();
 				// mPlayer = null;
 				mPlayer.setDataSource(pathS);
 				mPlayer.prepare();
 				mPlayer.setVolume(UVoxPlayer.volumeMessage,
 						UVoxPlayer.volumeMessage);
 				mPlayer.start();
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_BEGIN);
			} catch (IllegalArgumentException e) {
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_ERROR);
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_ERROR);
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_ERROR);
 				e.printStackTrace();
 			} catch (IOException e) {
 				LogPlay.write(UVoxPlayer.LOG_COMMAND_MESS, pathS, LogPlay.STAT_ERROR);
 				e.printStackTrace();
 			}
			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 }
