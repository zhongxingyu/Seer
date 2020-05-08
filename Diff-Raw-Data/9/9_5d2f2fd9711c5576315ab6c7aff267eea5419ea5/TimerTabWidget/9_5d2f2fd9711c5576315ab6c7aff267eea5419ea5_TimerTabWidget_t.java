 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TabHost;
 import android.widget.TextView;
 

 public class TimerTabWidget extends TabActivity{
 
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.timer);
 
 	    Resources res = getResources(); // Resource object to get Drawables
 	    TabHost tabHost = getTabHost();  // The activity TabHost
 	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
 	    Intent intent;  // Reusable Intent for each tab
 
 	    // Create an Intent to launch an Activity for the tab (to be reused)
 	    intent = new Intent().setClass(this, TimerActivity.class);
 
 	    // Initialize a TabSpec for each tab and add it to the TabHost
 	    spec = tabHost.newTabSpec("timer").setIndicator("Timer",
 	                      res.getDrawable(R.drawable.tab_timer))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 
 	    // Do the same for the other tabs
 	    intent = new Intent().setClass(this, StopWatchActivity.class);
 	    spec = tabHost.newTabSpec("stopwatch").setIndicator("Stopwatch",
 	                      res.getDrawable(R.drawable.tab_stopwatch))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 
 	    intent = new Intent().setClass(this, TabataActivity.class);
 	    spec = tabHost.newTabSpec("tabata").setIndicator("Tabata",
 	                      res.getDrawable(R.drawable.tab_tabata))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 
 	    tabHost.setCurrentTab(2);
 	}
 }
 
 	
