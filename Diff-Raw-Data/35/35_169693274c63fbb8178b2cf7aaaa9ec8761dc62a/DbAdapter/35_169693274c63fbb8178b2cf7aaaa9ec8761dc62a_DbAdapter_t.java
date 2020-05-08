 package clock.db;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import clock.sched.AlarmsManager.eCondition;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Address;
 import android.location.Geocoder;
 import android.util.Log;
 
 /**
  * Abstracts the work with the database.
  * Handles all queries, inserts and removes operations. 
  * @author Idan
  *
  */
 public class DbAdapter 
 {
 	// Database fields
 	private SQLiteDatabase database;
 	private Connection connection;
 	private String[] allColumns = { Connection.COLUMN_ID,
 									Connection.COLUMN_DATE,
 									Connection.COLUMN_LOCATION,
 									Connection.COLUMN_DETAILS,
 									Connection.COLUMN_ALARM,
 									Connection.COLUMN_NOTIFIED,
 									Connection.COLUMN_WAKEDUP};
 	
 	public DbAdapter(Context context) 
 	{
 		connection = new Connection(context);
 	}
 	
 	/**
 	 * Open the db connection.
 	 * Must be called before any other function.
 	 * @throws SQLException
 	 */
 	public void open() throws SQLException 
 	{
 //		database = connection.openDataBase();
 		database = connection.getWritableDatabase();
 	}
 
 	/**
 	 * Close the db connection.
 	 * Must be called after work.
 	 */
 	public void close() 
 	{
 		database.close();
 		connection.close();
 	}
 
 	/**
 	 * Inserts an event row to the Events table.
 	 * @param event - the event to be inserted.
 	 * @return the Event inserted.
 	 */
 	public Event insertEvent(Event event) 
 	{
 		ContentValues values = new ContentValues();
 		String dateSqlFormat = Event.getSqlTimeRepresent(event); 
 		values.put(Connection.COLUMN_DATE, dateSqlFormat);
 		values.put(Connection.COLUMN_LOCATION, event.getLocation());
 		values.put(Connection.COLUMN_DETAILS, event.getDetails());
 		values.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == false? 0 : 1);
 		values.put(Connection.COLUMN_NOTIFIED, event.isUserHasBeenNotified() == false? 0 : 1);
 		values.put(Connection.COLUMN_WAKEDUP, event.getUserHasBeenWakedUp());
 		long insertId = database.insert(Connection.TABLE_EVENTS, null, values);
 		Cursor cursor = database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_ID + " = " + insertId, null,
 										null, null, null);
 		
 		cursor.moveToFirst();
 		Event newEvent = cursorToEvent(cursor);
 		cursor.close();
 		return newEvent;
 	}
 
 	/**
 	 * Deletes an event using the Id.
 	 * @param event - the event to be deleted.
 	 */
 	public void deleteEvent(Event event) 
 	{
 		long id = event.getId();
		database.delete(Connection.TABLE_EVENTS, Connection.COLUMN_ID
				+ " = ?", new String[] { String.valueOf(event.getId()) });
 		Log.d("EVENT", "Event number " + id + ", Name: " + event.toString() + " has been deleted from db");
 	}
 	
 	/**
 	 * Returns a map of 'Day' => 'Events' associated with the month, year events.
 	 * @param month - requested month
 	 * @param year - requested year.
 	 * @return
 	 */
 	public HashMap<String, List<Event>> getEventsMapForMonth(int month, int year)
 	{
 		//TODO: quickly replace 31 to the const MAX_DAYS_IN_MONTH
 		HashMap<String, List<Event>> eventsMap = new HashMap<String, List<Event>>(31);
 		String yearMonth = String.valueOf(year) + "-";
 		yearMonth += month < 10 ? "0" + month : month;
 		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
 				"date <= '" + yearMonth + "-31 23-59-59'" + " AND " +
 				"date >= '" + yearMonth + "-00 00-00-00'", null); 
 				//database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_MONTH + "=" + month, 
 				//null, null, null, null);
 	
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) 
 		{
 			Event event = cursorToEvent(cursor);
 			List<Event> dayList = eventsMap.get(String.valueOf(event.getDay()));
 			if (dayList == null) // first event for day
 			{
 				dayList = new ArrayList<Event>();
 				eventsMap.put(String.valueOf(event.getDay()), dayList);
 			}
 			dayList.add(event);
 			cursor.moveToNext();
 		}
 		
 		return eventsMap;
 	}
 	
 	/**
 	 * get the next event from the database (where alarm field != PAST_EVENT).
 	 * returns null if no upcoming events.
 	 */
 	public Event getNextEvent()
 	{
 		Event retEvent = null;
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss");
         String currentTime = sdf.format(new Date());
         String query = "SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " + 
         		"date > '" + currentTime + "' order by date limit 1";
 		Cursor cursor = database.rawQuery(query , null);
 		Log.d("SQL", "NEXT EVENT SQL"  + query);
 		
 		cursor.moveToFirst();
 		if (!cursor.isAfterLast())
 		{
 			cursor.moveToFirst();
 			retEvent = cursorToEvent(cursor);
 		}
 		
 		Log.d("DB", "Next event Id: " + (retEvent == null ? "NONE" : retEvent.getId()));
 		return retEvent;
 	}	
 	
 	/**
 	 * returns a new event instance.
 	 * @param cursor - cursor to the row with the event.
 	 * @return
 	 */
 	private Event cursorToEvent(Cursor cursor) 
 	{
 		Event event = Event.createNewInstance();
 		event.setId(cursor.getLong(0));
 		event.setDateFromSql(cursor.getString(1));
 		event.setLocation(cursor.getString(2));
 		event.setDetails(cursor.getString(3));
 		event.setWithAlarmStatus(cursor.getInt(4) == 1);
 		event.setUserHasBeenNotified(cursor.getInt(5) == 1);
 		event.setUserHasBeenWakedUp(cursor.getLong(6));
 		return event;
 	}
 		
 	/**
 	 * Insert a record to the records table.
 	 * @param event - event to be save - only day name saved for now
 	 * @param arrangementTime - time took to arrange
 	 * @param enumCondition - weather condition enum
 	 * @param tempeture 
 	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
 	 */
 	public long addRecord(Event event, int arrangementTime, eCondition enumCondition, int tempeture) 
 	{
 		ContentValues values = new ContentValues(); 
 		values.put(Connection.COLUMN_ARR_TIME, arrangementTime);
 		values.put(Connection.COLUMN_DAY_OF_WEEK, event.getDayName());
 		values.put(Connection.COLUMN_WEATHER, enumCondition.toString());
 		values.put(Connection.COLUMN_TEMPETURE, String.valueOf(tempeture));
 		long insertId = database.insert(Connection.TABLE_RECORDS, null, values);
 		return insertId;
 	}
 	
 	/**
 	 * 
 	 * @param dayName
 	 * @param contidion
 	 * @param tempeture
 	 * @return arrangement time or -1 if no suggestion was found.
 	 */
 
 	public int getArrangmentTime(String dayName, eCondition contidion, int tempeture) 
 	{
 		int arrangeTime = -1;
 		String query = "SELECT (SUM(" + Connection.COLUMN_ARR_TIME + ")/COUNT(" + Connection.COLUMN_ARR_TIME + 
 				")) FROM " + Connection.TABLE_RECORDS + 
 				" WHERE " + Connection.COLUMN_WEATHER + "='" + contidion + 
 				"' AND " + Connection.COLUMN_TEMPETURE + ">" + (tempeture - 5) + 
 				" AND " + Connection.COLUMN_TEMPETURE + "<" + (tempeture + 5) + 
 				" GROUP BY " + Connection.COLUMN_WEATHER + ", " + Connection.COLUMN_TEMPETURE;
 		Log.d("ARRANGE", query);
 		Cursor cursor = database.rawQuery(query, null);
 	
 		cursor.moveToFirst();
 		if(!cursor.isAfterLast()) // did we get any results ? 
 		{
 			arrangeTime = cursor.getInt(0);			
 		}
 		else // no results, getting agg from all db.
 		{
 			query = "SELECT (SUM(" + Connection.COLUMN_ARR_TIME + ")/COUNT(" + Connection.COLUMN_ARR_TIME + 
 					")) FROM " + Connection.TABLE_RECORDS;
 			Log.d("ARRANGE", "Fall Back" + query);
 			cursor = database.rawQuery(query, null);
 			cursor.moveToFirst();
 			arrangeTime = cursor.getInt(0);
 		}
 		
 		return arrangeTime;
 	}
 
 	public Event getOneBefore(String sqlTimeRepresent)
 	{
 		return getOneBeforeOrAfter(sqlTimeRepresent, true);
 	}
 
 	public Event getOneAfter(String sqlTimeRepresent)
 	{
		Event oneAfter = getOneBeforeOrAfter(sqlTimeRepresent, false);
		Log.d("EVENT", "one after is: " + oneAfter.getId());
 		return getOneBeforeOrAfter(sqlTimeRepresent, false);
 	}
 	
 	/**
 	 * Update all changed fields of the event in the DB.
 	 * @param event - event to be updated in db.
 	 */
 	public void updateEvent(Event event)
 	{
 		ContentValues valuesMap = new ContentValues();
 		valuesMap.put(Connection.COLUMN_DATE, Event.getSqlTimeRepresent(event));
 		valuesMap.put(Connection.COLUMN_LOCATION, event.getLocation());
 		valuesMap.put(Connection.COLUMN_DETAILS, event.getDetails());
 		valuesMap.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == true ? 1 : 0);
 		valuesMap.put(Connection.COLUMN_NOTIFIED, event.getNotified() == true ? 1 : 0);
 		valuesMap.put(Connection.COLUMN_WAKEDUP, event.getWakedUp());
 
 		database.updateWithOnConflict(Connection.TABLE_EVENTS, valuesMap, Connection.COLUMN_ID + " = ?", new String[] { String.valueOf(event.getId()) }, SQLiteDatabase.CONFLICT_REPLACE);
 	}
 		
 	private Event getOneBeforeOrAfter(String sqlTimeRepresent, boolean isOneBefore) 
 	{
 		String compareSign, order;
 		if(isOneBefore)
 		{
 			compareSign = " < ";
 			order = " DESC ";
 		}
 		else
 		{
 			compareSign = " > ";
 			order = ""; // empty mean ascending.
 		}
 		String query = "SELECT * FROM " + Connection.TABLE_EVENTS + 
 						" WHERE " + Connection.COLUMN_DATE + compareSign + sqlTimeRepresent +
 						" ORDER BY " + Connection.COLUMN_DATE + " " + order + " LIMIT 1";
 		Log.d("SQL", query);
 		Cursor cursor = database.rawQuery(query, null);
 		Event oneBefore = null;
 		if(!cursor.isAfterLast())
 		{
 			oneBefore = cursorToEvent(cursor);
 		}
 		
 		return oneBefore;
 	}
 
 	
 	public Event getEventById(int eventId) 
 	{
 		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " + Connection.COLUMN_ID + " = " + eventId, null);
 		cursor.moveToFirst();
 		return cursorToEvent(cursor);
 	}
 	
 }
