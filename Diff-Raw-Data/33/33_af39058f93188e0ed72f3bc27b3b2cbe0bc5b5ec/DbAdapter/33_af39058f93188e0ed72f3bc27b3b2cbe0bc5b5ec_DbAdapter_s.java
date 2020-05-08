 package com.tonyjhuang.listpop;
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class DbAdapter {
 
 	private static final String DATABASE_NAME="ListPopDB";
 	private static final String DATABASE_TABLE="Lists";
 	private static final int DATABASE_VERSION=1;
 	
 	public static final String KEY_ROWID="_id";
 	public static final String KEY_LIST_HEADERS="List_Headers";
 	
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	private static final String DATABASE_CREATE=
 			"create table " + DATABASE_TABLE + " ("  
 				+ KEY_ROWID + " integer primary key autoincrement, "
 				+ KEY_LIST_HEADERS + " text not null);";
 	
 	private final Context mCtx;
 	
 	public DbAdapter(Context ctx) {
 		this.mCtx = ctx;
 	}
 	
 	//Opens Read/Write-accessible database.
 	//Call this in onCreate, onResume!
 	public DbAdapter open() throws android.database.SQLException{
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	//Closes database to free up resources.
 	//Call this in onStop!
 	public void close(){
 		mDbHelper.close();
 	}
 
 	public long enterPhrase(ArrayList<String> a) {
 		// Change ArrayList<String> to Byte Array.
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(baos);
 		for (String element : a) {
 		    try {
 				out.writeUTF(element);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		byte[] bytes = baos.toByteArray();
 
 		
 		ContentValues initialValues = new ContentValues();
 		// Store bytes (Byte Array) in mDb.
 		initialValues.put(KEY_LIST_HEADERS, bytes);
 		
 		return mDb.insert(DATABASE_TABLE, null, initialValues);
 	}
 	
 	
 	/*	
 	//Delete's the last entry to the database (first in last out)
 	public void deleteLast(){
 		mDb.execSQL("DELETE FROM "+DATABASE_TABLE+" WHERE "+KEY_ROWID+
 				" = (SELECT MAX("+KEY_ROWID+") FROM "+DATABASE_TABLE+");");
 	}
 	*/
	public void deleteByName(String phrase){
		 mDb.delete(DATABASE_TABLE, "=?", new String[] { phrase });
	}
 	
 	public boolean deleteById(long id){
 		return mDb.delete(DATABASE_TABLE,  KEY_ROWID+"="+id, null) > 0;
 	}
 	

	public long fetchNumberOfLists(){
		String cmd = "SELECT COUNT(*) FROM " + DATABASE_TABLE;
		SQLiteStatement statement = mDb.compileStatement(cmd);
		long count = statement.simpleQueryForLong();
		return count;
	}
	
	public Cursor fetchByRowId(long rowId) throws SQLException {
 		Cursor mCursor =
 				mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,KEY_LIST_HEADERS},
 						KEY_ROWID+"="+rowId, null, null, null, null, null);
 		if (mCursor != null){
 			mCursor.moveToFirst();
 		}
		return mCursor;
 	}
 	
	//returns a result set of all existing phrases and their rowid's in the db
 	public Cursor fetchAll(){
 		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LIST_HEADERS},
 				null, null, null, null, null);
 	}
		
 	public boolean isEmpty(){
 		return fetchNumberOfLists() == 0;
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		DatabaseHelper(Context c){
 			super(c, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE);
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion,
 				int newVersion){
 			//Not used
 		}
 		
 	}
 	
 }
 
