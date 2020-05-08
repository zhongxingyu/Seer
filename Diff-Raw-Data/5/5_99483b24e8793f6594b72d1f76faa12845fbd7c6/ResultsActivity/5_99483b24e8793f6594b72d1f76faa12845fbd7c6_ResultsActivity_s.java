 package com.hack.regionunlocked;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
 
 
 public class ResultsActivity extends Activity {
 
 	@Override
 	public void onBackPressed() {
 		finish();
 		
 		 this.startActivity(new Intent(ResultsActivity.this,MainActivity.class));  
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.results);
 		/*ImageButton scanButton = (ImageButton) findViewById(R.id.scanButton);
 	    scanButton.setOnClickListener(new View.OnClickListener() {
 	    	public void onClick(View v) {
 	    		Intent clickIntent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
 	    		startActivityForResult(clickIntent, 1);
 			 }
 		 });*/
 	}
 }
