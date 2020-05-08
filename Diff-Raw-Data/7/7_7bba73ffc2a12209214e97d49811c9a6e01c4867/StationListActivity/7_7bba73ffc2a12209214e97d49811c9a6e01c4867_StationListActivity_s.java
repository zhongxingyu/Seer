 package com.poguico.palmabici;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class StationListActivity extends ActionBarActivity {
 	ArrayList <Station> stations;
 	ProgressDialog dialog;
 
 	private class SynchronizeTask extends AsyncTask <Void, Void, Void> {
         
 		Activity activity;
 		
 		public SynchronizeTask (Activity activity) {
 			this.activity = activity;
 		}
 		
     	protected Void doInBackground(Void... params) {
     		NetworkInfo.setNetwork(Synchronizer.getNetworkInfo());
             return null;
         }
 
         protected void onPostExecute(Void params) {
         	dialog.hide();
         	Toast.makeText(activity, "Estacions actualitzades!", Toast.LENGTH_SHORT).show();
         	StationList station_list = new StationList(activity, NetworkInfo.getNetwork());        
             ListView list = (ListView) findViewById(R.id.stationList);        
             list.setAdapter(station_list);
         }
     }
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         
         setContentView(R.layout.main);
         
         StationList station_list = new StationList(this, NetworkInfo.getNetwork());        
         ListView list = (ListView) findViewById(R.id.stationList);        
         list.setAdapter(station_list);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.main, menu);
 
         // Calling super after populating the menu is necessary here to ensure that the
         // action bar helpers have a chance to handle this event.
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
                 break;
 
             case R.id.menu_refresh:
             	dialog = ProgressDialog.show(this, "", "Actualitzant estat...", true);
                 new SynchronizeTask(this).execute((Void [])null);
                 break;
 
             case R.id.menu_credits:
                 Toast.makeText(this, "Tapped credits", Toast.LENGTH_SHORT).show();
                 break;
 
             case R.id.menu_preferences:
                 Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                 break;
                 
             case R.id.menu_report:
                Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
