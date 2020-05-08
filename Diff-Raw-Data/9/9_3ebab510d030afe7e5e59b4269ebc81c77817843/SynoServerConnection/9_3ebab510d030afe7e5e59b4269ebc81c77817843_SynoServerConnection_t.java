 /**
  * 
  */
 package com.bigpupdev.synodroid.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.preference.ListPreferenceMultiSelectWithValue;
 import com.bigpupdev.synodroid.preference.PreferenceFacade;
 
 import com.bigpupdev.synodroid.data.SynoProtocol;
 
 import android.util.Log;
 
 /**
  * A technical connection definition to a SynoServer
  * 
  * @author Eric Taix
  */
 public class SynoServerConnection {
 
 	// The protocol used to communicate with the server
 	public SynoProtocol protocol = SynoProtocol.HTTP;
 	// The hostname or ip address
 	public String host;
 	// The port
 	public Integer port = 5000;
 	// The refresh interval in seconds
 	public Integer refreshInterval = 10;
 	// The resfresh state (enable or disable autorefresh)
 	public boolean autoRefresh = true;
 	// Show (or not) the upload progress in the main activity
 	public boolean showUpload = true;
 	// Wifi SSID allowed for this server (empty when used for a public connection)
 	public List<String> wifiSSID = new ArrayList<String>();
 
 	/**
 	 * Create an instance of SynoServerConnection
 	 * 
 	 * @param props
 	 * @return
 	 */
 	public static SynoServerConnection createFromProperties(boolean local, Properties props, boolean debug) {
 		SynoServerConnection result = null;
 		try {
 			String radical = "";
 			boolean valid = false;
 			if (local) {
 				radical += PreferenceFacade.WLAN_RADICAL;
 				String usewifi = props.getProperty(radical + PreferenceFacade.USEWIFI_SUFFIX);
 				if (usewifi != null && usewifi.equals("true")) {
 					valid = true;
 				}
 			} else {
 				String useext = props.getProperty(radical + PreferenceFacade.USEEXT_SUFFIX);
 				if (useext != null && useext.equals("true")) {
 					valid = true;
 				}
 			}
 
 			if (valid) {
 				SynoProtocol protocol = SynoProtocol.valueOf(props.getProperty(radical + PreferenceFacade.PROTOCOL_SUFFIX));
				int port = 5000;
				try{
					port = Integer.parseInt(props.getProperty(radical + PreferenceFacade.PORT_SUFFIX));
				}
				catch (NumberFormatException numEx){
					if (debug) Log.w(Synodroid.DS_TAG, "An invalid port number was detected while loading " + (local ? "local" : "public") + " connection. This connection will be ignored.");
				}
				
 				String host = props.getProperty(radical + PreferenceFacade.HOST_SUFFIX);
 				if (protocol != null && port != 0 && host != null && host.length() > 0) {
 					result = new SynoServerConnection();
 					result.protocol = protocol;
 					result.host = host;
 					result.port = port;
 					result.showUpload = Boolean.parseBoolean(props.getProperty(radical + PreferenceFacade.SHOWUPLOAD_SUFFIX));
 					result.refreshInterval = Integer.parseInt(props.getProperty(radical + PreferenceFacade.REFRESHVALUE_SUFFIX));
 					result.autoRefresh = Boolean.parseBoolean(props.getProperty(radical + PreferenceFacade.REFRESHSTATE_SUFFIX));
 					if (local) {
 						String separatedValues = props.getProperty(radical + PreferenceFacade.SSID_SUFFIX);
 						String[] ssids = ListPreferenceMultiSelectWithValue.parseStoredValue(separatedValues);
 						if (ssids != null) {
 							result.wifiSSID = Arrays.asList(ssids);
 						} else {
 							result = null;
 						}
 					} else {
 						result.wifiSSID = null;
 					}
 				}
 			}
 		} catch (Exception ex) {
 			if (debug) Log.e(Synodroid.DS_TAG, "An exception occured while loading " + (local ? "local" : "public") + " connection", ex);
 		}
 		return result;
 	}
 }
