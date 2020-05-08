 package com.mimo.app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;

import com.bugsense.trace.BugSenseHandler;
  
  
 public class HomeActivity extends Activity implements OnClickListener{
 	
 	private Intent intent;
 	private LinearLayout mapLayout, calendarLayout, bizLayout, newLayout;
 	private LinearLayout eventLayout;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         BugSenseHandler.setup(this, "56d1af98");
         
         setContentView(R.layout.layout_dashboard);
           
		mapLayout = (LinearLayout) findViewById(R.id.map_layout);
         eventLayout = (LinearLayout) findViewById(R.id.event_layout);
         calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
         bizLayout = (LinearLayout) findViewById(R.id.biz_layout);
         newLayout = (LinearLayout) findViewById(R.id.new_layout);
         
         mapLayout.setOnClickListener(this);
         eventLayout.setOnClickListener(this);
         calendarLayout.setOnClickListener(this);
         bizLayout.setOnClickListener(this);
         newLayout.setOnClickListener(this);
     } 
     
     @Override
     public void onClick(View v) {	
     	
     	BugSenseHandler.log("debugOfBugSense:", new Exception());
     	
     	
     	if(v == mapLayout){
     		intent = new Intent(this, MapDashboardActivity.class);
     	} else if(v == calendarLayout){
     		intent = new Intent(this, ViewPagerActivity.class);
     	} else if(v == eventLayout){
     		intent = new Intent(this, EventListActivity.class);
     	} else if(v == bizLayout){
     		intent = new Intent(this, BusinessLIstActivity.class);
     	} else if(v == newLayout){
     		intent = new Intent(this, InputDetailActivity.class);
     	}
     	
     	startActivity(intent); 
     }
 }
