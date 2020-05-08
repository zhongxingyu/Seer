 package com.hipsterrific.app;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Point;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.RotateAnimation;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.hipsterrific.R;
 import com.hipsterrific.FaceDetector.BitmapHelper;
 
 public class CameraPreviewActivity extends Activity implements SensorEventListener {
 
 	private static final String TAG = "CameraPreviewer";
 
 	private Camera camera;
 	private CameraPreviewView previewView;
 	private LayoutInflater buttonsInflater = null;
 	private int cameraNumber;
 	private SensorManager orientationSensorManager;
 	
 	private int orientation;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().containsKey("cameraNumber")) {
 			this.cameraNumber = this.getIntent().getExtras().getInt("cameraNumber");
 		}
 		else {
 			this.cameraNumber = this.findBackFacingCamera();
 		}
 		this.initializeCamera();
 
 		// Setup layout
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		
 		// Setup views
 		this.setContentView(R.layout.activity_camera_previewer);
 		
 		this.createFillerViews();
 		this.createPreviewView();
 		this.createButtonsOverlay();
 		
 		this.startOrientationSensors(this);
 	}
 
 	public void initializeCamera() {
 		if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
 			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
 			return;
 		}
 		// Create an instance of Camera
 		try {
 			this.camera = Camera.open(this.cameraNumber); // attempt to get a Camera instance
 		} catch (Exception e) {
 			Toast.makeText(this, "Camera " + this.cameraNumber + " not found", Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 
     @SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
     private void createFillerViews() {
     	Display display = this.getWindowManager().getDefaultDisplay();
 
     	int displayWidth;
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) { 
 	        displayWidth = display.getWidth(); 
 		}
 		else {
 	        Point size = new Point();
 	        display.getSize(size);
 	        displayWidth = size.x;
 		}
 		
 		Camera.Parameters cameraParameters = this.camera.getParameters();
         List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
         Camera.Size previewSize = previewSizes.get(0);
         
         if (displayWidth > previewSize.width) {
             int fillerWidth = (displayWidth - previewSize.width) / 2;
             ImageView leftFiller = (ImageView)this.findViewById(R.id.img_left);
             ImageView rightFiller = (ImageView)this.findViewById(R.id.img_right);
             leftFiller.getLayoutParams().width = fillerWidth;
             rightFiller.getLayoutParams().width = fillerWidth;
         }
     }
     
     private void createPreviewView() {
 		this.previewView = new CameraPreviewView(this, this.camera);
 		FrameLayout previewLayout = (FrameLayout)this.findViewById(R.id.camera_preview);
 		previewLayout.addView(this.previewView);
     }
 
 	/**
 	 * Creates the overlay with control buttons for the preview.
 	 */
 	public void createButtonsOverlay() {
 		this.buttonsInflater = LayoutInflater.from(this.getBaseContext());
 		View buttonsView = this.buttonsInflater.inflate(R.layout.control, null);
 		LayoutParams buttonsLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		this.addContentView(buttonsView, buttonsLayout);
 	}
 
 	/**
 	 * @param view
 	 *            Take a photo.
 	 */
 	public void takePhoto(View view) {
 		Log.d("MakePhoto", "Orientation angle: " + this.orientation);
 		
 		Camera.Parameters params = this.camera.getParameters();
 		
 		int rotation = this.orientation;
 		if (this.orientation == 270) rotation = 90;
 		if (this.orientation == 90)  rotation = 270;
 		params.setRotation(rotation);
 		
         this.camera.setParameters(params);
 		this.camera.takePicture(null, null, pictureCallback);
 	}
 
 	/**
 	 * @param view
 	 *            Close activity and go back to main menu.
 	 */
 	public void goBack(View view) {
 		NavUtils.navigateUpFromSameTask(this);
 	}
 
 	/**
 	 * @param view
 	 *            Restarts the activity with other camera, or backcamera.
 	 */
 	public void switchCamera(View view) {		
 		if (Camera.getNumberOfCameras() < 2)
 			return;
 		
 		int newCameraNumber;
 		if (this.cameraNumber == this.findBackFacingCamera()) {
 			newCameraNumber = this.findOtherCamera(this.cameraNumber);
 		} 
 		else {
 			newCameraNumber = this.findBackFacingCamera();
 		}
 		
 		if (newCameraNumber == this.cameraNumber)
 			return;
 
 		// This should really be in onDestroy
 		if (this.camera != null) {
 			this.camera.release();
 			this.camera = null;
 		}
 
 		Intent intent = this.getIntent();
 		
 		Bundle bundle = new Bundle();
 		bundle.putInt("cameraNumber", newCameraNumber);
 		intent.putExtras(bundle);
 		
 		this.finish();
 		this.startActivity(intent);
 	}
 
 	/*
 	 * The camera an sensor aren't recreated in onResume, so when locking/unlocking the phone this activity no longer does anything. 
 	 */
 	@Override
 	protected void onPause() {
 		if (this.camera != null) {
 			this.camera.release();
 			this.camera = null;
 		}
 		
 		if (this.orientationSensorManager != null) {
 			this.orientationSensorManager.unregisterListener(this);
 		}
 		
 		super.onPause();
 	}
 
 	/**
 	 * @param tempFileName
 	 *            Start new display activity, send file path with intent.
 	 */
 	private void startDisplayImageActivity(String tempFileName) {
 	 	Toast.makeText(this, "New Image saved: " + tempFileName, Toast.LENGTH_LONG).show();
 	 	 
 		final Intent intent = new Intent(this, DisplayImageActivity.class);
 		
 		intent.setData(Uri.parse(tempFileName));
 		intent.putExtra("orientation", this.orientation);
 		
 		startActivity(intent);
 	}
 
 	/**
 	 * If camera shutter button gets pressed create new File and save current
 	 * camera image to file.
 	 */
 	private PictureCallback pictureCallback = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] pictureData, Camera camera) {
 			File pictureFile = FileHelper.getOutputMediaFile(getString(R.string.image_directory));
 			if (pictureFile == null) {
 				Log.d(TAG, "Error creating media file, check storage permissions");
 				return;
 			}
 
 			try {
 				FileOutputStream fos = new FileOutputStream(pictureFile);
 				fos.write(pictureData);
 				fos.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String path = pictureFile.getPath();			
 			try {
 				int rotation = BitmapHelper.determineRotation(path);
 
 				if (rotation != 0) 
 					BitmapHelper.saveToFile(
 							BitmapHelper.getRotatedFor(
 									BitmapHelper.fromFile(pictureFile), 
 									rotation), 
 							pictureFile);
 			} catch (IOException e) {}
 			
 			startDisplayImageActivity(path);
 		}
 	};
 
 	/**
 	 * @return camera number
 	 *            Find the main camera facing back, sends back camera number.
 	 */
 	private int findBackFacingCamera() {
 		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
 			CameraInfo info = new CameraInfo();
 			Camera.getCameraInfo(i, info);
 			
 			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
 				Log.d(TAG, "Camera found back");
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * @param other
 	 * @return other camera number 
 	 *            Find other camera then camera 'other', sends
 	 *            back camera number.
 	 */
 	private int findOtherCamera(int other) {
 		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
 			if (i != other) {
 				Log.d(TAG, "Camera found other");
 				return i;
 			}
 		}
 		return other;
 	}
 
 	private void startOrientationSensors(Context context) {
 		this.orientationSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
 		this.orientationSensorManager.registerListener(this,
 				this.orientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_UI);
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			float x = event.values[0];
 			float y = event.values[1];
 			float z = event.values[2];
 			
 			int newOrientation = this.orientation;
 
 			if (newOrientation != 270 && Math.abs(x) < Math.abs(y) && z > -8 && z < 8
 					&& y > 0) {
 				newOrientation = 270;
 			}
 			else if (newOrientation != 0 && Math.abs(x) > Math.abs(y) && z > -8 && z < 8
 					&& x > 0) {
 				newOrientation = 0;
 			}
 			else if (newOrientation != 90 && Math.abs(x) < Math.abs(y) && z > -8 && z < 8
 					&& y < 0) {
 				newOrientation = 90;
 			}
 			else if (newOrientation != 180 && Math.abs(x) > Math.abs(y) && z > -8 && z < 8
 					&& x < 0) {
 				newOrientation = 180;
 			}
 			
 			if (newOrientation != this.orientation) {
 				this.rotateButtons(newOrientation);
 			}
 		}
 	}
 
 	private void rotateButtons(int newOrientation) {
 		int oldOrientation = this.orientation;
		this.orientation = newOrientation;
 		if (oldOrientation == 0 && newOrientation == 270)
 			oldOrientation = 360;
 		if (oldOrientation == 270 && newOrientation == 0)
 			newOrientation = 360;
 
 		ImageButton switchCameraButton = (ImageButton) this.findViewById(R.id.button_diffcam);
 		ImageButton backButton = (ImageButton) this.findViewById(R.id.button_back);
 		ImageButton captureButton = (ImageButton) this.findViewById(R.id.button_cap);
 		Animation animation = new RotateAnimation(oldOrientation, newOrientation, Animation.RELATIVE_TO_SELF,
 				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
 		animation.setFillAfter(true);
 		animation.setFillEnabled(true);
 		animation.setDuration(300);
 		
 		switchCameraButton.setAnimation(animation);
 		backButton.setAnimation(animation);
 		captureButton.setAnimation(animation);
 		
 		captureButton.startAnimation(animation);
 	}
 }
