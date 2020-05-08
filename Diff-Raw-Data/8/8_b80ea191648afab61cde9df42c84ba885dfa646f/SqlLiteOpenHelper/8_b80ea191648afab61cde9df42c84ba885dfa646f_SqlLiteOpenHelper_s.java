 package com.xmedic.troll.service.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SqlLiteOpenHelper extends SQLiteOpenHelper {
 
 	
 	 private static final String LOG_TAG = "troll-db";
 
 	 public static final String CITY = "city";
 	 
 	 public static final String ROAD = "road";
 	 
 	 public static final String LEVEL = "level";
 
 	 
 	 private Context context;
 
 	 
 	 public SqlLiteOpenHelper(Context context) {
 		    super(context, DDL.DB_NAME, null, DDL.DB_VERSION);
 		    this.context = context;
 	 }
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		 Log.i(LOG_TAG, "Creating initial database");
 		    try {
 		    	db.execSQL(DDL.CITY);
 		    	db.execSQL(DDL.ROAD);
 		    	db.execSQL(DDL.LEVEL);
 		    } catch (Exception e) {
 		    	Log.e(LOG_TAG, e.getMessage());
 		    }
 		    Log.i(LOG_TAG, "Finished creating database.");
 
 
 		    Log.i(LOG_TAG, "Inserting initial data");
 		    SqlLiteImportHelper.importInitialData(db, context);     
 		    Log.i(LOG_TAG, "Finished inserting initial data");
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(LOG_TAG, String.format(
 				"Upgrading database from version %s to %s which will destroy all old data", 
 				oldVersion, 
 				newVersion));
 		 db.execSQL("DROP TABLE IF EXISTS " + SqlLiteOpenHelper.CITY + ";");
 		 db.execSQL("DROP TABLE IF EXISTS " + SqlLiteOpenHelper.ROAD + ";");
 		 db.execSQL("DROP TABLE IF EXISTS " + SqlLiteOpenHelper.LEVEL + ";");
 	     onCreate(db);
 	}
 
 
 	 private static class DDL {
 
          public static String DB_NAME = "trucknroll";
          
          public static int DB_VERSION = 1;
 
          public static String CITY = 
         		 "CREATE TABLE " + SqlLiteOpenHelper.CITY + " (" +
                          "id             TEXT PRIMARY KEY, " +
                          "name           TEXT NOT NULL, " +
                          "country        TEXT NOT NULL, " +
                          "latitude       REAL NOT NULL, " +
                          "longitude      REAL NOT NULL, " +
                          "population     INTEGER NOT NULL," +
                          "x				 INTEGER," +
                          "y				 INTEGER)";
          
          public static String ROAD = 
         		 "CREATE TABLE " + SqlLiteOpenHelper.ROAD + " (" +
                          "fromCityId           TEXT NOT NULL, " +
                          "toCityId        	   TEXT NOT NULL)";
          
          public static String LEVEL = 
         		 "CREATE TABLE " + SqlLiteOpenHelper.LEVEL + " (" +
                          "id           TEXT PRIMARY KEY, " +
                         "name         TEXT PRIMARY KEY, " +
                         "description  TEXT PRIMARY KEY, " +
        				 "startCityId  TEXT PRIMARY KEY, " +
        				 "goalCityId   TEXT PRIMARY KEY)";
 	 }
 
 }
