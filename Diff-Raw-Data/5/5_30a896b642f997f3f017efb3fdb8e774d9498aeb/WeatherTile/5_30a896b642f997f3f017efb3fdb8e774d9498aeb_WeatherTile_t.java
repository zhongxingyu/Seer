 package com.android.systemui.statusbar.quicksettings.quicktile;
 
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
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
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
 
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.systemui.R;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsContainerView;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsController;
 
 public class WeatherTile extends QuickSettingsTile {
 
     private static final String TAG = "WeatherTile";
     private static final boolean DEBUG = false;
 
     private String tempC;
     private String timed;
     private String humY;
     private String date;
     private String time;
     private Drawable drwb;
     private boolean addDrwb = false;
     private boolean updating = false;
 
     public WeatherTile(Context context, LayoutInflater inflater,
             QuickSettingsContainerView container, QuickSettingsController qsc) {
         super(context, inflater, container, qsc);
 
         mTileLayout = R.layout.quick_settings_tile_weather;
 
         mOnClick = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 updating = true;
                 updateQuickSettings();
                if (!mWeatherRefreshing && !mHandler.hasMessages(QUERY_WEATHER)) {
                     mHandler.sendEmptyMessage(QUERY_WEATHER);
                 }
             }
         };
         mOnLongClick = new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 Intent intent = new Intent(Intent.ACTION_MAIN);
                 intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.UIWeatherActivity");
                 startSettingsActivity(intent);
                 return true;
             }
         };
         qsc.registerAction(Intent.ACTION_TIME_CHANGED, this);
         qsc.registerAction(Intent.ACTION_TIMEZONE_CHANGED, this);
     }
 
     @Override
     void onPostCreate() {
         refreshWeather();
         super.onPostCreate();
     }
 
     @Override
     public void onChangeUri(ContentResolver resolver, Uri uri) {
         refreshWeather();
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                 action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
             refreshWeather();
         }
     }
 
     /*
      * CyanogenMod Lock screen Weather related functionality
      */
     private static final String URL_YAHOO_API_WEATHER = "http://weather.yahooapis.com/forecastrss?w=%s&u=";
     private static WeatherInfo mWeatherInfo = new WeatherInfo();
     private static final int QUERY_WEATHER = 0;
     private static final int UPDATE_WEATHER = 1;
     private boolean mWeatherRefreshing;
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case QUERY_WEATHER:
                 Thread queryWeather = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         LocationManager locationManager = (LocationManager) mContext.
                                 getSystemService(Context.LOCATION_SERVICE);
                         final ContentResolver resolver = mContext.getContentResolver();
                         boolean useCustomLoc = Settings.System.getInt(resolver,
                                 Settings.System.WEATHER_USE_CUSTOM_LOCATION, 0) == 1;
                         String customLoc = Settings.System.getString(resolver,
                                     Settings.System.WEATHER_CUSTOM_LOCATION);
                         String woeid = null;
 
                         // custom location
                         if (customLoc != null && useCustomLoc) {
                             try {
                                 woeid = YahooPlaceFinder.GeoCode(mContext, customLoc);
                                 if (DEBUG)
                                     Log.d(TAG, "Yahoo location code for " + customLoc + " is " + woeid);
                             } catch (Exception e) {
                                 Log.e(TAG, "ERROR: Could not get Location code");
                                 e.printStackTrace();
                             }
                         // network location
                         } else {
                             Criteria crit = new Criteria();
                             crit.setAccuracy(Criteria.ACCURACY_COARSE);
                             String bestProvider = locationManager.getBestProvider(crit, true);
                             Location loc = null;
                             if (bestProvider != null) {
                                 loc = locationManager.getLastKnownLocation(bestProvider);
                             } else {
                                 loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                             }
                             try {
                                 woeid = YahooPlaceFinder.reverseGeoCode(mContext, loc.getLatitude(),
                                         loc.getLongitude());
                                 if (DEBUG)
                                     Log.d(TAG, "Yahoo location code for current geolocation is " + woeid);
                             } catch (Exception e) {
                                 Log.e(TAG, "ERROR: Could not get Location code");
                                 e.printStackTrace();
                             }
                         }
                         Message msg = Message.obtain();
                         msg.what = UPDATE_WEATHER;
                         msg.obj = woeid;
                         mHandler.sendMessage(msg);
                     }
                 });
                 mWeatherRefreshing = true;
                 queryWeather.setPriority(Thread.MIN_PRIORITY);
                 queryWeather.start();
                 break;
             case UPDATE_WEATHER:
                 String woeid = (String) msg.obj;
                 if (woeid != null) {
                     if (DEBUG) {
                         Log.d(TAG, "Location code is " + woeid);
                     }
                     WeatherInfo w = null;
                     try {
                         w = parseXml(getDocument(woeid));
                     } catch (Exception e) {
                     }
                     mWeatherRefreshing = false;
                     if (w == null) {
                         setNoWeatherData();
                     } else {
                         setWeatherData(w);
                         mWeatherInfo = w;
                     }
                 } else {
                     mWeatherRefreshing = false;
                     if (mWeatherInfo.temp.equals(WeatherInfo.NODATA)) {
                         setNoWeatherData();
                     } else {
                         setWeatherData(mWeatherInfo);
                     }
                 }
                 break;
             }
         }
     };
 
     /**
      * Reload the weather forecast
      */
     private void refreshWeather() {
         final ContentResolver resolver = mContext.getContentResolver();
             final long interval = Settings.System.getLong(resolver,
                     Settings.System.WEATHER_UPDATE_INTERVAL, 0); // Default to manual
             boolean manualSync = (interval == 0);
             if (!manualSync && (((System.currentTimeMillis() - mWeatherInfo.last_sync) / 60000) >= interval)) {
                 updating = true;
                 updateQuickSettings();
                if (!mWeatherRefreshing && !mHandler.hasMessages(QUERY_WEATHER)) {
                     mHandler.sendEmptyMessage(QUERY_WEATHER);
                 }
             } else if (manualSync && mWeatherInfo.last_sync == 0) {
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
         final Resources res = mContext.getResources();
         String conditionCode = w.condition_code;
         String condition_filename = "weather_" + conditionCode;
         int resID = res.getIdentifier(condition_filename, "drawable",
                         mContext.getPackageName());
 
         if (resID != 0) {
             addDrwb = true;
             drwb = res.getDrawable(resID);
         } else {
             addDrwb = false;
         }
         mLabel = (w.temp + " | " + w.humidity) ;
         Date lastTime = new Date(mWeatherInfo.last_sync);
         date = DateFormat.getDateFormat(mContext).format(lastTime);
         time = DateFormat.getTimeFormat(mContext).format(lastTime);
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
         TextView mWeatherUpdateTime = (TextView) mTile.findViewById(R.id.weathertwo_textview);
         if (mWeatherTemp != null && mWeatherUpdateTime != null) {
             if (updating) {
                 mWeatherTemp.setText(com.android.internal.R.string.weather_refreshing);
                 mWeatherUpdateTime.setText(com.android.internal.R.string.weather_refreshing);
                 updating = false;
             } else {
                 if (!addDrwb) {
                     mWeatherTemp.setText(com.android.internal.R.string.weather_tap_to_refresh);
                     mWeatherUpdateTime.setText("N/A");
                 } else {
                     mWeatherTemp.setText(mLabel);
                     mWeatherUpdateTime.setText(date + " " + time);
                 }
             }
         }
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
 
     /**
      * Get the weather forecast XML document for a specific location
      * @param woeid
      * @return
      */
     private Document getDocument(String woeid) {
         try {
             boolean celcius = Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.WEATHER_USE_METRIC, 1) == 1;
             String urlWithDegreeUnit;
 
             if (celcius) {
                 urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "c";
             } else {
                 urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "f";
             }
 
             return new HttpRetriever().getDocumentFromURL(String.format(urlWithDegreeUnit, woeid));
         } catch (IOException e) {
             Log.e(TAG, "Error querying Yahoo weather");
         }
 
         return null;
     }
 
     /**
      * Parse the weather XML document
      * @param wDoc
      * @return
      */
     private WeatherInfo parseXml(Document wDoc) {
         try {
             return new WeatherXmlParser(mContext).parseWeatherResponse(wDoc);
         } catch (Exception e) {
             Log.e(TAG, "Error parsing Yahoo weather XML document");
             e.printStackTrace();
         }
         return null;
     }
 }
