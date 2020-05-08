 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se.kudomessage.hustler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Utils {
     public static Map<String, String> getUserInfo (String accessToken) {
         try {
            String url = "http://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + accessToken;
             String result = getContentOfURL(url);
             
             JSONObject info = new JSONObject(result);
             
             Map<String, String> m = new HashMap<String, String>();
             m.put("email", info.getString("email"));
            m.put("userID", DigestUtils.sha1Hex(info.getString("email")));
             
             return m;
         } catch (JSONException ex) {
             return null;
         }
     }
 
     private static String getContentOfURL(String url) {
         try {
             InputStream is = new URL(url).openStream();
             Scanner scanner = new Scanner(is, "UTF-8");
             
             String result = scanner.useDelimiter("\\A").next();
             
             scanner.close();
             is.close();
             
             return result;
         } catch (IOException e) {}
         
         return "";
     }
 }
