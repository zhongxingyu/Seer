 package com.rullelinjeapp.activity;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.rullelinjeapp.R;
 
 public class CameraActivity extends Activity implements SurfaceHolder.Callback {
 
 	Camera camera;
 	SurfaceView surfaceView;
 	SurfaceHolder surfaceHolder;
 	final static private String TAG = "##### CameraActivity";
 
 	public void logm(String line) {
 		Log.i(TAG, line);
 	}
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.camera_layout);
 		Button finnKrengevinkelButton = (Button) findViewById(R.id.finnKrengeVinkel_Result);
 		getWindow().setFormat(PixelFormat.UNKNOWN);
 		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
 		surfaceHolder = surfaceView.getHolder();
 		surfaceHolder.addCallback(this);
 		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		finnKrengevinkelButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				findAnlge();
 				
 			}
 		});
 
 	}
 
 	private void findAnlge(){
 		
 		Intent resultIntent = new Intent();
 		resultIntent.putExtra("angle", Math.random());
 		// TODO Add extras or a data URI to this intent as appropriate.
 		setResult(Activity.RESULT_OK, resultIntent);
 		releaseCamera();
 		finish();
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		camera = Camera.open();
 		if (camera != null) {
 			try {
 				camera.setPreviewDisplay(surfaceHolder);
 				camera.startPreview();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 			releaseCamera();
 
 	}
 	
 	private void releaseCamera(){
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 	}
 }
