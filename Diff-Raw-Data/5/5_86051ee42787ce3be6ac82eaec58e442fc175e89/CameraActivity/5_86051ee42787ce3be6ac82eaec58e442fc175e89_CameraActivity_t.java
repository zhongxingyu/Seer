 package lectureOfCamera;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import system.FilePoolManager;
 import android.app.Activity;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 
 import com.example.lectureofandroid.R;
    
 public class CameraActivity extends Activity {
 	
 	private static final String TAG = "CameraActivity";
 	private FrameLayout mView  = null;
 	private Button mCaptureButton = null;
 	private PictureCallback mPicture = null;
 	private CameraPreview mCameraPreview = null;
 	private CameraFactory mCameraFactory = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.camera_preview);
 		
 		mCameraFactory = new CameraFactory(this);
 		//get an instance of Camera
 		mCameraFactory.getCameraInstance();
 		
 		mView = (FrameLayout)findViewById(R.id.camera_preview);
 		
 		//set the data type in jpg and save in specific file directory
 		mPicture = getPictureCallback();
 		
 		mCaptureButton = (Button) findViewById(R.id.camera_capture);
 		
 		btnCaptureSetting();
 		touchScreenSetting();
 	}
 	
 	@Override 
 	public void onResume() {
 		super.onResume();
 		Log.d(TAG,"onResume");
 		mCameraFactory.restartPreview();
 		mCameraPreview = new CameraPreview(this, mCameraFactory);
 		
 		mView.addView(mCameraPreview);
 	}
 	
 	private PictureCallback getPictureCallback() {
 		return new PictureCallback() {	
 			@Override
 			public void onPictureTaken(byte[] data, Camera camera) {
 				
 				File pictureFile = FilePoolManager.getOutputMediaFile(FilePoolManager.FILE_TYPE_IMAGE);
 				if(pictureFile == null) {
 					Log.d(TAG,"Error creating file, pls check the storage permission");			
 					return;
 				}
 				FileOutputStream fos;
 				try {
 					fos = new FileOutputStream(pictureFile);
 					fos.write(data);
 					fos.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		 };
 	}
 	
 	private void btnCaptureSetting() {
 		mCaptureButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				takePicture();
 			}
 		});
 	}
 	
 	private void touchScreenSetting() {
 		mView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				takePicture();
 			}
 		});
 	}
 	
 	private void takePicture() {
 		mCameraFactory.takePicture(null, null, mPicture);
 		mCameraFactory.restartPreview();
 	}
 	
	//the camera change from back to front need to remove the origin view first
 	public void changeCameraFacing(View view) {
		mView.removeView(mCameraPreview);
 		mCameraPreview.surfaceDestroyed(mCameraPreview.getSurfaceHolder());
 		mCameraFactory.removeCameraInstance();
 		
 		mCameraFactory.changeCameraFacing();
 		mCameraPreview = new CameraPreview(this, mCameraFactory);
 		mView.addView(mCameraPreview);
 	}
 }
