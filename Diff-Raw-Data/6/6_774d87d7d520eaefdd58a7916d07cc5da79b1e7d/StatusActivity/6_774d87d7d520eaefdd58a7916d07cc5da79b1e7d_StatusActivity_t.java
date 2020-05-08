 package com.akimdelli.akeur;
 
 import winterwell.jtwitter.Twitter;
 import android.os.Bundle;
 import android.app.Activity;
import android.util.Log;
 import android.view.Menu;
import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class StatusActivity extends Activity implements OnClickListener {
 	private static final String TAG = "StatusActivity";
 	EditText editText;
 	Button updateButton;
 	Twitter twitter;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.status);
         
         // Find views
         editText = (EditText) findViewById(R.id.editText);
         updateButton = (Button) findViewById(R.id.buttonUpdate);
         
         updateButton.setOnClickListener(this);
         
        twitter = new Twitter();
         twitter.setAPIRootUrl("http://yamba.marakana.com/api");
     }
 
     // Called when button is clicked
     public void onClick(View v) {
     	twitter.setStatus(editText.getText().toString());
     	Log.d(TAG, "onClicked");
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_status, menu);
         return true;
     }
     
 }
