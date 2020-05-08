 package com.call4paperz.android.activities;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.call4paperz.android.Call4PaperzApplication;
 import com.call4paperz.android.R;
 import com.call4paperz.android.adapters.EventsAdapter;
 import com.call4paperz.android.model.Event;
import org.jboss.aerogear.android.pipeline.AbstractActivityCallback;
 import org.jboss.aerogear.android.pipeline.LoaderPipe;
 
 import java.util.List;
 
 public class EventsActivity extends SherlockListActivity {
 
     private ListView eventsListView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.events);
 
         eventsListView = (ListView) findViewById(android.R.id.list);
         eventsListView.setClickable(true);
         eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                 Event event = (Event) eventsListView.getAdapter().getItem(position);
                 Intent eventIntent = new Intent(EventsActivity.this, EventActivity.class);
                 // TODO Move to static variable
                 eventIntent.putExtra("event", event);
                 startActivity(eventIntent);
             }
         });
 
         this.loadEvents();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu_events, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.eventsMenu.refresh) {
             loadEvents();
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private void loadEvents() {
 
         final ProgressDialog progress = ProgressDialog.show(EventsActivity.this,
                 getString(R.string.loading),
                 getString(R.string.load_events),
                 true,
                 true);
 
         Call4PaperzApplication application = (Call4PaperzApplication) getApplication();
         LoaderPipe<Event> eventPipe = application.getEventPipe(this);
 
        eventPipe.read(new AbstractActivityCallback<List<Event>>() {
             @Override
             public void onSuccess(List<Event> events) {
                setListAdapter(new EventsAdapter(EventsActivity.this, events));
                 progress.dismiss();
             }
 
             @Override
             public void onFailure(Exception e) {
                 progress.dismiss();
                 Log.e("Call4Paperz", e.getMessage(), e);
                 Toast.makeText(EventsActivity.this, getString(R.string.not_connection), Toast.LENGTH_LONG).show();
             }
         });
 
     }
 
 }
