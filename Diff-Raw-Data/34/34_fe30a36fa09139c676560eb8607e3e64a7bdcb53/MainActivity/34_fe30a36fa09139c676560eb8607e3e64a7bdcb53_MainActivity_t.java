 package com.example.getconnected.activities;
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
 
 import com.example.getconnected.R;
import com.example.getconnected.sqllite.DatabaseHandler;
 
 
 import android.os.Bundle;
 import android.view.Menu;
 
 public class MainActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		initLayout(R.string.title_activity_main, false, false, false, false);
		
		DatabaseHandler db = new DatabaseHandler(this);
		//db.deleteAll();
		//db.addBusstops();
		
		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
