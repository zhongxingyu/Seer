 
 package com.assurance;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 import com.library.DatabaseHandler;
 import com.library.UserFunctions;
 import com.rssreader.*;
 
 @TargetApi(Build.VERSION_CODES.GINGERBREAD)
 @SuppressLint("NewApi")
 public class MainActivity extends Activity {
 	Button btnLogin;
 	Button notemployee;
 	EditText inputEmail;
 	EditText inputPassword;
 	TextView loginErrorMsg;
 
 	// JSON Response node names
 	private static String KEY_SUCCESS = "success";
 	private static String KEY_UID = "uid";
 	private static String KEY_NAME = "name";
 	private static String KEY_EMAIL = "email";
 	private static String KEY_CREATED_AT = "created_at";
 
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
 	@SuppressLint("NewApi")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 			StrictMode.ThreadPolicy policy = 
 				new StrictMode.ThreadPolicy.Builder().permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 		}
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Importing all assets like buttons, text fields
 		inputEmail = (EditText) findViewById(R.id.loginEmail);
 		inputPassword = (EditText) findViewById(R.id.loginPassword);
 		btnLogin = (Button) findViewById(R.id.btnLogin);
 		notemployee = (Button) findViewById(R.id.notemployee);
 		loginErrorMsg = (TextView) findViewById(R.id.login_error);
 
 		// Login button Click Event
 		btnLogin.setOnClickListener(new View.OnClickListener() {
 
 			 
 			public void onClick(View view) {
 				String email = inputEmail.getText().toString();
 				String password = inputPassword.getText().toString();
 				UserFunctions userFunction = new UserFunctions();
 				Log.d("Button", "Login");
 				JSONObject json = userFunction.loginUser(email, password);
 
 				
 				
 				// check for login response
 				try {
 					if (json.getString(KEY_SUCCESS) != null) {
 						loginErrorMsg.setText("");
 						String res = json.getString(KEY_SUCCESS); 
 						if(Integer.parseInt(res) == 1){
 							// user successfully logged in
 							// Store user details in SQLite Database
 							DatabaseHandler db = new DatabaseHandler(getApplicationContext());
 							JSONObject json_user = json.getJSONObject("user");
 							
 							// Clear all previous data in database
 							userFunction.logoutUser(getApplicationContext());
 							db.addUser(json_user.getString(KEY_NAME), json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT));						
 							
 							// Launch Dashboard Screen
 							Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);
 							
 							// Close all views before launching Dashboard
 							dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 							startActivity(dashboard);
 							
 							// Close Login Screen
 							finish();
 						}else{
 							// Error in login
 							loginErrorMsg.setText("Incorrect username/password");
 						}
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		notemployee.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 Intent myintent2 = new Intent(MainActivity.this,RssReaderActivity.class);
                 startActivity(myintent2);
 
             }
         });
 		
 	}
 }
 
