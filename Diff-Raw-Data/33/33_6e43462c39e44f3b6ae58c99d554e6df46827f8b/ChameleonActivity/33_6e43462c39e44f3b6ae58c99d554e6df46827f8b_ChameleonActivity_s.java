 package orbotix.uisample;
 
 import java.io.IOException;
 
 import orbotix.robot.app.ColorPickerActivity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 
 /**********
  * 
  * @author mattshapiro
  *
  * Use the camera to scan image data for an average RGB color value and set the sphero to that color
  * Lots of code here is originally from http://developer.android.com/guide/topics/media/camera.html
  * 
  */
 
 public class ChameleonActivity extends Activity{
 
 	private Camera mCamera;
     private CameraPreview mPreview;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        
         setContentView(R.layout.chameleon);
         // Create an instance of Camera
         mCamera = getCameraInstance();
        
         // Create our Preview view and set it as the content of our activity.
         mPreview = new CameraPreview(this);
         FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
         preview.addView(mPreview);
         
      // Add a listener to the Capture button
         Button captureButton = (Button) findViewById(R.id.button_capture);
         captureButton.setOnClickListener(
             new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     // get an image from the camera
                     mCamera.takePicture(null, mPicture, null);
                 }
             }
         );
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         releaseCamera();              // release the camera immediately on pause event
     }
 
     private void releaseCamera(){
         if (mCamera != null){
             mCamera.release();        // release the camera for other applications
             mCamera = null;
         }
     }
 
 	
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance(){
 	    Camera c = null;
 	    try {
 	        c = Camera.open(); // attempt to get a Camera instance
 	    }
 	    catch (Exception e){
 	        // Camera is not available (in use or does not exist)
 	    	System.out.println(e.getStackTrace());
 	    }
 	    return c; // returns null if camera is unavailable
 	}
 	
 	/** A basic Camera preview class */
 	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 	    private SurfaceHolder mHolder;
 	    
 	    public CameraPreview(Context context) {
 	        super(context);
 	        // Install a SurfaceHolder.Callback so we get notified when the
 	        // underlying surface is created and destroyed.
 	        mHolder = getHolder();
 	        mHolder.addCallback(this);
 	        // deprecated setting, but required on Android versions prior to 3.0
 	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	    }
 
 	    public void surfaceCreated(SurfaceHolder holder) {
 	        // The Surface has been created, now tell the camera where to draw the preview.
 	        try {
 	            mCamera.setPreviewDisplay(holder);
 	            mCamera.startPreview();
 	        } catch (IOException e) {
 	            Log.d(this.getClass().getName(), "Error setting camera preview: " + e.getMessage());
 	        }
 	    }
 
 	    public void surfaceDestroyed(SurfaceHolder holder) {
 	        // empty. Take care of releasing the Camera preview in your activity.
 	    }
 
 	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 	        // If your preview can change or rotate, take care of those events here.
 	        // Make sure to stop the preview before resizing or reformatting it.
 
 	        if (mHolder.getSurface() == null){
 	          // preview surface does not exist
 	          return;
 	        }
 
 	        // stop preview before making changes
 	        try {
 	            mCamera.stopPreview();
 	        } catch (Exception e){
 	          // ignore: tried to stop a non-existent preview
 	        }
 
 	        // set preview size and make any resize, rotate or
 	        // reformatting changes here
 
 	        // start preview with new settings
 	        try {
 	            mCamera.setPreviewDisplay(mHolder);
 	            mCamera.startPreview();
 
 	        } catch (Exception e){
 	            Log.d(getClass().getName(), "Error starting camera preview: " + e.getMessage());
 	        }
 	    }
 	}
 	
 	private PictureCallback mPicture = new PictureCallback() {
 
 	    @Override
 	    public void onPictureTaken(byte[] data, Camera camera) {
 
 	    	// scan an interior ring of the data for color values...
 	    	// leave out the center in case sphero is there
 	    	/**
 	    	 *   00000000
 	    	 *   00000000
 	    	 *   00111100
 	    	 *   00100100
 	    	 *   00111100
 	    	 *   00000000
 	    	 *   00000000
 	    	 */
 	        
 	        Intent result_intent = new Intent();
 	        int red = 0, green = 0, blue = 0;
 			result_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, red);
 			result_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, green);
 			result_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, blue);
 			setResult(RESULT_OK, result_intent);
 			finish();
 	    }
 	};
 }
