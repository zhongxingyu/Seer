 package com.feedme.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import com.feedme.database.JournalColumn;
 import com.feedme.model.Journal;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * This DAO class handles all CRUD operations on the Entries table in the SQLLite database.
  */
 public class JournalDao {
 
     private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     private static final SimpleDateFormat ANDROID_TIME = new SimpleDateFormat("M-d-yyyy:hh:mm:ss");
 
     // Contacts table name
     private static final String TABLE_DATA = JournalColumn.TABLE_NAME;
 
     // Contacts Table Columns names
     private static final String KEY_ID = JournalColumn.ID.columnName();
     private static final String KEY_DATE = JournalColumn.DATE.columnName();
     private static final String KEY_START_TIME = JournalColumn.START_TIME.columnName();
     private static final String KEY_END_TIME = JournalColumn.END_TIME.columnName();
     private static final String KEY_DATE_TIME = JournalColumn.DATE_TIME.columnName();
     private static final String KEY_FEED_TIME = JournalColumn.FEED_TIME.columnName();
     private static final String KEY_SIDE = JournalColumn.SIDE.columnName();
     private static final String KEY_OUNCES = JournalColumn.OUNCES.columnName();
     private static final String KEY_CHILD_ID = JournalColumn.CHILD_ID.columnName();
     private static final String KEY_LATITUDE = JournalColumn.LATITUDE.columnName();
     private static final String KEY_LONGITUDE = JournalColumn.LONGITUDE.columnName();
     private static final String KEY_CREATED_DATE = JournalColumn.CREATED_DATE.columnName();
     private static final String KEY_LAST_MOD_DATE = JournalColumn.LAST_MOD_DATE.columnName();
 
     private SQLiteDatabase database;
     private DatabaseHandler databaseHandler;
 
     public JournalDao(Context context) {
         databaseHandler = new DatabaseHandler(context);
     }
 
     public void open() throws SQLException {
         database = databaseHandler.getWritableDatabase();
     }
 
     public void close() {
         databaseHandler.close();
     }
 
     /**
      * Adds a new Entry to the Database.
      *
      * @param entry - Journal POJO
      */
     public void addEntry(Journal entry) throws ParseException {
 
         Calendar now = Calendar.getInstance();
 
         //NOTE: DateTime is a combination of Date and StartTime converted into ISO8601 format. Used for date sorting.
         Date dateTime = ANDROID_TIME.parse(entry.getDate().trim() + ":" + entry.getStartTime());
         
         open();
 
         ContentValues values = new ContentValues();
         values.put(KEY_DATE, entry.getDate()); // Date
         values.put(KEY_START_TIME, entry.getStartTime()); // Time
         values.put(KEY_END_TIME, entry.getEndTime()); // Time
         values.put(KEY_DATE_TIME, ISO8601.format(dateTime)); // DateTime
         values.put(KEY_FEED_TIME, entry.getFeedTime()); // Feed Time
         values.put(KEY_SIDE, entry.getSide()); // Side
         values.put(KEY_OUNCES, entry.getOunces()); // Ounces
         values.put(KEY_CHILD_ID, entry.getChild()); // Child ID
         values.put(KEY_LATITUDE, entry.getLatitude());
         values.put(KEY_LONGITUDE, entry.getLongitude());
         values.put(KEY_CREATED_DATE, ISO8601.format(now.getTime()));
         values.put(KEY_LAST_MOD_DATE, ISO8601.format(now.getTime()));
 
         // Inserting Row
         long result = database.insert(TABLE_DATA, null, values);
         
         close();
     }
 
     /**
      * Getting single entry from the database by its ID.
      *
      * @param id - The ID of the Entry in the database.
      *
      * @return - Journal POJO representation of a specific Entry in the database.
      */
     public Journal getEntry(int id) {
 
         open();
         
         Cursor cursor = database.query(TABLE_DATA, JournalColumn.getColumnNames(), KEY_ID + "=?",
                 new String[]{String.valueOf(id)}, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
 
         Journal entry =  cursorToJournal(cursor);
         
         close();
 
         // return baby
         return entry;
     }
 
 
     /**
      * Getting single entry from the database by its date.
      *
      * @param date - The date of the entry in the database.
      *
      * @return - Journal POJO representation of a specific entry in the database.
      */
     public Journal getEntryByDate(String date) {
 
         open();
 
         Cursor cursor = database.query(TABLE_DATA, JournalColumn.getColumnNames(), KEY_DATE + "=?",
                 new String[] { String.valueOf(date) }, null, null, null, null);
         if (cursor != null) {
             cursor.moveToFirst();
         }
 
         Journal entry =  cursorToJournal(cursor);
 
         close();
 
         return entry;
     }
     
     public List<Journal> getEntriesForChildByDate(int childId, String date){
         
         open();
 
         List<Journal> entries = new ArrayList<Journal>();
 
         String selectQuery = "SELECT  * FROM " + TABLE_DATA + " WHERE " + KEY_CHILD_ID + "=" + childId + " AND "
                 + KEY_DATE + " = '" + date + "'";
         Cursor cursor = database.rawQuery(selectQuery, null);
 
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 entries.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
 
         close();
 
         return entries;
     }
 
     /**
      * Get All Entries from the Database
      *
      * @return - Collection of all Entries located in the database.
      */
     public List<Journal> getAllEntries() {
         
         open();
         
         List<Journal> entryList = new ArrayList<Journal>();
 
         // Select All Query
         String selectQuery = "SELECT  * FROM " + TABLE_DATA;
         Cursor cursor = database.rawQuery(selectQuery, null);
 
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 entryList.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
         
         close();
 
         return entryList;
     }
 
     /**
      * Returns all Feedings for a Child sorted by date in Descending Order by the passed in childId.
      *
      * @param childId
      * @return
      */
     public List<Journal> getAllEntriesForChild(int childId) {
 
         open();
 
         List<Journal> entryList = new ArrayList<Journal>();
 
         // Select All Query
         String selectQuery = "SELECT  * FROM " + TABLE_DATA + " WHERE " + KEY_CHILD_ID + "=" + childId + " ORDER BY " + KEY_DATE_TIME + " DESC";
         Cursor cursor = database.rawQuery(selectQuery, null);
 
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 entryList.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
 
         close();
 
         return entryList;
     }
 
     /**
      * Returns a Journal[] of all entries.
      *
      * @return
      */
     public Journal[] getAllEntriesAsArray(){
 
         List<Journal> entries = getAllEntries();
 
         Journal[] entryArray = new Journal[entries.size()];
         for(int i=0 ; i<entries.size() ; i++){
             Journal entry = entries.get(i);
             entryArray[i] = entry;
         }
 
         return entryArray;
     }
 
     /**
      * Returns the last X feedings (where X=limit) based on the passed in childId in the form of a List<Journal>.
      *
      * @param childId
      * @param limit
      * @return
      */
     public List<Journal> getLastFeedingsByChild(int childId, int limit){
 
         open();
 
         List<Journal> entryList = new ArrayList<Journal>();
 
         String query = "SELECT * FROM " + TABLE_DATA + " WHERE " + KEY_CHILD_ID + "=" + childId + " ORDER BY "
                 + KEY_DATE_TIME + " DESC LIMIT " + limit;
         Cursor cursor = database.rawQuery(query, null);
 
         if(cursor.moveToFirst()) {
             do{
                 entryList.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
 
         close();
 
         return entryList;
     }
 
     /**
      * Returns the last X feedings (where X=limit) based on the passed in childId in the form of a Journal[].
      *
      * @param childId
      * @param limit
      * @return
      */
     public Journal[] getLastFeedingsByChildAsArray(int childId, int limit){
         
         List<Journal> entries = getLastFeedingsByChild(childId, limit);
 
         Journal[] entryArray = new Journal[entries.size()];
         for(int i=0 ; i<entries.size() ; i++){
             Journal entry = entries.get(i);
             entryArray[i] = entry;
         }
 
         return entryArray;
     }
 
     /**
      * Updates an existing Entry in the database.
      *
      * @return
      */
     public int updateEntry(Journal entry, int id) throws ParseException {
 
         Calendar now = Calendar.getInstance();
 
         //NOTE: DateTime is a combination of Date and StartTime converted into ISO8601 format. Used for date sorting.
         Date dateTime = ANDROID_TIME.parse(entry.getDate().trim() + ":" + entry.getStartTime());
 
         open();
         
         ContentValues values = new ContentValues();
         values.put(KEY_DATE, entry.getDate());
         values.put(KEY_START_TIME, entry.getStartTime());
         values.put(KEY_END_TIME, entry.getEndTime());
         values.put(KEY_DATE_TIME, ISO8601.format(dateTime));
         values.put(KEY_FEED_TIME, entry.getFeedTime());
         values.put(KEY_SIDE, entry.getSide());
         values.put(KEY_OUNCES, entry.getOunces());
         values.put(KEY_CHILD_ID, entry.getChild());
         values.put(KEY_LAST_MOD_DATE, ISO8601.format(now.getTime()));
 
         // updating row
         int result = database.update(TABLE_DATA, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
         
         close();
         
         return result;
     }
 
     /**
      * Deletes entries with babyID from the Database
      *
      * @param babyID
      */
     public void deleteEntry(int babyID) {
         open();
         database.delete(TABLE_DATA, KEY_CHILD_ID + " = ?", new String[]{String.valueOf(babyID)});
         close();
     }
 
     /**
      * Deletes entry with ID from the Database
      */
     public void deleteEntryByID(int ID) {
         open();
         database.delete(TABLE_DATA, KEY_ID + " = ?", new String[]{String.valueOf(ID)});
         close();
     }
 
     /**
      * Returns the total number of entries that exist in the Database.
      *
      * @return - Total number of entries in the entries table.
      */
     public int getEntriesCount() {
         open();
         String countQuery = "SELECT  * FROM " + TABLE_DATA;
         Cursor cursor = database.rawQuery(countQuery, null);
         cursor.close();
 
         // return count
         int count = cursor.getCount();
         
         close();
         
         return count;
     }
 
     /**
      * Returns the total number of feedings for a baby on a given date.
      *
     * @param childId
      * @param date
      * @return
      */
    public int getEntriesCountByBabyAndDate(int childId, String date){

         open();
 
        String selectQuery = "SELECT  * FROM " + TABLE_DATA + " WHERE " + KEY_CHILD_ID + "=" + childId + " AND "
                + KEY_DATE + " = '" + date + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
         int count = cursor.getCount();
 
         close();

         return count;
     }
 
     /**
      * Converts a Cursor from the Entries Table to a Journal Object.
      *
      * @param cursor - Cursor object that contains the Entry data from the Database
      *
      * @return - Journal
      */
     private Journal cursorToJournal(Cursor cursor) {
         return new Journal(Integer.parseInt(cursor.getString(0)),
                 cursor.getString(1),
                 cursor.getString(2),
                 cursor.getString(3),
                 cursor.getString(4),
                 cursor.getString(5),
                 cursor.getString(6),
                 cursor.getString(7),
                 Integer.parseInt(cursor.getString(8)),
                 cursor.getString(9),
                 cursor.getString(10),
                 cursor.getString(11),
                 cursor.getString(12));
     }
 }
