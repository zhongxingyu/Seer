 package com.example.gamecontrollerdatatransfer;
 
 import gc.common_resources.CommandType;
 
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlayActivity extends Activity {
 
 	private String ipAddress,  connectType;
 	private final float NOISE = (float) 2.0;
 	Socket socket = null;
 	private boolean connected = false;
 	private SensorManager mSensorManager;
 	private MyRenderer mRenderer;
 	private boolean isStartPositionRegistered;
 	private float startX = 0;
 	private float startY = 0;
 	private float startZ = 0;
 	private boolean flag = false;
 	private final Context context = this;
 	private boolean isPaused= false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
 		// Create our Preview view and set it as the content of our
 		// Activity
 		mRenderer = new MyRenderer();
 		isStartPositionRegistered = false;
 		setContentView(R.layout.activity_play);
 		/* do this in onCreate */
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			connectType = extras.getString("connectType");
 			
 			if(connectType == "wifi")
 				ipAddress = extras.getString("ipAddress");
 		}
 
 		registerRestPosition();
 
 		ImageButton closeButton = (ImageButton) findViewById(R.id.buttoncloseicon);
 		closeButton.setOnClickListener(buttonCloseIcon);
 		
 		final ImageButton resetButton = (ImageButton) findViewById(R.id.buttonreset);
 		resetButton.setOnClickListener(new View.OnClickListener() {
 
 	        @Override
 	        public void onClick(View v) {
 	        	flag=false;
 	        	isStartPositionRegistered=false;
 	        	registerRestPosition();
 	        }
 	        });
 
 		final ImageButton playPauseButton = (ImageButton) findViewById(R.id.buttonplaypause);
 		playPauseButton.setOnClickListener(new View.OnClickListener() {
 
 	        @Override
 	        public void onClick(View v) {
 
 	            if(isPaused) {
 	            	// play it 
 	            	playPauseButton.setImageResource(R.drawable.pausebutton);
 	            	Toast.makeText(getApplicationContext(), "Tilt detection enabled", Toast.LENGTH_SHORT).show();
 	            	isPaused= false;
 	            	onResume();
 	            } else {
 	            	playPauseButton.setImageResource(R.drawable.playbutton);
 	            	Toast.makeText(getApplicationContext(), "Tilt detection disabled", Toast.LENGTH_SHORT).show();
 	            	isPaused= true;
 	            	onPause();
 	            }
 	        }
 	    });
 
 	}
 
 	public void registerRestPosition() {
 		// record initial coordinates
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle("Choose your position");
 		builder.setMessage(R.string.get_rest_position_message)
 				.setPositiveButton(R.string.ok,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int id) { //
 								// User clicked OK, so save the mSelectedItems
 								// results somewhere // or
 								// return them to the component that opened the
 								// dialog
 
 								System.out.println("Register x and y here");
 								flag = true;
 								System.out.println("START VALUES" + startX
 										+ " " + startY);
 							}
 						});
 				/*.setNegativeButton(R.string.cancel,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								Toast.makeText(getApplicationContext(),
 										"Tilt Detection disabled",
 										Toast.LENGTH_SHORT).show();
 							}
 						});
 */
 		builder.show();
 	}
 
 	ImageButton.OnClickListener buttonCloseIcon = new ImageButton.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setTitle("Quit playing?");
 			builder.setMessage("Are you sure you want to quit this session?")
 					.setPositiveButton(R.string.ok,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) { //
 									Intent k = new Intent(PlayActivity.this,
 											MainActivity.class);
 									startActivity(k);
 								}
 							})
 					.setNegativeButton(R.string.cancel,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// NOP
 								}
 							});
 			AlertDialog alertDialog = builder.create();
 
 			alertDialog.show();
 
 		}
 	};
 
 	@Override
 	public void onBackPressed() {
 		// diable back button
 		Toast.makeText(getApplicationContext(),
 				"Back button disabled. \nPlease use the close button above.",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	public void updateCoordinates(TouchCoordinates tc, CommandType newCommand) {
 		// We don't keep a set of tc and commandtype in this class anymore
 		// So this function only packs the command with fields and
 		// passes the data to sendPacketToServer()
 		CommandType updateCommand = newCommand;
 		updateCommand.setX(tc.getX());
 		updateCommand.setY(tc.getY());
 		updateCommand.setZ(tc.getZ());
 		updateCommand.setW(tc.getW());
 
 		sendPacketToServer(updateCommand);
 	}
 
 	// Create the threads and pass the commands to them
 	public void sendPacketToServer(CommandType updateCommand) {
 		if(connectType == "wifi") {
			if(connectType.equals("wifi")) {
 			if (!connected) {
 				Thread cThread = new Thread(new ClientSocketThread(updateCommand));
 				cThread.start();
 			}
		}else if(connectType.equals("bluetooth")) {
			SingletonBluetooth.getInstance().sendToDevice(updateCommand);
		}
 	}
 
 	// Thread class to send commands to the Server
 	public class ClientSocketThread implements Runnable {
 
 		private CommandType commandToSend;
 
 		public ClientSocketThread(CommandType currCommand) {
 			commandToSend = currCommand;
 
 			Log.d("PlayActivity - ThreadConstructor", "commandToSend: "
 					+ commandToSend + " " + commandToSend.getX() + " "
 					+ commandToSend.getY());
 		}
 
 		public void run() {
 			try {
 				Socket socket = new Socket(ipAddress, 8888);
 				ObjectOutputStream objOutputStream = new ObjectOutputStream(
 						socket.getOutputStream());
 				connected = true;
 
 				try {
 					Log.d("PlayActivity", "Sending: " + commandToSend + " "
 							+ commandToSend.getX() + " " + commandToSend.getY());
 
 					// Send the enum(commandToSend) and the fields(X and Y)
 					// separately
 					// as the serializing and deserializing of enum through
 					// ObjectOutputStream
 					// and ObjectInputStream will not save the fields in the
 					// enum
 					objOutputStream.writeObject(commandToSend);
 					objOutputStream.writeFloat(commandToSend.getX());
 					objOutputStream.writeFloat(commandToSend.getY());
 				} catch (Exception e) {
 					Log.e("PlayActivity", "S: Error", e);
 				}
 				// Close outputstream and socket
 				objOutputStream.close();
 				socket.close();
 				connected = false;
 			} catch (Exception e) {
 				Log.e("PlayActivity", "C: Error", e);
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		// Ideally a game should implement onResume() and onPause()
 		// to take appropriate action when the activity looses focus
 		super.onResume();
 		mRenderer.start();
 	}
 
 	@Override
 	protected void onPause() {
 		// Ideally a game should implement onResume() and onPause()
 		// to take appropriate action when the activity looses focus
 		super.onPause();
 		mRenderer.stop();
 	}
 
 	class MyRenderer implements SensorEventListener {
 		private Sensor mRotationVectorSensor;
 
 		public MyRenderer() {
 			// find the rotation-vector sensor
 			mRotationVectorSensor = mSensorManager
 					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 
 		}
 
 		public void start() {
 			// enable our sensor when the activity is resumed, ask for
 			// 10 ms updates.
 			mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
 		}
 
 		public void stop() {
 			// make sure to turn our sensor off when the activity is paused
 			mSensorManager.unregisterListener(this);
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 
 			ImageView iv = (ImageView) findViewById(R.id.image);
 			TextView tvX = (TextView) findViewById(R.id.x_axis);
 			TextView tvY = (TextView) findViewById(R.id.y_axis);
 			TextView tvZ = (TextView) findViewById(R.id.z_axis);
 
 			View view = findViewById(R.id.playActivity);
 			float x = event.values[0];
 			float y = event.values[1];
 			float z = event.values[2];
 
 			/*
 			 * System.out.println("X= " + values[0] + " Y= " + values[1] +
 			 * " Z= " + values[2]);
 			 */
 			if (flag) {
 				if (!isStartPositionRegistered) {
 					tvX.setText("0.0");
 					tvY.setText("0.0");
 					tvZ.setText("0.0");
 					startX = x;
 					startY = y;
 					startZ = z;
 					isStartPositionRegistered = true;
 				}
 
 				else {
 					float deltaX = Math.abs(startX - x);
 					float deltaY = Math.abs(startY - y);
 					float deltaZ = Math.abs(startZ - z);
 
 					if (deltaX < NOISE)
 						deltaX = (float) 0.0;
 					if (deltaY < NOISE)
 						deltaY = (float) 0.0;
 					if (deltaZ < NOISE)
 						deltaZ = (float) 0.0;
 
 					tvX.setText(Float.toString(deltaX));
 					tvY.setText(Float.toString(deltaY));
 					tvZ.setText(Float.toString(deltaZ));
 					iv.setVisibility(View.VISIBLE);
 					/*
 					 * if (deltaZ > deltaX && deltaZ > deltaY) {
 					 * Toast.makeText(getApplicationContext(),
 					 * "Z axis movement", Toast.LENGTH_SHORT).show();
 					 * iv.setImageResource(R.drawable.ic_launcher); } else {
 					 */
 					if (deltaX > deltaY) {
 						/*
 						 * Toast.makeText(getApplicationContext(),
 						 * "X axis movement", Toast.LENGTH_SHORT) .show();
 						 */
 						iv.setImageResource(R.drawable.horizontal);
 						
 						if((startX-x)<0)
 							//down tilt
 							view.setBackgroundColor(Color.RED);
 						else
 							view.setBackgroundColor(Color.GREEN);
 						
 						System.out.println("Delta 1=" + (startX - x) + " Delta 2="
 								+ (startY - y) );
 						
 					} else if (deltaX < deltaY) {
 						/*
 						 * Toast.makeText(getApplicationContext(),
 						 * "Y axis movement", Toast.LENGTH_SHORT) .show();
 						 */
 						iv.setImageResource(R.drawable.vertical);
 						
 						if((startY-y)<0)
 							//down tilt
 							view.setBackgroundColor(Color.MAGENTA);
 						else
 							view.setBackgroundColor(Color.YELLOW);
 						
 						System.out.println("Delta 1=" + (startX - x) + " Delta 2="
 								+ (startY - y) );
 						
 					} else {
 						// NOP
 						iv.setVisibility(View.INVISIBLE);
 						view.setBackgroundColor(Color.TRANSPARENT);
 					}
 				}
 			}
 
 		}
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		}
 
 	}
 
 }
