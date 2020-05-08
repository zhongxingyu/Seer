 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.kentph.ttcnextbus;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentSender;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 //import com.example.android.networkusage.StackOverflowXmlParser.Entry;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.kentph.ttcnextbus.NextBusRouteListXmlParser.Route;
 import com.kentph.ttcnextbus.NextBusRouteConfigXmlParser.RouteConfig;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import static android.database.DatabaseUtils.*;
 import static com.kentph.ttcnextbus.NextBusPredictionsXmlParser.Prediction;
 import static com.kentph.ttcnextbus.NextBusPredictionsXmlParser.RoutePredictions;
 import static com.kentph.ttcnextbus.NextBusRouteConfigXmlParser.Path;
 import static com.kentph.ttcnextbus.NextBusRouteConfigXmlParser.PathPoint;
 import static com.kentph.ttcnextbus.RouteDbHelper.RouteDbContract.*;
 
 
 /**
  * Main Activity for the sample application.
  *
  * This activity does the following:
  *
  * o Presents a WebView screen to users. This WebView has a list of HTML links to the latest
  *   questions tagged 'android' on stackoverflow.com.
  *
  * o Parses the StackOverflow XML feed using XMLPullParser.
  *
  * o Uses AsyncTask to download and process the XML feed.
  *
  * o Monitors preferences and the device's network connection to determine whether
  *   to refresh the WebView content.
  */
 public class NetworkActivity extends Activity implements
         GooglePlayServicesClient.ConnectionCallbacks,
         GooglePlayServicesClient.OnConnectionFailedListener {
 
     public static final String WIFI = "Wi-Fi";
     public static final String ANY = "Any";
 //    private static String URL =
 //            "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=ttc&r=";
 //            "http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
 
     // Whether there is a Wi-Fi connection.
     private static boolean wifiConnected = false;
     // Whether there is a mobile connection.
     private static boolean mobileConnected = false;
     // Whether the display should be refreshed.
     public static boolean refreshDisplay = true;
 
     // The user's current network preference setting.
     public static String sPref = null;
 
     // The BroadcastReceiver that tracks network connectivity changes.
     private NetworkReceiver receiver = new NetworkReceiver();
 
     // Global constants
     /*
      * Define a request code to send to Google Play services
      * This code is returned in Activity.onActivityResult
      */
     private final static int
             CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
     /**
      * Show a dialog returned by Google Play services for the
      * connection error code
      *
      * @param errorCode An error code returned from onConnectionFailed
      */
     private void showErrorDialog(int errorCode) {
 
         // Get the error dialog from Google Play services
         Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                 errorCode,
                 this,
                 LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
         // If Google Play services can provide an error dialog
         if (errorDialog != null) {
 
             // Create a new DialogFragment in which to show the error dialog
             ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 
             // Set the dialog in the DialogFragment
             errorFragment.setDialog(errorDialog);
 
             // Show the error dialog in the DialogFragment
             //errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
         }
     }
 
     // Define a DialogFragment that displays the error dialog
     public static class ErrorDialogFragment extends DialogFragment {
         // Global field to contain the error dialog
         private Dialog mDialog;
         // Default constructor. Sets the dialog field to null
         public ErrorDialogFragment() {
             super();
             mDialog = null;
         }
         // Set the dialog to display
         public void setDialog(Dialog dialog) {
             mDialog = dialog;
         }
         // Return a Dialog to the DialogFragment.
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return mDialog;
         }
     }
 
     private boolean servicesConnected() {
         // Check that Google Play services is available
         int resultCode =
                 GooglePlayServicesUtil.
                         isGooglePlayServicesAvailable(this);
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
             // In debug mode, log the status
             Log.d("Location Updates",
                     "Google Play services is available.");
             // Continue
             return true;
             // Google Play services was not available for some reason
         } else {
             // Display an error dialog
             Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
             if (dialog != null) {
                 ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                 errorFragment.setDialog(dialog);
                 //errorFragment.show(getSupportManager(), LocationUtils.APPTAG);
             }
             return false;
         }
     }
 
     /*
      * Called by Location Services when the request to connect the
      * client finishes successfully. At this point, you can
      * request the current location or start periodic updates
      */
     @Override
     public void onConnected(Bundle dataBundle) {
         // Display the connection status
         Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
 
         // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
         // display. For example, if the user has set "Wi-Fi only" in prefs and the
         // device loses its Wi-Fi connection midway through the user using the app,
         // you don't want to refresh the display--this would force the display of
         // an error page instead of stackoverflow.com content.
         if (refreshDisplay) {
             loadPage();
         }
     }
 
     /*
      * Called by Location Services if the connection to the
      * location client drops because of an error.
      */
     @Override
     public void onDisconnected() {
         // Display the connection status
         Toast.makeText(this, "Disconnected. Please re-connect.",
                 Toast.LENGTH_SHORT).show();
     }
 
     /*
      * Called by Location Services if the attempt to
      * Location Services fails.
      */
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         /*
          * Google Play services can resolve some errors it detects.
          * If the error has a resolution, try sending an Intent to
          * start a Google Play services activity that can resolve
          * error.
          */
         if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(
                         this,
                         CONNECTION_FAILURE_RESOLUTION_REQUEST);
                 /*
                  * Thrown if Google Play services canceled the original
                  * PendingIntent
                  */
             } catch (IntentSender.SendIntentException e) {
                 // Log the error
                 e.printStackTrace();
             }
         } else {
             /*
              * If no resolution is available, display a dialog to the
              * user with the error.
              */
             showErrorDialog(connectionResult.getErrorCode());
         }
     }
 
     // Stores the current instantiation of the location client in this object
     private LocationClient mLocationClient;
 
     // Global variable to hold the current location
     Location mCurrentLocation;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Register BroadcastReceiver to track connection changes.
         IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
         receiver = new NetworkReceiver();
         this.registerReceiver(receiver, filter);
 
         /*
          * Create a new location client, using the enclosing class to
          * handle callbacks.
          */
         mLocationClient = new LocationClient(this, this, this);
     }
 
     // Refreshes the display if the network connection and the
     // pref settings allow it.
     @Override
     public void onStart() {
         super.onStart();
 
         // Gets the user's network preference settings
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         // Retrieves a string value for the preferences. The second parameter
         // is the default value to use if a preference value is not found.
         sPref = sharedPrefs.getString("listPref", "Wi-Fi");
 
         updateConnectedFlags();
 
         // Connect the client.
         mLocationClient.connect();
 
 //        // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
 //        // display. For example, if the user has set "Wi-Fi only" in prefs and the
 //        // device loses its Wi-Fi connection midway through the user using the app,
 //        // you don't want to refresh the display--this would force the display of
 //        // an error page instead of stackoverflow.com content.
 //        if (refreshDisplay) {
 //            loadPage();
 //        }
     }
 
     /*
      * Called when the Activity is no longer visible.
      */
     @Override
     protected void onStop() {
         // Disconnecting the client invalidates it.
         mLocationClient.disconnect();
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (receiver != null) {
             this.unregisterReceiver(receiver);
         }
     }
 
     // Checks the network connection and sets the wifiConnected and mobileConnected
     // variables accordingly.
     private void updateConnectedFlags() {
         ConnectivityManager connMgr =
                 (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
         NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
         if (activeInfo != null && activeInfo.isConnected()) {
             wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
             mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
         } else {
             wifiConnected = false;
             mobileConnected = false;
         }
     }
 
     // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
     // This avoids UI lock up. To prevent network operations from
     // causing a delay that results in a poor user experience, always perform
     // network operations on a separate thread from the UI.
     private void loadPage() {
         if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                 || ((sPref.equals(WIFI)) && (wifiConnected))) {
             // AsyncTask subclass
             new DownloadXmlTask().execute();
         } else {
             showErrorPage();
         }
     }
 
     // Displays an error if the app is unable to load content.
     private void showErrorPage() {
         setContentView(R.layout.activity_network);
 
         // The specified network connection is not available. Displays error message.
         WebView myWebView = (WebView) findViewById(R.id.webview);
         myWebView.loadData(getResources().getString(R.string.connection_error),
                 "text/html", null);
 //        TextView textView = (TextView) findViewById(R.id.text);
 //        textView.setText(getResources().getString(R.string.connection_error));
     }
 
     // Populates the activity's options menu.
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.mainmenu, menu);
         return true;
     }
 
     // Handles the user's menu selection.
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
                 Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                 startActivity(settingsActivity);
                 return true;
         case R.id.refresh:
                 loadPage();
                 return true;
         case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     // Location subclass that stores stopId
     public static class StopLocation extends Location {
         public final String stopId;
         public float distance;
 
         public StopLocation (String stopId, double lat, double lon) {
             super("");
             this.stopId = stopId;
             this.setLatitude(lat);
             this.setLongitude(lon);
             this.distance = -1;
         }
     }
 
     // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
     private class DownloadXmlTask extends AsyncTask<Void, String, String> {
 //        private ProgressDialog pd;
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 //            pd = new ProgressDialog(getApplicationContext());
 //            pd.setMessage("Starting...");
 //            pd.show();
         }
 
         @Override
         protected String doInBackground(Void... params) {
             RouteDbAssetHelper mDbHelper = new RouteDbAssetHelper(getApplicationContext());
             // Gets the data repository in write mode
             SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
             // get current location in lat/lon
             if (servicesConnected())
                 mCurrentLocation = mLocationClient.getLastLocation();
             int gridLat = (int) (mCurrentLocation.getLatitude()/0.004);
             int gridLon = (int) (mCurrentLocation.getLongitude()/0.004);
 
             // get all stops withing a 3x3 grid of current locations grid
             String[] whereArgs = new String[] {
                     ""+gridLat, ""+(gridLat+1), ""+(gridLat-1),
                     ""+gridLon, ""+(gridLon+1), ""+(gridLon-1)};
 
             // construct sql select query
             Cursor cursor = db.query("stops", new String[] {"stopid", "lat", "lon"}, "(gridlat=? OR gridlat=? OR gridlat=?) " +
                     "AND (gridlon=? OR gridlon=? OR gridlon=?)",
                     whereArgs, null, null, null, null);
 
             // get column indices
             int stopIdColumn = cursor.getColumnIndex("stopid");
             int latColumn = cursor.getColumnIndex("lat");
             int lonColumn = cursor.getColumnIndex("lon");
 
             List<StopLocation> stopLocations = new ArrayList<StopLocation>();
 
             // fill stopLocations list with each stop obtained in sql query
             if (cursor.moveToFirst())
                 do {
                     StopLocation stopLocation = new StopLocation(
                             cursor.getString(stopIdColumn),
                             cursor.getDouble(latColumn),
                             cursor.getDouble(lonColumn));
                     // calculate distance of stop from current loc and save
                     stopLocation.distance = mCurrentLocation.distanceTo(stopLocation);
                     stopLocations.add(stopLocation);
                 } while (cursor.moveToNext());
 
             // sort stops by distance
             Collections.sort(stopLocations, new Comparator<StopLocation>() {
                 public int compare(StopLocation a, StopLocation b) {
                     float diff = a.distance - b.distance;
                     if (diff > 0) return 1;
                     else if (diff < 0) return -1;
                     else return 0;
                 }
             });
 
             try {
                 InputStream stream = null;
                 NextBusPredictionsXmlParser nextBusPredictionsXmlParser = new NextBusPredictionsXmlParser();
 
 //        String routeNumber = null; // Split routeTitle into Number and Name
 //        String routeName = null;
 //        String stopTitle = null;  // Otherwise match output attrs exactly
 //        // attrs from direction tag
 //        String direction = null;
 //        String terminal = null;
 //
                List<List<RoutePredictions>> listOfRoutePredictions = null;
 
                 StringBuilder htmlString = new StringBuilder();
 
                 // get predictions from nextbus
                 try {
                     for (StopLocation stopLocation : stopLocations) {
                         stream = downloadUrl("http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=ttc&stopId=" + stopLocation.stopId);
                         listOfRoutePredictions.add(nextBusPredictionsXmlParser.parse(stream));
                     }
 
                     // Makes sure that the InputStream is closed after the app is
                     // finished using it.
                 } finally {
                     if (stream != null) {
                         stream.close();
                     }
                 }
 
                 // print predictions
                 int count = 0;
                 for (List<RoutePredictions> routePredictions : listOfRoutePredictions) {
                     htmlString.append("<h1>Stop ");
                     htmlString.append(stopLocations.get(count).stopId);
                     htmlString.append("</h1>");
                     count++;
                     for (RoutePredictions routePrediction : routePredictions) {
                         htmlString.append("<h4>Route number: ");
                         htmlString.append(routePrediction.routeNumber);
 //                        htmlString.append("</h1>");
                         htmlString.append(" | Route name: ");
                         htmlString.append(routePrediction.routeName);
 //                        htmlString.append("</h4>");
                         htmlString.append(" | Stop title: ");
                         htmlString.append(routePrediction.stopTitle);
 //                        htmlString.append("</h4>");
                         htmlString.append(" | Direction: ");
                         htmlString.append(routePrediction.direction);
 //                        htmlString.append("</h4>");
                         htmlString.append(" | Terminal: ");
                         htmlString.append(routePrediction.terminal);
                         htmlString.append("</h4>");
 
                         for (Prediction prediction : routePrediction.listOfPredictions) {
 //                            htmlString.append("<p>seconds: ");
 //                            htmlString.append(prediction.seconds);
 //                            htmlString.append("</p>");
                             htmlString.append("<p>minutes: ");
                             htmlString.append(prediction.minutes);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Departure: ");
 //                            htmlString.append(prediction.isDeparture);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Layover: ");
 //                            htmlString.append(prediction.affectedByLayover);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Branch: ");
 //                            htmlString.append(prediction.branch);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Direction tag: ");
 //                            htmlString.append(prediction.dirTag);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Vehicle: ");
 //                            htmlString.append(prediction.vehicle);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Block: ");
 //                            htmlString.append(prediction.block);
 //                            htmlString.append("</p>");
 //                            htmlString.append("<p>Trip tag: ");
 //                            htmlString.append(prediction.tripTag);
 //                            htmlString.append("</p>");
                         }
                     }
                 }
 
                 htmlString.append("<p>Done</p>");
                 return htmlString.toString();
             } catch (IOException e) {
                 return getResources().getString(R.string.connection_error);
             } catch (XmlPullParserException e) {
                 return getResources().getString(R.string.xml_error);
             }
         }
 
         @Override
         protected void onProgressUpdate(String... values) {
 //            pd.setMessage("Getting route " + values[0]);
 //            pd.show();
         }
 
         @Override
         protected void onPostExecute(String result) {
             setContentView(R.layout.activity_network);
             // Displays the HTML string in the UI via a WebView
             WebView myWebView = (WebView) findViewById(R.id.webview);
             myWebView.loadData(result, "text/html", null);
 //            pd.setMessage("Done!");
 //            pd.show();
         }
     }
 
     // Given a string representation of a URL, sets up a connection and gets
     // an input stream.
     private InputStream downloadUrl(String urlString) throws IOException {
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setReadTimeout(10000 /* milliseconds */);
         conn.setConnectTimeout(15000 /* milliseconds */);
         conn.setRequestMethod("GET");
         conn.setDoInput(true);
         // Starts the query
         conn.connect();
         InputStream stream = conn.getInputStream();
         return stream;
     }
 
     /**
      *
      * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
      * which indicates a connection change. It checks whether the type is TYPE_WIFI.
      * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
      * main activity accordingly.
      *
      */
     public class NetworkReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             ConnectivityManager connMgr =
                     (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
             // Checks the user prefs and the network connection. Based on the result, decides
             // whether
             // to refresh the display or keep the current display.
             // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
             if (WIFI.equals(sPref) && networkInfo != null
                     && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                 // If device has its Wi-Fi connection, sets refreshDisplay
                 // to true. This causes the display to be refreshed when the user
                 // returns to the app.
                 refreshDisplay = true;
                 Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
 
                 // If the setting is ANY network and there is a network connection
                 // (which by process of elimination would be mobile), sets refreshDisplay to true.
             } else if (ANY.equals(sPref) && networkInfo != null) {
                 refreshDisplay = true;
 
                 // Otherwise, the app can't download content--either because there is no network
                 // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
                 // is no Wi-Fi connection.
                 // Sets refreshDisplay to false.
             } else {
                 refreshDisplay = false;
                 Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
             }
         }
     }
 }
