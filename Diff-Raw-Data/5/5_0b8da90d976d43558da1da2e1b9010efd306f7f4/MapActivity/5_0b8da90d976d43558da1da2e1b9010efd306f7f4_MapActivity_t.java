 package com.example.PrayerTimes;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This shows how to create a simple activity with a map and a marker on the map.
  * <p>
  * Notice how we deal with the possibility that the Google Play services APK is not
  * installed/enabled/updated on a user's device.
  */
 public class MapActivity extends FragmentActivity implements OnMapLongClickListener{
 
 	/**
 	 * Note that this may be null if the Google Play services APK is not available.
 	 */
 	private GoogleMap mMap;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maindialog);
 		setUpMapIfNeeded();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setUpMapIfNeeded();
 	}
 
 	/**
 	 * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
 	 * installed) and the map has not already been instantiated.. This will ensure that we only ever
 	 * call {@link #setUpMap()} once when {@link #mMap} is not null.
 	 * <p>
 	 * If it isn't installed {@link SupportMapFragment} (and
 	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
 	 * install/update the Google Play services APK on their device.
 	 * <p>
 	 * A user can return to this FragmentActivity after following the prompt and correctly
 	 * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
 	 * completely destroyed during this process (it is likely that it would only be stopped or
 	 * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
 	 * {@link #onResume()} to guarantee that it will be called.
 	 */
 	private void setUpMapIfNeeded() {
 		// Do a null check to confirm that we have not already instantiated the map.
 		if (mMap == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
 					.getMap();
 			// Check if we were successful in obtaining the map.
 			if (mMap != null) {
 				setUpMap();
 			}
 		}
 	}
 
 	/**
 	 * This is where we can add markers or lines, add listeners or move the camera. In this case, we
 	 * just add a marker near Africa.
 	 * <p>
 	 * This should only be called once and when we are sure that {@link #mMap} is not null.
 	 */
 	private void setUpMap() {
 		//Get the Lat, Long sent from MainActivity
 		Bundle params = getIntent().getExtras();
 		//Set them as myPos
 		LatLng myPos = new LatLng(params.getDouble("latitude"), params.getDouble("longitude"));
 		//Add marker, moveCamera, and setZoom
 		mMap.addMarker(new MarkerOptions().position(myPos).title("You are here"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
 		//Set zoom level to 15 if the position is not 0,0
 		if(myPos.latitude != 0 || myPos.longitude!=0)
			mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 
 		//Setup the click listeners
 		mMap.setOnMapLongClickListener(this);
 
 		//Show help
 		Toast.makeText(this, "Press and hold on the map to set position.",Toast.LENGTH_LONG).show();
 
 		//Refresh the location label
 		TextView label = (TextView)findViewById(R.id.textView1);
 		label.setText(String.format("Location: Latitude: %.5f Longitude: %.5f",myPos.latitude,myPos.longitude));
 	}
 
 	public void onCancel(View view){
 		Intent mapIntent = getIntent();
 		mapIntent.putExtra("result","cancelled");
 		setResult(0,mapIntent);
 		this.finish();
 	}
 	public void onOK(View view){
 		Intent mapIntent = getIntent();
 		mapIntent.putExtra("result","ok");
 		setResult(0,mapIntent);
 		this.finish();
 	}
 
 	@Override
 	public void onMapLongClick(LatLng point) {
 		mMap.clear();
 		mMap.addMarker(new MarkerOptions().position(point).title("New position"));
 
 		//Refresh the location label
 		TextView label = (TextView)findViewById(R.id.textView1);
 		label.setText(String.format("Location: Latitude: %.5f Longitude: %.5f",point.latitude,point.longitude));
 
 		//Save the point to the intent data
 		Intent mapIntent = getIntent();
 		mapIntent.putExtra("latitude",point.latitude);
 		mapIntent.putExtra("longitude",point.longitude);
 
 	}  
 	@Override
 	public void onBackPressed() {
 		onCancel(null);
 	}
 }
