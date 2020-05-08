 package com.uiproject.meetingplanner.database;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.uiproject.meetingplanner.MeetingInstance;
 import com.uiproject.meetingplanner.UserInstance;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MeetingPlannerDatabaseManager {
 
 	private Context context;
 	private int version;
 	private SQLiteDatabase db;
 	private MeetingPlannerDatabaseHelper dbHelper;
 	public final static String dbManagerTag = "MeetingPlannerDbManager";
 	
 	public MeetingPlannerDatabaseManager(Context context, int version){
 		this.context = context;
 		this.version = version;
 	}
 	
 	public MeetingPlannerDatabaseManager open() throws SQLException {
 		dbHelper = new MeetingPlannerDatabaseHelper(context, version);
 		db = dbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public void close(){
 		dbHelper.close();
 		db.close();
 	}
 	
 	
 	/*******************************************************************
 	 * 
 	 * All insert/update SQL statements start from here
 	 * 
 	 *******************************************************************/
 	
 	public void createMeeting(int meetingID, String meetingTitle, int meetingLat, int meetingLon,
 				String meetingDescription, String meetingAddress, String meetingDate, String meetingStartTime,
 				String meetingEndTime, int meetingTrackTime, int meetingInitiatorID, int meetingStartTimestamp){
 		
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.MEETING_ID, meetingID);
 		values.put(dbHelper.MEETING_TITLE, meetingTitle);
 		values.put(dbHelper.MEETING_LAT, meetingLat);
 		values.put(dbHelper.MEETING_LON, meetingLon);
 		values.put(dbHelper.MEETING_DESCRIPTION, meetingDescription);
 		values.put(dbHelper.MEETING_ADDRESS, meetingAddress);
 		values.put(dbHelper.MEETING_DATE, meetingDate.toString());
 		values.put(dbHelper.MEETING_STARTTIME, meetingStartTime);
 		values.put(dbHelper.MEETING_ENDTIME, meetingEndTime);
 		values.put(dbHelper.MEETING_TRACKTIME, meetingTrackTime);
 		values.put(dbHelper.MEETING_INITIATOR_ID, meetingInitiatorID);
 		values.put(dbHelper.MEETING_STARTTIMESTAMP, meetingStartTimestamp);
 		
 		try{
 			long l= db.insert(dbHelper.MEETING_TABLE, null, values);
 		
 			String s1 = "create meeting debug " + l;
 			//Toast.makeText(context, s1, Toast.LENGTH_SHORT).show(); //TODO
 			
 			Log.d(dbManagerTag, "createMeeting: meetingID = " + meetingID + ", meetingTitle = " + meetingTitle + 
 					", meetingDescription = " + meetingDescription + ", meetingInitiatorID = " + meetingInitiatorID + 
 					", meetingStartTime = " + meetingStartTime + ", meetingEndTime = " + meetingEndTime +
 					", meetingStartTimestamp = " + meetingStartTimestamp);
 		}
 		catch(Exception e)
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void updateMeeting(int meetingID, String meetingTitle, int meetingLat, int meetingLon,
 		String meetingDescription, String meetingAddress, String meetingDate, String meetingStartTime,
 		String meetingEndTime, int meetingTrackTime){
 		
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.MEETING_TITLE, meetingTitle);
 		values.put(dbHelper.MEETING_LAT, meetingLat);
 		values.put(dbHelper.MEETING_LON, meetingLon);
 		values.put(dbHelper.MEETING_DESCRIPTION, meetingDescription);
 		values.put(dbHelper.MEETING_ADDRESS, meetingAddress);
 		values.put(dbHelper.MEETING_DATE, meetingDate);
 		values.put(dbHelper.MEETING_STARTTIME, meetingStartTime);
 		values.put(dbHelper.MEETING_ENDTIME, meetingEndTime);
 		values.put(dbHelper.MEETING_TRACKTIME, meetingTrackTime);
 		
 		try {
 			db.update(dbHelper.MEETING_TABLE, values, dbHelper.MEETING_ID + "=" + meetingID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void deleteMeeting(int meetingID){
 		try {
 			db.delete(dbHelper.MEETING_TABLE, dbHelper.MEETING_ID + "=" + meetingID, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void deleteAllMeetings(){
 		try {
 			db.delete(dbHelper.MEETING_TABLE, null, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void createUser(int userID, String userFirstName, String userLastName, String userEmail, 
 							String userPhone, int userLocationLon, int userLocationLat){
 		
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.USER_ID, userID);
 		values.put(dbHelper.USER_FIRSTNAME, userFirstName);
 		values.put(dbHelper.USER_LASTNAME, userLastName);
 		values.put(dbHelper.USER_EMAIL, userEmail);
 		values.put(dbHelper.USER_PHONE, userPhone);
 		values.put(dbHelper.USER_LOCATIONLON, userLocationLon);
 		values.put(dbHelper.USER_LOCATIONLAT, userLocationLat);
 		
 		try{
 			db.insert(dbHelper.USER_TABLE, null, values);
 			Log.d(dbManagerTag, "createUser: userID=" + userID + ", userFirstName=" + userFirstName + 
 					", userLastName=" + userLastName + ", userEmail=" + userEmail + ", userPhone=" + userPhone +
 					", userLocationLon=" + userLocationLon + ", userLocationLat=" + userLocationLat);
 		}
 		catch(Exception e)
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateUserProfile(int userID, String userFirstName, String userLastName, String userEmail, 
 		String userPhone, int userLocationLon, int userLocationLat){
 		
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.USER_FIRSTNAME, userFirstName);
 		values.put(dbHelper.USER_LASTNAME, userLastName);
 		values.put(dbHelper.USER_EMAIL, userEmail);
 		values.put(dbHelper.USER_PHONE, userPhone);
 		
 		try {
 			db.update(dbHelper.USER_TABLE, values, dbHelper.USER_ID + "=" + userID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateUserLocation(int userID, int userLocationLon, int userLocationLat){
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.USER_LOCATIONLON, userLocationLon);
 		values.put(dbHelper.USER_LOCATIONLAT, userLocationLat);
 		
 		try {
 			db.update(dbHelper.USER_TABLE, values, dbHelper.USER_ID + "=" + userID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void deleteUser(int userID){
 		try {
 			db.delete(dbHelper.USER_TABLE, dbHelper.USER_ID + "=" + userID, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void deleteAllUsers(){
 		try {
 			db.delete(dbHelper.USER_TABLE, null, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void createMeetingUser(int meetingID, int userID, int attendingStatusID, String meetingUserEta){
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.MEETING_ID, meetingID);
 		values.put(dbHelper.USER_ID, userID);
 		values.put(dbHelper.ATTENDINGSTATUS_ID, attendingStatusID);
 		values.put(dbHelper.MEETINGUSER_ETA, meetingUserEta);
 		
 		try{
 			db.insert(dbHelper.MEETINGUSER_TABLE, null, values);
 			Log.d(dbManagerTag, "createMeetingUser: meetingID=" + meetingID + ", userID=" + userID + 
 					", attendingStatusID=" + attendingStatusID + ", meetingUserEta=" + meetingUserEta);
 		}
 		catch(Exception e)
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateMeetingUser(int meetingID, int userID, int attendingStatusID, String meetingUserEta){
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.ATTENDINGSTATUS_ID, attendingStatusID);
 		values.put(dbHelper.MEETINGUSER_ETA, meetingUserEta);
 		
 		try {
 			db.update(dbHelper.MEETINGUSER_TABLE, values, dbHelper.USER_ID + "=" + userID + " AND " + dbHelper.MEETING_ID + "=" + meetingID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateMeetingUserAttendingStatus(int meetingID, int userID, int attendingStatusID){
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.ATTENDINGSTATUS_ID, attendingStatusID);
 		
 		try {
 			db.update(dbHelper.MEETINGUSER_TABLE, values, dbHelper.USER_ID + "=" + userID + " AND " + dbHelper.MEETING_ID + "=" + meetingID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateMeetingUserEta(int meetingID, int userID, String meetingUserEta){
 		ContentValues values = new ContentValues();
 		values.put(dbHelper.MEETINGUSER_ETA, meetingUserEta);
 		
 		try {
 			db.update(dbHelper.MEETINGUSER_TABLE, values, dbHelper.USER_ID + "=" + userID + " AND " + dbHelper.MEETING_ID + "=" + meetingID, null);}
 		catch (Exception e)	{
 			Log.e("DB Error", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Delete 1 specific user for a meeting
 	 * @param meetingID
 	 * @param userID
 	 */
 	public void deleteMeetingUser(int meetingID, int userID){
 		try {
 			db.delete(dbHelper.MEETINGUSER_TABLE, dbHelper.USER_ID + "=" + userID + " AND " + dbHelper.MEETING_ID + " = " + meetingID, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Delete all users for a meeting
 	 * @param meetingID
 	 */
 	public void deleteMeetingUsers(int meetingID){
 		try {
 			db.delete(dbHelper.MEETINGUSER_TABLE, dbHelper.MEETING_ID + " = " + meetingID, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public void deleteAllMeetingUsers(){
 		try {
 			db.delete(dbHelper.MEETINGUSER_TABLE, null, null);
 		}catch (Exception e){
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 	}
 	/*******************************************************************
 	 * 
 	 * All select SQL statements start from here
 	 * 
 	 *******************************************************************/
 	public ArrayList<MeetingInstance> getAllMeetings(){
 		/*
 		 * meetingsArray - used for storing meeting info
 		 * cursor - used for storing query result 
 		 */
 		ArrayList<MeetingInstance> meetingsArray = new ArrayList<MeetingInstance>();
 		Cursor cursor;
 		
 		try{
 			// Do the query
 			cursor = db.query(
 					dbHelper.MEETING_TABLE,
 					new String[]{dbHelper.MEETING_ID, dbHelper.MEETING_LAT, dbHelper.MEETING_LON, dbHelper.MEETING_TITLE,
 							dbHelper.MEETING_DESCRIPTION, dbHelper.MEETING_ADDRESS, dbHelper.MEETING_DATE, dbHelper.MEETING_STARTTIME,
 							dbHelper.MEETING_ENDTIME, dbHelper.MEETING_TRACKTIME, dbHelper.MEETING_INITIATOR_ID},
 					null, null, null, null, null
 			);
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int meetingID = cursor.getInt(0);
 					int meetingLat = cursor.getInt(1);
 					int meetingLon = cursor.getInt(2);
 					String meetingTitle = cursor.getString(3);
 					String meetingDescription = cursor.getString(4);
 					String meetingAddress = cursor.getString(5);
 					String meetingDate = cursor.getString(6);
 					String meetingStartTime = cursor.getString(7);
 					String meetingEndTime = cursor.getString(8);
 					int meetingTrackTime = cursor.getInt(9);
 					int meetingInitiatorID = cursor.getInt(10);
 					
 					MeetingInstance m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 							meetingTitle, meetingDescription, meetingAddress, 
 							meetingDate, meetingStartTime, meetingEndTime, meetingTrackTime, meetingInitiatorID);
 					
 					meetingsArray.add(m);
 					
 					// Parse the date
 					/*Date meetingDate; //TODO
 					String meetingDateString = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.MEETING_DATE));
 					DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
 					
 					try {
 						meetingDate = iso8601Format.parse(meetingDateString);
 						
 						MeetingInstance m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 								meetingTitle, meetingDescription, meetingAddress, 
 								meetingDate,
 								meetingStartTime, meetingEndTime, meetingTrackTime);
 						
 						meetingsArray.add(m);
 						
 					} catch (ParseException e) {
 						Log.e("Parsing ISO8601 datetime failed", e.toString());
 					} */
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return meetingsArray;
 	}
 
 	public ArrayList<MeetingInstance> getAcceptedMeetings(int userID){
 		ArrayList<MeetingInstance> meetingsArray = new ArrayList<MeetingInstance>();
 		Cursor cursor;
 		
 		try{
 			
 			String query = "SELECT " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TITLE  + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LAT+ "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LON + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DESCRIPTION + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ADDRESS + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DATE + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ENDTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TRACKTIME + ","
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_INITIATOR_ID
 						+ " FROM " + dbHelper.MEETINGUSER_TABLE 
 						+ " LEFT JOIN " + dbHelper.MEETING_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=" + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID
 						+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=?"
 						+ " AND " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID + "=?"
 						+ " ORDER BY " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP + " DESC";
 			
 			Log.d(dbManagerTag, "getDeclinedMeetings query1: " + query);
 			
 			// Do the query
 			cursor = db.rawQuery(query, new String[]{String.valueOf(userID), String.valueOf(MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING)});
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int meetingID = cursor.getInt(0);
 					String meetingTitle = cursor.getString(1);
 					int meetingLat = cursor.getInt(2);
 					int meetingLon = cursor.getInt(3);
 					String meetingDescription = cursor.getString(4);
 					String meetingAddress = cursor.getString(5);
 					String meetingDate = cursor.getString(6);
 					String meetingStartTime = cursor.getString(7);
 					String meetingEndTime = cursor.getString(8);
 					int meetingTrackTime = cursor.getInt(9);
 					int meetingInitiatorID = cursor.getInt(10);
 
 					MeetingInstance m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 							meetingTitle, meetingDescription, meetingAddress, 
 							meetingDate, meetingStartTime, meetingEndTime, meetingTrackTime, meetingInitiatorID);
 					
 					// Do another query to get every meeting's invited users
 					HashMap<Integer, UserInstance> meetingUsersMap = getMeetingUsersMap(meetingID);
 					
 					// Add meeting users to the meeting
 					m.setMeetingAttendees(meetingUsersMap);
 					
 					// Add current meeting into meeting array
 					meetingsArray.add(m);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return meetingsArray;
 	}
 	
 	public ArrayList<MeetingInstance> getDeclinedMeetings(int userID){
 		ArrayList<MeetingInstance> meetingsArray = new ArrayList<MeetingInstance>();
 		Cursor cursor;
 		
 		try{
 			
 			String query = "SELECT " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TITLE  + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LAT+ "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LON + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DESCRIPTION + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ADDRESS + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DATE + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ENDTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TRACKTIME + ","
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_INITIATOR_ID
  				+ " FROM " + dbHelper.MEETINGUSER_TABLE 
 				+ " LEFT JOIN " + dbHelper.MEETING_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=" + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID
 				+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=?"
 				+ " AND " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID + "=?"
 				+ " ORDER BY " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP + " DESC";
 			
 			Log.d(dbManagerTag, "getDeclinedMeetings query1: " + query);
 			
 			// Do the query
 			cursor = db.rawQuery(query, new String[]{String.valueOf(userID), String.valueOf(MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLINING)});
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int meetingID = cursor.getInt(0);
 					String meetingTitle = cursor.getString(1);
 					int meetingLat = cursor.getInt(2);
 					int meetingLon = cursor.getInt(3);
 					String meetingDescription = cursor.getString(4);
 					String meetingAddress = cursor.getString(5);
 					String meetingDate = cursor.getString(6);
 					String meetingStartTime = cursor.getString(7);
 					String meetingEndTime = cursor.getString(8);
 					int meetingTrackTime = cursor.getInt(9);
 					int meetingInitiatorID = cursor.getInt(10);
 
 					MeetingInstance m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 							meetingTitle, meetingDescription, meetingAddress, 
 							meetingDate, meetingStartTime, meetingEndTime, meetingTrackTime, meetingInitiatorID);
 					
 					// Do another query to get every meeting's invited users
 					HashMap<Integer, UserInstance> meetingUsersMap = getMeetingUsersMap(meetingID);
 					
 					// Add meeting users to the meeting
 					m.setMeetingAttendees(meetingUsersMap);
 					
 					// Add current meeting into meeting array
 					meetingsArray.add(m);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		Log.d(dbManagerTag, "getDeclinedMeetings: meetingsArray size = " + meetingsArray.size());
 		return meetingsArray;
 	}
 	
 	//Done - need testing
 	public ArrayList<MeetingInstance> getPendingMeetings(int userID){
 		ArrayList<MeetingInstance> meetingsArray = new ArrayList<MeetingInstance>();
 		Cursor cursor;
 		
 		try{
 			
 			String query = "SELECT " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TITLE  + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LAT+ "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LON + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DESCRIPTION + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ADDRESS + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DATE + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ENDTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TRACKTIME + ","
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_INITIATOR_ID
 						+ " FROM " + dbHelper.MEETINGUSER_TABLE
 						+ " JOIN " + dbHelper.MEETING_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=" + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID
 						+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=?"
 						+ " AND " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID + "=?"
 						+ " ORDER BY " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP + " DESC";
 			
 			Log.d(dbManagerTag, "getPendingMeetings query1: " + query);
 			
 			// Do the query
 			//cursor = db.rawQuery(query, null);
 			cursor = db.rawQuery(query, new String[]{String.valueOf(userID), String.valueOf(MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDING)});
 			
 			Log.d(dbManagerTag, "getPendingMeetings cursor row count= " + cursor.getCount());
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int meetingID = cursor.getInt(0);
 					String meetingTitle = cursor.getString(1);
 					int meetingLat = cursor.getInt(2);
 					int meetingLon = cursor.getInt(3);
 					String meetingDescription = cursor.getString(4);
 					String meetingAddress = cursor.getString(5);
 					String meetingDate = cursor.getString(6);
 					String meetingStartTime = cursor.getString(7);
 					String meetingEndTime = cursor.getString(8);
 					int meetingTrackTime = cursor.getInt(9);
 					int meetingInitiatorID = cursor.getInt(10);
 
 					MeetingInstance m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 							meetingTitle, meetingDescription, meetingAddress, 
 							meetingDate, meetingStartTime, meetingEndTime, meetingTrackTime, meetingInitiatorID);
 					
 					// Do another query to get every meeting's invited users
 					HashMap<Integer, UserInstance> meetingUsersMap = getMeetingUsersMap(meetingID);
 					
 					// Add meeting users to the meeting
 					m.setMeetingAttendees(meetingUsersMap);
 					
 					// Add current meeting into meeting array
 					meetingsArray.add(m);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		Log.d(dbManagerTag, "getPendingMeetings: meetingsArray size = " + meetingsArray.size());
 		return meetingsArray;
 	}
 	
 	/**
 	 * Get the next upcoming meeting
 	 * @author Tina Chen
 	 * @param userID
 	 * @return
 	 */
 	public MeetingInstance getNextUpcomingMeeting(int userID){
 		
 		MeetingInstance m = new MeetingInstance();
 		Cursor cursor;
 		
 		try{
 			// Get current unix timestamp
 			long currentUnixTime = System.currentTimeMillis() / 1000L;
 			
 			String query = "SELECT " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TITLE  + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LAT+ "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_LON + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DESCRIPTION + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ADDRESS + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_DATE + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ENDTIME + "," 
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_TRACKTIME + ","
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_INITIATOR_ID + ","
 									+ dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP
 						+ " FROM " + dbHelper.MEETINGUSER_TABLE 
 						+ " LEFT JOIN " + dbHelper.MEETING_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=" + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_ID
 						+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=?"
 						+ " AND " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP + ">" + (int)currentUnixTime 
 						+ " ORDER BY " + dbHelper.MEETING_TABLE + "." + dbHelper.MEETING_STARTTIMESTAMP + " ASC";
 			
 			Log.d(dbManagerTag, "getNextUpcomingMeeting query: " + query);
 			
 			// Do the query
 			cursor = db.rawQuery(query, new String[]{String.valueOf(userID)});
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int meetingID = cursor.getInt(0);
 					String meetingTitle = cursor.getString(1);
 					int meetingLat = cursor.getInt(2);
 					int meetingLon = cursor.getInt(3);
 					String meetingDescription = cursor.getString(4);
 					String meetingAddress = cursor.getString(5);
 					String meetingDate = cursor.getString(6);
 					String meetingStartTime = cursor.getString(7);
 					String meetingEndTime = cursor.getString(8);
 					int meetingTrackTime = cursor.getInt(9);
 					int meetingInitiatorID = cursor.getInt(10);
 					int meetingStarttimestamp = cursor.getInt(11);
 
 					Log.d(dbManagerTag, "getNextUpcomingMeeting result: " + "meetingID = " + meetingID 
 																	+ ", meetingTitle = " + meetingTitle
 																	+ ", meetingDate = " + meetingDate
 																	+ ", meetingStartTime = " + meetingStartTime
 																	+ ", meetingEndTime = " + meetingEndTime
 																	+ ", meetingStarttimestamp = " + meetingStarttimestamp);
 					
 					m = new MeetingInstance(meetingID, meetingLat, meetingLon, 
 							meetingTitle, meetingDescription, meetingAddress, 
 							meetingDate, meetingStartTime, meetingEndTime, meetingTrackTime, meetingInitiatorID);
 					
 					break;
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 			
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return m;
 	}
 	
 	public HashMap<Integer, UserInstance> getMeetingUsersMap(int meetingID){
 		
 		/*
 		 * meetingUsersArray - used for storing meeting's users info
 		 * cursor - used for storing query result 
 		 */
 		HashMap<Integer, UserInstance> meetingUsersMap = new HashMap<Integer, UserInstance>();
 		Cursor cursor;
 		
 		try{
 			
 			//String MY_QUERY = "SELECT * FROM table_a a INNER JOIN table_b b ON a.id=b.other_id WHERE b.property_id=?";
 			String query = "SELECT " + dbHelper.USER_TABLE + "." + dbHelper.USER_ID + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_FIRSTNAME + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LASTNAME+ "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_EMAIL + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_PHONE + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LOCATIONLON + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LOCATIONLAT + "," 
 									+ dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETINGUSER_ETA + "," 
 									+ dbHelper.ATTENDINGSTATUS_TABLE + "." + dbHelper.ATTENDINGSTATUS_NAME 
 				+ " FROM " + dbHelper.MEETINGUSER_TABLE 
 				+ " LEFT JOIN " + dbHelper.USER_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=" + dbHelper.USER_TABLE + "." + dbHelper.USER_ID
 				+ " LEFT JOIN " + dbHelper.ATTENDINGSTATUS_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID + "=" + dbHelper.ATTENDINGSTATUS_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID
 				+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=?"
 				+ " ORDER BY " + dbHelper.USER_TABLE + "." + dbHelper.USER_LASTNAME + " ASC";
 			
 			// Do the query
 			cursor = db.rawQuery(query, new String[]{String.valueOf(meetingID)});
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int userID = cursor.getInt(0);
 					String userFirstName = cursor.getString(1);
 					String userLastName = cursor.getString(2);
 					String userEmail = cursor.getString(3);
 					String userPhone = cursor.getString(4);
 					int userLocationLon = cursor.getInt(5);
 					int userLocationLat = cursor.getInt(6);
 					String userEta = cursor.getString(7);
 					String userAttendingStatus = cursor.getString(8);
 					
 					UserInstance u = new UserInstance(userID, userFirstName, userLastName, 
 														userEmail, userPhone, userLocationLon, 
 														userLocationLat, userEta, userAttendingStatus);
 					
 					meetingUsersMap.put(userID, u);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return meetingUsersMap;
 	}
 	
 	public ArrayList<UserInstance> getMeetingUsersArray(int meetingID){
 		
 		/*
 		 * meetingUsersArray - used for storing meeting's users info
 		 * cursor - used for storing query result 
 		 */
 		ArrayList<UserInstance> meetingUsersArray = new ArrayList<UserInstance>();
 		Cursor cursor;
 		
 		try{
 			
 			//String MY_QUERY = "SELECT * FROM table_a a INNER JOIN table_b b ON a.id=b.other_id WHERE b.property_id=?";
 			String query = "SELECT " + dbHelper.USER_TABLE + "." + dbHelper.USER_ID + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_FIRSTNAME  + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LASTNAME+ "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_EMAIL + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_PHONE + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LOCATIONLON + "," 
 									+ dbHelper.USER_TABLE + "." + dbHelper.USER_LOCATIONLAT + ","
 									+ dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETINGUSER_ETA + "," 
 									+ dbHelper.ATTENDINGSTATUS_TABLE + "." + dbHelper.ATTENDINGSTATUS_NAME 
 						+ " FROM " + dbHelper.MEETINGUSER_TABLE 
 						+ " LEFT JOIN " + dbHelper.USER_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.USER_ID + "=" + dbHelper.USER_TABLE + "." + dbHelper.USER_ID
 						+ " LEFT JOIN " + dbHelper.ATTENDINGSTATUS_TABLE + " ON " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID + "=" + dbHelper.ATTENDINGSTATUS_TABLE + "." + dbHelper.ATTENDINGSTATUS_ID
 						+ " WHERE " + dbHelper.MEETINGUSER_TABLE + "." + dbHelper.MEETING_ID + "=?"
 						+ " ORDER BY " + dbHelper.USER_TABLE + "." + dbHelper.USER_LASTNAME + " ASC";
 			
 			// Do the query
 			cursor = db.rawQuery(query, new String[]{String.valueOf(meetingID)});
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int userID = cursor.getInt(0);
 					String userFirstName = cursor.getString(1);
 					String userLastName = cursor.getString(2);
 					String userEmail = cursor.getString(3);
 					String userPhone = cursor.getString(4);
 					int userLocationLon = cursor.getInt(5);
 					int userLocationLat = cursor.getInt(6);
 					String userEta = cursor.getString(7);
 					String userAttendingStatus = cursor.getString(8);
 					
 					UserInstance u = new UserInstance(userID, userFirstName, userLastName, 
 														userEmail, userPhone, userLocationLon, 
 														userLocationLat, userEta, userAttendingStatus);
 					
 					meetingUsersArray.add(u);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		Log.d(dbManagerTag, "getMeetingUsersArray array size = " + meetingUsersArray.size());
 		return meetingUsersArray;
 	}
 	
 	public MeetingInstance getMeeting(int meetingID){
 		Cursor cursor;
 		MeetingInstance meeting = new MeetingInstance();
 		meeting.setMeetingID(meetingID);
 		
 		try{
 			
 			// Do the query
 			cursor = db.query(
 					dbHelper.MEETING_TABLE,
 					new String[]{dbHelper.MEETING_TITLE, dbHelper.MEETING_LAT, dbHelper.MEETING_LON, dbHelper.MEETING_DESCRIPTION,
 							dbHelper.MEETING_ADDRESS, dbHelper.MEETING_DATE, dbHelper.MEETING_STARTTIME, dbHelper.MEETING_ENDTIME,
 							dbHelper.MEETING_TRACKTIME, dbHelper.MEETING_INITIATOR_ID},
 					dbHelper.MEETING_ID + "=?", new String[]{Integer.toString(meetingID)}, null, null, null
 			);
 			
 			Log.d(dbManagerTag, "getMeeting");
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{	
 					String meetingTitle = cursor.getString(0);
 					int meetingLat = cursor.getInt(1);
 					int meetingLon = cursor.getInt(2);
 					String meetingDescription = cursor.getString(3);
 					String meetingAddress = cursor.getString(4);
 					String meetingDate = cursor.getString(5);
 					String meetingStartTime = cursor.getString(6);
 					String meetingEndTime = cursor.getString(7);
 					int meetingTrackTime = cursor.getInt(8);
 					int meetingInitiatorID = cursor.getInt(9);
 					
 					meeting.setMeetingTitle(meetingTitle);
 					meeting.setMeetingLat(meetingLat);
 					meeting.setMeetingLon(meetingLon);
 					meeting.setMeetingDescription(meetingDescription);
 					meeting.setMeetingAddress(meetingAddress);
 					meeting.setMeetingDate(meetingDate);
 					meeting.setMeetingStartTime(meetingStartTime);
 					meeting.setMeetingEndTime(meetingEndTime);
 					meeting.setMeetingTrackTime(meetingTrackTime);
 					meeting.setMeetingInitiatorID(meetingInitiatorID);
 					
 				}
 				
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return meeting;
 	}
 	
 	public ArrayList<UserInstance> getAllUsers(){
 		
 		/*
 		 * meetingUsersArray - used for storing meeting's users info
 		 * cursor - used for storing query result 
 		 */
 		ArrayList<UserInstance> usersArray = new ArrayList<UserInstance>();
 		Cursor cursor;
 		
 		try{	
 			
 			// Do the query
 			cursor = db.query(
 					dbHelper.USER_TABLE,
 					new String[]{dbHelper.USER_ID, dbHelper.USER_FIRSTNAME, dbHelper.USER_LASTNAME, dbHelper.USER_EMAIL, dbHelper.USER_PHONE,
 							dbHelper.USER_LOCATIONLAT, dbHelper.USER_LOCATIONLON},
 					null, null, null, null, dbHelper.USER_LASTNAME + " ASC"
 			);
 			
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{
 					int userID = cursor.getInt(0);
 					String userFirstName = cursor.getString(1);
 					String userLastName = cursor.getString(2);
 					String userEmail = cursor.getString(3);
 					String userPhone = cursor.getString(4);
 					int userLocationLon = cursor.getInt(5);
 					int userLocationLat = cursor.getInt(6);
 					
 					Log.d(dbManagerTag, "getAllUsers: " + " userID = " + userID
 														+ ", userFirstName = " + userFirstName
 														+ ", userLastName = " + userLastName
 														+ ", userEmail = " + userEmail
 														+ ", userPhone = " + userPhone
 														+ ", userLocationLon = " + userLocationLat);
 					/*String userAttendingStatusName = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDINGSTRING;;
 					switch (userAttendingStatusId){
 						case MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING:
 							userAttendingStatusName = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDINGSTRING;
 							break;
 						case MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLINING:
 							userAttendingStatusName = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLININGSTRING;
 							break;
 						case MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDING:
 							userAttendingStatusName = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDINGSTRING;
 							break;
 					}*/
 					
 					UserInstance u = new UserInstance(userID, userFirstName, userLastName, 
 														userEmail, userPhone, userLocationLon, 
 														userLocationLat);
 					
 					usersArray.add(u);
 					
 				}
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 
 		Log.d(dbManagerTag, "getAllUsers user size: " + usersArray.size());
 		return usersArray;
 	}
 	
 	public UserInstance getUser(int userID){
 		Cursor cursor;
 		UserInstance user = new UserInstance(userID);
 
 		try{
 			
 			// Do the query
 			cursor = db.query(
 					dbHelper.USER_TABLE,
 					new String[]{dbHelper.USER_FIRSTNAME, dbHelper.USER_LASTNAME, dbHelper.USER_EMAIL, dbHelper.USER_PHONE,
 							dbHelper.USER_LOCATIONLAT, dbHelper.USER_LOCATIONLON},
 					dbHelper.USER_ID + "=?", new String[]{Integer.toString(userID)}, null, null, null
 			);
 			
 			Log.d(dbManagerTag, "getUser");
 			
 			// move the cursor's pointer to position zero.
 			cursor.moveToFirst();
  
 			// if there is data after the current cursor position, add it
 			// to the ArrayList.
 			if (!cursor.isAfterLast())
 			{
 				do
 				{	
 					String userFirstName = cursor.getString(0);
 					String userLastName = cursor.getString(1);
 					String userEmail = cursor.getString(2);
 					String userPhone = cursor.getString(3);
 					int userLocationLat = cursor.getInt(4);
 					int userLocationLon = cursor.getInt(5);
 					
 					user.setUserFirstName(userFirstName);
 					user.setUserLastName(userLastName);
 					user.setUserEmail(userEmail);
 					user.setUserPhone(userPhone);
 					user.setUserLocationLat(userLocationLat);
 					user.setUserLocationLon(userLocationLon);
 					
 				}
 				
 				// move the cursor's pointer up one position.
 				while (cursor.moveToNext());
 			}
 			
 			cursor.close();
 		}catch(SQLException e) 
 		{
 			Log.e("DB ERROR", e.toString());
 			e.printStackTrace();
 		}
 		
 		return user;
 	}
 }
