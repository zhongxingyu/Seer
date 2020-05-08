 package com.mntnorv.wrdl_holo.db;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class GameStatesTable {
 	// Database table
	public static final String TABLE_STATES = "states";
 	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LETTERS = "letters";
	public static final String COLUMN_SIZE = "size";
 
 	// Database creation SQL statement
 	private static final String DATABASE_CREATE = "create table " 
 			+ TABLE_STATES
 			+ "(" 
 			+ COLUMN_ID + " integer primary key autoincrement, " 
 			+ COLUMN_LETTERS + " text not null, " 
 			+ COLUMN_SIZE 
 			+ " integer not null"
 			+ ");";
 
 	public static void onCreate(SQLiteDatabase database) {
 		database.execSQL(DATABASE_CREATE);
 	}
 
 	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
 			int newVersion) {
 		Log.w(GameStatesTable.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		database.execSQL("DROP TABLE IF EXISTS " + TABLE_STATES);
 		onCreate(database);
 	}
 }
