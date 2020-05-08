 package fi.dratini.keikkalista;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.DateFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import fi.dratini.keikkalista.core.EventModule;
 import fi.dratini.keikkalista.core.EventService;
 import fi.dratini.keikkalista.core.dataaccess.DatabaseHelper;
 import fi.dratini.keikkalista.core.model.Event;
 import fi.dratini.keikkalista.core.model.EventArtist;
 import fi.dratini.keikkalista.core.model.Preference;
 
 public class MainActivity extends Activity  {
     public final static String EventsKey = "fi.dratini.keikkalista.MainActivity.events";
     public final static String PreferenceKey = "fi.dratini.keikkalista.MainActivity.preferece";
     public final static String EventsLoadedTimestampKey = "fi.dratini.keikkalista.MainActivity.eventsLastLoadedTimestamp";
     
     private EventService eventService;
     private EventListArrayAdapter arrayAdapter;
     private List<Event> events = new ArrayList<Event>();
     private Preference preference = Preference.TabulaRasa;
    private LoadEventsTask currentTaskEvents;
    private FindEventsTask currentTaskFind;  
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         eventService = initializeService();
         
         ListView lv = (ListView) findViewById(R.id.eventList);
         arrayAdapter = new EventListArrayAdapter(this, android.R.layout.simple_list_item_1, events);
         lv.setAdapter(arrayAdapter);
 
         lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                 Event event = events.get(position);
                 if (event.hasUrl()) {
                     Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getUrl()));
                     startActivity(myIntent);
                 }
             }
         });
         
         if (savedInstanceState != null) {
             Serializable eventsSerializable = savedInstanceState.getSerializable(EventsKey);
             Serializable preferenceSerializable = savedInstanceState.getSerializable(PreferenceKey);
             if (eventsSerializable != null && preferenceSerializable != null) {
                 updateEvents((List<Event>) eventsSerializable, (Preference) preferenceSerializable);
             } 
         }
         
         if (isEventLoadRequired()) {
            new LoadEventsTask().execute();
         }
     }
 
     private EventService initializeService() {
         EventModule eventModule = new EventModule(new DatabaseHelper(this));
         Injector injector = Guice.createInjector(eventModule);
         return injector.getInstance(EventService.class);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putSerializable(EventsKey, new ArrayList<Event>(events));
         outState.putSerializable(PreferenceKey, preference);
        
        // cancel active tasks
        if (currentTaskEvents != null) {
            currentTaskEvents.cancel(false);
        }
        if (currentTaskFind != null) {
            currentTaskFind.cancel(true);
        }
     }
         
     private boolean isEventLoadRequired() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         long timestamp = prefs.getLong(EventsLoadedTimestampKey, 0);
         return timestamp < System.currentTimeMillis() - Defaults.AutoRefreshInterval;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.load_events:
                currentTaskEvents = new LoadEventsTask();
                currentTaskEvents.execute();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void findEvents(View view) {
         EditText usernameField = (EditText) findViewById(R.id.username);
         String username = usernameField.getText().toString().trim();
        currentTaskFind = new FindEventsTask();
        
         if (username.equals("")) {
            currentTaskFind.execute(); 
         } else {
            currentTaskFind.execute(username);
         }
     }
     
     private void updateEvents(List<Event> events) {
         updateEvents(events, Preference.TabulaRasa);
     }
     
     private void updateEvents(List<Event> events, Preference preference) {
         this.events.clear();
         this.events.addAll(events);
         this.preference = preference;
         arrayAdapter.updatePreference(preference);
         arrayAdapter.notifyDataSetChanged();
     }
     
     
    private class FindEventsTask extends AsyncTask<String, Void, List<Event>> {
         private Preference pref = Preference.TabulaRasa;
         private ProgressDialog progressDialog;
         
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(MainActivity.this, "Search events", "Searching...", true);
         }
         
         protected List<Event> doInBackground(String... usernames) {
             if (usernames == null || usernames.length == 0) {
                 return eventService.FindEvents();
             } else {
                 String username = usernames[0];
                 pref = eventService.FindPreferenceFor(username);
                 return eventService.FindEventsFor(pref);
             }
         }
         
         @Override
         protected void onPostExecute(List<Event> events) {
             updateEvents(events, pref);
             String message = (events.size() == 0) 
                              ? "No events found" 
                              : String.format("Found %d events", events.size());
             Toast.makeText(MainActivity.this , message, Toast.LENGTH_SHORT).show();
             progressDialog.dismiss();
         }
     }
     
    private class LoadEventsTask extends AsyncTask<Void, Void, List<Event>> {
         private ProgressDialog progressDialog;
         
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(MainActivity.this, "Load events", "Loading...", true);
         }
         
         protected List<Event> doInBackground(Void... params) {
             eventService.LoadEvents();
             return eventService.FindEvents();
         }
         
         @Override
         protected void onPostExecute(List<Event> events) {
             updateEvents(events);
             String message = (events.size() == 0) 
                              ? "No events loaded" 
                              : String.format("Loaded %d events", events.size());
             Toast.makeText(MainActivity.this , message, Toast.LENGTH_SHORT).show();
             progressDialog.dismiss();
             
             if (events.size() > 0) {
                 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                 Editor editor = prefs.edit();
                 editor.putLong(EventsLoadedTimestampKey, System.currentTimeMillis());
                 editor.commit();
             }
         }
     }
 }
