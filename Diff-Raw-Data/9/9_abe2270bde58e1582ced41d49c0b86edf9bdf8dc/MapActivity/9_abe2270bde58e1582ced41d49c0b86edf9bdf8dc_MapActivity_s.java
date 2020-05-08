 package com.engine9;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 
 import com.engine9.R;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Build;
 
 
 /***
  * Show the basic map and user can select
  * */
 public class MapActivity extends FragmentActivity {
 
 	private GoogleMap mMap;
 	private JsonObject jData;
 	private JsonArray sjData;
 	private Polyline line;
 	private Vector<StopInfo> markers = new Vector<StopInfo>();
 	private Boolean sReady = false;
 	private Boolean pReady = false;
 	private CountDownTimer cdt;
 	private Marker vehicle;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		Intent i = getIntent();
 		final String iURL = i.getStringExtra("route");
 		final String sURL = i.getStringExtra("stops");
 		new MapRequest().execute(iURL);
 		new StopRequest().execute(sURL);
 		
 		setUpMap( null);
 		
 		Button aButton = (Button) findViewById(R.id.map_to_abstract_button);
 		aButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				Intent ia = new Intent(getApplicationContext(), AbstractActivity.class);
 				ia.putExtra("stops", sURL);
 				ia.putExtra("route", iURL);
 				startActivity(ia);
 			}
 			
 		});
 
 	}
 	
 	protected void onPause(){
 		super.onPause();
 		if(cdt != null){
 			cdt.cancel();
 		}
 	}
 	
 	protected void onStop(){
 		super.onPause();
 		if(cdt != null){
 			cdt.cancel();
 		}
 	}
 	
	/*protected void onResume(){
 		super.onResume();
		cdt.start();
	}*/
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Set initial map and use current location to should on the centre of screen.
 	 * 
 	 * @param center
 	 * 		the user's current location
 	 * */
 	private void setUpMap(LatLng center) {
 		if (mMap == null) {
 			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			if(center != null){
 				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));}
 		}
 	}
 	
 	private void addPolyline(){
 		for (JsonElement p : jData.get("Paths").getAsJsonArray())
 		{
 			JsonObject po = p.getAsJsonObject();
 			line = mMap.addPolyline(new PolylineOptions()
 			.addAll(decodePoly(po.get("Path").getAsString()))
 			.width(5)
 			.color(Color.RED));
 			
 		}
 	}
 	
 	private void addStops(){
 		for (JsonElement j : sjData){
 			JsonObject jo = j.getAsJsonObject();
 			
 			LatLng l = new LatLng(jo.get("Lat").getAsDouble(), jo.get("Lng").getAsDouble());
 			
 			
 			Marker m = mMap.addMarker(new MarkerOptions()
 			.position(l)
 			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
 			
 			Long t = Long.parseLong(jo.get("time").getAsString().substring(6, 19));
 			StopInfo sInfo = new StopInfo(m, t);
 			markers.add(sInfo);
 			
 			
 		}
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).m.getPosition(), 15));
 	}
 
 	/**
 	 * It extends the Request class (which handles getRequests) the onPostExecute 
 	 * function is overwritten so that the returned JSON data can be handled 
 	 * specifically for this activity (to get User location info)
 	 * */
 	public class MapRequest extends Request {
 		@Override
 		public void onPostExecute(String result)
 		{
 			try{
 				jData = (JsonObject) JParser2.main(result);
 				addPolyline();
 				pReady = true;
 				if(sReady && pReady){
 					sReady = false;
 					pReady = false;
 					updateBusPosition();
 					cdt = new CountDownTimer(10000, 10000){
 
 						@Override
 						public void onFinish() {
 							cdt.start();
 							
 						}
 
 						@Override
 						public void onTick(long arg0) {
 							vehicle.remove();
 							updateBusPosition();
 							
 						}
 						
 					}.start();
 				}
 				
 			}
 			catch(Exception e){
 				
 				Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		}
 	}
 	
 	public class StopRequest extends Request {
 		
 		@Override
 		public void onPostExecute(String result)
 		{
 			try{
 				sjData = (JsonArray) JParser2.main(result);
 				addStops();
 				sReady = true;
 				if(sReady && pReady){
 					sReady = false;
 					pReady = false;
 					updateBusPosition();
 					cdt = new CountDownTimer(10000, 10000){
 
 						@Override
 						public void onFinish() {
 							cdt.start();
 							
 						}
 
 						@Override
 						public void onTick(long arg0) {
 							vehicle.remove();
 							updateBusPosition();
 							
 						}
 						
 					}.start();
 				}
 			}
 			catch(Exception e){
 				Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		}
 	}
 	/*
 	 * Decode algorithm by Jeffrey Sambells
 	 */
 	private List<LatLng> decodePoly(String encoded) {
 	    List<LatLng> poly = new ArrayList<LatLng>();
 	    int index = 0, len = encoded.length();
 	    int lat = 0, lng = 0;
 
 	    while (index < len) {
 	        int b, shift = 0, result = 0;
 	        do {
 	            b = encoded.charAt(index++) - 63;
 	            result |= (b & 0x1f) << shift;
 	            shift += 5;
 	        } while (b >= 0x20);
 	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
 	        lat += dlat;
 
 	        shift = 0;
 	        result = 0;
 	        do {
 	            b = encoded.charAt(index++) - 63;
 	            result |= (b & 0x1f) << shift;
 	            shift += 5;
 	        } while (b >= 0x20);
 	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
 	        lng += dlng;
 
 	        LatLng p = new LatLng((lat/ 1E5),(lng / 1E5));
 	        poly.add(p);
 	    }
 
 	    return poly;
 	}
 	
 	private void updateBusPosition(){
 		
 		Log.e("DEBUG", "test");
 		List<LatLng> pList = line.getPoints();
 		Vector<StopInfo> stops = prevAndNextStop();
 		
 		if(stops.get(0).equals(stops.get(1))){
 			vehicle = mMap.addMarker(new MarkerOptions()
 				.position(stops.get(0).m.getPosition())
 				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
 			
 			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stops.get(0).m.getPosition(), 15));
 			vehicle.showInfoWindow();
 		}
 		else
 		{
 			
 			int start = findClosestPolylinePoint(stops.get(0).m.getPosition());
 			int end = findClosestPolylinePoint(stops.get(1).m.getPosition());
 			
 			Double distBetween = calcPolylineDistance(start, end);
 			Double distRatio = ((double) (stops.get(1).time - System.currentTimeMillis())) / ((double) (stops.get(1).time - stops.get(0).time));
 			Double targetDist = distRatio * distBetween;
 			
 			Double currentDist = 0.0;
 			Log.e("DEBUG", String.valueOf(stops.get(0).m.getPosition().longitude - stops.get(1).m.getPosition().longitude));
 			Log.e("DEBUG", String.valueOf(start) + " " + String.valueOf(end));
 			int j, k;
 			if(start < end){
 				j = start;
 				k = end;
 				for(int i =k; i > j; i--){
 					
 					currentDist += calcDistance(pList.get(i), pList.get(i-1));
 					
 					if(currentDist >= targetDist){
 						vehicle = mMap.addMarker(new MarkerOptions()
 							.position(pList.get(i - 1))
 							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
 						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pList.get(i), 15));
 						vehicle.showInfoWindow();
 						break;
 					}
 				}
 			}
 			else{
 				j = end;
 				k = start;
 				for(int i =j; i < k; i++){
 					
 					currentDist += calcDistance(pList.get(i), pList.get(i+1));
 					
 					if(currentDist >= targetDist){
 						vehicle = mMap.addMarker(new MarkerOptions()
 							.position(pList.get(i + 1))
 							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
 						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pList.get(i), 15));
 						vehicle.showInfoWindow();
 						break;
 					}
 				}
 			}
 			
 			
 		}
 		
 		
 	}
 	
 	
 	private Vector<StopInfo> prevAndNextStop(){
 		int c = 0;
 		Vector<StopInfo> retVect = new Vector<StopInfo>();
 		for(StopInfo i : markers){
 			
 			if(System.currentTimeMillis() < i.time){
 				if(c ==0){
 					retVect.add(i);
 					retVect.add(i);
 					return retVect;
 				}
 				else{
 					if(markers.get(c -1).time < i.time){
 						retVect.add(markers.get(c - 1));
 					}
 					else if(c != markers.size() - 1){
 						retVect.add(markers.get(c + 1));
 					}
 					else
 					{
 						break;
 					}
 					
 					retVect.add(i);
 					return retVect;
 				}
 			}
 			c++;
 		}
 		
 		retVect.add(markers.get(markers.size()));
 		retVect.add(markers.get(markers.size()));
 		return retVect;
 		
 	}
 	
 	private int findClosestPolylinePoint(LatLng position){
 		List<LatLng> pList = line.getPoints();
 		Double closest = 99999.0;
 		int closestP = 0;
 		for(int i = 0; i < pList.size(); i ++){
 			Double dist = calcDistance(position, pList.get(i));
 			if(dist < closest){
 				closest = dist;
 				closestP = i;
 			}
 		}
 		
 		return closestP;
 	}
 	
 	private double calcPolylineDistance(int pos1, int pos2){
 		List<LatLng> pList = line.getPoints();
 		Double dist = 0.0;
 		
 		for(int i = pos1; i < pos2; i ++){
 			dist += calcDistance(pList.get(i), pList.get(i+1));
 		}
 		
 		return dist;
 	}
 	
 	private double calcDistance(LatLng start, LatLng end){
 		Double dLng = end.longitude - start.longitude;
 		Double dLat = end.latitude - start.latitude;
 		
 		Double angle = Math.pow((Math.sin(dLat /2)), 2) + Math.cos(start.latitude) * Math.cos(end.latitude) * Math.pow((Math.sin(dLng /2)), 2);
 		Double cir = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));
 		
 		return 6373 * cir;
 	}
 	
 	private class StopInfo
 	{
 		public Marker m;
 		public Long time;
 		
 		public StopInfo(Marker m, Long time){
 			this.m = m;
 			this.time = time;
 		}
 	}
 
 	
 }
