 package org.omships.omships;
 
 import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.os.AsyncTask;
 
 class FetchLocation extends AsyncTask<String,Integer,LatLng>{
 		private SupportMapFragment map;
 		
 	public FetchLocation(SupportMapFragment map) {
 		this.map=map;
 	}
 	
 	@Override
 	protected LatLng doInBackground(String... urls) {
 		Double lat=0.0,lng=0.0;
 		try {
 			Scanner input=new Scanner(
 					new BufferedInputStream(new URL(urls[0]).openStream()));
 			input.useDelimiter(",");
 			//unix time
 			input.next();
 			//lng then lat (backwards)
 			lng=input.nextDouble();
 			lat=input.nextDouble();
 			input.close();
 			return new LatLng(lat, lng);
		} catch (Exception e) {
			e.printStackTrace();
 			return null;
 		}
 	}//end doInBackground
 	
 	protected void onPostExecute(LatLng pos){
 		GoogleMap daMap =map.getMap(); 
 		if(daMap==null)return;
 		if(pos==null)return;
 		daMap.addMarker(new MarkerOptions()
         .position(pos)
         .title(Settings.getShip().getName()));
         daMap.moveCamera(
         		CameraUpdateFactory.newCameraPosition(
         				CameraPosition.fromLatLngZoom(
         						pos,5.0f)));
 	}//end onPostExecute
 	
 }//end class FetchLocation
