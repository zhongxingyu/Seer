 package com.jperla.badge;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.Face;
 import android.hardware.Camera.FaceDetectionListener;
 import android.hardware.Camera.PictureCallback;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Surface;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
     private Context context;
     private SurfaceHolder holder;
     private Camera camera;
     private int cameraID;
     private Timer picTimer;
     private static final String LOG_TAG = "----- SurfaceView -----";
 
     public ImageView imageView;
 
     public CameraSurfaceView(Context context) {
         super(context);
         this.context = context;
         init();
     }
 
     public CameraSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.context = context;
         init();
     }
 
     private void init() {
         // Initialize the Surface Holder properly
         this.holder = this.getHolder();
         this.holder.addCallback(this);
         this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         //camera.setFaceDetectionListener(new MyFaceAnalyzer());
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         try {
             // Open the Camera in preview mode
             openFrontCamera();
             this.holder = holder;
             this.camera.setPreviewDisplay(holder);
         } catch(IOException ioe) {
             ioe.printStackTrace(System.out);
         }
 
        picTimer = new Timer();
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         // Now that the size is known, set up the camera parameters and begin
         // the preview.
         Camera.Parameters parameters = camera.getParameters();
         //parameters.setPreviewSize(width, height);
         camera.setParameters(parameters);
         setCameraDisplayOrientation();
         camera.startPreview();
         // camera.startFaceDetection();
 
         picTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 takePicture();
             }
         }, 0, 1000);
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Surface will be destroyed when replaced with a new screen
         picTimer.cancel();
         // camera.stopFaceDetection();
         camera.stopPreview();
         camera.release();
         camera = null;
     }
 
     public Camera getCamera() {
         return this.camera;
     }
 
     public int getCameraID() {
         return this.cameraID;
     }
 
     private void takePicture() {
         camera.takePicture(null, null, new PictureCallback() {
             @Override
             public void onPictureTaken(byte[] data, Camera cam) {
                 Log.d(LOG_TAG, "Took picture");
 
                 Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);
                 imageView.setImageBitmap(bMap);
 
 
                 cam.startPreview();
             }
         });
 
 
     }
 
     private void openFrontCamera() {
         // Open the first front-facing camera 
         android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
         this.cameraID = 0;
         for (int i = 0; i < camera.getNumberOfCameras(); i++) {
             android.hardware.Camera.getCameraInfo(i, info);
             if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                 this.cameraID = i;
                 break;
             }
         }
         this.camera = Camera.open(this.cameraID);
     }
 
     private void setCameraDisplayOrientation() {
         // Keep the camera oriented the same way as the screen
         android.hardware.Camera.CameraInfo info =
                 new android.hardware.Camera.CameraInfo();
         android.hardware.Camera.getCameraInfo(cameraID, info);
         int rotation = ((Activity) context).getWindowManager().
                            getDefaultDisplay().getRotation();
         int degrees = 0;
         switch (rotation) {
             case Surface.ROTATION_0: degrees = 0; break;
             case Surface.ROTATION_90: degrees = 90; break;
             case Surface.ROTATION_180: degrees = 180; break;
             case Surface.ROTATION_270: degrees = 270; break;
         }
         degrees = (degrees + 180) % 360;
 
         int result;
         if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
             result = (info.orientation + degrees) % 360;
             result = (360 - result) % 360;  // compensate the mirror
         } else {  // back-facing
             result = (info.orientation - degrees + 360) % 360;
         }
         camera.setDisplayOrientation(result);
      }
 }
 
 
 class MyFaceAnalyzer implements FaceDetectionListener {
     private static final String LOG_TAG = "----- FaceAnalyzer -----";
 
     @Override
     public void onFaceDetection(Face[] faces, Camera camera) {
         Log.d(LOG_TAG, "Found " + faces.length + " faces.");
     }
 }
