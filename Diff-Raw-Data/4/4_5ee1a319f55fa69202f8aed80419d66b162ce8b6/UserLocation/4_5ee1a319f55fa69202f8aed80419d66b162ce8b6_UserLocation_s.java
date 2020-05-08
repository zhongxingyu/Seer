 package edu.umn.pinkpanthers.beerfinder.data;
 
 // Location guidance gained from...
 // http://www.firstdroid.com/2010/04/29/android-development-using-gps-to-get-current-location-2/
 
 
 import com.google.android.maps.GeoPoint;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * Model class for a Beer.
  * 
  */
 public class UserLocation implements LocationListener{
 
 	private String zipcode;	
 	private static UserLocation instance;
 	private Context context;
 	private boolean customLocation;
 	private LocationManager mlocManager;
 	private GeoPoint deviceLocation;
 	
 	/** 
 	 * Private 'Singleton' constructor
 	 */
 	private UserLocation(Context appContext) {
 		zipcode = "";
 		context = appContext;
 		customLocation = false;	// assume device will provide the location.
 		deviceLocation = null;
 		
 		// TODO - ask device for GPS location. if found, set zip to "55455" (U of M)
 		//      - else, force user to enter a zip code.
 		
 		/* Use the LocationManager class to obtain GPS locations */
 		mlocManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
 		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
 	}
 
 	/**
 	 * Create the 'Singleton' object.
 	 */
 	public static void initialize(Context appContext) {
 		if (instance == null) {
 			instance = new UserLocation(appContext);
 		}
 	}
 
 	/**
 	 * 'Singleton' accessor method.
 	 */
 	public static UserLocation getInstance() {
 		if (instance == null) {
 			throw new RuntimeException("Cannot access UserLocation! Call initialize() first!");
 		}
 		return instance;
 	}
 	
 	public boolean isLocationValid(){
 		if(customLocation){
 			if(zipcode != null && zipcode.length() == 5)
 				return true;
 			else
 				return false;
 		} else {
 			return (deviceLocation != null); // have we received at least one location update.
 		}
 		
 	}
 	
 	// only update location if it is a five digit number.
 	// otherwise set location invalid
 	public void customLocation(String zip){
 		customLocation = true;
 		Log.d("UserLocation", "Updated: " + zip);
 		if(zip == null || zip.length() != 5){
 			zipcode = "";
 		}
 		else{
 			try{
 				Integer.parseInt(zip);
 				zipcode = zip;
 			}
 			catch(NumberFormatException e){
 				zipcode = "";
 			}
 		}
 	}
 	
 	public void useDevice(){
 		customLocation = true;	
 	}
 	
 	public String getZip(){
 		return zipcode;
 	}
 	
 	public GeoPoint getGPS(){
 		// ideally, the GPS would tell us directly the current location
 		// or, we would use the ZIP code to determine the GPS location
 		// instead, hard-code a location so the search results are always displayed
 		if(customLocation || deviceLocation == null){
 			return new GeoPoint((int)(1000000 * 44.976092),(int)(1000000 *  -93.232212)); // hard code a location.
 		}
 		else {
 			return deviceLocation;
 		}
 	}
 
 //-------------- LocationListener Interface Methods ------------------//
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		deviceLocation = new GeoPoint(
			(int)location.getLatitude() * 1000000,
			(int)location.getLongitude() * 1000000
 		);
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		//Toast.makeText(context, "Location lost", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		//Toast.makeText(context, "Location gained.", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// do nothing
 	}
 	
 	
 	
 }
