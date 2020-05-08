 package com.example.tabletapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class RaceActivity extends Activity {
 
 	private Match m;
 	private Information info;
 	
 	// Override onCreate
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);	
 		setContentView(R.layout.raceinfoview);
 		initButtons();
 		m = Match.getInstance();
 		info = m.getInfo();
 		getRaceInfo();
 	}
 	
 	// Button Init
 	private void initButtons() {		
 		ImageButton buttonback = (ImageButton)findViewById(R.id.back);
 		
 		if (buttonback == null)
 			return;
 		
 		buttonback.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(RaceActivity.this, MenuActivity.class);
 				startActivity(i);
 			}
 		});
 	}
 	
 	// Get race info from Information class, and display it
 	private void getRaceInfo() {		
 		TextView oppName = (TextView)findViewById(R.id.raceInfoOpp);
 		oppName.setText(info.getOpponentName());
 		
 		TextView raceDis = (TextView)findViewById(R.id.raceInfoDis);
 		raceDis.setText(Integer.toString(info.getDistance()));	
 		
 		TextView raceDes = (TextView)findViewById(R.id.raceInfoRace);
 		raceDes.setText(info.getRaceDescription());	
 	}
 }
