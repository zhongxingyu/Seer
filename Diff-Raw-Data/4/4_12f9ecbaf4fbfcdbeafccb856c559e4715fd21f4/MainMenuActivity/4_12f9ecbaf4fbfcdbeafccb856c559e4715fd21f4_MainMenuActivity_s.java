 package com.naturequest;
 
 
 import com.naturequest.R;
 import com.naturequest.camera.CameraActivity;
 import com.naturequest.radar.RadarActivity;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 public class MainMenuActivity extends TabActivity
 {
 	private Button signOutButton;
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_menu);
 
 		this.signOutButton = (Button)findViewById(R.id.signOutButton);
 		
 		this.signOutButton.setOnClickListener(new OnClickListener()
 		{	
 			@Override
 			public void onClick(View arg0)
 			{
 				Intent intent = new Intent (MainMenuActivity.this,LoginActivity.class);
 				startActivity(intent);
				Game.setGame(null);
 				finish();
 			}
 		});
 		
 		TabHost tabHost = getTabHost();
 
 		TabSpec locationsTab = tabHost.newTabSpec("Locations");
 		locationsTab.setIndicator(null, getResources().getDrawable(R.drawable.locations_button));
 		Intent locationsIntent = new Intent(this, RadarActivity.class);
 		locationsTab.setContent(locationsIntent);
 
 		TabSpec cameraTab = tabHost.newTabSpec("Camera");
 		cameraTab.setIndicator(null, getResources().getDrawable(R.drawable.camera_button));
 		Intent cameraIntent = new Intent(this, CameraActivity.class);
 		cameraTab.setContent(cameraIntent);
 
 		TabSpec leaderboardTab = tabHost.newTabSpec("Leaderboard");
 		leaderboardTab.setIndicator(null, getResources().getDrawable(R.drawable.leaderboard_button));
 		Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
 		leaderboardTab.setContent(leaderboardIntent);
 
 		TabSpec profileTab = tabHost.newTabSpec("Profile");
 		profileTab.setIndicator(null, getResources().getDrawable(R.drawable.profile_button));
 		Intent profileIntent = new Intent(this, ProfileActivity.class);
 		profileTab.setContent(profileIntent);
 		
 		TabSpec helpTab = tabHost.newTabSpec("Help");
 		helpTab.setIndicator(null, getResources().getDrawable(R.drawable.help_button));
 		Intent helpIntent = new Intent(this, HelpActivity.class);
 		helpTab.setContent(helpIntent);
 		
 		tabHost.addTab(locationsTab);
 		tabHost.addTab(cameraTab);
 		tabHost.addTab(leaderboardTab);
 		tabHost.addTab(profileTab);
 		tabHost.addTab(helpTab);
 		
 		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)
 		{
 			tabHost.getTabWidget().getChildAt(i).setPadding(0,0,0,0);
 			tabHost.getTabWidget().getChildAt(i).getBackground()
 			.setColorFilter(new LightingColorFilter(0xACD473, 0x82a354));
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		if(Game.getGame()==null){
 			Intent login = new Intent(this, LoginActivity.class);
 			startActivity(login);
 		}
 	}
 }
