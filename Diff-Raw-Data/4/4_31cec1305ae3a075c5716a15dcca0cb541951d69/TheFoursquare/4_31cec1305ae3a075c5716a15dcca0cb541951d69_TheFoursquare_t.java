 package com.megadevs.socialwrapper.thefoursquare;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Map;
 import java.util.Vector;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.jiramot.foursquare.android.Foursquare;
 import com.megadevs.socialwrapper.SocialFriend;
 import com.megadevs.socialwrapper.SocialNetwork;
 import com.megadevs.socialwrapper.SocialSessionStore;
 import com.megadevs.socialwrapper.SocialWrapper;
 import com.megadevs.socialwrapper.exceptions.InvalidAuthenticationException;
 import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;
 
 /**
  * This class models a personal Foursquare object. With an instance of 
  * TheFoursquare it is possible to authenticate, search venues and retrieve
  * various informations.
  * @author dextor
  *
  */
 public class TheFoursquare extends SocialNetwork {
 
 	private Foursquare mFoursquare;
 	private static TheFoursquare iAmTheFoursquare;
 	private Activity mActivity;
 
 	private String clientID;
 	private String clientSecret;
 	private String callbackURL;
 	private String accessToken;
 	
 	public final String clientIDKey = "client_id";
 	public final String clientSecretKey = "client_secret";
 	public final String callbackURLKey = "callback_url";
 	public final String accessTokenKey = "access_token";
 	
 	// static callback refs
 	private TheFoursquareLoginCallback loginCallback;
 	private TheFoursquareFriendListCallback friendslistCallback;
 
 	
 	private ArrayList<SocialFriend> mFoursquareFriends;
 	/**
 	 * Defaul constructor for the TheFoursquare class.
 	 * @param a the main activity
 	 */
 	public TheFoursquare(String id, Activity a) {
 		this.id = id;
 		mActivity = a;
 		iAmTheFoursquare = this;
 		
 		tag = "SocialWrapper-Foursquare";
 	}
 
 	/**
 	 * This method is used within the callback procedure to
 	 * set the authenticated Foursquare object.
 	 * @return the existing instance of TheFoursquare
 	 */
 	public static TheFoursquare getInstance() {
 		return iAmTheFoursquare;
 	}
 	
 	/**
 	 * This method is called from outside the wrapper and it is used to
 	 * set the application ID and the callback URL; the 
 	 * instanciation of the mFoursquare object can be done 
 	 * only after this action is completed.
 	 * @param id the application id provided by Foursquare
 	 */
 	public void setFSParams(String id, String secret, String url) {
 		clientID = id;
 		clientSecret = secret;
 		callbackURL = url;
 		
 		mFoursquare = new Foursquare(clientID, callbackURL);
 		
 		SocialSessionStore.restore(SocialWrapper.FOURSQUARE, this, mActivity);
 	}
 	
 	/**
 	 * 
 	 * @param obj
 	 */
 	public void setFoursquare(Foursquare obj) {
 		mFoursquare = obj;
 		// setting the newly-received access token
 		accessToken = mFoursquare.getAccessToken();
 		
 		Log.i(tag, "session validation: "+mFoursquare.isSessionValid());
 		SocialSessionStore.save(SocialWrapper.FOURSQUARE, this, mActivity);
 		
 		if (loginCallback != null) {
 			loginCallback.onLoginCallback(actionResult);
 			loginCallback = null;
 		}
 	}
 	
 	@Override
 	public void authenticate(SocialBaseCallback r) throws InvalidAuthenticationException {
 		loginCallback = (TheFoursquareLoginCallback) r;
 		if (mFoursquare.isSessionValid()) {
 			Log.i(tag, "session valid, use it wisely :P");
 			loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
 		}
 		else {
 			Intent i = new Intent(mActivity, TheFoursquareActivity.class);
 			Bundle b = new Bundle();
 			b.putString(clientIDKey, clientID);
 			b.putString(callbackURLKey, callbackURL);
 			i.putExtras(b);
 			mActivity.startActivity(i);
 		}
 		
 	}
 
 	@Override
 	public void deauthenticate() {
 		accessToken = "";
 		SocialSessionStore.clear(SocialWrapper.FOURSQUARE, mActivity);
 	}
 
 	@Override
 	protected Vector<String[]> getConnectionData() {
 		Vector<String[]> data = new Vector<String[]>();
 		data.add(new String[] {clientIDKey, clientID});
 		data.add(new String[] {clientSecretKey, clientSecret});
 		data.add(new String[] {accessTokenKey, accessToken});
 		data.add(new String[] {callbackURLKey, callbackURL});
 		
 		return data;
 	}
 
 	@Override
 	protected void setConnectionData(Map<String, String> connectionData) {
 		if (connectionData.size()==0) {
 			this.connectionData = null;
 		}
 		else {
 			clientID = connectionData.get(clientIDKey);
 			clientSecret = connectionData.get(clientSecretKey);
 			callbackURL = connectionData.get(callbackURLKey);
 			accessToken = connectionData.get(accessTokenKey);
 			
 			mFoursquare.setAccessToken(accessToken);
 		}
 	}
 	
 	public void forwardResult() {
 		if (loginCallback != null)
 			loginCallback.onErrorCallback(actionResult);
 		else if (friendslistCallback != null)
 			friendslistCallback.onErrorCallback(actionResult);
 	}
 
 	
 	/**
 	 * This method is used to seach the nearby venues from the 
 	 * current position. Each venue is then encapsulated in a 
 	 * TheFoursquareVenue object, which has the latitude/longitude
 	 * coordinates, the distance of the venue from the current 
 	 * position, the name and the id of the venue (these two are
 	 * assigned by Foursquare).
 	 * 
 	 * @param position the current position
 	 * @return an ArrayList of nearby venues
 	 * @throws InvalidSocialRequestException
 	 */
 	public ArrayList<TheFoursquareVenue> searchVenues(GeoPoint position) throws InvalidSocialRequestException {
 		int longitude = position.getLongitudeE6();
 		int latitude = position.getLatitudeE6();
 		String ll = String.valueOf(longitude) + "," + String.valueOf(latitude);
 		
 		Bundle b = new Bundle();
 		b.putString("ll", ll);
 		
 		// venues are searchable even if no user is logged in
 		if (!isAuthenticated()) {
 			b.putString(clientIDKey, clientID);
 			b.putString(clientSecretKey, clientSecret);
 			Calendar c = Calendar.getInstance();
 			String date = String.valueOf(c.get(Calendar.YEAR)) + String.valueOf(c.get(Calendar.MONTH)) + String.valueOf(c.get(Calendar.DAY_OF_MONTH));
 			b.putString("v", date);
 		}
 
 		ArrayList<TheFoursquareVenue> venues = null;
 		try {
 			String result = mFoursquare.request("venues/search", b);
 			
 			// parsing the request result
 			JSONObject obj = new JSONObject(result);
 			JSONObject response = obj.getJSONObject("response");
 			JSONArray groups = response.getJSONArray("groups");
 			JSONObject element = groups.getJSONObject(0);
 			JSONArray items = element.getJSONArray("items");
 			
 			venues = new ArrayList<TheFoursquareVenue>(items.length());
 			for (int i=0; i<items.length(); i++) {
 				JSONObject item = items.getJSONObject(i);
 				String id = item.getString("id");
 				String name = item.getString("name");
 				
 				JSONObject location = item.getJSONObject("location");
 				String lat = location.getString("lat");
 				String lon = location.getString("lng");
 				String dist = location.getString("distance");
 				
 				venues.add(new TheFoursquareVenue(
 						Float.valueOf(lat).intValue(),
 						Float.valueOf(lon).intValue(),
						id,
						name, 
 						Integer.valueOf(dist).intValue()));
 			}
 			
 		} catch (MalformedURLException e) {
 			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
 		} catch (IOException e) {
 			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
 		} catch (JSONException e) {
 			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
 		}
 		return venues;
 	}
 	
 	@Override
 	public void getFriendsList(SocialBaseCallback s) throws InvalidSocialRequestException {
 		mFoursquareFriends = new ArrayList<SocialFriend>();
 
 		try {
 			String result = mFoursquare.request("users/self/friends");
 			
 			JSONObject obj;
 			obj = new JSONObject(result);
 			// corresponds to the JSON response structure; see official API documentation
 			// for more informations
 			JSONObject response = obj.getJSONObject("response"); 
 			JSONObject friends = response.getJSONObject("friends");
 			JSONArray items = friends.getJSONArray("items");
 			
 			for (int i=0; i<items.length(); i++) {
 				JSONObject item = items.getJSONObject(i);
 				
 				String id = item.getString("id");
 				String name = item.getString("firstName");
 				String surname = item.getString("lastName");
 				
 				mFoursquareFriends.add(new SocialFriend(id, name+' '+surname, null));
 			}
 
 		} catch (JSONException e) {
 			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
 		}
 	}
 
 	@Override
 	public String getAccessToken() {
 		if (accessToken != null)
 			return accessToken;
 		
 		return null;
 	}
 
 	@Override
 	public boolean isAuthenticated() {
 		if (accessToken != null && accessToken != "") return true;
 		else return false;
 	}
 
 	@Override
 	public String getId() {
 		return this.id;
 	}
 	
 	public void setActionResult(String result) {actionResult = result;}
 	
 	///
 	///	CALLBACK ADAPTER CLASSES
 	///
 	
 	public static abstract class TheFoursquareLoginCallback implements SocialBaseCallback {
 		public abstract void onLoginCallback(String result);
 		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error); 
 	}
 
 	public static abstract class TheFoursquareSearchCallback implements SocialBaseCallback {
 		public void onLoginCallback(String result) {};
 		public abstract void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list);
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error);
 	}
 
 	public static abstract class TheFoursquareFriendListCallback implements SocialBaseCallback {
 		public void onLoginCallback(String result) {};
 		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
 		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
 		public abstract void onErrorCallback(String error);
 	}
 
 	
 }
