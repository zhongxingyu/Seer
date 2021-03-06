 package ca.mcgill.hs.plugin;
 
 import java.util.List;
 
 import ca.mcgill.hs.R;
 import ca.mcgill.hs.util.PreferenceFactory;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * Reads Wifi data.
  * 
  * @author Cicerone Cojocaru, Jonathan Pitre
  *
  */
 public class WifiLogger extends InputPlugin{
 	
 	private final boolean PLUGIN_ACTIVE;
 	
 	//The Thread for requesting scans.
 	private Thread wifiLoggerThread;
 	
 	//A boolean detailing whether or not the Thread is running.
 	private boolean threadRunning = false;
 	
 	//A WifiManager used to request scans.
 	private final WifiManager wm;
 	
 	//The interval of time between two subsequent scans.
 	private int sleepIntervalMillisecs;
 	
 	//The WifiLoggerReceiver from which we will get the Wifi scan results.
 	private WifiLoggerReceiver wlr;
 	
 	//The Context in which the WifiLoggerReceiver will be registered.
 	private final Context context;
 	
 	//Variables used to write out the Wifi data received.
 	int numResults;
 	long timestamp;
 	int[] levels;
 	String[] SSIDs;
 	String[] BSSIDs;
 		
 	/**
 	 * This is the basic constructor for the WifiLogger plugin. It has to be instantiated
 	 * before it is started, and needs to be passed a reference to a WifiManager and a Context.
 	 * 
 	 * @param wm - the WifiManager for this WifiLogger.
 	 * @param context - the context in which this plugin is created.
 	 */
 	public WifiLogger(WifiManager wm, Context context){
 		this.wm = wm;
 		this.context = context;
 		
 		SharedPreferences prefs = 
     		PreferenceManager.getDefaultSharedPreferences(context);
 		sleepIntervalMillisecs = Integer.parseInt(prefs.getString("wifiIntervalPreference", "30000"));
 		
 		PLUGIN_ACTIVE = prefs.getBoolean("wifiLoggerEnable", false);
 	}
 	
 	/**
 	 * This method starts the WifiLogger plugin and launches all appropriate threads. It
 	 * also registers a new WifiLoggerReceiver to scan for possible network connections.
 	 * This method must be overridden in all input plugins.
 	 * 
 	 * @override
 	 */
 	public void startPlugin() {
 		if (!PLUGIN_ACTIVE) return;
 		
 		wlr = new WifiLoggerReceiver(wm);
 		context.registerReceiver(wlr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		Log.i("WifiLogger", "Registered receiver.");
 		
 		wifiLoggerThread = new Thread() {
 			public void run() {
 				try {
 					while(threadRunning) {
 						Log.i("WifiLogger", "Scanning results.");
 						wm.startScan();
 						sleep(sleepIntervalMillisecs);
 					}
 				}
 				catch(InterruptedException e) {
 					Log.e("WifiLogger", "Logging thread terminated due to InterruptedException.");
 				}
 			}
 		};
 		wifiLoggerThread.start();
 		threadRunning = true;
 	}
 
 	/**
 	 * This method stops the thread if it is running, and does nothing if it is not.
 	 * 
 	 * @override
 	 */
 	public void stopPlugin() {
 		if (threadRunning){
 			threadRunning = false;
 			context.unregisterReceiver(wlr);
 			Log.i("WifiLogger", "Unegistered receiver.");
 		}
 	}
 	
 	/**
 	 * Processes the results sent by the Wifi scan and writes them out.
 	 */
 	private void processResults(List<ScanResult> results){
 		
 		numResults = results.size();
 		timestamp = System.currentTimeMillis();
 		levels = new int[numResults];
 		SSIDs = new String[numResults];
 		BSSIDs = new String[numResults];
 		
 		int i = 0;
 		for (ScanResult sr : results){
 			levels[i] = sr.level;
 			SSIDs[i] = sr.SSID;
 			BSSIDs[i] = sr.BSSID;
 			i++;
 		}
 		
 		write(new WifiLoggerPacket(numResults, timestamp, levels, SSIDs, BSSIDs));
 		
 	}
 	
 	/**
 	 * Returns the list of Preference objects for this InputPlugin.
 	 * 
 	 * @param c the context for the generated Preferences.
 	 * @return an array of the Preferences of this object.
 	 * 
 	 * @override
 	 */
 	public static Preference[] getPreferences(Context c) {
 		Preference[] prefs = new Preference[2];
 		
 		prefs[0] = PreferenceFactory.getCheckBoxPreference(c, "wifiLoggerEnable",
 				"Wifi Plugin", "Enables or disables this plugin.",
 				"WifiLogger is on.", "WifiLogger is off.");
 		
 		prefs[1] = PreferenceFactory.getListPreference(c, R.array.wifiLoggerIntervalStrings,
 				R.array.wifiLoggerIntervalValues, "30000", "wifiIntervalPreference",
 				R.string.wifilogger_interval_pref, R.string.wifilogger_interval_pref_summary);
 		
 		return prefs;
 	}
 	
 	/**
 	 * Returns whether or not this InputPlugin has Preferences.
 	 * 
 	 * @return whether or not this InputPlugin has preferences.
 	 */
 	public static boolean hasPreferences(){ return true; }
 	
 	// ***********************************************************************************
 	// PRIVATE INNER CLASS -- WifiLoggerReceiver
 	// ***********************************************************************************
 	
 	/**
 	 * Taken from Jordan Frank (hsandroidv1.ca.mcgill.cs.humansense.hsandroid.service) and
 	 * modified for this plugin.
 	 */
 	private class WifiLoggerReceiver extends BroadcastReceiver {
 		
 		public WifiLoggerReceiver(WifiManager wifi) {
 			super();
 			this.wifi = wifi;
 		}
 
 		@Override
 		public void onReceive(Context c, Intent intent) {
 			final List<ScanResult> results = wifi.getScanResults();
 			Log.i("WifiLogger", "Received wifi results.");
 			processResults(results);
 		}
 		
 		private final WifiManager wifi;
 	}
 	
 	// ***********************************************************************************
 	// PUBLIC INNER CLASS -- WifiLoggerPacket
 	// ***********************************************************************************
 	
 	public class WifiLoggerPacket implements DataPacket{
 		
 		final int neighbors;
 		final long timestamp;
 		final int[] levels;
 		final String[] SSIDs;
 		final String[] BSSIDs;
 		
 		/**
 		 * Constructor for this DataPacket.
 		 * @param neighbors the number of access points detected.
 		 * @param timestamp the time of the scan.
 		 * @param level	the signal strength level of each access point.
 		 * @param SSID the SSID of each access point.
 		 * @param BSSID the BSSID of each access point.
 		 */
 		public WifiLoggerPacket(int neighbors, long timestamp, int[] level, String[] SSID, String[] BSSID){
 			this.neighbors = neighbors;
 			this.timestamp = timestamp;
 			this.levels = level;
 			this.SSIDs = SSID;
 			this.BSSIDs = BSSID;
 		}
 
 		@Override
 		public String getInputPluginName() {
 			return "WifiLogger";
 		}
 		
 		@Override
 		public DataPacket clone(){
 			return new WifiLoggerPacket(neighbors, timestamp, levels, SSIDs, BSSIDs);
 		}
 	}
 }
