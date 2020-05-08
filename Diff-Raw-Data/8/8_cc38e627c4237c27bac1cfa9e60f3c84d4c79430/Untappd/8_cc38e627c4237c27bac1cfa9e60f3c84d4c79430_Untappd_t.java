 package com.cbetz.untappd;
 
 import java.io.BufferedReader;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.util.Log;
 
 import com.cbetz.untappd.exceptions.UntappdCredentialsException;
 import com.cbetz.untappd.exceptions.UntappdException;
 import com.cbetz.untappd.parsers.BreweryParser;
 import com.cbetz.untappd.parsers.CheckinParser;
 import com.cbetz.untappd.parsers.BeerParser;
 import com.cbetz.untappd.parsers.CheckinResponseParser;
 import com.cbetz.untappd.parsers.UserParser;
 import com.cbetz.untappd.types.Beer;
 import com.cbetz.untappd.types.Brewery;
 import com.cbetz.untappd.types.Checkin;
 import com.cbetz.untappd.types.CheckinResponse;
 import com.cbetz.untappd.types.User;
 
 public class Untappd {
 	private static final String API_ENDPOINT = "http://api.untappd.com/v4";
 	private static final String BEER_CHECKIN = "/checkin/add";	
 	private static final String BEER_INFO = "/beer/info";
 	private static final String BEER_SEARCH = "/search/beer";
 	private static final String CHECKIN_DETAILS = "/checkin/view";
 	private static final String FRIEND_FEED = "/checkin/recent";
 	private static final String USER_INFO = "/user/info";
 	private static final String WISHLIST = "/user/wishlist";
 	private static final String WISHLIST_ADD = "/user/wishlist/add";
	private static final String WISHLIST_DELETE = "/user/wishlist/delete";
 	private String mAccessToken;
 	
 	public Untappd(String accessToken) {
 		mAccessToken = accessToken;
 	}
 	
 	public ArrayList<Beer> beerSearch(String q, String sort) throws UntappdException {
 		Beer beer = null;
 		Brewery brewery = null;
 		JSONArray array = new JSONArray();
 		
 		try {
 			JSONObject object = getResponse(BEER_SEARCH + "?access_token=" + mAccessToken + "&q=" + URLEncoder.encode(q) + "&sort=" + sort);
 			array = object.getJSONObject("response").getJSONObject("beers").getJSONArray("items");
 		} catch (JSONException e1) {
 			Log.e("beerSearch", e1.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		
 		ArrayList<Beer> beers = new ArrayList<Beer>();
 		
 		for (int i=0; i<array.length(); ++i){
 			try {
 				beer = new BeerParser().parse(array.getJSONObject(i).getJSONObject("beer"));
 				brewery = new BreweryParser().parse(array.getJSONObject(i).getJSONObject("brewery"));
 				beer.setBrewery(brewery);
 			} catch (JSONException e2) {
 				Log.e("beerSearch", e2.getStackTrace().toString());
 			}
 			beers.add(beer);
 		}
 		return beers;
 	}
 	
 	public Beer beerInfo(String bid) throws UntappdException {
 		Beer beer = null;
 		
 		try {
 			JSONObject object = getResponse(BEER_INFO + "/" + bid + "?access_token=" + mAccessToken);
 			beer = new BeerParser().parse(object.getJSONObject("response").getJSONObject("beer"));
 		} catch (JSONException e) {
 			Log.e("beerInfo", e.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		
 		return beer;
 	}
 	
 	public CheckinResponse beerCheckin(String bid, String timezone, String offset, String shout, String rating, String twitter, String facebook) throws UntappdException {
 		CheckinResponse checkin = null;
 		
 		try {		
 			JSONObject object = postResponse(BEER_CHECKIN + "?access_token=" + mAccessToken, 
 					new BasicNameValuePair("bid", bid),
 					new BasicNameValuePair("timezone", timezone),
 					new BasicNameValuePair("gmt_offset", offset),
 					new BasicNameValuePair("shout", shout),
 					new BasicNameValuePair("rating", rating),
 					new BasicNameValuePair("twitter", twitter),
 					new BasicNameValuePair("facebook", facebook));
 			checkin = new CheckinResponseParser().parse(object.getJSONObject("response"));		
 		} catch (JSONException e) {
 			Log.e("beerCheckin", e.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		return checkin;
 	}
 	
 	public CheckinResponse beerCheckin(String bid, String timezone, String offset, String shout, String rating, String twitter, String facebook, String foursquareId, String lat, String lng, String foursquare) throws UntappdException {
 		CheckinResponse checkin = null;
 		
 		try {
 			JSONObject object = postResponse(BEER_CHECKIN + "?access_token=" + mAccessToken, 
 					new BasicNameValuePair("bid", bid),
 					new BasicNameValuePair("timezone", timezone),
 					new BasicNameValuePair("gmt_offset", offset),
 					new BasicNameValuePair("shout", shout),
 					new BasicNameValuePair("rating", rating),
 					new BasicNameValuePair("twitter", twitter),
 					new BasicNameValuePair("facebook", facebook),
 					new BasicNameValuePair("geolat", lat),
 					new BasicNameValuePair("geolng", lng),
 					new BasicNameValuePair("foursquare_id", foursquareId),
 					new BasicNameValuePair("foursquare", foursquare));
 			checkin = new CheckinResponseParser().parse(object.getJSONObject("response"));
 		} catch (JSONException e) {
 			Log.e("beerCheckin", e.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		return checkin;
 	}
 	
 	public Checkin[] getFriendFeed() throws UntappdException {
 		JSONArray array = new JSONArray();
 		
 		try {
 			JSONObject object = getResponse(FRIEND_FEED + "?access_token=" + mAccessToken);
 			array = object.getJSONObject("response").getJSONObject("checkins").getJSONArray("items");
 		} catch (JSONException e1) {
 			Log.e("userCheckin", e1.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 			
 		Checkin[] userCheckins = new Checkin[array.length()];
 		Checkin userCheckin = null;
 		
 		for (int i=0; i<array.length(); ++i){
 			try {
 				userCheckin = new CheckinParser().parse(array.getJSONObject(i));
 			} catch (JSONException e2) {
 				Log.e("beerSearch", e2.getStackTrace().toString());
 			}
 			userCheckins[i] = userCheckin;
 		}
 		
 		
 		return userCheckins;
 	}
 	
 	public ArrayList<Beer> getWishlist() throws UntappdException {
 		Beer beer = null;
 		JSONArray array = new JSONArray();
 		
 		try {
 			JSONObject object = getResponse(WISHLIST + "?access_token=" + mAccessToken);
 			array = object.getJSONObject("response").getJSONObject("beers").getJSONArray("items");
 		} catch (JSONException e1) {
 			Log.e("getWishlist", e1.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		
 		ArrayList<Beer> beers = new ArrayList<Beer>();
 		
 		for (int i=0; i<array.length(); ++i){
 			try {
 				beer = new BeerParser().parse(array.getJSONObject(i));
 			} catch (JSONException e2) {
 				Log.e("getWishlist", e2.getStackTrace().toString());
 			}
 			beers.add(beer);
 		}
 		return beers;
 	}
 	
 	public boolean wishlistAdd(String bid) throws UntappdException {
 		try {
 			getResponse(WISHLIST_ADD + "?access_token=" + mAccessToken + "&bid=" + bid);
 			return true;
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 	}
 	
 	public boolean wishlistRemove(String bid) throws UntappdException {
 		try {
			getResponse(WISHLIST_DELETE + "?access_token=" + mAccessToken + "&bid=" + bid);
 			return true;
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 	}
 	
 	public User userInfo() throws UntappdException {
 		User user = null;
 		
 		try {
 			JSONObject object = getResponse(USER_INFO + "?access_token=" + mAccessToken);
 			if (object.getJSONObject("response") != null) {
 				user = new UserParser().parse(object.getJSONObject("response").getJSONObject("user"));
 			}
 		} catch (JSONException e) {
 			Log.e("userInfo", e.toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		
 		return user;
 	}
 	
 	public Checkin getCheckinDetails() throws UntappdException {
 		Checkin checkinDetails = null;
 		
 		try {
 			JSONObject object = postResponse(CHECKIN_DETAILS + "?access_token=" + mAccessToken);
 			if (object.getJSONObject("response") != null) {
 				checkinDetails = new CheckinParser().parse(object.getJSONObject("response"));
 			}
 		} catch (JSONException e) {
 			Log.e("userInfo", e.getStackTrace().toString());
 		} catch (UntappdException ue) {
 			throw ue;
 		}
 		
 		return checkinDetails;
 	}
 	
 	private JSONObject getResponse(String url) throws UntappdException {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse httpresponse;
 		JSONObject object = null;
 		try {
 			HttpGet httpget = new HttpGet(API_ENDPOINT + url);
 			
 			httpresponse = httpclient.execute(httpget);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent(), "UTF-8"));
 			StringBuilder builder = new StringBuilder();
 			for (String line = null; (line = reader.readLine()) != null;) {
 				builder.append(line).append("\n");
 			}
 			JSONTokener tokener = new JSONTokener(builder.toString());
 			object = new JSONObject(tokener);
 			
 			int responseCode = object.getJSONObject("meta").getInt("code");
 			if (responseCode == 401) {
 				throw new UntappdCredentialsException(object.getString("error"));
 			} else if (responseCode != 200) {
				throw new UntappdException(object.getString("error_detail"));
 			}
 			return object;
 		} catch (ClientProtocolException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} catch (IOException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} catch (JSONException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} 
 		
 		return object;
 	}
 	
 	private JSONObject postResponse(String url, NameValuePair...nameValuePairs) throws UntappdException {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(API_ENDPOINT + url);
 		HttpResponse httpresponse;
 		JSONObject object = null;
 		
 		try {
 			List<NameValuePair> params = new ArrayList<NameValuePair>();
 			for (int i = 0; i < nameValuePairs.length; i++) {
 				NameValuePair param = nameValuePairs[i];
 				if (param.getValue() != null) {
 					params.add(param);
 				}
 			}
 			httppost.setEntity(new UrlEncodedFormEntity(params));
 			
 			httpresponse = httpclient.execute(httppost);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent(), "UTF-8"));
 			StringBuilder builder = new StringBuilder();
 			for (String line = null; (line = reader.readLine()) != null;) {
 				builder.append(line).append("\n");
 			}
 			JSONTokener tokener = new JSONTokener(builder.toString());
 			object = new JSONObject(tokener);
 			
 			int responseCode = object.getJSONObject("meta").getInt("code");
 			if (responseCode == 401) {
 				throw new UntappdCredentialsException(object.getString("error"));
 			} else if (responseCode != 200) {
 				throw new UntappdException(object.getString("error"));
 			}
 			return object;
 		} catch (ClientProtocolException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} catch (IOException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} catch (JSONException e) {
 			Log.e("getResponse", e.getStackTrace().toString());
 		} 
 		
 		return object;
 	}
 
 	public String getAccessToken() {
 		return mAccessToken;
 	}
 
 	public void setAccessToken(String accessToken) {
 		this.mAccessToken = accessToken;
 	}
 	
 }
