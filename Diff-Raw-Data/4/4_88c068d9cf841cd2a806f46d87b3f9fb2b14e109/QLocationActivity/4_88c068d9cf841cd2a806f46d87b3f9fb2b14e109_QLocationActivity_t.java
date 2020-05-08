 package com.qualcomm.location;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.View;
 import android.widget.TextView;
 
 public class QLocationActivity extends Activity {
 	TextView output;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         output = (TextView)findViewById(R.id.output);
     }
     
     /** Called when button is clicked. */
    public void onClick(View v) {
     		
     }
 }
