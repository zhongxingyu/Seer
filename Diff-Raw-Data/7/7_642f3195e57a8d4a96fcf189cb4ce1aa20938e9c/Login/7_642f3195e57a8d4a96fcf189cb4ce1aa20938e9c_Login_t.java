 package com.androidians.betapro;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.content.SharedPreferences;
 
 public class Login extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		System.out.println("checkpoint1");
 		setContentView(R.layout.activity_login);
 		System.out.println("checkpoint2");
		
 		User testDeveloper = new User("Kayvan", "123456");
 		User testReviewer  = new User("Alice", "654321");
 		App testApp = new App("App1", "App1ID", "this is app1 description", "icon1", "apk1", 9.3, 1, 10, 20);
 		testApp.addScreenShots("Screen Shot 1");
 		testApp.addScreenShots("Screen Shot 2");
 		testApp.addDeveloperAsksFor("Performance");
 		testApp.addDeveloperAsksFor("UI");
 		
 		testDeveloper.addApp(testApp);
 		System.out.println("test user: " + testDeveloper.toString());	
 		
 		final Intent intent = new Intent(this, DeveloperMain.class);
 		
 		final EditText user = (EditText)findViewById(R.id.editText1);
 		final EditText pass = (EditText)findViewById(R.id.editText2);
 	//	final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		Button loginButton = (Button)findViewById(R.id.button1);
 		loginButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 		//		String userString = preferences.getString(user.toString(), "f");
 		  //      if(userString != "f") {
 		    //    	User activeUser = new User(user.toString(), pass.toString());
 		        	
 		      //  }
 		        startActivity(intent);
 			}
 		});
 		
		
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 
 }
