 package edu.mit.csail.netmap.sensors;
 
 import java.util.List;
 
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.DhcpInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.WifiLock;
 import android.text.format.Formatter;
 
 public final class WiFi {
   /** Entry point to Android's WiFi functionality. */
   private static WifiManager wifiManager;
   
   /** Receives Android notifications when WiFi scan results are available. */
   private static ScanResultReceiver scanResultReceiver;
   
   /** Lock that prevents the WiFi radio from getting powered off. */
   private static WifiLock wifiLock = null;
   
   /** Most recent WiFi AP scan results. */
   private static List<ScanResult> scanResults;
   /** The time when the most recent WiFi AP scan results were received. */
   private static long scanResultsTimestamp;
   
   /** True when the WiFi is enabled by the user. */
   private static boolean enabled = false;
   
   /** True when the WiFi is powered up and reporting information. - not being used now*/
   private static boolean started = false;
   
   /** True when listening for WiFi updates. */
   private static boolean listening = false;
   
   /** Called by {@link Sensors#initialize(android.content.Context)}. */
   public static void initialize(Context context) {
     wifiManager = 
       (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
     scanResultReceiver = new ScanResultReceiver();
     context.registerReceiver(scanResultReceiver,
         new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); 
   }
   
   /**
    * Starts listening for location updates.
    * 
    * This should be called when your application / activity becomes active.
    */
   public static void start() {
     if (listening) return;
     // We only get onProviderDisabled() when we start listening.
     enabled = isEnabled();
     
     // TODO(yuhan): do we need this lock?
     wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,
         "NetMap-WiFiAPScan");
     wifiLock.acquire();
 
     // TODO(yuhan): does this do one scan, or does it scan continuously? if it
     //     only does one scan, we should probably make this a measurement; I
     //     know active scanning requires sending out data packets, so it might
     //     be super-expensive
     wifiManager.startScan();
     
     listening = true;
   }
 
   /** 
    * Stops listening for location updates.
    * 
    * This should be called when your application / activity is no longer active.
    */
   public static void stop() {
     if (!listening) return;
     wifiLock.release();
     wifiLock = null;
     listening = false;
   }
   
   /**
    * Checks if the user's preferences allow the use of WiFi.
    * 
    * @return true if the user lets us use WiFi
    */
   public static boolean isEnabled() {
     return wifiManager.isWifiEnabled();
   }
   
   /**
    * Writes a JSON representation of the WiFi data to the given buffer.
    * 
    * @param buffer a {@link StringBuffer} that receives a JSON representation of
    *     the WiFisensor data
    */
   @SuppressWarnings("deprecation")
   public static void getJson(StringBuffer buffer) {
     buffer.append("{\"enabled\":");
     buffer.append(enabled ? "true" : "false");
     
     WifiInfo wifiInfo = wifiManager.getConnectionInfo();
     if (wifiInfo != null) {
       buffer.append(",\"connection\":{\"ssid\":");
       buffer.append(JSONObject.quote(wifiInfo.getSSID()));
       buffer.append(",\"hidden\":");
       buffer.append(wifiInfo.getHiddenSSID() ? "true" : "false");
       buffer.append(",\"bssid\":\"");
       buffer.append(wifiInfo.getBSSID());
       buffer.append("\",\"mac\":\"");
       buffer.append(wifiInfo.getMacAddress());
       buffer.append("\",\"rssi\":\"");
       buffer.append(wifiInfo.getRssi());
       buffer.append("\",\"linkMbps\":");
       buffer.append(wifiInfo.getLinkSpeed());
       buffer.append(",\"state\":\"");
       buffer.append(wifiInfo.getSupplicantState().toString());
       buffer.append("\",\"ip\":\"");
       buffer.append(Formatter.formatIpAddress(wifiInfo.getIpAddress()));
       buffer.append("\"}");
     }
     DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
     if (dhcpInfo != null) {
       buffer.append(",\"dhcp\":{\"ip\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.ipAddress));
       buffer.append("\",\"netmask\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.netmask));
       buffer.append("\",\"gateway\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.gateway));
       buffer.append("\",\"dhcpServer\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.serverAddress));
       buffer.append("\",\"dns1\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.dns1));
       buffer.append("\",\"dns2\":\"");
       buffer.append(Formatter.formatIpAddress(dhcpInfo.dns2));
       buffer.append("\",\"lease\":");
       buffer.append(dhcpInfo.leaseDuration);
       buffer.append("}");
     }
     
     if (scanResults != null) {
       buffer.append(",\"aps\":[");      
       boolean firstElement = true;
       for (ScanResult scanResult : scanResults) {
         if (firstElement) {
           firstElement = false;
           buffer.append("{\"ssid\":");
         } else {
           buffer.append(",{\"ssid\":");
         }
         String ssid = scanResult.SSID;
         buffer.append(JSONObject.quote(ssid));
         buffer.append(",\"bssid\":\"");
         buffer.append(scanResult.BSSID);
         buffer.append("\",\"channelMhz\":");
         buffer.append(scanResult.frequency);
         buffer.append(",\"signalDb\":");
         buffer.append(scanResult.level);
         buffer.append(",\"timestamp\":");
         // TODO(pwnall, yuhan): API 17 has scanResult.timestamp; look into using
         //     that when available
         buffer.append(scanResultsTimestamp);
         buffer.append(",\"capabilities\":\"");
         buffer.append(scanResult.capabilities);
         buffer.append("\"}");
       }
       buffer.append("]");
     }
     buffer.append("}");
   }
   
   private static class ScanResultReceiver extends BroadcastReceiver {
     public void onReceive(Context c, Intent intent) {
       scanResultsTimestamp = System.currentTimeMillis();
       scanResults = wifiManager.getScanResults();
     }
   }
 }
