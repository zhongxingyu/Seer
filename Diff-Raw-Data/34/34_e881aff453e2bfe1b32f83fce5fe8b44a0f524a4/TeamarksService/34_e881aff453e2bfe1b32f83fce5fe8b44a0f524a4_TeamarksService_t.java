 package com.teamarks.android;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.AbstractHttpMessage;
 
 
 import android.util.Log;
 
 
 public class TeamarksService {
 	
 
 	public static String tag = "Teamarks" ;
 	ArrayList<NameValuePair> params;
 
 	public void shareUrl(String endpoint,
 			ArrayList<NameValuePair> postParameters, String charSetName) throws UnsupportedEncodingException {
 
 		String result = null;
 		if (charSetName == null) {
 			charSetName = "utf-8";
 		}
 		HttpClient httpclient = new DefaultHttpClient();
 		
 		HttpPost request = new HttpPost(endpoint);
 		request.addHeader("Charset",charSetName);
 		HttpResponse httpResponse = null;
 		request.setEntity(new UrlEncodedFormEntity(postParameters)) ;
 	    ResponseHandler<String> handler = new BasicResponseHandler();  
 	        try {  
 	           result = httpclient.execute(request, handler);  
	           Log.i(tag,result);
 	        } catch (ClientProtocolException e) {  
 	            e.printStackTrace();  
 	        } catch (IOException e) {  
 	            e.printStackTrace();  
 	        }  
 	        httpclient.getConnectionManager().shutdown();  
 	  //      Log.i(tag,result);  		
 		
 	}
 }
