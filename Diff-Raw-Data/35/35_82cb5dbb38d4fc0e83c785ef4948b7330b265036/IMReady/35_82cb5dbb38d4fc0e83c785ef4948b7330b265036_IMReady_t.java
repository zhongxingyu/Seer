 package com.monstersfromtheid.imready;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.SystemClock;
 
 import com.monstersfromtheid.imready.client.Meeting;
 import com.monstersfromtheid.imready.client.Participant;
 import com.monstersfromtheid.imready.client.User;
 import com.monstersfromtheid.imready.service.CheckMeetingsAlarmReceiver;
 
 public class IMReady {
 	public static final String PREFERENCES_NAME = "IMReadyPrefs";
 	
 	public static final String CLIENT_HTTP_NAME = "Android ImReady 0.1";
 	
 	public static final String SERVER_URI = "http://imready.monstersfromtheid.co.uk:54321/"; 
 	
 	public static final String ACTIONS_ACOUNT_CHANGE_DETAILS = "accountDetailsChange";
 	
 	public static final int VALUES_REFRESH_DELAY  = 1000;
 	public static final int VALUES_REFRESH_PERIOD = 15000;
 	
 	public static final int NOTIFICATION_ID = 1;
 	
 	public static final String RETURNS_MEETING_ID   = "MeetingID";
 	public static final String RETURNS_MEETING_NAME = "MeetingName";
 	public static final String RETURNS_USER_ID      = "UserID";
 	public static final String RETURNS_USER_NAME    = "UserName";
 
 	public static final String PREFERENCES_KEYS_ACCOUNT_DEFINED    = "accountDefined";
 	public static final String PREFERENCES_KEYS_USERNAME           = "accountUserName";
 	public static final String PREFERENCES_KEYS_NICKNAME           = "accountNickName";
 	public static final String PREFERENCES_KEYS_POL_INTERVAL       = "pollingInterval";
 	public static final String PREFERENCES_KEYS_NOTIFY_LEVEL       = "notificationLevel";
 	public static final String PREFERENCES_KEYS_MEETING_STATE      = "meetingState";
 
 	public static final boolean isAccountDefined(ContextWrapper c){
 		return c.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(PREFERENCES_KEYS_ACCOUNT_DEFINED, false);
 	}
 	
 	public static final void setAccountDefined(boolean defined, ContextWrapper c){
 		SharedPreferences.Editor preferences = c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
         preferences.putBoolean(PREFERENCES_KEYS_ACCOUNT_DEFINED, defined);
         preferences.commit();
 	}
 
 	public static final String getUserName(ContextWrapper c){
 		return c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCES_KEYS_USERNAME, "");
 	}
 	
 	public static final String getNickName(ContextWrapper c){
 		return c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCES_KEYS_NICKNAME, "");
 	}
 	
 	public static final void setUserAndNickName(String userName, String nickName, ContextWrapper c){
 		SharedPreferences.Editor preferences = c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
         preferences.putString(PREFERENCES_KEYS_USERNAME, userName);
         preferences.putString(PREFERENCES_KEYS_NICKNAME, nickName);
         preferences.commit();
 	}
 	
 	public static final int getPollingInterval(ContextWrapper c){
 		return c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(PREFERENCES_KEYS_POL_INTERVAL, 0);
 	}
 
 	public static final void setPollingInterval(int interval, ContextWrapper c){
 		SharedPreferences.Editor preferences = c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
         preferences.putInt(PREFERENCES_KEYS_POL_INTERVAL, interval);
         preferences.commit();
 	}
 
 	/**
 	 * Return the current level of notification:<br>
 	 * 
 	 * 0 - No notifications<br>
 	 * 1 - New &amp; ready meetings<br>
 	 * 2 - All meeting changes<br>
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static final int getNotificationLevel(ContextWrapper c){
 		return c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(PREFERENCES_KEYS_NOTIFY_LEVEL, 1);
 	}
 
 	/**
 	 * Set the level of notification:<br>
 	 * 
 	 * 0 - No notifications<br>
 	 * 1 - New &amp; ready meetings<br>
 	 * 2 - All meeting changes<br>
 	 * 
 	 * @param interval
 	 * @param c
 	 */
 	public static final void setNotificationLevel(int interval, ContextWrapper c){
 		SharedPreferences.Editor preferences = c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
         preferences.putInt(PREFERENCES_KEYS_NOTIFY_LEVEL, interval);
         preferences.commit();
 	}
 
 	/**
 	 * Take in a JSONArray representing the meeting list and store it away.
 	 * 
 	 * @param meetingList
 	 * @param c
 	 */
 	public static final void setMeetingState(JSONArray meetingList, ContextWrapper c){
 		SharedPreferences.Editor preferences = c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
         preferences.putString(PREFERENCES_KEYS_MEETING_STATE, meetingList.toString());
         preferences.commit();
 	}
 	
 	/**
 	 * Take in a list of Meeting objects and store it away.  It is internally stored as JSON
 	 * 
 	 * @param meetings
 	 * @param c
 	 */
 	public static final void setMeetingState(ArrayList<Meeting> meetings, ContextWrapper c){
 		JSONArray meetingList = toJSON(meetings);
 		setMeetingState(meetingList, c);
 	}
 	
 	/**
 	 * Return the stored list of Meeting objects
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static final ArrayList<Meeting> getMeetingState(ContextWrapper c){
 		try {
 			JSONArray meetingJSON = new JSONArray(c.getSharedPreferences(IMReady.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCES_KEYS_MEETING_STATE, ""));
 			return toMeetingList(meetingJSON);
 		} catch (JSONException e) {
 			return new ArrayList<Meeting>();
 		}
 	}
 	
 	/**
 	 * Take the list of meetings as a new set, and roll any changes in to the known meetings.  Meetings
 	 * that have been modified by the latest set have their modifications rolled in and our own markers
 	 * (for decorations etc) are suitably flagged.
 	 * 
 	 * @param latestMeetings
 	 * @param c
 	 * @return the merged list of Meeting objects.  This should be up to date.
 	 */
 	public static final synchronized ArrayList<Meeting> rollupMeetingLists(ArrayList<Meeting> latestMeetings, ContextWrapper c){
 		ArrayList<Meeting> currentMeetings = getMeetingState(c);
 		
 		// go through known list and check for their latest.
 		Iterator<Meeting> currentMeetingsIter = currentMeetings.iterator();
 		while(currentMeetingsIter.hasNext()){
 			Meeting thisCurrentMeeting = currentMeetingsIter.next();
 			boolean meetingIsDeleted = true;
 			
 			Iterator<Meeting> latestMeetingsIter = latestMeetings.iterator();
 			while(latestMeetingsIter.hasNext()){
 				Meeting thisLatestMeeting = latestMeetingsIter.next();
 				if(thisCurrentMeeting.getId() == thisLatestMeeting.getId()){
 					meetingIsDeleted = false;
 
 					if(thisCurrentMeeting.getName().compareTo(thisLatestMeeting.getName()) != 0){
 						thisCurrentMeeting.setDecorated(true);
 						thisCurrentMeeting.setChangedToUser(true);
 						thisCurrentMeeting.setName(thisLatestMeeting.getName());
 					}
 
 					if(thisCurrentMeeting.getState() != thisLatestMeeting.getState()){
 						// QUESTION - do we want to decorate a ready meeting?  We don't remove it.
 						thisCurrentMeeting.setDecorated(true);
 						thisCurrentMeeting.setChangedToUser(true);
 						thisCurrentMeeting.setState(thisLatestMeeting.getState());
 					}
 
 					if(compareParticipantLists(thisCurrentMeeting.getParticipants(),thisLatestMeeting.getParticipants())){
 						thisCurrentMeeting.setDecorated(true);
 						thisCurrentMeeting.setChangedToUser(true);
 					}
 					// NB - we always take the latest set of participant info; it will include any
 					// name changes and "notified" changes.  We don't flag those as interesting to us at the moment.
 					thisCurrentMeeting.setParticipants(thisLatestMeeting.getParticipants());
 
 					latestMeetingsIter.remove();
 					break;
 				}
 			}
 
 			if(meetingIsDeleted){
 				currentMeetingsIter.remove();
 			}
 		}
 		
 		// go through latest, and if new then add to list
 		Iterator<Meeting> latestMeetingsIter = latestMeetings.iterator();
 		while(latestMeetingsIter.hasNext()){
 			Meeting thisLatestMeeting = latestMeetingsIter.next();
 			currentMeetings.add(new Meeting(thisLatestMeeting.getId(), 
 					thisLatestMeeting.getName(), 
 					thisLatestMeeting.getState(), 
 					thisLatestMeeting.getParticipants(),
 					true,
 					true,
 					false));
 		}
 		
 		// Now current Meetings is up to date, push it back in
 		setMeetingState(currentMeetings, c);
 		return currentMeetings;
 	}
 	
 	/**
 	 * Add a new Meeting into the local data store.
 	 * 
 	 * @param meeting
 	 * @param c
 	 */
 	public static final synchronized void addLocallyCreatedMeeting(Meeting meeting, ContextWrapper c){
 		ArrayList<Meeting> currentMeetings = getMeetingState(c);
 		meeting.setDecorated(false);
 		meeting.setNewToUser(false);
 		meeting.setChangedToUser(false);
 		currentMeetings.add(meeting);
 		setMeetingState(currentMeetings, c);
 	}
 
 	public static final synchronized void addLocallyAddedParticipant(int meetingID, Participant participant, ContextWrapper c){
 		ArrayList<Meeting> currentMeetings = getMeetingState(c);
 
 		// Go through known list looking for this meeting so we can add the participant.
 		Iterator<Meeting> currentMeetingsIter = currentMeetings.iterator();
 		while(currentMeetingsIter.hasNext()){
 			Meeting thisCurrentMeeting = currentMeetingsIter.next();
 			if ( thisCurrentMeeting.getId() == meetingID ){
 				ArrayList<Participant> participants = thisCurrentMeeting.getParticipants();
 				participants.add(participant);
 			}
 		}
 
 		setMeetingState(currentMeetings, c);
 	}
 	
 	public static final synchronized void setMyselfReady(int meetingID, String userID, ContextWrapper c){
 		ArrayList<Meeting> currentMeetings = getMeetingState(c);
 		
 		// Go through known list looking for this meeting so we can add the participant.
 		Iterator<Meeting> currentMeetingsIter = currentMeetings.iterator();
		while (currentMeetingsIter.hasNext()) {
 			Meeting thisCurrentMeeting = currentMeetingsIter.next();
			if ( thisCurrentMeeting.getId() == meetingID ) {
 				ArrayList<Participant> participants = thisCurrentMeeting.getParticipants();
 				Iterator<Participant> participantsIter = participants.iterator();
 				boolean meetingReady = true;
				while (participantsIter.hasNext()) {
 					Participant thisParticipant = participantsIter.next();
					if (thisParticipant.getUser().getId().equals(userID)) {
						thisParticipant.setState(Participant.STATE_READY);
 					}
					if (thisParticipant.getState() != Participant.STATE_READY) {
 						meetingReady = false;
 					}
 				}
				if (meetingReady) {
					thisCurrentMeeting.setState(Meeting.STATE_READY);
 				}
				break;
 			}
 		}
 		
 		setMeetingState(currentMeetings, c);
 	}
 
 	/**
 	 * This returns true if the two lists of participants are different.  The comparison only conciders
 	 * the Participant id and state.
 	 * 
 	 * @param listOne
 	 * @param listTwo
 	 * @return
 	 */
 	private static boolean compareParticipantLists(List<Participant> listOne, List<Participant> listTwo){
 		// If they're dirrenet lengths then they are different
 		if(listOne.size() != listTwo.size()){
 			return true;
 		}
 
 		//  If every record in listOne is in listTwo, then they're the same.
 		boolean different = false;
 		Iterator<Participant> listOneIter = listOne.iterator();
 		while(listOneIter.hasNext()){
 			boolean foundMatch = false;
 			Participant thisListOneParticipant = listOneIter.next();
 			Iterator<Participant> listTwoIter = listTwo.iterator();
 			while(listTwoIter.hasNext()){
 				Participant thisListTwoParticipant = listTwoIter.next();
 				if( thisListOneParticipant.getUser().getId().compareTo(thisListTwoParticipant.getUser().getId()) == 0 &&
 				    thisListOneParticipant.getState() == thisListTwoParticipant.getState() ) {
 					foundMatch = true;
 					break;
 				}
 			}
 			if(!foundMatch){
 				different = true;
 			}
 		}
 		
 		return different;
 	}
 
 	/** 
 	 * Take a list of Meeting objects and convert it into a JSON representation.
 	 * 
 	 * @param meetings
 	 * @return
 	 */
 	public static final JSONArray toJSON(ArrayList<Meeting> meetings){
 		JSONArray meetingList = new JSONArray();
 		Iterator<Meeting> iter = meetings.iterator();
 		while(iter.hasNext()){
 			meetingList.put(iter.next().toJSON());
 		}
 		return meetingList;
 	}
 	
 	/**
 	 * Take a JSONArray representation of a list of Meeting objects and convert it into the corresponding 
 	 * List of Meeting objects.
 	 * 
 	 * @param meetingList
 	 * @return
 	 */
 	public static final ArrayList<Meeting> toMeetingList(JSONArray meetingList){
 		ArrayList<Meeting> meetings = new ArrayList<Meeting>();
 		try {
 			for (int i = 0; i < meetingList.length(); i++) {
 				ArrayList<Participant> participantList = new ArrayList<Participant>();
 				JSONObject meeting = meetingList.getJSONObject(i);
 				JSONArray participants = meeting.getJSONArray("participants");
 				
 				for (int j = 0; j < participants.length(); j++) {
 					JSONObject participant = participants.getJSONObject(j);
 					User u = new User(participant.getString("id"), participant.getString("defaultNickname"));
 					Participant p = new Participant(u, participant.getInt("state"), participant.getBoolean("notified"));
 					participantList.add(p);
 				}
 				
 				boolean decorated = true;
 				try {
 					decorated = meeting.getBoolean("decorated");
 				} catch (JSONException e){
 					// It's not there, so this is new to us.  Decorate it.
 				}
 
 				boolean newToUser = true;
 				try {
 					newToUser = meeting.getBoolean("newToUser");
 				} catch (JSONException e) {
 					// It's not there, so this is new to us.
 				}
 
 				boolean changedToUser = false;
 				try {
 					changedToUser = meeting.getBoolean("changedToUser");
 				} catch (JSONException e) {
 					// It's not there, so this is new to us.
 				}
 				
 				Meeting m = new Meeting(meeting.getInt("id"), 
 						meeting.getString("name"), 
 						meeting.getInt("state"), 
 						participantList, 
 						decorated, 
 						newToUser, 
 						changedToUser
 						);
 
 				meetings.add(m);
 			}
 		} catch (JSONException e) {
 			// TODO: handle exception
 		}
 		
 		return meetings;
 	}
 
 	/**
 	 * Mark an individual meeting as decorated (or not decorated).  This means that the UI can decide whether to mark the 
 	 * meeting as decorated
 	 */
 	public static final synchronized void markMeetingAsDecorated(int meetingId, boolean marked, ContextWrapper c){
 		ArrayList<Meeting> meetingList = getMeetingState(c);
 
 		Iterator<Meeting> iter = meetingList.iterator();
 		while(iter.hasNext()){
 			Meeting m = iter.next();
 			if(m.getId() == meetingId){
 				m.setDecorated(marked);
 			}
 		}
 
 		setMeetingState(meetingList, c);
 	}
 	
 	/**
 	 * Mark an individual meeting as new (or not new) to the user.  This means that notifications consider this 
 	 * in their "new" count.
 	 */
 	public static final synchronized void markMeetingAsNewToUser(int meetingId, boolean marked, ContextWrapper c){
 		ArrayList<Meeting> meetingList = getMeetingState(c);
 
 		Iterator<Meeting> iter = meetingList.iterator();
 		while(iter.hasNext()){
 			Meeting m = iter.next();
 			if(m.getId() == meetingId){
 				m.setNewToUser(marked);
 			}
 		}
 
 		setMeetingState(meetingList, c);
 	}
 	
 	/**
 	 * Mark an individual meeting as changed (or not changed) to the user.  This means that notifications consider 
 	 * this in their "changed" count.
 	 */
 	public static final synchronized void markMeetingAsChangedToUser(int meetingId, boolean marked, ContextWrapper c){
 		ArrayList<Meeting> meetingList = getMeetingState(c);
 
 		Iterator<Meeting> iter = meetingList.iterator();
 		while(iter.hasNext()){
 			Meeting m = iter.next();
 			if(m.getId() == meetingId){
 				m.setChangedToUser(marked);
 			}
 		}
 
 		setMeetingState(meetingList, c);
 	}
 
 	/**
 	 * Set the next alarm.  Use the current time, the polling interval settings and the 
 	 * notification level to work out when to set the alarm for, if at all. 
 	 * 
 	 * If the notification level is zero, then any existing alarm is cancelled.
 	 * 
 	 * If connected is true, then we set the interval to 20 seconds.
 	 * 
 	 * @param context
 	 * @param connected - Is an Activity running at the moment? 
 	 */
 	public static final void setNextAlarm(Context context){
 		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 	    Intent i = new Intent(context, CheckMeetingsAlarmReceiver.class);
 	    PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
 
 	    // If we're not connected and notifications are off, always turn off the alarm 
 	    if( getNotificationLevel(new ContextWrapper(context)) == 0 ){
 	    	alarm.cancel(pi);
 	    	return;
 	    }
 
 		// Should checks for whether to call go in here or out at the caller level?
 		//  are we in dynamic mode?
 	    // TODO - add the check to see if anything needs to be done.
 
 		long interval;
 		switch ( getPollingInterval(new ContextWrapper(context)) ) {
 		case 0:
 			interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
 			//interval = 10000;
 			break;
 
 		case 1:
 			interval = AlarmManager.INTERVAL_HOUR;
 			break;
 
 		case 2:
 			int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 			interval = ( 8 < currentHour && currentHour < 23 ) ? AlarmManager.INTERVAL_FIFTEEN_MINUTES : AlarmManager.INTERVAL_HOUR;
 			break;
 
 		default:
 			interval = AlarmManager.INTERVAL_HOUR;
 			break;
 		}
 		
 		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
 				SystemClock.elapsedRealtime()+1000,
 				interval,
 				pi);
 	
 	// TODO Ideally want to know if we just crossed a boundary and only fiddle the alarm then.
 	// Push this into another class and then all routes through code can just say setupAlarm.
 	
     //alarm.cancel(pi);
 	}
 
 }
 
