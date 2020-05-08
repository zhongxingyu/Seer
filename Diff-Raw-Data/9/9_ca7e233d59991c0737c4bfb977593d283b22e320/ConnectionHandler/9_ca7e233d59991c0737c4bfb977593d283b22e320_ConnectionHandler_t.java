 package com.connections;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.http.NameValuePair;
 
 import android.os.Handler;
 import android.os.Message;
 
 /**
  * Simplifier Asynchronous HTTP connections
  * 
  * @author Stefanus Diptya; Twitter @eSDhee
  * 
  * @category Blog {@link http ://stefanusdiptya.wordpress.com}
  */
 public class ConnectionHandler extends Handler {
 	public final static int GET = 0;
 	public final static int POST = 1;
 	public static final int PUT = 2;
 	public static final int DELETE = 3;
 	public static final int BITMAP = 4;
 
 	private ConnectionResult connectionResult;
 
 	public ConnectionHandler(ConnectionResult connectionResult,
 			ArrayList<NameValuePair> params, String url, int Type) {
 		// TODO Auto-generated constructor stub
 		this.connectionResult = connectionResult;
 		switch (Type) {
 		case GET:
 			new HttpConnection(this).get(url + paramsToString(params));
 			break;
 		case POST:
 			new HttpConnection(this).post(url, params);
 			break;
 		case PUT:
 			new HttpConnection(this).put(url, paramsToString(params));
 			break;
 		case DELETE:
 			new HttpConnection(this).delete(url);
 			break;
 		case BITMAP:
 			new HttpConnection(this).bitmap(url);
 			break;
 		default:
 			new HttpConnection(this).post(url, params);
 			break;
 		}
 	}
 
 	public ConnectionHandler(ConnectionResult connectionResult,
 			ArrayList<NameValuePair> params, String url, String username,
 			String password, int Type) {
 		// TODO Auto-generated constructor stub
 		this.connectionResult = connectionResult;
 		switch (Type) {
 		case GET:
			new HttpConnection(this).get(url + paramsToString(params),
					username, password);
 			break;
 		case POST:
			new HttpConnection(this).post(url, params, username, password);
 			break;
 		case PUT:
 			new HttpConnection(this).put(url, paramsToString(params));
 			break;
 		case DELETE:
 			new HttpConnection(this).delete(url);
 			break;
 		case BITMAP:
 			new HttpConnection(this).bitmap(url);
 			break;
 		default:
			new HttpConnection(this).post(url, params, username, password);
 			break;
 		}
 	}
 
 	private static String paramsToString(ArrayList<NameValuePair> arrayList) {
 		String param = "?";
 		if (arrayList != null) {
 			int itr = 0;
 			Iterator<NameValuePair> iterator = arrayList.iterator();
 			while (iterator.hasNext()) {
 				NameValuePair obj = (NameValuePair) iterator.next();
 				if (itr == 0)
 					param += obj.getName() + "=" + obj.getValue();
 				else
 					param += "&" + obj.getName() + "=" + obj.getValue();
 				itr++;
 			}
 
 		}
 		return param;
 	}
 
 	public void handleMessage(Message message) {
 		switch (message.what) {
 		case HttpConnection.DID_START: {
 			break;
 		}
 		case HttpConnection.DID_SUCCEED: {
 			String response = (String) message.obj;
 			connectionResult.gotResult(response, null);
 			break;
 		}
 		case HttpConnection.DID_ERROR: {
 			Exception e = (Exception) message.obj;
 			e.printStackTrace();
 			connectionResult.gotResult(
 					null,
 					"Connection failed. Please try again. Caused by "
 							+ e.getMessage());
 			break;
 		}
 		}
 	}
 
 	public static abstract class ConnectionResult {
 		public abstract void gotResult(String result, String message);
 	}
 }
