 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.media.ExifInterface;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * This class is inteded to be used to capture an image with the onboard camera and
  * ensure that the returned image is properly rotated
  * 
  * @author Charles Berlin
  *
  */
 public class MGCameraActivity extends Activity {
 
 	/**
 	 * Constant for desired path to saved image
 	 */
 	public final static String IMAGE_FILE_PATH = "IMAGE_FILE_PATH";
 	
 	private Button takePhotoButton;
 	private Button retakePhotoButton;
 	private Button acceptButton;
 	private SurfaceView preview;
 	private SurfaceHolder previewHolder;
 	private Camera camera;
 	private String imagePath;
 	
 	private boolean cameraConfigured = false;
 	private boolean inPreview = false;	
 	private boolean didTakePicture = false;
 	
 	public void onResume()
 	{
 		super.onResume();
 		
 		//if camera is not created, open it
 		if(camera == null)
 			camera = android.hardware.Camera.open();
 		//starts camera preview for activity
 		startPreview();
 	}
 	
 	@Override
 	public void onPause() 
 	{
 		if (inPreview) 
 		{
 			camera.stopPreview();
 		}
 
 		camera.release();
 		camera = null;
 		inPreview = false;
 
 		super.onPause();
 	}
 
 	/**
 	 * If the camera activity is cancelled, result cancelled is returned to the activity
 	 * and anything stored in the temporary file path is deleted
 	 */
 	@Override
 	public void onBackPressed ()
 	{
 		setResult(RESULT_CANCELED, getIntent());
 		deleteImageFile(imagePath);
 		finish();
 	}
 	
 	@Override
 	public void onConfigurationChanged (Configuration newConfig)
 	{
 		super.onConfigurationChanged(newConfig);
 	}
 	
 	public void onCreate(Bundle savedInstanceState)
 	{	
 		super.onCreate(savedInstanceState);
 
		if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(IMAGE_FILE_PATH))
			imagePath = (String) getIntent().getExtras().get(IMAGE_FILE_PATH);
 		else
 		{
 			//create generic temp path to return in bundle
 			imagePath = getExternalCacheDir().getAbsolutePath() +  "/" + String.valueOf(System.currentTimeMillis());
 		}
 		
 		setContentView(R.layout.activity_camera);
 
 		acceptButton = (Button) findViewById(R.id.camera_accept);
 		acceptButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = getIntent();
 				i.putExtra(IMAGE_FILE_PATH, imagePath);
 				setResult(RESULT_OK, i);
 				finish();
 			}
 		});
 		
 		retakePhotoButton = (Button) findViewById(R.id.camera_retake);
 		retakePhotoButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				didTakePicture = false;
 				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
 				
 				retakePhotoButton.setVisibility(View.GONE);
 				acceptButton.setVisibility(View.GONE);
 				takePhotoButton.setVisibility(View.VISIBLE);
 				
 				deleteImageFile(imagePath);
 				
 				startPreview();
 			}
 		});
 
 		takePhotoButton = (Button) findViewById(R.id.camera_button);
 		takePhotoButton.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 								
 				camera.takePicture(null, null, null, new PictureCallback() 
 				{
 
 					@Override
 					public void onPictureTaken(byte[] data, Camera camera) 
 					{
 		
 						FileOutputStream outStream = null;
 						try 
 						{
 							File temp = new File(imagePath);
 							outStream = new FileOutputStream(temp);
 							outStream.write(data);
 							outStream.flush();
 							outStream.close();
 							saveImageOrientation();				
 						} 
 						catch (FileNotFoundException e) 
 						{
 							e.printStackTrace();
 						} 
 						catch (IOException e) 
 						{
 							e.printStackTrace();
 						} 
 						finally 
 						{
 						}
 						didTakePicture = true;
 												
 						int rotation = getWindowManager().getDefaultDisplay().getRotation();
 						switch(rotation) {
 						case Surface.ROTATION_0:
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 							break;
 						case Surface.ROTATION_90:
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 							break;
 						case Surface.ROTATION_180:
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
 							break;
 						case Surface.ROTATION_270:
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
 							break;
 						default:
 							Log.e("PHOTO", "Unknown screen orientation. Defaulting to portrait.");
 							break;              
 						}	
 						
 						retakePhotoButton.setVisibility(View.VISIBLE);
 						acceptButton.setVisibility(View.VISIBLE);
 						takePhotoButton.setVisibility(View.GONE);
 						
 					}
 				});
 			}
 		});			
 
 		preview = (SurfaceView) findViewById(R.id.camera_preview);
 		
 		retakePhotoButton.setVisibility(View.GONE);
 		acceptButton.setVisibility(View.GONE);
 		takePhotoButton.setVisibility(View.VISIBLE);
 		
 		camera = android.hardware.Camera.open();
 		
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 		
 		if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB)
 			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) 
 	{
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) 
 		{
 			if (size.width <= width && size.height <= height) 
 			{
 				if (result == null) 
 				{
 					result = size;
 				}
 				else 
 				{
 					int resultArea=result.width * result.height;
 					int newArea = size.width * size.height;
 
 					if (newArea  > resultArea) 
 					{
 						result = size;
 					}
 				}
 			}
 		}
 
 		return(result);
 	}
 
 	private void initPreview(int width, int height) 
 	{
 		if (camera != null && previewHolder.getSurface() != null) 
 		{
 			camera.stopPreview();
 			setCameraDisplayOrientation(this, 0, camera);
 			
 			try 
 			{
 				camera.setPreviewDisplay(previewHolder);
 			}
 			catch (Throwable t) 
 			{
 			}
 			
 			if (!cameraConfigured) 
 			{
 				Camera.Parameters parameters = camera.getParameters();
 				Camera.Size size = getBestPreviewSize(width, height, parameters);
 
 				if (size != null) 
 				{
 					parameters.setPreviewSize(size.width, size.height);
 					camera.setParameters(parameters);
 					cameraConfigured = true;
 				}
 			}
 			
 		}
 	}
 
 	private void startPreview() 
 	{
 		if (cameraConfigured && camera != null) 
 		{
 			try {
 				camera.startPreview();
 				inPreview = true;
 			} catch (Exception e) {
 				Log.e("APropCameraAR", "", e);
 			}
 
 		}
 	}
 
 	//only get the default/natural orientation of the device, not the actual current orientation
 	private static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) 
 	{
 		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
 		android.hardware.Camera.getCameraInfo(cameraId, info);
 		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
 		int orientation = 0;
 		switch (rotation) 
 		{
 			case Surface.ROTATION_0: orientation = 0; break;
 			case Surface.ROTATION_90: orientation = 90; break;
 			case Surface.ROTATION_180: orientation = 180; break;
 			case Surface.ROTATION_270: orientation = 270; break;
 		}
 
 		int result;
 		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
 		{
 			result = (info.orientation + orientation) % 360;
 			result = (360 - result) % 360;  // compensate the mirror
 		} 
 		else 
 		{  // back-facing
 			result = (info.orientation - orientation + 360) % 360;
 		}
 		
 		camera.setDisplayOrientation(result);
 	}
 
 	
 	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() 
 	{
 		public void surfaceCreated(SurfaceHolder holder) 
 		{
 			// no-op -- wait until surfaceChanged()
 		}
 
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
 		{
 			if(didTakePicture == false)
 			{
 				initPreview(width, height);
 				startPreview();
 			}
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) 
 		{
 		}
 	};
 	
 	//returns current exif orientation of the image being captured, by taking the fixed orientation of the camera and 
 	//subtracting the current device orientation
 	private int getScreenOrientation() {
 	    int rotation = getWindowManager().getDefaultDisplay().getRotation();
 	    DisplayMetrics dm = new DisplayMetrics();
 	    getWindowManager().getDefaultDisplay().getMetrics(dm);
 		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
 		android.hardware.Camera.getCameraInfo(0, info);
 
 		int orientation = 0;
 		switch (rotation) 
 		{
 			case Surface.ROTATION_0: orientation = 0; break;
 			case Surface.ROTATION_90: orientation = 90; break;
 			case Surface.ROTATION_180: orientation = 180; break;
 			case Surface.ROTATION_270: orientation = 270; break;
 		}
 
 		int result;
 		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
 		{
 			result = (info.orientation + orientation) % 360;
 			result = (360 - result) % 360;  // compensate the mirror
 		} else 
 		{  // back-facing
 			result = (info.orientation - orientation + 360) % 360;
 		}
 	    
         switch(result) {
             case 0:
                 orientation = ExifInterface.ORIENTATION_NORMAL;
                 break;
             case 90:
             	orientation = ExifInterface.ORIENTATION_ROTATE_90;
                 break;
             case 180:
                 orientation = ExifInterface.ORIENTATION_ROTATE_180;
                 break;
             case 270:
             	orientation = ExifInterface.ORIENTATION_ROTATE_270;
                 break;
             default:
             	Log.e("PHOTO", "Unknown screen orientation. Defaulting to " +
             			"portrait.");
             	orientation = ExifInterface.ORIENTATION_UNDEFINED;
             	break;              
         }
 		
 	        return orientation;
 	}
 
 	private void saveImageOrientation()
 	{
         ExifInterface exif = null;
         try 
         {
         	exif = new ExifInterface(imagePath);
         } catch (IOException e) 
         {
         	e.printStackTrace();
         }
 
         if(exif != null)
         {
         	exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(getScreenOrientation()));
         	try 
         	{
         		exif.saveAttributes();
         	} catch (IOException e) 
         	{
         		e.printStackTrace();
         	}
         }	
 	}
 	
 	private void deleteImageFile(String path)
 	{
 		File file = new File(path);				
 		if(file != null && file.exists())
 			file.delete();
 	}
 	
 }
