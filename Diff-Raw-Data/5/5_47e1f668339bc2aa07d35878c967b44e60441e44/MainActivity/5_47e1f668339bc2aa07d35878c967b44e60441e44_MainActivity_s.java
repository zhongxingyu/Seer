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
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private EventArrayListAdapter fEventAdapter;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         ApplicationSettings settings = new ApplicationSettings(this);
         
         LinearLayout listLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main, null);
         ListView listView = (ListView) listLayout.findViewById(R.id.eventListView);
         fEventAdapter = new EventArrayListAdapter(this, getLayoutInflater());
         listView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Event event = fEventAdapter.getEntries().get(position);
 				Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
 				intent.putExtra("event", event);
 				startActivity(intent);
 			}
 			
 		});
         listView.setAdapter(fEventAdapter);
 
         
         Button registerButton = (Button) listLayout.findViewById(R.id.registerButton);
         TextView welcomeMessage = (TextView) listLayout.findViewById(R.id.welcomeMessage);
         if (settings.isUserRegistered()) {
        	registerButton.setVisibility(View.INVISIBLE);
         	welcomeMessage.setVisibility(View.VISIBLE);
         	welcomeMessage.setText("Welcome " + settings.getRegisteredUser()); 
         } else {
         	registerButton.setVisibility(View.VISIBLE);
        	welcomeMessage.setVisibility(View.INVISIBLE);
         	registerButton.setOnClickListener(new OnClickListener() {
     			public void onClick(View v) {
     				Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
     				startActivity(intent);
     			}
     		});
         }
         
         setContentView(listLayout);
         
         queryEvents();
         CurrentEventChecker ch = new CurrentEventChecker(this);
         ch.checkForEvent();
     }
 	
 	private void queryEvents() {
 		new HackergartenClient().listUpcomingEvents(new AsyncCallback<List<Event>>() {
 			
 			public void onSuccess(final List<Event> result) {
 				runOnUiThread(new Runnable() {
 					
 					public void run() {
 						fEventAdapter.setEntries(result);
 					}
 				});
 			}
 			
 			public void onFailure(final Throwable t) {
 				runOnUiThread(new Runnable() {
 					
 					public void run() {
 						Toast.makeText(MainActivity.this, "Failed to contact server.", Toast.LENGTH_LONG).show();
 						Log.e(MainActivity.class.getName(), "Failed to contact server.", t);
 					}
 				});
 				
 			}
 			
 		});
 	}
 	
 }
