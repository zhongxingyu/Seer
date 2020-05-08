 package com.oldcherokee.ftpeekr.fragments;
 
 import com.oldcherokee.ftpeekr.R;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class Detail extends Activity {
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    // Need to check if Activity has been switched to landscape mode
 	    // If yes, finished and go back to the start Activity
 	    if (getResources().getConfiguration().orientation == 
 	        Configuration.ORIENTATION_LANDSCAPE) {
 	    	finish();
 	    	return;
 	    }
 
 	    setContentView(R.layout.details);
 	    Bundle extras = getIntent().getExtras();
 	    if (extras != null) {
 	      String s = extras.getString("value");
	      TextView view = (TextView) findViewById(R.id.details_text);
 	      view.setText(s);
 	    }
 	}
 }
