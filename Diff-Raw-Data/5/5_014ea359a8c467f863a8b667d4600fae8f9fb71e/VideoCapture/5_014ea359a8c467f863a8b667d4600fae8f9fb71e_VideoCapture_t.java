 package com.mobvcasting.localreport2012;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VideoCapture extends Activity implements OnClickListener, SurfaceHolder.Callback {
 
 	public static final String LOGTAG = "VIDEOCAPTURE";
 
     private LocationTracker locationTracker;
 
 	public static final int HIGH_QUALITY = 1;
 	public static final int LOW_QUALITY = 0;
 	private int recordingQuality = LOW_QUALITY;
 	
 	public static final int TARGET_WIDTH = 720;
 	public static final int TARGET_HEIGHT = 480;
 	public static final int LOW_TARGET_BITRATE = 700000;
 	public static final int HIGH_TARGET_BITRATE = 1500000;
 	public static final int TARGET_FRAMERATE = 30;
 	
 	//public static final int MAX_FILESIZE = //10 MB
 	
 	int videoWidth = 0;
 	int videoHeight = 0;
 	int videoFramerate = 0;
 	int videoBitrate = LOW_TARGET_BITRATE;
 	int videoEncoder = MediaRecorder.VideoEncoder.H264;
 	int videoSource = MediaRecorder.VideoSource.DEFAULT;
 	int videoFormat = MediaRecorder.OutputFormat.MPEG_4;
 	
 	CamcorderProfile highQualityProfile;
 	
 	private MediaRecorder recorder;
 	private SurfaceHolder holder;
 	//private CamcorderProfile camcorderProfile;
 	private Camera camera;	
 	private TextView countdownText;
 	private Button startButton;
 	private Button cancelButton;
 	
 	private SurfaceView cameraView;
 	
 	private static long RECORD_TIME = 20000;
     private static long ONE_SECOND = 1000;
     
 	boolean recording = false;
 	boolean usecamera = true;
 	boolean previewRunning = false;
 	
 	String filePath = "";
 
     CountDownTimer countDownTimer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		//camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
 		// Check that H.264 is available
 		highQualityProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
 		if (highQualityProfile.videoCodec != videoEncoder) {
         	Toast.makeText(this, "Ut Oh, H.264 isn't available, we don't support your phone", Toast.LENGTH_LONG).show();
         	
         	finish();
         	
         	// Sadly H.263 isn't going to work for us
 		}
 		
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
 	    	//camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
 	    	recordingQuality = HIGH_QUALITY;
 	    	if (MainMenu.TESTING) {
 	    		Toast.makeText(this, "WiFi Enabled, High Quality Recordng", Toast.LENGTH_LONG).show();
 	    	}
 	    } else if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()) {
 	    	recordingQuality = LOW_QUALITY;
 	    	if (MainMenu.TESTING) {
 	    		Toast.makeText(this, "WiFi Not Enabled, Low Quality Recordng", Toast.LENGTH_LONG).show();
 	    	}
 	    } else {
 	    	Toast.makeText(this, "No Network Connection Available, Can Not Record Video", Toast.LENGTH_LONG).show();
 	    	finish();
 	    }
 
 		setContentView(R.layout.activity_video_capture);
 
 		cameraView = (SurfaceView) findViewById(R.id.CameraView);
 		
 		holder = cameraView.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		cameraView.setClickable(true);
 		cameraView.setOnClickListener(this);
 		
 		startButton = (Button) this.findViewById(R.id.StartButton);
 		startButton.setOnClickListener(this);
 		
 		cancelButton = (Button) this.findViewById(R.id.CancelButton);
 		cancelButton.setOnClickListener(this);
 		
 		countdownText = (TextView) this.findViewById(R.id.CountDownTimer);
 		countDownTimer = new CountDownTimer(RECORD_TIME, ONE_SECOND) {
 
 			public void onTick(long millisUntilFinished) {
 		    	 countdownText.setText("Time remaining: " + millisUntilFinished / ONE_SECOND);
 		     }
 
 		     public void onFinish() {
 		    	 stopRecording();
 		    	 countdownText.setText("Recording Complete");
 		     }
 		};

         bindService(new Intent(VideoCapture.this, 
                 LocationTracker.class), locationTrackerConnection, Context.BIND_AUTO_CREATE);        
 		
 	}
 	
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
     }
 
 	private void prepareRecorder() {
         recorder = new MediaRecorder();
 		recorder.setPreviewDisplay(holder.getSurface());
 		
 		if (usecamera) {
 			camera.unlock();
 			recorder.setCamera(camera);
 		}
 		
 		recorder.setVideoSource(videoSource);
 
 		// Not sure I really want to do this as I'll have to implment onInfo or whatever
 		try {
 			recorder.setMaxDuration((int)(RECORD_TIME + ONE_SECOND));
 			//recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
 		} catch (RuntimeException re) {
 			Log.v(LOGTAG,re.getMessage());
 		}
 
 		recorder.setOutputFormat(videoFormat);
 		
 		//Looping through settings in SurfaceChanged
 		System.out.println("Size: " + videoWidth + " " + videoHeight);
 		if (videoWidth > 0) {
 			recorder.setVideoSize(videoWidth, videoHeight);
 
 		} else {
 			// This shouldn't ever happen
 			Log.v(LOGTAG,"Please report an error with setVideoSize");
 			recorder.setVideoSize(TARGET_WIDTH, TARGET_HEIGHT);
 		}
 
 		//Looping through settings in SurfaceChanged
 		System.out.println("Framerate: " + videoFramerate);
 		if (videoFramerate > 0) {
 			recorder.setVideoFrameRate(videoFramerate);
 		} else {
 			recorder.setVideoFrameRate(TARGET_FRAMERATE);
 		}
 		
 		recorder.setVideoEncoder(videoEncoder);
 
 		if (recordingQuality == HIGH_QUALITY) {
 			videoBitrate = HIGH_TARGET_BITRATE;
 		} else {
 			videoBitrate = LOW_TARGET_BITRATE;
 		}
 		recorder.setVideoEncodingBitRate(videoBitrate);
 		
 		try {
 			File newFile = File.createTempFile("localreport", ".mp4", Environment.getExternalStorageDirectory());
 			filePath = newFile.getAbsolutePath();
 			recorder.setOutputFile(filePath);
 
 			recorder.prepare();			
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			finish();
 		} catch (IOException e) {
 			e.printStackTrace();
 			finish();
 		}
 	}
 
 	public void stopRecording() {
 		if (recording) {
 			recorder.stop();
 			/*
 			if (usecamera) {
 				try {
 					camera.reconnect();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			*/			
 			recorder.release();
 			recording = false;
 			Log.v(LOGTAG, "Recording Stopped");
 			// Let's prepareRecorder so we can record again - NOPE
 			//prepareRecorder();
 			
 			// Upload it
 			Intent fileUpIntent = new Intent(this,FileUploader.class);
 			fileUpIntent.putExtra("filePath", filePath);
 			fileUpIntent.putExtra("audio_or_video", "video");
 			fileUpIntent.putExtra("participant_device_id", MainMenu.getUniqueId(this));
 			
 			if (locationTracker.currentLocation != null) {
 				fileUpIntent.putExtra("latitude", ""+locationTracker.currentLocation.getLatitude());
 				fileUpIntent.putExtra("longitude", ""+locationTracker.currentLocation.getLongitude());
 				Log.v(LOGTAG,"MainMenu.currentLocation is " + locationTracker.currentLocation.toString());
 			} else {
 				Log.v(LOGTAG,"MainMenu.currentLocation is null");
 			}
 			
 			startActivity(fileUpIntent);
 			finish();
 		}
 	}
 	
 	public void onClick(View v) {
 		if (v == startButton || v == cameraView) {
 			if (!recording) {
 				recording = true;
 				recorder.start();
 				countDownTimer.start();		    
 				Log.v(LOGTAG, "Recording Started");
 				startButton.setEnabled(false);
 			}
 		} else if (v == cancelButton) {
 			// I think surfacedestroyed will take care of deinitalization
 			finish();
 		}
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.v(LOGTAG, "surfaceCreated");
 		
 		if (usecamera) {
 			camera = Camera.open();
 			
 			Camera.Parameters cParameters = camera.getParameters();
 			
 			List<Size> supportedSizes = cParameters.getSupportedPreviewSizes();
 			Iterator<Size> supportedSizesI = supportedSizes.iterator();
 			int currentDiff = Integer.MAX_VALUE;
 			while (supportedSizesI.hasNext()) {
 				Size cSize = supportedSizesI.next();
 				System.out.println("Supports: " + cSize.width + " " + cSize.height);
 				//if (cSize.width <= TARGET_WIDTH && TARGET_WIDTH - cSize.width < TARGET_WIDTH - videoWidth)
 				int testDiff = Math.abs(TARGET_WIDTH - cSize.width) + Math.abs(TARGET_HEIGHT - cSize.height);
 				if (testDiff < currentDiff) 
 				{
 					currentDiff = testDiff;
 					videoWidth = cSize.width;
 					videoHeight = cSize.height;
 					System.out.println("Using");
 				}
 			}
 		    if (videoWidth > 0) {
 		    	cParameters.setPreviewSize(videoWidth, videoHeight);
 				Log.v(LOGTAG,"Preview Size: " + videoWidth + " " + videoHeight);
 		    }
 			
 			List<Integer> supportedFramerates = cParameters.getSupportedPreviewFrameRates();
 			Iterator<Integer> supportedFrameratesI = supportedFramerates.iterator();
 			while (supportedFrameratesI.hasNext()) {
 				Integer cFramerate = supportedFrameratesI.next();
 				System.out.println("Supports: " + cFramerate);
 				if (cFramerate <= TARGET_FRAMERATE && TARGET_FRAMERATE - cFramerate < TARGET_FRAMERATE - videoFramerate) {
 					videoFramerate = cFramerate;
 					System.out.println("Using");
 				}
 			}
 			if (videoFramerate > 0) {
 		    	cParameters.setPreviewFrameRate(videoFramerate);
 		    	Log.v(LOGTAG,"Framerate: " + videoFramerate);
 			}
 
 		    camera.setParameters(cParameters);
 		    
 		    cameraView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth,videoHeight));
 			
 			try {
 				camera.setPreviewDisplay(holder);
 				camera.startPreview();
 				previewRunning = true;
 				
 				
 				prepareRecorder();	
 
 			}
 			catch (IOException e) {
 				Log.e(LOGTAG,e.getMessage());
 				e.printStackTrace();
 			}	
 		}		
 		
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		Log.v(LOGTAG, "surfaceChanged");
 	}
 	
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.v(LOGTAG, "surfaceDestroyed");
 		if (recording) {
 			recorder.stop();
 			recording = false;
 		}
 		recorder.release();
 		if (usecamera) {
 			previewRunning = false;
 			camera.lock();
 			camera.release();
 		}
 	}
 	
 	public void onDestroy() {
 		unbindService(locationTrackerConnection);
 		super.onDestroy();
 	}
 	
 	
 	private ServiceConnection locationTrackerConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             // This is called when the connection with the service has been
             // established, giving us the service object we can use to
             // interact with the service.  Because we have bound to a explicit
             // service that we know is running in our own process, we can
             // cast its IBinder to a concrete class and directly access it.
             locationTracker = ((LocationTracker.LocalBinder)service).getService();
 
             if (MainMenu.TESTING) {
                 // Tell the user about this for our demo.
             	Toast.makeText(VideoCapture.this, "Connected", Toast.LENGTH_SHORT).show();
             }
         }
 
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been
             // unexpectedly disconnected -- that is, its process crashed.
             // Because it is running in our same process, we should never
             // see this happen.
         	locationTracker = null;
         	
         	if (MainMenu.TESTING) {
         		Toast.makeText(VideoCapture.this, "Disconnected", Toast.LENGTH_SHORT).show();
         	}
         }
     };	
 }
