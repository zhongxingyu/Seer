 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.trentorise.smartcampus.ac.network;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import eu.trentorise.smartcampus.ac.AcServiceException;
 import eu.trentorise.smartcampus.ac.model.Attribute;
 import eu.trentorise.smartcampus.ac.model.UserData;
 
 /**
  * @author raman
  *
  */
 public class RemoteConnector {
 
     /** address of the create anonymous endpoint */
 	private static final String PATH_CREATE_ANONYMOUS = "/getToken/anonymous";
     /** address of the code validation endpoint */
 	private static final String PATH_VALIDATE = "/validateCode";
 	/** Timeout (in ms) we specify for each http request */
     public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
     /** The tag used to log to adb console. */
     private static final String TAG = "RemoteConnector";
 
 	public static UserData validateAccessCode(String service, String code) {
 
         final HttpResponse resp;
         final HttpEntity entity = null;
         Log.i(TAG, "validating code: " + code);
         final HttpPost post = new HttpPost(service + PATH_VALIDATE+"/"+code);
         post.setEntity(entity);
         post.setHeader("Accept", "application/json");
         try {
             resp = getHttpClient().execute(post);
             final String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	JSONObject json = new JSONObject(response);
             	UserData data = UserData.valueOfAccountData(json);
                 Log.v(TAG, "Successful authentication");
                 return data;
             }
             Log.e(TAG, "Error validating " + resp.getStatusLine());
             return null;
         } catch (final Exception e) {
             Log.e(TAG, "IOException when getting authtoken", e);
             return null;
         } finally {
             Log.v(TAG, "getAuthtoken completing");
         }
 	}  
 	
 	public static UserData createAnonymousUser(String service, String externalId) {
         String requestString = service + PATH_CREATE_ANONYMOUS;
         Log.i(TAG, "create anonymous user: " + externalId);
         if (externalId != null) {
         	requestString += "?externalId="+externalId;
         } else {
             Log.i(TAG, "Failed creating anonymous: external ID requred");
             return null;
         }
 		final HttpGet request = new HttpGet(requestString);
 		// HTTP parameters stores header etc.
 		final HttpParams params = new BasicHttpParams();
 		HttpClientParams.setRedirecting(params, false);
         request.setParams(params);
         request.setHeader("Accept", "application/json");
 		String accessCode = null;
         try {
 			final HttpResponse resp = getHttpClient().execute(request);
 			Header locationHeader = resp.getFirstHeader("location");
 			if (locationHeader != null && locationHeader.getValue().indexOf('#') > 0) {
 				accessCode = locationHeader.getValue().substring(locationHeader.getValue().indexOf('#')+1);
 			}
 			if ((accessCode != null) && (accessCode.length() > 0)) {
 			    Log.v(TAG, "Successfully created: "+externalId);
 			    return validateAccessCode(service, accessCode);
 			} else {
 			    Log.e(TAG, "Error creating anonymous: http code " + resp.getStatusLine());
 			    return null;
 			}
 		} catch (Exception e) {
 		    Log.e(TAG, "Error creating anonymous: " + e.getMessage());
 			return null;
 		}
 	}
 	
     private static HttpClient getHttpClient() {
         HttpClient httpClient = new DefaultHttpClient();
         final HttpParams params = httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         return httpClient;
     }
 
     public static UserData getUser(String host, String token) throws SecurityException, AcServiceException {
     	String json = getJSON(host, "/users/me", token);
     	try {
 			return UserData.valueOfServiceData(new JSONObject(json));
 		} catch (JSONException e) {
 			throw new AcServiceException(e);
 		} 
     }
 
     public static List<Attribute> getUserAttributes(String host, String authority, String key, String token) throws SecurityException, AcServiceException {
     	String json = null;
     	if (key != null && authority != null) {
     		json = getJSON(host, "/users/me/attributes/authorities/"+authority+"/keys/"+key, token);
     	} else if (authority != null) {
     		json = getJSON(host, "/users/me/attributes/authorities/"+authority, token);
     	} else {
     		json = getJSON(host, "/users/me/attributes", token);
     	}
     	
     	try {
     		JSONObject obj = new JSONObject(json);
     		JSONArray arr = obj.getJSONArray("Attribute");
     		List<Attribute> result = new ArrayList<Attribute>();
     		if (arr != null) {
     			for (int i = 0; i < arr.length(); i++) {
     				result.add(Attribute.valueOf(arr.getJSONObject(i)));
     			}
     		}
     		return result;
 		} catch (JSONException e) {
 			throw new AcServiceException(e);
 		} 
     }
 
     public static boolean isValidUser(String host, String token) throws SecurityException, AcServiceException {
     	String json = getJSON(host, "/users/me/validity", token);
 		return Boolean.valueOf(json);
     }
     public static boolean isAnonymousUser(String host, String token) throws SecurityException, AcServiceException {
     	String json = getJSON(host, "/users/me/anonymous", token);
 		return Boolean.valueOf(json);
     }
 	public static boolean canReadResource(String host, String token, String resourceId) throws SecurityException, AcServiceException {
     	String json = getJSON(host, "/resources/"+resourceId+"/access", token);
 		return Boolean.valueOf(json);
 	}
 
 	private static String getJSON(String host, String service, String token) throws SecurityException, AcServiceException {
         final HttpResponse resp;
         Log.i(TAG, "reading data: " + service);
         final HttpGet get = new HttpGet(host + service);
         get.setHeader("Accept", "application/json");
         get.setHeader("AUTH_TOKEN",token);
         try {
             resp = getHttpClient().execute(get);
             String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 return response;
             }
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                 throw new SecurityException();
             }
            throw new AcServiceException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
            Log.e(TAG, "IOException when getting authtoken", e);
             throw new AcServiceException(e.getMessage(),e);
         } finally {
            Log.v(TAG, "getAuthtoken completing");
         }
 
 	}
 
 }
