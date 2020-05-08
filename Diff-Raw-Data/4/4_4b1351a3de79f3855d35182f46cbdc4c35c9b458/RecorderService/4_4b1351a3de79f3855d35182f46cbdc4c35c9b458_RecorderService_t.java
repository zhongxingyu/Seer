 package org.yuttadhammo.androidwave;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.text.format.Time;
 import android.util.Log;
 
 // This class is a service that runs the recording thread.
 public class RecorderService extends Service
 {
 	private Thread recordThread = null;
 	private Recorder recorder = null;
 	private Handler handler;
 	private String filename = null;
 	private Context context;
 	
 	public void setHandler(Handler handler) {
 		this.handler = handler;
 	}
 	
 	// Public interface:
 	
 	public void startRecording(File dir)
 	{
 	//	Log.d("RecorderService", "startRecording()");
 		// 1. Check thread isn't already running.
 		if (recordThread != null)
 		{
 			// Already recording!
 			// TODO: Maybe stop it? Then the record button would also act as a 'mark' type thing.
 			return;
 		}
 		
 		// 2. Start thread.
 		recorder = new Recorder(this);
 		recorder.setDir(dir);
 		recordThread = new Thread(recorder);
 		recordThread.setPriority(Thread.MAX_PRIORITY);
 		recordThread.start();
 	}
 	
 	public void stopRecording()
 	{
 //		Log.d("RecorderService", "stopRecording()");
 		// 1. Tell thread to stop.
 		if (recorder != null)
 		{
 			recorder.stop();
 			recorder = null;
 			recordThread = null;
 		}
 	
 			
 		// 3. Stop this service if there are no clients bound. Actually this will usually be not true.
 		if (!clientsBound)
 			stopSelf();
 		
 		final Message msg = new Message();
 		msg.obj = filename;
 		
         Handler mHandler = new Handler();
 		mHandler.postDelayed(new Runnable() {
             public void run() {
         		handler.sendMessage(msg);
             }
         }, 200);
 		
 	}
 	
 	public boolean isRecording()
 	{
 		return recorder != null;
 	}
 	
 	// Used to say we are recording.
 	private NotificationManager mNM;
 	private SharedPreferences prefs;
 
 	/**
 	 * Class for clients to access. Because we know this service always runs in
 	 * the same process as its clients, we don't need to deal with IPC.
 	 */
 	public class LocalBinder extends Binder
 	{
 		RecorderService getService()
 		{
 			return RecorderService.this;
 		}
 	}
 
 	@Override
 	public void onCreate()
 	{
 //		Log.d("RecorderService", "onCreate()");
 		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 //		Log.d("RecorderService", "onStartCommand()");
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy()
 	{
 //		Log.d("RecorderService", "onDestroy()");
 		hideNotification();
 
 		// Tell the user we stopped.
 	//	Toast.makeText(this, "Recorder Service Stopped", Toast.LENGTH_SHORT).show();
 	}
 
 	// Presumably when it is first created someone will bind to it.
 	// We keep track of whether a client is bound so that when
 	// the recording stops we can end the service if necessary.
 	boolean clientsBound = true;
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		Log.d("RecorderService", "onBind()");
 		clientsBound = true;
 		return mBinder;
 	}
 
 	// This is the object that receives interactions from clients. See
 	// RemoteService for a more complete example.
 	private final IBinder mBinder = new LocalBinder();
 	public File mDir;
 	
 	@Override
 	public void onRebind(Intent intent)
 	{
 		Log.d("RecorderService", "onRebind()");
 		clientsBound = true;
 	}
 	
 	@Override
 	public boolean onUnbind(Intent intent)
 	{
 		Log.d("RecorderService", "onUnbind()");
 		clientsBound = false;
 		
 		// If we are not recording, stop the service
 		if (recorder == null)
 			stopSelf();
 		
 		return true;
 	}
 	
 	/**
 	 * Show a notification while this service is running.
 	 */
 	private void showNotification()
 	{
 		// In this sample, we'll use the same text for the ticker and the
 		// expanded notification
 		CharSequence text = getText(R.string.recording_notification);
 
 		// Set the icon, scrolling text and timestamp
 		Notification notification = new Notification(R.drawable.icon,
 				text, System.currentTimeMillis());
 		
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
 		// The PendingIntent to launch our activity if the user selects this
 		// notification
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(this, RecorderController.class), 0);
 
 		// Set the info for the views that show in the notification panel.
 		notification.setLatestEventInfo(this,
 				getText(R.string.recording_notification), text, contentIntent);
 
 		// Send the notification.
 		// We use a string id because it is a unique number. We use it later to
 		// cancel.
 		mNM.notify(R.string.recording_notification, notification);
 	}
 	
 	private void hideNotification()
 	{
 		// Cancel the persistent notification.
 		mNM.cancel(R.string.recording_notification);
 	}
 
 	// This is the actual thread that does the recording.
 	class Recorder implements Runnable
 	{
 		public Recorder(Context ctx)
 		{
 			context = ctx;	
 		}
 		
 		private Context context;
 		
 		public void setDir(File dir)
 		{
 			// If not called, the filename is autogenerated.
 			mDir = dir;
 		}
 		
 		private boolean stop = false;
 		
 		public void stop()
 		{
 			// Stop recording etc.
 			stop = true;
 		}
 		
 		private PowerManager.WakeLock wake = null;
 		private File dir;
 		@Override
 		public void run()
 		{
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sound Recorder ~ Eva");
 			
 //			Log.d("Recorder", "run()");
 			// Logic is like this:
 			
 			// 1. Acquire wakelock,
 			// 2. Open MP3 file,
 			// 3. Open audio input stream,
 			//
 			// 4. Loop:
 			//    a) See if we have been told to stop.
 			//    b) Read audio input.
 			//    c) Write MP3 data.
 			//    d) Send status information?
 			//
 			// 5. Close audio stream.
 			// 6. Close MP3 file.
 			// 7. Release wakelock. (MAKE SURE THIS HAPPENS!)
 			try
 			{
 				acquireWakelock();
 				mainLoop();
 			}
 			finally
 			{
 				releaseWakelock();
 			}
 		}
 		
 		private void acquireWakelock()
 		{
 			if (wake != null)
 				wake.acquire();
 
 		}
 		
 		private void releaseWakelock()
 		{
 			if (wake != null)
 				wake.release();
 		}
 		
 		private void mainLoop()
 		{
 
 			// Create filename from current date.
 			Time t = new Time();
 			t.setToNow();
 			filename = mDir +"/"+ t.format2445() + ".wav";
 
 			// Make sure directory exists.
 //				Log.d("Recorder", "Creating dir: /sdcard/Sound Recordings/");
 			dir = mDir;
 		
 			if (!dir.exists() && !dir.mkdirs())
 				Log.e("Recorder", "Dir creation failed!");
 			
 			int min = AudioRecord.getMinBufferSize(44100, 
 					AudioFormat.CHANNEL_IN_MONO, 
 					AudioFormat.ENCODING_PCM_16BIT);
 			if (min < 4096)
 				min = 4096;
 
 			AudioRecord record = openAudio();
 			if (record == null) // Couldn't open audio. Shouldn't really happen.
 			{
 				showToast("Couldn't open audio input");
 				return;
 			}
 			
 //			Log.d("Recorder", "starting recording");
 
 			record.startRecording();
 
 
 			AudioWriter out = new WavWriter();
 
 			try
 			{
 				out.open(filename, record.getSampleRate());
 				showToast("Recording to: " + filename);
 
 				short[] audioData = new short[4096];
 
 				showNotification();
 
 				while (!stop)
 				{
 					// Check if we need to stop. Probably don't reallly need to
 					// synchronise this.
 					int num = record.read(audioData, 0, audioData.length);
 //					Log.d("Recorder", "Got samples: " + num);
 					out.write(audioData, 0, num);
 				}
 				showToast("Stopped recording");
 			
 			}
 			catch (Exception e)
 			{
 				showToast("Error recording audio");
 				Log.e("Recorder", "Error recording audio.");
 			}
 			
 			// Still try to close the file - we may have just ran out of space on the SD card.
 			try
 			{
 				out.close();
 			}
 			catch (Exception e)
 			{
 				Log.e("Recorder", "Error closing output file.");
 			}
 			
 			
 			hideNotification();
 
 			record.stop();
 			record.release();
 			record = null;
 		}
 		
 		private AudioRecord openAudio()
 		{
 			int samplingRate = Integer.parseInt(prefs.getString("sample_rate", "44100"));
 			int min = AudioRecord.getMinBufferSize(samplingRate, 
 					AudioFormat.CHANNEL_IN_MONO, 
 					AudioFormat.ENCODING_PCM_16BIT);
 			Log.d("Recorder", "Min buffer size: " + min);
			Log.d("Recorder", "Sampling Rate: " + samplingRate);
 			if (min < 4096)
 				min = 4096;
 			
 			AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate,
 					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,	min);
 			if (record.getState() == AudioRecord.STATE_INITIALIZED)
 			{
 //				Log.d("Recorder", "Audio recorder initialised at " + record.getSampleRate());
 				return record;
 			}
 			record.release();
 			record = null;
 			return null;
 		}
 	}
 	private void showToast(String txt)
 	{
 		// Can't do this from a random thread unless we use Looper to give it a message loop.
 //		Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
 	}
 }
