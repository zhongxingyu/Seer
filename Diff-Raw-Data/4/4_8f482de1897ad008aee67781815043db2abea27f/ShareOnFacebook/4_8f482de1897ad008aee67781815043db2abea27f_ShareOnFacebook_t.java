 /*******************************************************************************
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @contributor(s): Freerider Team 2 (Group 3, IT2901 Spring 2013, NTNU)
  * @version: 2.0
  * 
  * Copyright 2013 Freerider Team 2
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package no.ntnu.idi.socialhitchhiking.utility;
 
 import java.io.IOException;
 import java.util.BitSet;
 import java.util.Calendar;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.Visibility;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.xml.RequestSerializer;
 import no.ntnu.idi.freerider.xml.ResponseParser;
 import no.ntnu.idi.socialhitchhiking.Main;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.Toast;
 
 public class ShareOnFacebook extends SocialHitchhikingActivity{
 
 	private static final String APP_ID = "321654017885450";
 	private static final String[] PERMISSIONS = new String[] {"read_stream","publish_stream"};
 //,"publish_actions"
 	private static final String TOKEN = "access_token";
         private static final String EXPIRES = "access_expires";
         private static final String KEY = "facebook-credentials";
 
 	private Facebook facebook;
 	private String messageToPost;
 	private boolean isDriver;
 	private String date,time,seats,extras;
 	private Route currentRoute;
 
 	@SuppressWarnings("deprecation")
 	public boolean saveCredentials(Facebook facebook) {
         	Editor editor = getApplicationContext().getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
         	editor.putString(TOKEN, facebook.getAccessToken());
         	editor.putLong(EXPIRES, facebook.getAccessExpires());
         	return editor.commit();
     	}
 
     	@SuppressWarnings("deprecation")
 		public boolean restoreCredentials(Facebook facebook) {
         	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApp());
         	facebook.setAccessToken(sharedPreferences.getString(TOKEN, null));
         	facebook.setAccessExpires(sharedPreferences.getLong(EXPIRES, 0));
         	return facebook.isSessionValid();
     	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		currentRoute = getApp().getSelectedRoute();
 		facebook = new Facebook(APP_ID);
 		restoreCredentials(facebook);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.facebook_dialog);
 		
 		//Intitialize TripOption string values
 		String formatedDate = formatDate(getApp().getSelectedJourney().getStart());
 		String formatedTime = formatTime(getApp().getSelectedJourney().getStart());
 		
 		date = "Date: "+ formatedDate;
 		time = "Start time: "+ formatedTime;
 		seats = "Seats available: "+ getApp().getSelectedJourney().getTripPreferences().getSeatsAvailable();
 //		String extras = "Extras: "+ getApp().getSelectedJourney().getTripPreferences().toString();
 		BitSet sExtras = getApp().getSelectedJourney().getTripPreferences().getExtras();
 		extras = "Preferences: ";
 		String[] items = {"Music", "Animals", "Breaks", "Talking", "Smoking"};
     	for(int i=0 ; i<sExtras.length() ; i++){
     		if(sExtras.get(i)){
     			if(i==sExtras.length()-1)
     				extras=extras+items[i]+".";
     			else{
     				if(i==sExtras.length()-2)
     					extras=extras+items[i]+" and ";
     				else
         				extras=extras+items[i]+", ";
     			}
     		}
     	}
     	String facebookMessage = "";
 		isDriver = getIntent().getExtras().getBoolean("isDriver");
 		if (isDriver){
 			facebookMessage = "I have created a new drive on FreeRider\n"+date+"\n"+time+"\n"+seats+"\n"+extras;
 		}else{
 			facebookMessage = "I am hitchhiking on a new ride on FreeRider\n"+date+"\n"+time+"\n"+seats+"\n"+extras;;
 		}
 		messageToPost = facebookMessage;
 	}
 
 	public void doNotShare(View button){
 		Intent intent = new Intent(ShareOnFacebook.this, Main.class);
 		startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 		showToast("Trip created");
 		finish();
 	}
 	@SuppressWarnings("deprecation")
 	public void share(View button){
 //		if (! facebook.isSessionValid()) {
 //			loginAndPostToWall();
 //			postToWall(messageToPost);
 //		}
 //		else {
 			postToWall(messageToPost);
 			Intent intent = new Intent(ShareOnFacebook.this, Main.class);
 			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 //		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public void loginAndPostToWall(){
 		 facebook.authorize(this, PERMISSIONS, new LoginDialogListener());
 	}
 
 	@SuppressWarnings("deprecation")
 	public void postToWall(String message){
 	    
 		final Bundle postParams = new Bundle();
 
 		postParams.putString("message", message);
 		postParams.putString("caption", "https://maps.google.com/maps?saddr="+currentRoute.getStartAddress()
 				+"&daddr="+currentRoute.getEndAddress());
 		postParams.putString("description", "Click to see the route");
 		
 		JSONObject jsonObject = new JSONObject();
 		try {
 			if(getApp().getSelectedJourney().getVisibility().equals(Visibility.PUBLIC)){
 				jsonObject.put("value", "EVERYONE");
 			}
 			else{
 				if(getApp().getSelectedJourney().getVisibility().equals(Visibility.FRIENDS_OF_FRIENDS))
 					jsonObject.put("value", "FRIENDS_OF_FRIENDS");
 				else
					jsonObject.put("value", "ALL_FRIENDS");
 			}
 		} catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		postParams.putString("privacy", jsonObject.toString());
 //		postParams.putString("privacy", "EVERYONE");
 //		postParams.putString("actions", "[{'name':'Test a simple Graph API call!','link':'https://developers.facebook.com/tools/explorer?method=GET&path=me'}]");
 		postParams.putString("type", "photo");
 		postParams.putString("link", "https://maps.google.com/maps?saddr="+currentRoute.getStartAddress()
 				+"&daddr="+currentRoute.getEndAddress());
 		postParams.putString("picture", "http://www.veryicon.com/icon/png/Business/Business/Cars.png");
 		//Fix
 		ExecutorService executor = Executors.newSingleThreadExecutor();
 		
 	    Callable<Boolean> callable = new Callable<Boolean>() {
 	        @Override
 	        public Boolean call() throws ClientProtocolException, IOException {
 	        	try {
         	        facebook.request("me");
 			String response = facebook.request("me/feed", postParams, "POST");
 			Log.d("Tests", "got response: " + response);
 			if (response == null || response.equals("") || response.equals("false")) {
 				//showToast("Blank response.");
 			}
 			else {
 				//showToast("Trip created and posted to your facebook wall!");
 			}
 			finish();
 		} catch (Exception e) {
 			//showToast("Failed to post to wall!");
 			
 			e.printStackTrace();
 			return false;
 			//finish();
 		}
 	    		return true;
 	        }
 	    };
 	    Future<Boolean> future = executor.submit(callable);
 	    try {
 			Boolean ret = future.get();
 			if(ret){
 				if(isDriver){
 					showToast("Trip created and posted to Facebook!");
 				}else{
 					showToast("Posted to Facebook!");
 				}
 			}else {
 				showToast("Failed to post to wall!");
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    executor.shutdown();
                 
 	}
 
 	class LoginDialogListener implements DialogListener {
 	    public void onComplete(Bundle values) {
 	    	saveCredentials(facebook);
 	    	if (messageToPost != null){
 			postToWall(messageToPost);
 		}
 	    }
 	    public void onFacebookError(FacebookError error) {
 	    	showToast("Authentication with Facebook failed!");
 	        finish();
 	    }
 	    public void onError(DialogError error) {
 	    	showToast("Authentication with Facebook failed!");
 	        finish();
 	    }
 	    public void onCancel() {
 	    	showToast("Authentication with Facebook cancelled!");
 	        finish();
 	    }
 	}
 
 	private void showToast(String message){
 		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
 	}
 	 public String formatDate(Calendar c){
 	    	String formatedDate = c.get(Calendar.DAY_OF_MONTH)
 					+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
 	    	return formatedDate;
 	    }
 	    
 	    public String formatTime(Calendar c){
 			//This formats Calendar.MINUTE so minutes below 10 show a 0 before
 	    	Integer min = c.get(Calendar.MINUTE);
 			String minutes=min.toString();
 			if(min<10)
 				minutes="0"+minutes;
 			
 			String formatedTime = c.get(Calendar.HOUR_OF_DAY)+":"+minutes;
 			return formatedTime;
 	    }
 }
