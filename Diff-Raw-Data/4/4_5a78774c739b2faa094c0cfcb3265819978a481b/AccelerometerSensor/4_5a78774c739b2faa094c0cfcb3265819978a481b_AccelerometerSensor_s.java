 package edu.incense.android.sensor;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 import edu.incense.android.datatask.data.AccelerometerData;
 
 /**
  * Stores new values sensed by the accelerometer in a AccelerometerData object.
  * @author mxpxgx
  * @version 1.1, 05/09/2011
  */
 
 public class AccelerometerSensor extends edu.incense.android.sensor.Sensor
         implements SensorEventListener {
     private static final String TAG = "AccelerometerSensor";
     private SensorManager sm;
     private Sensor accelerometer;
    private int sensorType;
 
    public AccelerometerSensor(Context context, int SensorType) {
         super(context);
 
         // Accelerometer initialization
         String service = Context.SENSOR_SERVICE;
         sm = (SensorManager) context.getSystemService(service);
 //        sensorType = Sensor.TYPE_ACCELEROMETER;
         accelerometer = sm.getDefaultSensor(sensorType);
         Log.d(TAG, "Sensor name: " + accelerometer.getName());
     }
     
     public static AccelerometerSensor createAccelerometer(Context context){
         AccelerometerSensor sensor = new AccelerometerSensor(context, Sensor.TYPE_ACCELEROMETER);
         return sensor;
     }
     
     public static AccelerometerSensor createGyroscope(Context context){
         AccelerometerSensor sensor = new AccelerometerSensor(context, Sensor.TYPE_GYROSCOPE);
         return sensor;
     }
 
     @Override
     public void start() {
         boolean success = sm.registerListener(this, accelerometer,
                 SensorManager.SENSOR_DELAY_NORMAL);
         if (success){
             super.setSensing(true);
             Log.d(TAG, "SensorEventLister registered!");
         } else {
             super.setSensing(false);
             Log.d(TAG, "SensorEventLister NOT registered!");
         }
     }
 
     @Override
     public void stop() {
         sm.unregisterListener(this);
         super.setSensing(false);
     }
 
     /**
      * Stores new axis values in a AccelerometerData object.
      * @param newX
      * @param newY
      * @param newZ
      */
     private void setNewReadings(float newX, float newY, float newZ, long timestamp) {
         currentData = new AccelerometerData(newX, newY, newZ);
         ((AccelerometerData)currentData).setTimestamp(timestamp);
     }
 
     /* SensorEventListener methods */
     
     /**
      *  Stores new accelerometer values when a change is sensed
      */
     public void onSensorChanged(SensorEvent event) {
         if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             float xAxis_lateralA = event.values[0];
             float yAxis_longitudinalA = event.values[1];
             float zAxis_verticalA = event.values[2];
             setNewReadings(xAxis_lateralA, yAxis_longitudinalA, zAxis_verticalA, event.timestamp);
         }
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
 }
