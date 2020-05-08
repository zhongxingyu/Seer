 package com.github.lazycure.db;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.github.lazycure.activities.Activity;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 	
 	// All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "activityManager";
  
     // Activities table name
     private static final String TABLE_ACTIVITIES = "activities";
  
     // Activities Table Columns names
     private static final String KEY_NAME = "name";
     private static final String KEY_FINISH_DATE = "finish_date";
 
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
         String CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + TABLE_ACTIVITIES + "("
                 + KEY_NAME + " TEXT,"
                 + KEY_FINISH_DATE + " TEXT" + ")";
         db.execSQL(CREATE_ACTIVITIES_TABLE);
     }
 
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
  
         // Create tables again
         onCreate(db);
     }
     
     // Adding new activity
     public void addActivity(Activity activity) {
         SQLiteDatabase db = this.getWritableDatabase();
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
      
         ContentValues values = new ContentValues();
         values.put(KEY_NAME, activity.getName()); // Activity Name
         values.put(KEY_FINISH_DATE, dateFormat.format(activity.getFinishTime())); // Activity start date
      
         // Inserting Row
         db.insert(TABLE_ACTIVITIES, null, values);
         db.close(); // Closing database connection
     }
     
  // Getting All Activities
     public List<Activity> getAllActivities() {
        List<Activity> activityList = new ArrayList<Activity>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIVITIES;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
         	   String name = cursor.getString(0);
         	   String date = cursor.getString(1);
         	   Date finishDate = null;
 				try {
 					finishDate = dateFormat.parse(date);
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	   Activity activity = new Activity(name, null, finishDate);
                // Adding activity to list
         	   activityList.add(activity);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
     
        // return activity list
        return activityList;
    }
     
  // Getting activities Count
     public int getActivitiesCount() {
         String countQuery = "SELECT  * FROM " + TABLE_ACTIVITIES;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(countQuery, null);
         cursor.close();
  
         // return count
        return cursor.getCount();
     }
 
 }
