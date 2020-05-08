 package com.example.payback;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends TitleActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		setTitle("PayBack");
 		Toast.makeText(getApplicationContext(),"opened", Toast.LENGTH_SHORT).show();
 		checkCache();
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 	
 	public void Login(View view) throws InterruptedException {
 		//for log in, url stub is AccountLogin.php
 		Logger CONLOG = Logger.getLogger(LoginActivity.class .getName());
 		CONLOG.setLevel(Level.INFO);
 		CONLOG.info("Login page loaded");
 		final String EMAIL_PATTERN = 
 				"^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
 		final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 		final Matcher matcher;
 
 		String email = ((EditText)findViewById(R.id.email)).getText().toString();
 		String password = ((EditText)findViewById(R.id.password)).getText().toString();
 		CONLOG.info("Login info: <"+email+", "+password+">");
 		matcher = pattern.matcher(email);
 
 		if(email.isEmpty()){
 			Toast.makeText(getApplicationContext(), "Email field is empty", Toast.LENGTH_SHORT).show();
 		}else if(password.isEmpty()){
 			Toast.makeText(getApplicationContext(), "Password field is empty", Toast.LENGTH_SHORT).show();
 		}else if(!matcher.matches()){
 			Toast.makeText(getApplicationContext(), "Email: \""+email+"\" is not a valid email address!", Toast.LENGTH_SHORT).show();
 		}else{
 
 			String status  ="fail";
 			AccessNet caller = new AccessNet();
 
 			String params = "userEmail="+email+"&password="+password;
			String urlstub = " db_verify_login.php";
 
 			CONLOG.info("Attempting to call server at: "+urlstub+", "+params);
 			status = caller.simpleServerCall(urlstub, params);
 
 			if(status.equalsIgnoreCase("success")){
 				
 				if (((CheckBox)findViewById(R.id.rememberLogin)).isChecked())
 					rememberLogin();
 				
 				CONLOG.info("Server call successful and logged in!");
 				Intent intent = new Intent(this, MainActivity.class);
 				
 				/* Will's new additions */
 				user = new User(email,password); //user is declared in TitleActivity, which every activity extends
 				
 				/* Will's old additions */
 				/*
 				SharedPreferences prefs = this.getSharedPreferences("com.example.payback", Context.MODE_PRIVATE);
 				User u = new User(email); //the rest is looked up from the server
 				String userKey = "com.example.payback.user";
 				String userVal = u.userToString();
 				prefs.edit().putString(userKey, userVal).commit();
 				*/
 				
 				Toast.makeText(getApplicationContext(),"Welcome", Toast.LENGTH_SHORT).show();
 
 				startActivity(intent);
 				this.finish();
 			}else{
 				CONLOG.info("Server call successful but user failed login.");
 				Toast.makeText(getApplicationContext(),"Incorrect username or password", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	public void checkCache(){
 		Toast.makeText(getApplicationContext(),"inCache", Toast.LENGTH_SHORT).show();
 		try {
 			FileInputStream fis = openFileInput("login_info");
 			//Toast.makeText(getApplicationContext(),fis.read(), Toast.LENGTH_SHORT).show();
 			String fileData = "";
 			String line = "";
 			BufferedReader reader = new BufferedReader(new InputStreamReader(fis,fileData));
 			StringBuilder builder = new StringBuilder();
 			while(( line = reader.readLine()) != null ) {
 		         builder.append( line );
 		         builder.append( '\n' );
 		      }
 			fileData = builder.toString();
 			
 			Toast.makeText(getApplicationContext(),fileData, Toast.LENGTH_LONG).show();
 			
 		} catch (FileNotFoundException e) {
 			Toast.makeText(getApplicationContext(),"FileError", Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(),"IOError", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public void rememberLogin(){
 		String filename = "login_info";
 		String storeData = "";
 	 
 		String email = ((EditText)findViewById(R.id.email)).getText().toString();
 		String password = ((EditText)findViewById(R.id.password)).getText().toString();
 		storeData = "email " + email.toString() + " password " + password.toString();
 		Toast.makeText(getApplicationContext(),storeData, Toast.LENGTH_LONG).show();
 
 		try {
 			FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
 			fos.write(storeData.getBytes());
 			String msg = storeData + " written!";
 			Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG).show();
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void CreateAccount(View view) {
 		Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
         startActivity(intent);
         this.finish();
 	}
 	
 	public void bypass(View view) {
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
         startActivity(intent);
         this.finish();
 	}
 }
