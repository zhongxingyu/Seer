 package org.bitducks.findmydate;
 
import android.os.Bundle;
 import android.app.Activity;
import android.graphics.Point;
import android.text.Layout;
import android.view.Display;
 import android.view.Menu;
import android.widget.Toast;
import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	getMenuInflater().inflate(R.menu.activity_main, menu);
 	return true;
     }
 }
