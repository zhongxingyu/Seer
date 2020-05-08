 package com.example.wifimanagerprototype;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class CameraAZActivity extends Activity {
 	private final ArrayList<String> listAll = new ArrayList<String>();
 	private final ArrayList<String> listPublic = new ArrayList<String>();
 	private final ArrayList<String> listPrivate = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_camera_az);
 		
 		ListView listview = (ListView) findViewById(R.id.listview_camera_az);
 		this.listAll.add(getString(R.string.network_eduroam));
 		this.listAll.add(getString(R.string.network_wifi));
 		this.listPublic.add(getString(R.string.network_wifi));
 		this.listPrivate.add(getString(R.string.network_eduroam));
 		
 		//listview.setAdapter(new StableArrayAdapter(this, android.R.layout.simple_list_item_1, this.listAll));
 		
 		listview.setAdapter(new NetworkListArrayAdapter(this, android.R.layout.simple_list_item_1, this.listAll));
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//final String item = (String) parent.getItemAtPosition(position);
 				loadHeatmap(null);
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
 		getMenuInflater().inflate(R.menu.main_no_relocate, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 	        case R.id.action_home:
 	    		startActivity(new Intent(this, MainActivity.class));
 	    		return true;
 	        case R.id.action_az:
             	return true;
 	        case R.id.action_favorite:
 	        	startActivity(new Intent(this, CameraFavouritesActivity.class));
 	        	return true;
         	case R.id.action_settings:
        		startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 	public void loadHeatmap(View view) {
     	startActivity(new Intent(this, CameraActivity.class));
     }
 	
 	public void filterAll(View view) {
 		setListAdapter(this.listAll);
 		findViewById(R.id.select_camera_all).setBackgroundResource(R.drawable.search_type_active);
 		findViewById(R.id.select_camera_public).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_camera_private).setBackgroundResource(R.drawable.search_type);
 	}
 	
 	public void filterPublic(View view) {
 		setListAdapter(this.listPublic);
 		findViewById(R.id.select_camera_all).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_camera_public).setBackgroundResource(R.drawable.search_type_active);
 		findViewById(R.id.select_camera_private).setBackgroundResource(R.drawable.search_type);
 	}
 	
 	public void filterPrivate(View view) {
 		setListAdapter(this.listPrivate);
 		findViewById(R.id.select_camera_all).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_camera_public).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_camera_private).setBackgroundResource(R.drawable.search_type_active);
 	}
 	
 	/** Takes an Array of Strings and sets the ListView to pull it's data from
 	 ** that array.
 	 **/
 	private void setListAdapter(ArrayList<String> list) {
 		ListView listview = (ListView) findViewById(R.id.listview_camera_az);
 		listview.setAdapter(new NetworkListArrayAdapter(this, android.R.layout.simple_list_item_1, list));
 	}
 
 }
