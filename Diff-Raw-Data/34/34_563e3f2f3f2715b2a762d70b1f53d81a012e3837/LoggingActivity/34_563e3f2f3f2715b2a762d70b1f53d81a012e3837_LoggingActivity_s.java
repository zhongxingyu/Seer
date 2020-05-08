 package com.kmcginn.airquotes;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 import com.parse.SignUpCallback;
 
 public class LoggingActivity extends Activity {
 	Context context= this;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_logging);
 		Parse.initialize(this, "AyI8XoEioHWsBFl7PmQWMLbiJ6woG6O7uJFfqAYK", 
 				"nzKyPzUYD5UUJIJSxZ8jUqCp9rWQxGXc7XnmdA85");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.logging, menu);
 		return true;
 	}
 
 	public void logInClick(View view) {
 		EditText addressText = (EditText)findViewById(R.id.emailText);
 		String address = addressText.getText().toString();	
 		EditText passText = (EditText)findViewById(R.id.passText);
 		String password = passText.getText().toString();
 		
 		ParseUser.logInInBackground(address, password, new LogInCallback() {
 			  public void done(ParseUser user, ParseException e) {
 			    if (user != null) {
 			      // Hooray! The user is logged in.
 			    	Intent intent= new Intent(context, MainActivity.class);
 			    	intent.putExtra("user", user.getEmail());
			    	startActivity(intent);
 			    } else {
 			      // Signup failed. Look at the ParseException to see what happened.
 					Toast.makeText(context, "Invalid Email Address or Password", Toast.LENGTH_LONG).show();
 
 			    }
 			  }
 			});
 		Log.e("loc","after log in");
 
 		
 	}
 	
 	public void signUpClick(View view) {
 		// get text from user's EditText box
 		EditText addressText = (EditText)findViewById(R.id.emailText);
 		String address = addressText.getText().toString();	
 		EditText passText = (EditText)findViewById(R.id.passText);
 		String password = passText.getText().toString();
 				
 		ParseUser user = new ParseUser();
 		user.setUsername(address);
 		user.setPassword(password);
 		user.setEmail(address);
 		user.put("filter", 0);
 		
 		final String email = address;
 		
 		user.signUpInBackground(new SignUpCallback() {
 			  public void done(ParseException e) {
 			    if (e == null) {
 			      // Hooray! Let them use the app now.
 			    	Intent intent= new Intent(context, MainActivity.class);
 			    	intent.putExtra("user", email);
 			    	startActivity(intent);
 			    } else {
 			      // Sign up didn't succeed. Look at the ParseException
 			      // to figure out what went wrong
 					Toast.makeText(context, "Invalid Email Address or Password", Toast.LENGTH_LONG).show();
 
 			    }
 			  }
 			});
 		
 	}
 }
