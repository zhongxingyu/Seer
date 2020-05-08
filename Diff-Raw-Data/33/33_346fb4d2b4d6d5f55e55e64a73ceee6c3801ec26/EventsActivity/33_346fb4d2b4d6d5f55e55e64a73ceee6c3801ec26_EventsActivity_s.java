 package com.allplayers.android;
  
 import com.allplayers.objects.AlbumData;
 import com.allplayers.objects.EventData;
  
 import com.allplayers.rest.RestApiV1;
  
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
  
 import java.util.ArrayList;
 import java.util.HashMap;
  
 public class EventsActivity extends ListActivity {
     private ArrayList<EventData> eventsList;
     private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
     private boolean hasEvents = false;
     private String jsonResult;
  
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
  
         //check local storage
         if (LocalStorage.getTimeSinceLastModification("UserEvents") / 1000 / 60 < 10) { //more recent than 10 minutes
             jsonResult = LocalStorage.readUserEvents(getBaseContext());
             setEventsMap();
         } else {
            //jsonResult = RestApiV1.getUserEvents();
          GetUserEventsTask helper = new GetUserEventsTask();
         	helper.execute();
         }
     }
  
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
  
         if (hasEvents) {
             // Can be used to display map or full details.
             Intent intent = (new Router(this)).getEventDisplayActivityIntent(eventsList.get(position));
             startActivity(intent);
         }
     }
     
     protected void setEventsMap() {
     	EventsMap events = new EventsMap(jsonResult);
         eventsList = events.getEventData();
         HashMap<String, String> map;
  
         if (!eventsList.isEmpty()) {
             for (int i = 0; i < eventsList.size(); i++) {
                 map = new HashMap<String, String>();
                 map.put("line1", eventsList.get(i).getTitle());
  
                 String start = eventsList.get(i).getStartDateString();
                 map.put("line2", start);
                 list.add(map);
             }
  
             hasEvents = true;
         } else {
             map = new HashMap<String, String>();
             map.put("line1", "No events to display.");
             map.put("line2", "");
             list.add(map);
             hasEvents = false;
         }
  
         String[] from = {"line1", "line2"};
  
         int[] to = {android.R.id.text1, android.R.id.text2};
  
         SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);
         setListAdapter(adapter);
     }
     
     public class GetUserEventsTask extends AsyncTask<Void, Void, String> {
    	 
         protected String doInBackground(Void... args) {
         	return RestApiV1.getUserEvents();
         }
         
         protected void onPostExecute(String jsonResult) {
         	EventsActivity.this.jsonResult = jsonResult;
         	LocalStorage.writeUserEvents(getBaseContext(), jsonResult, false);
         	setEventsMap();
         }
      }
 }
