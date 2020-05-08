 package com.spiteful.cipher.service;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.spiteful.cipher.Constants;
 
 public abstract class BaseWebService {
 	private static final String TAG = BaseWebService.class.toString();
 
 	private WebActionCallback callback;
 
 	public BaseWebService(WebActionCallback callback) {
 		this.callback = callback;
 	}
 
 	public abstract String getPath();
 
 	public abstract String getBody();
 
 	public void performAction() {
 		ServiceASyncTask task = new ServiceASyncTask();
 		task.execute(new Void[] {});
 	}
 
 	public class ServiceASyncTask extends AsyncTask<Void, Void, JSONObject> {
 
 		@Override
 		protected JSONObject doInBackground(Void... args) {
 			String urlString = Constants.BASE_URL + getPath();
 			String postData = getBody();
 
 			Log.d(TAG, "New Request: " + urlString + " with: " + postData);
 
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 
 			// url with the post data
 			HttpPost httpost = new HttpPost(urlString);
 
 			// passes the results to a string builder/entity
 			StringEntity se = null;
 			try {
 				se = new StringEntity(postData);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			// sets the post request as the resulting string
 			httpost.setEntity(se);
 			// sets a request header so the page receving the request
 			// will know what to do with it
 			httpost.setHeader("Accept", "application/json");
 			httpost.setHeader("Content-type", "application/json");
 
 			try {
 				return httpclient.execute(httpost, handler);
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject result) {
 			callback.onCompleted(result);
 		}
 
 		private ResponseHandler<JSONObject> handler = new ResponseHandler<JSONObject>() {
 
 			@Override
 			public JSONObject handleResponse(HttpResponse response)
 					throws ClientProtocolException, IOException {
 				String responseBody = EntityUtils
 						.toString(response.getEntity());
				try {
					return (JSONObject) JSONValue.parse(responseBody);
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
 			}
 		};
 	}
 
 }
