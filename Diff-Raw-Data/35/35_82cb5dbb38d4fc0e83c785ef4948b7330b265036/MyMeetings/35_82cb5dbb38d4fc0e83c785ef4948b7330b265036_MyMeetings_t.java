 package com.monstersfromtheid.imready;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import android.app.ListActivity;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.monstersfromtheid.imready.client.Meeting;
 import com.monstersfromtheid.imready.client.Participant;
 import com.monstersfromtheid.imready.client.User;
 import com.monstersfromtheid.imready.service.CheckMeetingsAlarmReceiver;
 
 // TODO - Decoration on ready meetings?  Why?  We can't get rid of it.
 // TODO - I set myself to ready and I get a notification.
// TODO - Should meetings have a timestamp
 
 public class MyMeetings extends ListActivity implements IMeetingChangeReceiver {
 	public static final int ACTIVITY_CREATE_MEETING = 0;
 	public static final int ACTIVITY_VIEW_MEETING   = 1;
 
 	private ArrayList<HashMap<String, Object>> meetings = new ArrayList<HashMap<String, Object>>();
 	private String[] from = new String[] { "name", "readiness", "decorated" };
 	private int[] to = new int[] { R.id.meeting_list_item_name,  
 			R.id.meeting_list_item_readiness,
 			R.id.meeting_list_item_decoration};
 	private SimpleAdapter adapter; 
 	private Button createMeetingButton;
 	private MeetingChangeReceiver receiver;
 	private ScheduledThreadPoolExecutor serverChecker;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Define an adapter to convert from our meetings data set to a list
 		adapter = new SimpleAdapter(this, meetings, R.layout.meeting_list_item, from, to);
 		adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
 			public boolean setViewValue(View view, Object data, String textRepresentation) {
 				switch (view.getId()) {
 				case R.id.meeting_list_item_readiness:
 					((TextView)view).setTextColor((Boolean)data ? Color.GREEN : Color.RED);
 					((TextView)view).setText((Boolean)data ? "Ready" : "Not ready");
 					break;
 
 				case R.id.meeting_list_item_decoration:
 					if ((Boolean)data) {
 						((ImageView)view).setImageResource(R.drawable.decoration_change);
 					} else {
 						((ImageView)view).setImageResource(R.drawable.decoration_none);
 					}
 					break;
 
 				default:
 					return false;
 				}
 				return true;
 			}
 		});
 
 	   	// Set each list item to launch ViewMeeting
 		getListView().setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
 				HashMap<String, ?> info = meetings.get(position);
 				int meetingId = (Integer)info.get("id");
 				String name = (String)info.get("name");
 				Uri internalMeetingUri = Uri.parse("content://com.monstersfromtheid.imready/meeting/" + meetingId + "/" + Uri.encode(name));
 				startActivityForResult( new Intent(Intent.ACTION_VIEW, internalMeetingUri, MyMeetings.this, ViewMeeting.class), ACTIVITY_VIEW_MEETING );
 			}
 		});
 
 		// Add a button to the list footer for creating a meeting
 		createMeetingButton = (Button)getLayoutInflater().inflate(R.layout.meetings_create_meeting_button, null);
 		createMeetingButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivityForResult(new Intent(MyMeetings.this, CreateMeeting.class), ACTIVITY_CREATE_MEETING);
 			}
 		});
 	   	getListView().addFooterView(createMeetingButton);
 
 		// Populate the list with the latest we have recorded.
 		processMeetingsChange();
 
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		// Clear any notifications - decorations should cover any visual cue requirements
 		NotificationManager notMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		notMgr.cancel(IMReady.NOTIFICATION_ID);
 
 		// Register the broadcast receiver to catch change notifications
 		IntentFilter filter = new IntentFilter(IMeetingChangeReceiver.ACTION_RESP);
 		filter.addCategory(Intent.CATEGORY_DEFAULT);
 		receiver = new MeetingChangeReceiver(this);
 		registerReceiver(receiver, filter);
 
 		// Start a scheduled job to poll the server.
 		Runnable pollServer = new Runnable() {
 			public void run() {
 				Intent broadcastIntent = new Intent(MyMeetings.this, CheckMeetingsAlarmReceiver.class);
 				sendBroadcast(broadcastIntent);
 			}
 		};
 		serverChecker = new ScheduledThreadPoolExecutor(1);
 		serverChecker.scheduleWithFixedDelay(pollServer, 
 				IMReady.VALUES_REFRESH_DELAY, 
 				IMReady.VALUES_REFRESH_PERIOD, 
 				TimeUnit.MILLISECONDS);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 
 		// Cancel the schedule runner.
 		serverChecker.shutdownNow();
 
 		unregisterReceiver(receiver);
 		receiver = null;
 	}
 
 	// We've been notified of a change, so go get the latest information and handle it
 	public void processMeetingsChange() {
 		clearMeetings();
 		
 		for (Meeting newMeeting : IMReady.getMeetingState(this)) {
 			addMeeting(newMeeting);
 		}
 		adapter.notifyDataSetChanged();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		switch (requestCode) {
 		case ACTIVITY_CREATE_MEETING:
 
 			if(resultCode == RESULT_OK) {
 				int meetingId = data.getIntExtra(IMReady.RETURNS_MEETING_ID, -1);
 				if ( meetingId == -1){
 					return;
 				}
 				String meetingName = data.getStringExtra(IMReady.RETURNS_MEETING_NAME);
 				if ( meetingName == null ){
 					return;
 				}
 				Participant p = new Participant(new User(IMReady.getUserName(this), IMReady.getNickName(this)), 0, true);
 				ArrayList<Participant> pList = new ArrayList<Participant>();
 				pList.add(p);
 				Meeting meeting = new Meeting(meetingId, 
 						meetingName, 
 						0,
 						pList, 
 						false, 
 						false, 
 						false);
 				IMReady.addLocallyCreatedMeeting(meeting, this);
 				processMeetingsChange();
 
 				Uri internalMeetingUri = Uri.parse("content://com.monstersfromtheid.imready/meeting/" + meetingId + "/" + Uri.encode(meetingName));
				startActivityForResult( new Intent(Intent.ACTION_VIEW, internalMeetingUri, MyMeetings.this, ViewMeeting.class), ACTIVITY_VIEW_MEETING );
 			}
 			break;
 			
 		case ACTIVITY_VIEW_MEETING:
			if (resultCode == RESULT_OK) {
 				IMReady.markMeetingAsDecorated(data.getIntExtra(IMReady.RETURNS_MEETING_ID, 0), false, this);
 				processMeetingsChange();
 			}
 
 		default:
 			/* If returning after ACTIVITY_GOT_ACCOUNT then just exit this Activity */
 			break;
 		}
 	}
 
 	private void clearMeetings() {
 		meetings.clear();
 	}
 
 	private void addMeeting(Meeting meeting) {
 		HashMap<String, Object> userItem = new HashMap<String, Object>();
 		userItem.put("id", meeting.getId());
 		userItem.put("name", meeting.getName());
 		userItem.put("readiness", (meeting.getState() == 1 ));
 		userItem.put("decorated", meeting.isDecorated());
 		meetings.add(userItem);
 		if(meeting.isNewToUser()){
 			IMReady.markMeetingAsNewToUser(meeting.getId(), false, this);
 		}
 		if(meeting.isChangedToUser()){
 			IMReady.markMeetingAsChangedToUser(meeting.getId(), false, this);
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_account_pref, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_account:
 			// Open the accounts page
 			Uri resetAccountDetails = Uri.parse("content://com.monstersfromtheid.imready/util/" + IMReady.ACTIONS_ACOUNT_CHANGE_DETAILS);
 			startActivity(new Intent(Intent.ACTION_VIEW, resetAccountDetails, MyMeetings.this, DefineAccount.class));
 			return true;
 		case R.id.menu_preferences:
 			// Open the preferences page
 			startActivity(new Intent( MyMeetings.this, Preferences.class ));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
