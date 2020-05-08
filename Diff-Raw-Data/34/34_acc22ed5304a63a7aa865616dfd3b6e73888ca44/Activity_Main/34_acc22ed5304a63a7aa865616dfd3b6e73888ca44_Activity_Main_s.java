 package com.placella.socialconnections;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
import android.webkit.*;
 import android.widget.Button;
 
 public class Activity_Main extends Activity_Web {
 	private Activity self = this;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_web);
 
 		Intent intent = getIntent();
		String token = intent.getStringExtra("token");
 		final int accesslevel = intent.getIntExtra("accesslevel", ACCESSLEVEL.ANONYMOUS);
 		
 		init("main", token, "");
 		
 		Button logOutBtn = (Button) findViewById(R.id.logOutBtn);
 		logOutBtn.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
				// Destroys user's session
			    CookieSyncManager.createInstance(self);
			    CookieManager.getInstance().removeAllCookie();
			    // Go back to MainActivity
 			    setResult(0);
 			    finish();
 			}
 		});
 		Button menuBtn = (Button) findViewById(R.id.menuBtn);
 		menuBtn.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 		        Bundle b = new Bundle();
 		    	String token = RemoteAuth.getToken();
 		        b.putString("token", token);
 		        b.putInt("accesslevel", accesslevel);
 				if (accesslevel == ACCESSLEVEL.LECTURER) {
 			        Intent intent = new Intent(self, Activity_LecturerMenu.class);
 			        intent.putExtras(b);
 		    		startActivityForResult(intent, 99);
 				} else if (accesslevel == ACCESSLEVEL.STUDENT) {
 			        Intent intent = new Intent(self, Activity_StudentMenu.class);
 			        intent.putExtras(b);
 		    		startActivityForResult(intent, 99);
 				} else {
 					finish();
 				}
 			}
 		});
 
 		// By default, we don't want to go back
 		// to MainActivity when the activity dies
 		setResult(1);
 	}
 	
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	if (resultCode == 0) {
     		finish();
     	}
     }
 
 }
