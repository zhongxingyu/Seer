 /**
  * @contributor(s): Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			22 November 2011
  * @revised:
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 
 /**
  * @description:
  *
  *
  */
 
 
 package org.ubicompforall.CityExplorer;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * This is an example of a {@link android.app.Application} class.  Ordinarily you would use
  * a class like this as a central repository for information that might be shared between multiple
  * activities.
  * In this case, we have not defined any specific work for this Application.
  * See samples/UbiTerms/tests/src/org.ubicompforall.ubiterms/ApiDemosApplicationTests for an example
  * of how to perform unit tests on an Application object.
  */
 public class CityExplorer extends Application{ // implements LocationListener // For GPS
 
 	public static final int DEBUG = 1;
     public static final String C = "CityExplorer";
 	public static final String GENERAL_SETTINGS = "SETTINGS";
 
 	// DEFAULT GEO-POINT for first map view
 	public static final String LAT = "Lat";
 	public static final String LNG = "Long";
 	public static final int TRONDHEIM_LAT = 63430396;  // Trondheim Torvet 63°25′49″N ;
 	public static final int TRONDHEIM_LNG = 10395041;  //Trondheim Torvet 10°23′42″E ;
 	public static final String URL = "Url";
 //	public static final String RUNE_URL 		JF: moved to @string/default_url
 //	 = "http://www.idi.ntnu.no/~satre/ubicomp/cityexplorer/"; //CityExplorer.sqlite
 
 	public static final int TYPE_ALL = 0;
 	public static final int TYPE_FREE = 1;
 	public static final int TYPE_FIXED = 2;
 
 	@Override
     public void onCreate() {
         /*
          * This populates the default values from the preferences XML file. See
          * {@link DefaultValues} for more details.
          */
         PreferenceManager.setDefaultValues( this, R.xml.default_values, false);
 
         //Local debug (stacktrace level = 3)
         StackTraceElement st = Thread.currentThread().getStackTrace()[2];
 		Log.d(C, st.getFileName().replace("java", "")+st.getLineNumber()+'~'+st.getMethodName()+'~'+"Start CityExplorer.java" );
 
 		//initGPS(); //RS-111122 Move to CityExplorer.java Application (Common for all activities) ? When?
 		// And what about DB-loading?  Here? ...
 		
     }//onCreate
 
     @Override
     public void onTerminate() {
     	//TODO: save preferences now???
 
         //Local debug (stacktrace level = 3)
         StackTraceElement st = Thread.currentThread().getStackTrace()[2];
 		Log.d(C, st.getFileName().replace("java", "")+st.getLineNumber()+'~'+st.getMethodName()+'~'+"Save the preferences now?");
     }//onTerminate
 
 
     
     /***
 	 * Debug method to include the filename, line-number and method of the caller
 	 */
 	public static void debug(int level, String message) {
 		if (DEBUG >= level) {
 			StackTraceElement st = Thread.currentThread().getStackTrace()[4];
 			//Log.d(C, st.getFileName().replace("java", "")+st.getLineNumber()+'~'+st.getMethodName()+'~'+message );
 			Log.d(C, st.getMethodName()+'~'+message+" at ("+st.getFileName()+":"+st.getLineNumber()+")" );
 		} // if verbosity is high enough
 	} // debug
 
 
     public static boolean isConnected( Context context ) {
         ConnectivityManager connectivityManager = (ConnectivityManager)
             context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = null;
         if (connectivityManager != null) {
             networkInfo = connectivityManager.getActiveNetworkInfo();
         }
         if ( networkInfo == null ){
 			Toast.makeText( context, R.string.map_wifi_disabled_toast, Toast.LENGTH_LONG).show();
         	return false; //Network is not enabled
         }else{
         	return networkInfo.getState() == NetworkInfo.State.CONNECTED ? true : false ;
         }
     } // isConnected
     
 
     /**
      * Display a dialog that user has no Internet connection
      * @param ctx1
      *
      * Code from: http://osdir.com/ml/Android-Developers/2009-11/msg05044.html
      */
     public static void showNoConnectionDialog(Context context) {
         final Context ctx = context;	// Protect original context!
         AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
         builder.setCancelable(true);
         builder.setMessage( R.string.no_connection );
         builder.setTitle( R.string.no_connection_title );
         builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 ctx.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 return;
             }
         });
         builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                 return;
             }
         });
 
         builder.show();
     } // showNoConnectionDialog
 
 
 /* Not valid for Application ?!!
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		System.getProperty("java.version");
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 */
 
 }//CityExplorer
 
 /*
 public class DefaultValues extends PreferenceActivity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		//Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.preferences);
         // or (R.xml.default_values) as loaded in the CityExplorer.java Application-class;
     }
 }
 */
