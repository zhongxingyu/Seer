 package com.uiproject.meetingplanner;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 
 import org.json.JSONException;
 
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseHelper;
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseManager;
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseUtility;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Signup extends Activity {
 	EditText fname_field, lname_field, phone_field, email_field, pw_field, pw2_field;
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	private MeetingPlannerDatabaseManager db;
 	private Button submit;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.signup);
         
         fname_field = (EditText) findViewById(R.id.fname);
         lname_field = (EditText) findViewById(R.id.lname);
         phone_field = (EditText) findViewById(R.id.phone);
         email_field = (EditText) findViewById(R.id.email);
         pw_field = (EditText) findViewById(R.id.pw);
         pw2_field = (EditText) findViewById(R.id.pw2);
         submit = (Button) findViewById(R.id.submit);
         
         //int colors[] = { 0xffffee66, 0xffffc941, 0xffffab23 };
 
         //GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
         //submit.setBackgroundDrawable(g);
         
         //get info from phone and input it into the fields here
         
         AccountManager accountManager = AccountManager.get(this); 
         Account[] accounts = accountManager.getAccountsByType("com.google");
         Account account;
         if (accounts.length > 0) {
           account = accounts[0];   
           String email = account.name;
           email_field.setText(email);
           
         }
         
         TelephonyManager tMgr =(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
         String phone = tMgr.getLine1Number();
         phone_field.setText(phone);
         
         // Hook up with database
 	    db = new MeetingPlannerDatabaseManager(this, MeetingPlannerDatabaseHelper.DATABASE_VERSION);
 	}
 
 	public void submit(View button) throws JSONException, ParseException, UnsupportedEncodingException{
 
         //error checking here
         
         String fname = fname_field.getText().toString();
         String lname = lname_field.getText().toString();
         String phone = phone_field.getText().toString();
        long phonenumber = Long.parseLong(phone);
         String email = email_field.getText().toString();
         String pw = pw_field.getText().toString();
         String pw2 = pw2_field.getText().toString();
         
         if (fname.length() == 0 || lname.length() == 0 || phone.length() == 0 || 
         		email.length() == 0 || pw.length() == 0 || pw2.length() == 0){
         	
 
             Toast.makeText(getBaseContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
             return;
         }
         
         if(!pw.equals(pw2)){
             Toast.makeText(getBaseContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
             return;
         	
         }
         
         // Send create user request to server
 		int uid = Communicator.createUser(phonenumber, fname, lname, email, pw);
 		
 		if(uid == -1){
 			// User has already been created, show error msg
 			Toast.makeText(getBaseContext(), "Phone number has already been used by other users!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		// User has been created successfully
 		
 		// Open db connection
 		db.open();
 		// Get meetings info & user infos from server and update internal db
     	MeetingPlannerDatabaseUtility.updateDatabase(db);
 		
 		// Store user into internal db
 		//db.createUser(uid, fname, lname, email, Long.toString(phonenumber), 0, 0);
 		db.close();
 		
 		
 		// Log user in
 		SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE);
 		Editor editor = settings.edit();
 		editor.putInt("uid", uid);
     	editor.putString("userPhoneNumber", Long.toString(phonenumber));
     	editor.putString("userFirstName", fname);
     	editor.putString("userLastName", lname);
     	editor.putString("userEmail", email);
 		editor.commit();
 		
 		// Notify user that the registration is successful
 		Toast.makeText(getBaseContext(), "You signed up successfully!", Toast.LENGTH_SHORT).show();
 		
     	Intent intent = new Intent(Signup.this, MainPage.class);
     	Signup.this.startActivity(intent);
 		
 	}
 
 }
