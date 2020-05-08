 package org.fourdnest.androidclient.services;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.Tag;
 import org.fourdnest.androidclient.comm.FourDNestProtocol;
 import org.fourdnest.androidclient.comm.OsmStaticMapGetter;
 import org.fourdnest.androidclient.comm.StaticMapGetter;
 import org.fourdnest.androidclient.comm.ThumbnailManager;
 import org.fourdnest.androidclient.tools.LocationHelper;
 import org.fourdnest.androidclient.ui.ListStreamActivity;
 import org.fourdnest.androidclient.ui.NewEggActivity;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.text.format.DateFormat;
 import android.text.method.DateTimeKeyListener;
 import android.util.Log;
 import android.widget.Toast;
 
 public class RouteTrackService
 	extends Service
 	implements LocationListener
 	,OnSharedPreferenceChangeListener{
 	
 	/**
 	 * Location manager to access location info
 	 */
 	private LocationManager locationManager;
 
 	/**
 	 * Task bar notification
 	 */
 	private Notification notification;
 	private final int NOTIFICATION_ID = 410983;
 	
 	/**
 	 * Output file
 	 */
 	private File outputFile;
 	
 	/**
 	 * Last known location
 	 */
 	private Location lastLocation;
 	
 	/**
 	 * Start date of track, used for file name & egg
 	 */
 	private Date startDate;
 	
 	private String provider = "gps"; // Fixed provider
 	
 	private static final File FILE_BASE_PATH = Environment.getExternalStorageDirectory();
 	private static final String FILE_DIRECTORY = "fourdnest" + File.separator + "routes";
 	private static final String FILE_EXTENSION = ".json";
 	private static final String JSON_LOC_SEPARATOR = "\n";
 	
 	private final static String TAG = RouteTrackService.class.getSimpleName();
 	
 	private final String MIN_DELAY_SETTING_KEY = "gps_update_frequency";
 	private final String MIN_DISTANCE_SETTING_KEY = "gps_update_mindistance";
 	private int minDelay = 1000; // ms
 	private float minDistance = 5; // m
 	private final int LATEST_LOC_MAX_DELAY = 1000 * 60; // 60 sec 
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		Log.d(TAG, "onCreate");
 		// Init pref manager and read relevant config
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		this.readPreferences(prefs);
 		
 		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		this.registerListener();
         
         this.outputFile = null;
         this.lastLocation = null;
         
 	}
 	
 	@Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(TAG, "onStart");
 		// If gps provider does not exist or is disabled, show error and die
 		if(this.locationManager.getProvider(this.provider) == null ||
 			!this.locationManager.getProviders(true).contains(this.provider)) {			
 			this.displayToast(getText(R.string.gps_error_outofservice));
 			stopSelf();
 		}
 
 		// Prepare notification message for status bar
 		this.notification = new Notification(R.drawable.icon, getText(R.string.gps_tracking_initialized), System.currentTimeMillis());
 
 		// Initialize start date to current time
 		this.startDate = new Date();
 				
 		// Prepare intent to start desired activity when notification is clicked
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ListStreamActivity.class), 0);        
 
         // Set status bar info
 		this.notification.setLatestEventInfo(this, getText(R.string.gps_statusbar_title), getText(R.string.gps_tracking_initialized), contentIntent);
 
         // Start service in foreground, checking for null to avoid Android testing bug
         if(getSystemService(ACTIVITY_SERVICE) != null) {
         	this.startForeground(NOTIFICATION_ID, this.notification);
         }
         
 		this.outputFile = this.getOutputFile();
 		if(this.outputFile == null) {
 			this.displayToast(getText(R.string.gps_error_file_open));
 			stopSelf();
 		}
 		
 		
 		
 		// Check last known location, if it's more recent than max delay specifies, add it as first point
 		Location lastKnownLocation = this.locationManager.getLastKnownLocation(this.provider);
 		if(lastKnownLocation != null && lastKnownLocation.getTime() < LATEST_LOC_MAX_DELAY) {
 			this.writeLocation(lastKnownLocation, this.outputFile);
 		}
         
         // Run until explicitly stopped
         return START_STICKY;
     }
 	
 	@Override
 	public void onDestroy() {
 		
 		Log.d(TAG, "onDestroy");
 		// Remove location updating
 		this.locationManager.removeUpdates(this);
 		
 		// Cancel the persistent notification
 		if(getSystemService(ACTIVITY_SERVICE) != null) {
 			this.stopForeground(true);
 		}
 		
 		// Create and save draft egg
 		Egg egg = this.createEggForCurrentRoute();
 		if(egg == null) {
 			this.displayToast(getText(R.string.gps_egg_create_fail));
 		} else {
 			
 			// Launch intent to edit route egg
 			Intent editIntent = new Intent(this.getApplication(), NewEggActivity.class);
 			editIntent.putExtra(NewEggActivity.EXTRA_EGG_ID, egg.getId());
 			editIntent.setAction(Intent.ACTION_VIEW);
 			editIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			
 			this.getApplication().startActivity(editIntent);
 		}
 		
 
 	}
 
 	public void onLocationChanged(Location location) {
 		Log.d(TAG, "onLocationChanged: " + LocationHelper.locationToJSON(location).toString());
 		this.displayNotification(getText(R.string.gps_statusbar_tracking_active));
 		
 		// Add some sanity checks, for now just write to output file
 		this.writeLocation(location, this.outputFile);
 		this.lastLocation = location;
 	}
 
 	public void onProviderDisabled(String provider) {
 		Log.d(TAG, "onProviderDisabled: " + provider);
 	}
 
 	public void onProviderEnabled(String provider) {
 		Log.d(TAG, "onProviderEnabled: " + provider);		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		Log.d(TAG, "onStatusChanged, provider: " + provider + ", status: " + status);
 		
 		if(provider == this.provider) {
 			CharSequence message = "";
 			
 			switch(status) {
 			case LocationProvider.OUT_OF_SERVICE:
 				message = getText(R.string.gps_error_outofservice);
 				break;
 			case LocationProvider.TEMPORARILY_UNAVAILABLE:
 				message = getText(R.string.gps_error_temporarilydisabled);
 				break;
 			case LocationProvider.AVAILABLE:
 				message = getText(R.string.gps_available);
 				break;
 			default:
 				return;
 			}
 			
 			this.displayNotification(message);
 			this.displayToast(message);
 		}
 	}
 	
 	/**
 	 * Called whenever preferences change
 	 */
 	public synchronized void onSharedPreferenceChanged(
             SharedPreferences sharedPreferences, String key) {
 		
 		Log.d(TAG, "onSharedPrefChanged: " + key);
 		if(key.equals(MIN_DELAY_SETTING_KEY) || key.equals(MIN_DISTANCE_SETTING_KEY)) {
 			this.readPreferences(sharedPreferences);
 			this.registerListener();
 		}
 		
 	}
 	
 	/**
 	 * Reads preferences from given preference object, validates them and either uses
 	 * them or reverts invalid values in the pref object
 	 * @param sharedPreferences to read prefs from
 	 */
 	private synchronized void readPreferences(SharedPreferences sharedPreferences) {
 		int newFreq = this.minDelay;
 		try {
			newFreq = sharedPreferences.getInt(MIN_DELAY_SETTING_KEY, this.minDelay);
 			if(newFreq >= 1000 && newFreq <= 600000) {
 				this.minDelay = newFreq;
 			} else { // Invalid value, rewrite setting
 				throw new NumberFormatException();
 			}
 		} catch(NumberFormatException e) {
 			sharedPreferences.edit().putInt(MIN_DELAY_SETTING_KEY, this.minDelay).commit();
 		}
 		
 		float newDist = this.minDistance;
 		try {
			newDist = sharedPreferences.getFloat(MIN_DISTANCE_SETTING_KEY, this.minDistance);
 			if(newDist >= 0 && newDist <= 1000) {
 				this.minDistance = newDist;
 			} else {
 				throw new NumberFormatException();
 			}
 		} catch(NumberFormatException e) {
 			sharedPreferences.edit().putFloat(MIN_DISTANCE_SETTING_KEY, this.minDistance).commit();
 		}
 	}
 	
 	/**
 	 * Unregisters location update listener and re-registers it with current settings
 	 */
 	private synchronized void registerListener() {
 		// Unregister old listening, register new
 		this.locationManager.removeUpdates(this);				
 		this.locationManager.requestLocationUpdates(
         		this.provider,
         		this.minDelay,
         		this.minDistance,
         		this
         		);
 	}
 	
 	/**
 	 * Helper for displaying status bar notification	 * 
 	 * @param message
 	 */
 	private void displayNotification(CharSequence message) {	
 		if(this.notification != null) {
 			Notification notification = new Notification(
 					this.notification.icon,
 					message,
 					System.currentTimeMillis()
 					);
 			
 			notification.setLatestEventInfo(
 					this,
 					getText(R.string.gps_statusbar_title), // Always the same title
 					message,
 					this.notification.contentIntent); // Don't change the configured return intent
 			
 			// (Re-)call startForeground to change status bar
 			this.startForeground(NOTIFICATION_ID, notification);
 			
 			this.notification = notification;
 		}
 	}
 	
 	/**
 	 * Helper for displaying a toast
 	 * @param message
 	 */
 	private void displayToast(CharSequence message) {
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
 	
 	
 	private boolean writeLocation(Location loc, File outFile) {
 		if(loc == null) return false;
 
 		try {
 			JSONObject o = LocationHelper.locationToJSON(loc);
 	
 			FileWriter fw = new FileWriter(outFile, true); // Open & append
 			fw.write(o.toString() + JSON_LOC_SEPARATOR);
 			fw.close();
 		} catch(IOException e) {
 			Log.e(TAG, e.getMessage() + ": " + e.getStackTrace());
 			this.displayToast(getText(R.string.gps_file_save_failure));
 			return false;
 		}
 		
 		Log.d(TAG, "Location written: " + loc.toString() + ", " + outFile.getAbsolutePath());
 		return true;
 	}
 	
 	/**
 	 * Gets output file handle
 	 * @return Output file handle or null if file cannot be opened
 	 */
 	private File getOutputFile() {
 		File outputFile = null;
 		try {
 			// Get file name, check that it can be created. If not,
 			// get a file name with added seq number and try a few times
 			File baseDir = new File(FILE_BASE_PATH + File.separator + FILE_DIRECTORY);
 			if(!baseDir.exists()) { 
 				baseDir.mkdirs();
 			}
 			
 			outputFile = new File(baseDir, getFileName());
 			
 			int tryCounter = 0;
 			while(!outputFile.createNewFile()) {
 				tryCounter++;
 				if(tryCounter > 9) {
 					throw new IOException("Unable to open file for writing. Gave up after 10 tries." + outputFile.getAbsolutePath());
 				}
 				outputFile = new File(FILE_BASE_PATH, getFileName(tryCounter));
 			}
 		} catch(IOException e) {
 			Log.e(TAG, e.getMessage() + ": " + e.getStackTrace());
 		}
 		
 		return outputFile;		
 	}
 	
 	/**
 	 * Get file name for a file that is saved "now". Calls getFileName(0);
 	 * @return string filename
 	 */
 	private String getFileName() {
 		return this.getFileName(0);
 	}
 	/***
 	 * Get file name for a file. Takes optional parameter that adds _duplicateNum after
 	 * timestamp
 	 * @param duplicateNum
 	 * @return string filename
 	 */
 	private String getFileName(int duplicateNum) {
 		String f = DateFormat.format("yyyy-MM-dd_hhmmss", this.startDate).toString();
 		if(duplicateNum > 0) f += "_" + duplicateNum;
 		
 		f += FILE_EXTENSION;
 		return f;
 	}
 	
 	/**
 	 * Creates egg for current route and saves it to draft database
 	 * @return saved Egg or null on failure
 	 */
 	private Egg createEggForCurrentRoute() {
 		if(this.outputFile == null) return null;
 		
 		FourDNestApplication app = (FourDNestApplication)this.getApplication(); 
 		Nest currentNest = app.getCurrentNest();		
 		if(currentNest == null) {
 			Log.e(TAG, "Active nest not set, egg cannot be created");
 			return null;
 		}
 		
 		// Create the egg
 		Egg e = new Egg();
 		e.setAuthor(currentNest.getUserName());
 		e.setCaption("");
 		e.setCreationDate(this.startDate);		
 		e.setLocalFileURI(Uri.fromFile(this.outputFile));
 		Log.d(TAG, Uri.fromFile(this.outputFile).toString());
 		e.setNestId(currentNest.getId());
 		e.setTags(new ArrayList<Tag>());
 		
 		if(this.lastLocation != null) {
 			e.setLatitude(this.lastLocation.getLatitude());
 			e.setLongitude(this.lastLocation.getLongitude());
 		}
 		
 		e = app.getDraftEggManager().saveEgg(e);
 		
 		// Retrieve static map of the route as a thumbnail
 		
 		StaticMapGetter smg = new OsmStaticMapGetter();
 		smg.getStaticMap(e);
 		
 		return e;
 	}
 	
 }
