 package de.fhb.maus.android.todolist.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 
 public class TodoDatabaseAdapter {
 
 	// Database fields
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_DATE = "date";
 	public static final String KEY_CATEGORY = "category";
 	public static final String KEY_SUMMARY = "summary";
 	public static final String KEY_DONE = "done";
 	public static final String KEY_DESCRIPTION = "description";
 	public static final String KEY_CONTACTID = "_cid";
 	public static final String KEY_DISPLAYNAME = "displayName";
 	public static final String KEY_EMAIL = "email";
 	public static final String KEY_PHONENUMBER = "phonenumber";
 	private static final String DATABASE_TABLE_TODO = "todo";
 	private static final String DATABASE_TABLE_HAT = "hat";
 	private static final String DATABASE_TABLE_CONTACTS = "contacttable";
 
 	private Context mContext;
 	private SQLiteDatabase mDatabase;
 	private TodoDatabaseHelper mDbHelper;
 	private SQLiteQueryBuilder querybuilder;
 
 	public TodoDatabaseAdapter(Context context) {
 		this.mContext = context;
 	}
 
 	public TodoDatabaseAdapter open() throws SQLException {
 		mDbHelper = new TodoDatabaseHelper(mContext);
 		mDatabase = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	/**
 	 * Create a new todo If the todo is successfully created return the new
 	 * rowId for that note, otherwise return a -1 to indicate failure.
 	 */
 	public long createTodo(String date, String category, boolean done,
 			String summary, String description) {
 		ContentValues initialValues = createContentValues(date, category, done,
 				summary, description);
 		return mDatabase.insert(DATABASE_TABLE_TODO, null, initialValues);
 	}
 
 	public long setContact(String contactId, String rowId) {
 		ContentValues updateValues = createContentValues(contactId, rowId);
 		return mDatabase.insert(DATABASE_TABLE_HAT, null, updateValues);
 	}
 
 	// public long setContact(String contactId, String displayName, String
 	// email, String phonenumber){
 	// ContentValues updateValues =
 	// createContentValues(contactId,displayName,email,phonenumber);
 	// return mDatabase.insert(DATABASE_TABLE_CONTACTS,null,updateValues);
 	// }
 
 	/**
 	 * Update the todo
 	 */
 	public boolean updateTodo(long rowId, String date, String category,
 			boolean done, String summary, String description) {
 		ContentValues updateValues = createContentValues(date, category, done,
 				summary, description);
 		return mDatabase.update(DATABASE_TABLE_TODO, updateValues, KEY_ROWID
 				+ "=" + rowId, null) > 0;
 	}
 
 	/**
 	 * Deletes todo
 	 */
 	public boolean deleteTodo(long rowId) {
 		return mDatabase.delete(DATABASE_TABLE_TODO, KEY_ROWID + "=" + rowId,
 				null) > 0;
 	}
 
 	public boolean deleteContact(String rowId, String contactId) {
 		return mDatabase.delete(DATABASE_TABLE_HAT, KEY_ROWID + "=? AND "
 				+ KEY_CONTACTID + "=?", new String[]{rowId, contactId}) > 0;
 	}
 
 	public Cursor fetchContacts(String rowId) throws SQLException {
 		Cursor mCursor = mDatabase.query(true, DATABASE_TABLE_HAT,
 				new String[]{KEY_ROWID, KEY_CONTACTID},
 				KEY_ROWID + "=" + rowId, null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 
 	public Cursor fetchContactsWithTodo() {
 		Cursor mCursor = mDatabase
 				.query(true, DATABASE_TABLE_HAT, new String[]{KEY_CONTACTID},
 						null, null, null, null, null, null);
 		return mCursor;
 	}
 
 	public Cursor fetchAllContacts() {
 		return mDatabase.query(DATABASE_TABLE_HAT, new String[]{KEY_ROWID,
 				KEY_CONTACTID}, null, null, null, null, null);
 	}
 
 	/**
 	 * Return a Cursor over the list of all todo in the database
 	 * 
 	 * @return Cursor over all notes
 	 */
	public Cursor fetchAllTodosOrderByDone() {
 		return mDatabase.query(DATABASE_TABLE_TODO,
 				new String[]{KEY_ROWID, KEY_DATE, KEY_CATEGORY, KEY_DONE,
 						KEY_SUMMARY, KEY_DESCRIPTION}, null, null, null, null,
				KEY_DONE);
 	}
	public Cursor fetchAllTodosOrderByDate() {
 		return mDatabase.query(DATABASE_TABLE_TODO,
 				new String[]{KEY_ROWID, KEY_DATE, KEY_CATEGORY, KEY_DONE,
 						KEY_SUMMARY, KEY_DESCRIPTION}, null, null, null, null,
				KEY_DATE);
 	}
	public Cursor fetchAllTodosOrderByCategory() {
 		return mDatabase.query(DATABASE_TABLE_TODO,
 				new String[]{KEY_ROWID, KEY_DATE, KEY_CATEGORY, KEY_DONE,
 						KEY_SUMMARY, KEY_DESCRIPTION}, null, null, null, null,
				KEY_CATEGORY);
 	}
 
 	/**
 	 * Return a Cursor positioned at the defined todo
 	 */
 	public Cursor fetchTodo(long rowId) throws SQLException {
 		Cursor mCursor = mDatabase.query(true, DATABASE_TABLE_TODO,
 				new String[]{KEY_ROWID, KEY_DATE, KEY_CATEGORY, KEY_DONE,
 						KEY_SUMMARY, KEY_DESCRIPTION}, KEY_ROWID + "=" + rowId,
 				null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 
 	private ContentValues createContentValues(String date, String category,
 			boolean done, String summary, String description) {
 		ContentValues values = new ContentValues();
 		values.put(KEY_DATE, date);
 		values.put(KEY_CATEGORY, category);
 		values.put(KEY_DONE, done);
 		values.put(KEY_SUMMARY, summary);
 		values.put(KEY_DESCRIPTION, description);
 		return values;
 	}
 	private ContentValues createContentValues(String contactId, String rowId) {
 		ContentValues values = new ContentValues();
 		values.put(KEY_ROWID, rowId);
 		values.put(KEY_CONTACTID, contactId);
 		return values;
 	}
 
 }
