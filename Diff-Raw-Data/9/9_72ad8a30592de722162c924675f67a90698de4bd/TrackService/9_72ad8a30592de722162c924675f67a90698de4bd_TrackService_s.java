 /**
 
 
  * 
  */
 package com.aboveware.abovetracker;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.google.android.maps.GeoPoint;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * @author SåA
  * 
  */
 public class TrackService extends Service {
 
 	public enum Mode {
 		NOT_STARTED, PAUSED, RUNNING, STOPPED
 	}
 
 	/**
 	 * Class for clients to access. Because we know this service always runs in
 	 * the same process as its clients, we don't need to deal with IPC.
 	 */
 	public class TrackServiceBinder extends Binder {
 		TrackService getService() {
 			return TrackService.this;
 		}
 	}
 
 	interface TrackServiceListener {
 		void LocationChange(Location location);
 
 		void ModeChange(Mode mode);
 	}
 
 	static final String REMOTE_COMMAND = "REMOTE_COMMAND";
 	static final String REMOTE_ORGINATOR = "REMOTE_ORGINATOR";
 
 	public static final String IS_REMOTE_COMMAND = "IS_REMOTE_COMMAND";
 
 	private TrackDbAdapter dbHelper = null;
 
 	private long lastRecordCount = 0;
 
 	private List<LocationListener> locationListeners = new ArrayList<LocationListener>();
 
 	// This is the object that receives interactions from clients.
 	private final IBinder mBinder = new TrackServiceBinder();
 
 	private NotificationManager mNotificationManager;
 
 	private Mode mode = Mode.NOT_STARTED;
 
 	// Unique Identification Number for the Notification.
 	// We use it on Notification start, and to cancel it.
 	private int NOTIFICATION = R.string.pause;
 
 	private Timer smsTimer = null;
 
 	private List<TrackServiceListener> trackServiceListeners = new ArrayList<TrackServiceListener>();
 
 	// To whom we should sms
 	private String address;
 
 	void AddListener(TrackServiceListener listener) {
 		if (null != listener) {
 			synchronized (trackServiceListeners) {
 				trackServiceListeners.add(listener);
 			}
 		}
 	}
 
 	/**
 	 * @return the mode
 	 */
 	public Mode getMode() {
 		return mode;
 	}
 
 	/**
 	 * @param entry
 	 */
 	private void newDbEntry(String entry) {
 		if (null != dbHelper) {
 			dbHelper.close();
 		}
 		stopTimer();
 		dbHelper = new TrackDbAdapter(getBaseContext());
 		dbHelper.open();
 		dbHelper.insertTrack(entry);
 	}
 
 	private void NotifyModeChange() {
 		synchronized (trackServiceListeners) {
 			for (TrackServiceListener trackServiceListener : trackServiceListeners) {
 				trackServiceListener.ModeChange(mode);
 			}
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		if (Mode.NOT_STARTED == mode) {
 			mode = Mode.STOPPED;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		// Cancel the persistent notification.
 		mNotificationManager.cancel(NOTIFICATION);
 		if (dbHelper != null) {
 			dbHelper.close();
 		}
 		dbHelper = null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		address = Preferences.getSmsRecipient(this);
 		if (intent.getBooleanExtra(IS_REMOTE_COMMAND, false)) {
 			String command = intent.getStringExtra(REMOTE_COMMAND);
 			address = intent.getStringExtra(REMOTE_ORGINATOR);
 			SmsParser smsParser = new SmsParser(command);
 			if (smsParser.parse(Preferences.getTriggerKeyword(this))) {
 				if (smsParser.isStart()) {
 					start(smsParser.getTrack());
 				}
 
 				if (smsParser.isStop()) {
 					stop();
 				}
 
 				if (smsParser.isPause()) {
 					pause();
 				}
 
 				if (smsParser.isLocation()) {
 					location(smsParser.getCallback());
 				}
 
 				if (smsParser.isRing()) {
 					ring();
 				}
 			}
 		}
 		NotifyModeChange();
 		return START_STICKY;
 	}
 
 	private void ring() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void location(String callback) {
 		if (null != callback && callback.length() != 0){
 			address = callback;
 		}
 		String message = "";
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		List<String> providers = locationManager.getAllProviders();
 		for (String provider : providers) {
 			Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
 			if (null != lastKnownLocation){
 				message = ConvertPointToLocation(this, geoPoint(lastKnownLocation.getLatitude(),
 						lastKnownLocation.getLongitude()));
 				message += " ";
 				message += "http://maps.google.com/maps?q=";
 				message += String.valueOf(lastKnownLocation.getLatitude());
 				message += ",";
 				message += String.valueOf(lastKnownLocation.getLongitude());
 				sms(message);
 				return;
 			}
 		}
 		sms(getString(R.string.no_location_found_));
 	}
 
 	public void pause() {
 		stopSmsTimer(true);
 		stopLocationManager();
 		mode = Mode.PAUSED;
 		showNotification();
 	}
 
 	public void resume() {
 		startSmsTimer(Mode.PAUSED == mode);
 		startLocationManager();
 		mode = Mode.RUNNING;
 		showNotification();
 	}
 
 	private void pauseSms() {
 		sms("Pause sms");
 	}
 
 	private void resumeSms() {
 		sms("Resume sms");
 	}
 
 	private void sms(String message) {
 		if (Preferences.getSmsEnabled(this)) {
 			SmsSenderTask smsSenderTask = new SmsSenderTask(getBaseContext());
 			smsSenderTask.execute(new String[] { message, address });
 		}
 	}
 
 	private void startSms() {
 		lastRecordCount = 0;
 		sms("Start sms");
 	}
 
 	private void stopSms() {
 		sms("Stop sms");
 	}
 
 	/**
 	 * Send a SMS with the tracking data but only if there has been any new data
 	 * stored in the database.
 	 */
 	private void sendTrackingSms() {
 		long currentRecordCount = dbHelper.getCount(dbHelper.getTrackId());
 		if (lastRecordCount != currentRecordCount) {
 			lastRecordCount = currentRecordCount;
 			sms("Tracking sms");
 		}
 	}
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	private void showNotification() {
 		// In this sample, we'll use the same text for the ticker and the expanded
 		// notification
 		CharSequence text = getString(R.string.tracking_is_on);
 		switch (getMode()) {
 		case STOPPED:
 			text = getString(R.string.tracking_is_off);
 			break;
 		case PAUSED:
 			text = getString(R.string.tracking_is_paused);
 			break;
 		}
 
 		// Set the icon, scrolling text and time stamp
		Notification notification = new Notification(R.drawable.icon, text,
 		    System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
 		// The PendingIntent to launch our activity if the user selects this
 		// notification
 		PendingIntent contentIntent = PendingIntent
 		    .getActivity(this, 0, new Intent(this, MainActivity.class),
 		        PendingIntent.FLAG_CANCEL_CURRENT);
 
 		// Set the info for the views that show in the notification panel.
 		notification.setLatestEventInfo(this, text, text, contentIntent);
 
 		// startForeground(NOTIFICATION, notification);
 		// Send the notification.
 		mNotificationManager.notify(NOTIFICATION, notification);
 	}
 
 	/**
 	 * Set up the location manager listeners. Any old once are first removed.
 	 * 
 	 */
 	private void startLocationManager() {
 		stopLocationManager();
 		synchronized (locationListeners) {
 			locationListeners.clear();
 		}
 
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		List<String> providers = locationManager.getAllProviders();
 		for (String provider : providers) {
 			LocationListener listener = new LocationListener() {
 				private void NotifyLocationChange(Location location) {
 					synchronized (trackServiceListeners) {
 						for (TrackServiceListener trackServiceListener : trackServiceListeners) {
 							trackServiceListener.LocationChange(location);
 						}
 					}
 				}
 
 				// @Override
 				public void onLocationChanged(Location location) {
 					if (location != null) {
 						dbHelper.insertRecord(location);
 						NotifyLocationChange(location);
 						Log.w(AboveTracker.TAG, "Id " + dbHelper.getTrackId() + " count "
 						    + dbHelper.getCount(dbHelper.getTrackId()));
 					}
 				}
 
 				// @Override
 				public void onProviderDisabled(String provider) {
 					Toast.makeText(getBaseContext(), provider + " disabled",
 					    Toast.LENGTH_SHORT).show();
 				}
 
 				// @Override
 				public void onProviderEnabled(String provider) {
 					Toast.makeText(getBaseContext(), provider + " enabled",
 					    Toast.LENGTH_SHORT).show();
 				}
 
 				// @Override
 				public void onStatusChanged(String provider, int status, Bundle extras) {
 					Toast.makeText(getBaseContext(),
 					    provider + " status changed to " + status, Toast.LENGTH_SHORT)
 					    .show();
 				}
 
 			};
 			locationManager.requestLocationUpdates(provider,
 			    Preferences.getMinTime(this), Preferences.getMinDistance(this),
 			    listener);
 			synchronized (locationListeners) {
 				locationListeners.add(listener);
 			}
 		}
 
 	}
 
 	private void startSmsTimer(boolean resuming) {
 		if (null == smsTimer) {
 			smsTimer.cancel();
 		}
 		smsTimer = new Timer();
 
 		if (resuming) {
 			resumeSms();
 		} else {
 			startSms();
 		}
 
 		final long smsInterval = Preferences.getSmsInterval(this);
 		smsTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				sendTrackingSms();
 			}
 		}, smsInterval, smsInterval);
 	}
 
 	/**
 	 * Start or resume a tracking session.
 	 * 
 	 * @param trackName
 	 *          the verbose database entry for this tracking session.
 	 */
 	public void start(String trackName) {
 		if (Mode.STOPPED == mode) {
 			newDbEntry(trackName);
 		}
 		resume();
 	}
 
 	/**
 	 * Stop all added location listeners.
 	 */
 	protected void stopLocationManager() {
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		// Synchronized because there may be multiple listeners running and
 		// we don't want them to both try to alter the listeners collection
 		// at the same time.
 		synchronized (locationListeners) {
 			for (LocationListener listener : locationListeners) {
 				locationManager.removeUpdates(listener);
 			}
 		}
 	}
 
 	private void stopSmsTimer(boolean pausing) {
 		stopTimer();
 		if (pausing) {
 			pauseSms();
 		} else {
 			stopSms();
 		}
 	}
 
 	private void stopTimer() {
 		if (null != smsTimer) {
 			smsTimer.cancel();
 		}
 		smsTimer = new Timer();
 	}
 
 	public void stop() {
 		stopSmsTimer(false);
 		stopLocationManager();
 		mNotificationManager.cancel(NOTIFICATION);
 		mode = Mode.STOPPED;
 	}
 
 	public static GeoPoint geoPoint(double latitude, double longitude){
 		return new GeoPoint((int) (latitude * 1E6),	(int) (longitude * 1E6));
 	}
 	
 	public static String ConvertPointToLocation(Context context, GeoPoint point) {
 		String address = "";
 		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
 		try {
 			List<Address> addresses = geoCoder.getFromLocation(
 			    point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 1);
 
 			if (addresses.size() > 0) {
 				for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++)
 					address += addresses.get(0).getAddressLine(index) + " ";
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return address;
 	}}
