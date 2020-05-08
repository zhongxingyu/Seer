 /* Copyright information */
 
 package com.allplayers.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /* Class information */
 public class Login extends Activity
 {
 	//private EditText username;
 	//private EditText password;
 
 	/** called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		//create the panel to enclose everything
 		//View mainPanel = createInputForm();
 		
 		//show the panel on the screen
 		//setContentView(mainPanel);
 		setContentView(R.layout.main);
 		
 		final Button button = (Button) findViewById(R.id.loginButton);
         button.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
             	EditText usernameEditText = (EditText)findViewById(R.id.usernameField);     
             	EditText passwordEditText = (EditText)findViewById(R.id.passwordField);
             	
             	String username = usernameEditText.getText().toString();
             	String password = passwordEditText.getText().toString();;
             	
                 String result = APCI_RestServices.validateLogin(username, password);
                 
                 //String name = "";
                 
                 //TextView tv = new TextView(Login.this);
				//tv.setText("" + result);
 				//setContentView(tv);
                 
 				try
 				{
 					
 					JSONObject jsonResult = new JSONObject(result);
 					//name += 
 					APCI_RestServices.user_id = jsonResult.getJSONObject("user").getString("uuid");
 					
 					String sessionName = jsonResult.getString("session_name");
					String sessionID = jsonResult.getString("sessid");
 					
					APCI_RestServices.session_name = sessionName;
					APCI_RestServices.session_id = sessionID;
					
					//CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(Login.this);
					//cookieSyncManager.startSync();
					//CookieManager cookieManager = CookieManager.getInstance();
					//cookieManager.setAcceptCookie(true);
					//cookieManager.setCookie("www.allplayers.com", sessionName);
					//cookieManager.setCookie("www.allplayers.com", sessionID);
					//cookieManager.setCookie(sessionName, sessionID);
 					
 					Intent intent = new Intent(Login.this, MainScreen.class);
 					startActivity(intent);
 				}
 				catch(JSONException ex)
 				{
 					System.out.println(ex);
 					//name += "Login Error: " + ex.toString();
 					
 					TextView tv = new TextView(Login.this);
 					tv.setText("Invalid Login");
 					setContentView(tv);
 				}
 				
             }
         });
 	}
 	
 	//No longer needed with xml login screen
 	/** create the login form */
 /*	private ViewGroup createInputForm()
 	{
 		LinearLayout panel = new LinearLayout(this);
 		panel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		panel.setOrientation(LinearLayout.VERTICAL);
 
 		//username label and text field
 		TextView usernameLbl = new TextView(this);
 		usernameLbl.setText("Username:");
 		usernameLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
 		usernameLbl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		username = new EditText(this);
 		username.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 
 		//password label and text field
 		TextView passwordLbl = new TextView(this);
 		passwordLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
 		passwordLbl.setText("Password:");
 		passwordLbl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		password = new EditText(this);
 		password.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		password.setTransformationMethod(new PasswordTransformationMethod());
 
 		//login button
 		final Button loginButton = new Button(this);
 		loginButton.setText("Login");
 		loginButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		loginButton.setGravity(Gravity.CENTER);
 		loginButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				TextView tv = new TextView(Login.this);
 				
 				String name = "";
 				//String result = checkLogin(username.getText().toString(), password.getText().toString());
 				//JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(result);
 				
 				tv.setText("Hello, " + name);
 				setContentView(tv);
 			}
 		});
 
 		//adding the views to the panel
 		//username
 		panel.addView(usernameLbl);
 		panel.addView(username);
 		//password
 		panel.addView(passwordLbl);
 		panel.addView(password);
 		//login button
 		panel.addView(loginButton);
 
 		return panel;
 	}
 */
 }
