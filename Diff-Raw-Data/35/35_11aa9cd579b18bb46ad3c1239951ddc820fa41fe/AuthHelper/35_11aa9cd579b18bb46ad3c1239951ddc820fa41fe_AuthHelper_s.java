 package de.fhb.mi.paperfly.auth;
 
 import android.content.Context;
 import android.util.Base64;
 import android.util.Log;
 import com.google.gson.Gson;
import de.fhb.mi.paperfly.dto.TokenDTO;
import lombok.Cleanup;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 
import java.io.*;
 
 /**
  * @author Christoph Ott
  */
 public class AuthHelper {
 
     public static final String TAG = "AuthHelper";
     public static final String URL_LOGIN_BASIC = "http://46.137.173.175:8080/PaperFlyServer-web/secure/";
     public static final String URL_LOGIN = "http://46.137.173.175:8080/PaperFlyServer-web/rest/v1/auth/login";
     public static final String URL_LOGIN_DIGEST = "http://46.137.173.175:8080/PaperFlyServer-web/rest/service/v1/login";
     public static final String URL_LOGOUT = "http://46.137.173.175:8080/PaperFlyServer-web/rest/service/v1/logout";
     public static final String URL_CHAT_GLOBAL = "ws://46.137.173.175:8080/PaperFlyServer-web/ws/chat/global";
     public static final String FILE_NAME = "secure";
 
     public static boolean logout() throws IOException {
         HttpUriRequest request = new HttpGet(URL_LOGOUT); // Or HttpPost(), depends on your needs
 
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response = httpclient.execute(request);
 
         if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
             Log.d(TAG, "Logout successful");
             return true;
         } else {
             Log.d(TAG, "Logout not successful");
             return false;
         }
     }
 
     /**
     * @param mail     the user´s mail address
     * @param password the user´s password
      * @return a TokenDTO if login successful, null if not
      * @throws IOException
      */
     public static TokenDTO login(String mail, String password) throws IOException {
         HttpUriRequest request = new HttpGet(URL_LOGIN); // Or HttpPost(), depends on your needs
         request.addHeader("user", mail);
         request.addHeader("pw", password);
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response = httpclient.execute(request);
 
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
             Log.d(TAG, response.getStatusLine().getStatusCode() + "");
             return null;
             // TODO switch mit status code
         }
 
         InputStream is = response.getEntity().getContent();
         BufferedReader rd = new BufferedReader(new InputStreamReader(is));
         String line;
         StringBuilder responseObj = new StringBuilder();
         while ((line = rd.readLine()) != null) {
             responseObj.append(line);
             responseObj.append('\r');
         }
         rd.close();
         Log.d(TAG, responseObj.toString());
         Gson gson = new Gson();
         return gson.fromJson(responseObj.toString(), TokenDTO.class);
     }
 
     private static boolean authenticate(Context context, String encodedCredentials) throws IOException {
         Log.d(TAG, "authenticate with server");
         HttpUriRequest request = new HttpGet(URL_LOGIN_BASIC); // Or HttpPost(), depends on your needs
         request.addHeader("Authorization", "Basic " + encodedCredentials);
 
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response = httpclient.execute(request);
         Log.d(TAG, response.getStatusLine().getStatusCode() + "");
 
         if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             String FILENAME = FILE_NAME;
 
             Log.d(TAG, "write password to file");
             @Cleanup FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
             fos.write(encodedCredentials.getBytes());
             return true;
         }
         return false;
     }
 
     public static boolean authenticate(Context context, String mail, String password) throws IOException {
         String credentials = mail + ":" + password;
         String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
         return authenticate(context, base64EncodedCredentials);
 
 //        try {
 //            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
 //            messageDigest.update(password.getBytes());
 //            byte[] byteData = messageDigest.digest();
 //            StringBuffer sb = new StringBuffer();
 //
 //            for (int i = 0; i < byteData.length; i++) {
 //                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
 //            }
 //            return authDigst(context, mail, sb.toString());
 //        } catch (NoSuchAlgorithmException e) {
 //            e.printStackTrace();
 //        }
 //        return false;
     }
 
     private static boolean authDigst(Context context, String mail, String hashPw) {
         HttpUriRequest request = new HttpPost(URL_LOGIN_DIGEST);
         return true;
     }
 
     /**
      * Authenticates the user, if he was logged at anytime.
      *
      * @param context the activity context
      * @return true if the login was successful, false otherwise
      */
     public static boolean authenticate(Context context) {
         Log.d(TAG, "authenticate file");
         try {
             @Cleanup FileInputStream fileInputStream = context.openFileInput(AuthHelper.FILE_NAME);
 
             int content;
             StringBuilder sb = new StringBuilder();
             while ((content = fileInputStream.read()) != -1) {
                 sb.append((char) content);
             }
             return authenticate(context, sb.toString());
         } catch (FileNotFoundException e) {
             Log.d(TAG, "FileNotFound", e);
         } catch (IOException e) {
             Log.d(TAG, "IOException", e);
         }
         return false;
     }
 
 }
