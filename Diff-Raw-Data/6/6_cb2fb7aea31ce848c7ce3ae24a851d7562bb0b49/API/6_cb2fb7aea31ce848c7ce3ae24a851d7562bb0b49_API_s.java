 package com.grandst.whiplash.api;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.grandst.whiplash.Whiplash;
 
 
 
 public final class API {
 	
 	public static String get(String apiCall, Whiplash w) throws ClientProtocolException, IOException {
 		HttpClient client = new DefaultHttpClient();
 		HttpGet getReq = new HttpGet(w.getApiBaseUrl()+apiCall);
 		setHeaders(getReq, w);
 		final HttpResponse response = client.execute(getReq);  
 		InputStream inputStream;
 		inputStream = response.getEntity().getContent();		
 		return inputStreamToString(inputStream);
 	}
 	
 	public static String post(String apiCall, Whiplash w, List<NameValuePair> postData, int timeoutConnection,int timeoutSocket) throws ClientProtocolException, IOException {
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		HttpClient client = new DefaultHttpClient(httpParameters); 	
 		HttpPost postReq = new HttpPost(w.getApiBaseUrl()+apiCall);
 		if (postData != null) {
 			HashMap<String,String> map = new HashMap<String,String>();
 			for(NameValuePair nvp : postData){
 				 map.put(nvp.getName(),nvp.getValue());
 			}
 			GsonBuilder gb = new GsonBuilder()
 				.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
 				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
 			postReq.setEntity(new StringEntity(gb.create().toJson(map)));
 		}
 		setHeaders(postReq,w);
 		final HttpResponse response = client.execute(postReq);  
 		InputStream inputStream;
 		inputStream = response.getEntity().getContent();		
 		return inputStreamToString(inputStream);
 	}
 	public static String post(String apiCall, Whiplash w, StringEntity jsonObj, int timeoutConnection,int timeoutSocket) throws ClientProtocolException, IOException {
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		HttpClient client = new DefaultHttpClient(httpParameters); 	
 		HttpPost postReq = new HttpPost(w.getApiBaseUrl()+apiCall);
 		if (jsonObj != null) {
 			//postReq.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
 			postReq.setEntity(jsonObj);
 		}
 		setHeaders(postReq,w);
 		final HttpResponse response = client.execute(postReq);  
 		InputStream inputStream;
 		inputStream = response.getEntity().getContent();		
 		return inputStreamToString(inputStream);
 	}
 	
 	public static String put(String apiCall, Whiplash w, List<NameValuePair> putData, int timeoutConnection,int timeoutSocket) throws ClientProtocolException, IOException  {
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		HttpClient client = new DefaultHttpClient(httpParameters); 	
 		HttpPut putReq = new HttpPut(w.getApiBaseUrl()+apiCall);
 		if (putData != null) {
 			putReq.setEntity(new UrlEncodedFormEntity(putData));
 		}
 		setHeaders(putReq,w);
 		final HttpResponse response = client.execute(putReq);  
 		InputStream inputStream = response.getEntity().getContent();
 		return inputStreamToString(inputStream);	 
 	}
 	
 	public static String delete(String apiCall, Whiplash w,int timeoutConnection,int timeoutSocket) throws ClientProtocolException, IOException  {
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		HttpClient client = new DefaultHttpClient(httpParameters); 
 		HttpDelete delReq = new HttpDelete(w.getApiBaseUrl()+apiCall);
 		setHeaders(delReq,w);
 		final HttpResponse response = client.execute(delReq);  
 		InputStream inputStream = response.getEntity().getContent();
 		return inputStreamToString(inputStream);
 	}
 	
 	private static String inputStreamToString(InputStream is) throws IOException{
 		byte[] data = new byte[512];
 		int len = 0;
 		StringBuffer buffer = new StringBuffer();
 		while (-1 != (len = is.read(data)) )
 		{
 			buffer.append(new String(data, 0, len)); 
 		}
 		is.close();
 		return buffer.toString();	
 	}
 	
 	private static void setHeaders(HttpRequestBase req, Whiplash w){
 		req.setHeader("X-API-KEY",w.getApiKey());
		req.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
		//req.setHeader("Content-Type", "application/json");
 		req.setHeader("Accept","application/json");
 		//use later
 		//req.setHeader("X-API-VERSION",API_KEY);
 	}
 
 }
