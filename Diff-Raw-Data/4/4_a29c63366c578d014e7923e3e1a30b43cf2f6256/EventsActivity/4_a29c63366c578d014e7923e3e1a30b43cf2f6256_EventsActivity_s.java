 package org.waynak.hackathon;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 public class EventsActivity extends Activity
 {
 	private ListView eventsListView;
 
 	private Event[] eventList;
 
 	int clickedEventPosition = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.events);
 		
 		
 		ImageView iv = (ImageView) this.findViewById(R.id.descImg);
 		
 		TextView tv = (TextView) this.findViewById(R.id.descTv);
 
 		/*
 		 	public static final String NYUAD = "NYU Abu Dhabi";	
 			public static final String QASRALHOSN = "Qasr Al Hosn";
 			public static final String DALMAISLAND = "Dalma Island"; 
 		 */
 		String whichPlace = getIntent().getStringExtra("place");
 
 		
 
 		if (whichPlace.equals(WaynakMapActivity.NYUAD)) {
 			iv.setImageResource(R.drawable.abudhabi);
 			tv.setText("Abu Dhabi");
 
 			eventList = new Event[7];
 			eventList[0] = new Event("Fish Souk",R.drawable.fish_souk,"");
 			
 			eventList[1] = new Event("Old Abu Dhabi Souk (1962)",R.drawable.souk1,"");
 			eventList[2] = new Event("Old Abu Dhabi Souk",R.drawable.souk2,"");
 			eventList[3] = new Event("Old Abu Dhabi Souk",R.drawable.souk3,"");
 			eventList[4] = new Event("Old Abu Dhabi Souk",R.drawable.souk4,"");
 			eventList[5] = new Event("Old Abu Dhabi Souk (1962)",R.drawable.souk5,"");
 			eventList[6] = new Event("Old Abu Dhabi Souk",R.drawable.souk6,"");
 			
 		} else if (whichPlace.equals(WaynakMapActivity.QASRALHOSN)) {
 			iv.setImageResource(R.drawable.qasralhosn);
 			tv.setText(WaynakMapActivity.QASRALHOSN);
 			
 			eventList = new Event[12];
 			eventList[0] = new Event("Qasr Al Hosn in the past.",R.drawable.hosn1,"");
 			eventList[1] = new Event("Qasr Al Hosn Fort (1962).",R.drawable.hosn2,"");
 			eventList[2] = new Event("Qasr Al Hosn (1961).",R.drawable.hosn3,"");
 			eventList[3] = new Event("Qasr Al Hosn Fort (1957).",R.drawable.hosn4,"");
 			eventList[4] = new Event("Qasr al-Hosn",R.drawable.hosn5,"Sheikh Zayed bin Khalifa the first , in his place in the open air with the Senate and the citizens, which taken by the German photographer Berrtkart during his visit to the Arabian Gulf and Abu Dhabi in February (1904).");
 			eventList[5] = new Event("Qasr al-Hosn",R.drawable.hosn6,"");
 			eventList[6] = new Event("Qasr al-Hosn",R.drawable.hosn7,"");
 			eventList[7] = new Event("Qasr al-Hosn",R.drawable.hosn8,"");
 			eventList[8] = new Event("Qasr al-Hosn Fort",R.drawable.hosn9,"");
 			eventList[9] = new Event("Qasr al-Hosn Fort",R.drawable.hosn10,"");
 			eventList[10] = new Event("Qasr al-Hosn in the past",R.drawable.hosn11,"");
 			eventList[11] = new Event("Qasr al-Hosn in the past (1963).",R.drawable.hosn12,"");
 			
 			
 
 		} else if (whichPlace.equals(WaynakMapActivity.DALMAISLAND)) {
 			iv.setImageResource(R.drawable.dalmaisland);
 			tv.setText(WaynakMapActivity.DALMAISLAND);			
 
 			eventList = new Event[9];
 			eventList[0] = new Event("Pearl Diving and Fishing",R.drawable.pearl1,"A group of Citizens preparing their nets for their fishing trip in Dalma Island.");
 			eventList[1] = new Event("Pearl Diving and Fishing",R.drawable.pearl2,"A set of nets used by fishermen to catch fish near the beach.");
 			eventList[2] = new Event("Pearl Diving and Fishing",R.drawable.pearl3,"A citizen preparing the nets for fishing.");
 			eventList[3] = new Event("Pearl Diving and Fishing",R.drawable.pearl4,"A fisherman drying fish.");
 			eventList[4] = new Event("Pearl Diving and Fishing",R.drawable.pearl5,"A citizen build dhows.");
 			eventList[5] = new Event("Pearl Diving and Fishing",R.drawable.pearl6,"A citizen build dhows.");
 			eventList[6] = new Event("Pearl Diving and Fishing",R.drawable.pearl7,"Pearling is the main occupation.");
 			eventList[7] = new Event("Pearl Diving and Fishing",R.drawable.pearl8,"A group of men receiving a signal from the diver by hand connecting rope between him and the diver.");
 			eventList[8] = new Event("Pearl Diving and Fishing",R.drawable.pearl9,"A group of divers preparing for a diving trip.");
 			
 		} else {
 		}
 		
 
 		eventsListView = (ListView) findViewById(R.id.eventsList);
 		eventsListView.setAdapter(new EventsAdapater(this, R.layout.events_row, eventList));
 		
 		eventsListView.setOnItemClickListener(new OnItemClickListener() {
 			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			    	clickedEventPosition = position;
 			    	//Toast.makeText(getApplicationContext(), eventList[position].eventName, Toast.LENGTH_SHORT).show();
 					Intent i = new Intent(EventsActivity.this,ImageAvctivity.class);
 					i.putExtra("resource",eventList[clickedEventPosition].imageId);
 					i.putExtra("title", eventList[clickedEventPosition].eventName);
 					i.putExtra("description", eventList[clickedEventPosition].description);
 					startActivity(i);
					TextView mainText = (TextView) findViewById(R.id.mainText);
					mainText.setText(eventList[position].description);
 			    }
 		});
 
 	}
 }
