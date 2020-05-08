 package com.kentph.ttcnextbus;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 import static com.kentph.ttcnextbus.RouteDbHelper.RouteDbContract.*;
 
 /**
  * Created by Kent on 9/3/13.
  */
 public class RouteDbHelper extends SQLiteOpenHelper {
 
     // Contract class for schema name constants
     public static final class RouteDbContract {
         public RouteDbContract() {}
 
         // Inner class that defines the table of route paths
         public static abstract class PathsTable implements BaseColumns {
             public static final String TABLE_NAME = "paths";
             public static final String COLUMN_NAME_ROUTE_NUMBER = "routenum";
             public static final String COLUMN_NAME_PATH_ID = "pathid";
             public static final String COLUMN_NAME_LAT = "lat";
             public static final String COLUMN_NAME_LON = "lon";
         }
 
         // Inner class that defines the table of route stops
         public static abstract class StopsTable implements BaseColumns {
             public static final String TABLE_NAME = "stops";
             public static final String COLUMN_NAME_ROUTE_NUMBER = "routenum";
             public static final String COLUMN_NAME_ROUTE_NAME = "routename";
             public static final String COLUMN_NAME_STOP_ID = "stopid";
             public static final String COLUMN_NAME_STOP_TAG = "stoptag";
             public static final String COLUMN_NAME_STOP_TITLE = "stoptitle";
             public static final String COLUMN_NAME_LAT = "lat";
             public static final String COLUMN_NAME_LON = "lon";
             public static final String COLUMN_NAME_DIRECTION = "direction";
             public static final String COLUMN_NAME_BRANCH = "branch";
             public static final String COLUMN_NAME_GRID_LAT = "gridlat";
             public static final String COLUMN_NAME_GRID_LON = "gridlon";
         }
     }
 
     // If you change the database schema, you must increment the database version.
     public static final int DATABASE_VERSION = 1;
     public static final String DATABASE_NAME = "RouteDb.db";
 
     public RouteDbHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(
                "CREATE TABLE" + PathsTable.TABLE_NAME + " (" +
                         PathsTable._ID + " INTEGER PRIMARY KEY," +
                         PathsTable.COLUMN_NAME_ROUTE_NUMBER + " TEXT," +
                         PathsTable.COLUMN_NAME_PATH_ID + " TEXT," +
                         PathsTable.COLUMN_NAME_LAT + " DOUBLE," +
                         PathsTable.COLUMN_NAME_LON + " DOUBLE )"
         );
         db.execSQL(
                "CREATE TABLE" + StopsTable.TABLE_NAME + " (" +
                         StopsTable._ID + " INTEGER PRIMARY KEY," +
                         StopsTable.COLUMN_NAME_ROUTE_NUMBER + " TEXT," +
                         StopsTable.COLUMN_NAME_ROUTE_NAME + " TEXT," +
                         StopsTable.COLUMN_NAME_STOP_ID + " TEXT," +
                         StopsTable.COLUMN_NAME_STOP_TAG + " TEXT," +
                         StopsTable.COLUMN_NAME_STOP_TITLE + " TEXT," +
                         StopsTable.COLUMN_NAME_LAT + " DOUBLE," +
                         StopsTable.COLUMN_NAME_LON + " DOUBLE," +
                         StopsTable.COLUMN_NAME_DIRECTION + " TEXT," +
                         StopsTable.COLUMN_NAME_BRANCH + " TEXT," +
                         StopsTable.COLUMN_NAME_GRID_LAT + " TEXT," +
                         StopsTable.COLUMN_NAME_GRID_LON + " TEXT )"
         );
     }
 
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // This database is only a cache for online data, so its upgrade policy is
         // to simply to discard the data and start over
         db.execSQL("DROP TABLES IF EXISTS " + PathsTable.TABLE_NAME);
         db.execSQL("DROP TABLES IF EXISTS " + StopsTable.TABLE_NAME);
         onCreate(db);
     }
 
     public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         onUpgrade(db, oldVersion, newVersion);
     }
 }
