 /*
  * Draw camera preview to the screen
  */
 package com.slsw.fiarworks;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.slsw.fiarworks.firework.GLRenderer;
 
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
     private SurfaceHolder mHolder;
     private Camera mCamera;
     private String TAG = "CameraPreview";
     private Context context;
 	private float[] mRotVec;
 	private float[] mRotOld;
 	private byte[][] mask = null;
 	public GLRenderer mRenderer;
 	public volatile boolean sendBitmap;
 
     @SuppressWarnings("deprecation")
     public CameraPreview(Context context, Camera camera, GLRenderer renderer) {
     super(context);
     mCamera = camera;
     this.context = context;
 
     // Install a SurfaceHolder.Callback so we get notified when the
     // underlying surface is created and destroyed.
     mHolder = getHolder();
     // deprecated setting, but required on Android versions prior to 3.0
     mHolder.addCallback(this);
     mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	sendBitmap=true;
 	mRenderer=renderer;
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
     // The Surface has been created, now tell the camera where to draw the preview.
     try {
         mCamera.setPreviewDisplay(holder);
         mCamera.startPreview();
 
     } catch (NullPointerException e) {
         Log.d(TAG, "Error setting camera preview - nullpointerexception: " + e.getMessage());
     } catch (IOException e) {
         Log.d(TAG, "Error setting camera preview: " + e.getMessage());
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
         Parameters parameters = mCamera.getParameters();
 
         List<Size> sizes = parameters.getSupportedPreviewSizes();
         Size optimalSize = getOptimalPreviewSize(sizes, w, h);
         parameters.setPreviewSize(optimalSize.width, optimalSize.height);
 
         if (context.getPackageManager().hasSystemFeature("android.hardware.camera.autofocus"))
             parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
 
         mCamera.setParameters(parameters);
 
         mCamera.setPreviewCallback((PreviewCallback) context);
         mCamera.setPreviewDisplay(mHolder);
         mCamera.startPreview();
 
     } catch (Exception e){
         Log.d(TAG, "Error starting camera preview: " + e.getMessage());
     }
     }
 
     private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
     final double ASPECT_TOLERANCE = 0.05;
     double targetRatio = (double) w / h;
     if (sizes == null) return null;
 
     Size optimalSize = null;
     double minDiff = Double.MAX_VALUE;
 
     int targetHeight = h;
 
     // Try to find an size match aspect ratio and size
     for (Size size : sizes) {
         double ratio = (double) size.width / size.height;
         if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
         if (Math.abs(size.height - targetHeight) < minDiff) {
             optimalSize = size;
             minDiff = Math.abs(size.height - targetHeight);
         }
     }
 
     // Cannot find the one match the aspect ratio, ignore the requirement
     if (optimalSize == null) {
         minDiff = Double.MAX_VALUE;
         for (Size size : sizes) {
             if (Math.abs(size.height - targetHeight) < minDiff) {
                 optimalSize = size;
                 minDiff = Math.abs(size.height - targetHeight);
             }
         }
     }
     return optimalSize;
     }
     
     @Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		System.err.println(Arrays.toString(event.values));
 		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
 			//Update rotation and position vector
 			mRotVec = event.values;
 		}
 	}
 	
 	public float[] changeInRot(){
 		if (mRotOld == null) {
 			mRotOld = mRotVec.clone();
 		}
 		float[] angleChange = new float[]{0f,0f,0f};
 		float[] rotVec = new float[9];
 		float[] rotOld = new float[9];
 		SensorManager.getRotationMatrixFromVector(rotVec, mRotVec);
 		SensorManager.getRotationMatrixFromVector(rotOld, mRotOld);
 		SensorManager.getAngleChange (angleChange, rotVec, rotOld);
 		mRotOld=mRotVec;
 		return angleChange;
 		
 	}
 }
