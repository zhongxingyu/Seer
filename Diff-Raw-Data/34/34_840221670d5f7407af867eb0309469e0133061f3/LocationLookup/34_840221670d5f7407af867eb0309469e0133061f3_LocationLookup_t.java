 package com.manavo.HowFarTo;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 
 public class LocationLookup extends AsyncTask<String, Void, List<Address>> {
 	
 	private main activity;
	private String error = null;
 	
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
			this.error = "Could not connect. Please try again!";
			return null;
		} catch (Exception e) {
			this.error = "Oops! Something went wrong! Please try again!";
 			return null;
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(List<Address> result)  {
 		// If the call was successful, run the callback
 		if (result != null) {
 			this.activity.searchLocationCallback(result);
		} else if (this.error != null) {
			this.activity.searchLocationError(this.error);
 		}
 		this.activity.hideDialog();
 	}
 }
