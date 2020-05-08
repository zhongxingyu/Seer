 package tvhackfest.likeplause;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.util.Log;
 
 import com.vobileinc.tvsyncexample.TVSYNCExampleActivity;
 
 /**
  * This class echoes a string called from JavaScript.
  */
 public class Echo extends CordovaPlugin {
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
     	Log.i("***Echo***", "execute: " + action);
         if (action.equals("echo")) {
             String message = args.getString(0); 
            echo(message, callbackContext);
             return true;
         }
         return false;
     }
 
     private void echo(String message, CallbackContext callbackContext) {
     	Log.i("***Echo***", "echo");
     	
         if (message != null && message.length() > 0) { 
             callbackContext.success(TVSYNCExampleActivity.LAST_RESULT);
         } else {
             callbackContext.error("Expected one non-empty string argument.");
         }
     }
 }
