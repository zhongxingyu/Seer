 package pl.ug.ash;
 
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.pm.ActivityInfo;
 import android.util.Log;
 
 public class AshPlugin extends CordovaPlugin {
 
   public static final String ACTION_ORIENTATION_HORIZONTAL = "orientationHorizontal";
   public static final String ACTION_ORIENTATION_VERTICAL = "orientationVertical";
   public static final String ACTION_NETWORK_OFF = "networkOff";
   public static final String ACTION_NETWORK_ON = "networkOn";
   
   @Override
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 
     if (ACTION_ORIENTATION_HORIZONTAL.equals(action)) {
       try {
         Log.d("HelloPlugin", "Changing orientation to horizontal");
         changeOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
     if (ACTION_ORIENTATION_VERTICAL.equals(action)) {
       try {
         Log.d("HelloPlugin", "Changing orientation to vertical");
         changeOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
     if (ACTION_NETWORK_OFF.equals(action)) {
       try {
         Log.d("HelloPlugin", "Blocking access to network");
         
         disableNetwork();
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
     if (ACTION_NETWORK_ON.equals(action)) {
       try {
         Log.d("HelloPlugin", "Enabling network");
           
 //        enableNetwork();
           
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
 
     Log.d("AshPlugin error: No action " + action, "");
     callbackContext.error("Error");
     return false;
   }
 
   private void disableNetwork() {
   
 //    Settings.System.putInt(
 //      cordova.getActivity().getApplicationContext().getContentResolver(),
 //      Settings.System.AIRPLANE_MODE_ON, 0);
 //
 //    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 //    intent.putExtra("state", 0);
 //    cordova.getActivity().sendBroadcast(intent);
 
     //TODO: 
     throw new RuntimeException("Since Android 4.x it's not posible to send Intent.ACTION_AIRPLANE_MODE_CHANGED without root permissions");
   }
 
   private void changeOrientation(int orientation) {
     this.cordova.getActivity().setRequestedOrientation(orientation);
   }
 }
