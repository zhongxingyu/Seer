 package de.karlsve.autolightsoff;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.admin.DevicePolicyManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 
 public class LightSwitcher extends Service implements SensorEventListener {
 
     private DevicePolicyManager deviceManager = null;
     private SensorManager sensorManager = null;
     private List<Sensor> registeredSensors = new ArrayList<Sensor>();
 
     private float difference = 450;
     private float accuracy = 180;
     private int delay = SensorManager.SENSOR_DELAY_GAME;
 
     private int currentUpdate = 0;
     private float lastMagneticFieldValue = 0;
 
     private boolean magneticClosed = false;
     private boolean lightClosed = false;
     @SuppressWarnings("unused")
     private boolean proximityClosed = false;
 
     private enum State {
         UNKNOWN, WOKEN, LOCKED
     }
 
     private State currentState = State.UNKNOWN;
 
     public class LightSwitcherBinder extends Binder {
         public LightSwitcher getService() {
             return LightSwitcher.this;
         }
     }
 
     IBinder binder = new LightSwitcherBinder();
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     @Override
     public void onAccuracyChanged(Sensor s, int acc) {
     }
     
     @Override 
     public void onCreate() {
         deviceManager = (DevicePolicyManager) this
                 .getSystemService(Context.DEVICE_POLICY_SERVICE);
         sensorManager = (SensorManager) this
                 .getSystemService(Context.SENSOR_SERVICE);
         Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
         sensorManager.registerListener(this, sensor, delay);
         registeredSensors.add(sensor);
         sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
         sensorManager.registerListener(this, sensor, delay);
         registeredSensors.add(sensor);
         // sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
         // sensorManager.registerListener(this, sensor,
         // delay);
         // registeredSensors.add(sensor);
         
         Intent notificationIntent = new Intent(this, LightSwitcherActivity.class);
         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         
         Notification note = new Notification.Builder(this)
                 .setContentTitle("AutoLightsOff")
                 .setContentText(
                         "To remove this, disable the notification in the settings.")
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setContentIntent(pendingIntent)
                 .build();
        note.flags|=Notification.FLAG_NO_CLEAR;
         this.startForeground(25, note);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return START_STICKY;
     }
     
     @Override
     public void onDestroy() {
         stopForeground(true);
     }
 
     @Override
     public void onSensorChanged(final SensorEvent se) {
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 switch (se.sensor.getType()) {
                 case Sensor.TYPE_PROXIMITY:
                     proximityClosed = se.values[0] == 0.0;
                     break;
                 case Sensor.TYPE_LIGHT:
                     lightClosed = se.values[0] == 0.0;
                     break;
                 case Sensor.TYPE_MAGNETIC_FIELD:
                     if (currentUpdate % 15 == 0) {
                         float sum = 0;
                         for (float value : se.values) {
                             sum += value;
                         }
                         float actualDifference = lastMagneticFieldValue > sum ? lastMagneticFieldValue
                                 - sum
                                 : sum - lastMagneticFieldValue;
                         float actualAccuracy = difference > actualDifference ? difference
                                 - actualDifference
                                 : actualDifference - difference;
                         if (lastMagneticFieldValue != 0) {
                             if (actualAccuracy >= 0
                                     && actualAccuracy <= accuracy + se.accuracy) {
                                 magneticClosed = lastMagneticFieldValue > sum ? false
                                         : true;
                             }
                         }
                         lastMagneticFieldValue = sum;
                     }
                     currentUpdate++;
                     break;
                 }
                 toggleLock();
             }
         };
        r.run();
     }
 
     protected void toggleLock() {
         if (currentState == State.UNKNOWN && magneticClosed && lightClosed) {
            currentState = State.LOCKED;
             lock();
         } else if (currentState == State.UNKNOWN
                 && (!magneticClosed && !lightClosed)) {
            currentState = State.WOKEN;
             wake();
         } else if (currentState == State.WOKEN
                 && (magneticClosed && lightClosed)) {
            currentState = State.LOCKED;
             lock();
        } else if (currentState == State.LOCKED
                && (!magneticClosed && !lightClosed)) {
            currentState = State.WOKEN;
            wake();
        } else if (currentState == State.LOCKED
                && (magneticClosed && !lightClosed)) {
            currentState = State.WOKEN;
             wake();
         }
     }
 
     private void lock() {
         if (isActiveAdmin()) {
             deviceManager.lockNow();
         }
     }
 
     @SuppressWarnings("deprecation")
     private void wake() {
         PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         WakeLock wake = manager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                 | PowerManager.FULL_WAKE_LOCK, "LightSwitcher");
         wake.acquire();
         wake.release();
     }
 
     private boolean isActiveAdmin() {
         ComponentName adminReceiver = new LightSwitcherDeviceAdminReceiver()
                 .getWho(this.getApplicationContext());
         return deviceManager.isAdminActive(adminReceiver);
     }
 }
