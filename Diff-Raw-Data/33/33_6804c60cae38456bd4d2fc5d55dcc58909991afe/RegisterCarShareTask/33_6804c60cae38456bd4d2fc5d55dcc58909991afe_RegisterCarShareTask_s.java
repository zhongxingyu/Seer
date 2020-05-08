 package gogo.gadgeto.car.tasks;
 
 import gogo.gadgeto.car.CreateCarGroupActivity;
import gogo.gadgeto.car.FuelFragment;
 import gogo.gadgeto.car.helper.DatabaseHandler;
 import gogo.gadgeto.car.helper.UserFunctions;
 
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 
 public class RegisterCarShareTask extends AsyncTask<Void, Void, Void> {
 
     // JSON Response node names
     private static String KEY_SUCCESS = "success";
     private static String KEY_ERROR_MSG = "error_msg";
     private static String KEY_EMAIL = "email";
     private static String KEY_ID = "id";
     
     // Properties
     private CreateCarGroupActivity activity;
     private UserFunctions userFunction;
     private JSONObject json;
     private String mileage; 
     private String password;
     private String error_msg;
 	
 	public RegisterCarShareTask(CreateCarGroupActivity activity, String password, String mileage)
 	{
 		this.activity = activity;
 		this.mileage = mileage;
 		this.password = password;
 		this.error_msg = "";
 	}
 	
     @Override
 	protected void onPostExecute(Void result) {
     	activity.showError(error_msg);
 	}
 
     @Override
     protected Void doInBackground(Void... params) {        
         userFunction = new UserFunctions();
         json = userFunction.registerCarshare(mileage, password);
 		
 		try {
             if (json.getString(KEY_SUCCESS) != null) {
                 String res = json.getString(KEY_SUCCESS);
                 if(Integer.parseInt(res) == 1){
 	                // registration successfully
 	                // Store carShare details in SQLite Database
 	                DatabaseHandler db = new DatabaseHandler(activity.getApplicationContext());                							
 	                JSONObject json_user = json.getJSONObject("carShare");
 	
 	                // Clear all previous data in database
 	                userFunction.logoutUser(activity.getApplicationContext());
 	                db.addCarSharing(new UserFunctions().getEmailFromLoggedInUser(activity.getApplicationContext()), json_user.getString(KEY_ID));
 	                error_msg = "CarSharing:" + json_user.getString(KEY_ID);
                 }
             }
 			else {
 				error_msg = json.getString(KEY_ERROR_MSG);
 			}
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         return null;
     }
 }
