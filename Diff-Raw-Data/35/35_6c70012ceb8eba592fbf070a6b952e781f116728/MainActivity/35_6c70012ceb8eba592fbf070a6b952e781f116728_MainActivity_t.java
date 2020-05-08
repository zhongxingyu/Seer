 package com.example.roboarmapp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Iterator;
 import java.util.LinkedList;
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
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.opengl.Matrix;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Switch;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements SensorEventListener {
 
 	// Axis Locks
 	public static boolean xAxisLocked;
 	public static boolean yAxisLocked;
 	public static boolean zAxisLocked;
 	public static boolean rollLocked;
 	public static boolean pitchLocked;
 	public static boolean xBoxChecked = false;
 	public static boolean yBoxChecked = false;
 	public static boolean zBoxChecked = false;
 	public static boolean rollBoxChecked = false;
 	public static boolean pitchBoxChecked = false;
 	public static boolean armMode;
 
 	// Connection
 	public static String serverIP = "";
 	public static String serverPort = "4012";
 	public static Socket mySocket;
 	public static OutputStream out;
 	public static InputStream in;
 	public static boolean socketConnected = false;
 	public static boolean connecting = true;
 	public static Timer myTimer;
 	public static String period = "1000"; // # of ms between tcp/ip data send
 
 	// Sound and Vibration
 	public static boolean doVibrate = true;
 	private static Vibrator v1;
 	private static MediaPlayer mp = null;
 	public static boolean doSound = true;
 
 	// Playback
 	public static int num_saves;
 	public static float[] armMovementTracker;
 	private static LinkedList<relativeArmPosition> arm_states;
 	public static boolean doPlayback;
 	public static boolean playbackMoveDone;
 
 	// Sensor flags
 	public static boolean tracking = false;
 	public static boolean sensorsStarted = false;
 	public static boolean reconnectButtonSet = false;
 	public static boolean serverSettingsChanged = false;
 
 	// UI
 	public Context context = this;
 	private static Context mainActivityContext;
 	public static SeekBar gripperBar;
 	public static Button myMainButton;
 	public static Button homeButton;
 	public static Button lockButton;
 	public static Button recordButton;
 	public static Button trashButton;
 	public static Switch modeSwitch;
 	public static String lock = "lock";
 
 	// Sensors and Data
 	private SensorManager mSensorManager;
 	private long lastMeasurement1, lastMeasurement2;
 	private Sensor mAccelerometer, mGyroscope, mRotation;
 	private static final float NS2S = 1.0f / 1000000000.0f;
 	public static float[] rotationVector;
 	public static float[] rotationMatrix;
 	public static float[] acceleration;
 	public static float[] previous_speed;
 	public static float[] previous_acceleration;
 	public static float[] speed;
 	public static float[] displacement;
 	public static float[] offset;
 	public static float rollAngle = 0;
 	public static float pitchAngle = 0;
 	public static float previousRollAngle = 0;
 	public static float previousPitchAngle = 0;
 	public static float grip = 0.0f;
 	public static float previousGrip = 0.0f;
 	public static int sensitivity = 0;
 	public static long[] sensorRestartTime;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		mainActivityContext = this;
 
 		arm_states = new LinkedList<relativeArmPosition>();
 		armMovementTracker = new float[5];
 		for (int i = 0; i < 5; i++) {
 			armMovementTracker[i] = 0.0f;
 		}
 		num_saves = 0;
 		doPlayback = false;
 		playbackMoveDone = true;
 
 		armMode = true;
 
 		xAxisLocked = false;
 		yAxisLocked = false;
 		zAxisLocked = false;
 
 		rollLocked = true;
 		pitchLocked = true;
 
 		modeSwitch = (Switch) findViewById(R.id.main_switch);
 
 		sensorRestartTime = new long[3];
 		for (int i = 0; i < 3; i++) {
 			sensorRestartTime[i] = 0;
 		}
 
 		displacement = new float[3];
 		for (int i = 0; i < 3; i++) {
 			displacement[i] = 0.0f;
 		}
 
 		previous_acceleration = new float[3];
 		speed = new float[3];
 		previous_speed = new float[3];
 		for (int i = 0; i < 3; i++) {
 			speed[i] = 0.0f;
 			previous_speed[i] = 0.0f;
 			previous_acceleration[i] = 0.0f;
 		}
 
 		v1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		gripperBar = (SeekBar) findViewById(R.id.gripperBar);
 		myMainButton = (Button) findViewById(R.id.startStopButton);
 		recordButton = (Button) findViewById(R.id.record);
 		trashButton = (Button) findViewById(R.id.trash);
 
 		// check for settings file
 		File fileTest = getFileStreamPath("settings.txt");
 		if (fileTest.exists()) {
 			try {
 				String entryDelims = "[|]";
 				FileInputStream in = openFileInput("settings.txt");
 				InputStreamReader inputStreamReader = new InputStreamReader(in);
 				BufferedReader bufferedReader = new BufferedReader(
 						inputStreamReader);
 				String rawInput = bufferedReader.readLine();
 				in.close();
 				String[] entries = rawInput.split(entryDelims);
 				serverIP = entries[0];
 				serverPort = entries[1];
 				period = entries[2];
 				doVibrate = true;
 				if (entries[3].equals("0")) {
 					doVibrate = false;
 				}
 				doSound = true;
 				if (entries[4].equals("0")) {
 					doSound = false;
 				}
 				sensitivity = Integer.parseInt(entries[5]);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// check for offset file
 		fileTest = getFileStreamPath("offsets.txt");
 		offset = new float[3];
 		if (fileTest.exists()) {
 			String entryDelims = "[|]";
 			FileInputStream in;
 			try {
 				in = openFileInput("offsets.txt");
 				InputStreamReader inputStreamReader = new InputStreamReader(in);
 				BufferedReader bufferedReader = new BufferedReader(
 						inputStreamReader);
 				String rawInput = bufferedReader.readLine();
 				in.close();
 				String[] entries = rawInput.split(entryDelims);
 				offset[0] = Float.parseFloat(entries[0]);
 				offset[1] = Float.parseFloat(entries[1]);
 				offset[2] = Float.parseFloat(entries[2]);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			for (int i = 0; i < 3; i++) {
 				offset[i] = 0.0f;
 			}
 		}
 
 		homeButton = (Button) findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (which == Dialog.BUTTON_POSITIVE) {
 							if (doSound) {
 								if (mp != null) {
 									mp.release();
 								}
 								mp = MediaPlayer.create(
 										getApplicationContext(), R.raw.home);
 								mp.start();
 							}
 							synchronized (lock) {
 								rollAngle = 0;
 								pitchAngle = 0;
 								for (int i = 0; i < 3; i++) {
 									displacement[i] = 0.0f;
 								}
 							}
 							grip = -300; // home signal
 							gripperBar.setProgress(0);
 
 							// zero tracker
 							for (int i = 0; i < 5; i++) {
 								armMovementTracker[i] = 0.0f;
 							}
 							previousGrip = 0.0f;
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
 
 		lockButton = (Button) findViewById(R.id.lockButton);
 
 		gripperBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar arg0, int progress,
 					boolean arg2) {
 				if ((grip <= 100) && (grip >= 0) && (!doPlayback)) {
 					grip = (float) progress;
 				}
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// Vibrate for 50 milliseconds
 				if (doVibrate) {
 					v1.vibrate(50);
 				}
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				// Log.d("CHECK:", "Current progress is " + grip);
 			}
 		});
 
 		final Handler handler = new Handler();
 		final Runnable r = new Runnable() {
 			public void run() {
 				checkConnectionStatus();
 				handler.postDelayed(this, 500);
 			}
 		};
 		handler.post(r);
 
 		new readFromServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	}
 
 	private void setOnMainButton() {
 		myMainButton.setBackgroundColor(Color.RED);
 		myMainButton.setText(R.string.release_to_stop);
 	}
 
 	private void resetMainButton() {
 		myMainButton.setBackgroundColor(Color.GREEN);
 		myMainButton.setText(R.string.start_stop);
 	}
 
 	public void playbackStates(View v) {
 		if (!socketConnected) {
 			Toast.makeText(getApplicationContext(),
 					"Cannot playback when ARM not connected",
 					Toast.LENGTH_SHORT).show();
 		} else if (arm_states.size() > 0) {
 			DialogInterface.OnClickListener listener_3 = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which == Dialog.BUTTON_POSITIVE) {
 						if (doSound) {
 							if (mp != null) {
 								mp.release();
 							}
 							mp = MediaPlayer.create(getApplicationContext(),
 									R.raw.playback_start);
 							mp.start();
 						}
 						doPlayback = true;
 						new playbackMode().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 					}
 				}
 			};
 
 			new AlertDialog.Builder(v.getContext())
 					.setMessage(R.string.playback_dialog_message)
 					.setPositiveButton(R.string.begin_playback, listener_3)
 					.setNegativeButton(R.string.cancel, listener_3)
 					.setTitle(R.string.playback_dialog_title).show();
 
 		} else {
 			Toast.makeText(getApplicationContext(),
 					"No positions for playback", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void recordState(View v) {
 		if (socketConnected) {
 			boolean did_save = false;
 			num_saves++;
 			// if both arm mode and wrist mode have been used, save the two
 			// movements as separate events
 			if ((armMovementTracker[0] != 0.0f)
 					|| (armMovementTracker[1] != 0.0f)
 					|| (armMovementTracker[2] != 0.0f)) {
 				arm_states.add(new relativeArmPosition(armMovementTracker[0],
 						armMovementTracker[1], armMovementTracker[2], 0.0f,
 						0.0f, previousGrip, num_saves));
 				did_save = true;
 			}
 
 			if ((armMovementTracker[3] != 0.0f)
 					|| (armMovementTracker[4] != 0.0f)) {
 				arm_states.add(new relativeArmPosition(0.0f, 0.0f, 0.0f,
 						armMovementTracker[3], armMovementTracker[4],
 						previousGrip, num_saves));
 				did_save = true;
 			}
 
 			if (grip != previousGrip) {
 				arm_states.add(new relativeArmPosition(0.0f, 0.0f, 0.0f, 0.0f,
 						0.0f, grip, num_saves));
 				previousGrip = grip;
 				did_save = true;
 			}
 
 			if (did_save) {
 				Toast.makeText(getApplicationContext(),
 						"ARM position recorded", Toast.LENGTH_SHORT).show();
 				if (doSound) {
 					if (mp != null) {
 						mp.release();
 					}
 					mp = MediaPlayer.create(getApplicationContext(),
 							R.raw.arm_position_recorded);
 					mp.start();
 				}
 			} else {
 				Toast.makeText(getApplicationContext(), "No data to save",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			// zero tracker
 			for (int i = 0; i < 5; i++) {
 				armMovementTracker[i] = 0.0f;
 			}
 		} else {
 			Toast.makeText(getApplicationContext(),
 					"Cannot save position when ARM not connected",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void trashState(View v) {
 		if (arm_states.size() > 0) {
 			DialogInterface.OnClickListener listener_2 = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which == Dialog.BUTTON_POSITIVE) {
 
 						relativeArmPosition temp_move_data;
 
 						// reconstruct current position
 						while (arm_states.size() > 0) {
 							temp_move_data = arm_states.removeLast();
 							armMovementTracker[0] += temp_move_data.x_pos;
 							armMovementTracker[1] += temp_move_data.y_pos;
 							armMovementTracker[2] += temp_move_data.z_pos;
 							armMovementTracker[3] += temp_move_data.roll_pos;
 							armMovementTracker[4] += temp_move_data.pitch_pos;
 						}
 
 						// arm_states.clear();
 						num_saves = 0;
 
 						if (doSound) {
 							if (mp != null) {
 								mp.release();
 							}
 							mp = MediaPlayer.create(getApplicationContext(),
 									R.raw.all_deleted);
 							mp.start();
 						}
 						Toast.makeText(getApplicationContext(),
 								"All saved positions deleted",
 								Toast.LENGTH_SHORT).show();
 
 					} else if (which == Dialog.BUTTON_NEUTRAL) {
 						relativeArmPosition temp_move_data = arm_states
 								.removeLast();
 						int save_id = temp_move_data.save_count;
 						armMovementTracker[0] += temp_move_data.x_pos;
 						armMovementTracker[1] += temp_move_data.y_pos;
 						armMovementTracker[2] += temp_move_data.z_pos;
 						armMovementTracker[3] += temp_move_data.roll_pos;
 						armMovementTracker[4] += temp_move_data.pitch_pos;
 
 						// Why remove twice? -Hao
 						if (arm_states.size() > 0) {
 							temp_move_data = arm_states.getLast();
 							if (temp_move_data.save_count == save_id) {
 								armMovementTracker[0] += temp_move_data.x_pos;
 								armMovementTracker[1] += temp_move_data.y_pos;
 								armMovementTracker[2] += temp_move_data.z_pos;
 								armMovementTracker[3] += temp_move_data.roll_pos;
 								armMovementTracker[4] += temp_move_data.pitch_pos;
 
 								arm_states.removeLast();
 							}
 						}
 
 						// gripper update
 						if (arm_states.size() > 0) {
 							temp_move_data = arm_states.getLast();
 							if (temp_move_data.save_count == save_id) {
 								arm_states.removeLast();
 							}
 						}
 
 						num_saves--;
 
 						if (doSound) {
 							if (mp != null) {
 								mp.release();
 							}
 							mp = MediaPlayer.create(getApplicationContext(),
 									R.raw.prev_deleted);
 							mp.start();
 						}
 						Toast.makeText(getApplicationContext(),
 								"Previous saved position deleted",
 								Toast.LENGTH_SHORT).show();
 					}
 				}
 			};
 
 			new AlertDialog.Builder(v.getContext())
 					.setMessage(R.string.trash_dialog_message)
 					.setPositiveButton(R.string.delete_all, listener_2)
 					.setNeutralButton(R.string.delete_one, listener_2)
 					.setNegativeButton(R.string.cancel, listener_2)
 					.setTitle(R.string.trash_dialog_title).show();
 
 		} else {
 			Toast.makeText(getApplicationContext(), "No positions to delete",
 					Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	/** Called when the user clicks the Lock Axis button */
 	public void lockAxis(View view) {
 		Intent intent = new Intent(this, LockAxis.class);
 		startActivity(intent);
 	}
 
 	public void startSensors() {
 		if (!sensorsStarted) {
 			sensorsStarted = true;
 			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 			mAccelerometer = mSensorManager
 					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 			// mOrientation = mSensorManager
 			// .getDefaultSensor(Sensor.TYPE_ORIENTATION);
 			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 			mRotation = mSensorManager
 					.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 			mSensorManager.registerListener(this, mAccelerometer,
 					SensorManager.SENSOR_DELAY_GAME);
 			// mSensorManager.registerListener(this, mOrientation,
 			// SensorManager.SENSOR_DELAY_NORMAL);
 			mSensorManager.registerListener(this, mGyroscope,
 					SensorManager.SENSOR_DELAY_GAME);
 			mSensorManager.registerListener(this, mRotation,
 					SensorManager.SENSOR_DELAY_GAME);
 		}
 	}
 
 	public void stopSensors() {
 		if (sensorsStarted) {
 			sensorsStarted = false;
 			mSensorManager.unregisterListener(this);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("Disconnect").setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						disconnectFromServer();
 						return false;
 					}
 
 				});
 		menu.add("Calibrate").setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 
 					public boolean onMenuItemClick(MenuItem item) {
 						Intent intent = new Intent(context, Calibration.class);
 						startActivity(intent);
 
 						return true;
 					}
 				});
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_settings) {
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 		}
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 
 		modeSwitch.setOnCheckedChangeListener(null);
 
 		startSensors();
 
 		if (armMode) {
 			modeSwitch.setChecked(false);
 		} else {
 			modeSwitch.setChecked(true);
 		}
 
 		modeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				if (isChecked) { // wrist mode
 					if (doSound) {
 						if (mp != null) {
 							mp.release();
 						}
 						mp = MediaPlayer.create(getApplicationContext(),
 								R.raw.wrist_mode);
 						mp.start();
 					}
 					setWristMode();
 				} else { // arm mode
 					if (doSound) {
 						if (mp != null) {
 							mp.release();
 						}
 						mp = MediaPlayer.create(getApplicationContext(),
 								R.raw.arm_mode);
 						mp.start();
 					}
 					setArmMode();
 				}
 			}
 		});
 
 		if (serverSettingsChanged) {
 			serverSettingsChanged = false;
 			disconnectFromServer();
 		}
 
 		if (serverIP.equals("")) {
 			setMainButton_noIPMode();
 		} else if (!socketConnected) {
 			new ConnectToServer().execute();
 		}
 
 		myTimer = new Timer();
 		doSendTimerTask myTask = new doSendTimerTask();
 		myTimer.schedule(myTask, 0, Integer.parseInt(period));
 
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		stopSensors();
 		myTimer.cancel();
 		myTimer.purge();
 		v1.cancel();
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		disconnectFromServer();
 		super.onDestroy();
 	}
 
 	public class playbackMode extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			relativeArmPosition temp_arm_state;
 			Iterator<relativeArmPosition> stateIterator = arm_states.iterator();
 			while ((socketConnected) && (doPlayback)) {
 
 				// temp for testing
 				/*
 				 * try { Thread.sleep(2000); } catch (InterruptedException e) {
 				 * // TODO Auto-generated catch block e.printStackTrace(); }
 				 */
 
 				while (!playbackMoveDone) {
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				if ((doPlayback) && (stateIterator.hasNext())) {
 					temp_arm_state = stateIterator.next();
 					synchronized (lock) {
 						displacement[0] = temp_arm_state.x_pos;
 						displacement[1] = temp_arm_state.y_pos;
 						displacement[2] = temp_arm_state.z_pos;
 						rollAngle = temp_arm_state.roll_pos;
 						pitchAngle = temp_arm_state.pitch_pos;
 					}
 					gripperBar.setProgress((int) temp_arm_state.grip_pos);
 					grip = temp_arm_state.grip_pos;
 					playbackMoveDone = false;
 				} else {
 					doPlayback = false;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			grip = -300; // home arm first
 			gripperBar.setProgress(0);
 			playbackMoveDone = false;
 			
 			// disable a bunch of buttons
 			homeButton.setEnabled(false);
 			lockButton.setEnabled(false);
 			gripperBar.setEnabled(false);
 			modeSwitch.setEnabled(false);
 			recordButton.setEnabled(false);
 			trashButton.setEnabled(false);
 			
 			myMainButton.setBackgroundColor(Color.RED);
 			myMainButton.setText(R.string.playback_text);
 			myMainButton.setOnTouchListener(new OnTouchListener() {
 				public boolean onTouch(View v, MotionEvent event) {
 
 					switch (event.getAction()) {
 					case MotionEvent.ACTION_DOWN: {
 						doPlayback = false;
 						playbackMoveDone = true;
 						myMainButton.setText("Canceling...");
 						myMainButton.setOnTouchListener(null);
 						return true;
 					}
 					default:
 						return false;
 					}
 				}
 			});
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			Toast.makeText(getApplicationContext(), "Playback complete",
 					Toast.LENGTH_SHORT).show();
 			
 			// enable a bunch of buttons
 			homeButton.setEnabled(true);
 			lockButton.setEnabled(true);
 			gripperBar.setEnabled(true);
 			modeSwitch.setEnabled(true);
 			recordButton.setEnabled(true);
 			trashButton.setEnabled(true);
 			
 			if (doSound) {
 				if (mp != null) {
 					mp.release();
 				}
 				mp = MediaPlayer.create(getApplicationContext(),
 						R.raw.playback_complete);
 				mp.start();
 			}
 			if (socketConnected) {
 				setMainButton_mainMode();
 			}
 			doPlayback = false;
 			grip = gripperBar.getProgress();
 			// previousGrip = 0.0f;
 
 			for (int i = 0; i < 5; i++) {
 				armMovementTracker[i] = 0.0f;
 			}
 		}
 
 	}
 
 	public class readFromServer extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			if (socketConnected) {
 				try {
 					int inVal = in.read();
 					Log.d("inVal", "" + inVal);
 					if (inVal == 2) {
 						playbackMoveDone = true;
						speed[0] = 0;
						speed[1] = 0;
						speed[2] = 0;
 					} else if (inVal != -1) {
 						int dot = 200;
 						int dash = 500;
 						int short_gap = 100;
 						long[] pattern = { 0, dot, short_gap, dash };
 						// Only perform this pattern one time (-1 means
 						// "do not repeat")
 						v1.vibrate(pattern, -1);
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			new readFromServer()
 					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 		}
 	}
 
 	public class ConnectToServer extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			Log.d("DEBUG: ", "Before connection");
 
 			try {
 				mySocket = new Socket();
 				mySocket.connect(
 						new InetSocketAddress(serverIP, Integer
 								.parseInt(serverPort)), 2000);
 				out = mySocket.getOutputStream();
 				in = mySocket.getInputStream();
 				socketConnected = true;
 				Log.d("DEBUG: ", "Tried Connection");
 			} catch (IOException e) {
 				Log.e("ERR: ", e.getMessage());
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			if (doSound) {
 				if (mp != null) {
 					mp.release();
 				}
 				mp = MediaPlayer.create(getApplicationContext(),
 						R.raw.connecting);
 				mp.start();
 
 			}
 			gripperBar.setEnabled(false);
 			myMainButton.setBackgroundColor(Color.YELLOW);
 			myMainButton.setText(R.string.connecting);
 			myMainButton.setOnTouchListener(null);
 			connecting = true;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (!socketConnected) {
 				if (doSound) {
 					if (mp != null) {
 						mp.release();
 					}
 					mp = MediaPlayer.create(getApplicationContext(),
 							R.raw.connection_failed);
 					mp.start();
 
 				}
 				gripperBar.setEnabled(false);
 				if (serverIP.equals("")) {
 					setMainButton_noIPMode();
 				} else {
 					reconnectButtonSet = false;
 				}
 			} else {
 				if (doSound) {
 					if (mp != null) {
 						mp.release();
 					}
 					mp = MediaPlayer.create(getApplicationContext(),
 							R.raw.arm_connected);
 					mp.start();
 
 				}
 				gripperBar.setEnabled(true);
 				reconnectButtonSet = false;
 				setMainButton_mainMode();
 			}
 			connecting = false;
 		}
 	}
 	
 	public void setMainButton_noIPMode() {
 		gripperBar.setEnabled(false);
 		myMainButton.setBackgroundColor(Color.YELLOW);
 		myMainButton.setText(R.string.no_ip);
 		if (doSound) {
 			if (mp != null) {
 				mp.release();
 			}
 			mp = MediaPlayer.create(getApplicationContext(),
 					R.raw.server_ip);
 			mp.start();
 		}
 		myMainButton.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_UP: {
 					Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
 					startActivity(intent);
 					return true;
 				}
 				default:
 					return false;
 				}
 			}
 		});
 
 	}
 
 	public void setMainButton_mainMode() {
 		myMainButton.setBackgroundColor(Color.GREEN);
 		myMainButton.setText(R.string.start_stop);
 
 		myMainButton.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN: {
 
 					// beep
 					if (doSound) {
 						if (mp != null) {
 							mp.release();
 						}
 						mp = MediaPlayer.create(getApplicationContext(),
 								R.raw.robot_blip);
 						mp.start();
 
 					}
 
 					// Vibrate for 50 milliseconds
 					if (doVibrate) {
 						v1.vibrate(50);
 					}
 
 					gripperBar.setEnabled(false);
 					homeButton.setEnabled(false);
 					lockButton.setEnabled(false);
 					setOnMainButton();
 
 					tracking = true;
 					rotationVector = new float[3];
 					acceleration = new float[4];
 					lastMeasurement1 = System.nanoTime();
 					lastMeasurement2 = System.nanoTime();
 
 					synchronized (lock) {
 						for (int i = 0; i < 3; i++) {
 							speed[i] = 0;
 							previous_speed[i] = 0.0f;
 							previous_acceleration[i] = 0.0f;
 						}
 						previousRollAngle = 0.0f;
 						previousPitchAngle = 0.0f;
 					}
 
 					return true;
 				}
 
 				case MotionEvent.ACTION_UP: {
 
 					gripperBar.setEnabled(true);
 					homeButton.setEnabled(true);
 					lockButton.setEnabled(true);
 					resetMainButton();
 					v1.cancel();
 					tracking = false;
 
 					return true;
 				}
 
 				default:
 					return false;
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.equals(mGyroscope)) {
 			long timeInterval = event.timestamp - lastMeasurement2;
 			lastMeasurement2 = event.timestamp;
 
 			if (tracking) {
 				float roll = event.values[1]; // around axis y
 				float pitch = event.values[0]; // around axis x
 				if (roll < 0.005 && roll > -0.005) {
 					roll = 0;
 				}
 				if (pitch < 0.005 & pitch > -0.005) {
 					pitch = 0;
 				}
 				// Assume the device has been turning with same speed for the
 				// whole
 				// interval
 				/*
 				 * if (!rollLocked) { roll = (float) (roll * timeInterval *
 				 * NS2S); rollAngle = rollAngle + roll; }
 				 * 
 				 * if (!pitchLocked) { pitch = (float) (pitch * timeInterval *
 				 * NS2S); pitchAngle = pitchAngle + pitch; }
 				 */
 
 				// Average previous speed and current speed
 				if (!rollLocked) {
 					rollAngle = rollAngle
 							+ ((float) (((roll + previousRollAngle) / 2.0f)
 									* timeInterval * NS2S));
 				}
 
 				if (!pitchLocked) {
 					pitchAngle = pitchAngle
 							+ ((float) (((pitch + previousPitchAngle) / 2.0f)
 									* timeInterval * NS2S));
 				}
 
 				previousRollAngle = roll;
 				previousPitchAngle = pitch;
 			}
 
 			// Log.d("CHECK: ", "The current roll value is " + rollAngle * 360 /
 			// 2 / Math.PI);
 
 			// float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip };
 			// doSend(outFloatData);
 		}
 
 		if (event.sensor.equals(mAccelerometer)) {
 
 			long timeInterval = event.timestamp - lastMeasurement1;
 			lastMeasurement1 = event.timestamp;
 
 			if (tracking) {
 				float[] rawLinear = { event.values[0], event.values[1],
 						event.values[2], 0 };
 				for (int i = 0; i < 3; i++) {
 					rawLinear[i] = rawLinear[i] - offset[i];
					
					if (rawLinear[i] < 0.2 && rawLinear[i] > -0.2) {
					rawLinear[i] = 0; }
					 
 				}
 				float[] temp = new float[16];
 				rotationMatrix = new float[16];
 				SensorManager.getRotationMatrixFromVector(temp, rotationVector);
 				Matrix.invertM(rotationMatrix, 0, temp, 0);
 				Matrix.multiplyMV(acceleration, 0, rotationMatrix, 0,
 						rawLinear, 0);
 
 				synchronized (lock) {
 
 					float speed_decay = (float) (0.1 * timeInterval * NS2S);
 					// float speed_decay = 0.0f;
 
 					if ((!xAxisLocked)
 							&& (event.timestamp > sensorRestartTime[0])) {
 						speed[0] = (float) (((acceleration[0] + previous_acceleration[0]) / 2.0f)
 								* timeInterval * NS2S)
 								+ speed[0];
 						if (Math.abs(speed[0]) < speed_decay) {
 							speed[0] = 0;
 						} else if (speed[0] < 0) {
 							speed[0] += speed_decay;
 						} else {
 							speed[0] -= speed_decay;
 						}
 						displacement[0] = (float) (((speed[0] + previous_speed[0]) / 2.0f)
 								* timeInterval * NS2S)
 								+ displacement[0];
 					}
 
 					if ((!yAxisLocked)
 							&& (event.timestamp > sensorRestartTime[1])) {
 						speed[1] = (float) (((acceleration[1] + previous_acceleration[1]) / 2.0f)
 								* timeInterval * NS2S)
 								+ speed[1];
 						if (Math.abs(speed[1]) < speed_decay) {
 							speed[1] = 0;
 						} else if (speed[1] < 0) {
 							speed[1] += speed_decay;
 						} else {
 							speed[1] -= speed_decay;
 						}
 						displacement[1] = (float) (((speed[1] + previous_speed[1]) / 2.0f)
 								* timeInterval * NS2S)
 								+ displacement[1];
 					}
 
 					if ((!zAxisLocked)
 							&& (event.timestamp > sensorRestartTime[2])) {
 						speed[2] = (float) (((acceleration[2] + previous_acceleration[2]) / 2.0f)
 								* timeInterval * NS2S)
 								+ speed[2];
 						if (Math.abs(speed[2]) < speed_decay) {
 							speed[2] = 0;
 						} else if (speed[2] < 0) {
 							speed[2] += speed_decay;
 						} else {
 							speed[2] -= speed_decay;
 						}
 						displacement[2] = (float) (((speed[2] + previous_speed[2]) / 2.0f)
 								* timeInterval * NS2S)
 								+ displacement[2];
 					}
 
 					for (int i = 0; i < 3; i++) {
 						previous_speed[i] = speed[i];
 						previous_acceleration[i] = acceleration[i];
 					}
 				}
 			}
 			// Log.d("CHECK:", "x = " + displacement[0] + "; y = " +
 			// displacement[1] + "; z = " + displacement[2]);
 		}
 
 		if (event.sensor.equals(mRotation) && tracking) {
 			for (int i = 0; i < 3; i++) {
 				rotationVector[i] = event.values[i];
 			}
 		}
 
 	}
 
 	public static void disconnectFromServer() {
 		homeButton.setEnabled(true);
 		lockButton.setEnabled(true);
 		if (socketConnected) {
 			socketConnected = false;
 
 			// send disconnect signal
 			float[] outFloatData = { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 200.0f };
 			for (int i = 0; i < outFloatData.length; i++) {
 				int data = Float.floatToRawIntBits(outFloatData[i]);
 				byte outByteData[] = new byte[4];
 				outByteData[3] = (byte) (data >> 24);
 				outByteData[2] = (byte) (data >> 16);
 				outByteData[1] = (byte) (data >> 8);
 				outByteData[0] = (byte) (data);
 				try {
 					MainActivity.out.write(outByteData);
 				} catch (IOException e) {
 					MainActivity.disconnectFromServer();
 				}
 			}
 			
 			try {
 				out.flush();
 				out.close();
 				in.close();
 				mySocket.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			if (doSound) {
 				if (mp != null) {
 					mp.release();
 				}
 				mp = MediaPlayer
 						.create(mainActivityContext, R.raw.arm_disconnected);
 				mp.start();
 			}
 			
 		} else if (connecting) {
 			connecting = false;
 		}
 	}
 
 	protected void checkConnectionStatus() {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				if ((!socketConnected) && (!connecting)
 						&& (!reconnectButtonSet) && (!serverIP.equals(""))) {
 					gripperBar.setEnabled(false);
 					myMainButton.setText(R.string.connect_error);
 					myMainButton.setBackgroundColor(Color.YELLOW);
 					myMainButton.setOnTouchListener(new OnTouchListener() {
 						public boolean onTouch(View v, MotionEvent event) {
 							switch (event.getAction()) {
 							case MotionEvent.ACTION_UP: {
 								// Vibrate for 50 milliseconds
 								if (doVibrate) {
 									v1.vibrate(50);
 								}
 								new ConnectToServer().execute();
 							}
 							}
 							return true;
 						}
 					});
 					reconnectButtonSet = true;
 				}
 			}
 		});
 	}
 
 	private void setWristMode() {
 		xAxisLocked = true;
 		yAxisLocked = true;
 		zAxisLocked = true;
 		if (rollBoxChecked) {
 			rollLocked = true;
 		} else {
 			rollLocked = false;
 		}
 		if (pitchBoxChecked) {
 			pitchLocked = true;
 		} else {
 			pitchLocked = false;
 		}
 
 		armMode = false;
 	}
 
 	private void setArmMode() {
 		if (xBoxChecked) {
 			xAxisLocked = true;
 		} else {
 			xAxisLocked = false;
 		}
 		if (yBoxChecked) {
 			yAxisLocked = true;
 		} else {
 			yAxisLocked = false;
 		}
 		if (zBoxChecked) {
 			zAxisLocked = true;
 		} else {
 			zAxisLocked = false;
 		}
 		rollLocked = true;
 		pitchLocked = true;
 
 		armMode = true;
 	}
 }
 
 class doSendTimerTask extends TimerTask {
 	public void run() {
 		if (MainActivity.socketConnected) {
 			boolean gripSignalMode = false;
 			if ((MainActivity.grip > 100) || (MainActivity.grip < 0)) {
 				gripSignalMode = true;
 			}
 			// Log.d("x displacement", "" + MainActivity.displacement[0]);
 
 			float[] outFloatData = new float[6];
 			synchronized (MainActivity.lock) {
 
 				if (!MainActivity.doPlayback) {
 					for (int i = 0; i < 3; i++) {
 						MainActivity.displacement[i] = MainActivity.displacement[i]
 								/ (1.0f + (float) (MainActivity.sensitivity / 25.0f));
 					}
 				}
 				outFloatData[0] = MainActivity.rollAngle;
 				outFloatData[1] = MainActivity.pitchAngle;
 				outFloatData[2] = MainActivity.displacement[0];
 				outFloatData[3] = MainActivity.displacement[1];
 				outFloatData[4] = MainActivity.displacement[2];
 				outFloatData[5] = MainActivity.grip;
 
 				if (MainActivity.displacement[0] != 0
 						|| MainActivity.displacement[1] != 0
 						|| MainActivity.displacement[2] != 0) {
 					Log.d("Arm Mode:", "x = " + MainActivity.displacement[0]
 							+ "; y = " + MainActivity.displacement[1]
 							+ "; z = " + MainActivity.displacement[2]);
 				}
 				if (MainActivity.rollAngle != 0 || MainActivity.pitchAngle != 0) {
 					Log.d("Wrist Mode", "roll = " + MainActivity.rollAngle
 							+ "; pitch = " + MainActivity.pitchAngle
 							+ "; grip = " + MainActivity.grip);
 				}
 
 				// add to accumulation of arm movements
 				for (int i = 0; i < 3; i++) {
 					MainActivity.armMovementTracker[i] += MainActivity.displacement[i];
 				}
 				MainActivity.armMovementTracker[3] += MainActivity.rollAngle;
 				MainActivity.armMovementTracker[4] += MainActivity.pitchAngle;
 
 				MainActivity.displacement[0] = 0.0f;
 				MainActivity.displacement[1] = 0.0f;
 				MainActivity.displacement[2] = 0.0f;
 				// MainActivity.speed[0] = 0.0f;
 				// MainActivity.speed[1] = 0.0f;
 				// MainActivity.speed[2] = 0.0f;
 
 				MainActivity.rollAngle = 0;
 				MainActivity.pitchAngle = 0;
 			}
 
 			for (int i = 0; i < outFloatData.length; i++) {
 				int data = Float.floatToRawIntBits(outFloatData[i]);
 				byte outByteData[] = new byte[4];
 				outByteData[3] = (byte) (data >> 24);
 				outByteData[2] = (byte) (data >> 16);
 				outByteData[1] = (byte) (data >> 8);
 				outByteData[0] = (byte) (data);
 				try {
 					MainActivity.out.write(outByteData);
 				} catch (IOException e) {
 					MainActivity.disconnectFromServer();
 				}
 			}
 			try {
 				MainActivity.out.flush();
 				if (gripSignalMode) {
 					MainActivity.grip = MainActivity.gripperBar.getProgress();
 				}
 			} catch (IOException e) {
 				MainActivity.disconnectFromServer();
 			}
 		} else {
 			MainActivity.grip = MainActivity.gripperBar.getProgress(); // reset
 																		// Home
 																		// or
 																		// Disconnect
 																		// signals
 																		// if
 																		// not
 																		// connected
 		}
 	}
 }
 
 class relativeArmPosition {
 	public float x_pos;
 	public float y_pos;
 	public float z_pos;
 	public float roll_pos;
 	public float pitch_pos;
 	public float grip_pos;
 	public int save_count;
 
 	public relativeArmPosition(float x, float y, float z, float roll,
 			float pitch, float grip, int saveNumber) {
 		x_pos = x;
 		y_pos = y;
 		z_pos = z;
 		roll_pos = roll;
 		pitch_pos = pitch;
 		grip_pos = grip;
 		save_count = saveNumber;
 	}
 }
