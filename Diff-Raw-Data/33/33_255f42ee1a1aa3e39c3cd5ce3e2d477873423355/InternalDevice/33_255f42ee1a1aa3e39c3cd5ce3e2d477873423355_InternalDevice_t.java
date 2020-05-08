 /**
  * 
  */
 
 package org.itea.neotiq.devicemodel;
 
 import org.itea.neotiq.application.CustomApplication;
 import org.itea.neotiq.devicemodel.DeviceManager.EventType;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * @author hbriand
  */
 public class InternalDevice extends Device implements SensorEventListener, RequestableDevice {
 
     private static final EventType EVT_VALUES_CHANGED = null;
 
     private static Collection<String> subclasses;
 
     private static Collection<InternalDevice> instantiatedInternalDevices = new ArrayList<InternalDevice>();
 
     private static SensorManager mSensorManager;
 
     private Sensor mSensor;
 
     private float[] lastValues;
 
     private long lastChange = 0;
 
     public static Collection<String> getInternalDeviceTypes() {
         if (InternalDevice.subclasses == null) {
             InternalDevice.subclasses = new ArrayList<String>();
            if (!"google_sdk".equals(android.os.Build.MODEL)) { // Then we are not in the simulator
                mSensorManager = (SensorManager)CustomApplication.getAppContext().getSystemService(
                        Context.SENSOR_SERVICE);
                for (Sensor s : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
                    InternalDevice.subclasses.add(s.getName());
                }
             }
         }
         return InternalDevice.subclasses;
     }
 
     public static Collection<InternalDevice> getInstantiatedInternalDevices() {
         return InternalDevice.instantiatedInternalDevices;
     }
 
     /**
      * 
      */
     protected InternalDevice(String name, String sensor) {
         super();
         this.name = name;
         for (Sensor s : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
             if (s.getName().equals(sensor)) {
                 mSensor = s;
                 mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
             }
         }
         this.uiElement = new TextView(CustomApplication.getAppContext());
         instantiatedInternalDevices.add(this);
         this.setDelegate(CustomApplication.getDefaultDelegate());
     }
 
     public String getCurrentValue() {
         return Arrays.toString(lastValues);
     }
 
     public void onSensorChanged(SensorEvent arg0) {
         if (System.currentTimeMillis() - this.lastChange > 1000
                 && !this.getCurrentValue().equals(Arrays.toString(arg0.values))) {
             String oldValue = this.getCurrentValue(); // Which is actually the last recorded values
             this.lastValues = arg0.values;
             DeviceEvent de = new DeviceEvent(this, EVT_VALUES_CHANGED);
             de.setParameter("old_values", oldValue);
             de.setParameter("new_values", this.getCurrentValue()); // Which is the new values
             this.sendEvent(de);
             ((TextView)this.uiElement).setText(this.getCurrentValue());
             this.lastChange = System.currentTimeMillis();
         }
     }
 
     public static void addDevice(String text, String name) {
         new InternalDevice(name, text);
     }
 
     public void onAccuracyChanged(Sensor arg0, int arg1) {
     }
 
     @Override
     public String getSemanticAnnotation() {
         return mSensor.getName();
     }
 
 }
