 package fi.spanasenko.android.instagram;
 
 import android.content.Context;
 import android.util.Log;
 import com.google.gson.Gson;
 import fi.spanasenko.android.model.Location;
 import fi.spanasenko.android.model.Media;
 import fi.spanasenko.android.model.User;
 import fi.spanasenko.android.utils.UserSettings;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Locale;
 
 /**
  * @author Thiago Locatelli <thiago.locatelli@gmail.com>
  * @author Lorensius W. L T <lorenz@londatiga.net>
  */
 public class InstagramApi {
 
     private static final String TAG = InstagramApi.class.getSimpleName();
 
     private static InstagramApi _instance;
 
     private UserSettings mSession;
 
     private String mAuthUrl;
     private String mAccessToken;
 
     // Instagram API endpoints
     private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
     private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
     private static final String API_URL = "https://api.instagram.com/v1";
     private static final String LOCATIONS_ENDPOINT_FORMAT = "/locations/search?lat=%f&lng=%f&access_token=%s";
     private static final String MEDIA_ENDPOINT_FORMAT = "/locations/%s/media/recent?access_token=%s";
 
     // Instagram API keys
     public static final String CLIENT_ID = "ab11cf78383844adba1c73ce9363f2de";
     public static final String CLIENT_SECRET = "8350a30c3cfe47fea5e0dd6248ffa89f";
     public static final String CALLBACK_URL = "instagram://connection";
 
     /**
      * Default constructor.
      * @param context Parent context.
      */
     private InstagramApi(Context context) {
         mSession = UserSettings.getInstance(context);
         mAccessToken = mSession.getAccessToken();
 
         mAuthUrl = AUTH_URL + "?client_id=" + CLIENT_ID + "&redirect_uri="
                 + CALLBACK_URL + "&response_type=code&display=touch&scope=likes+comments+relationships";
     }
 
     /**
      * Returns an instance of InstagramApi class or creates if not created.
      * @param context Parent context.
      * @return Instance of InstagramApi
      */
     public static InstagramApi getInstance(Context context) {
         if (_instance == null) {
             _instance = new InstagramApi(context);
         }
 
         return _instance;
     }
 
     /**
      * Logs user out by wiping access token and other user data data.
      */
     public void logout() {
         if (mAccessToken != null) {
             mSession.resetAccessToken();
             mAccessToken = null;
         }
     }
 
     /**
      * Returns whether the instance has access token or not.
      * @return True if there is a valid access token or false if not.
      */
     public boolean hasAccessToken() {
         return (mAccessToken == null) ? false : true;
     }
 
     /**
      * Requests authorization from Instagram. Shows authorization dialog and requests access token.
      * @param context  Parent context used for showing dialog. Must be relevant Activity.
      * @param callback Callback object responsible for handling completion or error.
      */
     public void authorize(Context context, final VoidOperationCallback callback) {
         InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
             @Override
             public void onComplete(String code) {
                 getAccessToken(code, new OperationCallback<User>() {
                     @Override
                     protected void onCompleted(User result) {
                         // Store access token and user data
                         mSession.storeAccessToken(result.getAccessToken(), result.getId(), result.getUsername(),
                                 result.getFullName());
 
                         // Notify caller application
                         callback.notifyCompleted();
                     }
 
                     @Override
                     protected void onError(Exception error) {
                         callback.notifyError(error);
                     }
                 });
             }
 
             @Override
             public void onError(String error) {
                 callback.notifyError(new Exception(error));
             }
         };
 
         InstagramDialog dialog = new InstagramDialog(context, mAuthUrl, listener);
         dialog.show();
     }
 
     /**
      * Returns callback URL.
      * @return Returns callback url.
      */
     protected String getCallbackUrl() {
         return CALLBACK_URL;
     }
 
     /**
      * Requests access token for given code.
      * @param code     Code to be used for access token request.
      * @param callback Callback object responsible for handling completion or error.
      */
     private void getAccessToken(final String code, final OperationCallback<User> callback) {
 
         new Thread() {
             @Override
             public void run() {
                 Log.i(TAG, "Getting access token");
                 try {
                     String postData = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
                             "&grant_type=authorization_code" + "&redirect_uri=" + CALLBACK_URL + "&code=" + code;
                     String response = postRequest(TOKEN_URL, postData);
 
                     Log.i(TAG, "response " + response);
                     JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
 
                     mAccessToken = jsonObj.getString("access_token");
                     Log.i(TAG, "Got access token: " + mAccessToken);
 
                     Gson gson = new Gson();
                     User user = gson.fromJson(jsonObj.getJSONObject("user").toString(), User.class);
                     user.setAccessToken(mAccessToken);
 
                     callback.notifyCompleted(user);
                 } catch (Exception ex) {
                     callback.notifyError(ex);
                     Log.e(TAG, "Error getting access token", ex);
                 }
 
             }
         }.start();
     }
 
     /**
      * Requests from Instagram API nearby locations for given latitude and longitude values.
      * @param latitude  Latitude.
      * @param longitude Longitude.
      * @param callback  Callback object responsible for handling server response.
      */
     public void fetchNearbyLocations(final float latitude, final float longitude,
             final OperationCallback<Location[]> callback) {
 
         new Thread() {
             @Override
             public void run() {
                 Log.i(TAG, "Getting access token");
                 try {
                     // Prepare request.
                    String getParams = String.format(Locale.ENGLISH, LOCATIONS_ENDPOINT_FORMAT,
                            latitude, longitude, mAccessToken);
                     String url = API_URL + getParams;
                     String response = getRequest(url);
 
                     Log.i(TAG, "response " + response);
 
                     // Parse JSON answer and notify caller.
                     Gson gson = new Gson();
                     JSONObject responseJson = (JSONObject) new JSONTokener(response).nextValue();
                     List<Location> locations = new ArrayList<Location>(3);
                     JSONArray locationsJson = responseJson.getJSONArray("data");
                     for (int i = 0; i < locationsJson.length(); i++) {
                         locations.add(gson.fromJson(locationsJson.get(i).toString(), Location.class));
                     }
 
                     callback.notifyCompleted(locations.toArray(new Location[locations.size()]));
                 } catch (Exception ex) {
                     callback.notifyError(ex);
                     Log.e(TAG, "Error getting access token", ex);
                 }
 
             }
         }.start();
     }
 
     /**
      * Requests from Instagram API recent media for given location id.
      * @param locationId Id of the location to search for media.
      * @param callback   Callback object responsible for handling server response.
      */
     public void fetchRecentMedia(final String locationId, final OperationCallback<Media[]> callback) {
 
         new Thread() {
             @Override
             public void run() {
                 Log.i(TAG, "Getting access token");
                 try {
                     // Form request.
                     String getParams = String.format(MEDIA_ENDPOINT_FORMAT, locationId, mAccessToken);
                     String url = API_URL + getParams;
                     String response = getRequest(url);
                     Log.i(TAG, "response " + response);
 
                     // Parse JSON data
                     Gson gson = new Gson();
                     JSONObject responseJson = (JSONObject) new JSONTokener(response).nextValue();
                     List<Media> media = new ArrayList<Media>();
                     JSONArray mediaJson = responseJson.getJSONArray("data");
                     for (int i = 0; i < mediaJson.length(); i++) {
                         media.add(gson.fromJson(mediaJson.get(i).toString(), Media.class));
                     }
 
                     callback.notifyCompleted(media.toArray(new Media[media.size()]));
                 } catch (Exception ex) {
                     callback.notifyError(ex);
                     Log.e(TAG, "Error getting access token", ex);
                 }
 
             }
         }.start();
     }
 
     /**
      * Reads stream to string.
      * @param is InpurStream to read data from.
      * @return String with data from given stream.
      * @throws IOException
      */
     private String streamToString(InputStream is) throws IOException {
         String str = "";
 
         if (is != null) {
             StringBuilder sb = new StringBuilder();
             String line;
 
             try {
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is));
 
                 while ((line = reader.readLine()) != null) {
                     sb.append(line);
                 }
 
                 reader.close();
             } finally {
                 is.close();
             }
 
             str = sb.toString();
         }
 
         return str;
     }
 
     /**
      * Issues GET request to the given url and returns response.
      * @param requestUrl Api endpoint with parameters.
      * @return Server response.
      * @throws IOException
      */
     private String getRequest(String requestUrl) throws IOException {
         URL url = new URL(requestUrl);
 
         Log.d(TAG, "Opening URL " + url.toString());
         HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.setRequestMethod("GET");
         urlConnection.setDoInput(true);
         urlConnection.connect();
         String response = streamToString(urlConnection.getInputStream());
 
         return response;
     }
 
     /**
      * Issues POST request to the given url and writes given data from string.
      * @param requestUrl Api endpoint for POST request.
      * @param postData   Data to be posted.
      * @return Server response.
      * @throws IOException
      */
     private String postRequest(String requestUrl, String postData) throws IOException {
         URL url = new URL(requestUrl);
 
         Log.i(TAG, "Opening Token URL " + url.toString());
         HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.setRequestMethod("POST");
         urlConnection.setDoInput(true);
         urlConnection.setDoOutput(true);
         OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
         writer.write(postData);
         writer.flush();
         String response = streamToString(urlConnection.getInputStream());
 
         return response;
     }
 
 }
