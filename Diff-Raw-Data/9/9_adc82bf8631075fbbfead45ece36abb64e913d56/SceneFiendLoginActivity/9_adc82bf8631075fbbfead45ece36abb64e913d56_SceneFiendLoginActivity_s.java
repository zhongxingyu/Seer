 package com.silke.sceneFiendAndroidApp;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.silke.sceneFiendAndroidApp.asynctasks.FileDownloader;
 import com.silke.sceneFiendAndroidApp.asynctasks.IJsonDownloaded;
 import com.silke.sceneFiendAndroidApp.handlers.DBHandler;
 import com.silke.sceneFiendAndroidApp.handlers.UserFunctions;
 
 public class SceneFiendLoginActivity extends SceneFiendAndroidAppActivity implements IJsonDownloaded
 {
 	Button btnLogin;
 	Button btnLinkToRegister;
 	static JSONObject jObj = null;
 	EditText inputUsername;
 	EditText inputPassword;
 	TextView login_error;
 	private SceneFiendLoginActivity context;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         context = this;
         
         Typeface tf = Typeface.createFromAsset(getAssets(),
                 "fonts/lucindablack.ttf");
         TextView tv = (TextView) findViewById(R.id.CustomFont);
         tv.setTypeface(tf);
        
         //actionbar
   		ActionBar ab = getActionBar();
   		ab.setDisplayHomeAsUpEnabled(true);
        
         // Importing all assets like buttons, text fields
      	inputUsername = (EditText) findViewById(R.id.loginUsername);
      	inputPassword = (EditText) findViewById(R.id.loginPassword);
      	btnLogin = (Button) findViewById(R.id.btnLogin);
      	btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
      	login_error = (TextView) findViewById(R.id.login_error);
 
      	// Login button Click Event
  		btnLogin.setOnClickListener(new View.OnClickListener() 
  		{
 
  			public void onClick(View view) 
  			{
  				String player_name = inputUsername.getText().toString();
  				String password = inputPassword.getText().toString();
  				Log.d("Button", player_name);
  				UserFunctions userFunction = new UserFunctions();
  				FileDownloader fd = userFunction.loginUser(context, player_name, password);
  				//Log.d("Testing JSON string for Login", fd.toString());
  				
  				GAME_PREFERENCES_PLAYER_NAME = player_name;
  				
  				String json_str = fd.getApiResponse();
  				
  				Log.d("Testing JSON string for Login", json_str);
  				Log.d("LOGGED IN USER PREFERENCE", GAME_PREFERENCES_PLAYER_NAME);
 
  				//parse string to json object
  				try 
  				{
 					jObj = new JSONObject(json_str);
 				} 
  				catch (JSONException e) 
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
  			
  				
  				
  			}
  		});
 
  		// Link to Register Screen
  		btnLinkToRegister.setOnClickListener(new View.OnClickListener() 
  		{
  			public void onClick(View view) 
  			{
  				Intent i = new Intent(getApplicationContext(),
  						RegisterActivity.class);
  				startActivity(i);
  				finish();
  			}
  		});
  	}
     
    
     
     //this function passes data to the sqlite db and if success tag registered pass user to menu activity
     private void successfulLog(JSONObject jObj)
     {
     	// check for login response
 			try {
 				if (jObj.getString("success") != null) 
 				{
 					login_error.setText("Hello Again!");
 					login_error.setTextColor(Color.parseColor("#FFCC66"));
 					
 					String res = jObj.getString("success"); 
 					if(Integer.parseInt(res) == 1)
 					{
 						GAME_PREFERENCES_LOGGED_IN = true;
 						// user successfully logged in
 						// Store user details in SQLite Database
 						DBHandler db = new DBHandler(getApplicationContext());
 						JSONObject json_user = jObj.getJSONObject("user");
 						
 						// Clear all previous data in database
 						UserFunctions userFunction = new UserFunctions();
 						userFunction.logoutUser(getApplicationContext());
 						//add new data to db
 						db.addUser(jObj.getString(DBHandler.KEY_PLAYER_ID), json_user.getString(DBHandler.KEY_PLAYER_NAME), json_user.getString(DBHandler.KEY_PLAYER_EMAIL));						
 						GAME_PREFERENCES_PLAYER_ID = jObj.getString(DBHandler.KEY_PLAYER_ID);
 						
 						Log.d("LOGIN ACTIVITY", "Logged in player_name: " + jObj.getString("user"));
 						//GAME_PREFERENCES_PLAYER_NAME = jObj.getString("player_name");
 						Log.d("LOGIN ACTIVITY", "Logged in player_id: " + GAME_PREFERENCES_PLAYER_ID);
 						//Log.d("LOGIN ACTIVITY", "Logged in player_name: " + GAME_PREFERENCES_PLAYER_NAME);
 						
 						// Launch Menu Screen
 						Intent menu = new Intent(getApplicationContext(), MenuActivity.class);
 						
 						// Close all views before launching Menu
 						menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 						startActivity(menu);
 							
 						// Close Login Screen
 						finish();
 					}
 					
 					if(jObj.getInt("success") == 0)
 					{
						// Error in login
						login_error.setText("Incorrect username/password");
 						login_error.setTextColor(Color.parseColor("#FF0000"));	
 					}
 				}
 			} 
 			catch (JSONException e) 
 			{
 				e.printStackTrace();
 			}
 			
     }
 
     //Json has been downloaded now - login with user entries (context) - passed to userfunctions (context)
     //passed to filedownloader (context) on finishing download calls this function and pass the jObj to it
     //interface was to datatype which kind of object is being passed to filedownloader
 	public void onJsonDownloaded(JSONObject jObj) 
 	{
 		successfulLog(jObj);
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				Intent i = new Intent(getApplicationContext(),
  						LoginActivity.class);
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  				startActivity(i);
  				Log.d("LoginAct", "activity started");
  				return true;
  			default:
  				return super.onOptionsItemSelected(item);
 		}
 	}
  }
