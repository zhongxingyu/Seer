 /**
  * 
  */
 package whyq.utils;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import whyq.activity.FavouriteActivity;
 import whyq.activity.ListActivity;
 import whyq.interfaces.HttpAccess;
 import android.os.AsyncTask;
 import android.util.Log;
 
 
 /**
  * @author Linh Nguyen
  *
  */
 public class HttpPermUtils {
 	private DefaultHttpClient client = new DefaultHttpClient();
 	private HttpAccess httpAccess;
 	public static int TIME_OUT = 30000;
 	public HttpPermUtils(HttpAccess delegate) {
 		// TODO Auto-generated constructor stub
 		httpAccess = delegate;
 	}
 
 	public HttpPermUtils() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * Get the response as string from the GET method request with its parameters
 	 * @param url the url
 	 * @return the response as string
 	 */
 	public String sendAsynRequest(String url, List<NameValuePair> nameValuePairs, boolean isGet) {
 //		 new sendRequestTask(nameValuePairs, null, isGet).execute(url);
 			if(isGet){
 				sendGetRequest(url);
 				return null;
 			}else{
 				String result=null;
 				HttpPost postRequest = new HttpPost(url);
 				try {			
 					if (nameValuePairs != null) {
//						postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
 					}
 					if(ListActivity.isSearch)
 					{
 						postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
 					}else if(FavouriteActivity.isFavorite){
 						postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
 					}
 					client = new DefaultHttpClient();
 					final HttpParams httpParameters = client.getParams();
 					HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
 					HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);
 					
 					HttpResponse postResponse = client.execute(postRequest);
 					int statusCode = postResponse.getStatusLine().getStatusCode();
 					if (statusCode != HttpStatus.SC_OK) {
 						//Log.w(getClass().getSimpleName(), "ERROR: " + statusCode + " for URL: " + url); 
 			            result = null;
 			            return null;
 					}else{
 						HttpEntity postResponseEntity = postResponse.getEntity();
 						if (postResponseEntity != null)
 							result =  EntityUtils.toString(postResponseEntity);
 						Log.d("result", "Result: "+result);
 					}
 
 				} catch (IOException ioe) {
 					//Logger.appendLog(ioe.toString(), "httpErrorLog");
 					postRequest.abort();
 					//Log.w(getClass().getSimpleName(), "thien====>ERROR for URL " + url, ioe);
 					httpAccess.onError();
 					result = null;
 					return null;
 				}
 				return result;
 			}
 	}
 	public String sendRequest(String url, List<NameValuePair> nameValuePairs, boolean isGet) {
 		 new sendRequestTask(nameValuePairs, null, isGet).execute(url);
 		 return null;
 	}
 	public String sendRequest(String url, List<NameValuePair> nameValuePairs, String id, boolean isGet) {
 		 new sendRequestTask(nameValuePairs, id, isGet).execute(url);
 		 return null;
 	}
 	public void execSendAsynRequest(){
 		
 	}
 	class sendRequestTask extends AsyncTask<String, Void, String> {
 		
 	    private Exception exception;
 	    private List<NameValuePair> nameValuePairs;
 	    private String myDiaryThumbId;
 	    private boolean isGet;
 	    public sendRequestTask(List<NameValuePair> nameValuePairs, String id, boolean isGet) {
 			// TODO Auto-generated constructor stub
 	    	this.nameValuePairs = nameValuePairs;
 	    	this.myDiaryThumbId = id;
 	    	this.isGet = isGet;
 		}
 
 		protected String doInBackground(String... urls) {
 			if(isGet){
 				String result = sendGetRequest(urls[0]);
 				return result;
 			}else{
 				String url = null;
 				String result=null;
 				if(urls != null)
 					 url = urls[0];
 				HttpPost postRequest = new HttpPost(url);
 				try {		
 					String charset ="UTF_8";
 					//Log.d("==","=========>"+nameValuePairs.toString());
 					if (nameValuePairs != null) {
 						postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
 						postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
 					}
 					client = new DefaultHttpClient();
 					final HttpParams httpParameters = client.getParams();
 					HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
 					HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);
 					
 					postRequest.setHeader("Accept-Charset", charset);
 					HttpResponse postResponse = client.execute(postRequest);
 					int statusCode = postResponse.getStatusLine().getStatusCode();
 					if (statusCode != HttpStatus.SC_OK) {
 						//Log.w(getClass().getSimpleName(), "ERROR: " + statusCode + " for URL: " + url); 
 			            result = null;			
 					}else{
 						HttpEntity postResponseEntity = postResponse.getEntity();
 						if (postResponseEntity != null)
 							result =  EntityUtils.toString(postResponseEntity);
 						Log.d("Register reuslt","Result:"+result);
 					}
 
 				} catch (IOException ioe) {
 					//Logger.appendLog(client+":"+ioe.toString()+"data"+nameValuePairs.toString(), "httpParseLog");
 					postRequest.abort();
 					//Log.w(getClass().getSimpleName(), "thien====>ERROR for URL " + url, ioe);
 					httpAccess.onError();
 					result = null;
 					return null;
 				}
 
 				return result;
 
 			}
 		}
 
 	    protected void onPostExecute(String result) {
 	        // TODO: check this.exception 
 	        // TODO: do something with the feed
 			if (result != null && result != "" && httpAccess != null) {	//!result.isEmpty() &&			
 				httpAccess.onSeccess(result, this.myDiaryThumbId);
 			}else if(httpAccess != null){
 				httpAccess.onError();
 			}
 	    		
 
 	    }
 	 }
 
 	/**
 	 * Get the response as string from the GET method request without parameters
 	 * @param url the url
 	 * @return the response as string
 	 */
 	public String sendGetRequest(String url) {
 		HttpGet getRequest = new HttpGet(url);
 		try {
 			client = new DefaultHttpClient();
 			final HttpParams httpParameters = client.getParams();
 			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
 			HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);
 			
 			HttpResponse getResponse = client.execute(getRequest);
 			int statusCode = getResponse.getStatusLine().getStatusCode();
 			if (statusCode != HttpStatus.SC_OK) {
 				//Log.w(getClass().getSimpleName(), "ERROR: " + statusCode + " for URL: " + url);
 				return null;
 			}
 			HttpEntity getResponseEntity = getResponse.getEntity();
 			if (getResponseEntity != null)
 				return EntityUtils.toString(getResponseEntity);					
 		} catch (IOException ioe) {
 			//Logger.appendLog(ioe.toString(), "sendGetRequestLog");
 			getRequest.abort();
 			//Log.w(getClass().getSimpleName(), "ERROR for URL: " + url, ioe);
 		}
 		
 		return null;
 	}
 }
