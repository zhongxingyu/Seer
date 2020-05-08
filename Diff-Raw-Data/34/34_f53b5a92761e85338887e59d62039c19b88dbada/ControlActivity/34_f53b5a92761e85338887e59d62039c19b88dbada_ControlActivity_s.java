 package com.example.tabletapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 
 public class ControlActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);	
 		setContentView(R.layout.controlview);	
 		initButtons();		
 	}
 	
 	private void initButtons() {		
 		ImageButton buttonback = (ImageButton)findViewById(R.id.back);
 		Button buttonsubmit = (Button)findViewById(R.id.submitcontrol);
 		
 		if (buttonback == null || buttonsubmit == null)
 			return;
 		
 		buttonback.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(ControlActivity.this, MenuActivity.class);
 				startActivity(i);
 			}
 		});
 		buttonsubmit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				postForm();
 			}
 		});
 	}
 	
 	private void postForm() {
 
 		// Make new information instance
 		Information info = new Information();
 		
 		EditText racerName = (EditText)findViewById(R.id.racerName);
 		info.setRacerName(racerName.getText().toString());
 		
 		EditText distance = (EditText)findViewById(R.id.distance);
 		info.setDistance(Integer.parseInt(distance.getText().toString()));		
 		
 		EditText opponent = (EditText)findViewById(R.id.opponent);
 		info.setOpponentName(opponent.getText().toString());	
 				
 		EditText description = (EditText)findViewById(R.id.description);
 		info.setRaceDescription(description.getText().toString());	
 		
 		EditText goldGoal = (EditText)findViewById(R.id.goldGoal);
 		info.setGoldenGoal(Integer.parseInt(goldGoal.getText().toString()));
 		
 		EditText silverGoal = (EditText)findViewById(R.id.silverGoal);
 		info.setSilverGoal(Integer.parseInt(silverGoal.getText().toString()));
 		
 		EditText bronzeGoal = (EditText)findViewById(R.id.bronzeGoal);
 		info.setBronzeGoal(Integer.parseInt(bronzeGoal.getText().toString()));		
 				
 		// Communicate with database
 		Match m = Match.getInstance();
 		m.setInfo(info);
 		new Thread(m).start();
 		
 	}
 }
