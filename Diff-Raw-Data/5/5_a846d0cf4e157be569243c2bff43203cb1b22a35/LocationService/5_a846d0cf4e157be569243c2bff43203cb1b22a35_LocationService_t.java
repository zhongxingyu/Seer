 package uk.ac.sussex.asegr3.tracker.client.service;
 
 import java.util.LinkedList;
 import java.util.List;
 
 //import org.mockito.cglib.proxy.CallbackGenerator.Context;
 
 import uk.ac.sussex.asegr3.tracker.client.dto.LocationDto;
 import uk.ac.sussex.asegr3.tracker.client.util.Logger;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class LocationService implements LocationListener {
 
 	private final List<LocationUpdateListener> listeners;
 	private final LocationManager locationManager;
 	private final int proximityDistance;
 	private final Logger logger;
 	private Location lastLocation = null;
 
 	public LocationService(LocationManager locationManager, int proximityDistance, Logger logger) {
 		this.listeners = new LinkedList<LocationUpdateListener>();
 		this.locationManager = locationManager;
 		this.proximityDistance = proximityDistance;
 		this.logger = logger;
 	}
 
 	
 	public void start() {
 		// register this with location manager providers
 		logger.debug(this.getClass(), "Registering with gps and network providers");
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, proximityDistance, this);
 		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, proximityDistance, this);
 		
 		
 	}
 	
 	public void stop(){
 		
 	}
 
 	public void registerListener(LocationUpdateListener listener) {
 		this.listeners.add(listener);
 	}
 
 	private void doLocationFiltering(Location location) {
 		
 		// logic here to filter
 		// need to work out how to best optimize location approximation
 
 		boolean locationValid = false;
 		location = findGoodAccuracy(location);
 		Location lastKnownLocation = getLastLocation();
 
 		//check if doesn't receive empty coordinates
 		// possible implementation would be to discard negative coordinates??
 
 		if (location != null) {
 
			logger.debug(LocationService.class, "Recieved location update: "+location.toString());
 			// this if will check if last and current location are close to each other by 100 meters
 			//or use the eclideoum equation
 			if (lastKnownLocation != null){
 				if (lastKnownLocation.distanceTo(location) > proximityDistance) {
 
 					locationValid = true;
 				} else {
 					logger.debug(LocationService.class, "this location has not moved enough from last know location ("+lastKnownLocation+"). Ignoring location");
 				}
 			} else{
 				locationValid = true; // we dont have a last location so this must be valid.
 			}
 			
 			this.lastLocation = location;
 		}
 
 		if (locationValid) {
 			
 			notifyListeners(new LocationDto(location.getLatitude(),
 					location.getLongitude(), location.getTime()));
 		}
 	}
 
 	private Location getLastLocation() {
 		return lastLocation;
 	}
 
 
 	// this method will notify when a valid location have been added
 	private void notifyListeners(LocationDto location) {
 		logger.debug(LocationService.class, "location: "+location+" is valid.notifying listeners: "+listeners);
 		for (LocationUpdateListener listener : listeners) {
 			listener.notifyNewLocation(location);
 		}
 	}
 
 	
 	
 	//test the accuracy
 	private Location findGoodAccuracy(Location location) {
 		return location;
 	}
 	
 	
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		doLocationFiltering(location);
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	public boolean hasRequiredPermissions() {
 		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
 				locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 	}
 }
