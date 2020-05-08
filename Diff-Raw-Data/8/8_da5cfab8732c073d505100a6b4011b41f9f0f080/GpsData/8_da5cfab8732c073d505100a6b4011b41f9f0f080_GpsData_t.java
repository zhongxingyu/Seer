 package com.example.lufteapp;
 
 import com.google.android.gms.location.LocationListener;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.R.bool;
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class GpsData extends Activity {
 
 	SQLiteDatabase db;
 	double latitude;
 	double longitude;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gps_data);
 		
 		db = openOrCreateDatabase("gpsDataDB", MODE_PRIVATE,null);
 		db.execSQL("CREATE TABLE IF NOT EXISTS gpsDataa(longitude BIGINT, latitude BIGINT, isHome INTEGER, name STRING);");
 	    
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.gps_data, menu);
 		return true;
 	}
 	
 	public void compareHomeCurrent(View view)
 	{
 		/*
 		 * Midlertidig kode for  f ut siste fra DB
 		 * WHERE isHome = 1*/
 		Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa WHERE isHome = 1;", null);
 		if(cursor.moveToFirst())
 		{
 			String lat = cursor.getString(0);
 			String lon = cursor.getString(1);
 			String isHom = cursor.getString(2);
 			int number = cursor.getCount();
 		       Toast.makeText(getApplicationContext(), 
 		    		   "fra DB number = " + number
 		    		  + "lat= " + lat + "lon = " + lon + "isHom= " +isHom , Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public void setHome(View view) 
 	{
 		setGpsData();
 		storeMapdataInDatabase(1);
 	}
 	
 	public void setCurrent(View view)
 	{
 		setGpsData();
 		storeMapdataInDatabase(0);
 	}
 	
 	public void setGpsData() 
 	{
 		
 	     LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 	     Criteria criteria = new Criteria();
 	     String bestProvider = locationManager.getBestProvider(criteria, false);
 	     Location location = locationManager.getLastKnownLocation(bestProvider);
 
 	     try {
 	       latitude = location.getLatitude();
 	       longitude = location.getLongitude ();
 	       }
 	     catch (NullPointerException e){
 	         e.printStackTrace();
 	     }
 	}
 	
 		
 		void storeMapdataInDatabase(int isHome)
 		{
 			if(latitude != 0 && longitude != 0)		//Has not acquired GPS data yet.
 			{
 				try {	
 					
 					//If home already exists, delete the current one first.
 					if(isHome == 1)
 					{
 						db.execSQL("DELETE FROM gpsDataa WHERE isHome = 1;");
 					}
 					
 					//Try to store GPS data to database*/
 					db.execSQL("INSERT INTO gpsDataa VALUES('"+longitude + "','" + latitude + "','" + isHome + "','homeyo');");
 				       Toast.makeText(getApplicationContext(), 
 				    		   "Inserting to DB lat= " + latitude + " lon= " + longitude, Toast.LENGTH_LONG).show();
 				}
 				
 				catch(NullPointerException e) {
 					e.printStackTrace();
 				}
 			}
 			else 	//If GPS data not yet acquired.
 			{
 				setGpsData();
 			}
 		}
 		
 		public bool checkHome(int lat, int lon)
 		{
 			//Check if sent lat and lon is your home address
 			return null;
 		}
 		
 		/*----------Listener class to get coordinates ------------- */
		/*private class MyLocationListener implements LocationListener {
 
 		    @Override
 		    public void onLocationChanged(Location loc) {
 		        editLocation.setText("");
 		        pb.setVisibility(View.INVISIBLE);
 		        Toast.makeText(
 		                getBaseContext(),
 		                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
 		                    + loc.getLongitude(), Toast.LENGTH_SHORT).show();
 		        String longitude = "Longitude: " + loc.getLongitude();
 		        Log.v(TAG, longitude);
 		        String latitude = "Latitude: " + loc.getLatitude();
 		        Log.v(TAG, latitude);
		        /*-------to get City-Name from coordinates -------- 
 		        String cityName = null;
 		        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
 		        List<Address> addresses;
 		        try {
 		            addresses = gcd.getFromLocation(loc.getLatitude(),
 		                    loc.getLongitude(), 1);
 		            if (addresses.size() > 0)
 		                System.out.println(addresses.get(0).getLocality());
 		            cityName = addresses.get(0).getLocality();
 		        } catch (IOException e) {
 		            e.printStackTrace();
 		        }
 		        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
 		            + cityName;
 		        editLocation.setText(s);
 		    }
 
 		    @Override
 		    public void onProviderDisabled(String provider) {}
 
 		    @Override
 		    public void onProviderEnabled(String provider) {}
 
 		    @Override
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
		}*/
 }
 
