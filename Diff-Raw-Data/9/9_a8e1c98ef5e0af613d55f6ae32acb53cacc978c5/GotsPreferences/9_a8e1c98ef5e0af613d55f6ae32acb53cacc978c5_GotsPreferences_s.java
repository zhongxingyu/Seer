 /* *********************************************************************** *
  * project: org.gots.*
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  * copyright       : (C) 2013 by the members listed in the COPYING,        *
  *                   LICENSE and WARRANTY file.                            *
  * email           : contact at gardening-manager dot com                  *
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *   See also COPYING, LICENSE and WARRANTY file
  *   Contributors:                                                         *
  *                - Sebastien FLEURY                                       *
  *                                                                         *
  * *********************************************************************** */
 package org.gots.preferences;
 
 import java.util.List;
 
 import org.gots.broadcast.BroadCastMessages;
 import org.gots.utils.NotConfiguredException;
 import org.nuxeo.android.config.NuxeoServerConfig;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class GotsPreferences implements OnSharedPreferenceChangeListener {
 
     private static final String TAG = "GotsPreferences";
 
     public static final boolean ISDEVELOPMENT = false;
 
     public static final boolean DEBUG = false;
 
     // /**
     // * @see NuxeoServerConfig#PREF_SERVER_PASSWORD
     // */
     // public static final String ORG_GOTS_GARDEN_PASSWORD = "org.gots.garden.password";
     public static final String ORG_GOTS_GARDEN_PASSWORD = NuxeoServerConfig.PREF_SERVER_PASSWORD;
 
     // public static final String ORG_GOTS_GARDEN_NUXEO_URI = "org.gots.garden.nuxeo.uri";
     public static final String ORG_GOTS_GARDEN_NUXEO_URI = NuxeoServerConfig.PREF_SERVER_URL;
 
     // /**
     // * @see NuxeoServerConfig#PREF_SERVER_LOGIN
     // */
     // public static final String ORG_GOTS_GARDEN_LOGIN = "org.gots.garden.login";
     public static final String ORG_GOTS_GARDEN_LOGIN = NuxeoServerConfig.PREF_SERVER_LOGIN;
 
     public static final String ORG_GOTS_GARDEN_SUCCESSFUL_LOGIN = "nuxeo.successful.login";
 
     public static final String ORG_GOTS_GARDEN_SERVERCONNECTED = "org.gots.garden.connected";
 
     public static final String ORG_GOTS_GARDEN_DEVICEID = "org.gots.garden.deviceid";
 
     // /**
     // * @see NuxeoServerConfig#PREF_SERVER_TOKEN
     // */
     // public static final String ORG_GOTS_GARDEN_TOKEN = "org.gots.garden.token";
     public static final String ORG_GOTS_GARDEN_TOKEN = NuxeoServerConfig.PREF_SERVER_TOKEN;
 
     private static final String ANALYTICS_API_KEY = "UA-916500-18";
 
     private static final String WEATHER_API_KEY = "6ba97b2306fd5b9d47992d8716dab16a";
 
     private static final String ADMOB_API_KEY = "a14f50fa231b26d";
 
     public static final String GARDENING_MANAGER_DIRECTORY = "Gardening-Manager";
 
     public static final String GARDENING_MANAGER_APPNAME = "Gardening Manager";
 
     private static final String GARDENING_MANAGER_DOCUMENTATION_URL = "http://doc.gardening-manager.com";
 
     private static final String GARDENING_MANAGER_NUXEO_AUTOMATION_TEST = "http://services.gardening-manager.com/nuxeo/";
 
     private static final String GARDENING_MANAGER_NUXEO_AUTOMATION = "http://services.gardening-manager.com/nuxeo/";
 
     // private static final String GARDENING_MANAGER_NUXEO_AUTOMATION = "http://srv2.gardening-manager.com:8090/nuxeo/";
 
     // private static final String DEFAULT_LOCAL_URL = "http://10.0.2.2:8080/nuxeo/";
 
     // private static final String GARDENING_MANAGER_NUXEO_AUTHENTICATION =
     // "http://srv2.gardening-manager.com:8090/nuxeo/authentication/temptoken?";
 
     public static final String ORG_GOTS_CURRENT_GARDENID = "org.gots.preference.gardenid";
 
     public static final String ORG_GOTS_CURRENT_ALLOTMENT = "org.gots.preference.allotmentid";
 
     protected SharedPreferences sharedPreferences;
 
     protected Context mContext;
 
     private static GotsPreferences instance = null;
 
     private static Exception firstCall;
 
     private boolean initDone = false;
 
     private GotsPreferences() {
     }
 
     /**
      * After first call, {@link #initIfNew(Context)} must be called else a {@link NotConfiguredException} will be thrown
      * on the second call attempt.
      */
     public static synchronized GotsPreferences getInstance() {
         if (instance == null) {
             instance = new GotsPreferences();
             firstCall = new Exception();
         } else if (!instance.initDone) {
             throw new NotConfiguredException(firstCall);
         }
         return instance;
     }
 
     /**
      * If it was already called once, the method returns without any change.
      */
     public synchronized GotsPreferences initIfNew(Context context) {
         if (!initDone) {
             mContext = context;
             setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(context));
             setGardeningManagerServerURI(ISDEVELOPMENT ? GARDENING_MANAGER_NUXEO_AUTOMATION_TEST : GARDENING_MANAGER_NUXEO_AUTOMATION);
             initDone = true;
         }
         return this;
     }
 
     public SharedPreferences getSharedPrefs() {
         return sharedPreferences;
     }
 
     protected void setSharedPreferences(SharedPreferences sharedPreferences) {
         this.sharedPreferences = sharedPreferences;
         sharedPreferences.registerOnSharedPreferenceChangeListener(this);
         // initFromPrefs(sharedPreferences);
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         if (ORG_GOTS_GARDEN_SERVERCONNECTED.equals(key)) {
             mContext.sendBroadcast(new Intent(BroadCastMessages.CONNECTION_SETTINGS_CHANGED));
             Log.d(TAG, key + " has changed: " + isConnectedToServer());
         } else if (ORG_GOTS_CURRENT_GARDENID.equals(key)) {
             mContext.sendBroadcast(new Intent(BroadCastMessages.GARDEN_SETTINGS_CHANGED));
             Log.d(TAG, key + " has changed: " + getCurrentGardenId());
         }
         // initFromPrefs(prefs);
     }
 
     protected void initFromPrefs(SharedPreferences prefs) {
         // if need to store some values instead of read from pref each time
     }
 
     public void set(String key, String value) {
         SharedPreferences.Editor prefedit = sharedPreferences.edit();
         prefedit.putString(key, value);
         prefedit.commit();
         Log.d(TAG, key + "=" + value);
     }
 
     public void set(String key, long value) {
         SharedPreferences.Editor prefedit = sharedPreferences.edit();
         prefedit.putLong(key, value);
         prefedit.commit();
         Log.d(TAG, key + "=" + value);
     }
 
     public void set(String key, int value) {
         SharedPreferences.Editor prefedit = sharedPreferences.edit();
         prefedit.putInt(key, value);
         prefedit.commit();
         Log.d(TAG, key + "=" + value);
     }
 
     public void set(String key, boolean value) {
         SharedPreferences.Editor prefedit = sharedPreferences.edit();
         prefedit.putBoolean(key, value);
         prefedit.commit();
         Log.d(TAG, key + "=" + value);
     }
 
     public String get(String key, String defValue) {
         return sharedPreferences.getString(key, defValue);
     }
 
     public long get(String key, long defValue) {
         return sharedPreferences.getLong(key, defValue);
     }
 
     public int get(String key, int defValue) {
         return sharedPreferences.getInt(key, defValue);
     }
 
     public boolean get(String key, boolean defValue) {
         return sharedPreferences.getBoolean(key, defValue);
     }
 
     public boolean isPremium() {
         return unlockPremium();
 //        return true;
     }
 
     public static String getAnalyticsApiKey() {
         return ANALYTICS_API_KEY;
     }
 
     public static String getWeatherApiKey() {
         return WEATHER_API_KEY;
     }
 
     public static String getAdmobApiKey() {
 
         return ADMOB_API_KEY;
     }
 
     public void setGardeningManagerServerURI(String uri) {
         if (!uri.endsWith("/")) {
             uri = uri + "/";
         }
         Log.d(TAG, "setGardeningManagerServerURI " + uri);
         set(ORG_GOTS_GARDEN_NUXEO_URI, uri);
     }
 
     public String getGardeningManagerServerURI() {
         String url = sharedPreferences.getString(ORG_GOTS_GARDEN_NUXEO_URI, "");
         Log.d(TAG, "return " + url);
         return url;
     }
 
     // public void setPREMIUM(boolean pREMIUM) {
     // ORG_GOTS_PREMIUM_LICENCE = pREMIUM;
     // }
 
     public static boolean isDevelopment() {
         return ISDEVELOPMENT;
     }
 
     public String getNuxeoLogin() {
        return sharedPreferences.getString(ORG_GOTS_GARDEN_LOGIN, null);
     }
 
     public void setNuxeoLogin(String login) {
         set(ORG_GOTS_GARDEN_LOGIN, login);
     }
 
     public String getNuxeoPassword() {
         return sharedPreferences.getString(ORG_GOTS_GARDEN_PASSWORD, "");
     }
 
     public void setNuxeoPassword(String password) {
         set(ORG_GOTS_GARDEN_PASSWORD, password);
     }
 
     public void setConnectedToServer(boolean isConnected) {
         set(ORG_GOTS_GARDEN_SERVERCONNECTED, isConnected);
     }
 
     public boolean isConnectedToServer() {
         return sharedPreferences.getBoolean(ORG_GOTS_GARDEN_SERVERCONNECTED, false);
     }
 
     public String getToken() {
         return sharedPreferences.getString(ORG_GOTS_GARDEN_TOKEN, "");
     }
 
     public void setToken(String token) {
         set(ORG_GOTS_GARDEN_TOKEN, token);
     }
 
     private boolean unlockPremium() {
         boolean unlocked = false;
         PackageManager pm = mContext.getPackageManager();
         List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
 
         for (ApplicationInfo applicationInfo : packages) {
             try {
 
                 PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
                 if ("org.gots.premium".equals(packageInfo.packageName)) {
                     unlocked = true;
                 }
             } catch (NameNotFoundException e) {
                 Log.e(TAG, e.getMessage(), e);
             }
         }
         return unlocked;
 
     }
 
     public String getDeviceId() {
         return get(ORG_GOTS_GARDEN_DEVICEID, "");
     }
 
     public void setDeviceId(String device_id) {
         set(ORG_GOTS_GARDEN_DEVICEID, device_id);
     }
 
     public String getGardeningManagerAppname() {
         return GARDENING_MANAGER_APPNAME;
     }
 
     public String getNuxeoAuthenticationURI() {
         return getGardeningManagerServerURI() + "authentication/token?";
     }
 
     public String getNuxeoAutomationURI() {
         return getGardeningManagerServerURI() + "site/automation";
     }
 
     public int getCurrentGardenId() {
         SharedPreferences preferences = mContext.getSharedPreferences("org.gots.preference", 0);
         int oldGardenId = preferences.getInt("org.gots.preference.gardenid", -1);
         if (oldGardenId > -1) {
             GotsPreferences.getInstance().set(GotsPreferences.ORG_GOTS_CURRENT_GARDENID, oldGardenId);
             SharedPreferences.Editor prefedit = preferences.edit();
             prefedit.putInt("org.gots.preference.gardenid", -1);
             prefedit.commit();
         }
         return get(GotsPreferences.ORG_GOTS_CURRENT_GARDENID, -1);
     }
 
     public String getLastSuccessfulNuxeoLogin() {
         return get(ORG_GOTS_GARDEN_SUCCESSFUL_LOGIN, "");
     }
 
     public void setLastSuccessfulNuxeoLogin(String login) {
         set(ORG_GOTS_GARDEN_SUCCESSFUL_LOGIN, login);
     }
 
     public String getDocumentationURI() {
         return GARDENING_MANAGER_DOCUMENTATION_URL;
     }
 
 }
