 package com.cloude.entropin;
 
 import java.net.CookieStore;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.xml.xpath.XPathException;
 
 import  org.apache.http.conn.ssl.SSLSocketFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.scheme.SocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 
 	public class CloudLoginTow extends Activity
 	{
 	
 	
 		/** Called when the activity is first created. */
 		@Override
 		public void onCreate(Bundle savedInstanceState)
 		{
 			super.onCreate(savedInstanceState);
 			setContentView(R.layout.main);
 				try{
 					new Thread(new Runnable()
 					{
 						public void run()
 						{
 							GAEConnector _gaeConnector = new GAEConnector(null, "http://logincookietest.appspot.com");
 						
 							if (!_gaeConnector.Authenticate(CloudLoginTow.this)) {
							Log.d("CLOUD","***AUTHENTICATION ERROR***");
 							}
 							
 							if (_gaeConnector.isAuthenticated()) 
 							{
 								try{
 									int httpStatusCode = _gaeConnector.GETContent("/test.jsp", true, true);
 									if (httpStatusCode == 200) {
 										String content = _gaeConnector.getLastContent();
										Log.d("CLOUD",content);
 									}
 								}catch(Exception e){
 									
 									
 								}
 							}
 							
 						}
 					}).start();
 				
 				}catch(Exception e){
 					
 					
 				}
 		
 		
 		}
 
 	}
