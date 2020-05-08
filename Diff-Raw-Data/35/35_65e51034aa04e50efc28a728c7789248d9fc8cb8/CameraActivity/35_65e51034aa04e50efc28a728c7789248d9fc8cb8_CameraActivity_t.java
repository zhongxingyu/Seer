 package com.rullelinjeapp.activity;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 
 import com.rullelinjeapp.R;
 
 public class CameraActivity extends Activity implements SurfaceHolder.Callback {
 
 	Camera mCamera;
 	SurfaceView mSurfaceView;
 	SurfaceHolder surfaceHolder;
 	final Context context = this;
 
 	boolean isPreviewRunning = false;
 	final float[] mValuesMagnet = new float[3];
 	final float[] mValuesAccel = new float[3];
 	final float[] mValuesOrientation = new float[3];
 	final float[] mRotationMatrix = new float[9];
 	SensorManager sensorManager;
 	SensorEventListener mEventListener;
 
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
 		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
 		surfaceHolder = mSurfaceView.getHolder();
 		surfaceHolder.addCallback(this);
 		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
 		setUpDialog();
 		mEventListener = new SensorEventListener() {
 			public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			}
 
 			public void onSensorChanged(SensorEvent event) {
 
 				switch (event.sensor.getType()) {
 				case Sensor.TYPE_ACCELEROMETER:
 					System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
 					// logm("x="+event.values[0]+ "    y=" +event.values[1]+
 					// "    z="+event.values[2]+ "    angle="
 					// +Math.atan2(event.values[0],
 					// event.values[1])/(Math.PI/180));
 					break;
 
 				case Sensor.TYPE_MAGNETIC_FIELD:
 					System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
 					break;
 				}
 			};
 		};
 
 		setSensorListners();
 
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		finnKrengevinkelButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				findAnlge();
 			}
 		});
 
 	}
 
 	private void findAnlge() {
 		SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel,
 				mValuesMagnet);
 		SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
 		float angleTemp = mValuesOrientation[1];
 		double angle;
//		if (mValuesOrientation[2] > 0) {
//			angle = (Math.PI / 2) - Math.abs(angleTemp) - (Math.PI / 2);
//		} else {
			angle = (Math.PI / 2) - Math.abs(angleTemp);
//		}

		// logm("verdi 1:" + mValuesOrientation[0] * (180 / Math.PI)
		// + "   verdi 2 " + mValuesOrientation[1] * (180 / Math.PI)
		// + " verdi 3 " + mValuesOrientation[2] * (180 / Math.PI));
 
 		logm("found angle: " + angle * (180 / Math.PI) + " grader");
 		Intent resultIntent = new Intent();
 		resultIntent.putExtra("angle", angle);
 		setResult(Activity.RESULT_OK, resultIntent);
 		releaseCamera();
 		finish();
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		if (isPreviewRunning) {
 			mCamera.stopPreview();
 		}
 
 		Parameters parameters = mCamera.getParameters();
 		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
 				.getDefaultDisplay();
 
 		if (display.getRotation() == Surface.ROTATION_0) {
 			parameters.setPreviewSize(height, width);
 			mCamera.setDisplayOrientation(90);
 		}
 
 		if (display.getRotation() == Surface.ROTATION_90) {
 			parameters.setPreviewSize(width, height);
 		}
 
 		if (display.getRotation() == Surface.ROTATION_180) {
 			parameters.setPreviewSize(height, width);
 		}
 
 		if (display.getRotation() == Surface.ROTATION_270) {
 			parameters.setPreviewSize(width, height);
 			mCamera.setDisplayOrientation(180);
 		}
 
 		mCamera.setParameters(parameters);
 		previewCamera();
 	}
 
 	public void previewCamera() {
 		try {
 			mCamera.setPreviewDisplay(surfaceHolder);
 			mCamera.startPreview();
 			isPreviewRunning = true;
 		} catch (Exception e) {
 			logm("Cannot start preview" + e);
 		}
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		mCamera = Camera.open();
 		if (mCamera != null) {
 			previewCamera();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		releaseCamera();
 
 	}
 
 	private void releaseCamera() {
 		if (mCamera != null) {
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 			isPreviewRunning = false;
 		}
 	}
 
 	public void setSensorListners() {
 		sensorManager.registerListener(mEventListener,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_NORMAL);
 		sensorManager.registerListener(mEventListener,
 				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	public void removeSensorListners() {
 		sensorManager.unregisterListener(mEventListener);
 	}
 
 	protected void onResume() {
 		super.onResume();
 		setSensorListners();
 	}
 
 	protected void onPause() {
 		super.onPause();
 		removeSensorListners();
 
 	}
 
 	private void setUpDialog() {
 		final Dialog dialog = new Dialog(context);
 		dialog.setContentView(R.layout.dialog_instruksjoner_camera);
 		dialog.setTitle("Instruksjoner");
 
 		Button dialogButton = (Button) dialog
 				.findViewById(R.id.dialogKameraButtonOK);
 		dialogButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		Button instruksjonButton = (Button) findViewById(R.id.finnKrengeVinkel_info);
 		instruksjonButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				dialog.show();
 			}
 		});
 	}
 
 }
