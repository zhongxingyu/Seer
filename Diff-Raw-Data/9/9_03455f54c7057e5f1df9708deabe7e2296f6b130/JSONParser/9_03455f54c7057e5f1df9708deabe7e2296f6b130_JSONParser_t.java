 package realtalk.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * JSONParser is a helper class that makes a generic POST/GET request to a given url,
  * with embedded params.
  * 
  * @author Taylor Williams / Colin Kho
  *
  */
 public class JSONParser {
     public JSONParser() {}
     
     /** Sends a request to a url with given params
      * @param stUrl		The url to send the request to
      * @param stMethod	POST or GET
      * @param rgparams	a list of parameters to embed in the request
      *	@return a JSONObject with the result of the request
      */
     public JSONObject makeHttpRequest(String stUrl, String stMethod, List<NameValuePair> rgparams) {
        InputStream inputstream = null;
        JSONObject jsonobject = null;
        String stJson = "";
         try {
             // check for request method
             if(stMethod == "POST"){
                 // request method is POST
                 // defaultHttpClient
                 DefaultHttpClient httpclient = new DefaultHttpClient();
                 HttpPost httppost = new HttpPost(stUrl);
                 httppost.setEntity(new UrlEncodedFormEntity(rgparams));
  
                 HttpResponse httpresponse = httpclient.execute(httppost);
                 HttpEntity httpentity = httpresponse.getEntity();
                 inputstream = httpentity.getContent();
  
             }else if(stMethod == "GET"){
                 // request method is GET
                 DefaultHttpClient httpclient = new DefaultHttpClient();
                 String stParam = URLEncodedUtils.format(rgparams, "utf-8");
                 stUrl += "?" + stParam;
                 HttpGet httpget = new HttpGet(stUrl);
  
                 HttpResponse httpresponse = httpclient.execute(httpget);
                 HttpEntity httpentity = httpresponse.getEntity();
                 inputstream = httpentity.getContent();
             } 
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     inputstream, "iso-8859-1"), 8);
             StringBuilder stringbuilder = new StringBuilder();
             String line = null;
             while ((line = reader.readLine()) != null) {
                 stringbuilder.append(line + "\n");
             }
             inputstream.close();
             stJson = stringbuilder.toString();
         } catch (Exception e) {
             Log.e("Buffer Error", "Error converting result " + e.toString());
         }
  
         // try parse the string to a JSON object
         try {
             jsonobject = new JSONObject(stJson);
         } catch (JSONException e) {
             Log.e("JSON Parser", "Error parsing data " + e.toString());
         }
  
         // return JSON String
         return jsonobject;
     }
 }
