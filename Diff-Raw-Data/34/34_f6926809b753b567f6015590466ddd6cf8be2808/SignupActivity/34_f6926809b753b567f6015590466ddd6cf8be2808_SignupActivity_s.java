 package uq.deco7381.runspyrun.Activity;
 
import com.example.runspyrunv3.R;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class SignupActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signup);
 		
 		Parse.initialize(this, "2XLuNz2w0M4iTL5VwXY2w6ICc7aYPZfnr7xyB4EF", "6ZHEiV500losBP4oHmX4f1qVuct1VyRgOlByTVQB");
 		ParseAnalytics.trackAppOpened(getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.signup, menu);
 		return true;
 	}
 	
 	public void signup(View view){
 		
 		final Intent intent = new Intent(this, LoginActivity.class);
 		EditText usernameET = (EditText)findViewById(R.id.sUsername);
 		String username = usernameET.getText().toString();
 		
 		EditText passwordET = (EditText)findViewById(R.id.sPassword);
 		String password = passwordET.getText().toString();
 		
 		EditText epasswordET = (EditText)findViewById(R.id.ePassword);
 		String ePassword = epasswordET.getText().toString();
 		
 		EditText emailET = (EditText)findViewById(R.id.email);
 		String email = emailET.getText().toString();
 		System.out.println(password);
 		System.out.println(ePassword);
 		
 		
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		if(!password.equals(ePassword)){
 			//Build an AlertDialog for the mistyping of password
 			builder.setTitle("Wrong typing");
 			builder.setMessage("Password comfirmation fail");
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.cancel();
 				}
 			});
 			//show the dialog box
 			AlertDialog alert = builder.create();
 			alert.show();
 		}else{
 			ParseUser user = new ParseUser();
 			user.setUsername(username);
 			user.setPassword(ePassword);
 			user.setEmail(email);
 			user.signUpInBackground(new SignUpCallback() {
 				@Override
 				public void done(ParseException e) {
 					// TODO Auto-generated method stub
 					if(e == null){
 						//if signup success, show in dialog box
 						builder.setTitle("SuccessActivity");
 						builder.setMessage("Please check your email to activate your account");
 						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								startActivity(intent);
 							}
 						});
 						//show the dialog box
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else {
 						//if there is a exception from parse, show in dialog box
 						builder.setTitle("Alert");
 						builder.setMessage(e.getMessage());
 						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								dialog.cancel();
 							}
 						});
 						//show the dialog box
 						AlertDialog alert = builder.create();
 						alert.show();
 					}
 				}
 			});
 			
 		}
 		
 		
 		
 	}
 	
 	public void cancel(View view){
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 	}
 
 }
