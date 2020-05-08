 package com.barview.rest;
 
 import android.os.AsyncTask;
 
 import com.barview.constants.BarviewConstants;
import com.barview.mobile.BarviewMobileUtility;
 import com.barview.rest.RestClient.RequestMethod;
 import com.barview.utilities.BarviewUtilities;
 import com.barview.utilities.FacebookUtility;
 
 public class FavoriteDeleter extends AsyncTask<String, Integer, String> {
 	
 	public static final String SUCCESS = "success";
 	public static final String ERROR	= "error";
 	
 	private String result;
 
 	@Override
 	protected String doInBackground(String... params) {
 		RestClient client = new RestClient(BarviewUtilities.getFavoriteURLForRunMode() + "/" + params[0]);
		if(FacebookUtility.isLoggedIn())
			client.AddHeader(BarviewConstants.REST_USER_ID, FacebookUtility.getRESTUserId());
		else if(BarviewMobileUtility.isLoggedIn())
			client.AddHeader(BarviewConstants.REST_USER_ID, BarviewMobileUtility.getUser().getUserId());
 		
 		try {
 			client.Execute(RequestMethod.DELETE);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result = ERROR;
 			return result;
 		}
 		
 		result = SUCCESS;
 		return result;
 	}
 
 	protected void onPostExecute(String result) {
 
 	}
 
 	public String getResult() {
 		return result;
 	}
 }
