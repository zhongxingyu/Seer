 package com.example.roboarmapp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.Matrix;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class MainActivity extends Activity implements SensorEventListener {
 
 	private static boolean buttonIsDown;
 	public static boolean xAxisLocked = false;
 	public static boolean yAxisLocked = false;
 	public static boolean zAxisLocked = false;
 	public static boolean rollLocked = false;
 	public static boolean pitchLocked = false;
 	public static String serverIP = "";
 	public static String serverPort = "4012";
 	public static Socket mySocket;
 	public static OutputStream out;
 	public static boolean socketConnected = false;
 	public static Timer myTimer;
 
 	public static boolean aboutToLock;
 	public static int connectingToServer = 0;
 
 	public static String period = "1000"; // # of ms between tcp/ip data send
 
 	// Sensors
 	private SensorManager mSensorManager;
 	private long lastMeasurement1, lastMeasurement2;
 	private Sensor mAccelerometer, mOrientation, mGyroscope, mRotation;
 	private boolean reference;
 	private static final float NS2S = 1.0f / 1000000000.0f;
 	public static float[] prevSentDataVector;
 	public static float[] rotationVector;
 	public static float[] rotationMatrix;
 	public static float[] acceleration;
 	public static float[] speed;
 	public static float[] displacement = new float [3];
 	public static float rollAngle = 0;
 	public static float pitchAngle = 0;
 	private float referenceRoll;
 	private float referencePitch;
 	public static float grip;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		prevSentDataVector = new float[6];
 		for (int i = 0; i < 0; i++) {
 			prevSentDataVector[i] = 0.0f;
 		}
 
 		// check for settings file
 		File fileTest = getFileStreamPath("settings.txt");
 		if (fileTest.exists()) {
 			try {
 				int counter = 0;
 				String entryDelims = "[|]";
 				FileInputStream in = openFileInput("settings.txt");
 				InputStreamReader inputStreamReader = new InputStreamReader(in);
 				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
 				String rawInput = bufferedReader.readLine();
 				in.close();
 				String[] entries = rawInput.split(entryDelims);
 				serverIP = entries[0];
 				serverPort = entries[1];
 				period = entries[2];
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		Button homeButton = (Button) findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (which == Dialog.BUTTON_POSITIVE) {
 							// TODO: HOME ROBOT POSITION
 						}
 					}
 				};
 
 				new AlertDialog.Builder(v.getContext())
 				.setMessage(R.string.home_dialog_message)
 				.setPositiveButton(R.string.proceed, listener)
 				.setNegativeButton(R.string.cancel, listener)
 				.setTitle(R.string.home_dialog_title).show();
 			}
 		});
 
 		SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
 		gripperBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar arg0, int progress,
 					boolean arg2) {
 				grip = (float) progress;
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				Log.d("CHECK:", "Current progress is " + grip);
 				// float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip
 				// };
 				// doSend(outFloatData);
 			}
 		});
 	}
 
 	private void moveRobotArm(long pressTime) {
 		Button myMainButton = (Button) findViewById(R.id.startStopButton);
 		// wait two seconds before starting
 		/*
 		 * while ((System.currentTimeMillis() - pressTime) < 2000) { // DO
 		 * NOTHING }
 		 */
 		myMainButton.setBackgroundColor(Color.RED);
 		myMainButton.setText(R.string.release_to_stop);
 		/*
 		 * while (buttonIsDown) { // DO MAIN ARM MOVEMENTS }
 		 * myMainButton.setBackgroundColor(Color.GREEN);
 		 */
 		// myMainButton.setText("@string/start_stop");
 	}
 
 	private void resetMainButton() {
 		Button myMainButton = (Button) findViewById(R.id.startStopButton);
 		myMainButton.setBackgroundColor(Color.GREEN);
 		myMainButton.setText(R.string.start_stop);
 	}
 
 	/** Called when the user clicks the Lock Axis button */
 	public void lockAxis(View view) {
 		aboutToLock = true;
 		Intent intent = new Intent(this, LockAxis.class);
 		startActivity(intent);
 	}
 
 	@SuppressWarnings("deprecation")
 	public void startSensors() {
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometer = mSensorManager
 				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 		mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 		mSensorManager.registerListener(this, mAccelerometer,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		mSensorManager.registerListener(this, mOrientation,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		mSensorManager.registerListener(this, mGyroscope,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		mSensorManager.registerListener(this, mRotation,
 				SensorManager.SENSOR_DELAY_NORMAL);
 
 		rotationVector = new float [3];
 		acceleration = new float [4];
 		speed = new float [3];
 		displacement = new float [3];
 		for (int i = 0; i < 3; i++) {
 			displacement[i] = 0.0f;
 		}
 		lastMeasurement1 = System.nanoTime();
 		lastMeasurement2 = System.nanoTime();
 		reference = true;
 		// rollAngle = 0;
 		// pitchAngle = 0;
 	}
 
 	public void stopSensors() {
 		mSensorManager.unregisterListener(this);
 	}
 
 	/*
 	 * public void doSend(float[] outFloatData) { for (int i = 0; i <
 	 * outFloatData.length; i++) { int data =
 	 * Float.floatToRawIntBits(outFloatData[i]); byte outByteData[] = new
 	 * byte[4]; outByteData[3] = (byte) (data >> 24); outByteData[2] = (byte)
 	 * (data >> 16); outByteData[1] = (byte) (data >> 8); outByteData[0] =
 	 * (byte) (data); try { out.write(outByteData); } catch (IOException e) { //
 	 * TODO Auto-generated catch block e.printStackTrace(); } } try {
 	 * out.flush(); } catch (IOException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); } }
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		myTimer = new Timer();
 		doSendTimerTask myTask = new doSendTimerTask();
 		myTimer.schedule(myTask, 1000, Integer.parseInt(period));
 		aboutToLock = false;
 		Button myMainButton = (Button) findViewById(R.id.startStopButton);
 		if (!socketConnected) {
 			myMainButton.setBackgroundColor(Color.YELLOW);
 			myMainButton.setText(R.string.connecting);
 			connectingToServer++;
 			new ConnectToServer().execute();
 		}
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		myTimer.cancel();
 		myTimer.purge();
 		try {
 			if ((socketConnected) && (!aboutToLock)) {
 				socketConnected = false;
 				out.flush();
 				out.close();
 				mySocket.close();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		File fileTest = getFileStreamPath("settings.txt");
 		if (fileTest.exists()) {
 			fileTest.delete();
 		}
 		try {
 			FileOutputStream out = openFileOutput("settings.txt", Context.MODE_PRIVATE);
 			int i;
 			String entry = "";
 			entry += serverIP;
 			entry += "|";
 			entry += serverPort;
 			entry += "|";
 			entry += period;
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
 		super.onStop();
 	}
 
 	public class ConnectToServer extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			if (connectingToServer == 1) {
 				Log.d("DEBUG: ", "Before connection");
 	
 				try {
 					mySocket = new Socket();
 					mySocket.connect(new InetSocketAddress(serverIP, Integer.parseInt(serverPort)), 1000);
 					out = mySocket.getOutputStream();
 					socketConnected = true;
 					/*
 					 * out.write(1); ObjectOutputStream out = new
 					 * ObjectOutputStream(mySocket.getOutputStream()); DataPacket
 					 * packetToServer = new DataPacket(); packetToServer.x =
 					 * (float)1.1; packetToServer.y = (float)2.2; packetToServer.z =
 					 * (float)3.3; out.writeObject(packetToServer); out.flush();
 					 * out.close();
 					 */
 					Log.d("DEBUG: ", "Tried Connection");
 					// mySocket.close();
 				} catch (IOException e) {
 					Log.e("ERR: ", e.getMessage());
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			connectingToServer--;
 			Button myMainButton = (Button) findViewById(R.id.startStopButton);
 			if (!socketConnected) {
 				if ((serverIP == "") || (serverPort == "")) {
 					myMainButton.setText(R.string.no_ip);
 				} else {
 					myMainButton.setText(R.string.connect_error);
 					myMainButton.setOnTouchListener(new OnTouchListener() {
 						public boolean onTouch(View v, MotionEvent event) {
 							switch (event.getAction()) {
 							case MotionEvent.ACTION_UP: {
 								Button myMainButton = (Button) findViewById(R.id.startStopButton);
 								myMainButton.setText(R.string.connecting);
 								connectingToServer++;
 								new ConnectToServer().execute();
 							}}
 							return true;
 						}
 					});
 				}
 			} else {
 				myMainButton.setBackgroundColor(Color.GREEN);
 				myMainButton.setText(R.string.start_stop);
 
 				myMainButton = (Button) findViewById(R.id.startStopButton);
 				myMainButton.setOnTouchListener(new OnTouchListener() {
 					public boolean onTouch(View v, MotionEvent event) {
 
 						switch (event.getAction()) {
 						case MotionEvent.ACTION_DOWN: {
 							if (!socketConnected) {
								SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
								gripperBar.setEnabled(true);
 								Button myMainButton = (Button) findViewById(R.id.startStopButton);
 								myMainButton.setText(R.string.connect_error);
 								myMainButton.setBackgroundColor(Color.YELLOW);
 								myMainButton.setOnTouchListener(new OnTouchListener() {
 									public boolean onTouch(View v, MotionEvent event) {
 
 										switch (event.getAction()) {
 										case MotionEvent.ACTION_UP: {
 											connectingToServer++;
 											new ConnectToServer().execute();
 										}}
 										return true;
 									}});
 								return true;
 							}
 
 							buttonIsDown = true;
 							SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
 							gripperBar.setEnabled(false);
 							Button homeButton = (Button) findViewById(R.id.homeButton);
 							Button lockButton = (Button) findViewById(R.id.lockButton);
 							homeButton.setEnabled(false);
 							lockButton.setEnabled(false);
 							startSensors();
 							moveRobotArm(System.currentTimeMillis());
 							return true;
 						}
 
 						case MotionEvent.ACTION_UP: {
 							if (!socketConnected) {
								SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
								gripperBar.setEnabled(true);
 								Button myMainButton = (Button) findViewById(R.id.startStopButton);
 								myMainButton.setText(R.string.connect_error);
 								myMainButton.setBackgroundColor(Color.YELLOW);
 								myMainButton.setOnTouchListener(new OnTouchListener() {
 									public boolean onTouch(View v, MotionEvent event) {
 
 										switch (event.getAction()) {
 										case MotionEvent.ACTION_UP: {
 											connectingToServer++;
 											new ConnectToServer().execute();
 										}}
 										return true;
 									}});
 								return true;
 							}
 							
 							buttonIsDown = false;
 							SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
 							gripperBar.setEnabled(true);
 							Button homeButton = (Button) findViewById(R.id.homeButton);
 							Button lockButton = (Button) findViewById(R.id.lockButton);
 							homeButton.setEnabled(true);
 							lockButton.setEnabled(true);
 							stopSensors();
 							resetMainButton();
 							return true;
 						}
 
 						default:
 							return false;
 						}
 					}
 				});
 			}
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.equals(mGyroscope)) {
 			float roll = event.values[1]; // around axis y
 			float pitch = event.values[0]; // around axis x
 			long timeInterval = event.timestamp - lastMeasurement2;
 			lastMeasurement2 = event.timestamp;
 			// Assume the device has been turning with same speed for the whole
 			// interval
 			if (!rollLocked) {
 				roll = (float) (roll * timeInterval * NS2S);
 				rollAngle = rollAngle + roll;
 			}
 
 			if (!pitchLocked) {
 				pitch = (float) (pitch * timeInterval * NS2S);
 				pitchAngle = pitchAngle + pitch;
 			}
 			/*
 			 * Log.d("DEBUG: ", "The current roll value is " + rollAngle * 360 /
 			 * 2 / Math.PI);
 			 */
 			// float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip };
 			// doSend(outFloatData);
 		}
 
 		if (event.sensor.equals(mAccelerometer)) {
 			float [] rawLinear = { event.values[0], event.values[1],
 					event.values[2], 0 };
 			float [] temp = new float [16];
 			rotationMatrix = new float [16];
 			SensorManager.getRotationMatrixFromVector(temp, rotationVector);
 			Matrix.invertM(rotationMatrix, 0, temp, 0);
 			Matrix.multiplyMV(acceleration, 0, rotationMatrix, 0, rawLinear, 0);
 			//Log.d("CHECK:", "x = " + acceleration[0] + "; y = " + acceleration[1] + "; z = " + acceleration[2]);
 			long timeInterval = event.timestamp - lastMeasurement1;
 			lastMeasurement1 = event.timestamp;
 
 			if (!xAxisLocked) {
 				speed[0] = (float) (acceleration[0] * timeInterval * NS2S) + speed[0];
 				displacement[0] = (float) (speed[0] * timeInterval * NS2S) + displacement[0];
 			}
 
 			if (!yAxisLocked) {
 				speed[1] = (float) (acceleration[1] * timeInterval * NS2S) + speed[1];
 				displacement[1] = (float) (speed[1] * timeInterval * NS2S) + displacement[1];
 			}
 
 			if (!zAxisLocked) {
 				speed[2] = (float) (acceleration[2] * timeInterval * NS2S) + speed[2];
 				displacement[2] = (float) (speed[2] * timeInterval * NS2S) + displacement[2];				
 			}
 			//Log.d("CHECK:", "x = " + displacement[0] + "; y = " + displacement[1] + "; z = " + displacement[2]);
 		}
 
 		if (event.sensor.equals(mRotation)) {
 			rotationVector = event.values;
 		}
 
 		if (event.sensor.equals(mOrientation)) {
 			float roll = event.values[2];
 			float pitch = event.values[1];
 			if (pitch > 90) {
 				if (roll > 0)
 					roll = 180 - roll;
 				else
 					roll = -180 - roll;
 			}
 
 			if (reference) {
 				referenceRoll = roll;
 				referencePitch = pitch;
 				reference = false;
 			}
 
 			float tempRoll = (float) (referenceRoll + rollAngle * 360 / 2
 					/ Math.PI);
 			float tempPitch = (float) (referencePitch + pitchAngle * 360 / 2
 					/ Math.PI);
 			while (tempRoll > 180) {
 				tempRoll = tempRoll - 360;
 			}
 			while (tempRoll < -180) {
 				tempRoll = tempRoll + 360;
 			}
 			while (tempPitch > 180) {
 				tempPitch = tempPitch - 360;
 			}
 			while (tempPitch < -180) {
 				tempPitch = tempPitch + 360;
 			}
 
 			float diffRoll = roll - tempRoll;
 			float diffPitch = pitch - tempPitch;
 			if (diffRoll > 180) {
 				diffRoll = diffRoll - 360;
 			} else if (diffRoll < -180) {
 				diffRoll = diffRoll + 360;
 			}
 			if (diffPitch > 180) {
 				diffPitch = diffPitch - 360;
 			} else if (diffPitch < -180) {
 				diffPitch = diffPitch + 360;
 			}
 			/*
 			Log.d("CHECK:", "The current roll is " + tempRoll
 					+ "; The expected roll is " + roll);*/
 			// Log.d("CHECK:", "The current roll is " + (rollAngle * 360 / 2 /
 			// Math.PI) + "; The expected roll is " + (diffPitch + rollAngle *
 			// 360 / 2 / Math.PI));
 		}
 
 	}
 }
 
 class doSendTimerTask extends TimerTask {
 	public void run() {
 		if (MainActivity.socketConnected) {
 			float[] outFloatData = { MainActivity.rollAngle,
 					MainActivity.pitchAngle, MainActivity.displacement[0], MainActivity.displacement[1], MainActivity.displacement[2], MainActivity.grip };
 			boolean dataHasChanged = false;
 			for (int i = 0; i < outFloatData.length; i++) {
 				if (outFloatData[i] != MainActivity.prevSentDataVector[i]) {
 					dataHasChanged = true;
 					break;
 				}
 			}
 			if (dataHasChanged) {
 				MainActivity.displacement[0] = 0.0f;
 				MainActivity.displacement[1] = 0.0f;
 				MainActivity.displacement[2] = 0.0f;
 				MainActivity.speed[0] = 0.0f;
 				MainActivity.speed[1] = 0.0f;
 				MainActivity.speed[2] = 0.0f;
 
 
 				for (int i = 0; i < outFloatData.length; i++) {
 					MainActivity.prevSentDataVector[i] = outFloatData[i];
 					int data = Float.floatToRawIntBits(outFloatData[i]);
 					byte outByteData[] = new byte[4];
 					outByteData[3] = (byte) (data >> 24);
 					outByteData[2] = (byte) (data >> 16);
 					outByteData[1] = (byte) (data >> 8);
 					outByteData[0] = (byte) (data);
 					try {
 						MainActivity.out.write(outByteData);
 					} catch (IOException e) {
 						MainActivity.socketConnected = false;
 						e.printStackTrace();
 					}
 				}
 				try {
 					MainActivity.out.flush();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
