 package com.example.android.network.kit;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Base64;
 import android.util.Log;
 
 public abstract class ANTask extends AsyncTask<String, Void, Object>
 {
 	private Handler UIHandler;
 	private String URL;
 	private String action;
 	private List<NameValuePair> params;
 	private String queryString;
 	private static final String baseURL = "http://api.twitter.com";
 	private String password;
 	private String username;
 	private Context context;
 	private String type;
 	private static String userAgent;
 	private boolean hasInternet;
 	//unimplemented methods
 	protected abstract Object decode(String jsondata) throws JSONException;
 	
 	public ANTask(String URL, String action, List<NameValuePair> params, String type, Handler mainUIHandler, Context context)
 	{
 		UIHandler = mainUIHandler;
 		this.URL = URL;
 		this.action = action; //POST - GET - POSTJSON
 		this.params = params; //if post, use as post params, else turn into query string
 		this.context = context;
 		this.type = type;
 		hasInternet=true;
		ANUtils util = new ANUtils(context);
		username = util.getUsername();
		password = util.getPassword();
 		userAgent = getVersionString();
 		if(action.equalsIgnoreCase("GET"))//is get
 		{
 			decodeParams();
 		}
 	}
 	private boolean isNetworkAvailable() {
         ConnectivityManager connectivityManager 
               = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         return activeNetworkInfo != null;
     }
 	public String getVersionString(){
     	String toReturn = "ANDROID v.";
     	toReturn += android.os.Build.VERSION.SDK_INT;
     	toReturn += " - Model:" + android.os.Build.MODEL;
     	toReturn += " by " + android.os.Build.MANUFACTURER;
     	toReturn += "("+android.os.Build.DISPLAY + ")";
     	try {
 			toReturn += " - munchful v." + context.getPackageManager().getPackageInfo(context.getPackageName(), 0 ).versionName;
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return toReturn;
     }
 	protected void decodeParams()
 	{
 		if(params != null)
 		{
 			queryString = "?" + URLEncodedUtils.format(params, "UTF-8");
 			Log.d("queryString", queryString);
 		}
 		else{
 			queryString = "";
 			Log.d("queryString", "empty query string");
 		}
 	}
 	
 	@Override
 	protected Object doInBackground(String... arg0) 
 	{
 		if(isNetworkAvailable())
 		{
 			String data = "";
 			//first get the data from the server
 			if(action.equalsIgnoreCase("POST")){
 				data = doPost();
 			}
 			else if(action.equalsIgnoreCase("GET")){
 				data = doGet();
 			}
 			else{
 				data = doPostJson();
 			}
 			if(data!= null)
 			{
 				Object toReturn=null;
 				//now decode the response
 				try {
 					toReturn= decode(data);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return toReturn;
 			}
 			Log.d("Error", "Error in asych task");
 			return "ERROR";
 		}
 		Log.d("Error", "No internet");
 		hasInternet = false;
 		return null;
 	}
 	
 	@Override
 	protected void onPreExecute() 
 	{
 		super.onPreExecute();
 		Log.d("URL", baseURL + URL);
 	}
 	
 	@Override
 	protected void onPostExecute(Object result) 
 	{
 		//Notify UI
 		Message msg = Message.obtain();
 		if(result != null){
 			
 	        msg.what = 1; // 1 is succuess
 	        Bundle b = new Bundle();
 	        b.putString("type", type);
 	        msg.setData(b);
 	        msg.obj = result;
 	        
 		}
 		else if(!hasInternet){
 			msg.what = 3;
 		}
 		else{
 			msg.what = 2;//2= general error
 		}
 		UIHandler.sendMessage(msg);
         super.onPostExecute(result);
 	}
 	
 	public String doPost()
 	{
 		Log.d("ASYCH", "Starting POST");
 		StringBuilder builder = new StringBuilder();
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		try 
         {
 			
 			String full_url = baseURL + URL;
 			Log.d("URL", full_url);
 			HttpPost httppost = new HttpPost(full_url);
 			httppost.setHeader("Authorization", "Basic "+Base64.encodeToString(((username+":"+password).getBytes()), Base64.NO_WRAP));
 	        httppost.setEntity(new UrlEncodedFormEntity(params));
 	        httppost.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
 	        response = client.execute(httppost);
 	        
 	        StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			Log.d("Status code", String.valueOf(statusCode));
 			if (statusCode == 200) 
 			{
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) 
 				{
 					builder.append(line);
 				}
 			} 
 			else 
 			{
 				Log.e(ANTask.this.toString(), "Failed to download file");
 				
 			}
 		} 
 	    catch (ClientProtocolException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		finally {
             // When HttpClient instance is no longer needed,
             // shut down the connection manager to ensure
             // immediate deallocation of all system resources
             client.getConnectionManager().shutdown();
         }
 		return builder.toString();
 	
 	}
 	public String doGet()
 	{
 		Log.d("ASYCH", "Starting GET");
 		StringBuilder builder = new StringBuilder();
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		try 
         {
 			
 			String full_url = baseURL + URL + queryString;
 			Log.d("URL", full_url);
 			HttpGet httpget = new HttpGet(full_url);
 			//String encoded = Base64.encodeToString((device_id+":"+sec_token).getBytes(), Base64.URL_SAFE));
 			httpget.setHeader("Authorization", "Basic "+Base64.encodeToString(((username+":"+password).getBytes()), Base64.NO_WRAP));
 			httpget.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
 	        response = client.execute(httpget);
 	        
 	        StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			Log.d("Status code", String.valueOf(statusCode));
 			if (statusCode == 200) 
 			{
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) 
 				{
 					builder.append(line);
 				}
 			} 
 			else 
 			{
 				Log.e(ANTask.this.toString(), "Failed to download file");
 				
 			}
 		} 
 	    catch (ClientProtocolException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		finally {
             // When HttpClient instance is no longer needed,
             // shut down the connection manager to ensure
             // immediate deallocation of all system resources
             client.getConnectionManager().shutdown();
         }
 		return builder.toString();
 	}
 	public String doPostJson()
 	{
 		Log.d("ASYNC", "Starting POSTJSON");
 		StringBuilder builder = new StringBuilder();
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		try 
         {
 			
 			String full_url = baseURL + URL;
 			Log.d("URL", full_url);
 			HttpPost httppost = new HttpPost(full_url);
 			httppost.setHeader("Authorization", "Basic "+Base64.encodeToString(((username+":"+password).getBytes()), Base64.NO_WRAP));
 			//passes the results to a string builder/entity
 			BasicNameValuePair p = (BasicNameValuePair)params.get(0);
 			String jsondata = p.getValue();
 			Log.d("JSONDATAPARAM", jsondata);
 		    StringEntity se = new StringEntity(jsondata);
 		    
 		    //sets the post request as the resulting string
 		    httppost.setEntity(se);
 		    //sets a request header so the page receving the request
 		    //will know what to do with it
 		    httppost.setHeader("Accept", "application/json");
 		    httppost.setHeader("Content-type", "application/json");
 		    httppost.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
 
 		    //Handles what is returned from the page 
 	        
 	        response = client.execute(httppost);
 	        
 	        StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			Log.d("Status code", String.valueOf(statusCode));
 			if (statusCode == 200) 
 			{
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) 
 				{
 					builder.append(line);
 				}
 			} 
 			else 
 			{
 				Log.e(ANTask.this.toString(), "Failed to download file");
 				
 			}
 		} 
 	    catch (ClientProtocolException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		finally {
             // When HttpClient instance is no longer needed,
             // shut down the connection manager to ensure
             // immediate deallocation of all system resources
             client.getConnectionManager().shutdown();
         }
 		return builder.toString();
 	
 	}
 }
