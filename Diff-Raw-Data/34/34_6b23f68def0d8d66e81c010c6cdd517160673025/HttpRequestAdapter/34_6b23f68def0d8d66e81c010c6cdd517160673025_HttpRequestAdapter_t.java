 package ca.ryerson.scs.rus.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
import android.util.Log;
 
 public class HttpRequestAdapter {
 
 	
 	public static void httpRequest(Context context, String url, ResponseHandler handler){
 		HttpRequestAdapter.Request request= new HttpRequestAdapter.Request(context,handler);
 		request.execute(url);
 	}
 	public static void httpRequest(Context context, String url, JSONObject data, ResponseHandler handler){
 		HttpRequestAdapter.Request request= new HttpRequestAdapter.Request(context,data,handler);
 		request.execute(url);
 	}
 	
 	private static JSONObject httpProcessRequest(Context context, String url,
 			JSONObject data) {
 		try {
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(url);
 			
 	        StringEntity se = new StringEntity(data.toString());
 	        httpPost.setEntity(se);
 	        httpPost.setHeader("Content-Type","application/json");
 	        
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 
 			HttpEntity httpEntity = httpResponse.getEntity();
 
 			if (httpEntity != null) {
 				httpEntity.toString();
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						httpEntity.getContent()));
 
 				StringBuilder fullResponse = new StringBuilder();
 				String line;
 				while ((line = br.readLine()) != null) {
 					fullResponse.append(line + "\n");
 				}
				Log.i("RESPONSE", fullResponse.toString());
 				return (new JSONObject(fullResponse.toString()));
 			}
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static JSONObject httpProcessRequest(Context context, String url) {
 
 		try {
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(url);
 
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity httpEntity = httpResponse.getEntity();
 
 			if (httpEntity != null) {
 				httpEntity.toString();
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						httpEntity.getContent()));
 
 				StringBuilder fullResponse = new StringBuilder();
 				String line;
 				while ((line = br.readLine()) != null) {
 					fullResponse.append(line + "\n");
 				}
				Log.i("RESPONSEGET", fullResponse.toString());
 				return (new JSONObject(fullResponse.toString()));
 			}
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private static class Request extends AsyncTask<String, Integer, JSONObject> {
 
 		private ProgressDialog dialog;
 		private Context context;
 		private JSONObject data;
 		private ResponseHandler handler;
 		public Request(Context context,ResponseHandler handler) {
 			this.context = context;
 			this.handler=handler;
			this.dialog = new ProgressDialog(context);
 		}
 		
 		public Request(Context context,JSONObject data,ResponseHandler handler) {
 			this.context = context;
 			this.handler=handler;
			this.data = data;
			this.dialog = new ProgressDialog(context);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			dialog.setMessage("Connecting to Server");
 			dialog.show();
 		}
 		
 		@Override
 		protected JSONObject doInBackground(String... url) {
 			JSONObject response = null;
 			if (data==null){
				Log.i("DOING GET","DOING GET");
 				response=HttpRequestAdapter.httpProcessRequest(context, url[0]);
 			}else if(data!=null){
				Log.i("DOING POST","DOING POST");
 				response=HttpRequestAdapter.httpProcessRequest(context, url[0],data);
 			}			
 			return response;
 		}
 		
 		@Override
 		protected void onPostExecute(JSONObject result) {
 			handler.postResponse(result);
 		}
 	}
 	
 	public interface ResponseHandler {
 		
 		public void postResponse(JSONObject response);
 
 		public void postTimeout();
 	}
 }
 
