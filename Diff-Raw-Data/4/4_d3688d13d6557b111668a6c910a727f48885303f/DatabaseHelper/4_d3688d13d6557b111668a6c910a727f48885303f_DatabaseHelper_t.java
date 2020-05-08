 package de.bit.android.syncsample.content;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * Helper class that creates and upgrades the SQLite database tables for the
  * application.
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	/**
 	 * The database that the provider uses as its underlying data store
 	 */
 	static final String DATABASE_NAME = "syncsample.db";
 
 	/**
 	 * The database version
 	 */
 	static final int DATABASE_VERSION = 1;
 
 	DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + TodoEntity.TABLE_NAME + " (" //
 				+ TodoEntity.ID + " INTEGER PRIMARY KEY," //
 				+ TodoEntity.SERVER_ID + " LONG," //
				+ TodoEntity.SERVER_VERSION + " LONG," //
				+ TodoEntity.TITLE + " TEXT" //
 				+ ");");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		db.execSQL("DROP TABLE IF EXISTS " + TodoEntity.TABLE_NAME);
 
 		onCreate(db);
 
 	}
 
 }
