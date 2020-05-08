 package com.monstersfromtheid.imready;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.ListActivity;
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
 import android.widget.Button;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.monstersfromtheid.imready.client.Meeting;
 import com.monstersfromtheid.imready.client.Participant;
 import com.monstersfromtheid.imready.client.ServerAPI;
 import com.monstersfromtheid.imready.client.ServerAPI.Action;
 import com.monstersfromtheid.imready.client.ServerAPICallFailedException;
 import com.monstersfromtheid.imready.client.User;
 import com.monstersfromtheid.imready.service.CheckMeetingsAlarmReceiver;
 
// TODO - Sit there staring at a meeting that just changed and the notification keeps getting reissued.
//        Leave app and return and it's happy.

 public class ViewMeeting extends ListActivity implements IMeetingChangeReceiver {
 	public static final int ACTIVITY_ADD_PARTICIPANT = 1;
     private ArrayList<HashMap<String, ?>> participants = new ArrayList<HashMap<String, ?>>();
     private String[] from = new String[] { "name", "readiness" };
     private int[] to = new int[] { R.id.meeting_participant_list_item_name,  
             R.id.meeting_participant_list_item_readiness };
     private SimpleAdapter adapter;
     private MeetingChangeReceiver receiver;
     private ScheduledThreadPoolExecutor serverChecker;
     Runnable pollServer;
     
     private ServerAPI api;
     private int meetingId;
     private String meetingName;
     String userID;
     private boolean myStatus = false;
     View setReadiness;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setReadiness = getLayoutInflater().inflate(R.layout.view_meeting_set_participant_status_button, null);
 
         Uri internalMeetingUri = getIntent().getData();
         if (!"content".equals(internalMeetingUri.getScheme())) { return; } // TODO Error handling
         if (!internalMeetingUri.getEncodedPath().startsWith("/meeting")) { return; } // TODO Error handling
 
         String meetingPath = internalMeetingUri.getEncodedPath();
         Pattern p = Pattern.compile("/meeting/(\\d+)/(.*)");
         Matcher m = p.matcher(meetingPath);
         if (!m.matches()) { 
         	setResult(RESULT_CANCELED);  // TODO Error handling
         	return;
         }
 
         String userNickName = IMReady.getNickName(this);
         userID = IMReady.getUserName(this);
         meetingId = Integer.parseInt(m.group(1));
         meetingName = Uri.decode(m.group(2));
         api = new ServerAPI(userID);
 
         Intent i = new Intent();
     	i.putExtra(IMReady.RETURNS_MEETING_ID, meetingId);
     	setResult(RESULT_OK, i);
 
         // Define an adapter to convert from our participants data set to a list
     	adapter = new SimpleAdapter(this, participants, R.layout.meeting_participant_list_item, from, to);
     	updateMeetingInfo(meetingName, meetingId);
         adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
             public boolean setViewValue(View view, Object data, String textRepresentation) {
                 if (view.getId() == R.id.meeting_participant_list_item_readiness) {
                     ((TextView)view).setTextColor((Boolean)data ? Color.GREEN : Color.RED);
                     ((TextView)view).setText((Boolean)data ? "Ready" : "Not ready");
                     return true;
                 }
                 return false;
             }
         });
 
         final TextView userIdText = (TextView) setReadiness.findViewById(R.id.meeting_participant_my_name);
         userIdText.setText(userNickName + "(" + userID + ")");
 		setMyStatus(myStatus);
 
         setReadiness.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		/* We can only set status to ready, so if we already are then we're done. */
 				if( myStatus ) {
 					return;
 				}
 
 				/* Set the status on the server */
 				ServerAPI.performInBackground(new Action<Void>() {
         			@Override
         			public Void action() throws ServerAPICallFailedException {
         				api.ready(meetingId, userID);
         				return null;
         			}
         			@Override
         			public void success(Void result) {
         				/* Set the status colour to green */
         				setMyStatus(true);
         				IMReady.setMyselfReady(meetingId, userID, ViewMeeting.this);
         			}
         			@Override
         			public void failure(ServerAPICallFailedException e) {
         				Toast.makeText(ViewMeeting.this, "Failed to set user status: " + e, Toast.LENGTH_LONG).show();
         			}
         		});
         	}
         });
         getListView().addHeaderView(setReadiness);
         
         final Button addParticipant = (Button)getLayoutInflater().inflate(R.layout.view_meeting_add_participant_button, null);
         addParticipant.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
             	Uri internalMeetingUri = Uri.parse("content://com.monstersfromtheid.imready/meeting/" + meetingId + "/" + Uri.encode(meetingName));
             	startActivityForResult( new Intent(Intent.ACTION_VIEW, internalMeetingUri, ViewMeeting.this, AddParticipant.class), ACTIVITY_ADD_PARTICIPANT);
         	}
         });
         getListView().addFooterView(addParticipant);
         
         // Populate the list with the latest we have recorded.
 		processMeetingsChange();
 
 		setListAdapter(adapter);
     }
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		// Register the broadcast receiver to catch change notifications
 		IntentFilter filter = new IntentFilter(ACTION_RESP);
 		filter.addCategory(Intent.CATEGORY_DEFAULT);
 		receiver = new MeetingChangeReceiver(this);
 		registerReceiver(receiver, filter);
 
 		// Start a scheduled job to poll the server.
 		Runnable pollServer = new Runnable() {
 			public void run() {
 				Intent broadcastIntent = new Intent(ViewMeeting.this, CheckMeetingsAlarmReceiver.class);
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
 		clearParticipants();
 		
 		ArrayList<Meeting> meetingList = IMReady.getMeetingState(this);
 		Iterator<Meeting> iter = meetingList.iterator();
 		Meeting thisMeeting;
 		while(iter.hasNext()){
 			thisMeeting = iter.next();
 			if(thisMeeting.getId() == meetingId){
 				updateMeetingInfo(thisMeeting.getName(), thisMeeting.getId());
 				try {
 					for (Participant newParticipant : thisMeeting.getParticipants() ) {
 						if(newParticipant.getUser().getId().compareTo(userID) != 0){
 							addParticipant(newParticipant);
 						} else {
 							setMyStatus(newParticipant.getState() == 1);
 						}
 					}
 				} catch (NullPointerException e) {
 					// Problem with the participant list
 				}
 				break;
 			}
 		}
 
 		adapter.notifyDataSetChanged();
 	}
     
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		switch (requestCode) {
 		case ACTIVITY_ADD_PARTICIPANT:
 			if(resultCode == RESULT_OK) {
 				int meetingId = data.getIntExtra(IMReady.RETURNS_MEETING_ID, -1);
 				if ( meetingId == -1){
 					return;
 				}
 				String userID = data.getStringExtra(IMReady.RETURNS_USER_ID);
 				if ( userID == null ){
 					return;
 				}
 				String userNickName = "...";
 
 				Participant participant = new Participant(new User(userID, userNickName), 0, false);
 				IMReady.addLocallyAddedParticipant(meetingId, participant, this);
 
 				processMeetingsChange();
 			}
 			break;
 			
 		default:
 			break;
 		}
 	}
 
     private void updateMeetingInfo(String meetingName, int meetingId) {
         setTitle("Meeting: " + meetingName + " (id=" + meetingId + ")");
     }
 
     private void clearParticipants() {
         participants.clear();
     }
 
     private void addParticipant(Participant participant) {
     	//String nick, String id, boolean readiness
         HashMap<String, Object> userItem = new HashMap<String, Object>();
         userItem.put("userId", participant.getUser().getId());
         userItem.put("name", participant.getUser().getDefaultNickname() + " (" + participant.getUser().getId() + ")");
         userItem.put("readiness", (participant.getState() == 1));
         participants.add(userItem);
     }
     
     private void setMyStatus(boolean status) {
     	myStatus = status;
     	
         TextView setStatus = (TextView) setReadiness.findViewById(R.id.view_meeting_set_participant_status_button);
         TextView readinessText = (TextView) setReadiness.findViewById(R.id.meeting_participant_my_readiness);
 
         setStatus.setText( myStatus ? R.string.view_meeting_set_participant_status_button_ready : R.string.view_meeting_set_participant_status_button_not_ready );
         readinessText.setTextColor( myStatus ? Color.GREEN : Color.RED );
         readinessText.setText( myStatus ? "Ready" : "Not ready" );
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu_pref, menu);
     	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_preferences:
         	// Open the preferences page
         	startActivity(new Intent( ViewMeeting.this, Preferences.class ));
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
