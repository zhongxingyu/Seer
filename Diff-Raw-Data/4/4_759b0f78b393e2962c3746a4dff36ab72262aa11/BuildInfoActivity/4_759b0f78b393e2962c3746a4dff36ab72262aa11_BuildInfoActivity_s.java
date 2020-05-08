 package com.erdfelt.android.buildinfo;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.widget.ListView;
 
 public class BuildInfoActivity extends Activity {
     private InfoModel model;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         model = new InfoModel();
         
         addBuildInfo();
         addSystemInfo();
         addDisplayInfo();
 
         addSensorInfo("Accelerometer", Sensor.TYPE_ACCELEROMETER);
         addSensorInfo("Gyroscope", Sensor.TYPE_GYROSCOPE);
         addSensorInfo("Light", Sensor.TYPE_LIGHT);
         addSensorInfo("Magnetic Field", Sensor.TYPE_MAGNETIC_FIELD);
         addSensorInfo("Orientation", Sensor.TYPE_ORIENTATION);
         addSensorInfo("Pressure", Sensor.TYPE_PRESSURE);
         addSensorInfo("Proximity", Sensor.TYPE_PROXIMITY);
         addSensorInfo("Temperature", Sensor.TYPE_TEMPERATURE);
 
         ListView list = (ListView) findViewById(R.id.list);
         InfoModelAdapter adapter = new InfoModelAdapter(getLayoutInflater(), model);
         list.setAdapter(adapter);
     }
 
     private void addSensorInfo(String label, int sensorType) {
         model.addHeader("Sensor: " + label);
         
         SensorManager smgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         List<Sensor> sensors = smgr.getSensorList(sensorType);
         int len = sensors.size();
        if(len < 0) {
            model.addDetail("Not Present", "No hardware specific sensor of this type");
             return;
         }
         String prefix;
         for(int i=0; i<len; i++) {
             Sensor sensor = sensors.get(i);
             prefix = "[" + (i+1) + "] ";
             model.addDetail(prefix + "name", sensor.getName());
             model.addDetail(prefix + "vendor", sensor.getVendor());
             model.add(new InfoDetail(prefix + "type", sensor.getType()));
             model.add(new InfoDetail(prefix + "version", sensor.getVersion()));
             model.add(new InfoDetail(prefix + "power", sensor.getPower()));
             model.add(new InfoDetail(prefix + "resolution", sensor.getResolution()));
             model.add(new InfoDetail(prefix + "maximum-range", sensor.getMaximumRange()));
         }
     }
     
 
     private void addDisplayInfo() {
         model.addHeader("Display Info");
         
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
         model.add(new InfoDetail("density", metrics.density));
         model.add(new InfoDetail("densityDpi", metrics.densityDpi));
         model.add(new InfoDetail("scaledDensity", metrics.scaledDensity));
         model.add(new InfoDetail("widthPixels", metrics.widthPixels));
         model.add(new InfoDetail("heightPixels", metrics.heightPixels));
         model.add(new InfoDetail("xdpi", metrics.xdpi));
         model.add(new InfoDetail("ydpi", metrics.ydpi));
     }
 
     private void addSystemInfo() {
         model.addHeader("System Info");
         
         ContentResolver contentResolver = getContentResolver();
         String id = android.provider.Settings.System.getString(contentResolver, 
                 android.provider.Settings.System.ANDROID_ID);
         if(id == null) {
             id = "<on_emulator>";
         }
         
         model.addDetail("ANDROID_ID", id);
     }
 
     private void addBuildInfo() {
         model.addHeader("Build Info");
         
         model.add(new InfoDetail("BOARD", Build.BOARD));
         model.add(new InfoDetail("BRAND", Build.BRAND));
         model.add(new InfoDetail("CPU_ABI", Build.CPU_ABI));
         model.add(new InfoDetail("DEVICE", Build.DEVICE));
         model.add(new InfoDetail("FINGERPRINT", Build.FINGERPRINT));
         model.add(new InfoDetail("HOST", Build.HOST));
         model.add(new InfoDetail("ID", Build.ID));
         model.add(new InfoDetail("MANUFACTURER", Build.MANUFACTURER));
         model.add(new InfoDetail("MODEL", Build.MODEL));
         model.add(new InfoDetail("PRODUCT", Build.PRODUCT));
         model.add(new InfoDetail("TAGS", Build.TAGS));
         model.add(new InfoDetail("TYPE", Build.TYPE));
         model.add(new InfoDetail("USER", Build.USER));
         model.add(new InfoDetail("VERSION.RELEASE", Build.VERSION.RELEASE));
         model.add(new InfoDetail("VERSION.SDK_INT", Build.VERSION.SDK_INT));
         model.add(new InfoDetail("VERSION.CODENAME", Build.VERSION.CODENAME));
     }
 }
