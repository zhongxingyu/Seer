 /*
  * Copyright (C) 2013 asksven
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
 
 package com.asksven.mytrack;
 
 import java.util.Calendar;
 
 import android.app.ActivityManager;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.asksven.android.common.location.GeoUtils;
 import com.asksven.android.common.utils.DateUtils;
 import com.asksven.mytrack.utils.Configuration;
 import com.asksven.mytrack.utils.Constants;
 import com.asksven.mytrack.utils.LocationWriter;
 
 
 /**
  * The LocationService keeps running even if the main Activity is not displayed/never called
  * The Services takes care of always running location updatestasks and of tasks taking place once in the lifecycle
  * without user interaction.
  * @author sven
  *
  */
 /**
  * @author sven
  *
  */
 public class LocationService extends Service implements LocationListener, OnSharedPreferenceChangeListener
 {
 	/** singleton */
 	private static LocationService m_instance = null;
 	
 	private NotificationManager mNM;
 	
 	public static String SERVICE_NAME = "com.asksven.mytrack.LocationService";
 	private static int QUICK_ACTION = 1234567;
 	private static int QOS_ALARM 	= 1234568;
 
 	private LocationManager m_locationManager;
 	
 	private static final String TAG = "LocationService";
 		
 	/** the connection status */
 	private String m_strStatus = "";
 	
 	private boolean m_bRegistered = false;
 	
 	private boolean bQuickChangeRunning = false;
 
 	/** the location provider in use */
 	String m_strLocProvider = "";
 	
 	/** the current location (is geo is on) */
 	String m_strCurrentLocation  = "";
 	
 	/** the last updated location */
 	Location m_lastUpdatedLoc = null;
 
 	/** the time of last update */
 	long m_lastUpdatedTime = 0;
 
 	/** precision for current location manager */
 	private int m_iIterval = 0;
 	private int m_iAccuracy = 0;
 	private int m_iDuration = 0;
 	
 	
 	/** spinner indexes for quick actions */
 	int m_iIntervalIndex = 0;
 	int m_iAccuracyIndex = 0;
 	int m_iDurationIndex = 0;
 	
 	long m_lUpdated = 0;
 	long m_lQuickUntil = 0;
 	
 	Notification m_stickyNotification = null;
 
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder
     {
         public LocationService getService()
         {
         	Log.i(TAG, "getService called");
             return LocationService.this;
         }
     }
 
     @Override
     public void onCreate()
     {
     	super.onCreate();
     	m_instance = this;
 
     	Log.i(getClass().getSimpleName(), "onCreate called");
 
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         
         // register the location listener
         this.registerLocationListener();
         
     	// Set up a listener whenever a key changes
     	PreferenceManager.getDefaultSharedPreferences(this)
                 .registerOnSharedPreferenceChangeListener(this);
 
     	// set status
     	setStatus(Constants.getInstance(this).STATUS_UPDATE_PENDING);
     	
     	// trigger QoS alarm (will do nothing if settings say not to
    }
     
     private void registerLocationListener()
     {
     	if (m_bRegistered)
     	{
         	// unregister the receiver
     		m_locationManager.removeUpdates(this);    		
     	}
 
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	String strInterval = prefs.getString("update_interval", "15");
     	String strAccuracy = prefs.getString("update_accuracy", "2000");
 
     	    	
 		int iInterval = 15 * 60 * 1000;
 		int iAccuracy = 2000;
 		try
     	{
 			iInterval = Integer.valueOf(strInterval) * 60 * 1000;
 			iAccuracy = Integer.valueOf(strAccuracy);
     	}
     	catch (Exception e)
     	{
     		Log.e(TAG, "Error reading prefernces, using defaults");
     	}
     	
 		registerLocationListener(iInterval, iAccuracy);
 
             
     }
     
     private void registerLocationListener(int intervalMs, int accuracyM)
     {
     	if (m_bRegistered)
     	{
         	// unregister the receiver
     		m_locationManager.removeUpdates(this);    		
     	}
 
     	
 		intervalMs = 15 * 60 * 1000;
 		accuracyM = 2000;
     		
     	Criteria criteria = new Criteria();
     	criteria.setSpeedRequired(false);
     	criteria.setAltitudeRequired(false);
     	criteria.setCostAllowed(false);
     	criteria.setBearingRequired(false);
     	
     	if (accuracyM <= 100)
     	{
     		criteria.setAccuracy(Criteria.ACCURACY_FINE);
     		criteria.setPowerRequirement(Criteria.POWER_HIGH);
     	}
     	else
     	{
     		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
     		criteria.setPowerRequirement(Criteria.POWER_LOW);
     	}
     	
 
 		// Get the location manager
 		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		if (m_locationManager != null)
 		{
 	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	    	boolean bUsePassiveProvider = prefs.getBoolean("passive_provider", false);
 
 			if (!bUsePassiveProvider)
 			{
 		        m_strLocProvider = m_locationManager.getBestProvider(criteria, true);
 		        Log.i(TAG, "registerLocationListener determined best provider: " + m_strLocProvider);
 		        if (m_strLocProvider != null)
 		        {
 			        m_locationManager.requestLocationUpdates(m_strLocProvider, intervalMs, accuracyM, this);
 			        m_iAccuracy = accuracyM;
 			        m_iIterval = intervalMs;
 			        Log.i(TAG, "Using provider '" + m_strLocProvider + "'");
 				}
 		        else
 		        {
 		        	Log.e(TAG, "requestLocationUpdates could not be called because there is no location provider available: m_strLocProvider='" + m_strLocProvider + "'");
 		        	m_bRegistered = false;
 		        }
 			}
 			else
 			{
 				m_locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
 				Log.i(TAG, "Requesting passive provider");
 				m_iAccuracy = 0;
 		        m_iIterval = 0;
 			}
 	        m_bRegistered = true;
 		}
 		else
 		{
 			m_bRegistered = false;
 			Log.i(TAG, "No location manager could be set");
 		}
     }
 
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
     {
     	if (key.equals("update_interval") || key.equals("update_accuracy"))
     	{
     		
     		Log.i(TAG, "Preferences have change. Register location listener again");
     		// re-register location listener with new prefs
     		this.registerLocationListener();
     	}
 
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	
     	if (key.equals("notify_status"))
     	{
     		// activate / deactivate the notification
     		if (prefs.getBoolean("notify_status", true))
     		{
     			notifyStatus(Constants.getInstance(this).STATUS_NOTIFICATION_ON);
     		}
     		else
     		{
     	        // Cancel the persistent notification.
     	        mNM.cancel(R.string.app_name);
 
     		}
     	}
     	    	
     	if (key.equals("foreground_service"))
     	{
     		// stop and start the service, starting it will lead to prefs being read
 			Intent i = new Intent();
 			i.setClassName( "com.asksven.betterlatitude", LocationService.SERVICE_NAME );
        		stopService(i);
        		startService(i);
     	}
 
     	if (key.equals("force_interval"))
     	{
         	if (prefs.getBoolean("force_interval", false))
         	{
         		// set the QoS alarm
         		this.setQosAlarm();
         	}
         	else
         	{
         		// cancel the running QoS alarm 
         		this.cancelQosAlarm();
 
         	}
     	}
 
     }
 
     /** 
      * Called when service is started
      */
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         Log.i(getClass().getSimpleName(), "Service started, received start id " + startId + ": " + intent);
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean bForegroundService = prefs.getBoolean("foreground_service", true);
     	if (bForegroundService)
     	{
     		setupAsForeground(Constants.getInstance(this).STATUS_FG_SERVICE_STARTED);
     	}
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         
         return Service.START_STICKY;
     }
 
     void setupAsForeground(String strNotification)
     {
     	m_stickyNotification = new Notification(
    			R.drawable.icon, Constants.getInstance(this).STATUS_SERVICE_RUNNING, System.currentTimeMillis());
 		Intent i=new Intent(this, MainActivity.class);
 		
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
 		Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		
 		PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
 
		m_stickyNotification.setLatestEventInfo(this, "ALTitude", strNotification, pi);
 		m_stickyNotification.flags|=Notification.FLAG_NO_CLEAR;
 		
 		if (isServiceRunning(this))
 		{
 			Log.i(TAG, "setupAsForeground was called to update the notification");
 		}
 		else
 		{
 			Log.i(TAG, "setupAsForeground was called and started the service");
 		}
 			
 		startForeground(12245, m_stickyNotification);
     	
     }
     @Override
     /**
      * Called when Service is terminated
      */
     public void onDestroy()
     {        
         // Cancel the persistent notification.
         mNM.cancel(R.string.app_name);
     	// unregister the receivers
 		m_locationManager.removeUpdates(this);
 		
 		// hack: there is no way to test whether a receiver is registered so we have to try and ignore the exception
 		
         // Unregister the listener whenever a key changes
         PreferenceManager.getDefaultSharedPreferences(this)
                 .unregisterOnSharedPreferenceChangeListener(this);
 
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return mBinder;
     }
 
     // This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 	
     @Override
 	public void onLocationChanged(Location location)
 	{
     	Log.i(TAG, "onLocationChanged called");
     	// we may have QoS: as the location was just updated we need to reset the alarm counter
     	setQosAlarm();
     	
     	// if we are not in quick change mode the we need to check if there are limitiations
     	// to the update frequency / distance to be applied
     	if (isQuickChangeRunning())
     	{
     		LocationWriter.LogLocationToFile(this, location);
     	}
     	else
     	{
     		LocationWriter.LogLocationToFile(this, location);    			
     	}
 
 	}
 
     /** 
      * forces an update with the last known location
      */
 	public void forceLocationUpdate()
 	{    	
     	// we may have QoS: as the location was just updated we need to reset the alarm counter
     	setQosAlarm();
     	
     	if (m_strLocProvider == null)
     	{
     		Log.i(TAG, "forceLocationUpdate aborted: no location provider defined");
     		return;
     	}
     	
     	Location here = m_locationManager.getLastKnownLocation(m_strLocProvider);
     	
     	// we need to change the timestamp to "now"
     	long now =System.currentTimeMillis();
     	here.setTime(now);
     	
     	LocationWriter.LogLocationToFile(this, here);
 
 	}
 
     /** 
      * forces an update with the last known location
      */
 	public void forceLocationUpdate(float latitude, float longitude)
 	{    	
     	// we may have QoS: as the location was just updated we need to reset the alarm counter
     	setQosAlarm();
     	
     	if (m_strLocProvider == null)
     	{
     		Log.i(TAG, "forceLocationUpdate aborted: no location provider defined");
     		return;
     	}
     	
     	Location here = m_locationManager.getLastKnownLocation(m_strLocProvider);
     	here.setLatitude(latitude);
     	here.setLongitude(longitude);
     	here.setAccuracy(10f);
     	
     	// we need to change the timestamp to "now"
     	long now =System.currentTimeMillis();
     	here.setTime(now);
     	
     	LocationWriter.LogLocationToFile(this, here);
 
 	}
 
     @Override
 	public void onStatusChanged(String provider, int status, Bundle extras)
 	{
 		Log.e(TAG, "onStatusChanged called with status=" + status);
 	}
 
 	/* 
 	 * Called when a new location provider was enabled
 	 * (non-Javadoc)
 	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
 	 */
 	@Override
 	public void onProviderEnabled(String provider)
 	{
 		// we may have a better provider now, redefine
 		Log.e(TAG, "Provider " + provider + " was enabled. Maybe we want to use it");
 		if (bQuickChangeRunning)
 		{
 			registerLocationListener(m_iIterval, m_iAccuracy);
 		}
 		else
 		{
 			registerLocationListener();
 		}
 		
 		if (LocationManager.GPS_PROVIDER.equals(provider) && !isQuickChangeRunning())
 		{
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	    	if (prefs.getBoolean("auto_on_when_gps", false))
 	    	{
 	    		setQuickChange();
 	    	}			
 		}
 	}
 
 	/* 
 	 * Called when a location provider was disabled
 	 * (non-Javadoc)
 	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
 	 */
 	@Override
 	public void onProviderDisabled(String provider)
 	{
 		// we may have to change providers if we use it right now
 		if (provider.equals(m_strLocProvider))
 		{
 			Log.e(TAG, "Provider " + provider + " was in use but got disabled. Getting a new one");
 			if (bQuickChangeRunning)
 			{
 				registerLocationListener(m_iIterval, m_iAccuracy);
 			}
 			else
 			{
 				registerLocationListener();
 			}		
 		}
 		
 		if (LocationManager.GPS_PROVIDER.equals(provider))
 		{
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	    	if (prefs.getBoolean("auto_on_when_gps", false) && isQuickChangeRunning())
 	    	{
 	    		resetQuickChange();
 	    	}			
 		}
 
 	}	
 
 	/**
 	 * Notify status change in notification bar (if enabled)
 	 */
 	void notifyStatus(String strStatus)
 	{
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean bNotify 	= prefs.getBoolean("notify_status", true);
     	if (bNotify)
     	{
     		if (m_stickyNotification != null)
     		{
     			setupAsForeground(strStatus);
     		}
     		else
     		{
 		    	Notification notification = new Notification(
		    			R.drawable.icon, strStatus, System.currentTimeMillis());
 		    	PendingIntent contentIntent = PendingIntent.getActivity(
 		    			this, 0, new Intent(this, MainActivity.class), 0);
 		    	notification.setLatestEventInfo(
 		    			this, getText(R.string.app_name), strStatus, contentIntent);
 		    	mNM.notify(R.string.app_name, notification);
     		}
     	}
 	}
 	
 	/**
 	 * Notify location change in notification bar (if enabled)
 	 */
 	public void notifyCurrentLocation(Location location)
 	{
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	String strStatus = Constants.getInstance(this).STATUS_LOCATION_UPDATED;
     	
     	boolean bNotify 	= prefs.getBoolean("notify_status", true);
     	boolean bNotifyGeo 	= prefs.getBoolean("notify_geodata", false);
     	if (bNotify && bNotifyGeo)
     	{
 			if ( (bNotifyGeo) && (location != null) )
  			{
 				m_strCurrentLocation = GeoUtils.getNearestAddress(this, location);
 				strStatus = strStatus
 						+ ": "
 						+ m_strCurrentLocation;
 				notifyStatus(strStatus);
 			}
 			else
 			{
 				m_strCurrentLocation = "";
 			}
 			
 	    }
     	else if (bNotify)
     	{
     		// simple notification
 			strStatus = strStatus
 					+ " at "
 					+ DateUtils.now();
 			notifyStatus(strStatus);
     	}
     		
 	}
 
 	
 	/**
 	 * Notify errors in notification bar (if enabled)
 	 */
 	void notifyError(String strStatus)
 	{
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean bNotify 	= prefs.getBoolean("notify_error", true);
     	if (bNotify)
     	{
 
 	    	Notification notification = new Notification(
 	    			R.drawable.icon, strStatus, System.currentTimeMillis());
 	    	PendingIntent contentIntent = PendingIntent.getActivity(
 	    			this, 0, new Intent(this, MainActivity.class), 0);
 	    	notification.setLatestEventInfo(
 	    			this, getText(R.string.app_name), strStatus, contentIntent);
 	    	mNM.notify(R.string.app_name, notification);
     	}
     	// Log the error
     	Log.e(TAG, "An error occured: " + strStatus);
 	}
 	
 
 	/** 
 	 * Broadcasts the connection status change
 	 * @param strStatus the broadcasted message
 	 */
 	public void setStatus(String strStatus)
 	{
 		m_strStatus = strStatus;
 		sendBroadcast(new Intent(Constants.getInstance(this).BROADCAST_STATUS_CHANGED));
 	}
 	
 	/**
 	 * Returns the status of the Latitude connection
 	 * @return the status of the latitude connection
 	 */
 	public String getStatus()
 	{
 		return m_strStatus;
 	}
 
 	/**
 	 * Returns the last known location address (if enabled)
 	 * @return the last known address
 	 */
 	public String getAddress()
 	{
 		return m_strCurrentLocation;
 	}
 
 	/**
 	 * Returns until when the quick change is active
 	 * @return
 	 */
 	public long getUntil()
 	{
 		return m_lQuickUntil;
 	}
 
 	/**
 	 * Returns the sigleton instance of the service
 	 * @return
 	 */
 	public static LocationService getInstance()
 	{
 		return m_instance;
 	}
 	
 	/**
 	 * Returns true if a quick action is running
 	 * @return 
 	 */
 	public boolean isQuickChangeRunning()
 	{
 		return bQuickChangeRunning;
 	}
 	
 	/**
 	 * 
 	 * @return the accuracy spinner index
 	 */
 	public int getAccuracyIndex()
 	{
 		return m_iAccuracyIndex;
 	}
 
 	/**
 	 * 
 	 * @return the duration spinner index
 	 */
 	public int getDurationIndex()
 	{
 		return m_iDurationIndex;
 	}
 
 	/**
 	 * 
 	 * @return the interval spinner index
 	 */
 	public int getIntervalIndex()
 	{
 		return m_iIntervalIndex;
 	}
 
 	/**
 	 * Run a quick action for the given parameters
 	 * @param interval as index (@see array.xml)
 	 * @param accuracy as index (@see array.xml)
 	 * @param duration as index (@see array.xml)
 	 * @return true if set successfully
 	 */
 	public boolean setQuickChange(int interval, int accuracy, int duration)
 	{
 		Log.i(TAG, "setQuickChange called with " + interval + accuracy + duration);
 		// @see arrays.xml
 		// get a Calendar object with current time
 		Calendar cal = Calendar.getInstance();
 		int minutes = 0;
 		int intervalMs = 0;
 		int accuracyM = 0;
 		
 		m_iIntervalIndex = interval;
 		m_iAccuracyIndex = accuracy;
 		m_iDurationIndex = duration;
 
 
 		switch (interval)
 		{
 		case 0:
 			intervalMs = 5 * 1000;
 			break;
 		case 1:
 			intervalMs = 10 * 1000;
 			break;
 		case 2:
 			intervalMs = 30 * 1000;
 			break;
 		case 3:
 			intervalMs = 60 * 1000;
 			break;
 		case 4:
 			intervalMs = 5 * 60 * 1000;
 			break;
 		case 5:
 			intervalMs = 15 * 60 * 1000;
 			break;
 		}
 
 		switch (accuracy)
 		{
 		case 0:
 			accuracyM = 10;
 			break;
 		case 1:
 			accuracyM = 50;
 			break;
 		case 2:
 			accuracyM = 100;
 			break;
 		case 3:
 			accuracyM = 500;
 			break;
 		case 5:
 			accuracyM = 1000;
 			break;
 		}
 
 		switch (duration)
 		{
 		case 0:
 			minutes = 15;
 			break;
 		case 1:
 			minutes = 30;
 			break;
 		case 2:
 			minutes = 60;
 			break;
 		case 3:
 			minutes = 120;
 			break;
 		}
 
 		m_iIterval = intervalMs;
 		m_iAccuracy = accuracyM;		
 		m_iDuration = minutes;
 
 		cal.add(Calendar.MINUTE, minutes);
 		m_lQuickUntil = cal.getTimeInMillis();
 
 		Intent intent = new Intent(this, AlarmReceiver.class);
 
 		PendingIntent sender = PendingIntent.getBroadcast(this, QUICK_ACTION,
 				intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		// Get the AlarmManager service
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
 
 		// change
 		registerLocationListener(intervalMs, accuracyM);
 		bQuickChangeRunning = true;
 		return true;
 	}
 	
 	/**
 	 * Reset (void) any running quick action
 	 */
 	public void resetQuickChange()
 	{
 		Log.i(TAG, "resetQuickChange called");
 		// check if there is an intent pending
 		Intent intent = new Intent(this, AlarmReceiver.class);
 	
 		PendingIntent sender = PendingIntent.getBroadcast(this, QUICK_ACTION,
 				intent, PendingIntent.FLAG_UPDATE_CURRENT);
 	
 		if (sender != null)
 		{
 			// Get the AlarmManager service
 			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 			am.cancel(sender);
 		}
 		// reset to pref values
 		registerLocationListener();
 		bQuickChangeRunning = false;
 		m_iIntervalIndex = 0;
 		m_iAccuracyIndex = 0;
 		m_iDurationIndex = 0;
 	
 	}
 
 	/**
 	 * Start a quick change with the default values 
 	 */
 	public void setQuickChange()
 	{
     	int iAccuracy = 0;
     	int iInterval = 0;
     	int iDuration = 0;
     	try
     	{
     		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         	
     		iAccuracy = Integer.valueOf(sharedPrefs.getString("quick_update_accuracy", "0"));
     		iInterval = Integer.valueOf(sharedPrefs.getString("quick_update_interval", "0"));
     		iDuration = Integer.valueOf(sharedPrefs.getString("quick_update_duration", "0"));
     	}
     	catch (Exception e)
     	{
     		Log.e(TAG, "An error occured while reading quick action preferences");
     	}
 
     	// start a quick setting with defaults
 		this.setQuickChange(iInterval, iAccuracy, iDuration);
 
 	}
 
 	/**
 	 * Returns the update interval requested from the LocationProvider in ms
 	 * @return the interval in ms
 	 */
 	public int getInterval()
 	{
 		return m_iIterval;
 	}
 	
 	/**
 	 * Returns the accuracy requested from the LocationProvider in m
 	 * @return the accuracy in m
 	 */
 	public int getAccuracy()
 	{
 		return m_iAccuracy;
 	}
 			
 	/**
 	 * Returns the duration of the quick setting in ms
 	 * @return the duration in ms
 	 */
 	public int getDuration()
 	{
 		return m_iDuration;
 	}
 	
 	/**
 	 * Returns the time (in ms) when the last update took place
 	 * @return
 	 */
 	public long getUpdated()
 	{
 		return m_lUpdated;
 	}
 
 	/**
 	 * Return the current address (if geo is on)
 	 * @return the current location as a string
 	 */
 	public String getLocationProvider()
 	{
 		return m_strLocProvider;
 	}
 	
 	/**
 	 * Returns the current location (address) as a string
 	 * @return the current address
 	 */
 	public String getCurrentLocation()
 	{
 		return m_strCurrentLocation;
 	}
 
 
 	protected static boolean isServiceRunning(Context ctx)
 	{
 	    ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
 	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
 	    {
 	        if (LocationService.SERVICE_NAME.equals(service.service.getClassName()))
 	        {
 	        	Log.i(TAG, "isMyServiceRunning confirmed that service is running");
 	            return true;
 	        }
 	    }
 	    Log.i(TAG, "isMyServiceRunning confirmed that service is not running");
 	    return false;
 	}
 	
 	/**
 	 * Adds an alarm to schedule a wakeup to retrieve the current location
 	 */
 	public boolean setQosAlarm()
 	{
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	if (!prefs.getBoolean("force_interval", false))
     	{
     		cancelQosAlarm();
     		return false;
     	}
 
 		Log.i(TAG, "setQosAlarm called");
 		
 		// cancel any exiting alarms
 		cancelQosAlarm();
 
     	String strInterval = prefs.getString("update_interval", "15");
     	if (!Configuration.isFullVersion(this))
 		{
     		strInterval = "15";
 		}
     	    	
 		int iInterval = 15;
 		try
     	{
 			iInterval = Integer.valueOf(strInterval);
     	}
     	catch (Exception e)
     	{
     	}
 
 		Log.i(TAG, "QoS alarm scheduled in " + iInterval + " minutes");
 		long fireAt = System.currentTimeMillis() + (iInterval * 60 * 1000);
 
 		Intent intent = new Intent(this, QosAlarmReceiver.class);
 
 		PendingIntent sender = PendingIntent.getBroadcast(this, QOS_ALARM,
 				intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		// Get the AlarmManager service
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);
 
 		return true;
 	}
 	
 	/**
 	 * Cancels the current alarm (if existing)
 	 */
 	public void cancelQosAlarm()
 	{
 		Log.i(TAG, "cancelQosAlarm");
 		// check if there is an intent pending
 		Intent intent = new Intent(this, QosAlarmReceiver.class);
 
 		PendingIntent sender = PendingIntent.getBroadcast(this, QOS_ALARM,
 				intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		if (sender != null)
 		{
 			// Get the AlarmManager service
 			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 			am.cancel(sender);
 		}
 	}
 
 }
 
