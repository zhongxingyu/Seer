 /*
  * Copyright (C) 2011 SenseForce.com
  *
  * The project is shared form jcccn. The source could be transfered and modified freely.
  * 
  * Welcome to {@link http://www.senseforce.com}.
  */
 
 package com.senseforce.testSharedPrefs;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 /**
  * @author : jcccn
  * Creation time : 2011-12-25
  * Description : This demo shows a trap when you wanna delete data from SharedPreferences.
  */
 public class HomeActivity extends Activity implements OnClickListener {
 	
 	private static final String PREF_NAME = "home_pref";
 	private static final String PREF_KEY_ME = "my_name";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         this.findViewById(R.id.button1).setOnClickListener(this);
         this.findViewById(R.id.button2).setOnClickListener(this);
         this.findViewById(R.id.button3).setOnClickListener(this);
         this.findViewById(R.id.button4).setOnClickListener(this);
         this.findViewById(R.id.button5).setOnClickListener(this);
         this.findViewById(R.id.button6).setOnClickListener(this);
         this.findViewById(R.id.button7).setOnClickListener(this);
     }
 
 
 	@Override
 	public void onClick(View v) {
 		SharedPreferences sharedPref = null;
 		switch (v.getId()) {
 		//refresh the pref display
 		case R.id.button1:
 			refreshDisplay();
 			break;
 		//put a value
 		case R.id.button2:
 			sharedPref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
 			sharedPref.edit().putString(PREF_KEY_ME, "Morakot").commit();
 			refreshDisplay();
 			break;
 		//clear
 		case R.id.button3:
 			clearPrefs();
 			refreshDisplay();
 			break;
 		//delete file
 		case R.id.button4:
 			delPrefFiles();
 			refreshDisplay();
 			break;
 		//clear then delete file
 		case R.id.button5:
 			clearPrefs();
 			delPrefFiles();
 			refreshDisplay();
 			break;
 		//just finish the activity
 		case R.id.button6:
 			this.finish();
 			break;
 		//quit the process
 		case R.id.button7:
 			System.exit(0); 
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	private void refreshDisplay() {
 		SharedPreferences sharedPref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
 		String sharedPref_ME = sharedPref.getString(PREF_KEY_ME, "null");
 		String displayString = "The value for key " + PREF_KEY_ME + " is " + sharedPref_ME;
 		
 		File file = new File("/data/data/com.senseforce.testSharedPrefs/shared_prefs");
 		displayString = displayString + "\nExisting files:\n";
		if (file.listFiles() != null) {
			for (File item : file.listFiles()) {
				displayString = displayString + item.getAbsolutePath() + "\n";
			}
 		}
 		
 		((TextView)this.findViewById(R.id.displayTextView)).setText(displayString);
 	}
 	
 	private void clearPrefs() {
 		SharedPreferences sharedPref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
 		sharedPref.edit().clear().commit();
 	}
     
 	private void delPrefFiles() {
 		File file = new File("/data/data/com.senseforce.testSharedPrefs/shared_prefs");
 		for (File item : file.listFiles()) {
 			item.delete();
 		}
 	}
     
 }
