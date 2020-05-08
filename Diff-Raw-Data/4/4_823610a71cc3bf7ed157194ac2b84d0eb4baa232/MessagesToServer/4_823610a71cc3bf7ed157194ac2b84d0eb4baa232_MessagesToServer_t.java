 package il.ac.huji.chores;
 
 import com.parse.FunctionCallback;
 import com.parse.ParseCloud;
 import com.parse.ParseUser;
 
 import java.util.HashMap;
 
 public class MessagesToServer {
 	
 	
 
     /**
      * Send invitation to requested phone numbers
      * @param callback
      * @param name
      * @param phoneNumbers semicolon separated list of phone numbers
      */
     public static void invite(FunctionCallback callback, String name, String phoneNumbers) {
         HashMap<String, Object> params = new HashMap<String, Object>();
         params.put("name", name);
         params.put("phone", phoneNumbers);
        params.put("inviterName", ParseUser.getCurrentUser().getUsername());
        params.put("apartmentId", ParseUser.getCurrentUser().getString("apartmentID"));

         ParseCloud.callFunctionInBackground("invite", params, callback);
     }
 
 }
