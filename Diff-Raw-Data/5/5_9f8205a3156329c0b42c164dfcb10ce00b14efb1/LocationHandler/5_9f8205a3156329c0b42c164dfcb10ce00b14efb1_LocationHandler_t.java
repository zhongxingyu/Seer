 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Pequea y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2010.
  *   author Alberto Romeu aromeu@prodevelop.es 
  *   author Ruben Blanco rblanco@prodevelop.es 
  *   
  */
 
 package es.prodevelop.gvsig.mini.location;
 
 import net.sf.microlog.core.Logger;
 import net.sf.microlog.core.LoggerFactory;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class LocationHandler {
 
 	private Logger log = LoggerFactory.getLogger(LocationHandler.class);
 	protected LocationListenerAdaptor mGpsLocationListener;
 	protected LocationListenerAdaptor mNetworkLocationListener;
 
 	/** true if Gps Location is activated, false otherwise */
 	protected boolean gpsLocationActivated = false;
 	/** true if Network Location is activated, false otherwise */
 	protected boolean networkLocationActivated = false;
 	protected String lastLocation = "";
 	protected Location lastFixedLocation;
 
 	protected LocationManager mLocationManager;
 	protected LocationListener mLocationReceiver;
 	protected Context mContext;
 
 	protected Location firstLocation;
 	private LocationTimer timer;
 
 	public LocationHandler(LocationManager lm, LocationListener dest,
 			Context ctx) {
 		mLocationManager = lm;
 		mLocationReceiver = dest;
 		mContext = ctx;
 	}
 
 	public Location getFirstLocation() {
 		return firstLocation;
 	}
 
 	public synchronized void start() {
 		// initialize state of location providers and launch location listeners
 		if (!networkLocationActivated
 				&& mLocationManager
 						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			log.debug("Request updates for network provider");
 			networkLocationActivated = true;
 			mNetworkLocationListener = new LocationListenerAdaptor();
 			mLocationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 0, 0,
 					this.mNetworkLocationListener);
 		}
 		if (!gpsLocationActivated
 				&& mLocationManager
 						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			log.debug("Request updates for gps provider");
 			gpsLocationActivated = true;
 			mGpsLocationListener = new LocationListenerAdaptor();
 			mLocationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 0, 0,
 					this.mGpsLocationListener);
 		}
 		// get the best location using bestProvider()
 		try {
 			firstLocation = mLocationManager
 					.getLastKnownLocation(bestProvider());
 		} catch (Exception e) {
 
 		}
 
 		if (timer != null) {
 			timer.schedule(10000);
 		}
 
 		// test to see which location services are available
 		// if (!gpsLocationActivated) {
 		// if (!networkLocationActivated) {
 		// // no location providers are available, ask the user if they
 		// // want to go and change the setting
 		// AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 		// builder.setCancelable(true);
 		// builder.setMessage(R.string.location_services_disabled)
 		// .setCancelable(false).setPositiveButton(
 		// android.R.string.yes,
 		// new DialogInterface.OnClickListener() {
 		// public void onClick(DialogInterface dialog,
 		// int id) {
 		// dialog.dismiss();
 		// mContext.startActivity(new Intent(
 		// android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 		// }
 		// }).setNegativeButton(android.R.string.no,
 		// new DialogInterface.OnClickListener() {
 		// public void onClick(DialogInterface dialog,
 		// int id) {
 		// dialog.cancel();
 		// }
 		// });
 		// AlertDialog alert = builder.create();
 		// alert.show();
 		// } else {
 		// // we have network location but no GPS, tell the user that
 		// // accuracy is bad because of this
 		// // Toast.makeText(mContext, R.string.gps_disabled, Toast.LENGTH_LONG)
 		// // .show();
 		// }
 		// } else if (!networkLocationActivated) {
 		// // we have GPS (but no network), this tells the user
 		// // that they might have to wait for a fix
 		// Toast.makeText(mContext, R.string.getting_gps_fix, Toast.LENGTH_LONG)
 		// .show();
 		// }
 	}
 
 	public synchronized void stop() {
 		// Log.v(OpenSatNavConstants.LOG_TAG, "LocationHandler Stop");
 		log.debug("LocationHandler stop");
 		try {
 			mLocationManager.removeUpdates(mGpsLocationListener);
			gpsLocationActivated = false;
 			mGpsLocationListener = null;
 		} catch (IllegalArgumentException e) {
 			// Log.v(OpenSatNavConstants.LOG_TAG, "Ignoring: " + e);
 			// there's no gps location listener to disable
 		}
 		try {
 			mLocationManager.removeUpdates(mNetworkLocationListener);
			networkLocationActivated = false;			
 			mNetworkLocationListener = null;
 		} catch (IllegalArgumentException e) {
 			// Log.v(OpenSatNavConstants.LOG_TAG, "Ignoring: " + e);
 			// there's no network location listener to disable
 		} finally {
 			if (this.timer != null)
 				this.timer.cancel();
 		}
 	}
 
 	/**
 	 * Tests if the given provider is the best among all location providers
 	 * available
 	 * 
 	 * @param myLocation
 	 * @return true if the location is the best choice, false otherwise
 	 */
 	private boolean isBestProvider(Location myLocation) {
 		if (myLocation == null)
 			return false;
 		boolean isBestProvider = false;
 		String myProvider = myLocation.getProvider();
 		boolean gpsCall = myProvider
 				.equalsIgnoreCase(LocationManager.GPS_PROVIDER);
 		boolean networkCall = myProvider
 				.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER);
 		// get all location accuracy in meter; note that less is better!
 		float gpsAccuracy = Float.MAX_VALUE;
 		long gpsTime = 0;
 		if (gpsLocationActivated) {
 			Location lastGpsLocation = mLocationManager
 					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			if (lastGpsLocation != null) {
 				gpsAccuracy = lastGpsLocation.getAccuracy();
 				gpsTime = lastGpsLocation.getTime();
 			}
 		}
 		float networkAccuracy = Float.MAX_VALUE;
 		if (networkLocationActivated) {
 			Location lastNetworkLocation = mLocationManager
 					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			if (lastNetworkLocation != null)
 				networkAccuracy = lastNetworkLocation.getAccuracy();
 		}
 		float currentAccuracy = myLocation.getAccuracy();
 		long currentTime = myLocation.getTime();
 		// Use myLocation if:
 		// 1. it's a gps location & network is disabled
 		// 2. it's a gps loc & network activated
 		// & gps accuracy is better than network
 		// 3. it's a network loc & gps is disabled
 		// 4. it's a network loc, gps enabled
 		// & (network accuracy is better than gps
 		// OR last network fix is newer than last gps fix+30seconds)
 		boolean case1 = gpsCall && !networkLocationActivated;
 		boolean case2 = gpsCall && networkLocationActivated
 				&& currentAccuracy < networkAccuracy;
 		boolean case3 = networkCall && !gpsLocationActivated;
 		boolean case4 = networkCall
 				&& gpsLocationActivated
 				&& (currentAccuracy < gpsAccuracy || currentTime > gpsTime + 30000);
 		if (case1 || case2 || case3 || case4) {
 			isBestProvider = true;
 		}
 		return isBestProvider;
 	}
 
 	/**
 	 * Defines the best location provider using isBestProvider() test
 	 * 
 	 * @return LocationProvider or null if none are available
 	 */
 	protected String bestProvider() {
 		String bestProvider = null;
 		if (networkLocationActivated
 				&& isBestProvider(mLocationManager
 						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))) {
 			bestProvider = LocationManager.NETWORK_PROVIDER;
 		} else if (gpsLocationActivated) {
 			bestProvider = LocationManager.GPS_PROVIDER;
 		}
 		return bestProvider;
 	}
 
 	private class LocationListenerAdaptor implements LocationListener {
 		public void onLocationChanged(final Location loc) {
 			if (isBestProvider(loc)) {
 				log.debug("location changed");
 				mLocationReceiver.onLocationChanged(loc);
 				lastLocation = loc.getProvider();
 			}
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// LocationHandler.this.mNumSatellites = extras.getInt(
 			// "satellites", NOT_SET); // TODO Check on an actual device
 			if (provider.equals(bestProvider())) {
 				mLocationReceiver.onStatusChanged(provider, status, extras);
 			}
 		}
 
 		public void onProviderEnabled(String a) { /* ignore */
 		}
 
 		public void onProviderDisabled(String a) { /* ignore */
 		}
 	}
 
 	public void setLocationTimer(LocationTimer timer) {
 		this.timer = timer;
 	}
 }
