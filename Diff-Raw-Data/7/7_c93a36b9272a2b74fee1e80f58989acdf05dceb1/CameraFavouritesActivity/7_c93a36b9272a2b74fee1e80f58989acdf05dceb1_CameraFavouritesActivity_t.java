 package com.example.wifimanagerprototype;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 public class CameraFavouritesActivity extends Activity {
 	private final ArrayList<String> listAll = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_camera_favourites);
 		
 		ListView listview = (ListView) findViewById(R.id.listview_camera_favorite);
 		this.listAll.add(getString(R.string.network_eduroam));
 		this.listAll.add(getString(R.string.network_wifi));
 		
 		listview.setAdapter(new NetworkListArrayAdapter(this, android.R.layout.simple_list_item_1, this.listAll));
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//final String item = (String) parent.getItemAtPosition(position);
 				loadHeatmap();
 			}
 		});
 		listview.setOnTouchListener(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_MOVE) {
                     return true;
                 }
                 return false;
             }
 
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.camera_favourites, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 	        case R.id.action_home:
 	    		startActivity(new Intent(this, MainActivity.class));
 	    		return true;
 	        case R.id.action_az:
             	startActivity(new Intent(this, CameraAZActivity.class));
             	return true;
 	        case R.id.action_favorite:
 	        	return true;
         	case R.id.action_settings:
        		startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 
 	public void loadHeatmap() {
     	startActivity(new Intent(this, CameraActivity.class));
     }
 	
 }
