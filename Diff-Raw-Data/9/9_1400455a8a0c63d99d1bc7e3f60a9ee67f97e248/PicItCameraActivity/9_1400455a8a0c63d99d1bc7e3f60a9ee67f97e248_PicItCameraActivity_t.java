 package edu.montclair.hci.picit.camera;
 
 import edu.montclair.hci.picit.R;
 import edu.montclair.hci.picit.camera.OverlayView;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PicItCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, OnClickListener {
 	
 	private final String TAG = "PicItCameraActivity";
 	
 	private Camera camera = null;
 	private TextView fileLocationWidget = null;
 	
 	private SurfaceView surfaceView = null;
 	private OverlayView overlayView = null;
 	private SurfaceHolder surfaceHolder = null;
 	
 	private int numberOfCameras;
 	private int defaultCameraId;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.camera_layout);
         surfaceView = (SurfaceView)findViewById(R.id.preview);       
         surfaceHolder = surfaceView.getHolder();
         surfaceHolder.addCallback(this);
         surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         
         overlayView = (OverlayView)findViewById(R.id.drawOverlay);
         overlayView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
         
         Button button = (Button)findViewById(R.id.button1);
         button.setOnClickListener(this);
         
         fileLocationWidget = (TextView)findViewById(R.id.textView1);
         
     	
     	numberOfCameras = Camera.getNumberOfCameras();
         
         // Grabs the back facing camera
         CameraInfo cameraInfo = new CameraInfo();
         for ( int i = 0; i < numberOfCameras; i++ ) {
         	Camera.getCameraInfo(i, cameraInfo);
         	if ( cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK ) {
         		defaultCameraId = i;
         	}
         }
         
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	
     	if ( camera != null ) {
     		//preview.setCamera(null);
     		overlayView.removePreview();
     		camera.stopPreview();
     		camera.release();
     		camera = null;
     		Log.d(TAG, "Camera Released");
     	}
     }
     
     @Override
     public void onResume() {
     	super.onResume();
 
 		Log.d("PicItCameraActivity", "onResume");
     	camera = Camera.open(defaultCameraId);
         overlayView.setCamera(camera);
         
         surfaceView.requestLayout();
     }
     
     public void startPreview() {
     	if ( camera != null ) {
         	camera.startPreview();
     	}
     }
     
     private Size getOptimalSize(int width, int height) {
     	
     	Camera.Parameters params = camera.getParameters();
     	List<Size> sizes = params.getSupportedPreviewSizes();
     	
     	int targetHeight = height;
     	int targetWidth = width;
     	final double ASPECT_TOLERANCE = 0.1;
         double targetRatio = (double) width / height;
     	Size optimalSize = null;
         double minDiff = Double.MAX_VALUE;
         
     	// Try to find an size match aspect ratio and size
         for (Size size : sizes) {
         	Log.d(TAG, "Width: " + size.width + " Height: " + size.height);
             double ratio = (double) size.width / size.height;
             if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
             if (Math.abs(size.width - targetWidth) < minDiff) {
                 optimalSize = size;
                 minDiff = Math.abs(size.width - targetWidth);
             }
         }
     	
         if (optimalSize == null) {
             minDiff = Double.MAX_VALUE;
             for (Size size : sizes) {
                 if (Math.abs(size.width - targetWidth) < minDiff) {
                     optimalSize = size;
                     minDiff = Math.abs(size.width - targetWidth);
                 }
             }
         }
 
     	Log.d(TAG, "Optimal Width: " + optimalSize.width + " Optimal Height: " + optimalSize.height);
     	return optimalSize;    	
     }
     
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		try {
 			camera.setPreviewDisplay(surfaceHolder);
 		} catch ( IOException e ) {
 			Log.e(TAG, "IOException: setPreviewDisplay()", e);
 		}
 		
		Size optimalSize = null;
		
		if (height > 480) {
			optimalSize = getOptimalSize(800, 480);
		} else {
			optimalSize = getOptimalSize(width, height);
		}
		
 		Camera.Parameters params = camera.getParameters();
 		params.setPreviewSize(optimalSize.width, optimalSize.height);
 		
 		camera.setParameters(params);
 		
 		surfaceView.layout(0, 0, optimalSize.width, optimalSize.height);
 		overlayView.setPreviewSize(optimalSize);
 		
     	startPreview();
 	}
 
 	public void surfaceCreated(SurfaceHolder arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onClick(View arg0) {
 		camera.autoFocus(this);	
 	}
 
 	public void onAutoFocus(boolean arg0, Camera arg1) {
 		//camera.takePicture(null, null, photoCallback);		
 	}
 	
 
 	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
 		
 		public void onPictureTaken(byte[] data, Camera camera) {
 			if ( data != null ) {
 				try {
 					File photoFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraPreviewDemo2");
 					
 					if ( !photoFileDir.exists() ) {
 						photoFileDir.mkdirs();
 					}
 					
 					String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 					
 					File photoFile = new File(photoFileDir.getAbsolutePath() + File.separator + "IMG_" + timestamp + ".jpg");
 					fileLocationWidget.setText(photoFile.toString());
 					FileOutputStream fout = new FileOutputStream(photoFile);
 					fout.write(data);
 					fout.close();
 					
 					
 				} catch ( Exception e ) {
 					Log.e(TAG+".photoCallback", "IOException: FileOutputStream()", e);
 				}
 				
 			}
 			
 			startPreview();
 		}
 	};
     
 }
