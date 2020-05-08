 package ch.adorsaz.loungeDroid.servercom;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class SessionManager {
     private String mLogin = null;
     private String mPassword = null;
     private String mServerUrl = null;
     private String mSessionCookie = null;
     private static SessionManager mSessionManager = null;
 
     private final static String LOGIN_PAGE_RSSLOUNGE = "/index/login";
     private final static String LOGIN_GET_RSSLOUNGE = "username";
     private final static String PASSWORD_GET_RSSLOUNGE = "password";
     protected final static String JSON_GET_RSSLOUNGE = "json=true";
 
     protected final static String LOG_DEBUG_LOUNGE = "loungeDroid.server :";
 
     public final static SessionManager getInstance(Context context) {
         if (mSessionManager == null) {
             mSessionManager = new SessionManager();
         }
 
         // mSessionManager.getPreferences(context);
 
         return mSessionManager;
     }
 
     public final static SessionManager getInstance(String url, String login,
             String password) {
         if (mSessionManager == null) {
             mSessionManager = new SessionManager();
         }
 
         mSessionManager.setPreferences(url, login, password);
 
         return mSessionManager;
     }
 
     private void setPreferences(String url, String login, String password) {
         mLogin = login;
         mPassword = password;
         mServerUrl = url;
     }
 
     private void loginLounge() throws AuthenticationFailLoungeException {
         try {
             String urlParameters = LOGIN_GET_RSSLOUNGE + "="
                     + URLEncoder.encode(mLogin, "UTF-8") + "&"
                     + PASSWORD_GET_RSSLOUNGE + "=" + mPassword + "&"
                     + JSON_GET_RSSLOUNGE;
 
             JSONObject jsonResponse = doRequest(LOGIN_PAGE_RSSLOUNGE,
                     urlParameters);
 
             if (jsonResponse.getBoolean("success") == true) {
                 Log.d(LOG_DEBUG_LOUNGE, "Logged to the server.");
             } else {
                 throw new AuthenticationFailLoungeException();
             }
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private SessionManager() {
     }
 
     private void getPreferences(Context context) {
         if (context != null) {
             SharedPreferences prefs = PreferenceManager
                     .getDefaultSharedPreferences(context);
 
             mServerUrl = prefs.getString("urlPref", "");
             if (!mServerUrl.startsWith("http://")
                     && !mServerUrl.startsWith("https://")) {
                 mServerUrl = "http://" + mServerUrl;
             }
 
             mLogin = Uri.encode(prefs.getString("loginPref", ""));
             mPassword = Uri.encode(prefs.getString("passwordPref", ""));
         }
     }
 
     private String streamToString(InputStream inputStream) throws IOException {
         BufferedReader br = new BufferedReader(new InputStreamReader(
                 inputStream));
 
         String result = br.readLine();
         String line = result;
 
         while (line != null) {
             result = result + "\n" + line;
             line = br.readLine();
         }
 
         return result;
     }
 
    private JSONObject doRequest(String pageUrl, String httpParameters)
        throws AuthenticationFailLoungeException {
         JSONObject jsonResponse = null;
         HttpURLConnection urlConnection = null;
 
         try {
             urlConnection = (HttpURLConnection) new URL(mServerUrl + pageUrl)
                     .openConnection();
             urlConnection.setDoOutput(true);
             urlConnection.setChunkedStreamingMode(0);
 
             if (mSessionCookie != null) {
                 urlConnection.setRequestProperty("Cookie", mSessionCookie);
             }
 
             DataOutputStream out = new DataOutputStream(
                     urlConnection.getOutputStream());
             out.writeBytes(httpParameters);
             out.flush();
             out.close();
 
             if (urlConnection.getResponseCode() == 200) {
                 if (urlConnection.getHeaderField("Set-Cookie") != null) {
                     mSessionCookie = urlConnection.getHeaderField("Set-Cookie");
                 }
 
                 InputStream responseInput = urlConnection.getInputStream();
                 jsonResponse = new JSONObject(streamToString(responseInput));
                 responseInput.close();
             } else {
                 throw new ConnectException();
             }
 
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } finally {
             urlConnection.disconnect();
         }
 
         if (jsonResponse == null) {
             Log.e(LOG_DEBUG_LOUNGE,
                     "jsonResponse is null ! Cannot access page " + pageUrl);
         }
         return jsonResponse;
     }
 
     protected JSONObject serverRequest(String pageUrl, String httpParameters)
         throws AuthenticationFailLoungeException {
         if (mSessionCookie == null) {
             loginLounge();
         }
 
         return doRequest(pageUrl, httpParameters);
     }
 }
