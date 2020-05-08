 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.aac;
 
 import java.net.URLEncoder;
 
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
import org.springframework.util.StringUtils;
 
 import eu.trentorise.smartcampus.aac.model.TokenData;
 
 /**
  * Class used to connect with the AAC service.
  * 
  */
 public class AACService {
 
 	private String aacURL;
 	private String clientId;
 	private String clientSecret;
 
     /** address of the code validation endpoint */
 	private static final String PATH_TOKEN = "oauth/token";
     /** address of the authorization endpoint */
 	private static final String AUTH_PATH = "eauth/authorize";
     /** resource access validation endpoint */
 	private static final String RESOURCE_TOKEN = "resources/access?scope=%s";
 	/** Timeout (in ms) we specify for each http request */
     public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
 
     /**
      * @param aacURL URL of the AAC service
      * @param clientId 
      * @param clientSecret
      */
 	public AACService(String aacURL, String clientId, String clientSecret) {
 		super();
 		this.aacURL = aacURL;
 		if (!aacURL.endsWith("/")) this.aacURL += '/';
 		this.clientId = clientId;
 		this.clientSecret = clientSecret;
 	}
 
     /**
      * The method generates an authorization URL for SC given the parameters
      * @param redirectUri redirect URI to redirect the authorization code to
      * @param authority if specified, specific IdP authority to call for authentication
      * @param scope call scope (may be null)
      * @param state state var (recommended but may be null)
      * @param loginAuthorities autohrities to show on the login page (may be null)
      * @return
      */
     public String generateAuthorizationURIForCodeFlow(String redirectUri, String authority, String scope, String state, String[] loginAuthorities) {
     	return generateAuthorizationURI(redirectUri, authority, scope, state, loginAuthorities);
     }
 
     /**
      * The method generates an authorization URL for SC given the parameters
      * @param redirectUri redirect URI to redirect the authorization code to
      * @param authority if specified, specific IdP authority to call for authentication
      * @param scope call scope (may be null)
      * @param state state var (recommended but may be null)
      * @return
      */
     public String generateAuthorizationURIForCodeFlow(String redirectUri, String authority, String scope, String state) {
     	return generateAuthorizationURI(redirectUri, authority, scope, state, null);
     }
 
 	private String generateAuthorizationURI(String redirectUri,
 			String authority, String scope, String state,
 			String[] loginAuthorities) {
 		String s = aacURL + AUTH_PATH;
     	if (authority != null && !authority.trim().isEmpty()) {
     		s += authority;
     	}
     	s += "?client_id="+clientId+"&response_type=code&redirect_uri="+redirectUri;
     	if (scope != null && !scope.trim().isEmpty()) {
     		s += "&scope="+scope;
     	}
     	if (state != null && !state.trim().isEmpty()) {
     		s += "&state="+state;
     	}
     	if (loginAuthorities != null) {
    		s += "&authorities="+StringUtils.arrayToCommaDelimitedString(loginAuthorities);
     	}
     	return s;
 	}
     
 
 	/**
 	 * Exchange the access code for the refresh token
 	 * @param code
 	 * @param redirectUri
 	 * @return
 	 * @throws SecurityException
 	 * @throws AACException
 	 */
 	public TokenData exchngeCodeForToken(String code, String redirectUri) throws SecurityException, AACException {
         final HttpResponse resp;
         final HttpEntity entity = null;
         String url = aacURL + PATH_TOKEN+"?grant_type=authorization_code&code="+code+"&client_id="+clientId +"&client_secret="+clientSecret+"&redirect_uri="+redirectUri;
         final HttpPost post = new HttpPost(url);
         post.setEntity(entity);
         post.setHeader("Accept", "application/json");
         try {
             resp = getHttpClient().execute(post);
             final String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	TokenData data = TokenData.valueOf(response);
                 return data;
             }
             throw new AACException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
             throw new AACException(e);
         }
 	}
 
 	/**
 	 * Generate client access token for the current application.
 	 * @return
 	 * @throws AACException
 	 */
 	public TokenData generateClientToken() throws AACException {
         final HttpResponse resp;
         final HttpEntity entity = null;
         String url = aacURL + PATH_TOKEN+"?grant_type=client_credentials&client_id="+clientId +"&client_secret="+clientSecret;
         final HttpPost post = new HttpPost(url);
         post.setEntity(entity);
         post.setHeader("Accept", "application/json");
         try {
             resp = getHttpClient().execute(post);
             final String response = EntityUtils.toString(resp.getEntity());
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	TokenData data = TokenData.valueOf(response);
                 return data;
             }
             throw new AACException("Error validating " + resp.getStatusLine());
         } catch (final Exception e) {
             throw new AACException(e);
         }
 	}
 	
 	/**
 	 * Refresh the user access token
 	 * 
 	 * @param token
 	 *            a user refresh token
 	 * @return a basic profile
 	 * @throws AACException
 	 */
 	public TokenData refreshToken(String token) throws SecurityException, AACException {
 		try {
 	        final HttpResponse resp;
 	        final HttpEntity entity = null;
 	        String url = aacURL + PATH_TOKEN+"?grant_type=refresh_token&refresh_token="+token+"&client_id="+clientId +"&client_secret="+clientSecret;
 	        final HttpPost post = new HttpPost(url);
 	        post.setEntity(entity);
 	        post.setHeader("Accept", "application/json");
 	        try {
 	            resp = getHttpClient().execute(post);
 	            final String response = EntityUtils.toString(resp.getEntity());
 	            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	            	TokenData data = TokenData.valueOf(response);
 	                return data;
 	            }
 	            throw new AACException("Error validating " + resp.getStatusLine());
 	        } catch (final Exception e) {
 	            throw new AACException(e);
 	        }
 		} catch (Exception e) {
 			throw new AACException(e);
 		}
 	}
 
 	/**
 	 * Check whether the specified token is applicable for the given
 	 * scope
 	 * @param token
 	 * @param scope comma-separated list of scope values
 	 * @return true if the user/client associated with the token has granted the required permissions
 	 * @throws AACException 
 	 */
 	public boolean isTokenApplicable(String token, String scope) throws AACException {
 		try {
 			String sentToken = token;
 			if (!token.toLowerCase().startsWith("bearer")) {
 				sentToken = "Bearer "+token;
 			}
 
 	        final HttpResponse resp;
 	        String url = aacURL + String.format(RESOURCE_TOKEN, URLEncoder.encode(scope,"utf8"));
 	        final HttpGet get = new HttpGet(url);
 	        get.setHeader("Accept", "application/json");
 	        get.setHeader("Authorization", sentToken);
 	        try {
 	            resp = getHttpClient().execute(get);
 	            final String response = EntityUtils.toString(resp.getEntity());
 	            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	            	return Boolean.parseBoolean(response);
 	            }
 	            throw new AACException("Error validating resource " + resp.getStatusLine());
 	        } catch (final Exception e) {
 	            throw new AACException(e);
 	        }
 		} catch (Exception e) {
 			throw new AACException(e);
 		}
 	}
 
 	protected static HttpClient getHttpClient() {
 		HttpClient httpClient = new DefaultHttpClient();
 		final HttpParams params = httpClient.getParams();
 		HttpConnectionParams.setConnectionTimeout(params,
 				HTTP_REQUEST_TIMEOUT_MS);
 		HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 		ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 		return httpClient;
 	}
 
 	
 }
