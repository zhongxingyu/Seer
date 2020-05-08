 package com.example.lufteapp;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.R.bool;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GpsData extends Activity {
 
 	SQLiteDatabase db;
 	double latitude;
 	double longitude;
 	TextView editLocation;
 	LocationManager locationManager;
 	LocationListener locationListener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gps_data);
 		
 		 editLocation = (TextView) findViewById(R.id.kords);
		 
 		 /*
 		  * This code gets a new GPS position every 20. second with a 5 metre interval.
 		  */    
 		 
 		 /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		 locationListener = new MyLocationListener();  
 		 locationManager.requestLocationUpdates(  
 		 LocationManager.GPS_PROVIDER, 20000, 5, locationListener);*/
 		
 		db = openOrCreateDatabase("gpsDataDB", MODE_PRIVATE,null);
 		db.execSQL("CREATE TABLE IF NOT EXISTS gpsDataa(longitude BIGINT, latitude BIGINT, isHome INTEGER, name STRING);");
 	    
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.gps_data, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    
 		 locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 	    {
 			 locationListener = new MyLocationListener();  
 			 locationManager.requestLocationUpdates(  
 			 LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		 }
 	    else	{
 	    	enableGPS();
 	    }
 		 
 	}
 	
 	@Override 
 	protected void onPause() {
 		if(locationListener != null){	//It cant be removed, if it is not initialised
 			locationManager.removeUpdates(locationListener);
 		}
 		super.onPause();
 	}
 	
 	public void enableGPS()
 	{
 		// Get Location Manager and check for GPS & Network location services
 		 if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
 		       !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 		   // Build the alert dialog
 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        switch (which){
 				        case DialogInterface.BUTTON_POSITIVE:
 				   	     Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					     startActivity(intent);
 				            break;
 	
 				        case DialogInterface.BUTTON_NEGATIVE:
 				            //No button clicked
 				        	//DO nothing
 				            break;
 				        }
 				    }
 				};
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage("Enable GPS?").setPositiveButton("Yes", dialogClickListener)
 				    .setNegativeButton("No", dialogClickListener).show();
 		 }
 	}
 	
 	public void compareHomeCurrent(View view)
 	{
 		/*
 		 * Midlertidig kode for  f ut siste fra DB
 		 * Gjelder da kun hjemmeposisjon.
 		 */
 		Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa WHERE isHome = 1;", null);
 		if(cursor.moveToFirst())
 		{
 			String lat = cursor.getString(0);
 			String lon = cursor.getString(1);
 			String isHom = cursor.getString(2);
 			int number = cursor.getCount();
 		       editLocation.setText(
 		    		   "fra DB number = " + number
 		    		  + "lat= " + lat + "lon = " + lon + "isHom= " +isHom);
 		}
 	}
 	
 	public void setHome(View view) 
 	{
 		/*
 		 * Makes a new home for the user, but also checks if the
 		 * user already has a home.
 		 * 
 		 */
 		if(latitude != 0 && longitude != 0)
 		{
 			Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa WHERE isHome = 1;", null);
 			if(cursor.getCount() > 0)
 			{
 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        switch (which){
 				        case DialogInterface.BUTTON_POSITIVE:
 							db.execSQL("DELETE FROM gpsDataa WHERE isHome = 1;");
 							storeMapdataInDatabase(1);
 				            break;
 	
 				        case DialogInterface.BUTTON_NEGATIVE:
 				            //No button clicked
 				        	//DO nothing
 				            break;
 				        }
 				    }
 				};
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage("Are you sure you want to set a new home?").setPositiveButton("Yes", dialogClickListener)
 				    .setNegativeButton("No", dialogClickListener).show();
 			}
 			else
 			{
 				storeMapdataInDatabase(1);
 			}
 		}
 		else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 		{
 			enableGPS();
 		}
 		else
 		{
 	    	 Toast.makeText(getApplicationContext(), 
 		     "Please wait for GPS to find position" +
 		     "\nTry again in a moment",
 		     Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public void setCurrent(View view)
 	{
 		storeMapdataInDatabase(0);
 	}
 	
 		
 		void storeMapdataInDatabase(int isHome)
 		{
 			if((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
 			{
 				if(latitude != 0 && longitude != 0)		//Has not acquired GPS data yet.
 				{
 					try {	
 						db.execSQL("INSERT INTO gpsDataa VALUES('"+longitude + "','" + latitude + "','" + isHome + "','homeyo');");
 					       Toast.makeText(getApplicationContext(), 
 					    		   "Inserting to DB lat= " + latitude + " lon= " + longitude, Toast.LENGTH_LONG).show();
 					}
 					catch(NullPointerException e) {
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 			    	 Toast.makeText(getApplicationContext(), 
 				     "Please wait for GPS to find position" +
 				     "\nTry again in a moment",
 				     Toast.LENGTH_LONG).show();
 				}
 			}
 			
 			else	{		//If GPS has been disabled while using the app.
 				enableGPS();
 			}
 		}
 		
 		public bool checkHome(int lat, int lon)
 		{
 			//Check if sent lat and lon is your home address
 			return null;
 		}
 		
 		/*----------Listener class to get coordinates ------------- */
 		/*
 		 *  Code from http://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
 		 *  Helperclass to get GPS
 		 */
 		private class MyLocationListener implements LocationListener {
 
 		    @Override
 		    public void onLocationChanged(Location loc) {
 		        longitude = loc.getLongitude();
 		        latitude = loc.getLatitude();
 		    }
 
 		    public void onProviderDisabled(String provider) {}
 
 		    public void onProviderEnabled(String provider) {}
 
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 		}
 }
 
