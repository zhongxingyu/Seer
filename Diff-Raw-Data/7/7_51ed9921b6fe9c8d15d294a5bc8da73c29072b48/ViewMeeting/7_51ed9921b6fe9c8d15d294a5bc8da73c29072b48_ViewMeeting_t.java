 package com.realitycheckpoint.imready;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.ListActivity;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.realitycheckpoint.imready.client.API;
 import com.realitycheckpoint.imready.client.API.Action;
 import com.realitycheckpoint.imready.client.APICallFailedException;
 import com.realitycheckpoint.imready.client.Meeting;
 import com.realitycheckpoint.imready.client.Participant;
 
 public class ViewMeeting extends ListActivity {
     private ArrayList<HashMap<String, ?>> participants = new ArrayList<HashMap<String, ?>>();
     private String[] from = new String[] { "name", "readiness" };
     private int[] to = new int[] { R.id.meeting_participant_list_item_name,  
             R.id.meeting_participant_list_item_readiness };
    private SimpleAdapter adapter; 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Uri internalMeetingUri = getIntent().getData();
 
         if (!"content".equals(internalMeetingUri.getScheme())) { return; } // TODO Error handling
         if (!internalMeetingUri.getEncodedPath().startsWith("/meeting")) { return; } // TODO Error handling
 
         String meetingPath = internalMeetingUri.getEncodedPath();
         Pattern p = Pattern.compile("/meeting/(\\d+)/(.*)");
         Matcher m = p.matcher(meetingPath);
         if (!m.matches()) { return; } // TODO Error handling
 
         final int meetingId = Integer.parseInt(m.group(1));
         String meetingName = Uri.decode(m.group(2));
 
         String userNickName = getSharedPreferences(IMReady.PREFERENCES_NAME, MODE_PRIVATE).getString("accountNickName", "");
         String userName = getSharedPreferences(IMReady.PREFERENCES_NAME, MODE_PRIVATE).getString("accountUserName", "");
         
         final API api = new API(userName);
 
        adapter = new SimpleAdapter(this, participants, R.layout.meeting_participant_list_item, from, to);
         initialiseActivityFromLocalKnowledge(meetingName, meetingId, userNickName, userName);
         API.performInBackground(new Action<Meeting>() {
             @Override
             public Meeting action() throws APICallFailedException {
                 return api.meeting(meetingId);
             }
             @Override
             public void success(Meeting meeting) {
                 updateMeetingInfo(meeting.getName(), meeting.getId());
                 clearParticipants();
                 for (Participant participant : meeting.getParticipants()) {
                     addParticipant(
                             participant.getUser().getDefaultNickname(), 
                             participant.getUser().getId(), 
                             participant.getState() == Participant.STATE_READY
                             );
                 }
                 adapter.notifyDataSetChanged();
             }
             @Override
             public void failure(APICallFailedException e) {
                 Toast.makeText(ViewMeeting.this, "Failed: " + e, Toast.LENGTH_LONG).show();
             }
         });
 
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
         setListAdapter(adapter);
     }
 
     private void initialiseActivityFromLocalKnowledge(String meetingName, int meetingId, String creatorNick, String creatorUserId) {
         updateMeetingInfo(meetingName, meetingId);
         addParticipant(creatorNick, creatorUserId, false);
         adapter.notifyDataSetChanged();
     }
 
     private void updateMeetingInfo(String meetingName, int meetingId) {
         setTitle("Meeting: " + meetingName + " (id=" + meetingId + ")");
     }
 
     private void clearParticipants() {
         participants.clear();
     }
 
     private void addParticipant(String nick, String id, boolean readiness) {
         HashMap<String, Object> userItem = new HashMap<String, Object>();
        userItem.put("name", nick + " (" + id + ")");
         userItem.put("readiness", readiness);
         participants.add(userItem);
     }
 }
