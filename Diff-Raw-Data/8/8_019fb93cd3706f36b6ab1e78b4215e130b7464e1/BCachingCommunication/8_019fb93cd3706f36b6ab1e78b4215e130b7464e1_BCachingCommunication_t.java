 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.bcaching.communication;
 
 import com.google.code.geobeagle.bcaching.BCachingModule;
 import com.google.inject.Inject;
 
 import android.content.SharedPreferences;
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.DigestException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 /**
  * Communicates with the bacching.com server to fetch geocaches as a GPX
  * InputStream.
  * 
  * @author Mark Bastian
  */
 public class BCachingCommunication {
     private static char[] map1 = new char[64];
     static {
         int i = 0;
         for (char c = 'A'; c <= 'Z'; c++) {
             map1[i++] = c;
         }
         for (char c = 'a'; c <= 'z'; c++) {
             map1[i++] = c;
         }
         for (char c = '0'; c <= '9'; c++) {
             map1[i++] = c;
         }
         map1[i++] = '+';
         map1[i++] = '/';
     }
 
     public static String base64Encode(byte[] in) {
         int iLen = in.length;
         int oDataLen = (iLen * 4 + 2) / 3;// output length without padding
         int oLen = ((iLen + 2) / 3) * 4;// output length including padding
         char[] out = new char[oLen];
         int ip = 0;
         int op = 0;
         int i0, i1, i2, o0, o1, o2, o3;
         while (ip < iLen) {
             i0 = in[ip++] & 0xff;
             i1 = ip < iLen ? in[ip++] & 0xff : 0;
             i2 = ip < iLen ? in[ip++] & 0xff : 0;
             o0 = i0 >>> 2;
             o1 = ((i0 & 3) << 4) | (i1 >>> 4);
             o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
             o3 = i2 & 0x3F;
             out[op++] = map1[o0];
             out[op++] = map1[o1];
             out[op] = op < oDataLen ? map1[o2] : '=';
             op++;
             out[op] = op < oDataLen ? map1[o3] : '=';
             op++;
         }
         return new String(out);
     }
 
     private final String baseUrl = "http://www.bcaching.com/api";
     private final String hashword;
     private final int timeout = 60000; // millisec
     private final String username;
 
     public static class BCachingCredentials {
         public final String password;
         public final String username;
 
         @Inject
         public BCachingCredentials(SharedPreferences sharedPreferences) {
             password = sharedPreferences.getString(BCachingModule.BCACHING_PASSWORD, "");
             username = sharedPreferences.getString(BCachingModule.BCACHING_USERNAME, "");
         }
     }
     
     @Inject
     public BCachingCommunication(BCachingCredentials bcachingCredentials) {
         username = bcachingCredentials.username;
         String hashword = "";
         try {
             hashword = encodeHashword(username, bcachingCredentials.password);
         } catch (Exception ex) {
             Log.e("GeoBeagle", ex.toString());
         }
         this.hashword = hashword;
     }
 
     public String encodeHashword(String username, String password) {
         return encodeMd5Base64(password + username);
     }
 
     public String encodeMd5Base64(String s) {
         byte[] buf = s.getBytes();
         try {
             java.security.MessageDigest md;
             md = java.security.MessageDigest.getInstance("MD5");
             md.update(buf, 0, buf.length);
             buf = new byte[16];
             md.digest(buf, 0, buf.length);
         } catch (DigestException e) {
             // Should never happen.
             Log.d("GeoBeagle", "Digest exception encoding md5");
             throw new RuntimeException(e);
         } catch (NoSuchAlgorithmException e) {
             // Should never happen.
             Log.d("GeoBeagle", "NoSuchAlgorithmException exception encoding md5");
             throw new RuntimeException(e);
         }
         return base64Encode(buf);
     }
 
     public InputStream sendRequest(Hashtable<String, String> params) throws BCachingException {
         if (params == null || params.size() == 0)
             throw new IllegalArgumentException("params are required.");
         if (!params.containsKey("a"))
             throw new IllegalArgumentException("params must include an action (key=a)");
 
         StringBuffer sb = new StringBuffer();
         Enumeration<String> keys = params.keys();
         while (keys.hasMoreElements()) {
             if (sb.length() > 0)
                 sb.append('&');
             String k = keys.nextElement();
             sb.append(k);
             sb.append('=');
             sb.append(URLEncoder.encode(params.get(k)));
         }
 
         return sendRequest(sb.toString());
     }
 
     private String encodeQueryString(String username, String hashword, String params) {
         if (username == null)
             throw new IllegalArgumentException("username is required.");
         if (hashword == null)
             throw new IllegalArgumentException("hashword is required.");
         if (params == null || params.length() == 0)
             throw new IllegalArgumentException("params are required.");
 
         StringBuffer sb = new StringBuffer();
         sb.append("u=");
         sb.append(URLEncoder.encode(username));
         sb.append("&");
         sb.append(params);
         sb.append("&time=");
         java.util.Date date = java.util.Calendar.getInstance().getTime();
         sb.append(date.getTime());
         String signature = encodeMd5Base64(sb.toString() + hashword);
         sb.append("&sig=");
         sb.append(URLEncoder.encode(signature));
         return sb.toString();
     }
 
     private URL getURL(String username, String hashword, String params) {
         try {
             return new URL(baseUrl + "/q.ashx?" + encodeQueryString(username, hashword, params));
         } catch (MalformedURLException e) {
             // baseUrl is a constant, it should never be malformed.
             throw new RuntimeException(e);
         }
     }
 
     private InputStream sendRequest(String query) throws BCachingException {
         if (query == null || query.length() == 0)
             throw new IllegalArgumentException("query is required");
 
         final URL url = getURL(username, hashword, query);
         Log.d("GeoBeagle", "sending url: " + url);
         HttpURLConnection connection;
         try {
             connection = (HttpURLConnection)url.openConnection();
             connection.setReadTimeout(timeout);
             connection.setConnectTimeout(timeout);
             connection.addRequestProperty("Accept-encoding", "gzip");
             int responseCode = connection.getResponseCode();
             InputStream inputStream = null;
             try {
                 inputStream = connection.getInputStream();
             } catch (IOException e) {
                 InputStream errorStream = connection.getErrorStream();
                if (connection.getContentEncoding() != null
                        && connection.getContentEncoding().equalsIgnoreCase("gzip")) {
                     inputStream = new java.util.zip.GZIPInputStream(errorStream);
                 }
                InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                BufferedReader reader = new BufferedReader(errorStreamReader);
                 String string = connection.getResponseCode() + " error: " + reader.readLine();
                 throw new BCachingException(string);
             }
             if (connection.getContentEncoding().equalsIgnoreCase("gzip")) {
                 inputStream = new java.util.zip.GZIPInputStream(inputStream);
             }
             if (responseCode != HttpURLConnection.HTTP_OK) {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 StringBuilder sb = new StringBuilder();
                 String line = null;
                 while ((line = reader.readLine()) != null) {
                     sb.append(line + "\n");
                 }
                 throw new BCachingException("HTTP problem: " + sb.toString());
             }
             return inputStream;
         } catch (IOException e) {
             throw new BCachingException("IO error: " + e.toString());
         }
     }
 }
