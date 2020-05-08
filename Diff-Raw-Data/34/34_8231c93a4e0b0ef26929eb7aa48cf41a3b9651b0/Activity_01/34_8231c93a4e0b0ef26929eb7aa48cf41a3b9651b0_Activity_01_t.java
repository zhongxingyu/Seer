 package com.example.exercise_02;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.content.Intent;
 import android.view.Menu;
import android.widget.TextView;
 
 public class Activity_01 extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_activity_01);
		
		TextView txView = (TextView) this
                .findViewById(R.id.textActivity); // I create another TextView just for the Activity 01

		Intent i = getIntent();
        
		if (i!=null) {
    	 
			String strTitle = i.getStringExtra("TITLE");
    	 
    	 	if(strTitle!=null){
    	 		
    		 txView.setText(strTitle);
    	 }
     }
     
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_01, menu);
 		return true;
 	}
 
 }
