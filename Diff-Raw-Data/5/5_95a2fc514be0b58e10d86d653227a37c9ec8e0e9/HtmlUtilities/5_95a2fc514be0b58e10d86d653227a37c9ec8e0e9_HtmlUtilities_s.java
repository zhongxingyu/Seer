 package com.mhacks.reencounter.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class HtmlUtilities {
     public static HttpClient defaultClient = new DefaultHttpClient();
 //    public static String endpoint = Resources.getSystem().getString(R.string.endpoint);
     public static String endpoint = "http://web.engr.illinois.edu/~reese6/MHacks/";
     static {
         HttpConnectionParams.setConnectionTimeout(defaultClient.getParams(), 10000);
     }
 
     public static String enc(String str) {
         try {
             return (str == null) ? "" : URLEncoder.encode(str, "ISO-8859-1");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return str;
     }
     
     public static JSONArray query(String query) {
         return queryArray(query, "posts");
     }
     
     public static JSONArray queryArray(String query, String key) {
         try {
             return HtmlUtilities.requestResponse(query).getJSONArray(key);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     public static JSONObject requestResponse(String query) {
         return toJson(executeResponse(query));
     }
 
     public static void executeResponseless(String request) {
         try {
             defaultClient.execute(new HttpPost(request)).getEntity().consumeContent();
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static HttpResponse executeResponse(String request) {
         try {
             return defaultClient.execute(new HttpPost(request));
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static JSONObject toJson(HttpResponse response) {
         try {
             return new JSONObject(inputStreamToString(response.getEntity().getContent()).toString());
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private static StringBuilder inputStreamToString(InputStream is) {
         String rLine = "";
         StringBuilder answer = new StringBuilder();
         BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          
         try {
             while ((rLine = rd.readLine()) != null) {
             answer.append(rLine);
             }
        }      
         catch (IOException e) {
             e.printStackTrace();
         }
         return answer;
     }
 }
