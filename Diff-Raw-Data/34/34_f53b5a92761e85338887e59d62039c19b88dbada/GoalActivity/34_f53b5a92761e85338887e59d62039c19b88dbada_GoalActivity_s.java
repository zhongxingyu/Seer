 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.example.tabletapp;
 
 import android.app.Activity;
 import android.content.Intent;
import android.graphics.drawable.Drawable;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
import android.widget.Toast;
 import java.util.Random;
 
 /**
  *
  * @author school
  */
 public class GoalActivity extends Activity {
 	
 	private static int goal = 1;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.goalview);
 		this.backbuttonenable();
 		this.switchGoals();
 		this.showRaceGoal();
 	}
 		
 	public void showRaceGoal() {
 		Match match = Match.getInstance();
 		
 		//int currentTime = match.getCurrentTime();
 		//Log.i("GoalActivity", Integer.toString(currentTime));
 		//int goalTime = match.getGoalTime(this.goal);
 		Random random = new Random();
 		int currentTime = random.nextInt(999999);
 		int goalTime = random.nextInt(999999);
 		int differenceTime = goalTime - currentTime;
 		
 		TextView currentTimeView = (TextView) findViewById(R.id.textView1);
 		currentTimeView.setText(match.createMatchTime(currentTime));
 		TextView differenceView = (TextView) findViewById(R.id.TextView03);
 		differenceView.setText(match.createMatchTime(differenceTime));
 		TextView goalTimeView = (TextView) findViewById(R.id.TextView05); 
 		goalTimeView.setText(match.createMatchTime(goalTime));
 		
 		
 	}
 	
 	private void switchGoals()
 	{
 		if(findViewById(R.id.imageView1)!=null){
 			ImageButton switchGoal = (ImageButton) findViewById(R.id.imageView1);
 			switchGoal.setOnClickListener(new Button.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					goal++;
 					if(goal > 3) {
 						goal = 1;
 					}
 					ImageButton goalButton = (ImageButton) findViewById(R.id.imageView1);
 					if(goal == 1) {
 						goalButton.setImageResource(R.drawable.gold);
 					}
 					else if(goal == 2) {
 						goalButton.setImageResource(R.drawable.silver);
 					}
 					else {
 						goalButton.setImageResource(R.drawable.bronze);
 					}
 					showRaceGoal();
 				}
 			});
 		}
 	}
 	
 	public void backbuttonenable(){
 		ImageButton buttonback = (ImageButton)findViewById(R.id.back);
 		
 		if (buttonback == null)
 			return;
 		
 		buttonback.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(GoalActivity.this, MenuActivity.class);
 				startActivity(i);
 			}
 		});
 	}
 }
