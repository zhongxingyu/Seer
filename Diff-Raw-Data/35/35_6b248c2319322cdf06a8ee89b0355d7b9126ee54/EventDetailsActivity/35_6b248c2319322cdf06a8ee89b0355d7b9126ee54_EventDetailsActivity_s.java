 package no.uka.findmyapp.ukaprogram.activities;
 
 import java.util.ArrayList;
 
 import no.uka.findmyapp.android.rest.datamodels.models.UkaEvent;
 import no.uka.findmyapp.ukaprogram.R;
 import no.uka.findmyapp.ukaprogram.utils.DateUtils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventDetailsActivity extends Activity implements OnClickListener {
 	private static final String debug = "EventsDetailsActivity";
 	
 	private UkaEvent selectedEvent; 
 	private ArrayList<String> friendList;
 	private String TAG = "EventDetails";
 	private ArrayAdapter<String> friendAdapter;
 	private ListView friendListView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.event_details);
 		
 		Log.v(debug, debug + " onCreate()");
 		
 		Bundle bundle = getIntent().getExtras(); 
 		Log.v(debug, "Bundle toString " + bundle.toString());
 		
 		if (bundle != null) {
 			DateUtils du = new DateUtils(); 
 
 			Button friendsButton = (Button) findViewById(R.id.detailedEventFriendsOnEventButton);
 			friendsButton.setOnClickListener(this); 
 
 			selectedEvent = (UkaEvent) bundle.getSerializable(EventListActivity.ITEM_CLICKED);
 			Log.v(debug, "selectedEvent " + selectedEvent.toString());
 
 			TextView ageLimit = (TextView) findViewById(R.id.detailedEventAgeLimit);
 			TextView price = (TextView) findViewById(R.id.detailedEventPrice);
 			TextView title = (TextView) findViewById(R.id.detailedEventTitle);
 			TextView timeAndPlace = (TextView) findViewById(R.id.detailedEventTimeAndPlace);
 			TextView description = (TextView) findViewById(R.id.detailedEventDescription);
 
 			timeAndPlace.setText(	
 				du.getWeekdayNameFromTimestamp(selectedEvent.getShowingTime()) + " " 
 				+ du.getCustomDateFormatFromTimestamp("dd E MMM.", selectedEvent.getShowingTime()) + " " 
 				+ du.getTimeFromTimestamp(selectedEvent.getShowingTime()) + ", " 
 				+ selectedEvent.getPlace());
 			
 			title.setText(selectedEvent.getTitle());
 			description.setText(selectedEvent.getText());
 			ageLimit.setText("Aldersgrense: " + selectedEvent.getAgeLimit() + " r");
 			if(selectedEvent.isFree()){
 				price.setText("Gratis");
 			}
 			else{
 				String exception = "Exception: empty bundle!";
 				Toast t = Toast.makeText(getApplicationContext(), exception, Toast.LENGTH_LONG);
 			}
 		}
 	}
 	
 	public void populateFriendList() {
 		 friendList.clear();
 		 friendList.add("Audun");
 		 friendList.add("Kaare");
 		 friendList.add("Ole Christian");
 	 } 
 
 	public void showPopupWindow() {
 		friendListView = new ListView(this);
 		populateFriendList();
 		friendAdapter = new ArrayAdapter<String>(this, R.layout.friends_list, R.id.friendsListText, friendList);
 		friendListView.setAdapter(friendAdapter);
 		Dialog d = new Dialog(this);
 		friendListView.setBackgroundColor(Color.WHITE);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Deltagende venner");
 		builder.setView(friendListView);
 		d = builder.create();
 		d.show();
 	}
 
 	@Override
 	public void onClick(View v) {
 		showPopupWindow();
 	}
 }
