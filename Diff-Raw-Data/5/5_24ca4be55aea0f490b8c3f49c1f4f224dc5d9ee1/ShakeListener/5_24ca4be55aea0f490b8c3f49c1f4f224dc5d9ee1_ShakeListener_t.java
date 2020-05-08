 package name.kropp.diceroller;
 
 /**
  * Created by IntelliJ IDEA.
  * User: kropp
  */
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.content.Context;
 
 public class ShakeListener implements SensorEventListener {
     private static final int FORCE_THRESHOLD = 350;
     private static final int TIME_THRESHOLD = 100;
     private static final int SHAKE_TIMEOUT = 500;
     private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;
 
     private SensorManager mySensorManager;
     private float myPreviousX = -1.0f;
     private float myPreviousY = -1.0f;
     private float myPreviousZ = -1.0f;
     private long myPreviousTime;
     private OnShakeListener myShakeListener;
     private int myShakeCount = 0;
     private long myPreviousShake;
     private long myPreviousForce;
     private Sensor mySensor;
 
     public interface OnShakeListener {
         public void onShake();
     }
 
     public ShakeListener(Context context) {
         mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
         resume();
     }
 
     public void setOnShakeListener(OnShakeListener listener) {
         myShakeListener = listener;
     }
 
     public void resume() {
         if (mySensorManager == null) {
             return;
         }
         mySensor = mySensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
         if (mySensor == null) {
             return;
         }
        boolean supported = mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
         if (!supported) {
             mySensorManager.unregisterListener(this, mySensor);
         }
     }
 
     public void pause() {
         if (mySensorManager != null && mySensor != null) {
             mySensorManager.unregisterListener(this, mySensor);
             mySensorManager = null;
             mySensor = null;
         }
     }
 
     public void onAccuracyChanged(Sensor sensor, int i) {
     }
 
     public void onSensorChanged(SensorEvent sensorEvent) {
         if (sensorEvent.sensor.getType() != SensorManager.SENSOR_ACCELEROMETER)
             return;
 
         long now = System.currentTimeMillis();
 
         if ((now - myPreviousForce) > SHAKE_TIMEOUT) {
             myShakeCount = 0;
         }
 
         float[] values = sensorEvent.values;
 
         if ((now - myPreviousTime) > TIME_THRESHOLD) {
             long diff = now - myPreviousTime;
             float speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - myPreviousX - myPreviousY - myPreviousZ) / diff * 10000;
             if (speed > FORCE_THRESHOLD) {
                 if ((++myShakeCount >= SHAKE_COUNT) && (now - myPreviousShake > SHAKE_DURATION)) {
                     myPreviousShake = now;
                     myShakeCount = 0;
                     if (myShakeListener != null) {
                         myShakeListener.onShake();
                     }
                 }
                 myPreviousForce = now;
             }
             myPreviousTime = now;
             myPreviousX = values[SensorManager.DATA_X];
             myPreviousY = values[SensorManager.DATA_Y];
             myPreviousZ = values[SensorManager.DATA_Z];
         }
     }
 }
