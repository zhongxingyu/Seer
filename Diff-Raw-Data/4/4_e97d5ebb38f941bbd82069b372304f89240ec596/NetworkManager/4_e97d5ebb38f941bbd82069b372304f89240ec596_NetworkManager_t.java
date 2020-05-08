 package foam.mongoose;
 
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import android.net.wifi.WifiManager;
 import android.content.BroadcastReceiver;
 import android.content.IntentFilter;
 import android.os.Handler;
 import android.os.Message;
 
 import android.net.wifi.WifiConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.InputStream;
 
 import java.net.URL;
 import java.net.HttpURLConnection;
 import android.os.SystemClock;
 
 public class NetworkManager {
 
     public enum State {
         IDLE, SCANNING, CONNECTED // what else?
     }
 
     WifiManager wifi;
 	BroadcastReceiver receiver;
     public State state;
     String SSID;
 
     String m_CallbackName;
     StarwispActivity m_Context;
     StarwispBuilder m_Builder;
 
     NetworkManager() {
         state = State.IDLE;
     }
 
     void Start(String ssid, StarwispActivity c, String name, StarwispBuilder b) {
         m_CallbackName=name;
         m_Context=c;
         m_Builder=b;
 		wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
         state = State.SCANNING;
         SSID = ssid;
         wifi.startScan();
 		receiver = new WiFiScanReceiver(SSID, this);
 		c.registerReceiver(receiver, new IntentFilter(
                                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 
         // todo - won't work from inside fragments
         m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"Scanning\"");
     }
 
     void Connect() {
         Log.i("starwisp", "Attemping connect to "+SSID);
 
         List<WifiConfiguration> list = wifi.getConfiguredNetworks();
 
         Boolean found = false;
 
         for( WifiConfiguration i : list ) {
             if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                 found = true;
                 Log.i("starwisp", "Connecting");
                 state=State.CONNECTED;
                 wifi.disconnect();
                 wifi.enableNetwork(i.networkId, true);
                 wifi.reconnect();
                 Log.i("starwisp", "Connected");
                 m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"Connected\"");
                 break;
             }
         }
 
         if (!found) {
             Log.i("starwisp", "adding wifi config");
             WifiConfiguration conf = new WifiConfiguration();
             conf.SSID = "\"" + SSID + "\"";
 
 //conf.wepKeys[0] = "\"" + networkPass + "\"";
 //conf.wepTxKeyIndex = 0;
 //conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 //conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
 
             conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
             wifi.addNetwork(conf);
         }
 
     }
 
     public void StartRequestThread(final String url, final String t, final String callbackname) {
         Runnable runnable = new Runnable() {
 	        public void run() {
                 Request(url, t, callbackname);
 	        }
         };
         Thread mythread = new Thread(runnable);
         mythread.start();
     }
 
     private class ReqMsg {
         ReqMsg(InputStream is, String t, String c) {
             m_Stream=is;
             m_Type=t;
             m_CallbackName=c;
         }
         public InputStream m_Stream;
         public String m_Type;
         public String m_CallbackName;
     }
 
     private void Request(String u, String type, String CallbackName) {
         try {
             Log.i("starwisp","pinging: "+u);
             URL url = new URL(u);
             HttpURLConnection con = (HttpURLConnection) url
                 .openConnection();
 
             con.setUseCaches(false);
            con.setReadTimeout(100000 /* milliseconds */);
            con.setConnectTimeout(150000 /* milliseconds */);
             con.setRequestMethod("GET");
             con.setDoInput(true);
             // Starts the query
             con.connect();
             m_RequestHandler.sendMessage(
                 Message.obtain(m_RequestHandler, 0,
                                new ReqMsg(con.getInputStream(),type,CallbackName)));
 
         } catch (Exception e) {
             Log.i("starwisp",e.toString());
             e.printStackTrace();
         }
     }
 
     private Handler m_RequestHandler = new Handler(){
         @Override
         public void handleMessage(Message msg){
             ReadStream((ReqMsg)msg.obj);
         }
     };
 
 
     private void ReadStream(ReqMsg m) {
         InputStream in = m.m_Stream;
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new InputStreamReader(in));
             String line = "";
             String all = "";
             while ((line = reader.readLine()) != null) {
                 all+=line+"\n";
             }
             Log.i("starwisp","got data for "+m.m_CallbackName+"["+all+"]");
 
             if (m.m_Type.equals("download")) {
                 m_Builder.SaveData(m.m_CallbackName, all.getBytes());
             } else {
                 // results in evaluating data read from via http - fix if used from net
                 m_Builder.DialogCallback(m_Context,m_Context.m_Name,m.m_CallbackName,all);
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
 
 
 
     private class WiFiScanReceiver extends BroadcastReceiver {
         private static final String TAG = "WiFiScanReceiver";
 
         public WiFiScanReceiver(String ssid, NetworkManager netm) {
             super();
             SSID=ssid;
             nm = netm;
         }
 
         public String SSID;
         public NetworkManager nm;
 
         @Override
         public void onReceive(Context c, Intent intent) {
             List<ScanResult> results = nm.wifi.getScanResults();
             ScanResult bestSignal = null;
 
             if (nm.state==State.SCANNING) {
                 Log.i("starwisp", "Scanning "+nm.state);
 
 
                 for (ScanResult result : results) {
                     if (result.SSID.equals(SSID)) {
                         m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"In range\"");
                         nm.Connect();
                         return;
                     }
                 }
 
                 if (nm.state==State.SCANNING) {
                     Log.i("starwisp", "REScanning "+nm.state);
                     nm.wifi.startScan();
                 }
             }
         }
     }
 }
