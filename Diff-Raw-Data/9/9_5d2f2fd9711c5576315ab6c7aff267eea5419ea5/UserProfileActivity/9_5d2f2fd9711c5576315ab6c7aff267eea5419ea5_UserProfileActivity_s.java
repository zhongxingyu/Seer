 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class UserProfileActivity extends Activity implements OnClickListener 
 {
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.userprofile);
 
 		View user_profile_button = findViewById(R.id.edit_profile_button);
 		user_profile_button.setOnClickListener(this);
 		
 		View open_timer_button = findViewById(R.id.open_timer_button);
 		open_timer_button.setOnClickListener(this);
 	}
 
 	public void onClick(View v) 
 	{
 		// TODO Auto-generated method stub
 		switch (v.getId()) 
 		{
 		case R.id.edit_profile_button:
 			Intent u = new Intent(this, EditUserProfileActivity.class);
 			startActivity(u);
 			break;
 		case R.id.open_timer_button:
 			Intent t = new Intent(this, TimerTabWidget.class);
 			startActivity(t);
 			break;
 		}
 	}
 }
