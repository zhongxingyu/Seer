 package edu.berkeley.cellscope.cscore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class CameraActivity extends Activity {
 	//PhotoSurface mSurfaceView; 
 	SurfaceView mSurfaceView;
 	SurfaceHolder mHolder;
 	Camera mCamera;
 	MediaRecorder recorder;
 	boolean previewRunning;
 	Activity activity;
 	TextView zoomText;
 	float pY;
 	boolean videoState; //false for camera, true for video
 	boolean recording;
 	ImageButton takePhoto;
 	ImageButton switchMode;
 	// On create the surface view
 	 public static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
 	 public static File videoStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyVideoApp");
 	 static {
 		 if (!mediaStorageDir.exists())
			 mediaStorageDir.mkdirs();
 		 if (!videoStorageDir.exists())
			 videoStorageDir.mkdirs();
 	 }
     /*
      * surfaceChanged() is automatically called whenever the screen changes,
      * including when the app is started.
      * Currently, the app is set so that it starts in portrait mode and cannot
      * switch to landscape, so this is only called once at the start.
      * 
      * This method sets the camera to display the preview on mSurfaceView,
      * sets the preview to the appropriate size,
      * and starts the preview.
      */
     SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
 		public void surfaceCreated(SurfaceHolder holder) {}
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			System.out.println("Surface changed...");
 			if (previewRunning)
 				stopCameraPreview();
 			Display display = ((WindowManager)(activity.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
 			Camera.Parameters parameters = mCamera.getParameters();
 		    Camera.Size mPreviewSize = getPreviewSize(parameters, width, height);
 		    System.out.println(parameters);
 		  //  parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
 			try {
 				int orientation = display.getRotation();
 				int rotation = 0;
 				switch (orientation) {
 					case Surface.ROTATION_0:
 						//parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
 						rotation = 90;
 						break;
 					case Surface.ROTATION_180:
 						//parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
 						rotation = 270;
 						break;
 					case Surface.ROTATION_270:
 						//parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
 						rotation = 180;
 						break;
 					case Surface.ROTATION_90:
 						System.out.println("case 4");
 						rotation = 0;
 						break;
 				}
 				mCamera.setDisplayOrientation(rotation);
 				mCamera.setParameters(parameters);
 				mCamera.setPreviewDisplay(mHolder);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			mCamera.setParameters(parameters);
 		    startCameraPreview();
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {}
 	};
 	
 	PictureCallback mPicture = new PictureCallback() {
 	    public void onPictureTaken(byte[] data, Camera camera) {
 	       /* File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 	        if (pictureFile == null){
 	            System.out.println("Error creating media file, check storage permissions: ");
 	            return;
 	        }
 	        try {
 	            FileOutputStream fos = new FileOutputStream(pictureFile);
 	            fos.write(data);
 	            fos.close();
 	        } catch (FileNotFoundException e) {
 	           System.out.println("File not found: " + e.getMessage());
 	        } catch (IOException e) {
 	        	 System.out.println("Error accessing file: " + e.getMessage());
 	        }*/
 	    	
 	    	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
 	    	int rotation = 0;
 	    	if (bitmap.getHeight() < bitmap.getWidth())
 	    		rotation = 90;
 	    	Bitmap rotatedBitmap;
 	    	if (rotation != 0) {
 	    		 Matrix matrix = new Matrix();
 	             matrix.postRotate(rotation);
 	             rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
 	            		 bitmap.getHeight(), matrix, true);
 	    	}
 	    	else
 	    		rotatedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
 	    				 bitmap.getHeight(), true);
 	    	
 	    	File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 	        if (pictureFile == null){
 	            System.out.println("Error creating media file, check storage permissions: ");
 	            return;
 	        }
 	        try {
 	            FileOutputStream fos = new FileOutputStream(pictureFile);
 	            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
 	            //fos.write(data);
 	            fos.close();
 	            bitmap.recycle();
 	            bitmap = null;
 	            rotatedBitmap.recycle();
 	            rotatedBitmap = null;
 	        } catch (FileNotFoundException e) {
 	           System.out.println("File not found: " + e.getMessage());
 	        } catch (IOException e) {
 	        	 System.out.println("Error accessing file: " + e.getMessage());
 	        }
 	        
 	        stopCameraPreview();
 	        releaseCameraAndPreview();
 	        activity.finish();
 	       // startCameraPreview();
 	    }
 	};
 	
 	ShutterCallback mShutter = new ShutterCallback() {
 		public void onShutter() {
 			
 		}
 	};
 	
 	View.OnTouchListener touchListener = new View.OnTouchListener() {
 		
 		public boolean onTouch(View v, MotionEvent event) {
 			if (event.getPointerCount() == 1){
 				int action = event.getActionMasked();
 				
 				 if (action == MotionEvent.ACTION_MOVE) {
 					float y = event.getY();
 					zoom((int)((y - pY)/4));
 					pY = y;
 				}
 				else {
 					pY = event.getY();
 				}
 			}
 			return true;
 		}
 	};
 	
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         System.out.println("Launching Cellscope...");
         activity = this;
         //mSurfaceView = new PhotoSurface(this);
     	//setContentView(mSurfaceView);
         setContentView(R.layout.activity_photo);
         mSurfaceView = (SurfaceView)findViewById(R.id.previewSurface);
         mSurfaceView.setOnTouchListener(touchListener);
         mHolder = mSurfaceView.getHolder();
 	    mHolder.addCallback(mCallback);
 	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	    zoomText = (TextView)findViewById(R.id.zoomtext);
 	    zoomText.setText("100%");
 	    takePhoto = (ImageButton)findViewById(R.id.takePhotoButton);;
 	    switchMode = (ImageButton)findViewById(R.id.switchCameraMode);
     }
     
     /*
      * This is automatically called when the application is opened
      * or resumed.
      */
     public void onResume() {
     	super.onResume();
     	System.out.println("Surface resuming...");
     	if (safeCameraOpen())
     		startCameraPreview();
     }
     
     public void onPause() {
     	super.onPause();
     	stopCameraPreview();
     	releaseCameraAndPreview();
     }
     
     boolean safeCameraOpen() {
         boolean qOpened = false;
         System.out.println("Opening camera...");
         try {
             releaseCameraAndPreview();
             mCamera = Camera.open(); /* This is the important thing!
             							It makes an instance of a Camera object that
             							lets the application do stuff with the hardware.
             							*/
             System.out.println("   " + mCamera);
             qOpened = (mCamera != null);
         } catch (Exception e) {
         	System.out.println("Failed to open Camera!");
             Log.e(activity.getString(R.string.app_name), "failed to open Camera");
             e.printStackTrace();
         }
         return qOpened;    
     }
 
     private void releaseCameraAndPreview() {
         if (mCamera != null) {
             mCamera.release();
             mCamera = null;
         }
     }
     
     /*
      * Returns the best size that the camera preview should be, based
      * on the size of the screen.
      */
 	static Camera.Size getPreviewSize(Camera.Parameters parameters, int width, int height) {
 		Camera.Size result = null;
 		for (Camera.Size current: parameters.getSupportedPreviewSizes()) {
 			if (current.width < width && current.height < height) {
 				if (result == null)
 					result = current;
 				else if (result.width * result.height < current.width * current.height)
 					result = current;
 			}
 		}
 		return result;
 	}
 	/** Create a file Uri for saving an image or video */
 	private static Uri getOutputMediaFileUri(int type){
 	      return Uri.fromFile(getOutputMediaFile(type));
 	}
 
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type){
 	    // To be safe, you should check that the SDCard is mounted
 	    // using Environment.getExternalStorageState() before doing this.
 
 	    
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
 	        mediaFile = new File(videoStorageDir.getPath() + File.separator +
 	        "VID_"+ timeStamp + ".mp4");
 	    } else {
 	        return null;
 	    }
 
 	    return mediaFile;
 	}
 	
 	public void startCameraPreview() {
 		mCamera.startPreview();
 		previewRunning = true;
 	}
 	
 	public void stopCameraPreview() {
 		if (mCamera != null)
 			mCamera.stopPreview();
 		previewRunning = false;
 	}
 	
 	public void takePhoto(View v) {
 		if (!videoState)
 			mCamera.takePicture(mShutter, null, mPicture);
 		else {
 			if (!recording) {
 				startRecording();
 			}
 			else {
 				stopRecording();
 			}
 		}
 	}
 	
 	private void startRecording() {
 		mCamera.unlock();
 		recorder = new MediaRecorder();
 		recorder.setCamera(mCamera);
 		//recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 		//recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 		//recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
 	//	recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
 		//recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 	//	recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
 	/*	recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
         recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
         */
         recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
         recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
 		System.out.println("OUTPUT " + getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
 		recorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
 		recorder.setPreviewDisplay(mHolder.getSurface());
 		try {
 			System.out.println("Preparing recorder");
 			recorder.prepare();
 			System.out.println("Starting recorder");
 			recorder.start();
 			recording = true;
 		} catch (IllegalStateException e) {
 			System.out.println("Error preparing recorder");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error preparing video output");
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private void stopRecording() {
 		recorder.stop();
 		recorder.reset();
 		recorder.release();
 		mCamera.lock();
 		mCamera.stopPreview();
 		releaseCameraAndPreview();
 		activity.finish();
 	}
 	
 	private void zoom(int step) {
 		Camera.Parameters parameters = mCamera.getParameters();
 		if (!parameters.isZoomSupported())
 			return;
 		int zoom = parameters.getZoom() + step;
 		if (zoom > parameters.getMaxZoom())
 			zoom = parameters.getMaxZoom();
 		else if (zoom < 0)
 			zoom = 0;
 		parameters.setZoom(zoom);
 		String str= parameters.getZoomRatios().get(zoom) + "%";
 		zoomText.setText(str);
 		mCamera.setParameters(parameters);
 	}
 	
 	public void zoomIn(View view) {
 		zoom(10);
 	}
 	public void zoomOut(View view) {
 		zoom(-10);
 	}
 	
 	public void switchMode(View view) {
 		if (recording)
 			return;
 		videoState = !videoState;
 		if (videoState) {
 			switchMode.setImageResource(R.drawable.camera);
 			takePhoto.setImageResource(R.drawable.record);
 		}
 		else {
 			switchMode.setImageResource(R.drawable.record);
 			takePhoto.setImageResource(R.drawable.camera);
 		}
 	}
 }
