 package com.connectsy.events;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.connectsy.ActionBarHandler;
 import com.connectsy.R;
 import com.connectsy.categories.CategoryManager;
 import com.connectsy.categories.CategoryManager.Category;
 import com.connectsy.data.DataManager;
 import com.connectsy.data.DataManager.DataUpdateListener;
 import com.connectsy.events.EventManager.Event;
 import com.connectsy.events.EventManager.Filter;
 import com.connectsy.settings.MainMenu;
 
 public class EventList extends Activity implements DataUpdateListener, 
 		OnClickListener, OnItemClickListener, LocationListener {
 	@SuppressWarnings("unused")
 	private static final String TAG = "EventList";
 	private EventsAdapter adapter;
     private EventManager eventManager = null;
     private ArrayList<Event> events;
     private Filter filter;
     private String category;
     private final int GET_EVENTS = 0;
     private final int SELECT_CATEGORY = 1;
 	
     public static enum Init {CATEGORY, FRIENDS}
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.event_list);
         
         ActionBarHandler abHandler = new ActionBarHandler(this);
         ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
         abNewEvent.setOnClickListener(abHandler);
         ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
         abRefresh.setOnClickListener(this);
         
         Bundle b = getIntent().getExtras();
         if (b != null && b.containsKey("filter")){
     		filter = (Filter) b.get("filter");
 	        if (b.containsKey("category"))
 	        	category = b.getString("category");
         }else{
         	filter = Filter.ALL;
         }
         
         if (filter == Filter.CATEGORY && category == null){
         	category = DataManager.getCache(this).getString("category_saved", "All");
         }
         
         updateData();
         
         TextView heading = (TextView)findViewById(R.id.event_list_heading_text);
         if (filter == Filter.ALL)
        	heading.setText("All Activity");
         if (filter == Filter.FRIENDS)
        	heading.setText("Friends Activity");
         if (filter == Filter.CATEGORY){
         	heading.setText("Category: "+category);
         	findViewById(R.id.event_list_heading_arrow).setVisibility(View.VISIBLE);
         	LinearLayout cat = (LinearLayout)findViewById(R.id.event_list_heading);
         	cat.setClickable(true);
         	cat.setOnClickListener(this);
         }
         
         ListView lv = (ListView)findViewById(R.id.events_list);
         lv.setOnItemClickListener(this);
         lv.setAdapter(adapter);
         refresh();
         
         CategoryManager.precacheCategories(this);
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         return MainMenu.onCreateOptionsMenu(menu);
 	}
     
     public boolean onOptionsItemSelected(MenuItem item) {
         return MainMenu.onOptionsItemSelected(this, item);
     }
 
 	public void onDataUpdate(int code, String response) {
 		updateData();
 		setRefreshing(false);
 	}
 
 	public void onRemoteError(int httpStatus, int code) {
 		setRefreshing(false);
 	}
 
 	public void onClick(View v) {
     	if (v.getId() == R.id.ab_refresh) refresh();
     	else if (v.getId() == R.id.event_list_heading){
     		Intent i = new Intent(Intent.ACTION_CHOOSER);
     		i.setType("vnd.android.cursor.item/vnd.connectsy.category");
     		startActivityForResult(i, SELECT_CATEGORY);
     	}
 	}
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (resultCode == RESULT_OK && requestCode == SELECT_CATEGORY){
 			try {
 				category = new Category(data.getExtras().getString("com.connectsy.category")).name;
 				DataManager.getCache(this).edit()
 						.putString("category_saved", category).commit();
 				((TextView)findViewById(R.id.event_list_heading_text)).setText("Category: "+category);
 				updateData();
 				refresh();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void updateData(){
         eventManager = new EventManager(this, this, filter, category);
         events = eventManager.getEvents();
         if (adapter != null){
         	adapter.clear();
         	for (int n = 0;n < events.size();n++)
         		adapter.add(events.get(n));
     		adapter.notifyDataSetChanged();
         }else{
             adapter = new EventsAdapter(this, R.layout.event_list_item, events);
         }
 	}
 	
 	private void refresh(){
 		eventManager.refreshEvents(GET_EVENTS);
 		setRefreshing(true);
 	}
 	
     private void setRefreshing(boolean on) {
     	if (on){
 	        findViewById(R.id.ab_refresh).setVisibility(View.GONE);
 	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
     	}else{
 	        findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
 	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
     	}
     }
 
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		Event event = adapter.getItem(position);
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		i.setType("vnd.android.cursor.item/vnd.connectsy.event");
 		i.putExtra("com.connectsy.events.revision", event.revision);
 		startActivity(i);
 	}
 
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 }
