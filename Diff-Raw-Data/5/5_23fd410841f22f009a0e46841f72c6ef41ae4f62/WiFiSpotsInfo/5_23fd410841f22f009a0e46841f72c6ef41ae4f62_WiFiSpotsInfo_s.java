 /* Copyright (c) 2012 cat_in_136
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.blogspot.catin136.android.toomanywifi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 
 /**
  * Wi-Fi AP information with SSID and the number of it.
  * 
  * @author cat_in_136
  */
 public class WiFiSpotsInfo implements Comparable<WiFiSpotsInfo> {
     /** the SSID. */
     private String ssid;
     
     /** the number of the Wi-Fi APs */
     private int count;
     
     /** Get the SSID. */
     public String getSSID() {
         return ssid;
     }
     /** Get the number of the Wi-Fi APs. */
     public int getCount() {
         return count;
     }
 
     /**
      * Constructor initialized with specific SSID and {@link #getCount()}=1.
      * 
      * @param ssid	the SSID.
      */
     public WiFiSpotsInfo(String ssid) {
 	this.ssid = ssid;
 	this.count = 1;
     }
     
     /** count up the AP. */
     private void countUp() {
 	this.count ++;
     }
 
     @Override
     public int compareTo(WiFiSpotsInfo another) {
 	int diff;
 	
	diff = this.getCount() - another.getCount();
 	if (diff == 0) {
 	    diff = this.getSSID().compareTo(another.getSSID());
 	}
 	
 	return diff;
     }
     
     /** Convenience method returning whether Wi-Fi is enabled or disabled. */
     public static boolean isWiFiEnabled(Activity activity) {
 	WifiManager wifimanager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
 	return wifimanager.isWifiEnabled();
     }
     
     /** Get Wi-Fi scan result map. The key is SSID and the value is {@link WiFiSpotsInfo}. */
     protected static Map<String, WiFiSpotsInfo> getScanResultSSIDs(Activity activity) {
 	WifiManager wifimanager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
 	HashMap<String, WiFiSpotsInfo> countmap = new HashMap<String, WiFiSpotsInfo>();
 	List<ScanResult> results = wifimanager.getScanResults();
 	
 	if (results != null) {
 	    for (ScanResult result : results) {
 		if (!countmap.containsKey(result.SSID)) {
 		    WiFiSpotsInfo info = new WiFiSpotsInfo(result.SSID);
 		    countmap.put(result.SSID, info);
 		} else {
 		    WiFiSpotsInfo info = countmap.get(result.SSID);
 		    info.countUp();
 		}
 	    }
 	} else {
 	    // no item shall return a zero-size map object.
 	}
 	
 	// a read-only map shall be return!
 	return Collections.unmodifiableMap(countmap);
     }
     
     /** Get Wi-Fi scan results as a sorted list. */
     public static List<WiFiSpotsInfo> getScanResults(Activity activity) {
 	Map<String, WiFiSpotsInfo> ssids = getScanResultSSIDs(activity);
 	List<WiFiSpotsInfo> info = new ArrayList<WiFiSpotsInfo>(ssids.values());
 	
 	Collections.sort(info);
 	
 	// a read-only list shall be return!
 	return Collections.unmodifiableList(info);
     }
 
 }
