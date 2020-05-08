 package com.kentph.ttcnextbus;
 
 //import android.app.ActionBar;
 //import android.app.FragmentTransaction;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentSender;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.app.ActionBar;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class PredictionsActivity extends ActionBarActivity
         implements ActionBar.TabListener, PredictionsTransferInterface,
         GooglePlayServicesClient.ConnectionCallbacks,
         GooglePlayServicesClient.OnConnectionFailedListener {
 
     // NETWORK STUFF
 
     // List of predictions obtained from network AsyncTask
     List<List<NextBusPredictionsXmlParser.RoutePredictions>> CurrListOfRoutePredictions = null;
 
     public static final String WIFI = "Wi-Fi";
     public static final String ANY = "Any";
 
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
             getPredictions();
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
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
      * derivative, which will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     AppSectionsPagerAdapter mAppSectionsPagerAdapter;
 
     /**
      * The {@link android.support.v4.view.ViewPager} that will display the three primary sections of the app, one at a
      * time.
      */
     ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getSupportActionBar();
 
         // Specify that the Home/Up button should not be enabled, since there is no hierarchical
         // parent.
         actionBar.setHomeButtonEnabled(false);
 
         // Specify that we will be displaying tabs in the action bar.
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager, attaching the adapter and setting up a listener for when the
         // user swipes between sections.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mAppSectionsPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
 
         // Hide action bar for API 11+
         if (Build.VERSION.SDK_INT >= 11) {
             actionBar.setDisplayShowHomeEnabled(false);
             actionBar.setDisplayShowTitleEnabled(false);
         }
 
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
 //            getPredictions();
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
     private void getPredictions() {
         if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                 || ((sPref.equals(WIFI)) && (wifiConnected))) {
             // AsyncTask subclass
             new DownloadXmlTask().execute();
         } else {
             showErrorPage();
         }
     }
 
     // Displays an error if the app is unable to load content. TODO also send intent
     private void showErrorPage() {
 //        setContentView(R.layout.activity_network);
 //
 //        // The specified network connection is not available. Displays error message.
 //        WebView myWebView = (WebView) findViewById(R.id.webview);
 //        myWebView.loadData(getResources().getString(R.string.connection_error),
 //                "text/html", null);
 //        TextView textView = (TextView) findViewById(R.id.text);
 //        textView.setText(getResources().getString(R.string.connection_error));
     }
 
 //    // Populates the activity's options menu.
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        MenuInflater inflater = getMenuInflater();
 //        inflater.inflate(R.menu.mainmenu, menu);
 //        return true;
 //    }
 
 //    // Handles the user's menu selection.
 //    @Override
 //    public boolean onOptionsItemSelected(MenuItem item) {
 //        switch (item.getItemId()) {
 //            case R.id.settings:
 //                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
 //                startActivity(settingsActivity);
 //                return true;
 //            case R.id.refresh:
 //                getPredictions();
 //                return true;
 //            case android.R.id.home:
 //                // This ID represents the Home or Up button. In the case of this
 //                // activity, the Up button is shown. Use NavUtils to allow users
 //                // to navigate up one level in the application structure. For
 //                // more details, see the Navigation pattern on Android Design:
 //                //
 //                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
 //                //
 //                NavUtils.navigateUpFromSameTask(this);
 //                return true;
 //            default:
 //                return super.onOptionsItemSelected(item);
 //        }
 //    }
 
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
     private class DownloadXmlTask extends AsyncTask<Void, String,
             List<List<NextBusPredictionsXmlParser.RoutePredictions>>> {
         //        private ProgressDialog pd;
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 //            pd = new ProgressDialog(getApplicationContext());
 //            pd.setMessage("Starting...");
 //            pd.show();
         }
 
         @Override
         protected List<List<NextBusPredictionsXmlParser.RoutePredictions>>
         doInBackground(Void... params) {
             RouteDbAssetHelper mDbHelper = new RouteDbAssetHelper(getApplicationContext());
             // Gets the data repository in write mode
             SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
             // get current location in lat/lon TODO exit if lost connection
             if (servicesConnected())
                 mCurrentLocation = mLocationClient.getLastLocation();
             int gridLat = (int) (mCurrentLocation.getLatitude()/0.004);
             int gridLon = (int) (mCurrentLocation.getLongitude()/0.004);
 
             // get all stops withing a 3x3 grid of current locations grid TODO make grid smaller
             String[] whereArgs = new String[] {""+(gridLat-1), ""+(gridLat+1), ""+(gridLon+1), ""+(gridLon-1)};
 
             // construct sql select query
             Cursor cursor = db.query("stops", new String[] {"stopid", "lat", "lon"}, "(gridlat BETWEEN ? AND ?) " +
                     "AND (gridlon BETWEEN ? AND ?)",
                     whereArgs, null, null, null, null);
 
             int rows = cursor.getCount()/2; //TODO fix why doubled cursor
             int count = 0;
 
             // get column indices
             int stopIdColumn = cursor.getColumnIndex("stopid");
             int latColumn = cursor.getColumnIndex("lat");
             int lonColumn = cursor.getColumnIndex("lon");
 
             List<StopLocation> stopLocations = new ArrayList<StopLocation>();
 
             // fill stopLocations list with each stop obtained in sql query
             if (cursor.moveToFirst())
                 while (count < rows) {
                     StopLocation stopLocation = new StopLocation(
                             cursor.getString(stopIdColumn),
                             cursor.getDouble(latColumn),
                             cursor.getDouble(lonColumn));
                     // calculate distance of stop from current loc and save
                     stopLocation.distance = mCurrentLocation.distanceTo(stopLocation);
                     stopLocations.add(stopLocation);
                     cursor.moveToNext(); // brute force chop list in half
                     count++;
                 }
 
             // sort stops by distance
             Collections.sort(stopLocations, new Comparator<StopLocation>() {
                 public int compare(StopLocation a, StopLocation b) {
                     float diff = a.distance - b.distance;
                     if (diff > 0) return 1;
                     else if (diff < 0) return -1;
                     else return 0;
                 }
             });
 
             InputStream stream = null;
             NextBusPredictionsXmlParser nextBusPredictionsXmlParser =
                     new NextBusPredictionsXmlParser();
 
             List<List<NextBusPredictionsXmlParser.RoutePredictions>> listOfRoutePredictions =
                     new ArrayList<List<NextBusPredictionsXmlParser.RoutePredictions>>();
 
             StringBuilder htmlString = new StringBuilder();
 
             // get predictions from nextbus for 5 closest stops
             try {
                 try {
                     for (StopLocation stopLocation : stopLocations.subList(0, 5)) {    // TODO make user settable
                         stream = downloadUrl(
                                 "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=ttc&stopId=" +
                                         stopLocation.stopId);
                         listOfRoutePredictions.add(nextBusPredictionsXmlParser.parse(stream));
                     }
                     // Makes sure that the InputStream is closed after the app is
                     // finished using it.
                 } finally {
                     if (stream != null) {
                         stream.close();
                     }
                 }
             } catch (IOException e) {
                 return null;
             } catch (XmlPullParserException e) {
                 return null;
             }
             // print predictions
 //                count = 0;
 //                for (List<NextBusPredictionsXmlParser.RoutePredictions> routePredictions : listOfRoutePredictions) {
 //                    htmlString.append("<h1>Stop ");
 //                    htmlString.append(stopLocations.get(count).stopId);
 //                    htmlString.append("</h1>");
 //                    count++;
 //                    for (NextBusPredictionsXmlParser.RoutePredictions routePrediction : routePredictions) {
 //                        htmlString.append("<h4>Route number: ");
 //                        htmlString.append(routePrediction.routeNumber);
 ////                        htmlString.append("</h1>");
 //                        htmlString.append(" | Route name: ");
 //                        htmlString.append(routePrediction.routeName);
 ////                        htmlString.append("</h4>");
 //                        htmlString.append(" | Stop title: ");
 //                        htmlString.append(routePrediction.stopTitle);
 ////                        htmlString.append("</h4>");
 //                        htmlString.append(" | Direction: ");
 //                        htmlString.append(routePrediction.direction);
 ////                        htmlString.append("</h4>");
 //                        htmlString.append(" | Terminal: ");
 //                        htmlString.append(routePrediction.terminal);
 //                        htmlString.append("</h4>");
 //
 //                        for (NextBusPredictionsXmlParser.Prediction prediction : routePrediction.listOfPredictions) {
 ////                            htmlString.append("<p>seconds: ");
 ////                            htmlString.append(prediction.seconds);
 ////                            htmlString.append("</p>");
 //                            htmlString.append("<p>minutes: ");
 //                            htmlString.append(prediction.minutes);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Departure: ");
 ////                            htmlString.append(prediction.isDeparture);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Layover: ");
 ////                            htmlString.append(prediction.affectedByLayover);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Branch: ");
 ////                            htmlString.append(prediction.branch);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Direction tag: ");
 ////                            htmlString.append(prediction.dirTag);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Vehicle: ");
 ////                            htmlString.append(prediction.vehicle);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Block: ");
 ////                            htmlString.append(prediction.block);
 ////                            htmlString.append("</p>");
 ////                            htmlString.append("<p>Trip tag: ");
 ////                            htmlString.append(prediction.tripTag);
 ////                            htmlString.append("</p>");
 //                        }
 //                    }
 //                }
 
 //                htmlString.append("<p>Done</p>");
 
 //                return htmlString.toString();
             return listOfRoutePredictions;
 
         }
 
 //        /**
 //         * Parcelable class for List<List<NextBusPredictionsXmlParser.RoutePredictions>>
 //         */
 //        public class MultiStopRoutePredictions implements Parcelable {
 //            private int mData;
 //
 //            @Override
 //            public int describeContents() {
 //                return 0;
 //            }
 //
 //            @Override
 //            public void writeToParcel(Parcel out, int flags) {
 //                out.writeInt(mData);
 //            }
 //
 //            public static final Parcelable.Creator<MultiStopRoutePredictions> CREATOR
 //                    = new Parcelable.Creator<MultiStopRoutePredictions>() {
 //                public MultiStopRoutePredictions createFromParcel(Parcel in) {
 //                    return new MultiStopRoutePredictions(in);
 //                }
 //
 //                public MultiStopRoutePredictions[] newArray(int size) {
 //                    return new MultiStopRoutePredictions[size];
 //                }
 //            };
 //
 //            private MultiStopRoutePredictions(Parcel in) {
 //                mData = in.readInt();
 //            }
 //        }
 
         @Override
         protected void onProgressUpdate(String... values) {
 //            pd.setMessage("Getting route " + values[0]);
 //            pd.show();
         }
 
         @Override
         protected void onPostExecute(List<List<NextBusPredictionsXmlParser.RoutePredictions>> result) {
             CurrListOfRoutePredictions = result;
             // TODO instantiate new fragments
 //            pd.setMessage("Done!");
 //            pd.show();
         }
     }
 
 
     /**
      * Pass RoutePredictions from AsyncTask to any fragments of same Activity.
      * @return field containing most current list of route predictions.
      */
     public List<List<NextBusPredictionsXmlParser.RoutePredictions>> transferPredictions() {
         return CurrListOfRoutePredictions;
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
 
     // END NETWORK STUFF
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.predictions_activity_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             case R.id.action_refresh:
                 getPredictions();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 
         public AppSectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             switch (i) {
                 case 0:
                     // The first section of the app is the most interesting -- it offers
                     // a launchpad into the other demonstrations in this example application.
                     return new RoutePredictionsListFragment();
 
                 default:
                     // The other sections of the app are dummy placeholders.
                     Fragment fragment = new DummySectionFragment();
                     Bundle args = new Bundle();
                     args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                     fragment.setArguments(args);
                     return fragment;
             }
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return "Section " + (position + 1);
         }
     }
 
 //    /**
 //     * A fragment that launches other parts of the demo application. TODO RoutePredictionsListFragment
 //     */
 //    public static class LaunchpadSectionFragment extends Fragment {
 //
 //        @Override
 //        public View onCreateView(LayoutInflater inflater, ViewGroup container,
 //                                 Bundle savedInstanceState) {
 //            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);
 //
 //            // Demonstration of a collection-browsing activity.
 //            rootView.findViewById(R.id.demo_collection_button)
 //                    .setOnClickListener(new View.OnClickListener() {
 //                        @Override
 //                        public void onClick(View view) {
 //                            Intent intent = new Intent(getActivity(), CollectionDemoActivity.class);
 //                            startActivity(intent);
 //                        }
 //                    });
 //
 //            // Demonstration of navigating to external activities.
 //            rootView.findViewById(R.id.demo_external_activity)
 //                    .setOnClickListener(new View.OnClickListener() {
 //                        @Override
 //                        public void onClick(View view) {
 //                            // Create an intent that asks the user to pick a photo, but using
 //                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
 //                            // the application from the device home screen does not return
 //                            // to the external activity.
 //                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
 //                            externalActivityIntent.setType("image/*");
 //                            externalActivityIntent.addFlags(
 //                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 //                            startActivity(externalActivityIntent);
 //                        }
 //                    });
 //
 //            return rootView;
 //        }
 //    }
 
     /**
      * A dummy fragment representing a section of the app, but that simply displays dummy text. TODO
      */
     public static class DummySectionFragment extends Fragment {
 
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
             Bundle args = getArguments();
             ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                     getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
             return rootView;
         }
     }
 }
