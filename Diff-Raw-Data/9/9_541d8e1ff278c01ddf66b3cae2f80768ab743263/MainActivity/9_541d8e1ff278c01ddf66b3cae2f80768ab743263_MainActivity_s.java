 package net.hackergarten.android.app;
 
 import java.util.List;
 
 import net.hackergarten.android.app.client.AsyncCallback;
 import net.hackergarten.android.app.client.HackergartenClient;
 import net.hackergarten.android.app.model.Event;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private EventArrayListAdapter fEventAdapter;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         LinearLayout listLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main, null);
         ListView listView = (ListView) listLayout.findViewById(R.id.eventListView);
         fEventAdapter = new EventArrayListAdapter(this, getLayoutInflater());
         listView.setAdapter(fEventAdapter);
 
         Button registerButton = (Button) listLayout.findViewById(R.id.registerButton);
         registerButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
 				startActivity(intent);
 			}
 		});
         
         setContentView(listLayout);
         
         queryEvents();
         CurrentEventChecker ch = new CurrentEventChecker(this);
         ch.checkForEvent();
     }
 	
 	private void queryEvents() {
 		new HackergartenClient().listUpcomingEvents(new AsyncCallback<List<Event>>() {
 			
			public void onSuccess(List<Event> result) {
				fEventAdapter.setEntries(result);
 			}
 			
 			public void onFailure(final Throwable t) {
 				runOnUiThread(new Runnable() {
 					
 					public void run() {
 						Toast.makeText(MainActivity.this, "Failed to query server.", Toast.LENGTH_LONG);
 						Log.e(MainActivity.class.getName(), "Failed to contact server.", t);
 					}
 				});
 				
 			}
 			
 		});
 	}
 	
 }
