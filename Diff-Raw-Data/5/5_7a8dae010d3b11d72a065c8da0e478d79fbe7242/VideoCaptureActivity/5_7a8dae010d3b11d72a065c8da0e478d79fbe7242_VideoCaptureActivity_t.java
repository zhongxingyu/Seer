 package org.servalproject.svd.camerasimple;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class VideoCaptureActivity extends Activity implements OnClickListener,
 		SurfaceHolder.Callback, MediaRecorder.OnInfoListener {
 
 	MediaRecorder recorder;
 	SurfaceHolder holder;
 
 	private boolean recording = false;
 	public static final String TAG = "SPCA";
 	private static final String BASE_PATH = "/sdcard/";
 	private static final String BASE_NAME = "video";
 
 	private int currentChunkId = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// No title
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// Full screen landscape
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		// Init recorder
 		recorder = new MediaRecorder();
 		initRecorder();
 
 		setContentView(R.layout.main);
 
 		// Set up the surface
 		SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
 		holder = cameraView.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		// Clickable and loopback to the class listener
 		cameraView.setClickable(true);
 		cameraView.setOnClickListener(this);
 
 		// Define as the event listener for max duration
 		recorder.setOnInfoListener(this);
 	}
 
 	/**
 	 * Set up the recorder with default input and a highest definition available
 	 */
 	private void initRecorder() {
 		// Sources
 		recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
 		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
 		// HD
 		CamcorderProfile highProfile = CamcorderProfile
 				.get(CamcorderProfile.QUALITY_HIGH);
 		recorder.setProfile(highProfile);
 		recorder.setOutputFile(createFilePath());
 
 		// Lenght max
 		recorder.setMaxDuration(10000); // Set max duration 60 sec.
 	}
 
 	/**
 	 * Generate a name for the file including the current chunk id, and based on
 	 * the {@link VideoCaptureActivity#BASE_PATH} and the base name
 	 * {@link VideoCaptureActivity#BASE_NAME}
 	 * 
 	 * @return the path
 	 */
 	private String createFilePath() {
		return new String(BASE_PATH + BASE_NAME + "-" + currentChunkId++
				+ ".mp4");
 	}
 
 	private void prepareRecorder() {
 		recorder.setPreviewDisplay(holder.getSurface());
 		try {
 			recorder.prepare();
 			Log.v(TAG, "Prepare finished");
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			yellInPain();
 		} catch (IOException e) {
 			e.printStackTrace();
 			yellInPain();
 		}
 
 	}
 
 	/**
 	 * Manage the on / off switch
 	 */
 	@Override
 	public void onClick(View v) {
 		if (recording) { // Switch off
 			recorder.stop();
 			recorder.reset();
 			recorder.release();
 			recording = false;
 			Log.v(TAG, "Recording Stopped");
 			initRecorder();
 			prepareRecorder();
 		} else { // Switch on
 			recording = true;
 			recorder.start();
 			Log.v(TAG, "Recording Started");
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.v(TAG, "The surface has been created");
 		prepareRecorder();
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.v(TAG, "surface Destroyed Called -- stop recording");
 		if (recording) {
 			recorder.stop();
 			recording = false;
 		}
 		recorder.release();
 		finish();
 	}
 
 	private void yellInPain() {
 		Log.e(TAG, "Quitting cause something is WRONG!");
 		finish();
 	}
 
 	@Override
 	public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
 		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
 			Log.v(TAG, "Max duration reached !");
 			recorder.stop();
 			recording = false;

 		} else {
 			Log.e(TAG, "Media Recorder sent an unknown event... Not good.");
 		}
 	}
 
 }
