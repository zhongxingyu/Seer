 package com.example.tabletapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	 
 	private Handler mHandler;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.mainview);
 	    backbuttonenable();
 	    mHandler = new Handler();
         mHandler.post(mUpdate);
 
 						
 	
 	}	
 	private Runnable mUpdate = new Runnable() {
 		   public void run() {
 
 			   showCurrentTime();
 			   showBestTime();
 			   showLane();
 			   showCurrentDistance();
 			   showCurrentRoundNumber();
 		       mHandler.postDelayed(this, 1000);
 
 		    }
 	};
 
 	private void showCurrentTime() {
 		Match match = Match.getInstance();
 		TextView currentTime = (TextView) findViewById(R.id.textView1); 
		currentTime.setText(match.createMatchTime(match.getCurrentTime()));
 	}
 	
 	private void showBestTime() {
 		Match match = Match.getInstance();
 		TextView bestTime = (TextView) findViewById(R.id.TextView03); 
		bestTime.setText(match.createMatchTime(match.getBestLapTime()));
 	}
 	
 	private void showCurrentDistance() {
 		Match match = Match.getInstance();
 		TextView currentDistance = (TextView) findViewById(R.id.TextView08);
 		currentDistance.setText(Integer.toString(match.getCurrentDistance()));
 	}
 	
 	private void showCurrentRoundNumber() {
 		Match match = Match.getInstance();
 		TextView currentRound = (TextView) findViewById(R.id.roundNumber);
 		currentRound.setText(Integer.toString(match.getRoundNumber()) + "/" + Integer.toString(match.getTotalRounds()));
 	}
 	
 	private void showLane() {
 		Match match = Match.getInstance();
 		TextView lane = (TextView) findViewById(R.id.TextView05);
 		String currentLane;
 		if(match.getInfo().getLane())
 			currentLane = "Inner";
 		else
 			currentLane = "Outer";
 		
 		lane.setText(currentLane);
 	}
 	
 	
 	public void backbuttonenable(){
 		if(findViewById(R.id.back)!=null){
 			ImageButton buttonback = (ImageButton) findViewById(R.id.back);
 			buttonback.setOnClickListener(new Button.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					Intent i = new Intent(MainActivity.this, MenuActivity.class);
 					startActivity(i);
 				}
 			});
 		}
 	}
 }
