 package com.sniper.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.hardware.Camera.PictureCallback;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Display;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
 
 import com.parse.ParseUser;
 import com.sniper.ActivityLogin;
 import com.sniper.ActivityMain;
 import com.sniper.CrosshairsView;
 
 
 public class Camera extends CrosshairsView implements SurfaceHolder.Callback  {
 	private static final String TAG = "Camera Error";
 	public static Context context;
 	
 	protected static final int MEDIA_TYPE_IMAGE = 1;
 	private static final int MEDIA_TYPE_VIDEO = 2;
 	
 	private SurfaceHolder mHolder;
     public static android.hardware.Camera mCamera;
     private WindowManager mWindowManager;
 
     public Camera(Context context) {
         super(context);
         Log.d("camera", "end super");
         Camera.context = context;
         
         RefreshCamera();
         mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
         
         mHolder = getHolder();
         mHolder.addCallback(this);
         Log.d("camera", "done");
     }
         
     public static void TakePicture(){
     	mCamera.takePicture(null, null, mPicture);
 		mCamera.startPreview();
     }
     
     private static PictureCallback mPicture = new PictureCallback() {
 
 	    @Override
 	    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
 
 	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 	        if (pictureFile == null){
 	            Log.d(TAG, "Error creating media file");
 	            return;
 	        }
 
 	        try {
 	            FileOutputStream fos = new FileOutputStream(pictureFile);
 	            fos.write(data);
 	            fos.close();
 	        } catch (FileNotFoundException e) {
 	            Log.d(TAG, "File not found: " + e.getMessage());
 	        } catch (IOException e) {
 	            Log.d(TAG, "Error accessing file: " + e.getMessage());
 	        }
 	        
 	        Method method = null;
 			try
 			{
 				method = Camera.class.getMethod("receiveResponse", String.class);
 			} catch (NoSuchMethodException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	        
 			// don't need a million testing copies on aws
 			//String title = ParseUser.getCurrentUser().getUsername()+"_";
 			//title += new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	        ApplicationServices.getInstance().uploadKillPhoto(pictureFile, "test", method);
 	    }
 	};
 	
 	public static void receiveResponse(String response)
 	{
<<<<<<< HEAD
 
 		Log.v("Debug", "receiveResponse");
 		//IMPORTANT: THIS IS NOT SETTING THE TARGET
 
=======
/*
		Log.v("Debug", "receiveResponse");
		//IMPORTANT: THIS IS NOT SETTING THE TARGET
*/
>>>>>>> 9bcf8ad5ec59cf49323214d9e43425ba7ff97a29
 		KillAction kill = new KillAction();
 		kill.setPhotoURL(response);
 		kill.setTarget(ActivityMain.target.getObjectId());
 		kill.push();
 		//kill.url = response;
 		//kill.publish();
 	}
 	
 	public static File getImageFile(){
 		return getOutputMediaFile(MEDIA_TYPE_IMAGE);	
 	}
 	
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type){
 	    // To be safe, you should check that the SDCard is mounted
 	    // using Environment.getExternalStorageState() before doing this.
 
 	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
 	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
 	    // This location works best if you want the created images to be shared
 	    // between applications and persist after your app has been uninstalled.
 
 	    // Create the storage directory if it does not exist
 	    if (! mediaStorageDir.exists()){
 	        if (! mediaStorageDir.mkdirs()){
 	            Log.d("MyCameraApp", "failed to create directory");
 	            return null;
 	        }
 	    }
 
 	    // Create a media file name
 	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	    File mediaFile;
 	    if (type == MEDIA_TYPE_IMAGE){
 	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	        "IMG_"+ timeStamp + ".jpg");
 	    } else if(type == MEDIA_TYPE_VIDEO) {
 	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	        "VID_"+ timeStamp + ".mp4");
 	    } else {
 	        return null;
 	    }
 
 	    return mediaFile;
 	}
     
     private void RefreshCamera(){
     	try{
     		// try to open camera
     		mCamera = android.hardware.Camera.open();
     	}catch(Exception e){
     		// camera was already open
     	}
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
     	RefreshCamera();
     	setWillNotDraw(false);
         try {
             mCamera.setPreviewDisplay(holder);
             mCamera.startPreview();
         } catch (IOException e) {
             Log.d(TAG, "Error setting camera preview: " + e.getMessage());
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
     	mCamera.release();
     }
     
     private void SetOrientation(){
     	if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
         	mCamera.setDisplayOrientation(90);
         }        
         else{
         	Display mDisplay = mWindowManager.getDefaultDisplay();
         	int rot = mDisplay.getRotation();
         	if(!(rot == Surface.ROTATION_0 || rot == Surface.ROTATION_90)){
         		mCamera.setDisplayOrientation(180);
         	}
         } 
     }
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         RefreshCamera();
     	
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
 
         SetOrientation(); 
 
         // start preview with new settings
         try {
             mCamera.setPreviewDisplay(mHolder);
             mCamera.startPreview();
 
         } catch (Exception e){
             Log.d(TAG, "Error starting camera preview: " + e.getMessage());
         }
     }    
 }
