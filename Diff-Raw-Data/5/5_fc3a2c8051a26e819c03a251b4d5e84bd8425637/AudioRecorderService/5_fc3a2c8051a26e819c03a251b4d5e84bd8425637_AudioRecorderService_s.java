 package edu.dartmouth.cs.audiorecorder;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.ohmage.mobility.blackout.BlackoutDesc;
 import org.ohmage.mobility.blackout.base.TriggerDB;
 import org.ohmage.mobility.blackout.utils.SimpleTime;
 import org.ohmage.probemanager.StressSenseProbeWriter;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.media.AudioFormat;
 import android.media.MediaRecorder.AudioSource;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.Process;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class AudioRecorderService extends Service {
 
 	private final class ServiceHandler extends Handler {
 		public ServiceHandler(Looper looper) {
 			super(looper);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			Bundle data = msg.getData();
 			if (null == data) {
 				return;
 			}
 			String action = data.getString(AUDIORECORDER_ACTION);
 			if (null == action) {
 				return;
 			}
 		}
 	}
 
 	public static final String AUDIORECORDER_STRING_ID = "edu.dartmouth.cs.audiorecorder.AudioRecorder";
 	public static final String AUDIORECORDER_ON = "edu.dartmouth.besafe.AccelMonitor.intent.ON";
 	public static final String AUDIORECORDER_OFF = "edu.dartmouth.besafe.AccelMonitor.intent.OFF";
 
 	public static final String AUDIORECORDER_NEWTEXT = "edu.dartmouth.besafe.AccelMonitor.intent.NEW_TEXT";
 	public static final String AUDIORECORDER_NEWTEXT_CONTENT = "edu.dartmouth.besafe.AccelMonitor.intent.NEW_TEXT_CONTENT";
 
 	public static final String AUDIORECORDER_ACTION = "edu.dartmouth.cs.audiorecorder.AudioRecorder.ACTION";
 
 	public static final String AUDIORECORDER_ACTION_START = "edu.dartmouth.cs.audiorecorder.AudioRecorder.action.START";
 	public static final String AUDIORECORDER_ACTION_STOP = "edu.dartmouth.cs.audiorecorder.AudioRecorder.action.STOP";
 
 	private static final String AUDIO_RECORDING_DIR = "rawaudio";
 	private static final int WAV_CHUNK_LENGTH_MS = 5 * 60 * 1000; // 5 minutes
 
 	private static final String TAG = "AudioRecorderService";
 
 	public static final AtomicBoolean isServiceRunning = new AtomicBoolean(
 			false);
 
 	private PowerManager.WakeLock mWl;
 	private Looper mServiceLooper;
 	private ServiceHandler mServiceHandler;
 	private RehearsalAudioRecorder mWavAudioRecorder;
 	private Timer mTimer;
 	private IncomingCallDetector mIncomingCallDetector;
 	private OutgoingCallDetector mOutgoingCallDetector;
 	private StressSenseProbeWriter probeWriter;
 
 	// Blackout functionality
 	private Handler handler = new Handler();
 	private Runnable Blackout;
 	private TriggerDB db;
 	private Cursor c;
 	private boolean isRecording = false;
 	
 	// Audio Processing Log History (Used in Analytics/StressActivity)
 	public static LinkedList<String> changeHistory = new LinkedList<String>();
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////
 	// All code below this line is for internal develpment only
 	// //////////////////////////////////////////////////////////////////////////////////
 
 	@Override
 	public void onCreate() {
 		Log.i(TAG, "onCreate()");
 		try {
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			mWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 					AudioRecorderService.class.getName());
 			mWl.acquire();
 
 			HandlerThread thread = new HandlerThread("AudioRecorderHandler",
 					Process.THREAD_PRIORITY_BACKGROUND);
 			thread.start();
 
 			probeWriter = new StressSenseProbeWriter(this);
 			probeWriter.connect();
 
 			// Get the HandlerThread's Looper and use it for our Handler
 			mServiceLooper = thread.getLooper();
 			mServiceHandler = new ServiceHandler(mServiceLooper);
 			isServiceRunning.set(true);
 			mWavAudioRecorder = new RehearsalAudioRecorder(probeWriter,
 					AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
 					AudioFormat.ENCODING_PCM_16BIT, false);
 
 			mIncomingCallDetector = new IncomingCallDetector();
 			mOutgoingCallDetector = new OutgoingCallDetector();
 			registerReceiver(mIncomingCallDetector, new IntentFilter(
 					"android.intent.action.PHONE_STATE"));
 			registerReceiver(mOutgoingCallDetector, new IntentFilter(
 					Intent.ACTION_NEW_OUTGOING_CALL));
 
 			/*
 			 * The Handler calls the Blackout runnable almost every minute to see if
 			 * the time coincides with a user-dictated Blackout time. It then
 			 * starts/stops recording accordingly.
 			 */
 			db = new TriggerDB(this);
 			db.open();
 			c = db.getAllTriggers();
 
 			Blackout = new Runnable() {
 				
 				@Override
 				public void run() {
 					if (c.moveToFirst()) {
 						do {
 							int trigId = c.getInt(c
 									.getColumnIndexOrThrow(TriggerDB.KEY_ID));
 
 							String trigDesc = db.getTriggerDescription(trigId);
 							BlackoutDesc conf = new BlackoutDesc();
 
 							if (!conf.loadString(trigDesc)) {
 								continue;
 							}
 							SimpleTime start = conf.getRangeStart();
 							SimpleTime end = conf.getRangeEnd();
 							SimpleTime now = new SimpleTime();
 							if (!start.isAfter(now) && !end.isBefore(now)) {
								if (isRecording)
 									stopRecording(true);
 								handler.postDelayed(Blackout, 45000);
 								return;
 							}
 
 						} while (c.moveToNext());
 					}
 					if (!isRecording) {
 						startRecoding(true);
 						isRecording = true;
 					}
 					handler.postDelayed(Blackout, 45000);
 				}
 			};
 
 			handler.post(Blackout);
 
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage());
 			e.printStackTrace();
 			stopSelf();
 		}
 
 	}
 
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(mIncomingCallDetector);
 		unregisterReceiver(mOutgoingCallDetector);
 		mWl.release();
 		stopRecording(true);
 		mWavAudioRecorder.release();
 		AudioRecorderService.isServiceRunning.set(false);
 		mServiceLooper.quit();
 		probeWriter.close();
 		handler.removeCallbacks(Blackout);
 		c.close();
 		db.close();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// start the service on foreground to avoid it being killed too soon.
 		CharSequence text = getText(R.string.foreground_service_started);
 		Notification notification = new Notification(R.drawable.icon, text,
 				System.currentTimeMillis());
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(this, SensorPreferenceActivity.class), 0);
 		notification.setLatestEventInfo(this,
 				getText(R.string.local_service_label), text, contentIntent);
 		startForeground(R.string.foreground_service_started, notification);
 
 		Message msg = mServiceHandler.obtainMessage();
 		msg.arg1 = startId;
 		if (intent != null) {
 			msg.setData(intent.getExtras());
 		}
 		mServiceHandler.sendMessage(msg);
 
 		// If we get killed, after returning from here, restart
 		return START_STICKY;
 
 	}
 
 	private void rollToNewAudioFile() {
 		stopRecording(false);
 		startRecoding(false);
 	}
 
 	private String getFileOnSD() {
 		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		String imei = telephonyManager.getDeviceId();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
 		String filename = String.format("%s_%s.wav", imei,
 				sdf.format(new Date()));
 
 		File root = new File(Environment.getExternalStorageDirectory(),
 				AUDIO_RECORDING_DIR);
 		if (!root.exists()) {
 			root.mkdir();
 		}
 		File f = new File(root, filename);
 		return f.getAbsolutePath();
 	}
 
 	class RollWaveFile extends TimerTask {
 
 		@Override
 		public void run() {
 			rollToNewAudioFile();
 		}
 	}
 
 	private void stopRecording(boolean cancelTimer) {
 		if (mWavAudioRecorder.getState() == RehearsalAudioRecorder.State.RECORDING) {
 			Intent i = new Intent();
 			i.setAction(AUDIORECORDER_OFF);
 			sendBroadcast(i);
 			mWavAudioRecorder.stop();
 			if (cancelTimer) {
 				mTimer.cancel();
 			}
 			Log.i(TAG, "Recording stopped");
 		}
 	}
 
 	private void startRecoding(boolean startTimer) {
 
 		if (mWavAudioRecorder.getState() != RehearsalAudioRecorder.State.RECORDING) {
 			if (startTimer) {
 				mTimer = new Timer();
 				mTimer.schedule(new RollWaveFile(), WAV_CHUNK_LENGTH_MS,
 						WAV_CHUNK_LENGTH_MS);
 			}
 
 			String targetFile = getFileOnSD();
 			Log.d(TAG, "Recording audio to " + targetFile);
 
 			mWavAudioRecorder.reset();
 
 			mWavAudioRecorder.setOutputFile(targetFile);
 
 			mWavAudioRecorder.prepare();
 
 			mWavAudioRecorder.start();
 
 			Intent i = new Intent();
 			i.setAction(AUDIORECORDER_ON);
 			sendBroadcast(i);
 			Log.i(TAG, "Recording started");
 		}
 	}
 
 	class IncomingCallDetector extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String extra = intent
 					.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE);
 			// means call running
 			if (extra
 					.equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
 				Log.i(TAG, "Incoming call, stop recording");
 				stopRecording(true);
 			}
 
 			if (extra
 					.equals(android.telephony.TelephonyManager.EXTRA_STATE_IDLE)) {
 				// strategy if the phone call end then start the audio service
 				Log.i(TAG, "Call ended, start recording");
 				startRecoding(true);
 			}
 		}
 
 	}
 
 	class OutgoingCallDetector extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i(TAG, "Outgoing call, stopping recording");
 			stopRecording(true);
 		}
 	}
 
 }
