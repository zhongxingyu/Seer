 package egl.positioningsystem;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class SensorGPS implements SensorEventListener{
 
 	private double longitudeNetwork;
 	private double latitudeNetwork;
 	private double longitudeGPS;
 	private double latitudeGPS;
 	private double dError = 0.0001;
 	
 	private LocationListener gpsListener=null;
 	private LocationListener networkListener=null;
 //	private float velocidade;
 	
 	// flag for GPS status
 	boolean isGPSEnabled = false;
 	// flag for network status
 	boolean isNetworkEnabled = false;
 	// flag for Location status
	boolean canGetLocation = false; 
 	
	public SensorGPS(LocationManager lm){
     	networkListener = new myLL_Network();
    	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, networkListener);
     	gpsListener = new myLL_GPS();
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpsListener);
 	}
 	
 	// TODO Criar um filtro para os dados
 	
 	public double getLatitude(){
 		if(latitudeGPS < dError)
 			return latitudeNetwork;
 		else return latitudeGPS;
 	}
 	
 	public double getLongitude(){
 		if(longitudeGPS < dError)
 			return longitudeNetwork;
 		else return longitudeGPS;
 	}
 	/*
 	public double getVelocidade(){
 		return velocidade;
 	}*/
 	
     //TODO Melhorar GPS
 	//Class to get location
 	private class myLL_GPS implements LocationListener {
     	@Override
     	public void onLocationChanged(Location position){
     		longitudeGPS = position.getLongitude();
     		latitudeGPS  = position.getLatitude();
     	}
     	
         @Override
         public void onProviderDisabled(String provider) {
        	isGPSEnabled = false;       	
         }
 
         @Override
         public void onProviderEnabled(String provider) {
        	isGPSEnabled = true;        	
         }
 
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
             // TODO Auto-generated method stub        	
         }
     }
 	
 	private class myLL_Network implements LocationListener {
     	@Override
     	public void onLocationChanged(Location position){
     		longitudeNetwork = position.getLongitude();
     		latitudeNetwork  = position.getLatitude();
     	}
     	
         @Override
         public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub  
        	//SensorGPS.myLL_GPS.this
         }
 
         @Override
         public void onProviderEnabled(String provider) {
             // TODO Auto-generated method stub        	
         }
 
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
             // TODO Auto-generated method stub        	
         }
     }
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 	}
 }
