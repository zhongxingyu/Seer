 package com.RoboMobo;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nats
  * Date: 31.07.13
  * Time: 10:55
  */
 public class GPSModule implements LocationListener
 {
     public double last_latt;
     public double last_long;
     public boolean active = false;
 
     @Override
     public void onLocationChanged(Location location)
     {
         if(active == false) active = true;
         last_latt = location.getLatitude();      // широта
         last_long = location.getLongitude();     // долгота
         TextView text = (TextView) RMR.am.findViewById(R.id.tv_coord);
         TextView text1 = (TextView) RMR.am.findViewById(R.id.tv_accuracy);
         TextView text2 = (TextView) RMR.am.findViewById(R.id.tv_speed);
         text.setText("Координаты: " + last_latt + ", " + last_long);
         text1.setText("Точность: " + location.getAccuracy());
         text2.setText("Скорость: " + location.getSpeed());
 
         Log.wtf("1", Double.toString(last_latt) + ' ' + Double.toString(last_long));
         //Log.wtf("Accuracy"," "+location.getAccuracy());
         //Log.wtf("Speed"," "+location.getSpeed());
        TextView text1 = (TextView) RMR.am.findViewById(R.id.tv_accuracy);
         TextView text2 = (TextView) RMR.am.findViewById(R.id.tv_speed) ;
         text1.setText("Точность: "+location.getAccuracy());
        text2.setText("Скорость: "+location.getSpeed());
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle)
     {
 
     }
 
     @Override
     public void onProviderEnabled(String s)
     {
         Toast.makeText(RMR.am.getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onProviderDisabled(String s)
     {
         Toast.makeText(RMR.am.getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
     }
 }
