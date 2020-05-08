 package com.novell.android.yastroid;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 //import android.widget.Toast;
 
 public class YastroidOpenHelper extends SQLiteOpenHelper {
 
 	private static final String TAG = "YaSTroidDatabase";
 
 	private static final String DATABASE_NAME = "yastroid";
	private static final int DATABASE_VERSION = 3;
 	static final String SERVER_TABLE_NAME = "servers";
 
 	private static final String CREATE_TABLES = "CREATE TABLE "
 			+ SERVER_TABLE_NAME + " ("
 			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT, "
 			+ "scheme TEXT, " + "hostname TEXT, " + "port INTEGER, "
 			+ "user TEXT, " + "pass TEXT);";
 	// +
 	// "CREATE TABLE group (_id INTEGER PRIMARY KEY AUTOINCREMENTE, name TEXT, serverID INTEGER";
 
 	private static final String ADD_SERVER = "INSERT INTO "
 			+ SERVER_TABLE_NAME
 			+ "(name,scheme,hostname,port,user,pass) VALUES ('webyast1', 'http', '137.65.132.194', '4984','root','sandy');";
 
 	private static final String ADD_SERVER_2 = "INSERT INTO "
 			+ SERVER_TABLE_NAME
 			+ "(name,scheme,hostname,port,user,pass) VALUES ('webyast2', 'http', '137.65.132.194', '4984','root','sandy');";
 
 	public YastroidOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLES);
 		// Demo data
 		db.execSQL(ADD_SERVER);
 		db.execSQL(ADD_SERVER_2);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TAG, "Upgrading database from version " + oldVersion + "to "
 				+ newVersion);
 		// Any changes to the database structure should occur here.
 		// This is called if the DATABASE_VERSION installed is older
 		// than the new version.
 		// ie. db.execSQL("alter table " + TASKS_TABLE + " add column " +
 		// TASK_ADDRESS + " text");
		db.execSQL("DROP TABLE servers");
 		onCreate(db);
 	}
 }
