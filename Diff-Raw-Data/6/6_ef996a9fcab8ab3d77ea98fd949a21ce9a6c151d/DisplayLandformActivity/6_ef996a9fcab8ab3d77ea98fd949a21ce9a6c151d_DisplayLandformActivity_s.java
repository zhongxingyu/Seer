 package com.tejadillas.armov3;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Toast;
 import es.ucm.look.ar.LookAR;
 import es.ucm.look.ar.util.LookARUtil;
 import es.ucm.look.data.EntityData;
 import es.ucm.look.data.LookData;
 
 public class DisplayLandformActivity extends LookAR {
 
 	protected MyApplication app;
 	
 	private LocationManager mLocationManager;
 	private LocationListener mLocationListener;
 
 	private double device_latitude;
 	private double device_longitude;
 	private double device_altitude;
 
 	private ArrayList<String[]> distanceViewLandforms;
 	private String[] landform;
 	private ArrayList<EntityData> labelList;
 
 	final Context context = this;
 
 	public DisplayLandformActivity() {
 		super(true, true, false, true, 100.0f, false);
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		device_latitude = 0;
 		device_longitude = 0;
 		device_altitude = 0;
 
 		// Initializations
 //		landformsDB = new ArrayList<String[]>();
//		distanceViewLandforms = new ArrayList<String[]>();
//		labelList = new ArrayList<EntityData>();
 
 		// Intent intent = getIntent();
 //		ArrayList<String> message = getIntent().getStringArrayListExtra(
 //				CountrySelectActivity.EXTRA_COUNTRY);
 //
 //		for (String group : message) {
 //			String[] secondSplit = group.split(",");
 //			landformsDB.add(secondSplit);
 //		}
 
 //		 for (String[] field : landformsDB) {
 //		 System.out.println(field[0] + "\t" + field[1] + "\t" + field[2]
 //		 + "\t" + field[3]);
 
 		// Enable hud over the camera view
 		ViewGroup hud = this.getHudContainer();
 		hud.addView(LookARUtil
 				.getView(R.layout.activity_display_landform, null));
 
 		// Handler for the button to update coordinates
 		Button gps_button = (Button) findViewById(R.id.button_gps);
 		gps_button.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Toast.makeText(
 						context,
 						"Updating coordinates: \n" + device_latitude + "\n"
 								+ device_longitude + "\n" + device_altitude,
 						Toast.LENGTH_SHORT).show();
 				mLocationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
 			}
 		});
 
 		// Declarations to get data from the gps location provider
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		mLocationListener = new CurrentLocationListener();
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				0, 0, mLocationListener);
 
 		LookData.getInstance().setWorldEntityFactory(new LabelEntityFactory());
 
 		LookData.getInstance().updateData();
 
 	}
 
 	// LOCATION LISTENER
 	public class CurrentLocationListener implements LocationListener {
 
 		public void onLocationChanged(Location location) {
 
 			device_latitude = location.getLatitude();
 			device_longitude = location.getLongitude();
 			device_altitude = location.getAltitude();
 
 			distanceViewLandformsFilter();
 
 			for (String[] s : distanceViewLandforms) {
 				createLabels(s);
 			}
 			for (EntityData e : labelList) {
 				LookData.getInstance().getDataHandler().addEntity(e);
 			}
 			LookData.getInstance().updateData();
 			mLocationManager.removeUpdates(mLocationListener);
 
 		}
 
 		public void onProviderDisabled(String provider) {
 
 		}
 
 		public void onProviderEnabled(String provider) {
 
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		mLocationManager.removeUpdates(mLocationListener);
 	}
 
 	private void distanceViewLandformsFilter() {
 
 		landform = new String[4];
 		float[] result = new float[7];
 		for (int i = 0; i < MySingleton.getInstance().landformsDB.size(); i++) {
 			landform = MySingleton.getInstance().landformsDB.get(i);
 			double lflatitude = Double.parseDouble(landform[1]);
 			double lflongitude = Double.parseDouble(landform[2]);
 			Location.distanceBetween(device_latitude, device_longitude,
 					lflatitude, lflongitude, result);
 			if (result[0] < 10000) {
 				distanceViewLandforms.add(landform);
 			}
 		}
 	}
 
 	private void createLabels(String[] s) {
 		double alt;
 		float altitude;
 		EntityData data = new EntityData();
 		if ((alt = Double.parseDouble(s[3])) != -9999) {
 			altitude = Double.valueOf(alt).floatValue() / 100;
 		} else {
 			altitude = 0.0f;
 		}
 		data.setLocation(1000 * (Double.valueOf(Double.parseDouble(s[2]))
 				.floatValue() - Double.valueOf(device_longitude).floatValue()),
 				altitude, 1000 * (Double.valueOf(Double.parseDouble(s[1]))
 						.floatValue() - Double.valueOf(device_latitude)
 						.floatValue()));
 		data.setPropertyValue(LabelEntityFactory.NAME, s[0]);
 		labelList.add(data);
 	}
 
 }
