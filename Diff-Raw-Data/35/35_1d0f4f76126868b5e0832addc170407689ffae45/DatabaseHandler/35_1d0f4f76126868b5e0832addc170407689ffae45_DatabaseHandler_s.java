 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.database;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 /**
  * A class to help the creation and accessing of alarms in a database. Gives the
  * ability to create and delete alarms, and fetching one or all alarms.
  * 
  * @author Daniel Augurell
  * 
  */
 public class DatabaseHandler implements AlarmHandler {
 	private Context aCtx;
 
 	private DatabaseHelper aDbHelper;
 	private SQLiteDatabase aDb;
 
 	/**
 	 * Database SQL statements.
 	 */
 
 	private static final String DB_NAME = "data";
 	private static final String DB_TABLE = "alarms";
 
 	public static final String KEY_TIME = "time";
 	public static final String KEY_DAYSOFWEEK = "daysofweek";
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_ENABLED = "enabled";
 	public static final String KEY_MODULE = "module";
 	public static final String KEY_VOLUME = "volume";
 	public static final String KEY_VIBRATION = "vibration";
 	public static final String KEY_ALARMTONE = "alarmtone";
 
 	public static final String[] KEYS = {KEY_ROWID, KEY_TIME, KEY_DAYSOFWEEK,
 			KEY_ENABLED, KEY_MODULE, KEY_VOLUME, KEY_VIBRATION, KEY_ALARMTONE };
 
 	private static final String DB_CREATE = "CREATE TABLE " + DB_TABLE + " ("
 			+ KEY_ROWID + " INTEGER PRIMARY KEY , " + KEY_TIME + " DATETIME, "
 			+ KEY_DAYSOFWEEK + " INTEGER," + KEY_ENABLED + " BOOLEAN,"
			+ KEY_MODULE + " STRING," + KEY_VOLUME + " INTEGER," + KEY_VIBRATION + " BOOLEAN," + KEY_ALARMTONE + " STRING);";
 
 	private static final int DB_VERSION = 9;
 
 	/**
 	 * 
 	 * A help class to open or create the database.
 	 * 
 	 * @author Daniel Augurell
 	 * 
 	 */
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		/**
 		 * Constructor to the DatabaseHelper.
 		 * 
 		 * @param context
 		 *            The android context
 		 */
 		DatabaseHelper(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DB_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
 			onCreate(db);
 			Log.w("DATABASE", "The database is changed from version "
 					+ oldVersion + " to version " + newVersion);
 		}
 
 		@Override
 		public void onDowngrade(SQLiteDatabase db, int oldVersion,
 				int newVersion) {
 			onUpgrade(db, oldVersion, newVersion);
 		}
 	}
 
 	/**
 	 * Constructor for the DatabaseHandler.
 	 * 
 	 * @param ctx
 	 *            The android context
 	 */
 	public DatabaseHandler(Context ctx) {
 		aCtx = ctx;
 	}
 
 	@Override
 	public AlarmHandler openCon() {
 		if (aDbHelper != null) {
 			closeCon();
 		}
 
 		aDbHelper = new DatabaseHelper(aCtx);
 		aDb = aDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	@Override
 	public void closeCon() {
 		aDbHelper.close();
 	}
 
 	@Override
 	public long addAlarm(Alarm alarm) {
 		ContentValues alarmTime = new ContentValues();
 		alarmTime.putNull(KEY_ROWID);
 		alarmTime.put(KEY_DAYSOFWEEK, alarm.getDaysOfWeek());
 		alarmTime.put(KEY_ENABLED, alarm.isEnabled());
		alarmTime.put(KEY_TIME, alarm.getAlarmHours() + ":" + alarm.getAlarmMinutes());
 		alarmTime.put(KEY_MODULE, alarm.getModule());
 		alarmTime.put(KEY_VOLUME, alarm.getVolume());
 		alarmTime.put(KEY_VIBRATION, alarm.isVibrationEnabled());
 		alarmTime.put(KEY_ALARMTONE, alarm.getToneUri());
 		return aDb.insert(DB_TABLE, null, alarmTime);
 	}
 
 	@Override
 	public boolean deleteAlarm(int alarmID) {
 		return aDb.delete(DB_TABLE, KEY_ROWID + "=" + alarmID, null) > 0;
 	}
 
 	@Override
 	public Alarm fetchFirstEnabledAlarm() {
 		List<Alarm> list = fetchAllAlarms();
 		Collections.sort(list);
 		for (Alarm alarm : list) {
 			if (alarm.isEnabled()) {
 				return alarm;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Fetches all alarms as Cursor to the database.
 	 * 
 	 * @return Cursor with all the alarmdata
 	 */
 
 	private Cursor getAlarms() {
 		return aDb.query(true, DB_TABLE, KEYS, null, null, null, null, null,
 				null);
 	}
 
 	@Override
 	public Alarm fetchAlarm(int alarmID) {
 		Cursor cursor = aDb.query(true, DB_TABLE, KEYS, KEY_ROWID + "="
 				+ alarmID, null, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			return getAlarmFromCursor(cursor);
 		}
 		return null;
 	}
 
 	@Override
 	public int getNumberOfAlarms() {
 		return getAlarms().getCount();
 	}
 
 	@Override
 	public List<Alarm> fetchAllAlarms() {
 		Cursor cursor = getAlarms();
 		ArrayList<Alarm> list = new ArrayList<Alarm>();
 		if (cursor != null) {
 			if (cursor.moveToFirst()) {
 				do {
 					list.add(getAlarmFromCursor(cursor));
 				} while (cursor.moveToNext());
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Gets the alarm from the data at the cursors current position.
 	 * 
 	 * @param cursor
 	 *            The cursor set at the specified position.
 	 * @return An Alarm with the specified data.
 	 */
 
 	private Alarm getAlarmFromCursor(Cursor cursor) {
 		String[] time = cursor.getString(cursor.getColumnIndex(KEY_TIME))
 				.split(":");
 		Alarm a = new Alarm(Integer.parseInt(time[0]),
 				Integer.parseInt(time[1]), cursor.getInt(cursor
 						.getColumnIndex(KEY_ROWID)), cursor.getString(cursor
 						.getColumnIndex(KEY_MODULE)), cursor.getInt(cursor
 						.getColumnIndex(KEY_VOLUME)));
 		a.setEnabled(cursor.getInt(cursor.getColumnIndex(KEY_ENABLED)) > 0);
 		a.setDaysOfWeek(cursor.getInt(cursor.getColumnIndex(KEY_DAYSOFWEEK)));
		a.setVibrationEnabled(cursor.getInt(cursor.getColumnIndex(KEY_VIBRATION)) > 0);
 		a.setToneUri(cursor.getString(cursor.getColumnIndex(KEY_ALARMTONE)));
 		return a;
 	}
 
 	@Override
 	public boolean isEnabled(int id) {
 		Cursor cursor = aDb.query(true, DB_TABLE, KEYS, KEY_ROWID + "=" + id,
 				null, null, null, null, null);
 		return cursor.getInt(cursor.getColumnIndex(KEY_ENABLED)) > 0;
 	}
 
 	@Override
 	public boolean setAlarmEnabled(int id, boolean enable) {
 		ContentValues values = new ContentValues();
 		values.put(KEY_ENABLED, enable);
 		return aDb.update(DB_TABLE, values, KEY_ROWID + "=" + id, null) > 0;
 	}
 
 }
