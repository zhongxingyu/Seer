 package com.android.systemui.statusbar.quicksettings.quicktile;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.AsyncTask;
 import android.provider.Settings;
 
 import android.util.Log;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import org.w3c.dom.Document;
 import android.text.format.DateFormat;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import com.android.internal.util.weather.HttpRetriever;
 import com.android.internal.util.weather.WeatherInfo;
 import com.android.internal.util.weather.WeatherXmlParser;
 import com.android.internal.util.weather.YahooPlaceFinder;
 import android.view.View;
 
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.systemui.R;
 import com.android.systemui.statusbar.CmStatusBarView;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsContainerView;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsController;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class WeatherTile extends QuickSettingsTile {
 
     private static final String TAG = "WeatherTile";
     private static final boolean DEBUG = false;
 
     private static final String ACTION_LOC_UPDATE = "com.android.systemui.action.LOCATION_UPDATE";
 
     private static final long MIN_LOC_UPDATE_INTERVAL = 15 * 60 * 1000; /* 15 minutes */
     private static final float MIN_LOC_UPDATE_DISTANCE = 5000f; /* 5 km */
 
     private LocationManager mLocManager;
     private ConnectivityManager mConnM;
     private NetworkInfo mInfo;
 
     private String tempC;
     private String timed;
     private String humY;
     private String date;
     private String time;
     private String mLoc;
     private String mDate;
     private Drawable drwb;
     private boolean addDrwb = false;
     private boolean updating = false;
     private boolean mForceRefresh;
     private PendingIntent mLocUpdateIntent;
 
     public WeatherTile(Context context, LayoutInflater inflater,
             QuickSettingsContainerView container, QuickSettingsController qsc) {
         super(context, inflater, container, qsc);
 
         mTileLayout = R.layout.quick_settings_tile_weather;
 
         mLocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
         mConnM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         mInfo = mConnM.getActiveNetworkInfo();
         mLocUpdateIntent = PendingIntent.getService(context, 0, new Intent(ACTION_LOC_UPDATE), 0);
         mForceRefresh = false;
 
         mOnClick = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 updateLocationListenerState();
                 mForceRefresh = true;
                 refreshWeather();
             }
         };
         mOnLongClick = new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 CmStatusBarView.runCMSettings("com.cyanogenmod.cmparts.activities.UIWeatherActivity", mContext);
                 startCollapseActivity();
                 return true;
             }
         };
         qsc.registerAction(Intent.ACTION_TIME_CHANGED, this);
         qsc.registerAction(Intent.ACTION_TIMEZONE_CHANGED, this);
         qsc.registerAction(Intent.ACTION_CONFIGURATION_CHANGED, this);
         qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.WEATHER_CUSTOM_LOCATION)
                 , this);
         qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.WEATHER_USE_CUSTOM_LOCATION)
                 , this);
         qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.WEATHER_UPDATE_INTERVAL)
                 , this);
     }
 
     @Override
     void onPostCreate() {
         updateLocationListenerState();
         super.onPostCreate();
     }
 
     @Override
     public void onChangeUri(ContentResolver resolver, Uri uri) {
         updateLocationListenerState();
         refreshWeather();
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
         if (action.equals(ACTION_LOC_UPDATE)) {
             Location location = (Location) intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
             triggerLocationQueryWithLocation(location);
         } else if (action.equals(Intent.ACTION_TIME_CHANGED) || 
                     action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                     action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
             updateLocationListenerState();
         }
         refreshWeather();
     }
 
     //===============================================================================================
     // Weather related functionality
     //===============================================================================================
     private static final String URL_YAHOO_API_WEATHER = "http://weather.yahooapis.com/forecastrss?w=%s&u=";
     private static WeatherInfo mWeatherInfo = new WeatherInfo();
     private WeatherQueryTask mWeatherQueryTask;
     private LocationQueryTask mLocationQueryTask;
     private LocationInfo mLocationInfo = new LocationInfo();
     private String mLastKnownWoeid;
     private boolean mNeedsWeatherRefresh;
     private Set<String> mTrackedProviders;
     private long mGetLastSync = 0;
 
     private void updateLocationListenerState() {
         if (mInfo == null || !mInfo.isConnected()) {
             return;
         }
 
         final ContentResolver resolver = mContext.getContentResolver();
         boolean useCustomLoc = Settings.System.getInt(resolver,
                                 Settings.System.WEATHER_USE_CUSTOM_LOCATION, 0) == 1;
         String customLoc = Settings.System.getString(resolver,
                                     Settings.System.WEATHER_CUSTOM_LOCATION);
         final long interval = Settings.System.getLong(resolver,
                     Settings.System.WEATHER_UPDATE_INTERVAL, 0); // Default to manual
         if ((((System.currentTimeMillis() - mGetLastSync) / 60000) >= interval)) {
             if (useCustomLoc && customLoc != null) {
                 mLocManager.removeUpdates(mLocUpdateIntent);
                 mLocationInfo.customLocation = customLoc;
                 triggerLocationQueryWithLocation(null);
             } else {
                 mTrackedProviders = getTrackedProviders();
                 List<String> locationProviders = mLocManager.getProviders(true);
                 for (String providerName : locationProviders) {
                      if (mTrackedProviders.contains(providerName)) {
                          mLocManager.requestLocationUpdates(providerName, MIN_LOC_UPDATE_INTERVAL,
                                 MIN_LOC_UPDATE_DISTANCE, mLocUpdateIntent);
                          triggerLocationQueryWithLocation(mLocManager.getLastKnownLocation(providerName));
                      }
                 }
                 mLocationInfo.customLocation = null;
             }
         }
     }
 
     private Set<String> getTrackedProviders() {
         Set<String> providerSet = new HashSet<String>();
 
         if (trackGPS()) {
             providerSet.add(LocationManager.GPS_PROVIDER);
         }
         if (trackNetwork()) {
             providerSet.add(LocationManager.NETWORK_PROVIDER);
         }
         return providerSet;
     }
 
     private boolean trackNetwork() {
         return true;
     }
 
     private boolean trackGPS() {
         return true;
     }
 
     private void triggerLocationQueryWithLocation(Location location) {
         if (mInfo == null || !mInfo.isConnected()) {
             return;
         }
 
         if (location != null) {
             mLocationInfo.location = location;
         }
         if (mLocationQueryTask != null) {
             mLocationQueryTask.cancel(true);
         }
         mLocationQueryTask = new LocationQueryTask();
         mLocationQueryTask.execute(mLocationInfo);
     }
 
     private boolean triggerWeatherQuery(boolean force) {
         if (mInfo == null || !mInfo.isConnected()) {
             return false;
         }
 
         if (!force) {
             if (mLocationQueryTask != null && mLocationQueryTask.getStatus() != AsyncTask.Status.FINISHED) {
                 /* the location query task will trigger the weather query */
                 return true;
             }
         }
         if (mWeatherQueryTask != null) {
             if (force) {
                 mWeatherQueryTask.cancel(true);
             } else if (mWeatherQueryTask.getStatus() != AsyncTask.Status.FINISHED) {
                 return false;
             }
         }
         mWeatherQueryTask = new WeatherQueryTask();
         mWeatherQueryTask.execute(mLastKnownWoeid);
         return true;
     }
 
     private static class LocationInfo {
         Location location;
         String customLocation;
     }
 
     private class LocationQueryTask extends AsyncTask<LocationInfo, Void, String> {
         @Override
         protected String doInBackground(LocationInfo... params) {
             LocationInfo info = params[0];
 
             try {
                 if (info.customLocation != null) {
                     String woeid = YahooPlaceFinder.GeoCode(
                             mContext, info.customLocation);
                     if (DEBUG)
                         Log.d(TAG, "Yahoo location code for " + info.customLocation + " is " + woeid);
                     return woeid;
                 } else if (info.location != null) {
                     String woeid = YahooPlaceFinder.reverseGeoCode(mContext,
                             info.location.getLatitude(), info.location.getLongitude());
                     if (DEBUG)
                         Log.d(TAG, "Yahoo location code for geolocation " + info.location + " is " + woeid);
                     return woeid;
                 }
             } catch (Exception e) {
                 Log.e(TAG, "ERROR: Could not get Location code", e);
                 mNeedsWeatherRefresh = true;
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(String woeid) {
             mLastKnownWoeid = woeid;
             triggerWeatherQuery(true);
         }
     }
 
     private class WeatherQueryTask extends AsyncTask<String, Void, WeatherInfo> {
         private Document getDocument(String woeid) throws IOException {
             final boolean celsius = Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.WEATHER_USE_METRIC, 1) == 1;
             final String urlWithUnit = URL_YAHOO_API_WEATHER + (celsius ? "c" : "f");
             return new HttpRetriever().getDocumentFromURL(String.format(urlWithUnit, woeid));
         }
 
         private WeatherInfo parseXml(Document doc) {
             WeatherXmlParser parser = new WeatherXmlParser(mContext);
             return parser.parseWeatherResponse(doc);
         }
 
         @Override
         protected WeatherInfo doInBackground(String... params) {
             String woeid = params[0];
 
             if (DEBUG)
                 Log.d(TAG, "Querying weather for woeid " + woeid);
 
             if (woeid != null) {
                 try {
                     return parseXml(getDocument(woeid));
                 } catch (Exception e) {
                     Log.e(TAG, "ERROR: Could not parse weather return info", e);
                     mNeedsWeatherRefresh = true;
                 }
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(WeatherInfo info) {
             if (info != null) {
                 setWeatherData(info);
                 mWeatherInfo = info;
             } else if (mWeatherInfo.getTemp() == 0) {
                 setNoWeatherData();
             } else {
                 setWeatherData(mWeatherInfo);
             }
         }
     }
 
     /**
      * Reload the weather forecast
      */
     private void refreshWeather() {
         if (mInfo == null || !mInfo.isConnected()) {
             return;
         }
 
         final ContentResolver resolver = mContext.getContentResolver();
         final long interval = Settings.System.getLong(resolver,
                 Settings.System.WEATHER_UPDATE_INTERVAL, 0); // Default to manual
         boolean manualSync = (interval == 0);
        if (mForceRefresh || (!manualSync && (((System.currentTimeMillis() - mGetLastSync) / 60000) >= interval))) {
             updating = true;
             updateQuickSettings();
             if (triggerWeatherQuery(false)) {
                 mForceRefresh = false;
             }
         } else if (manualSync && mGetLastSync == 0) {
             setNoWeatherData();
         } else {
             setWeatherData(mWeatherInfo);
         }
     }
 
     /**
      * Display the weather information
      * @param w
      */
     private void setWeatherData(WeatherInfo w) {
         mGetLastSync = System.currentTimeMillis();
         final Resources res = mContext.getResources();
         if (w.getConditionResource() != 0) {
             addDrwb = true;
             drwb = res.getDrawable(w.getConditionResource());
         } else {
             addDrwb = false;
         }
         mLabel = (w.getFormattedTemperature() + " | " + w.getFormattedHumidity()) ;
         mLoc = w.getCity();
         date = DateFormat.getDateFormat(mContext).format(w.getTimestamp());
         time = DateFormat.getTimeFormat(mContext).format(w.getTimestamp());
         mDate = (date + " " + time);
         updateQuickSettings();
     }
 
     @Override
     void updateQuickSettings() {
         ImageView mWeatherImage = (ImageView) mTile.findViewById(R.id.weather_image);
         if (mWeatherImage != null) {
             if (addDrwb) {
                 mWeatherImage.setImageDrawable(drwb);
             } else {
                 mWeatherImage.setImageResource(com.android.internal.R.drawable.weather_na);
             }
         }
         TextView mWeatherTemp = (TextView) mTile.findViewById(R.id.weatherone_textview);
         TextView mWeatherLoc = (TextView) mTile.findViewById(R.id.weatherthree_textview);
         TextView mWeatherUpdateTime = (TextView) mTile.findViewById(R.id.weathertwo_textview);
         if ((mWeatherTemp != null) && (mWeatherUpdateTime != null) && (mWeatherLoc != null)) {
             if (updating) {
                 mWeatherTemp.setText(com.android.internal.R.string.weather_refreshing);
                 mWeatherUpdateTime.setVisibility(View.GONE);
                 mWeatherLoc.setVisibility(View.GONE);
                 updating = false;
             } else {
                 if (!addDrwb) {
                     mWeatherTemp.setText(com.android.internal.R.string.weather_tap_to_refresh);
                     mWeatherUpdateTime.setVisibility(View.GONE);
                     mWeatherLoc.setVisibility(View.GONE);
                 } else {
                     mWeatherTemp.setText(mLabel);
                     mWeatherLoc.setVisibility(View.VISIBLE);
                     mWeatherLoc.setText(mLoc);
                     mWeatherUpdateTime.setVisibility(View.VISIBLE);
                     mWeatherUpdateTime.setText(mDate);
                 }
             }
         }
         flipTile();
     }
 
     /**
      * There is no data to display, display 'empty' fields and the
      * 'Tap to reload' message
      */
     private void setNoWeatherData() {
         mLabel = mContext.getString(com.android.internal.R.string.weather_tap_to_refresh);
         addDrwb = false;
         updateQuickSettings();
     }
 }
