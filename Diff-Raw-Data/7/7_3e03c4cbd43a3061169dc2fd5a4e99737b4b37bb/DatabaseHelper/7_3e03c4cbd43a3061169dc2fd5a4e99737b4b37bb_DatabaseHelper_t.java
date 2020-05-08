 package com.ijmacd.gpstools.mobile;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Iain
  * Date: 28/06/13
  * Time: 12:35
  * To change this template use File | Settings | File Templates.
  */
 class DatabaseHelper extends SQLiteOpenHelper {
     private static final String DATABASE_NAME = "GPSTools.db";
     private static final int DATABASE_VERSION = 2;
 
     public static final String ID_COLUMN = "_id";
     public static final String TYPE_COLUMN = "type";
     public static final String ORDER_COLUMN = "child";
     public static final String WIDTH_COLUMN = "width";
     public static final String HEIGHT_COLUMN = "height";
     public static final String UNITS_COLUMN = "units";
     public static final String WIDGET_TABLE_NAME = "Widgets";
     public static final String TRACK_TABLE_NAME = "Tracks";
     public static final String TRACKPOINT_TABLE_NAME = "Trackpoints";
     public static final String DATE_COLUMN = "date";
     public static final String NAME_COLUMN = "name" ;
     public static final String DISTANCE_COLUMN = "distance";
     public static final String DURATION_COLUMN = "duration";
     public static final String TRACK_ID_COLUMN = "track_id";
     public static final String LAT_COLUMN = "latitude";
     public static final String LON_COLUMN = "longitude";
     public static final String ALTITUDE_COLUMN = "altitude";
     public static final String ACCURACY_COLUMN = "accuracy";
     public static final String SPEED_COLUMN = "speed";
     public static final String HEADING_COLUMN = "heading";
     public static final String COMPLETE_COLUMN = "complete";
 
     private static final String LOG_TAG = "GPSTools";
 
     public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + WIDGET_TABLE_NAME + " (" +
                 ID_COLUMN + " INTEGER PRIMARY KEY, " +
                 ORDER_COLUMN  + " INTEGER, " +
                 TYPE_COLUMN + " INTEGER, " +
                 WIDTH_COLUMN + " INTEGER, " +
                 HEIGHT_COLUMN + " INTEGER, " +
                 UNITS_COLUMN + " INTEGER" +
                 ");");
         db.execSQL("CREATE TABLE " + TRACK_TABLE_NAME + " (" +
                 ID_COLUMN + " INTEGER PRIMARY KEY, " +
                 DATE_COLUMN  + " INTEGER, " +
                 NAME_COLUMN + " INTEGER, " +
                 DISTANCE_COLUMN + " INTEGER, " +
                DURATION_COLUMN + " INTEGER, " +
                COMPLETE_COLUMN + " INTEGER" +
                 ");");
         db.execSQL("CREATE TABLE " + TRACKPOINT_TABLE_NAME + " (" +
                 ID_COLUMN + " INTEGER PRIMARY KEY, " +
                 TRACK_ID_COLUMN  + " INTEGER, " +
                 DATE_COLUMN + " INTEGER, " +
                 LAT_COLUMN + " REAL, " +
                 LON_COLUMN + " REAL, " +
                 ALTITUDE_COLUMN + " REAL, " +
                 ACCURACY_COLUMN + " REAL, " +
                 SPEED_COLUMN + " REAL, " +
                HEADING_COLUMN + " INTEGER" +
                 ");");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         if(oldVersion < 2){
             db.execSQL("ALTER TABLE " + TRACK_TABLE_NAME + " ADD COLUMN " + COMPLETE_COLUMN + " INTEGER");
             db.execSQL("UPDATE " + TRACK_TABLE_NAME + " SET " + COMPLETE_COLUMN + " = 1");
             Log.v(LOG_TAG, "Updated database version: " + oldVersion + " -> " + newVersion);
         }else {
             db.execSQL("DROP TABLE IF EXISTS " + WIDGET_TABLE_NAME + ", " +
                     TRACK_TABLE_NAME + ", " +
                     TRACKPOINT_TABLE_NAME);
             onCreate(db);
         }
     }
 }
