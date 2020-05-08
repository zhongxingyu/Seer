 package org.hicapacity.techhui;
 
import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
import android.webkit.WebView;
 import android.widget.TabHost;
 
 /**
  * @author Jason Axelson
  * 
  */
 public class ConferenceOverview extends TabActivity {
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.conference);
     Resources res = getResources(); // Resource object to get Drawables
     TabHost tabHost = getTabHost(); // The activity TabHost
     TabHost.TabSpec spec; // Resusable TabSpec for each tab
     Intent intent; // Reusable Intent for each tab
 
     // Create an Intent to launch an Activity for the tab (to be reused)
     intent = new Intent().setClass(this, ConferenceActivity.class);
 
     // Initialize a TabSpec for each tab and add it to the TabHost
     spec = tabHost.newTabSpec("track1")
         .setIndicator("Track 1", res.getDrawable(R.drawable.ic_tab_conference))
         .setContent(intent);
     tabHost.addTab(spec);
 
     // Do the same for the other tabs
     intent = new Intent().setClass(this, ConferenceOverview.class);
     //intent = new Intent().setClass(this, WebScheduleActivity.class);
     spec = tabHost.newTabSpec("track2")
         .setIndicator("Track 2", res.getDrawable(R.drawable.ic_tab_conference)).setContent(intent);
     tabHost.addTab(spec);
 
     tabHost.setCurrentTab(0);
   }
 }
