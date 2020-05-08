 package com.indivisible.tortidy.database;
 import android.database.sqlite.*;
 import android.content.*;
 import android.util.*;
 
 /** class to access the db's labels table **/
 public class LabelsHelper extends SQLiteOpenHelper
 {
 	private static final String TAG = "com.indivisible.tortidy";
 	
 	//TODO move db name and version to a common class
 	public static final String DATABASE_NAME = "TorTidyDb";
 	public static final int DATABASE_VERSION = 1;
 	public static final String TABLE_LABELS  = "labels";
 	public static final String COLUMN_ID     = "_id";
 	public static final String COLUMN_TITLE  = "title";
	public static final String COLUMN_EXISTS = "existing_label";
 	
 	
 	/** constructor for the db's labels table **/
 	public LabelsHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	
 	/** create a new, empty table **/
 	public void onCreate(SQLiteDatabase db)
 	{
 		Log.i(TAG, "creating labels table...");
 		String create = createStatement();
 		db.execSQL(create);
 	}
 	
 	/** drop and recreate table on new version **/
 	public void onUpgrade(SQLiteDatabase db, int oldVerNum, int newVerNum)
 	{
 		Log.i(TAG, "upgrading labels table from " +oldVerNum+ " to " +newVerNum+ "...");
 		Log.w(TAG, "existing data will be deleted");
 		
 		db.execSQL("drop table if exists " +TABLE_LABELS+ ";");
 		onCreate(db);
 	}
 	
 	/** generate the sql statement to create the table in the db **/
 	private static String createStatement() {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("create table ").append(TABLE_LABELS);
 		sb.append(" (");
		sb.append(COLUMN_ID).append(" INTEGER primary key autoincrement, ");
 		sb.append(COLUMN_EXISTS).append(" INTEGER, ");
 		sb.append(COLUMN_TITLE).append(" TEXT not null ");
 		sb.append(");");
 		
 		return sb.toString();
 	}
 	
 	
 	
 }
