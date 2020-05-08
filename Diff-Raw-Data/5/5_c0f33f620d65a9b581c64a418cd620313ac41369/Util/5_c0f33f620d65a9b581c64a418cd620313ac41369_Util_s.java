 /**
  * @author fatih
  */
 package edu.buffalo.cse.phonelab.utilities;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.TelephonyManager;
 import edu.buffalo.cse.phonelab.phonelabservices.PhoneLabMainView;
 import edu.buffalo.cse.phonelab.phonelabservices.R;
 
 public class Util {
 	public static final String MANIFEST_DOWNLOAD_URL = "http://107.22.187.240/manifest/";//url to download manifest
 	public static final String NEW_MANIFEST_DIR = "new_manifest.xml";//directory to download new manifest
 	public static final String CURRENT_MANIFEST_DIR = "manifest.xml";//directory for current, latest manifest 
 	public static final String APP_DOWNLOAD_URL = "http://107.22.187.240/experiment/";//base url to download application 
 	public static final String OTA_DOWNLOAD_URL = "http://download.phone-lab.org/ota/";//base url to download OTA Image
 	public static final String MANIFEST_UPLOAD_URL = "http://50.19.247.145/phonelab/upload_manifest.php";
 	public static final String DEVICE_STATUS_UPLOAD_URL = "http://107.22.187.240/devicestatus/";
 	public static final String URLTOUPLOAD = "http://107.22.187.240/device/";
 	public static final String APPLICATION_ACTION_URL = "http://107.22.187.240/deviceapplication/";
 	
 	public static final String SHARED_PREFERENCES_FILE_NAME = "phonelab_settings";
 	public static final String SHARED_PREFERENCES_REG_ID_KEY = "reg_id";
 	public static final String SHARED_PREFERENCES_SYNC_KEY = "is_synced";
 	public static final String SHARED_PREFERENCES_DATA_LOGGER_PID = "logcat_pid";
 	public static final String SHARED_PREFERENCES_DATA_LOGGER_LAST_UPDATE_TIME = "data_logger_last_updated";
 	public static final String SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG = "log_wifi";
 	public static final String SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG = "log_power";
 	public static final String SHARED_PREFERENCES_POWER_CONNECTED = "power_connected";
 	public static final String SHARED_PREFERENCES_LOCATION_SOURCE = "location_source";//possible values: network, gps, both
 	//added for logcat filters
 	public static final String SHARED_PREFERENCES_LOGCAT_FILTERS = "logcat_filters";
 	
 	//Peridic Checks
	public static final long PERIODIC_CHECK_INTERVAL = 360 * 1000;//5 minutes
 	
 	//C2DM
 	public static final String C2DM_EMAIL = "phone.lab.buffalo@gmail.com";
 	
 	//Data logger Constants
 	public final static long UPDATE_INTERVAL = 1000*120;//2 mins
 	public final static String TAG = "LogService";
	public final static int LOG_FILE_SIZE = 30;//KB
 	public final static int AUX_LOG_FILES = 20;
 	public final static String LOG_DIR = ".log";
 	public final static long THRESHOLD = 25;//KB
 	public final static String POST_URL = "http://107.22.187.240/log/";
 	
 	public static String getDeviceId (Context context) {
 		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getDeviceId();
 	}
 	
 	/**
 	 * send Notification message to device user and points him to PhonelabMainView Class
 	 * @param context
 	 * @param Header
 	 * @param Message
 	 */
 	public void nofityUser(Context context,String Header, String Message) {
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
 		int icon = R.drawable.ic_launcher;
 		CharSequence tickerText = Header;
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, tickerText, when);
 		Context appContext = context.getApplicationContext();
 		CharSequence contentTitle = Header;
 		CharSequence contentText = Message;
 		
 		Intent notificationIntent = new Intent(context, PhoneLabMainView.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
 		notification.setLatestEventInfo(appContext, contentTitle, contentText, contentIntent);
 		mNotificationManager.notify((int) when, notification);
 	}
 }
