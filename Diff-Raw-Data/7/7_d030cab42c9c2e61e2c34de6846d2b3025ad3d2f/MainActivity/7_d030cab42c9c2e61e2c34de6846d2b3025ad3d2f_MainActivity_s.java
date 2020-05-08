 package org.sethnr.MetropoList;
 
 import org.sethnr.MetropoList.map.MLMapActivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
 	public String CURRENT_RESULTS = "org.sethnr.metropolist.CURRENT_RESULTS";
 	
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
     
     /** Called when the user clicks the maps button */
    public void viewMaps(View view) {
     	Log.d("main","calling view maps:");
     	Intent intent = new Intent(this, MLMapActivity.class);
  //   	MapView mapview = (MapView) findViewById(R.id.mapview);
  //  	RESTResultSet results = mapview.getExtra(CURRENT_RESULTS);
  //   	intent.putExtra(CURRENT_RESULTS, results);
     	Log.d("main",intent.toString());
     	startActivity(intent);    	
     }
 
     /** Called when the user clicks the maps button */
     public void viewList(View view) {
     	Log.d("main","calling view list:");
     	Intent intent = new Intent(this, MLListActivity.class);
  //   	MapView mapview = (MapView) findViewById(R.id.mapview);
  //  	RESTResultSet results = mapview.getExtra(CURRENT_RESULTS);
  //   	intent.putExtra(CURRENT_RESULTS, results);
     	Log.d("main",intent.toString());
     	startActivity(intent);    	
     }
 
 
 }
