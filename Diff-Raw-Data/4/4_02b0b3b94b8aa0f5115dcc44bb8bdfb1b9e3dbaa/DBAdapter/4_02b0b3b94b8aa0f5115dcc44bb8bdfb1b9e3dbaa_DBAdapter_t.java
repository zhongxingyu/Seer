 package com.fernferret.android.fortywinks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;

 /**
 * Database adapter that allows easy communication with the SQLite DB with handling of injection attacks.
  * @author Jimmy Theis
  *
  */
 public class DBAdapter extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 2;
     private static final String DATABASE_NAME = "fortywinks";
     
     private SQLiteDatabase mDb;
 
     public DBAdapter(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mDb = getWritableDatabase();
     }
     /**
      * Save the given alarm to the Database.  If it does not have an ID, it will be given one, otherwise, its values will override the previous values, effectively giving this query add/edit privilages.
      * @param alarm The alarm to add to the database.
      */
     public void saveAlarm(Alarm alarm) {
         mDb.delete("alarms", "id = ?", new String[] {alarm.getId() + ""});
         SQLiteStatement s = mDb.compileStatement("INSERT INTO alarms (hour, minute, threshold, days_of_week, followups, interval_start, interval_end, enabled) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
         s.bindString(1, alarm.getHour() + "");
         s.bindString(2, alarm.getMinute() + "");
         s.bindString(3, alarm.getThreshold() + "");
         s.bindString(4, alarm.getDaysOfWeek() + "");
         s.bindString(5, alarm.getFollowups() + "");
         s.bindString(6, alarm.getIntervalStart() + "");
         s.bindString(7, alarm.getIntervalEnd() + "");
         s.bindString(8, (alarm.getEnabled() ? 1 : 0) + "");
         s.executeInsert();
     }
 
     /**
      * Get a List of all the Alarms in the database.
      * @return a List of all the Alarms in ArrayList form.
      */
     public List<Alarm> listAlarms() {
         List<Alarm> result = new ArrayList<Alarm>();
         String query = "SELECT * FROM alarms";
         Cursor cursor = mDb.rawQuery(query, null);
         
         if (cursor.moveToFirst()) {
             do {
                 
                 /* Create and populate each alarm */
                 Alarm next = new Alarm(cursor.getInt(0));
                 next.setHour(cursor.getInt(1));
                 next.setMinute(cursor.getInt(2));
                 next.setThreshold(cursor.getInt(3));
                 next.setDaysOfWeek(cursor.getInt(4));
                 next.setFollowups(cursor.getInt(5));
                 next.setIntervalStart(cursor.getInt(6));
                 next.setIntervalEnd(cursor.getInt(7));
                 next.setEnabled(cursor.getInt(8) == 1);
                 result.add(next);
                 
             } while (cursor.moveToNext());
         }
         
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         return result;
     }
     
     /**
      * Returns the most recently set PowerNap.
      * @return the most recent PowerNap.
      */
     public Alarm getPowerNap() {
         Alarm result;
         String query = "SELECT * FROM alarms WHERE days_of_week = 0 ORDER BY id DESC";
         Cursor cursor = mDb.rawQuery(query, null);
         
         if (cursor.moveToFirst()) {
             
             /* Populate alarm object */
             result = new Alarm(cursor.getInt(0));
             result.setHour(cursor.getInt(1));
             result.setMinute(cursor.getInt(2));
             result.setThreshold(cursor.getInt(3));
             result.setDaysOfWeek(cursor.getInt(4));
             result.setFollowups(cursor.getInt(5));
             result.setIntervalStart(cursor.getInt(6));
             result.setIntervalEnd(cursor.getInt(7));
             result.setEnabled(cursor.getInt(8) == 1);
             
         } else {
             
             /* We didn't find that one */
             result = null;
         }
         
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         return result;
     }
     
     /**
      * Retrieves an alarm from the database with the given ID.
      * @param id The ID of the alarm to retrieve
      * @return An alarm with all quantities specified
      */
     public Alarm getAlarm(int id) {
         Alarm result = new Alarm(id);
         String query = "SELECT * FROM alarms WHERE id = ?";
         Cursor cursor = mDb.rawQuery(query, new String[] { id + "" });
         
         if (cursor.moveToFirst()) {
             
             /* Populate alarm object */
             result.setHour(cursor.getInt(1));
             result.setMinute(cursor.getInt(2));
             result.setThreshold(cursor.getInt(3));
             result.setDaysOfWeek(cursor.getInt(4));
             result.setFollowups(cursor.getInt(5));
             result.setIntervalStart(cursor.getInt(6));
             result.setIntervalEnd(cursor.getInt(7));
             result.setEnabled(cursor.getInt(8) == 1);
             
         } else {
             
             /* We didn't find that one */
             result = null;
         }
         
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         return result;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER, minute INTEGER, threshold INTEGER, days_of_week INTEGER, followups INTEGER, interval_start INTEGER, interval_end INTEGER, enabled INTEGER);");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE IF EXISTS alarms");
         onCreate(db);
     }
 
 }
