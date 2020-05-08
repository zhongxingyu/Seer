 package fitnessapps.acceltest.activity;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.LocationManager;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 import fitnessapps.acceltest.activity.AccelerometerData;
 
 public class AccelerometerService extends Service implements
 		SensorEventListener {
 
 	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1;
 	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000;
 	private LocationManager manager;
 	private ArrayList<AccelerometerData> accelerationData;
 	private SensorManager sensorManager;
 	private Sensor sensorAccelerometer;
 	private String gameName;
 	private boolean endOfGame;
 	private String idNum;
 	private IAccelRemoteService.Stub accelRemoteService = new IAccelRemoteService.Stub() {
 
 		public void setGameNameFromService(String name) throws RemoteException {
 			gameName = name;
 
 		}
 
 		public void setEndGameFlagFromService(boolean flag)
 				throws RemoteException {
 			endOfGame = flag;
 
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.d("Service Bind", "onBind()");
 		return accelRemoteService; // object of the class that implements
 									// Service
 									// interface.
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onCreate() {
 		// code to execute when the service is first created
 		super.onCreate();
 		gameName = null;
 		endOfGame = false;
 	}
 
 	@Override
 	public void onDestroy() {
 		// code to execute when the service is shutting down
 
 		try {
 			sensorManager.unregisterListener(this);
 		} catch (NullPointerException e) {
 
 		}
 		(new Thread(new Runnable() {
 
 			public void run() {
 				try {
 					File card = Environment.getExternalStorageDirectory();
 					File directory = new File(card.getAbsolutePath()
 							+ "/test/accelerometerdata");
 					directory.mkdirs();
 
 					Date time = new Date();
 					String dayString = "";
 					int day = time.getDate();
 
 					if (day < 10) {
 						dayString = "0" + day;
 					} else {
 						dayString += day;
 					}
 
 					String monthString = "";
 					int month = time.getMonth() + 1;
 
 					if (month < 10) {
 						monthString = "0" + month;
 					} else {
 						monthString += month;
 					}
 					int year = time.getYear() + 1900;
 					String fileName = idNum + "_" + year + "-"
 							+ monthString + "-" + dayString + "_"
 							+ time.getHours() + "-" + time.getMinutes()
 							+ ".csv";
 
 					File sampleFile = new File(directory, fileName);
 					sampleFile.createNewFile();
 
 					FileWriter fw = new FileWriter(sampleFile);
 					fw.write("--------------------------------Data File Created by Motorola Droid Android v2.0.1-----------------------------------------\n");
 					fw.write("Epoch Period (hh:mm:ss) 00:00:15\n");
 					fw.write("---------------------------------------------------------------------------------------------------------------------------\n");
 					fw.write("Timestamp,X-Acceleration,Y-Acceleration,Z-Acceleration\n");
 					Log.d("SAMPLES", Integer.toString(accelerationData.size()));
 					for (AccelerometerData data : accelerationData) {
						if (data.getGameName() != null && data.getEndOfGame() != true) {
 							fw.write("-----------------"
 									+ data.getGameName()
 									+ " "
 									+ data.getTime()
 									+ " ------------------------------------------\n");
 						}
 						fw.write(data.getTime() + "," + data.getAccelerationX()
 								+ "," + data.getAccelerationY() + ","
 								+ data.getAccelerationZ() + "\n");
						if (data.getEndOfGame() == true && data.getGameName() != null) {
 							fw.write("-----------------END OF "
 									+ data.getGameName()
 									+ " "
 									+ data.getTime()
 									+ " ------------------------------------------\n");
 						}
 					}
 					fw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					Log.d("WRITEACCELEROMETER", "File write failed");
 				}
 			}
 		})).start();
 		super.onDestroy();
 		Toast.makeText(this, "Service destroyed...", Toast.LENGTH_LONG).show();
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startid) {
 		// code to execute when the service is starting up
 		idNum = intent.getStringExtra("IDNUMBER");
 		accelerationData = new ArrayList<AccelerometerData>();
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		sensorAccelerometer = sensorManager
 				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		sensorManager.registerListener(this, sensorAccelerometer,
 				SensorManager.SENSOR_DELAY_GAME);
 
 		Toast.makeText(this, "Service started...", Toast.LENGTH_LONG).show();
 
 		return startid;
 	}
 
 	private void accelerometerHandler(SensorEvent event) {
 		AccelerometerData data = new AccelerometerData(event.values[0],
 				event.values[1], event.values[2], new Date());
 		if (gameName != null && endOfGame != true) {
 			data.setGameName(gameName);
 			gameName = null;
 		}
 		if (endOfGame == true && gameName != null) {
 			data.setEndOfGame(endOfGame);
 			data.setGameName(gameName);
 			endOfGame = false;
 			gameName = null;
 		}
 		accelerationData.add(data);
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		switch (event.sensor.getType()) {
 		case Sensor.TYPE_ACCELEROMETER:
 			accelerometerHandler(event);
 			break;
 		}
 	}
 }
