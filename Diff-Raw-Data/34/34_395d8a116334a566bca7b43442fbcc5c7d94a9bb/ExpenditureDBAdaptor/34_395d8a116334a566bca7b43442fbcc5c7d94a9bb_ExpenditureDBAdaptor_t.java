 package com.lcm.data;
 
 import java.util.Calendar;
 import java.util.Date;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class ExpenditureDBAdaptor {
 	public static final String DATABASE_NAME = "Expenditure";
 	public static final String DATABASE_TABLE = "expenditure_list";
 	public static int DATABASE_VERSION = 1;
 	// below is for columns in the database
 	public static final String KEY_ID = "_id";
 	public static final String KEY_SPEND = "spend"; // this can be the time a sms has come or the time user inserted
 	public static final String KEY_CATEGORY = "category";
 	public static final String KEY_YEAR = "year";
 	public static final String KEY_MONTH = "month";
 	public static final String KEY_DAY = "day";
 	public static final String KEY_TIME = "time";
 	public static final String KEY_DETAIL = "detail";
 	public static final String KEY_LOCATION_LONG = "loc_long";
 	public static final String KEY_LOCATION_LATI = "loc_lati";
 	public static final String KEY_BANK = "bank";
 	public static final String KEY_SMS_ID = "sms_id";
 	// end of columns definition
 	
 	public static final String UNKNOWN = "__Unknown";
 	
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	private final Context ctx;
 	
 	private static final String DB_CREATE = "create table "+ DATABASE_TABLE +" ("
 		+ KEY_ID + " integer primary key autoincrement, "
 		+ KEY_SPEND + " integer not null, "
 		+ KEY_CATEGORY + " text not null, "
 		+ KEY_YEAR + " int not null, "
 		+ KEY_MONTH + " int not null, "
 		+ KEY_DAY + " int not null, "
 		+ KEY_TIME + " int not null, "
 		+ KEY_DETAIL + " text, "
 		+ KEY_LOCATION_LONG + " real, "
 		+ KEY_LOCATION_LATI + " real, "
 		+ KEY_BANK + " text, "
 		+ KEY_SMS_ID + " integer);";
 	private static final String TAG = "ExpenditureDBAdaptor";
 	
 	public static class DatabaseHelper extends SQLiteOpenHelper {
 		public DatabaseHelper(Context context) {
 			super(context,DATABASE_NAME,null,DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DB_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO: ALL saved sms list should be put back into the inbox list
 		}
 	}
 	
 	// basic functions for initialization and annihilation
 	public ExpenditureDBAdaptor(Context ctx) {
 		this.ctx = ctx;
 	}
 	
 	public ExpenditureDBAdaptor open() throws SQLException {
 		mDbHelper = new DatabaseHelper(ctx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public void close() {
 		mDbHelper.close();
 	}
 	
 	// when data is inserted manually
 	public long insertDB(int spend, String category, Calendar date, String detail, double loc_long, double loc_lati, String bank) {
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_SPEND, spend);
 		if(category!=null) cv.put(KEY_CATEGORY, category);
 		else cv.put(KEY_CATEGORY, UNKNOWN);
 		
 		cv.put(KEY_YEAR, date.get(Calendar.YEAR));
 		cv.put(KEY_MONTH, date.get(Calendar.MONTH));
 		cv.put(KEY_DAY, date.get(Calendar.DAY_OF_MONTH));
 		cv.put(KEY_TIME, ""+date.getTimeInMillis());
 		
 		if(detail!=null) cv.put(KEY_DETAIL, detail);
 		else cv.put(KEY_DETAIL, "");
 		cv.put(KEY_LOCATION_LONG, loc_long);
 		cv.put(KEY_LOCATION_LATI, loc_lati);
 		cv.put(KEY_BANK, bank);
 //		Log.i("db_log","in createDB");
 		Log.e(TAG,"InsertDB: "+cv.toString());
 		return mDb.insert(DATABASE_TABLE, null, cv);
 	}
 	
 	//when data is inserted using sms data
 	public long insertDB(int spend, Calendar date, String detail, String bank, int sms_id) {
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_SPEND, spend);
 		
 		cv.put(KEY_YEAR, date.get(Calendar.YEAR));
 		cv.put(KEY_MONTH, date.get(Calendar.MONTH));
 		cv.put(KEY_DAY, date.get(Calendar.DAY_OF_MONTH));
 		cv.put(KEY_TIME, ""+date.getTimeInMillis());
 		
 		cv.put(KEY_DETAIL, detail);
 		cv.put(KEY_BANK, detail);
 		cv.put(KEY_SMS_ID, sms_id);
 		return mDb.insert(DATABASE_TABLE, null, cv);
 	}
 	
 	
 //	public boolean deleteDB(int year, int month, int day, long time) {
 //		return mDb.delete(DATABASE_TABLE, 
 //				KEY_YEAR + "=" + year + " AND " + KEY_MONTH + "=" + month + " AND " +  
 //				KEY_DAY + "=" + day + " AND " + KEY_TIME + "=" + time
 //				, null) > 0;
 //	}
 	
 	public boolean deleteDB(Calendar date) {
 		return mDb.delete(DATABASE_TABLE, 
 				KEY_YEAR + "=" + date.get(Calendar.YEAR) + " AND " + KEY_MONTH + "=" + date.get(Calendar.MONTH) + " AND " +  
 				KEY_DAY + "=" + date.get(Calendar.DAY_OF_MONTH) + " AND " + KEY_TIME + "=" + date.getTimeInMillis()
 				, null) > 0;
 	}
 	
 	public boolean deleteSMS(int sms_id) {
 		return mDb.delete(DATABASE_TABLE, KEY_SMS_ID + "=" + sms_id, null) > 0;
 	}
 	
 	
 	public Cursor fetchAllDB() {
 		return mDb.query(DATABASE_TABLE, 
 				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 						KEY_TIME, KEY_DETAIL, KEY_LOCATION_LONG, KEY_LOCATION_LATI, 
 						KEY_BANK, KEY_SMS_ID},
 				null, null, null, null, null);
 	}
 	
 //	public Cursor fetchDB(int year, int month, int day, long time) throws SQLException {
 //		Cursor mCursor = mDb.query(true, DATABASE_TABLE, 
 //				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 //						KEY_TIME, KEY_DETAIL, KEY_LOCATION_X, KEY_LOCATION_Y, 
 //						KEY_BANK, KEY_SMS_ID}, 
 //				KEY_YEAR + "=" + year + " AND " + KEY_MONTH + "=" + month + " AND " +  
 //						KEY_DAY + "=" + day + " AND " + KEY_TIME + "=" + time,
 //				null, null, null,null,null);
 //		if(mCursor!=null) {
 //			mCursor.moveToFirst();
 //		}
 //		return mCursor;
 //	}
 	
 	public Cursor fetchDB(Calendar date) throws SQLException {
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, 
 				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 						KEY_TIME, KEY_DETAIL, KEY_LOCATION_LONG, KEY_LOCATION_LATI, 
 						KEY_BANK, KEY_SMS_ID}, 
 				KEY_YEAR + "=" + date.get(Calendar.YEAR) + " AND " + KEY_MONTH + "=" + date.get(Calendar.MONTH) + " AND " +  
 						KEY_DAY + "=" + date.get(Calendar.DAY_OF_MONTH) + " AND " + KEY_TIME + "=" + date.getTimeInMillis(),
 				null, null, null,null,null);
 		if(mCursor!=null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 	
 	public Cursor fetchDB(String category) throws SQLException {
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, 
 				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 						KEY_TIME, KEY_DETAIL, KEY_LOCATION_LONG, KEY_LOCATION_LATI, 
 						KEY_BANK, KEY_SMS_ID}, 
 				KEY_CATEGORY + "=" + category,
 				null, null, null,null,null);
 		if(mCursor!=null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 	
 	public Cursor fetchDB(long from, long to) throws SQLException {
 		Cursor mCursor = mDb.query(DATABASE_TABLE, 
 				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 						KEY_TIME, KEY_DETAIL, KEY_LOCATION_LONG, KEY_LOCATION_LATI, 
 						KEY_BANK, KEY_SMS_ID}, 
 //				KEY_TIME + " >= " + from + " AND " + KEY_TIME + " <= " + to,
 						KEY_TIME + " BETWEEN " + from + " AND " + to,
 						null,
 				null, null,null,null);
 		if(mCursor!=null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 	
 	public boolean isDataExist(long time) {
 		boolean exist = false;
 		
 		Cursor mCursor = mDb.query(DATABASE_TABLE, 
 				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 						KEY_TIME, KEY_DETAIL, KEY_LOCATION_LONG, KEY_LOCATION_LATI, 
 						KEY_BANK, KEY_SMS_ID}, 
						KEY_TIME + " = " + time + " OR " + KEY_TIME + " = " + (time + 1), // + " AND " + KEY_SPEND + " = " + spend,
 				null, null,null,null);

		int timeid = mCursor.getColumnIndex(KEY_TIME);
		int detailid = mCursor.getColumnIndex(KEY_DETAIL);
		while(mCursor.moveToNext()) {
			String detail = mCursor.getString(detailid);
			long timeV = mCursor.getLong(timeid);
			Log.e(TAG,"db loading detail:" +detail + " time: " + timeV);
		}
//		Log.e(TAG,"database count:" + mCursor.getCount());
 		if(mCursor.getCount()!=0) {
 			exist = true;
 		}
 		mCursor.close();
 		return exist;
 	}
 	
 	// TODO: this needs to be modified
 //	public Cursor fetchDBbyYM(int year, int month) throws SQLException {
 //		Cursor mCursor = mDb.query(true, DATABASE_TABLE, 
 //				new String[] {KEY_SPEND, KEY_CATEGORY, KEY_YEAR, KEY_MONTH, KEY_DAY,
 //						KEY_TIME, KEY_DETAIL, KEY_LOCATION_X, KEY_LOCATION_Y, 
 //						KEY_BANK, KEY_SMS_ID}, 
 //				KEY_YEAR + "=" + year + " AND " + KEY_MONTH + "=" + month,
 //				null, null, null,null,null);
 //		if(mCursor!=null) {
 //			mCursor.moveToFirst();
 //		}
 //		return mCursor;
 //	}
 	
 	public boolean updateDB(int year, int month, int day, long time, 
 			int spend, String category, String detail, String bank) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_SPEND, spend);
 		args.put(KEY_CATEGORY, category);
 		args.put(KEY_DETAIL, detail);
 		args.put(KEY_BANK, bank);
 		
 		return mDb.update(DATABASE_TABLE, args, 
 				KEY_YEAR + "=" + year + " AND " + KEY_MONTH + "=" + month + " AND " +  
 				KEY_DAY + "=" + day + " AND " + KEY_TIME + "=" + time
 				, null) > 0;
 		
 	}
 }
