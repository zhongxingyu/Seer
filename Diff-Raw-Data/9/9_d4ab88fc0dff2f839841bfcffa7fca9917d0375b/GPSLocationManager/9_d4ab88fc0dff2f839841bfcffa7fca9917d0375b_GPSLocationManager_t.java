 package uk.org.smithfamily.mslogger;
 
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.content.Context;
 import android.content.Intent;
 import android.location.*;
 import android.os.Bundle;
 
 public enum GPSLocationManager implements LocationListener
 {
 	INSTANCE;
 
 	private Location		lastLocation	= new Location("null");
 	private LocationManager	locationManager	= null;
 
 	synchronized public Location getLastKnownLocation()
 	{
 		return lastLocation;
 	}
 
 	synchronized public void start()
 	{
 		locationManager = (LocationManager) ApplicationSettings.INSTANCE.getContext().getSystemService(Context.LOCATION_SERVICE);
 		Criteria c = new Criteria();
 		String providerName = locationManager.getBestProvider(c, true);
 
 		DebugLogManager.INSTANCE.log("Using location provider " + providerName);
 
		if(providerName != null)
		{
		    locationManager.requestLocationUpdates(providerName, 1000, 0, this);
		}
		else
		{
		    sendMessage("No location provider available");
		}
 	}
 
 	synchronized public void stop()
 	{
 
 		if (locationManager != null)
 		{
 			locationManager.removeUpdates(this);
 		}
 		locationManager = null;
 
 	}
 
 	protected void sendMessage(String msg)
 	{
 		Intent broadcast = new Intent();
 		broadcast.setAction(ApplicationSettings.GENERAL_MESSAGE);
 		broadcast.putExtra(ApplicationSettings.MESSAGE, msg);
 		ApplicationSettings.INSTANCE.getContext().sendBroadcast(broadcast);
 	}
 
 	@Override
 	public void onLocationChanged(Location location)
 	{
 		lastLocation = location;
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider)
 	{
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider)
 	{
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras)
 	{
 		if (status == LocationProvider.OUT_OF_SERVICE)
 		{
 			sendMessage(provider + " is out of service");
 		}
 
 		if (status == LocationProvider.AVAILABLE)
 		{
 			sendMessage(provider + " is available");
 		}
 
 		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
 		{
 			sendMessage(provider + " is temporarily unavailable");
 		}
 
 	}
 }
