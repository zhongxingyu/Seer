 package edu.illinois.geosight.servercom;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 
 import edu.illinois.geosight.ProgressCallback;
 
 /**
  * The GeosightEntity class handles JSON retrieval from both POST and GET methods and 
  * simplified ways to access the returned JSON.
  * @author Steven Kabbes
  */
 public class GeosightEntity {
 	
 	// Base url for all requests on Geosight
 	protected static final String BASE_URL = "https://geosight.heroku.com";
 	
 	// the http client to use for all requests.  Also handles cookies
 	protected static HttpClient client = null;
 	
 	// the http context used for doing queries.  This is to preserve cookies
 	protected static HttpContext httpContext = null;
 		
 	// The underlying JSONObject to retrieve data from (if it exists)
 	protected JSONObject jObj = null;
 	
 	// The underlying JSONArray to retrieve data from (if it exists)
 	protected JSONArray jArr = null;
 	
 	// enum for the 2 supported HTTP methods
 	protected enum Method { GET, POST };
 	
 	/**
 	 * Fetch a Geosight Entity from a POST (without any parameters)
 	 * @param relativeURL the relative URL to fetch the JSON from
 	 * @return
 	 * @throws GeosightException
 	 */
 	public static GeosightEntity jsonFromPost(String relativeURL) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();
 		result.go(relativeURL, Method.POST, null);
 		return result;
 	}
 	
 	/**
 	 * Fetch a Geosight Entity from a POST with parameters
 	 * @param relativeURL the relative url to fetch from
 	 * @param pairs the parameters to send along with the POST
 	 * @return
 	 * @throws GeosightException
 	 */
 	public static GeosightEntity jsonFromPost(String relativeURL, List<NameValuePair> pairs) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();	
 		result.go(relativeURL, Method.POST, pairs);
 		return result;
 	}
 	
 	/**
 	 * Fetch an array of object from the Geosight server using a POST
 	 * @param relativeURL the relative URL to fetch from
 	 * @return List of Geosight Entities retrieved from the server
 	 * @throws GeosightException
 	 */
 	public static List<GeosightEntity> jsonArrayFromPost(String relativeURL) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();
 		result.go(relativeURL, Method.POST, null);
 		return result.getArray();
 	}
 	
 	/**
 	 * Fetch an array of object from the Geosight server using a POST
 	 * @param relativeURL the relative URL to fetch from
 	 * @return List of Geosight Entities retrieved from the server
 	 * @throws GeosightException
 	 */
 	public static List<GeosightEntity> jsonArrayFromPost(String relativeURL, List<NameValuePair> pairs) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();	
 		result.go(relativeURL, Method.POST, pairs);
 		return result.getArray();
 	}
 	
 	/**
 	 * Fetch an object from the Geosight server using a GET request
 	 * @param relativeURL the relative URL to fetch from
 	 * @return List of Geosight Entities retrieved from the server
 	 * @throws GeosightException
 	 */
 	public static GeosightEntity jsonFromGet(String relativeURL) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();
 		result.go(relativeURL, Method.GET, null);
 		return result;
 	}
 	
 	/**
 	 * Fetch an array of object from the Geosight server using a GET
 	 * @param relativeURL the relative URL to fetch from
 	 * @return List of Geosight Entities retrieved from the server
 	 * @throws GeosightException
 	 */
 	public static List<GeosightEntity> jsonArrayFromGet(String relativeURL) throws GeosightException{
 		GeosightEntity result = new GeosightEntity();
 		result.go(relativeURL, Method.GET, null);
 		return result.getArray();
 	}
 	
 
 	/**
 	 * Geosight Entities must be constructed from the static methods above
 	 */
 	protected GeosightEntity(){
 		// keep all connections on one HTTP Client
 		if( client == null ){
 			client = new DefaultHttpClient();
 
 			httpContext = new BasicHttpContext();
 			httpContext.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore() );
 		}
 	}
 	
 	/**
 	 * Initialize a GeosightEntity with a JSONObject
 	 * @param obj the JSONObject to wrap
 	 */
 	public GeosightEntity(JSONObject obj){
 		this();
 		jObj = obj;
 	}
 	
 	protected void go(String relativeURL, Method method) throws GeosightException{
 		go( relativeURL, method, null );
 	}
 	
 	protected void go(String relativeURL, Method method, List<NameValuePair> pairs) throws GeosightException{
 		try{
 			
 			HttpUriRequest request;
 			if( method == Method.GET){
 				request = new HttpGet(BASE_URL + relativeURL);
 			} else {
 				HttpPost temp = new HttpPost(BASE_URL + relativeURL);
 				
 				if(pairs != null){
 					temp.setEntity(new UrlEncodedFormEntity(pairs));
 				}
 				request = temp;
 			}
 			
 			HttpResponse response = client.execute(request, httpContext);
 	
 			String str = EntityUtils.toString(response.getEntity());
 			
 			Log.v("JSON", "JSON RESULT:" + str + "!");
 			
 			Object temp = new JSONTokener(str).nextValue();
 			changeContext(temp);
 			
 		} catch (ParseException e) {
 			e.printStackTrace();
 
 			throw new GeosightException(e);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new GeosightException(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new GeosightException(e);
 		}
 	}
 	
 	public static void uploadImage(File file) {
 		uploadImage(file, null);
 	}
 
 	// http://moazzam-khan.com/blog/?tag=android-upload-file
 	public static void uploadImage(File file, ProgressCallback progress) {
 		HttpURLConnection connection = null;
 		DataOutputStream outputStream = null;
 		//DataInputStream inputStream = null;
 
 		String urlServer = BASE_URL + "/photos.json";
 		String lineEnd = "\r\n";
 		String twoHyphens = "--";
 		
 		// Maybe this should be random...
 		String boundary = "7665267813202";
 
 		int bytesRead, bytesAvailable, bufferSize;
 		byte[] buffer;
 		int maxBufferSize = 100 * 1024;
 
 		try {
 			FileInputStream fileInputStream = new FileInputStream(file);
 
 			URL url = new URL(urlServer);
 			connection = (HttpURLConnection) url.openConnection();
 
 			
 			CookieStore cookieStore = (CookieStore) httpContext.getAttribute( ClientContext.COOKIE_STORE );
 			List<Cookie> cookies = cookieStore.getCookies();
 			String cookieString = "";
 			for( Cookie c : cookies ){
 				cookieString += c.getName() + "=" + c.getValue() + ";";
 			}
 			cookieString = cookieString.replaceFirst(";^", "");
 			Log.v("COOKIES", cookieString);
 			
 			
 			
 			// Allow Inputs & Outputs
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 			connection.setUseCaches(false);
 
 			// Enable POST method
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Connection", "Keep-Alive");
 			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
 			connection.setRequestProperty("Cookie", cookieString);
 
 			outputStream = new DataOutputStream(connection.getOutputStream());
 			
 			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 			outputStream.writeBytes("Content-Disposition: form-data; name=\"photo[file]\"; filename=\""
 							+ file.getName() + "\"" + lineEnd);
 			outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
 			
 			outputStream.writeBytes(lineEnd);
 
 			bytesAvailable = fileInputStream.available();
 			
 			int bytesTotal = bytesAvailable;
 			int bytesSent = 0;
 			
 			bufferSize = Math.min(bytesAvailable, maxBufferSize);
 			buffer = new byte[bufferSize];
 
 			// Read file
 			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
 
 			while (bytesRead > 0) {
 				outputStream.write(buffer, 0, bufferSize);
 				
 				bytesSent += bufferSize;
 				
 				if( progress != null ){
 					progress.onProgress( bytesSent / (double)bytesTotal );
 				}
 				
 				bytesAvailable = fileInputStream.available();
 				bufferSize = Math.min(bytesAvailable, maxBufferSize);
 				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
 			}
 			
 			progress.onProgress(1.0);
 
 			outputStream.writeBytes(lineEnd);
 			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
 					+ lineEnd);
 
 			// Responses from the server (code and message)
 			//int serverResponseCode = connection.getResponseCode();
 			String serverResponseMessage = connection.getResponseMessage();
 
 			Log.v("UPLOAD", "Message: " + serverResponseMessage);
 			
 			fileInputStream.close();
 			outputStream.flush();
 			outputStream.close();
 			
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Login to the Geosight Server.  Must be called before authenticated methods can be called
 	 * @param email the email address to login with
 	 * @param password the password to log in with
 	 * @return whether the login succeeded
 	 * @throws GeosightException
 	 */
 	public static User login(String email, String password) {	
 		GeosightEntity temp = new GeosightEntity();
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 		pairs.add( new BasicNameValuePair("user_session[email]", email));
 		pairs.add( new BasicNameValuePair("user_session[password]", password));
 		
 		temp.go("/user_sessions.json", Method.POST, pairs);
 		
 		boolean valid = false;
 		User user = null;
 		try{
 			valid = !temp.getBoolean("invalid_password");
 			if( valid ){
 				user = new User( temp.getObject("record") );
 			}
 		} catch (JSONException ex){
 			user = null;
 		}
 		
 		return user;
 	}
 	
 	/**
 	 * Set the underlying JSON Object / Array based on the next fetched object
 	 * @param o the object to change contexts to
 	 */
 	protected void changeContext(Object o){
 		if( o instanceof JSONObject ){
 			jObj = (JSONObject)o;
 			jArr = null;
 		} else if (o instanceof JSONArray ){
 			jArr = (JSONArray)o;
 			jObj = null;
 		}
 	}
 	
 	//=========== GETTERS =========================================================
 	
 	/**
 	 * Convert a JSONArray to an Array of GeosightEntities
 	 */
 	private List<GeosightEntity> getArray() throws GeosightException {
 		List<GeosightEntity> arr = new ArrayList<GeosightEntity>();
 		if( jArr != null ){
 			for( int i=0;i<jArr.length();i++){
 				try {
 					arr.add( new GeosightEntity( jArr.getJSONObject(i) ) );
 				} catch (JSONException e) {
 					throw new GeosightException(e);
 				}
 			}
 		}
 		return arr;
 	}
 	
 	public GeosightEntity getObject(String key) throws JSONException{
 		return new GeosightEntity( jObj.getJSONObject(key) );
 	}
 
 	public boolean getBoolean(String key) throws JSONException{
 		if( jObj == null) throw new GeosightException("Invalid JSON");
 		
 		return jObj.getBoolean(key);
 	}
 
 	public Date getDate(String key) throws java.text.ParseException, JSONException{
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
 		return df.parse( jObj.getString(key) );
 	}
 	
 	public Double getDouble(String key) throws JSONException{
 		return jObj.getDouble(key);
 	}
 
 	public GeoPoint getGeoPoint(String lat, String lon) throws JSONException{
 		return new GeoPoint( (int)(getDouble(lat) * 1000000), (int)(getDouble(lon) * 1000000) );
 	}
 
 	public int getInt(String key) throws JSONException{
 		return jObj.getInt(key);
 	}
 
 	public long getLong(String key) throws JSONException{
 		return jObj.getLong(key);
 	}
 	
 	public JSONArray getJSONArray(){
 		return jArr;
 	}
 
 	public JSONArray getJSONArray(String key) throws JSONException{
 		return jObj.getJSONArray(key);
 	}
 	
 	public JSONObject getJSONObject(String key) throws JSONException{
 		return jObj.getJSONObject(key);
 	}
 
 	public JSONObject getJSONObject(){
 		return jObj;
 	}
 	
 	public String getString(String key) throws JSONException{
 		return jObj.getString(key);
 	}
 }
