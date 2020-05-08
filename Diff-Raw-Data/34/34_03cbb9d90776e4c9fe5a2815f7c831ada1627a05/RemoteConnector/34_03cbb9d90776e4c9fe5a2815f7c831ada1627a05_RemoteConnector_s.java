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
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 
 import android.util.Log;
 import eu.trentorise.smartcampus.ac.AACException;
 import eu.trentorise.smartcampus.ac.model.TokenData;
 
 /**
  * @author raman
  *
  */
 public class RemoteConnector {
 
     /** address of the code validation endpoint */
 	private static final String PATH_TOKEN = "oauth/token";
     /** address of the code revocation endpoint */
 	private static final String REVOKE_TOKEN = "eauth/revoke";
 	/** Timeout (in ms) we specify for each http request */
     public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
     /** The tag used to log to adb console. */
     private static final String TAG = "RemoteConnector";
 
 	public static TokenData validateAccessCode(String service, String code, String clientId, String clientSecret, String scope, String redirectUri) throws AACException {
         final HttpResponse resp;
         final HttpEntity entity = null;
         Log.i(TAG, "validating code: " + code);
        String url = service + PATH_TOKEN+"?grant_type=authorization_code&code="+code+"&client_id="+clientId +"&client_secret="+clientSecret+"&redirect_uri="+redirectUri;
        if (scope != null) url+= "&scope="+scope;
        final HttpPost post = new HttpPost(url);
        post.setEntity(entity);
         post.setHeader("Accept", "application/json");
         try {
             resp = getHttpClient().execute(post);
             final String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	TokenData data = TokenData.valueOf(response);
                 Log.v(TAG, "Successful authentication");
                 return data;
             }
             Log.e(TAG, "Error validating " + resp.getStatusLine());
             throw new AACException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
             Log.e(TAG, "Exception when getting authtoken", e);
             throw new AACException(e);
         } finally {
             Log.v(TAG, "getAuthtoken completing");
         }
 	}  
 	
     private static HttpClient getHttpClient() {
         HttpClient httpClient = new DefaultHttpClient();
         final HttpParams params = httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         
         // return httpClient;
         return new HttpsClientBuilder().getNewHttpClient(params);
     }
 
 	/**
 	 * @param string
 	 * @param refresh
 	 * @param clientId
 	 * @param clientSecret
 	 * @throws AACException 
 	 */
 	public static TokenData refreshToken(String service, String refresh, String clientId, String clientSecret) throws AACException {
         final HttpResponse resp;
         final HttpEntity entity = null;
         Log.i(TAG, "refreshing token: " + refresh);
         String url = service + PATH_TOKEN+"?grant_type=refresh_token&refresh_token="+refresh+"&client_id="+clientId +"&client_secret="+clientSecret;
         final HttpPost post = new HttpPost(url);
         post.setEntity(entity);
         post.setHeader("Accept", "application/json");
         try {
             resp = getHttpClient().execute(post);
             final String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	TokenData data = TokenData.valueOf(response);
                 Log.v(TAG, "Successful authentication");
                 return data;
             }
             Log.e(TAG, "Error validating " + resp.getStatusLine());
             Log.e(TAG, "Error validating " + resp.getStatusLine());
             throw new AACException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
             Log.e(TAG, "Exception when getting authtoken", e);
             throw new AACException(e);
         } finally {
             Log.v(TAG, "refresh token completing");
         }
 	}
 
 	/**
 	 * @param clientId
 	 * @param clientSecret
 	 * @param token
 	 * @throws AACException 
 	 */
 	public static void revokeToken(String service, String token) throws AACException {
         final HttpResponse resp;
         Log.i(TAG, "revoke token: " + token);
         String url = service + REVOKE_TOKEN+"/"+token;
         final HttpGet get = new HttpGet(url);
         try {
             resp = getHttpClient().execute(get);
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 Log.v(TAG, "Successful revoke");
                 return;
             }
             Log.e(TAG, "Error validating " + resp.getStatusLine());
             throw new AACException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
             Log.e(TAG, "Exception when getting authtoken", e);
             throw new AACException(e);
         } finally {
             Log.v(TAG, "getAuthtoken completing");
         }
 	}
 }
