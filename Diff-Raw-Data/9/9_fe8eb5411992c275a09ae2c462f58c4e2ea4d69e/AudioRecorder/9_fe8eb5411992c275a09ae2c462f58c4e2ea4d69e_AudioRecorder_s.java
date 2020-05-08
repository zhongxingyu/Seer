 package ca.ilanguage.oprime.datacollection;
 
 import java.io.IOException;
 
 import ca.ilanguage.oprime.activity.OPrimeLib;
 import ca.ilanguage.oprime.content.OPrime;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.media.MediaRecorder;
 import android.os.IBinder;
 
 import ca.ilanguage.oprime.R;
 
 public class AudioRecorder extends Service {
 	
 	protected static String TAG = "AudioRecorder";
 	private NotificationManager mNM;
 	private Notification mNotification;
 	private int NOTIFICATION = 7029;
 	private PendingIntent mContentIntent;
 	private int mAuBlogIconId = R.drawable.ic_oprime;
 	
 	private String mAudioResultsFile ="";
 	
 	private MediaRecorder mRecorder;
     private Boolean mRecordingNow = false;
     private RecordingReceiver audioFileUpdateReceiver;
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
	@Override
 	public void onCreate() {
 		super.onCreate();
 		if (audioFileUpdateReceiver == null){
 			audioFileUpdateReceiver = new RecordingReceiver();
 		}
 		IntentFilter intentDictRunning = new IntentFilter(OPrime.INTENT_STOP_AUDIO_RECORDING);
 		registerReceiver(audioFileUpdateReceiver, intentDictRunning);
 
 		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		// The PendingIntent to launch our activity if the user selects this notification
 		Intent i = new Intent(this, OPrimeLib.class);
 		mContentIntent = PendingIntent.getActivity(this, 0, i, 0);
 		
 		int sdk = android.os.Build.VERSION.SDK_INT;
 		Resources res = getApplicationContext().getResources();
 		
 		if(sdk >= 11){
 //			http://stackoverflow.com/questions/6391870/how-exactly-to-use-notificiation-builder
 			Notification.Builder builder = new Notification.Builder(getApplicationContext());
 	
 			builder.setContentIntent(mContentIntent)
 			            .setSmallIcon(mAuBlogIconId)
 			            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_oprime))
			            .setTicker("hi ticker")
 			            .setWhen(System.currentTimeMillis())
 			            .setAutoCancel(true)
 			            .setContentTitle(res.getString(R.string.app_name))
 			            .setContentText(res.getString(R.string.app_name));
 			mNotification = builder.getNotification();
 		}else{
 			mNotification = new Notification(mAuBlogIconId, res.getString(R.string.app_name), System.currentTimeMillis());
 			mNotification.setLatestEventInfo(this, res.getString(R.string.app_name), "Recording...", mContentIntent);
 			mNotification.flags  |= Notification.FLAG_AUTO_CANCEL;
 		}
 	}
 
 
 
 	@Override
 	public void onDestroy() {
 		saveRecording();
 		mNM.cancel(NOTIFICATION);
 		
 		
 		super.onDestroy();
 		if (audioFileUpdateReceiver != null) {
 			unregisterReceiver(audioFileUpdateReceiver);
 		}
 		
 	}
 
 
 
 	@Override
 	public void onLowMemory() {
 		saveRecording();
 		mNM.cancel(NOTIFICATION);
 		
 		super.onLowMemory();
 		if (audioFileUpdateReceiver != null) {
 			unregisterReceiver(audioFileUpdateReceiver);
 		}
 	}
 
 	public class RecordingReceiver extends BroadcastReceiver {
 	    @Override
 	    public void onReceive(Context context, Intent intent) {
 	    	saveRecording();
 	   	}
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		startForeground(startId, mNotification);
 		/*
 		 * get data from extras bundle, store it in the member variables
 		 */
 		try {
 			mAudioResultsFile = intent.getExtras().getString(OPrime.EXTRA_RESULT_FILENAME);
 			
 		} catch (Exception e) {
 			//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
 		}
 		if (mAudioResultsFile == null) {
 			mAudioResultsFile="/sdcard/temp.mp3";
 		}
 		/*
 		 * turn on the recorder
 		 */
 		mRecordingNow = true;
 		mRecorder = new MediaRecorder();
 		try {
 	    	//http://www.benmccann.com/dev-blog/android-audio-recording-tutorial/
 			mRecordingNow = true;
 	    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 	    	mRecorder.setAudioChannels(1); //mono
 	    	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 		    int sdk =  android.os.Build.VERSION.SDK_INT;
 		    // gingerbread and up can have wide band ie 16,000 hz recordings (much better for transcription)
 		    if( sdk >= 10){
 		    	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
 		    	mRecorder.setAudioSamplingRate(16000);
 		    }else{
 		    	// other devices will have to use narrow band, ie 8,000 hz (same quality as a phone call)
 			    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 		    }
 		    mRecorder.setOutputFile(mAudioResultsFile);
 		    mRecorder.prepare();
 		    mRecorder.start();
 		} catch (IllegalStateException e) {
 			
 		} catch (IOException e) {
 			
 		}
 		
 		//autofilled by eclipsereturn super.onStartCommand(intent, flags, startId);
 		// We want this service to continue running until it is explicitly
         // stopped, so return sticky.
 		return START_STICKY;
 	}
 
 	
 
 	private void saveRecording(){
 		String appendToContent ="";
 		if (mRecorder != null) {
 			/*
 			 * if the recorder is running, save everything essentially simulating a click on the save button in the UI
 			 */
 			if(mRecordingNow == true){
 				/*
 				 * Save recording
 				 */
 				mRecordingNow=false;
 				try{
 			   	mRecorder.stop();
 			   	mRecorder.release();
 				}catch (Exception e) {
 					//Do nothing
 				}
 			   	mRecorder = null;
 			   	
 	            
 			}else{
 				//this should not run
 				mRecorder.release(); //this is called in the stop save recording
 	            mRecorder = null;
 			}
 		}
 	}
 	
 
 }
