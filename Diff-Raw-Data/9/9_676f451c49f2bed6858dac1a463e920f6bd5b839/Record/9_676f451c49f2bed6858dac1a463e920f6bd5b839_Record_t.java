 package com.example.gifting;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 
 public class Record extends Activity {
 
	private static final int ACTION_TAKE_VIDEO = 3;
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_record);
		PackageManager pm=this.getPackageManager();
		if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			//create an alert dialog
 			
		}else{
 			
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_record, menu);
 		return true;
 	}
 	
 	private void dispatchTakeVideoIntent() {
 	    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 	    startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
 	}
 
 }
