 package com.manavo.HowFarTo;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
import android.widget.Toast;
 
 public class LocationLookup extends AsyncTask<String, Void, List<Address>> {
 	
 	private main activity;
 	
 	public LocationLookup(main activity) {
 		this.activity = activity;
 	}
 	
 	@Override
 	protected List<Address> doInBackground( String... params )  {
 		Geocoder g = new Geocoder(this.activity);
     	try {
     		List<Address> addresses = g.getFromLocationName(params[0].trim(), 5);
 			return addresses;
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this.activity, "Could not connect. Please try again!", Toast.LENGTH_SHORT).show();
 			return null;
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(List<Address> result)  {
 		// If the call was successful, run the callback
 		if (result != null) {
 			this.activity.searchLocationCallback(result);
 		}
 		this.activity.hideDialog();
 	}
 }
