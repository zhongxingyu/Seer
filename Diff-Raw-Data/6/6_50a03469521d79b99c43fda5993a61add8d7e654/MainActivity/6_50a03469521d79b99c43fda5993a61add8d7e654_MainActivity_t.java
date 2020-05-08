 package pt.snowplow.dodgingxmas;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 
 public class MainActivity extends Activity implements SensorEventListener {
 
 	private MainView mainView;
 
 	private SensorManager sensorManager;
 	private Sensor sensorAccelerometer;
 	private Sensor sensorMagneticField;
 	private float[] tiltData = {0, 0, 0};
 	private float[] gravityData = {0, 0, 0};
 	private float[] magneticData = {0, 0, 0};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		mainView = (MainView) findViewById(R.id.main_view);
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		sensorAccelerometer = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
 		sensorMagneticField = sensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
 		enableSensor();
 	}
 
 	private void enableSensor() {
 		sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 		sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	public void disableSensor() {
 		sensorManager.unregisterListener(this);	
 	}
 
 	@Override
 	public final void onSensorChanged(SensorEvent event) {
 		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			System.arraycopy(event.values, 0, gravityData, 0, 3);
 		} else {
 			System.arraycopy(event.values, 0, magneticData, 0, 3);
 		}
 
 		float[] R={0,0,0,0,0,0,0,0,0};
 
 		if(SensorManager.getRotationMatrix(R, null, gravityData, magneticData)) {
 			SensorManager.getOrientation(R, tiltData);
 		}
 
		mainView.setBallXvelocity((int) (tiltData[2]*10));
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	protected void onPause() {
 		disableSensor();
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		disableSensor();
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		disableSensor();
 		super.onDestroy();
 	}
 
 }
