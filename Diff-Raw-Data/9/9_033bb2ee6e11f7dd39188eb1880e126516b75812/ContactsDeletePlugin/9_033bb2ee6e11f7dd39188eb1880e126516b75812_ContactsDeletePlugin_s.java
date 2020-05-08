 /**
  * 
  */
 package edu.northwestern.contactsplugin;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import android.widget.Toast;
 
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 import com.phonegap.api.PluginResult.Status;
 //-+import com.phonegap.api.PluginResult.Status;
 /**
  * @author khalid
  *
  */
 public class ContactsDeletePlugin extends Plugin {
 	
 	public static String ACTION = "deleteNumber";
 
 	/* (non-Javadoc)
 	 * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
 	 */
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackId) {
 		PluginResult result = null;
 		if (ACTION.equals(action)) {
 			try {
 				JSONObject resultJson= new JSONObject();
 				//long numbersToDel = data.getLong(0);
 				
 				String callsDeleted = deleteCalls(data);
 				//Log.d("Contacts Del Plugin", "Got data from JSON, called del function ...");
 				
 				if (callsDeleted != null) {
 					resultJson.put("callsDel",callsDeleted);
 					result = new PluginResult(Status.OK, resultJson);
 				}
 				else
 					result = new PluginResult(Status.ERROR);
 				
 				
 			} catch (JSONException jsonEx) {
 				Log.d("Contacts Del Plugin", "Got JSON Exception "+ jsonEx.getMessage());
 				result = new PluginResult(Status.JSON_EXCEPTION);
 			} catch (Exception e) {
 				Log.d("Contacts Del Plugin", "Got Exception "+ e.getMessage() + e.toString());
 			}
 		}
 		else {
 			result = new PluginResult(Status.INVALID_ACTION);
 		}
 		return result;
 	}
 	/*
 	public String deleteCalls(String numberToDel) {
 
 		   try{
 			   numberToDel = numberToDel.replaceAll( "[^\\d]", "" );
 
 			   Integer numDel = ctx.getContentResolver().delete(android.provider.CallLog.Calls.CONTENT_URI, 
 		    		   	"number='"+numberToDel+"'", null);
 			   return numDel.toString();
 		   }catch(Exception ex){
 			   return null;
 			   //Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(),Toast.LENGTH_SHORT).show(); 
 		   }
 	}
 	*/
 	public String deleteCalls(JSONArray numbers) {
 
 		   try{
 			   Integer numDel = 0;
 			   for (int i = 0; i < numbers.length(); i++) {
 				   numDel += ctx.getContentResolver().delete(android.provider.CallLog.Calls.CONTENT_URI, 
 			    		   	"number='"+numbers.getLong(i)+"'", null);
 				   numDel += ctx.getContentResolver().delete(android.provider.CallLog.Calls.CONTENT_URI, 
			    		   	"number='+"+numbers.getLong(i)+"'", null);
 			   }
 			   return numDel.toString();
 		   }catch(Exception ex){
 			   return null;
 			   //Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(),Toast.LENGTH_SHORT).show(); 
 		   }
 	}
 
 }
