 package com.sp.norsesquare.froyo;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class NorseSquare extends MapActivity {
     LocationProvider wifiProvider;
     LocationProvider gpsProvider;
     String currentLatLong;
     LocationManager locationManager;
     MapController mapController;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_norse_square);
         
         //Get location manager reference to enable all further locating
         
         LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         
         //Get WiFi provider
         wifiProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
         //Get GPS provider
         gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
         //Can also use Criteria class to pick a provider based on certain criteria
         
         MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         
       
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_norse_square, menu);
         return true;
     }
     
     public void onStart()
     {
     	super.onStart();
     	
     	// Reobtain location manager at restart of activity
     	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     	final boolean wifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
     	final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
     	
     	
     	if (!wifiEnabled)
     	{
     		//put alert box here, for now exit
     		//System.exit(0);
     	}
     	
     	if (!gpsEnabled)
     	{
     		//put alert box here, for now exit
     		//System.exit(0);
     	}
     	
    	/* THIS IS THE FREAKING PROBLEM PART
    	locateMeCoarse((MapView)findViewById(R.id.mapview));  //Initialize app to current wifi location  */
     }
     
     
     //Below are all top level methods called by this app
     public void locateMeCoarse(MapView view)
     {
     	mapController = view.getController();
     	
     	Location coarseLocation;
     	//Get GPS coordinates, pass to Google Maps
     	//locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 25, locationListener);
     	coarseLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     	
    	//currentLatLong = "geo:" +  coarseLocation.getLatitude() + "," + coarseLocation.getLongitude() + "?z=15";
    	GeoPoint geo = new GeoPoint(((Double)coarseLocation.getLatitude()).intValue(),((Double)coarseLocation.getLongitude()).intValue());
     	
 		if (currentLatLong.equals(null))
 		{
 			System.out.println("Longitude and latitude are null ");
 		}
    	/*
     	Uri coarseURI = Uri.parse(currentLatLong);    //Parse latitude and longitude
     	
     	Intent wifiIntent = new Intent(Intent.ACTION_VIEW,coarseURI);
    	startActivity(wifiIntent);  */
     	mapController.animateTo(geo);
     }
     
     public void locateMeFine(View view)
     {
     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
     	Uri fineLocation = Uri.parse(currentLatLong);  
     	Intent gpsIntent = new Intent(Intent.ACTION_VIEW,fineLocation);
     }
     
     private final LocationListener locationListener = new LocationListener()
     {
     	public void onLocationChanged(Location location)
     	{
     		//Where does this go? How can it be used?
     		currentLatLong = "geo:" +  location.getLatitude() + "," + location.getLongitude() + "?z=15";
     		if (currentLatLong.equals(null))
     		{
     			System.out.println("Longitude and latitude are null");
     		}
     		System.out.println("Location object is:");
     		System.out.println(location);
     	}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 			
 		}
     };
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
     
 
 }
