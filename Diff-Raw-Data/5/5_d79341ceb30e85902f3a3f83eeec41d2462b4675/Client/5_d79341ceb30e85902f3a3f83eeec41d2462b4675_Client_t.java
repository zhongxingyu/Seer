 package io.msgs.v2;
 
 import io.msgs.v2.entity.Endpoint;
 import io.msgs.v2.entity.User;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import ch.boye.httpclientandroidlib.Header;
 import ch.boye.httpclientandroidlib.NameValuePair;
 import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
 import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
 import ch.boye.httpclientandroidlib.message.BasicHeader;
 import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
 
 import com.egeniq.utils.api.APIException;
 
 /**
  * Msgs client.
  * 
  * All methods are executed synchronously. You are responsible for <br>
  * wrapping the calls in an AsyncTask or something similar.
  */
 public class Client {
     private final static String TAG = Client.class.getSimpleName();
     private final static boolean DEBUG = true; // BuildConfig.DEBUG;
 
     private final String _baseURL;
     private final String _apiKey;
 
     private APIClient _apiClient;
 
     private static class APIClient extends com.egeniq.utils.api.APIClient {
         public APIClient(String baseURL) {
             super(baseURL);
             _setLoggingEnabled(DEBUG);
             _setLoggingTag(getClass().getName());
         }
     }
 
     /**
      * Constructor.
      * 
      * @param context
      * @param baseURL
      * @param apiKey
      */
     public Client(String baseURL, String apiKey) {
         _baseURL = baseURL;
         _apiKey = apiKey;
     }
 
     /**
      * Returns the API client.
      * 
      * @return API client.
      */
     protected APIClient _getAPIClient() {
         if (_apiClient == null) {
             _apiClient = new APIClient(_baseURL);
         }
 
         return _apiClient;
     }
 
     /**
      * Register endpoint.
      * 
      * @param properties
      * 
      * @return Endpoint.
      * 
      * @throws APIException
      */
     public Endpoint registerEndpoint(JSONObject data) throws APIException {
         try {
             JSONObject object = _post("endpoints", _getParams(data));
             return new Endpoint(object);
         } catch (Exception e) {
             if (DEBUG) {
                 Log.e(TAG, "Error registering endpoint", e);
             }
 
             if (!(e instanceof APIException)) {
                 e = new APIException(e);
             }
 
             throw (APIException)e;
         }
     }
 
     /**
      * Register user.
      * 
      * @param externalUserId
      * 
      * @return User.
      * 
      * @throws APIException
      */
     public User registerUser(JSONObject data) throws APIException {
         try {
             JSONObject object = _post("users", _getParams(data));
             return new User(object);
         } catch (Exception e) {
             if (DEBUG) {
                 Log.e(TAG, "Error registering user", e);
             }
 
             if (!(e instanceof APIException)) {
                 e = new APIException(e);
             }
 
             throw (APIException)e;
         }
     }
 
     /**
      * User helper.
      * 
      * @param token User token.
      */
     public UserRequestHelper forUser(String token) {
         return new UserRequestHelper(this, token);
     }
 
     /**
      * Endpoint helper.
      * 
      * @param token Endpint token.
      */
     public EndpointRequestHelper forEndpoint(String token) {
         return new EndpointRequestHelper(this, token);
     }
 
     /**
      * Get Api Header
      */
     private Header _getApiHeader() {
         return new BasicHeader("X-MsgsIo-APIKey", _apiKey);
     }
 
     /**
      * Convert JSON object to name value pairs.
      * 
      * @param properties
      * 
      * @return Name value pairs.
      */
     protected List<NameValuePair> _getParams(JSONObject data) {
         List<NameValuePair> params = new ArrayList<NameValuePair>();
 
         Iterator<?> iter = data.keys();
         while (iter.hasNext()) {
             try {
                 String key = (String)iter.next();
                 Object value = data.get(key);
                 if (value != null) {
                     if (value instanceof Boolean) {
                         value = ((Boolean)value).booleanValue() ? 1 : 0;
                     }
 
                     params.add(new BasicNameValuePair(key, String.valueOf(value)));
                 }
             } catch (JSONException e) {
             }
         }
 
         return params;
     }
 
     /**
      * Perform a GET request with the ApiKey header.
      */
     protected JSONObject _get(String path, List<NameValuePair> params) throws APIException {
        return _getAPIClient().get(path + (params != null && !params.isEmpty() ? "?" + URLEncodedUtils.format(params, "utf-8") : ""), true, new Header[] { _getApiHeader() });
     }
 
     /**
      * Perform a POST request with the ApiKey header.
      */
     protected JSONObject _post(String path, List<NameValuePair> params) throws APIException {
         try {
            return _getAPIClient().post(path, params == null ? null : new UrlEncodedFormEntity(params, "utf-8"), true, new Header[] { _getApiHeader() });
         } catch (UnsupportedEncodingException e) {
             return null;
         }
     }
 
     /**
      * Perform a DELETE request with the ApiKey header.
      */
     protected JSONObject _delete(String path) throws APIException {
         return _getAPIClient().delete(path, true, new Header[] { _getApiHeader() });
     }
 }
