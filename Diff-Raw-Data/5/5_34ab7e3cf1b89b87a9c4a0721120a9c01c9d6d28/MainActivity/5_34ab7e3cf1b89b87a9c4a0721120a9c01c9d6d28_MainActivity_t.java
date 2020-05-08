 package org.bitducks.findmydate;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_activity);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.main_activity, menu);
 	return true;
     }
 }
