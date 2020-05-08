 package com.andriod.tailorassist;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class CustomerTable {
 
 	public static final String KEY_NAME = "cust_name";
 	public static final String KEY_MOBILE = "cust_mobile";
 	public static final String KEY_ADDRESS = "cust_address";
 	public static final String KEY_SHIRTDETAILS = "cust_shirt_measurments";
 	public static final String KEY_PANTDETAILS = "cust_pant_measurments";
 	public static final String KEY_OTHERDETAILS = "cust_other_measurments";
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_MEASURE_DATE = "cust_measurement_date";
 	
 	public static final String TAG = "CutomerTable";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	
 	private static final String DATABASE_NAME = "customer_database";
 	public static final String DATABASE_TABLE = "customer";
 	public static final int DATABASE_VERSION = 5;
 	
 	// Database creation SQL statement
 	
 	public static final String DATABASE_CREATE = "create table "+DATABASE_TABLE + "(" + KEY_ROWID +
 			" integer primary key autoincrement, " + KEY_NAME +" text not null, "+ KEY_MOBILE + " text not null, "+
			KEY_ADDRESS +" text, " + KEY_SHIRTDETAILS + " text, "+ KEY_PANTDETAILS +" text, "+ KEY_OTHERDETAILS +" text, "+ KEY_MEASURE_DATE +" text );";
 	
 	public static String DATABASE_ALTER = "alter table "+DATABASE_TABLE+ " add column "+ KEY_MEASURE_DATE +" text " ;
 	
 	private Context mCtx;
 	
 	private static class DatabaseHelper extends SQLiteOpenHelper{
 		
 		DatabaseHelper(Context context){
 			super(context,DATABASE_NAME,null, DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db){
 			Log.i(TAG, "Creating Databese: "+DATABASE_CREATE);
 			db.execSQL(DATABASE_CREATE);
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
 			Log.w(TAG, "Upgrading database from version "+ oldVersion +" to " + newVersion + 
 					" which wil not destroy all old data");
 			//db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_CREATE);	
 			db.execSQL(DATABASE_ALTER);
 //			onCreate(db);
 			Log.w(TAG, "Successfully Upgraded database from version "+ oldVersion +" to " + newVersion );
 		}		
 	}
 	// constructor - takes the context to allow the database to be opened/created
 	
 	public CustomerTable(Context ctx){
 		this.mCtx = ctx;
 	}
 	
 	public CustomerTable open()throws SQLException {
 		Log.i(TAG, "Opening Database connections....");
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public void close(){
 		
 		mDbHelper.close();
 		Log.i(TAG, "Closing Database connections....");
 	}
 	public long addCustomer(String custName, String custMobile, String custAddress, String custShirt, String custPant, String custOther, String measureDate){
 		
 		Log.i(TAG, "Inserting records...");
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_NAME,custName);
 		initialValues.put(KEY_MOBILE,custMobile);
 		initialValues.put(KEY_ADDRESS,custAddress);
 		initialValues.put(KEY_SHIRTDETAILS,custShirt);
 		initialValues.put(KEY_PANTDETAILS,custPant);
 		initialValues.put(KEY_OTHERDETAILS,custOther);
 		initialValues.put(KEY_MEASURE_DATE,measureDate);
 		
 		return mDb.insert(DATABASE_TABLE, null, initialValues);		
 	}
 	
 	public boolean deleteCustomer(long rowId){
 		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0 ;
 	}
 	
 
 
 	public Cursor fetchAllCutomers(){
 		
 		Cursor mCursor = 
 				mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,KEY_MOBILE, KEY_ADDRESS, KEY_SHIRTDETAILS, KEY_PANTDETAILS, KEY_OTHERDETAILS},
 						null, null, null, null,KEY_NAME, null);
 		if (mCursor !=null){
 			mCursor.moveToFirst();
 		}
 		return mCursor;	
 	}
 	
 	public Cursor fetchCustomerById(long custId)throws SQLException {
 		
 		Cursor mCursor = 
 				 mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,KEY_NAME,KEY_MOBILE, KEY_ADDRESS, KEY_SHIRTDETAILS, KEY_PANTDETAILS, KEY_OTHERDETAILS,KEY_MEASURE_DATE},
 							KEY_ROWID + "=" + custId, null, null, null, null, null);		
 		if (mCursor !=null){
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	
 	}
 	
 	public Cursor fetchCustomerByMob(String mobNum)throws SQLException {
 			
 			Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,KEY_MOBILE, KEY_ADDRESS, KEY_SHIRTDETAILS, KEY_PANTDETAILS, KEY_OTHERDETAILS},
 							KEY_MOBILE+ "=" + mobNum, null, null, null, null, null);
 			if (mCursor !=null){
 				mCursor.moveToFirst();
 			}
 			return mCursor;
 		
 	}
 	
 	public Cursor fetchCustomerByName(String custName)throws SQLException {
 		
 		Cursor mCursor = 
 				mDb.query(true, DATABASE_TABLE, new String[] {KEY_NAME,KEY_ROWID,KEY_MOBILE, KEY_ADDRESS, KEY_SHIRTDETAILS, KEY_PANTDETAILS, KEY_OTHERDETAILS},
 						KEY_NAME + " like '%" + custName +"%'", null, null, null,"'"+ KEY_NAME+"'ASC", null);
 		if (mCursor !=null){
 			mCursor.moveToFirst();
 		}
 		return mCursor;	
 	}
 
 	public Cursor searchCustomer(String custValue)throws SQLException {
 		if(Util.isNumeric(custValue)){
 			Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,KEY_MOBILE},
 					KEY_MOBILE+ "=" + custValue+" OR "+KEY_ROWID + "=" + custValue+" OR "+KEY_NAME + " like '%" + custValue +"%'", null, null, null, null, null);
 			if (mCursor !=null){
 				mCursor.moveToFirst();
 			}
 			return mCursor;	
 		}else{
 			return fetchCustomerByName(custValue);
 		}
 		
 	}
 	public boolean updateCustomer(long custId, String custName, String custMobile, String custAddress, String custShirt, 
 			String custPant, String custOther, String measureDate){
 		
 		ContentValues args = new ContentValues();
 		args.put(KEY_NAME, custName);
 		args.put(KEY_MOBILE, custMobile);
 		args.put(KEY_ADDRESS, custAddress);
 		args.put(KEY_SHIRTDETAILS, custShirt);
 		args.put(KEY_PANTDETAILS, custPant);
 		args.put(KEY_OTHERDETAILS, custOther);
 		args.put(KEY_MEASURE_DATE, measureDate);
 		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + custId, null)>0;
 		
 	}	
 	public boolean updateCustomerProfile(long custId, String custName, String custMobile, String custAddress){
 		
 		ContentValues args = new ContentValues();
 		args.put(KEY_NAME, custName);
 		args.put(KEY_MOBILE, custMobile);
 		args.put(KEY_ADDRESS, custAddress);
 
 		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + custId, null)>0;
 		
 	}	
 	
 }
