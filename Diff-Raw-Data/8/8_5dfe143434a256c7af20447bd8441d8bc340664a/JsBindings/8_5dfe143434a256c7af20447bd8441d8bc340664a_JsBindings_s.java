 package edu.mit.csail.netmap.client;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import edu.mit.csail.netmap.NetMapListener;
 import edu.mit.csail.netmap.MeasureCallback;
 import edu.mit.csail.netmap.NetMap;
 import edu.mit.csail.netmap.UploadCallback;
 import us.costan.chrome.ChromeJavascriptInterface;
 import us.costan.chrome.ChromeView;
 import us.costan.chrome.ChromeCookieManager;
 
 /** The bindings available as the "NetMap.Pil" object. */
 public class JsBindings implements NetMapListener {
   /** The WebView using these bindings. */
   private final ChromeView webView;
   /** The activity that contains the WebView using these bindings. */
   private final GameActivity activity;
   
   private final SharedPreferences preferences;
   
   public JsBindings(ChromeView webView_, GameActivity activity_) {
     webView = webView_;
     activity = activity_;
     preferences = activity.getSharedPreferences("webview_session",
         Context.MODE_PRIVATE);
   }
   
   @ChromeJavascriptInterface
   public void trackLocation(boolean enabled) {
    NetMap.trackLocation(true);
   }
   
   @ChromeJavascriptInterface
   public String locationJson() {
     return NetMap.locationJson();
   }
 
   @ChromeJavascriptInterface
   public String powerSourceJson() {
     return NetMap.powerSourceJson();
   }
   
   @ChromeJavascriptInterface
   public String networkSourceJson() {
     return NetMap.networkSourceJson();
   }
   
   @ChromeJavascriptInterface
   public void startReading(final String measurements,
                            final String callbackName) {
     NetMap.measureAsync(measurements, new MeasureCallback() {
       @Override
       public void done(final String digest) {
         activity.runOnUiThread(new Runnable() {
           public void run() {
             webView.loadUrl("javascript:_pil_cb." + callbackName +
                             "(\"" + digest + "\")");
           }
         });        
       }
     });
   }
   
   @ChromeJavascriptInterface
   public void uploadReadingPack(final String callbackName) {
     NetMap.uploadAsync(new UploadCallback() {
       @Override
       public void done(final boolean stillHasData) {
         final String doneString = stillHasData ? "repeat" : "done";
         activity.runOnUiThread(new Runnable() {
           public void run() {
             webView.loadUrl("javascript:_pil_cb." + callbackName +
                             "(\"" + doneString + "\")");
           }
         });
       }
     });
   }
   
   @ChromeJavascriptInterface
   public void setReadingsUploadBackend(String url, String uid) {
     NetMap.configure(uid, url);
   }
   
   @ChromeJavascriptInterface
   public void saveCookies(String origin) {
     ChromeCookieManager cookies = ChromeCookieManager.getInstance();
     String cookie = cookies.getCookie(origin);
     
     Editor editor = preferences.edit();
     editor.putString("origin", origin);
     editor.putString("cookies", cookie);
     editor.commit();    
   }
   
   /** Called by GameActivity to load saveCookie's cookies into the WebView. **/
   public void loadCookies() {
     ChromeCookieManager cookies = ChromeCookieManager.getInstance();
     String origin = preferences.getString("origin", "http://netmap.pwnb.us");
     String cookie = preferences.getString("cookies", ""); 
     cookies.setCookie(origin, cookie);
   }
   
   @Override
   public void onLocation() {
     activity.runOnUiThread(new Runnable() {
       public void run() {
         webView.loadUrl("javascript:_pil_ev.location()");
       }
     });
   }
 
   @Override
   public void onBattery() {
     activity.runOnUiThread(new Runnable() {
       public void run() {
         webView.loadUrl("javascript:_pil_ev.power()");
       }
     });
   }
 
   @Override
   public void onNetwork() {
     activity.runOnUiThread(new Runnable() {
       public void run() {
         webView.loadUrl("javascript:_pil_ev.network()");
       }
     });
   }
 }
