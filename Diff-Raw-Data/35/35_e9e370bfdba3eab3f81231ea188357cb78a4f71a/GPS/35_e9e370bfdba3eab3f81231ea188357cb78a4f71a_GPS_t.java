 package com.formicite;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import com.google.android.maps.GeoPoint;
 import android.location.GpsStatus;
 import android.location.GpsSatellite;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import java.util.ArrayList;
 import android.util.Log;
 
 public class GPS implements LocationListener, GpsStatus.Listener, SensorEventListener  {
 	public GPS(LocationManager lm, SensorManager s) {
 		super();
 		this.lm = lm;
 		this.sensorManager = s;
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		lm.addGpsStatusListener(this); 
 		listeners = new ArrayList<GPSListener>();
 		startStartPointCapture();
 		
		
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
 	}
 	
 	public void addListener(GPSListener l) {
 		listeners.add(l);
 	}
 	
 	public void halt() {
 		lm.removeGpsStatusListener(this);
 		lm.removeUpdates(this);
 		sensorManager.unregisterListener(this);
 		listeners = new ArrayList<GPSListener>();
 	}
 
 	private SensorManager sensorManager;
 	private LocationManager lm;
 	private GeoPoint myGeoPoint;
 	private GeoPoint myStartPoint;
 	private GeoPoint courseStartPoint;
 	private long startLat, startLong;
 	private int startPointCountdown;
 	private int startPointN;
 	private ArrayList<GPSListener> listeners;
 	
 	
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
  
 	private float azimut;
 	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            azimut = (event.values[0] - 90.0f) / 180.0f * 3.14159265f;
			onGpsStatusChanged(0);
        }
 	}
 	
 	public float getOrientation() {
 		return azimut;
 	}
 	public GeoPoint getLocation() {
 		return myGeoPoint;
 	}
 	public GeoPoint getStartPoint() {
 		return myStartPoint;
 	}
 	public int getStartPointCountdown() {
 		return startPointN;
 	}
 	public int getStartPointMax() {
 		return startPointCountdown + startPointN;
 	}
 	
 	public void setCourseStartPoint(GeoPoint pt) {
 		courseStartPoint = pt;
 	}
 	
 	public void startStartPointCapture() {
 		myStartPoint = null;
 		startLat = 0;
 		startLong = 0;
 		startPointCountdown = 3;
 		startPointN = 0;
 		onGpsStatusChanged(0);
 	}
 	
 	public interface GPSListener {
 		public void onGpsStatusChanged(int st, int max);
 	}
 	
 	public void onGpsStatusChanged(int event) {
 		GpsStatus stat = lm.getGpsStatus(null);
 		int st = 0, max = 0;
 		for (GpsSatellite s : stat.getSatellites()) {
 			if (s.usedInFix()) {
 				st++;
 			}
 			max++;
 		}
 		for (GPSListener l : listeners) {
 			l.onGpsStatusChanged(st, max);
 		}
 	}
 	
 	public void onLocationChanged(Location argLocation) {
 		GeoPoint gp = new GeoPoint(
 			(int)(argLocation.getLatitude()*1000000),
 			(int)(argLocation.getLongitude()*1000000));
 			
 		if (startPointCountdown != 0) {
 			startLat += gp.getLatitudeE6();
 			startLong += gp.getLongitudeE6();
 			Log.i("MyLocationListener", "Me: " + gp.getLatitudeE6() + "," + gp.getLongitudeE6()
 					+ " My Start: " + startLat + "," + startLong + " " + startPointN);
 			startPointN++;
 			startPointCountdown--;
 			if (startPointCountdown == 0) {
 				myStartPoint = new GeoPoint((int)(startLat / startPointN),
 					                    (int)(startLong / startPointN));
 			}
 			onGpsStatusChanged(0);
 		}
 		
 			
 		if (myStartPoint != null && courseStartPoint != null && startPointCountdown == 0) { //Do subtraction
 			Log.i("GPS", "Me: " + gp.getLatitudeE6() + "," + gp.getLongitudeE6()
 					+ " My Start: " + myStartPoint.getLatitudeE6() + "," + myStartPoint.getLongitudeE6()
 					+ " Course: " + courseStartPoint.getLatitudeE6() + "," + courseStartPoint.getLongitudeE6());
 			myGeoPoint = new GeoPoint(gp.getLatitudeE6() - myStartPoint.getLatitudeE6() 
 										+ courseStartPoint.getLatitudeE6(),
 									  gp.getLongitudeE6() - myStartPoint.getLongitudeE6() 
 										+ courseStartPoint.getLongitudeE6());
 		} else {
 			myGeoPoint = gp;
 		}
 		Log.i("GPS", "Lat: " + argLocation.getLatitude() + " Long: " + argLocation.getLongitude());
 		onGpsStatusChanged(0);
 	}
 	public void onProviderDisabled(String provider) {}
 	public void onProviderEnabled(String provider) {}
 	public void onStatusChanged(String provider, int status, Bundle extras) {}
 }
 	
 	
