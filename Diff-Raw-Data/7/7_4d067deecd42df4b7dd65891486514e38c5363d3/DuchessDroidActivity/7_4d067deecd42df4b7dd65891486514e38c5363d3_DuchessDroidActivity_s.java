 package org.duchessfrance.duchessdroid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 
 public class DuchessDroidActivity extends Activity {
 	private static final String TAG = DuchessDroidActivity.class.getSimpleName();
 
 	DrawView mDrawView;
 	PreviewView mPreviewView;
 	Duchess duchess;
  
     private static SensorManager sensorManager;
     private OrientationSensorEventListener osel;
     
     private GestureDetector gestureDetector;
     private View.OnTouchListener gestureListener;
 
 
 /*    CameraPreview mCameraPreview;
 
     public static final int MEDIA_TYPE_IMAGE = 1;
     public static final int MEDIA_TYPE_VIDEO = 2;
     private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
     private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
     private Uri fileUri;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         //create new Intent
         Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 
         fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
         intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
 
         intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
 
         // start the Video Capture Intent
         startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
     }
 */
     
     /** Called when the activity is first created. */
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
 
     	Log.d(TAG, "Entering onCreate");
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     	setContentView(R.layout.main);
 
     	Display display = getWindowManager().getDefaultDisplay();
     	Point size = new Point();
     	display.getSize(size);
     	int width = size.x;
     	int height = size.y;
     	
     	setupSensor();
     	
     	// create duchess and load bitmap
     	duchess = new Duchess(
     			BitmapFactory.decodeResource(getResources(), R.drawable.duchessfr), 
     			width*2/3, height*2/3); //TODO actual size of the screen
 
 
     	mDrawView = (DrawView) findViewById(R.id.draw_view);
     	mDrawView.duchess = duchess;
     	
     	mPreviewView = (PreviewView) findViewById(R.id.preview_view);
     	mPreviewView.mTargetView = mDrawView;
     	
     	
         // Gesture detection
     	/*
         gestureDetector = new GestureDetector(new ScaleListener());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
             	Log.d(TAG, "received gesture in activity");
                 return gestureDetector.onTouchEvent(event);
             }
         };*/
 
         final Button button = (Button) findViewById(R.id.save_button);
     }
 
 
     /** Register for the updates when Activity is in foreground */
     @Override
     protected void onResume() {
     	super.onResume();
     	Log.d(TAG, "onResume");
     	setupSensor(); 
     }
 
     /** Stop the updates when Activity is paused */
     @Override
     protected void onPause() {
     	super.onPause();
     	Log.d(TAG, "onPause");
     	sensorManager.unregisterListener(osel);
     }
 
     @Override
     protected void onDestroy() {
     	// TODO Auto-generated method stub
     	super.onDestroy();
     	sensorManager.unregisterListener(osel);
     } 
     
     public void whenSave(View view) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.dialog_saved_message);
         builder.setTitle(R.string.dialog_saved_title);
         builder.setNeutralButton(R.string.button_text_ok, 
         		new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		               // User cancelled the dialog
 		           }
        });       
        AlertDialog dialog = builder.create();
        dialog.show();
     }
 
     public void whenScaleDown(View view) {
     	float currentScale = duchess.getScale();
    	if (currentScale>2) {
    		duchess.setScale(currentScale - 1);
     		mDrawView.invalidate();
     	}
     }
 
     public void whenScaleUp(View view) {
     	float currentScale = duchess.getScale();
     	if (currentScale<4) {
    		duchess.setScale(currentScale + 1);
     		mDrawView.invalidate();
     	}
     }
 
     public void whenRotate(View view) {
 //TODO
     }
 
     public void whenDrag(View view) {
 //TODO
     }
 
     // unused
     private void setupSensor() {
     	sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
     	sensorManager.registerListener(osel, 
     			sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), // Anonymous Sensors- no further use for them. 
     			SensorManager.SENSOR_DELAY_UI); 
     	sensorManager.registerListener(osel, 
     			sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
     			SensorManager.SENSOR_DELAY_UI); 
     }
     
 
 }
