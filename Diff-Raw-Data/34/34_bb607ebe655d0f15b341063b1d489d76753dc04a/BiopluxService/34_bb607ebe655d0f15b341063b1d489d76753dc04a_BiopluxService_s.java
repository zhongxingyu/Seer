 package ceu.marten.services;
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import plux.android.bioplux.BPException;
 import plux.android.bioplux.Device;
 import plux.android.bioplux.Device.Frame;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.model.io.DataManager;
 import ceu.marten.ui.NewRecordingActivity;
 
 public class BiopluxService extends Service {
 
 	private static final String TAG = BiopluxService.class.getName();
 
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_RECORDING_DURATION = 3;
 	public static final int MSG_DATA = 4;
 	public static final int MSG_CONNECTION_ERROR = 5;
 	
 	public static final int ERROR_PROCESSING_FRAMES = 6;
 	public static final int ERROR_SAVING_RECORDING = 7;
 	
 	//Get 80 frames every 50 miliseconds
 	public static final int NUMBER_OF_FRAMES = 80; 
 	public static final long TIMER_TIME = 50L;
 	
 	//Used to synchronize threads
 	private static final Object writingLock = new Object();
 	private boolean isWriting;
 	
 	static Messenger client = null;
 	private NotificationManager notificationManager;
 	private Timer timer = new Timer();
 	private boolean forceStopError= false;
 	private static DataManager dataManager;
 
 	private String recordingName;
	private long samplingFrames;
	private long samplingCounter;
 	private DeviceConfiguration configuration;
 
 	private Device connection;
 	private Device.Frame[] frames;
 
 	private final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	static class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				client = msg.replyTo;
 				break;
 			case MSG_UNREGISTER_CLIENT:
 				//client = null
 				break;
 			case MSG_RECORDING_DURATION:
 				dataManager.setDuration(msg.getData().getString("duration"));
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		
 		Log.i(TAG, "Service created");
 		super.onCreate();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		getInfoFromActivity(intent);
 		samplingCounter=0;
 		frames = new Device.Frame[NUMBER_OF_FRAMES];
 		for (int i = 0; i < frames.length; i++)
 			frames[i] = new Frame();
 		
 		if(connectToBiopluxDevice()){
 			dataManager = new DataManager(this, recordingName,
 					configuration);
 			showNotification(intent);
 
 			timer.schedule(new TimerTask() {
 				public void run() {
 					processFrames();
 				}
 			}, 0, TIMER_TIME);
 		}
 		
 		return mMessenger.getBinder();
 	}
 	
 	private void processFrames() {
 		
 		synchronized (writingLock) {
 			isWriting = true;
 		}
	
 		getFrames(NUMBER_OF_FRAMES);
 		
 		loop:
 		for (Frame f : frames) {
 			if(!dataManager.writeFramesToTmpFile(f)){
 				sendErrorNotificationToActivity(ERROR_PROCESSING_FRAMES);
 				forceStopError = true;
 				stopService();
 				break loop;
 			}
 			if(samplingCounter++ >= samplingFrames){
 				sendGraphDataToActivity(f.an_in);
 				samplingCounter -= samplingFrames;
 			}	
 		}
 		synchronized (writingLock) {
 			isWriting = false;
 		}
 	}
 
 	private void getFrames(int nFrames) {
 		try {
 			connection.GetFrames(nFrames, frames);
 		} catch (BPException e) {
 			Log.e(TAG, "exception getting frames", e);
 			sendErrorNotificationToActivity(e.code);
 			forceStopError = true;
 			stopService();
 		}
 	}
 
 	private void getInfoFromActivity(Intent intent) {
 		recordingName = intent.getStringExtra("recordingName").toString();
 		configuration = (DeviceConfiguration) intent
 				.getSerializableExtra("configSelected");
		samplingFrames = configuration.getReceptionFrequency()/configuration.getSamplingFrequency();
 	}
 
 	private boolean connectToBiopluxDevice() {
 
 		// BIOPLUX INITIALIZATION
 		try {
 			connection = Device.Create(configuration.getMacAddress());
 			connection.BeginAcq(configuration.getReceptionFrequency(),
 					configuration.getActiveChannelsAsInteger(),
 					configuration.getNumberOfBits());
 		} catch (BPException e) {
 			try {
 				connection.Close();
 			} catch (BPException e1) {
 				Log.e(TAG, "bioplux close connection exception", e1);
 				sendErrorNotificationToActivity(e1.code);
 				forceStopError = true;
 				stopService();
 				return false;
 			}
 			Log.e(TAG, "bioplux connection exception", e);
 			sendErrorNotificationToActivity(e.code);
 			forceStopError = true;
 			stopService();
 			return false;
 		}
 		return true;
 	}
 
 	private void showNotification(Intent parentIntent) {
 
 		// SET THE BASICS
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.notification)
 				.setContentTitle("Device Connected")
 				.setContentText("service running, receiving data..");
 
 		// EXTRA INFO ON INTENT
 		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
 		newRecordingIntent.putExtra("recordingName", recordingName);
 		newRecordingIntent.putExtra("configSelected", configuration);
 
 		// PENDING INTENT
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
 				newRecordingIntent, 0);
 		mBuilder.setContentIntent(pendingIntent);
 		mBuilder.setOngoing(true);
 
 		// GET THE NOTIFICATION AND NOTIFY
 		Notification notification = mBuilder.build();
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.notify(R.string.service_id, notification);
 	}
 
 	private void sendGraphDataToActivity(short[] data) {
 		Bundle b = new Bundle();
         b.putShortArray("frame", data);
         Message message = Message.obtain(null, MSG_DATA);
         message.setData(b);
 		try {
 			client.send(message);
 		} catch (RemoteException e) {
 			Log.e(TAG, "client is dead. Client removed", e);
 			forceStopError = true;
 			stopService();
 			client = null;
 		}
 	}
 
 	private void sendErrorNotificationToActivity(int errorCode) {
 		try {
 			client.send(Message.obtain(null, MSG_CONNECTION_ERROR, errorCode, 0));
 		} catch (RemoteException e) {
 			Log.e(TAG, "Exception sending error message to activity", e);
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return START_NOT_STICKY; 
 	}
 	
 	private void stopService(){
 		notificationManager.cancel(R.string.service_id);
 		if (timer != null)
 			timer.cancel();
 
 		while (isWriting) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e2) {
 				Log.e(TAG, "Exception thread is sleeping", e2);
 			}
 		}
 		if(!dataManager.closeWriters())
 			sendErrorNotificationToActivity(ERROR_SAVING_RECORDING);
 		try {
 			connection.EndAcq();
 			connection.Close();
 		} catch (BPException e) {
 			Log.e(TAG, "error ending ACQ", e);
 			sendErrorNotificationToActivity(e.code);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		if(!forceStopError)
 			stopService();
 		
 		if(!dataManager.saveFiles())
 			sendErrorNotificationToActivity(ERROR_SAVING_RECORDING);
 		
 		Log.i(TAG, "service stopped");
 		super.onDestroy();
 	}
 }
