 package com.example.roboarmapp;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.support.v4.app.NavUtils;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 public class Calibration extends Activity implements SensorEventListener {
 
 	Context context = this;
 
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	private static int counter = 0;
 	private float[] offset;
 
 	private ProgressDialog pDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calibration);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		offset = MainActivity.offset;
 
 		TextView display_offset = (TextView) findViewById(R.id.textView6);
 		display_offset.setText("x = " + offset[0] + "\ny = " + offset[1]
 				+ "\nz = " + offset[2]);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			finish();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void startSensors() {
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometer = mSensorManager
 				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		mSensorManager.registerListener(this, mAccelerometer,
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	public void stopSensors() {
 		mSensorManager.unregisterListener(this);
 	}
 
 	public void StartCalibration(View view) {
 		counter = 0;
 		offset = new float[3];
		new Calibrate().execute();
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		for (int i = 0; i < 3; i++) {
 			offset[i] = (event.values[i] + offset[i] * counter) / (counter + 1);
 		}
 		counter++;
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 	    if ((keyCode == KeyEvent.KEYCODE_BACK))
 	    {
 	        finish();
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	public class Calibrate extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			while (true) {
 				if (Calibration.counter >= 100) {
 					break;
 				} else {
 					pDialog.setProgress(counter);
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			startSensors();
 			pDialog = new ProgressDialog(context);
 			pDialog.setCancelable(true);
 			pDialog.setOnCancelListener(new OnCancelListener(){
 				@Override
 				public void onCancel(DialogInterface arg0) {
 					stopSensors();
 				}				
 			});
 			pDialog.setTitle("Calibration in Process");
 			pDialog.setMessage("Calibrating... Please Wait.");
 			pDialog.show();
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			pDialog.dismiss();
 			stopSensors();
 
 			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which == Dialog.BUTTON_POSITIVE) {
 						TextView display_offset = (TextView) findViewById(R.id.textView6);
 						display_offset.setText("x = " + offset[0] + "\ny = " + offset[1]
 								+ "\nz = " + offset[2]);
 						File fileTest = getFileStreamPath("offsets.txt");
 						if (fileTest.exists()) {
 							fileTest.delete();
 						}
 						try {
 							FileOutputStream out = openFileOutput("offsets.txt",
 									Context.MODE_PRIVATE);
 							String entry = "" + offset[0] + "|" + offset[1] + "|"
 									+ offset[2];
 							out.write(entry.getBytes());
 							out.getFD().sync();
 							out.close();
 						} catch (FileNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			};
 
 			new AlertDialog.Builder(context)
 					.setMessage(R.string.save_offset_message)
 					.setPositiveButton(R.string.save, listener)
 					.setNegativeButton(R.string.cancel, listener)
 					.setTitle(R.string.save_offset_title).show();
 		}
 	}
 }
