 package com.framework.leopardus.utils.storage;
 
 import java.io.File;
 
 import com.framework.leopardus.utils.RESTSimpleHelper;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.util.Log;
 
 public class RESTInternalStorage extends SQLiteOpenHelper {
 
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 1;
 
 	// Database Name
 	private static final String DATABASE_NAME = "rest_db";
 
 	// Contacts table name
 	private static final String TABLE_CONTENTS = "rest_contents";
 
 	// Contacts Table Columns names
 	private static final String KEY_ID = "id";
 	private static final String KEY_URL = "url";
 	private static final String KEY_CONTENT = "content";
 
 	// ///////////////////////////////////////////////////////
 	public static final String BASE_PATH;
 	public static final String sep = System.getProperty("file.separator", "/");
 	public static final String FULL_PATH;
 	static {
 		BASE_PATH = Environment.getExternalStorageDirectory().toString() + sep
 				+ "leopardus" + sep + "databases";
 		File f = new File(BASE_PATH);
 		if (!f.exists()) {
 			f.mkdirs();
 		}
 		FULL_PATH = BASE_PATH + sep + DATABASE_NAME;
 	}
 
 	public RESTInternalStorage(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTENTS + "("
 				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_URL + " TEXT,"
 				+ KEY_CONTENT + " TEXT" + ")";
 		db.execSQL(CREATE_CONTACTS_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS);
 		onCreate(db);
 	}
 
 	// /////////////// CRUD OPERATIONS //////////////////
 	public void addContent(String url, String content) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(KEY_URL, url); // Contact Name
 		values.put(KEY_CONTENT, content); // Contact Phone Number
 		// Inserting Row
 		db.insert(TABLE_CONTENTS, null, values);
 		db.close(); // Closing database connection
 		if (RESTSimpleHelper.INFO)
 			Log.i("Leopardus", "Content is saved succefully");
 	}
 
 	// Getting single content
 	public String getContent(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.query(TABLE_CONTENTS, new String[] { KEY_ID,
 				KEY_URL, KEY_CONTENT }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 			String content = cursor.getString(2);
 			return content;
 		} else {
 			return null;
 		}
 	}
 
 	// Getting single content
 	public String getContent(String url) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.query(TABLE_CONTENTS, new String[] { KEY_URL,
 				KEY_CONTENT }, KEY_URL + "=?", new String[] { url }, null,
 				null, null, null);
 		if (cursor != null && cursor.getCount() > 0) {
 			cursor.moveToFirst();
 			String content = cursor.getString(1);
 			return content;
 		} else {
 			return null;
 		}
 	}
 
 	// Getting single content
 	public int getUrlId(String url) {
 		try {
 			SQLiteDatabase db = this.getReadableDatabase();
 			Cursor cursor = db.query(TABLE_CONTENTS, new String[] { KEY_ID },
 					KEY_URL + "=?", new String[] { url }, null, null, null,
 					null);
 			if (cursor != null && cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				String id = cursor.getString(0);
 				return Integer.parseInt(id);
 			} else {
 				return -1;
 			}
 		} catch (Exception e) {
 			if (RESTSimpleHelper.INFO)
 				Log.e("leopardus",
 						"RESTInternalStorageError(getUrlId): " + e.getMessage());
 			return -1;
 		}
 	}
 
 	public int updateContent(String url, String content) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		String storedContent = getContent(url);
 		if (storedContent != null) {
 			if (!content.equals(storedContent)) {
 				int id = getUrlId(url);
 				ContentValues values = new ContentValues();
 				values.put(KEY_CONTENT, content);
 				// updating row
 				int stts = db.update(TABLE_CONTENTS, values, KEY_ID + " = ?",
 						new String[] { String.valueOf(id) });
 				if (RESTSimpleHelper.INFO)
 					Log.i("Leopardus", "URL " + url + " Updated succefully");
 				return stts;
 			} else
 				return getUrlId(url);
 		} else {
 			addContent(url, content);
 			return getUrlId(url);
 		}
 	}
 
 	// Deleting single contact
 	public void deleteContact(String url) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_CONTENTS, KEY_URL + " = ?",
 				new String[] { String.valueOf(url) });
 		db.close();
 	}
 	// //////////////////////////////////////////////////
 
 }
