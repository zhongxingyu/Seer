 package com.orange.labs.plugins;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.http.HeaderIterator;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeaderIterator;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class HttpRequest extends CordovaPlugin {
 	public static final String ACTION_MAKE_REQUEST_ENTRY = "makeRequest";
 	public static final String COOKIEHEADER = "Set-Cookie";
 	JSONObject result;
 	JSONObject jsonCookie;
 	JSONObject cookiesJson;
 
 	// CallbackContext generalCallback;
 
 	@Override
 	public boolean execute(String action, JSONArray args,
 			final CallbackContext callbackContext) throws JSONException {
 		try {
 			if (ACTION_MAKE_REQUEST_ENTRY.equals(action)) {
 				result = new JSONObject();
 				// log.d("---ANDROID PLUG IN", args.toString());
 				final JSONObject arg_object = args.getJSONObject(0);
 				// final CallbackContext generalCallback = callbackContext;
 				final JSONObject responseJson = new JSONObject();
 				if (arg_object != null) {
 					Log.d("json received", arg_object.toString());
 
 					cordova.getThreadPool().execute(new Runnable() {
 						public void run() {
 							String url, type;
 							try {
 								type = arg_object.getString("type");
 								url = arg_object.getString("url");
 								Log.d("url witouth decoding", url);
 								responseJson.put("type", type);
 								HttpClient httpClient = new DefaultHttpClient();
 								HttpGet httpGet = new HttpGet(url);
 								// adding all the headers that were attached
 								if (arg_object.has("headers")) {
 									JSONObject headers = arg_object
 											.getJSONObject("headers");
 									Iterator<?> keys = headers.keys();
 									while (keys.hasNext()) {
 										String key = (String) keys.next();
 										httpGet.addHeader(key,
 												headers.getString(key));
 										Log.d("adding header " + key,
 												headers.getString(key));
 									}
 								}
 								//executing the get function
 								HttpResponse response = httpClient
 										.execute(httpGet);
 								//getting the status code
 								StatusLine statusLine = response
 										.getStatusLine();
 								// getting the body of the message
 								StringBuilder stringBuilder = new StringBuilder();
 								HttpEntity entity = response
 										.getEntity();
 								InputStream inputStream = entity
 										.getContent();
 								BufferedReader reader = new BufferedReader(
 										new InputStreamReader(
 												inputStream));
 								String line;
 								while ((line = reader.readLine()) != null) {
 									stringBuilder.append(line);
 								}
 								responseJson.put("content",
 										stringBuilder.toString());
 								
 								int statusCode = statusLine.getStatusCode();
 								responseJson.put("statusCode",
 										statusCode);
 								JSONObject cookies = getCookies(response);
 								responseJson.put("headers",
 										cookies);
 								//IF THE RESPONSE IS SUCCESSFULL
 								if (statusCode == 200) {
 									Log.d("success receiving a 200", "-- 200");
 									try {
 										responseJson.put("result", "success");
 										// getting cookies from header
 
 									} catch (JSONException e1) {
 										// TODO Auto-generated catch block
 										e1.printStackTrace();
 									}
 									Log.d("-- RETURN SUCCESS",
 											responseJson.toString());
 									callbackContext.success(responseJson); // Thread-safe.
 								} else {
 									try {
 										responseJson.put("result", "error");
 									} catch (JSONException e1) {
 										// TODO Auto-generated catch block
 										e1.printStackTrace();
 									}
 									callbackContext.error(responseJson);
 								}
 								// if there is any error reading the url then we
 								// return an error
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								try {
 									responseJson.put("result", "error");
 									responseJson.put("errorMessage",
 											e.getMessage());
 									responseJson.put("statusCode",
 											0);
 								} catch (JSONException e1) {
 									// TODO Auto-generated catch block
 									e1.printStackTrace();
 								}
 								callbackContext.error(responseJson);
 								e.printStackTrace();
 							} catch (ClientProtocolException e) {
 								// TODO Auto-generated catch block
 								Log.d("------ protocol exception", "http exception");
 								try {
 									responseJson.put("result", "error");
 									responseJson.put("errorMessage",
 											e.getMessage());
 									responseJson.put("statusCode",
 											0);
 									
 								} catch (JSONException e1) {
 									// TODO Auto-generated catch block
 									e1.printStackTrace();
 								}
 								callbackContext.error(responseJson);
 								e.printStackTrace();
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								Log.d("------ protocol exception", "IO exception");
 								try {
 									responseJson.put("result", "error");
 									responseJson.put("errorMessage",
 											e.getMessage());
 									responseJson.put("statusCode",
 											0);
 									
 								} catch (JSONException e1) {
 									// TODO Auto-generated catch block
 									e1.printStackTrace();
 								}
 								callbackContext.error(responseJson);
 								e.printStackTrace();
 							}
 							Log.d("running on the thread", "running");
 							callbackContext.success(); // Thread-safe.
 						}
 					});
 					
 				}
 
 				return true;
 			}
 			callbackContext.error("Invalid action");
 			return false;
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			callbackContext.error(e.getMessage());
 			return false;
 		}
 
 	}
 
 	private JSONObject getCookies(HttpResponse response) {
 		Map<String, String> cookies = new HashMap<String, String>();
 		
 		JSONObject headersJson = null;
 		result = new JSONObject();
 		try {
 			cookiesJson = new JSONObject();
 			headersJson = new JSONObject();
 			// cookiesJson = new J
 			
 
 			org.apache.http.Header[] headers = response.getAllHeaders();
 
 			HeaderIterator headerIterrator = new BasicHeaderIterator(headers,
 					null);
 			while (headerIterrator.hasNext()) {
 				org.apache.http.Header header = headerIterrator.nextHeader();
 				Log.d("header: " + header.getName(),
 						"header value: " + header.getValue());
 				// we store in a separate json the cookie values
 				if (header.getName().equalsIgnoreCase(COOKIEHEADER)) {
 					getCookie(header.getValue());
 				} else {
 					headersJson.accumulate(header.getName(), header.getValue());
 				}
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			result.accumulate("cookies", cookiesJson);
 			result.accumulate("headers", headersJson);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Log.d("json cookie", result.toString());
 		return result;
 		// return null;
 	}
 
 	private JSONObject getCookie(String cookie) {
 		JSONObject jsonCookie= new JSONObject();
 		JSONObject tempCookie = new JSONObject();
 		String cookieName=null;
 		String[] cookieRaw = cookie.split(";");
 		try {
 			for (int x = 0; x < cookieRaw.length; x++) {
 				String[] singleCookie = cookieRaw[x].split("=");
 				if (x == 0) {
 					cookieName = singleCookie[0];
					tempCookie.accumulate("value", singleCookie[1]);
 
 				} else {
 					tempCookie.accumulate(singleCookie[0].replace(" ", ""), singleCookie[1]);
 				}
 			}
 			cookiesJson.accumulate(cookieName, tempCookie);
 			//cookiesJson.accumulate("cookies", jsonCookie);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Log.d("returning cookie", jsonCookie.toString());
 		return jsonCookie;
 	}
 }
