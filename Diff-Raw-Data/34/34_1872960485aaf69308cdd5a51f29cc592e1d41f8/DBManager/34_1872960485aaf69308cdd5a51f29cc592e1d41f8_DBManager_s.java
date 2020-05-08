 package com.delin.speedlogger.Serialization;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.delin.speedlogger.Results.SessionResult;
 
 import net.sqlcipher.database.SQLiteDatabase;
 import net.sqlcipher.database.SQLiteOpenHelper;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 public class DBManager extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "SpeedLogger.db"; // TODO: get app name from strings.xml or something
 	private static final int DATABASE_VERSION = 1;
 	private static final String PASSWORD = "Push! Push! Push!";
 	
 	//------------------- LOCAL_RESULTS table------------------------
 	private static final String LOCAL_RESULTS = "LocalResults";
 	// columns
 	private static final String START_TIME 	 = "StartTime";
 	private static final String MAX_SPEED 	 = "MaxSpeed";
 	private static final String DISTANCE 	 = "Distance";
 	private static final String DURATION 	 = "Duration";
 
 	
 	public DBManager(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String sql = "create table " + LOCAL_RESULTS + "( " + 
 						BaseColumns._ID + " integer primary key autoincrement, " + 
 						START_TIME 		+ " integer not null, " + 
 						MAX_SPEED 		+ " real not null, " + 
 						DISTANCE 		+ " real not null, " + 
 						DURATION 		+ " integer not null" + ");";
 		db.execSQL(sql);
 		Log.i("DBManager", "onCreate(): " + sql);
 	}
 	
 	public void InsertSessionResult(SessionResult result) {
 	    ContentValues values = new ContentValues();
 	    values.put(START_TIME, result.GetStartTime());
 	    values.put(MAX_SPEED, result.GetMaxSpeed());
 	    values.put(DISTANCE, result.GetTotalDistance());
 	    values.put(DURATION, result.GetTotalTime());
 	    
 	    SQLiteDatabase db = getWritableDatabase(PASSWORD);
 	    db.insert(LOCAL_RESULTS, null, values);
 	    db.close();
 	    
 	    Log.i("DBManager", "InsertSessionResult()");
 	  }
 	
 	public List<SessionResult> GetSessionResults() {
 		// load data from DB
 		SQLiteDatabase db = getReadableDatabase(PASSWORD);
 	    Cursor cursor = db.query(LOCAL_RESULTS,null,null,null,null,null,null);
 	    // parse data
 	    List<SessionResult> results = new ArrayList<SessionResult>();
 	    while(cursor.moveToNext()){
	    	/*SessionResult r = new SessionResult();
			r.setStartTime(cursor.getLong(1)); // 0 is for id_
			r.setMaxSpeed(cursor.getFloat(2));
			r.setDistance(cursor.getFloat(3));
			r.setDuration(cursor.getLong(4));
			results.add(r);*/
 	      }
 	    cursor.close(); // TODO: can be buggy. read about managing cursors
 	    db.close();
 	    Log.i("DBManager", "GetSessionResults()");
 	    return results;
 	}
 	
 	public void ClearLocalResults() {
 		String sql = "delete from " + LOCAL_RESULTS;
 		SQLiteDatabase db = getWritableDatabase(PASSWORD);
 		db.execSQL(sql);
 		db.close();
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (oldVersion >= newVersion)
 			return;
 		// here we will transform DB and try to save old data
 		// we will possibly need it in the far future
 	}
 
 }
