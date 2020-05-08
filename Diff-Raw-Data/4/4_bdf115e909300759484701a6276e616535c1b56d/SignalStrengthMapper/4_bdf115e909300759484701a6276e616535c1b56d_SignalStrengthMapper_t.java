 package com.crimsonsky.signalmapper;
 
 import java.text.DecimalFormat;
 
 import com.crimsonsky.signalstrengthmapper.R;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Intent;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class SignalStrengthMapper extends Activity implements LocationListener {
 
 	private TelephonyManager tm;
 	private LocationManager lm;
 	private String provider;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signal_strength_mapper);
 		
 		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
 		
 		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
 		boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		// Check if enabled and if not send user to the GSP settings
 		if (!enabled) {
 		  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		  startActivity(intent);
 		} 
 		
 		Criteria criteria = new Criteria();
 	    provider = lm.getBestProvider(criteria, false);
 	    Location location = lm.getLastKnownLocation(provider);
 
 	    // Initialize the location fields
 	    if (location != null) {
 	    	onLocationChanged(location);
 	    } 
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	    lm.requestLocationUpdates(provider, 400, 1, this);
 	}
 
 	@Override
 	protected void onPause() {
 	    super.onPause();
 	    lm.removeUpdates(this);
 	}
 	
 	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
 		@Override
 		public void onSignalStrengthsChanged(SignalStrength signalStrength)
 		{
 			TextView operatorView = (TextView) findViewById(R.id.operator);
 			operatorView.setText(tm.getNetworkOperatorName());
 			TextView signalStrengthView = (TextView) findViewById(R.id.signalStrength);
 			signalStrengthView.setText(getGsmSignalStrengthInDbm(signalStrength.getGsmSignalStrength()));
 			TextView berView = (TextView) findViewById(R.id.bitErrorRate);
 			berView.setText(getBER(signalStrength.getGsmBitErrorRate()));
 			
 			
 			super.onSignalStrengthsChanged(signalStrength);
 		}
 	};
 	
 	// 3GPP TS27.007 Section 8.5
 	private String getGsmSignalStrengthInDbm(int gsmSignalStrength) {
 		if (gsmSignalStrength == 99)
 			return "Not Detectable";
 		
		int dbmStrength = -113;
		dbmStrength = dbmStrength + (gsmSignalStrength * 2);
 		String strength = Integer.toString(dbmStrength) + " dBm";
 		return strength;
 	}
 	
 	// 3GPP TS45.008 Section 8.2.4
 	private String getBER(int ber) {
 		String berString;
 		switch (ber) {
 		case 0:
 			berString = "0.14 %";
 			break;
 		case 1:
 			berString = "0.28 %";
 			break;
 		case 2:
 			berString = "0.57 %";
 			break;
 		case 3:
 			berString = "1.13 %";
 			break;
 		case 4:
 			berString = "2.26 %";
 			break;
 		case 5:
 			berString = "4.53 %";
 			break;
 		case 6:
 			berString = "9.05 %";
 			break;
 		default:
 			berString = "18.10 %";
 		}
 		
 		return berString;
 	}
 	
 	private String getGpsCoordinates(Location location) {
 		DecimalFormat df3 = new DecimalFormat("###.###");
 		double lat = Double.valueOf(df3.format(location.getLatitude()));
 		double lng = Double.valueOf(df3.format(location.getLongitude()));
 		
 		String gpsCoordinates = Double.toString(lat) + " ; " + Double.toString(lng);
 		
 		return gpsCoordinates;
 	}
 	
 	private String getLocation(Location location) {
 		float accuracy = location.getAccuracy();
 		
 		String locationString = "Indoor";
 		
 		if (accuracy < 10)
 			locationString = "Outdoor";
 		
 		locationString = locationString + " (Accuracy = " + Float.toString(accuracy) + " m)";
 		
 		return locationString;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.signal_strength_mapper, menu);
 		return true;
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		TextView gpsCoordinatesView = (TextView) findViewById(R.id.gpsCoordinates);
 		gpsCoordinatesView.setText(getGpsCoordinates(location));
 		
 		TextView locationView = (TextView) findViewById(R.id.location);
 		locationView.setText(getLocation(location));
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		
 	}
 
 }
