 package com.codeb.cordova.plugins.settings;
  
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CallbackContext;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * This class echoes a string called from JavaScript.
  */
 public class Settings extends CordovaPlugin {
 
 	
 	
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        System.err.println("SETTINGS PLUGIN Execution");
 		try {
 			if (action.equals("getBluetooth")) {
 				String message = args.getString(0);
				System.err.println("SETTINGS PLUGIN Execution getBluetooth");
 				this.getBluetooth(message, callbackContext);
 				return true;
 			}
 			return false;
 		} catch(Exception e) {
			System.err.println("SETTINGS PLUGIN Exception: " + e.getMessage());
             callbackContext.error(e.getMessage());
             return false;
 		}
     }
 
     private void getBluetooth(String message, CallbackContext callbackContext) {
         
 		if (message != null && message.length() > 0) {
             callbackContext.success(message);
         } else {
             callbackContext.error("Expected one non-empty string argument.");
         }
     }
 }
 
