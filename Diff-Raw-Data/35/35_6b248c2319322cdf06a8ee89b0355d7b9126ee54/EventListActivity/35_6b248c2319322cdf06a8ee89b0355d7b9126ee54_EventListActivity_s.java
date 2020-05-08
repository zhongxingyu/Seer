 package no.uka.findmyapp.ukaprogram.activities;
 
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import no.uka.findmyapp.android.rest.datamodels.models.UkaEvent;
 import no.uka.findmyapp.ukaprogram.R;
 import no.uka.findmyapp.ukaprogram.adapters.EventListCursorAdapter;
 import no.uka.findmyapp.ukaprogram.wrapper.EventDatabase;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 public class EventListActivity extends ListActivity {	
 	private final static String debug = "EventListActivity";
 	
 	private final static String ORDER_BY = UkaEventContract.SHOWING_TIME + " asc";
 	public final static String ITEM_CLICKED = "clicked";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.event_list);
 		Cursor eventCursor = this.managedQuery(UkaEventContract.EVENT_CONTENT_URI, null, null, null, ORDER_BY);
 		this.setListAdapter(new EventListCursorAdapter(this, eventCursor));
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		Cursor c = (Cursor) l.getItemAtPosition(position);
 		
 		UkaEvent event = EventDatabase.getInstance().getEventFromCursor(c);
 		Log.v(debug, event.toString());
 
 		Intent intent = new Intent(this, EventDetailsActivity.class); 
 		intent.putExtra(ITEM_CLICKED, event);
 		
 		startActivity(intent);
 	}
 }
