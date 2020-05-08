 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of GenericAndroidClient.
  *
  *  GenericAndroidClient is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  GenericAndroidClient is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package uk.ac.horizon.ug.exploding.client;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.GpsSatellite;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.location.GpsStatus.Listener;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 /**
  * @author cmg
  *
  */
 public class GpsStatusActivity extends Activity implements Listener, LocationListener {
 
 	private static final String GPS_PROVIDER = "gps";
 	private static final String TAG = "GpsStatus";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.gpsstatus);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		locationManager.removeGpsStatusListener(this);
 		locationManager.removeUpdates(this);
 		Log.d(TAG, "Pause: disabled Location callbacks");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		locationManager.addGpsStatusListener(this);
 		locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);
 		Location loc = locationManager.getLastKnownLocation(GPS_PROVIDER);
 		updateLocation(loc);
 		updateEnabled(locationManager.isProviderEnabled(GPS_PROVIDER));
 		Log.d(TAG, "Resume	: enabled Location callbacks");
 	}
 
 	private void updateEnabled(boolean providerEnabled) {
 		TextView enabledTextView = (TextView)findViewById(R.id.gps_enabled_text_view);
 		enabledTextView.setText(providerEnabled ? "Yes" : "No");
 	}
 
 	private void updateLocation(Location loc) {
 		TextView tv;
 		tv = (TextView)findViewById(R.id.gps_loc_time_text_view);
 		tv.setText(loc==null ? "-" : ""+new Date(loc.getTime()));
 		tv = (TextView)findViewById(R.id.gps_time_text_view);
 		tv.setText(loc==null ? "-" : ""+new Date());
 		tv = (TextView)findViewById(R.id.gps_latitude_text_view);
 		tv.setText(loc==null ? "-" : ""+loc.getLatitude());
 		tv = (TextView)findViewById(R.id.gps_longitude_text_view);
 		tv.setText(loc==null ? "-" : ""+loc.getLongitude());
 		tv = (TextView)findViewById(R.id.gps_altitude_text_view);
 		tv.setText(loc==null ? "-" : ""+loc.getAltitude());
 		tv = (TextView)findViewById(R.id.gps_accuracy_text_view);
 		tv.setText(loc==null ? "-" : loc.hasAccuracy() ? ""+loc.getAccuracy() : "Not Available");
 		tv = (TextView)findViewById(R.id.gps_accuracy_text_view);
 		tv.setText(loc==null ? "-" : loc.hasAccuracy() ? ""+loc.getAccuracy() : "Not Available");
 	}
 
 	@Override
 	public void onGpsStatusChanged(int event) {
 		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		GpsStatus status = locationManager.getGpsStatus(null);
 		TextView tv;
 		tv = (TextView)findViewById(R.id.gps_time_to_fix_text_view);
		tv.setText(""+(status.getTimeToFirstFix()/1000)+"s");
 		int satCount = 0, satFixCount = 0;
 		double snrs[] = new double[status.getMaxSatellites()];
 		int i =0;
 		Iterator<GpsSatellite> sats = status.getSatellites().iterator();
 		while(sats.hasNext()) {
 			GpsSatellite sat = sats.next();
 			if (sat.usedInFix())
 				satFixCount++;
 			satCount++;
 			snrs[i] = sat.getSnr();
 			i++;			
 		}
 		// ascending
 		Arrays.sort(snrs);
 		tv = (TextView)findViewById(R.id.gps_sat_number_fix_text_view);
 		tv.setText(""+satFixCount);
 		tv = (TextView)findViewById(R.id.gps_sat_number_total_text_view);
 		tv.setText(""+satCount);
 		tv = (TextView)findViewById(R.id.gps_sat_snr_text_view);
 		if (snrs.length>=4) 
 			tv.setText(""+snrs[snrs.length-4]);
 		else if (snrs.length>0)
 			tv.setText(""+snrs[snrs.length-1]);
 		else
 			tv.setText("-");
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		updateLocation(location);		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		if (GPS_PROVIDER.equals(provider))
 			updateEnabled(false);
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		if (GPS_PROVIDER.equals(provider))
 			updateEnabled(true);		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		if (GPS_PROVIDER.equals(provider))
 			updateStatus(status);
 	}
 
 	private void updateStatus(int status) {
 		TextView enabledTextView = (TextView)findViewById(R.id.gps_status_text_view);
 		String statusText = (status==LocationProvider.AVAILABLE ? "AVAILABLE" : status==LocationProvider.OUT_OF_SERVICE ? "OUT_OF_SERVICE" : status==LocationProvider.TEMPORARILY_UNAVAILABLE ? "TEMPORARILY_UNAVAILABLE" : "unknown ("+status+")");
 		enabledTextView.setText(statusText);
 	}
 
 }
