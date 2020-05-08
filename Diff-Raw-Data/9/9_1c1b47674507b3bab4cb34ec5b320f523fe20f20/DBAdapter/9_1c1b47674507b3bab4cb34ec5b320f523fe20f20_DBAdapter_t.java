 package shu.journal;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBAdapter { 
 	public static final String KEY_ROWID = "_id";
     public static final String KEY_USERNAME = "username";
     public static final String KEY_PASSWORD = "password";
     public static final String KEY_ACCOUNTLOCKED = "account_locked";
     public static final String KEY_LOCATION = "location";
     public static final String KEY_FNAME = "f_name";
     public static final String KEY_LNAME = "l_name";
     public static final String KEY_DATE = "date";
     public static final String KEY_JENTRY = "j_entry";
     public static final String KEY_USERID = "user_id";
     public static final String KEY_SQUESTION1 = "s_question1";
     public static final String KEY_SQUESTION2 = "s_question2";
     public static final String KEY_SQUESTION3 = "s_question3";
     public static final String KEY_SANSWER1 = "s_answer1";
     public static final String KEY_SANSWER2 = "s_answer2";
     public static final String KEY_SANSWER3 = "s_answer3";
 
     
     private static final String TAG = "DBAdapter";
     
     private static final String DATABASE_NAME = "journal";
     private static final String USERS_TABLE = "users";
     private static final String JOURNAL_TABLE = "journal_entries";
     private static final int DATABASE_VERSION = 2;
 
     private static final String USERS_CREATE =
 			"create table users (_id integer primary key autoincrement, "
 					+ "username text not null, password text not null, account_locked integer not null," 
 					+ "location text not null, " 
 					+ "f_name text not null, l_name text not null, "
 					+ "s_question1 text not null, " 
 					+ "s_question2 text not null, " 
 					+ "s_question3 text not null, " 
 					+ "s_answer1 text not null, " 
 					+ "s_answer2 text not null, " 
 					+ "s_answer3 text not null, " 
 					+ "date text not null); " ;
 
 	private static final String JOURNAL_CREATE =
 			"create table journal_entries (_id integer primary key autoincrement, "
 					+ "user_id integer not null, "
 					+ "date text not null, j_entry text not null);";
         
     private final Context context;
     
     private DatabaseHelper DBHelper;
     private SQLiteDatabase db;
 
     public DBAdapter(Context ctx) 
     {
         this.context = ctx;
         DBHelper = new DatabaseHelper(context);
     }
         
     private static class DatabaseHelper extends SQLiteOpenHelper 
     {
         DatabaseHelper(Context context) 
         {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) 
         {
             db.execSQL(USERS_CREATE);
             db.execSQL(JOURNAL_CREATE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                               int newVersion) 
         {
             Log.w(TAG, "Upgrading database from version " + oldVersion 
                   + " to "
                   + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS titles");
             onCreate(db);
         }
     }    
   //---opens the database---
     public DBAdapter open() throws SQLException 
     {
         db = DBHelper.getWritableDatabase();
         return this;
     }
 
     //---closes the database---    
     public void close() 
     {
         DBHelper.close();
     }
     
     public boolean checkUserExists(String username)
     {
     	Cursor mCursor =
 				db.query(true, USERS_TABLE, new String[] {
 						KEY_ROWID},
 						KEY_USERNAME + "=" + username,
 						null, 
 						null, 
 						null, 
 						null, null);
     	
     	if(mCursor != null && mCursor.getCount() == 1)
     		return true;
     	else 
     		return false;
     }
     
     //---insert a user into the users table---
     public long insertUser(String username, String password, String location,
 			String f_name,String l_name,String s_question1, 
 			String s_question2, String s_question3,
 			String s_answer1, String s_answer2, String s_answer3) 
 	{
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USERNAME, username);
 		initialValues.put(KEY_PASSWORD, password);
 		initialValues.put(KEY_ACCOUNTLOCKED, 0);
 		initialValues.put(KEY_LOCATION,location);
 		initialValues.put(KEY_FNAME, f_name);
 		initialValues.put(KEY_LNAME, l_name);
 		initialValues.put(KEY_SQUESTION1, s_question1);
 		initialValues.put(KEY_SQUESTION2, s_question2);
 		initialValues.put(KEY_SQUESTION3, s_question3);
 		initialValues.put(KEY_SANSWER1, s_answer1);
 		initialValues.put(KEY_SANSWER2, s_answer2);
 		initialValues.put(KEY_SANSWER3, s_answer3);
 		initialValues.put(KEY_DATE, "Test Date");
 
 		return db.insert(USERS_TABLE, null, initialValues);
 	}
     //---updates user password---
 	public boolean updatePassword(long user_id, String password) 
 	{
 	    ContentValues args = new ContentValues();
 	    args.put(KEY_PASSWORD, password);
 	    return db.update(USERS_TABLE, args, 
 	                     KEY_ROWID + "=" + user_id, null) > 0;
 	}
 
 	//--check if username and password match--
 	public long checkPassword (String username, String password) throws SQLException 
 	{
 		Cursor mCursor =
 				db.query(true, USERS_TABLE, new String[] {
 						KEY_ROWID},
 						KEY_USERNAME + "='" + username + "' AND " + KEY_PASSWORD + "='" + password+"'",
 						null, 
 						null, 
 						null, 
 						null, null);
 		
		if (mCursor != null && mCursor.getCount() == 1) {
 			mCursor.moveToFirst();
 			return mCursor.getLong(0);
 		}
 		else
 			return -1;
 	}
 	
 	public boolean lockAccount(String username)
 	{
 		ContentValues args = new ContentValues();
         args.put(KEY_ACCOUNTLOCKED, 1);
         return db.update(USERS_TABLE, args, 
                          KEY_USERNAME + "='" + username+"'", null) > 0;
 	}
 	
 	public boolean unlockAccount(String username)
 	{
 		ContentValues args = new ContentValues();
         args.put(KEY_ACCOUNTLOCKED, 0);
         return db.update(USERS_TABLE, args, 
                          KEY_USERNAME + "='" + username+"'", null) > 0;
 	}
 	
 	public Cursor getSecQuestions(String username)
 	{
 		Cursor mCursor =
 				db.query(true, USERS_TABLE, new String[] {
 						KEY_SQUESTION1,
 						KEY_SQUESTION2,
 						KEY_SQUESTION3},
 						KEY_USERNAME + "='" + username+"'",
 						null, 
 						null, 
 						null, 
 						null, null);
 		
 		
 		return mCursor;
 	}
 	
 	public boolean getLockStatus(String username)
 	{
 		Cursor mCursor =
 				db.query(true, USERS_TABLE, new String[] {
 						KEY_ROWID,
 						KEY_ACCOUNTLOCKED},
 						KEY_USERNAME + "='" + username+"'",
 						null, 
 						null, 
 						null, 
 						null, null);
 		
 		
 		if(mCursor != null)
 		{
 			mCursor.moveToFirst();
 			return mCursor.getInt(mCursor.getColumnIndex(KEY_ACCOUNTLOCKED)) == 1;
 		}
 		else
 			return false;
 	}
     
     //--check if security answer matches--
     public boolean checkSecAnswer (String s_answer1, String s_answer2,String s_answer3, String username) throws SQLException 
     {
         Cursor mCursor =
                 db.query(true,USERS_TABLE, new String[]{KEY_SANSWER1,KEY_SANSWER2,KEY_SANSWER3},
                 		KEY_USERNAME + "=" + username,
                 		null, 
                 		null, 
                 		null, 
                 		null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         
         String answer1 = mCursor.getString(mCursor.getColumnIndex(KEY_SANSWER1)).toLowerCase();
         String answer2 = mCursor.getString(mCursor.getColumnIndex(KEY_SANSWER2)).toLowerCase();
         String answer3 = mCursor.getString(mCursor.getColumnIndex(KEY_SANSWER3)).toLowerCase();
         
         return (s_answer1.toLowerCase().equals(answer1) &&
         		s_answer2.toLowerCase().equals(answer2) &&
         		s_answer3.toLowerCase().equals(answer3));
     }
     
     //--insert a journal entry--
 	public long insertPage(long user_id, String j_entry) 
 	{
 		Date date = new Date();
 		String strDate = date.getDate() + "/" + date.getMonth() + "/" + date.getYear();
 		
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USERID, user_id);
 		initialValues.put(KEY_DATE, strDate);
 		initialValues.put(KEY_JENTRY, j_entry);
 		
 		return db.insert(JOURNAL_TABLE, null, initialValues);
 	}
 	
     //---deletes a particular page---
     public boolean deletePage(long rowId) 
     {
         return db.delete(JOURNAL_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
 
     //---retrieves all the titles---
     public Cursor getAllUserPages(long user_id) 
     {
     	 Cursor mCursor = db.query(true, JOURNAL_TABLE, new String[] {
         		KEY_ROWID,
         		KEY_DATE,
         		KEY_JENTRY,},KEY_USERID + " = " + user_id,null,null, 
                 null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
  //---retrieves a particular journal entry---
     public Cursor getJournalPage(long rowId) throws SQLException 
     {
         Cursor mCursor =
                 db.query(true, JOURNAL_TABLE, new String[] {
                 		KEY_ROWID,
                 		KEY_DATE},
                 		KEY_ROWID + "=" + rowId,
                 		null, 
                 		null, 
                 		null, 
                 		null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
   //---retrieves a particular user entry---
     public Cursor getUserById(long user_id) throws SQLException 
     {
         Cursor mCursor =
                 db.query(true, USERS_TABLE, new String[] {
                 		KEY_ROWID,
                 		KEY_USERNAME,
                 		KEY_PASSWORD,
                 		KEY_FNAME,
                 		KEY_LNAME,
                 		KEY_LOCATION,
                 		KEY_SQUESTION1,
                 		KEY_SQUESTION2,
                 		KEY_SQUESTION3,
                 		KEY_SANSWER1,
                 		KEY_SANSWER2,
                 		KEY_SANSWER3},
                 		KEY_ROWID + "=" + user_id,
                 		null, 
                 		null, 
                 		null, 
                 		null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
   //--retrieves a particular journal page---
     public Cursor getPageById(long entry_id) throws SQLException 
     {
         Cursor mCursor =
                 db.query(true, JOURNAL_TABLE, new String[] {
                 		KEY_ROWID,
                 		KEY_DATE,
                 		KEY_JENTRY},
                 		KEY_ROWID + "=" + entry_id,
                 		null, 
                 		null, 
                 		null, 
                 		null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
 
   //---updates an existing journal page---
     public boolean updatePage(long entry_id, String j_entry) 
     {
         ContentValues args = new ContentValues();
         args.put(KEY_JENTRY, j_entry);
         return db.update(JOURNAL_TABLE, args, 
                          KEY_ROWID + "=" + entry_id, null) > 0;
     }
   //--- get first record ---
     public Cursor getFirstJournalEntry()
     {
     	Cursor mCursor =
             db.query(true, JOURNAL_TABLE, new String[] {
             		KEY_ROWID,
             		KEY_DATE,
             		KEY_JENTRY},
             		null,
             		null, 
             		null, 
             		null, 
             		null, "1");
     if (mCursor != null) {
         mCursor.moveToFirst();
     }
     return mCursor;
     }
   //--- get first user ---
     public Cursor getFirstUser()
     {
     	Cursor mCursor =
             db.query(true, USERS_TABLE, new String[] {
                		KEY_ROWID,
             		KEY_USERNAME,
             		KEY_PASSWORD,
             		KEY_FNAME,
             		KEY_LNAME,
             		KEY_LOCATION,
             		KEY_SQUESTION1,
             		KEY_SQUESTION2,
             		KEY_SQUESTION3,
             		KEY_SANSWER1,
             		KEY_SANSWER2,
             		KEY_SANSWER3},
             		null,
             		null, 
             		null, 
             		null, 
             		null, "1");
     if (mCursor != null) {
         mCursor.moveToFirst();
     }
     return mCursor;
     }
 }
