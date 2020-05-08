 /*
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.heneryh.aquanotes.io;
 
 
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HttpContext;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 /**
  * Executes an {@link HttpUriRequest} and passes the result as an
  * {@link XmlPullParser} to the given {@link XmlHandler}.
  */
 public class ApexExecutor {
     private final HttpClient mHttpClient;
     private final ContentResolver mDbResolver;
     private final Context mActContext;
     Uri controllerUri;
 
     public ApexExecutor(Context cx, HttpClient httpClient, ContentResolver resolver) {
     	mActContext = cx;
    		mHttpClient = httpClient;
    		mDbResolver = resolver; // resolver from the context of syncService
     }
 
     /**
      * Execute a {@link HttpGet} request, passing a valid response through
      * {@link XmlHandler#parseAndApply(XmlPullParser, ContentResolver)}.
      */
     public void executeGet(Uri ctrlUri, DefaultHandler xmlParser) throws HandlerException {
 
     	controllerUri = ctrlUri;
 		Cursor cursor = null;
 
 		String username = null;
 		String password = null;
 		String apexBaseURL = null;
 		String apexWANURL = null;
 		String apexWiFiURL = null;
 		String apexWiFiSID = null;
 		String controllerType = null;
 
 		// Poll the database for facts about this controller
 		try {
 			cursor = mDbResolver.query(controllerUri, ControllersQuery.PROJECTION, null, null, null);
 			if (cursor != null && cursor.moveToFirst()) {
 				username = cursor.getString(ControllersQuery.USER);
 				password = cursor.getString(ControllersQuery.PW);
 				apexWANURL = cursor.getString(ControllersQuery.WAN_URL);
 				apexWiFiURL = cursor.getString(ControllersQuery.LAN_URL);
 				apexWiFiSID = cursor.getString(ControllersQuery.WIFI_SSID);
 				controllerType = cursor.getString(ControllersQuery.MODEL);
 			}
 		} catch (SQLException e) {
 			throw new HandlerException("Database error getting controller data.");
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		// Uhg, WifiManager stuff below crashes in AVD if wifi not enabled so first we have to check if on wifi
 		ConnectivityManager cm  = (ConnectivityManager) mActContext.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo nInfo = cm.getActiveNetworkInfo();
 
 		if(nInfo.getType()==ConnectivityManager.TYPE_WIFI) {
 			// Get the currently connected SSID, if it matches the 'Home' one then use the local WiFi URL rather than the public one
 			WifiManager wm = (WifiManager) mActContext.getSystemService(Context.WIFI_SERVICE);
 			WifiInfo wInfo = wm.getConnectionInfo();
 
			if(wInfo.getSSID().substring(1).toLowerCase().startsWith(apexWiFiSID.toLowerCase())) {  // the ssid will be quoted in the info class
 				apexBaseURL=apexWiFiURL;
 			} else {
 				apexBaseURL=apexWANURL;
 			}
 		} else {
 			apexBaseURL=apexWANURL;
 
 		}
 		
 		// for this function we need to append to the URL.  I should really
 		// check if the "/" was put on the end by the user here to avoid 
 		// possible errors.
 		if(!apexBaseURL.endsWith("/")) {
 			String tmp = apexBaseURL + "/";
 			apexBaseURL = tmp;
 		}
 
 		// oh, we should also check if it starts with an "http://"
 		if(!apexBaseURL.startsWith("http://")) {
 			String tmp = "http://" + apexBaseURL;
 			apexBaseURL = tmp;
 		}
 
 		// oh, we should also check if it ends with an "status.sht" on the end and remove it.
 
 		/********************/
 		// When all cleaned up, add the xml portion of the url to grab the status.
 		String apexURL = apexBaseURL + "cgi-bin/status.xml";
 
         final HttpUriRequest request = new HttpGet(apexURL);
         execute(request, xmlParser, username, password);
     }
 
     /**
      * Execute this {@link HttpUriRequest}, passing a valid response through
      * {@link XmlHandler#parseAndApply(XmlPullParser, ContentResolver)}.
      */
     public void execute(HttpUriRequest request, DefaultHandler xmlParser, String user, String pw) throws HandlerException {
     	
         try {  	
     		//Create credentials for basic auth
     		// create a basic credentials provider and pass the credentials
     		// Set credentials provider for our default http client so it will use those credentials
     		UsernamePasswordCredentials c = new UsernamePasswordCredentials(user,pw);
     		BasicCredentialsProvider cP = new BasicCredentialsProvider();
     		cP.setCredentials(AuthScope.ANY, c );
     		((DefaultHttpClient) mHttpClient).setCredentialsProvider(cP);
 
             final HttpResponse resp = mHttpClient.execute(request);
             final int status = resp.getStatusLine().getStatusCode();
             if (status != HttpStatus.SC_OK) {
                 throw new HandlerException("Unexpected server response " + resp.getStatusLine()
                         + " for " + request.getRequestLine());
             }
 
             final InputStream input = resp.getEntity().getContent();
             
             try {
                 NewXmlHandler.parseAndStore(input, controllerUri, xmlParser);
             } catch (HandlerException e) {
                 throw new HandlerException("Malformed response for " + request.getRequestLine(), e);
             } finally {
                 if (input != null) input.close();
             }
         } catch (HandlerException e) {
             throw e;
         } catch (IOException e) {
             throw new HandlerException("Problem reading remote response for "
                     + request.getRequestLine(), e);
         }
     }
     
     private interface ControllersQuery {
         String[] PROJECTION = {
 //              String CONTROLLER_ID = "_id";
 //              String TITLE = "title";
 //              String WAN_URL = "wan_url";
 //              String LAN_URL = "wifi_url";
 //              String WIFI_SSID = "wifi_ssid";
 //              String USER = "user";
 //              String PW = "pw";
 //              String LAST_UPDATED = "last_updated";
 //              String UPDATE_INTERVAL = "update_i";
 //              String DB_SAVE_DAYS = "db_save_days";
 //              String CONTROLLER_TYPE = "controller_type";
                 BaseColumns._ID,
                 AquaNotesDbContract.Controllers.TITLE,
                 AquaNotesDbContract.Controllers.WAN_URL,
                 AquaNotesDbContract.Controllers.LAN_URL,
                 AquaNotesDbContract.Controllers.WIFI_SSID,
                 AquaNotesDbContract.Controllers.USER,
                 AquaNotesDbContract.Controllers.PW,
                 AquaNotesDbContract.Controllers.LAST_UPDATED,
                 AquaNotesDbContract.Controllers.UPDATE_INTERVAL,
                 AquaNotesDbContract.Controllers.DB_SAVE_DAYS,
                 AquaNotesDbContract.Controllers.MODEL,
         };
         
         int _ID = 0;
         int TITLE = 1;
         int WAN_URL = 2;
         int LAN_URL = 3;
         int WIFI_SSID = 4;
         int USER = 5;
         int PW = 6;
         int LAST_UPDATED = 7;
         int UPDATE_INTERVAL = 8;
         int DB_SAVE_DAYS = 9;
         int MODEL = 10;
     }
 }
