 package edu.southern.data;
 
 import java.io.IOException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.webkit.JavascriptInterface;
 
 /**
  * Daily Verse Database for storing daily verse references. 
  * Daily verse used by HomeScreen.
  * 
  * @author Nathanael
  */
 public class DailyVerseDBHelper {
 
 	private DailyVerseDB dvDB;
 
 	/**
 	 * Constructor for database. Creates and opens
 	 * it for use.
 	 */
 	public DailyVerseDBHelper(Context context) {
 
 		dvDB = new DailyVerseDB(context);
 
 		// Create database (does nothing if database exists)
 		try {
 			dvDB.createDataBase();
 		} 
 		catch (IOException ioe) {
 			throw new Error("Unable to create database");
 		}
 	}
 
 	/**
 	 * Gets the next verse that has not been seen from
 	 * daily verse table. Checks the DateLastSeen attribute.
 	 */
 	public int getNextDailyVerse() {
 
 		// Attempt to open database
 		try {
 			dvDB.openDataBase();
 		} 
 		catch(SQLException sqle){
 			throw sqle;
 		}	
 		SQLiteDatabase dailyVerseDB = dvDB.getWritableDatabase();
 
 		// Get cursor from query
 		Cursor c = dailyVerseDB.rawQuery("SELECT _id, referenceNumber, dateLastSeen FROM DailyVerse ORDER BY dateLastSeen", new String[] {});
 		c.moveToFirst();
 		int dailyVerseReference = c.getInt(1);
 
 		Log.d("Database", Integer.toString(dailyVerseReference));
 		updateVerseLastSeen(dailyVerseReference);
 
 		c.close();
 		dailyVerseDB.close();
		dvDB.close();
		
 		return dailyVerseReference;
 	}
 
 	/**
 	 * Sets dateLastSeen on row given reference number
 	 */
 	public void updateVerseLastSeen(int refNumber) {
 		
 	// Attempt to open database
 			try {
 				dvDB.openDataBase();
 			} 
 			catch(SQLException sqle){
 				throw sqle;
 			}	
 			SQLiteDatabase dailyVerseDB = dvDB.getWritableDatabase();
 			
 		dailyVerseDB.execSQL("UPDATE DailyVerse SET dateLastSeen=date('now') WHERE referenceNumber="
 				.concat(Integer.toString(refNumber)));
 		
 		dailyVerseDB.close();
		dvDB.close();
 	}
 }
