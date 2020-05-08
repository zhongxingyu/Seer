 package com.pcreations.restclient;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import android.util.Log;
 
 public class RestService extends IntentService{
 	
 	private final static String TAG = "Http";
 	
	public RestService() {
		super("RestService");
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Uri uri = intent.getData();
 		Bundle bundle = intent.getExtras();
 		int method = bundle.getInt(WebService.METHOD_KEY);
 		//Bundle params = bundle.getParcelable(WebService.PARAMS_KEY);
 		ResultReceiver receiver = bundle.getParcelable(WebService.RECEIVER_KEY);
 		
 		try {            
 	        HttpRequestBase request = null;
 	        switch (method) {
 	            case WebService.GET:
 	                request = new HttpGet();
 	            break;
 	            
 	            case WebService.DELETE:
 	                request = new HttpDelete();
 	            break;
 	     
 	            case WebService.POST:
 	                //TODO
 	            break;
 	            
 	            case WebService.PUT:
 	                //TODO
 	            break;
 	        }
 	        
 	        request.setURI(new URI(uri.toString()));
 	        
 	        if (request != null) {
 	            HttpClient client = new DefaultHttpClient();
 	            
 	            // Let's send some useful debug information so we can monitor things
 	            // in LogCat.
 	            Log.d(TAG, "Executing request: "+ Integer.valueOf(method) +": "+ uri.toString());
 	            
 	            // Finally, we send our request using HTTP. This is the synchronous
 	            // long operation that we need to run on this thread.
 	            HttpResponse response = client.execute(request);
 	            
 	            HttpEntity responseEntity = response.getEntity();
 	            StatusLine responseStatus = response.getStatusLine();
 	            int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
 	            
 	            // Our ResultReceiver allows us to communicate back the results to the caller. This
 	            // class has a method named send() that can send back a code and a Bundle
 	            // of data. ResultReceiver and IntentService abstract away all the IPC code
 	            // we would need to write to normally make this work.
 	            if (responseEntity != null) {
 	                Bundle resultData = new Bundle();
 	                resultData.putString(WebService.RESULT_KEY, EntityUtils.toString(responseEntity));
 	                receiver.send(statusCode, resultData);
 	            }
 	            else {
 	                receiver.send(statusCode, null);
 	            }
 	        }
 	    }
 	    catch (URISyntaxException e) {
 	        Log.e(TAG, "URI syntax was incorrect. "+ Integer.valueOf(method) +": "+ uri.toString(), e);
 	        receiver.send(0, null);
 	    }
 	    catch (UnsupportedEncodingException e) {
 	        Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
 	        receiver.send(0, null);
 	    }
 	    catch (ClientProtocolException e) {
 	        Log.e(TAG, "There was a problem when sending the request.", e);
 	        receiver.send(0, null);
 	    }
 	    catch (IOException e) {
 	        Log.e(TAG, "There was a problem when sending the request.", e);
 	        receiver.send(0, null);
 	    }
 	}
 
 }
