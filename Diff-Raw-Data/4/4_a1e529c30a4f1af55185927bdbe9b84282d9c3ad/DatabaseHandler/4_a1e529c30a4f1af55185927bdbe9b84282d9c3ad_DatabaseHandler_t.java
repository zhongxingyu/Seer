 package com.example.handsoncentralohio;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.*;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 	
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "handsOnDatabase";
 	
 	private static final String TABLE_EVENTS = "events";
 	
 	//column names
 	private static final String KEY_ID = "eventID";
 	private static final String KEY_START_DATE = "startDate";
 	private static final String KEY_NAME = "eventName";
 	private static final String KEY_DESCRIPTION = "description";
 	
 	private final ArrayList<EventData> event_list = new ArrayList<EventData>();
 	
 	public DatabaseHandler(Context context){
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	
 	// Create Tables
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
 				+ KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
 				+ KEY_START_DATE + " TEXT, " + KEY_DESCRIPTION + " TEXT)";
 		db.execSQL(CREATE_EVENTS_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
 		onCreate(db);
 	}
 	
 	public void Add_Event(EventData event) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(KEY_NAME, event.getName());
 		values.put(KEY_DESCRIPTION, event.getDescr());
 		
 		db.insert(TABLE_EVENTS, null, values);
 		db.close();
 	}
 	
 	public void Add_Event(String [] event) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(KEY_ID, Integer.parseInt(event[0]));
		values.put(KEY_NAME, event[2]);
		values.put(KEY_START_DATE, event[1]);
 		values.put(KEY_DESCRIPTION, event[3]);
 		
 		db.insert(TABLE_EVENTS, null, values);
 		db.close();
 	}
 	
 	//TODO: implement Get_Event
 	
 	public ArrayList<EventData> Get_Events() {
 		try {
 			event_list.clear();
 			
 			String selectQuery = "SELECT * FROM " + TABLE_EVENTS;
 			
 			SQLiteDatabase db = this.getWritableDatabase();
 			Cursor cursor = db.rawQuery(selectQuery, null);
 			
 			if(cursor.moveToLast()) {
 				do {
 					EventData event = new EventData();
 					event.setID(Integer.parseInt(cursor.getString(0)));
 					event.setDescr(cursor.getString(0));
 				} while(cursor.moveToNext());
 			}
 			
 			cursor.close();
 			db.close();
 		} catch (Exception e) {
 			Log.e("all_events", "" + e);
 		}
 		
 		return event_list;
 	}
 	
 	//TODO: update an event
 
 }
