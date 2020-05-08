 package com.abril.traker.satelite;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Satelite extends Activity implements LocationListener {
 		
 	private TextView latituteField;
 	private TextView longitudeField;
 	private LocationManager locationManager;
 	private String provider;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		latituteField = (TextView) findViewById(R.id.TextView02);
 		longitudeField = (TextView) findViewById(R.id.TextView04);
 
 		// Get the location manager
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		// Define the criteria how to select the locatioin provider -> use
 		// default
 		Criteria criteria = new Criteria();
 		provider = locationManager.getBestProvider(criteria, false);
 		Location location = locationManager.getLastKnownLocation(provider);
 
 		// Initialize the location fields
 		if (location != null) {
 			System.out.println("Provider " + provider + " has been selected.");
 			int lat = (int) (location.getLatitude());
 			int lng = (int) (location.getLongitude());
 			latituteField.setText(String.valueOf(lat));
 			longitudeField.setText(String.valueOf(lng));
 		} else {
 			latituteField.setText("Provider not available");
 			longitudeField.setText("Provider not available");
 		}
 	}
 
 	/* Request updates at startup */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		locationManager.requestLocationUpdates(provider, 400, 1, this);
 	}
 
 	/* Remove the locationlistener updates when Activity is paused */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
		double lat = (location.getLatitude());
		double lng = (location.getLongitude());
		latituteField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
 		sendLocation(location);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(this, "Enabled new provider " + provider,
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(this, "Disenabled provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 	
 	private void sendLocation(Location location) {
 		double lat = (location.getLatitude());
 		double lng = (location.getLongitude());
 		
 		//Started POST "/locals" for 127.0.0.1 at 2011-03-11 00:51:08 -0300
 		//  Processing by LocalsController#create as HTML
 		//  Parameters: {"utf8"=>"✓", "authenticity_token"=>"I2sSV7wL5DsWUW7+mjnZgG4J7/lhJ6Jov9bHVbae7d0=", "local"=>{"satelite_id"=>"1", "latitude"=>"13.9393", "longitude"=>"-42.9999"}, "commit"=>"Create Local"}
 		
 		// sendLocation to "local"=>{"satelite_id"=>"1", "latitude"=>"13.9393", "longitude"=>"-42.9999"}
 		URI url=null;
 		try {
 			url = new URI("http://www.greenlizard.com.br/locals");
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		HttpParams params = new BasicHttpParams();
		params.setParameter("satelite_id", 1);
		params.setDoubleParameter("latitude", lat);
		params.setDoubleParameter("longitude", lng);
 		HttpClient client = new DefaultHttpClient(params);
 		HttpPost post = new HttpPost(url);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(); 
		pairs.add(new BasicNameValuePair("satelite_id", "1")); 
		pairs.add(new BasicNameValuePair("latitude", Double.toString(lat))); 
		pairs.add(new BasicNameValuePair("longitude", Double.toString(lng))); 
		
 		try 
 		{
		post.setEntity(new UrlEncodedFormEntity(pairs));
 		} 
 		catch (UnsupportedEncodingException e) 
 		{
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 		} 
		
		String endResult = null;
 
 		try 
 		{
 		String response = client.execute(post, new BasicResponseHandler());
 		endResult = response;
 		} 
 		catch (ClientProtocolException e) 
 		{
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 		}  		
 		
		Toast.makeText(this, endResult, Toast.LENGTH_SHORT).show();

 	}
 	 
 	 	 
 }
