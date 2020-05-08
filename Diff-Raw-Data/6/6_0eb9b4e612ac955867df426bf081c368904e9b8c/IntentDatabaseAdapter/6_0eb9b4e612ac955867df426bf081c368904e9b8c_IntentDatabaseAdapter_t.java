 // Inspired by http://www.vogella.de/articles/AndroidSQLite/article.html
 
 package jp.peo3.android.intentspooler.database;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.text.TextUtils;
 
 public class IntentDatabaseAdapter {
 	private Context context;
 	private SQLiteDatabase db;
 	private IntentDatabaseHelper dbHelper;
 
 	public IntentDatabaseAdapter(Context context) {
 		this.context = context;
 	}
 
 	public IntentDatabaseAdapter open() throws SQLException {
 		dbHelper = new IntentDatabaseHelper(context);
 		db = dbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public long createIntentEntry(Intent intent) {
 		long ret;
 		ContentValues values;
     	Date date = new Date();
     	
 		values = createIntentContentValues(date, intent);
 		ret = db.insert(IntentTables.DB_TABLE_INTENT, null, values);
 		if (ret < 0)
 			return ret;
     	Set<String> categories = intent.getCategories();
     	if (categories != null) {
     		Iterator<String> it = categories.iterator();
     		while (it.hasNext()) {
     			values = createCategoryContentValues(date, ret, it.next());
     			ret = db.insert(IntentTables.DB_TABLE_CATEGORY, null, values);
     			if (ret < 0)
     				return ret; // FIXME
     		}
     		values.put("nbCategories", categories.size());
     	}
 
     	return ret;
 	}
 
 	/*
 	 * Format date for SQLite's datetime
 	 */
 	private String formateDate(Date date) {
     	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	return dateFormat.format(date);
 	}
 
 	public boolean deleteIntent(long rowId) {
 		long ret;
 		ret = db.delete(IntentTables.DB_TABLE_INTENT, 
 				IntentTables.KEY_ROWID + "=" + rowId, null);
 		if (ret < 0)
 			return ret > 0;
 		db.delete(IntentTables.DB_TABLE_CATEGORY, 
 				IntentTables.KEY_ROWID + "=" + rowId, null);
 		return ret > 0;
 	}
 
 	public Cursor fetchAllIntents() {
 		return db.query(IntentTables.DB_TABLE_INTENT,
 				new String[] { IntentTables.KEY_ROWID, 
 					IntentTables.KEY_TIMESTAMP,
 					IntentTables.KEY_SUMMARY},
 				null, null, null, null, null);
 	}
 
 	public Intent getIntent(Integer rowid, String timestamp) {
 		Intent intent = new Intent();
 		Cursor cur = fetchIntent(rowid);
 		//startManagingCursor(cur);
 		intent.setAction(cur.getString(cur.getColumnIndex(IntentTables.KEY_ACTION)));
 		intent.setFlags(cur.getInt(cur.getColumnIndex(IntentTables.KEY_FLAGS)));
 		Integer nbCategories = cur.getInt(cur.getColumnIndex(IntentTables.KEY_NB_CATEGORIES));
 		String data = cur.getString(cur.getColumnIndex(IntentTables.KEY_DATA));
 		if (data != null) {
 			intent.setData(Uri.parse(data));
 		}
 		
 		byte[] extrasBytes = cur.getBlob(cur.getColumnIndex(IntentTables.KEY_EXTRAS));
 		final Parcel parcel = Parcel.obtain();
 		parcel.unmarshall(extrasBytes, 0, extrasBytes.length);
 		parcel.setDataPosition(0);
 		Bundle extras = (Bundle) parcel.readBundle();
 		parcel.recycle();
 		intent.putExtras(extras);
 
 		String type = cur.getString(cur.getColumnIndex(IntentTables.KEY_TYPE));
 		if (type != null) {
 			intent.setType(type);
 		}
 		cur.close();
 		if (nbCategories > 0) {
 			cur = fetchCategories(rowid);
 			if (cur != null && cur.moveToFirst()) {
 				do {
 					String category = cur.getString(cur.getColumnIndex(IntentTables.KEY_CATEGORY));
 					intent.addCategory(category);
 				} while(cur.moveToNext());
 				cur.close();
 			}
 		}
 		return intent;
 	}
 
 	private ContentValues createIntentContentValues(Date date, Intent intent) {
 		ContentValues values = new ContentValues();
 
 		values.put(IntentTables.KEY_TIMESTAMP, formateDate(date));
 		values.put(IntentTables.KEY_ACTION, intent.getAction());
 		values.put(IntentTables.KEY_FLAGS, intent.getFlags());
     	Set<String> categories = intent.getCategories();
     	if (categories != null) {
     		values.put(IntentTables.KEY_NB_CATEGORIES, categories.size());
     	} else {
     		values.put(IntentTables.KEY_NB_CATEGORIES, 0);
     	}
     	
     	Bundle extras = intent.getExtras();
     	if (extras != null) {
     		values.put(IntentTables.KEY_NB_EXTRAS, extras.size());
     		// Choose a summary from Extra values
     		Iterator<String> it = extras.keySet().iterator();
     		String text = null;
     		String subject = null;
     		ArrayList<String> items = new ArrayList<String>();
     		while (it.hasNext()) {
     			String key = it.next();
     			if (key.contains("SUBJECT")) {
     				subject = extras.get(key).toString();
     			} else if (key.contains("TEXT")) {
     				text = extras.get(key).toString();
     			} else {
    				Object val = extras.get(key);
    				if (val == null)
    					items.add("(null)");
    				else
    					items.add(val.toString());
     			}
     		}
     		// The order: subject, text, others
 			if (text != null)
 				items.add(0, text);
 			if (subject != null)
 				items.add(0, subject);
 			values.put(IntentTables.KEY_SUMMARY, TextUtils.join("\n", items));
     	} else {
     		values.put(IntentTables.KEY_NB_EXTRAS, 0);
     		// Is there a better alternative?
     		values.put(IntentTables.KEY_SUMMARY, intent.getAction());
     	}
 
 		final Parcel parcel = Parcel.obtain();
 		intent.getExtras().writeToParcel(parcel, 0);
 		byte[] extrasBytes = parcel.marshall();
 		parcel.recycle();
 		//Log.v("size", Integer.toString(extrasBytes.length));
 		values.put(IntentTables.KEY_EXTRAS, extrasBytes);
 		
 		values.put(IntentTables.KEY_DATA, intent.getDataString());
 		values.put(IntentTables.KEY_TYPE, intent.getType());
 		values.put(IntentTables.KEY_SCHEME, intent.getScheme());
 
 		return values;
 	}
 
 	private ContentValues createCategoryContentValues(Date date, long rowId, String category) {
 		ContentValues values = new ContentValues();
 		values.put(IntentTables.KEY_ROWID, rowId);
 		values.put(IntentTables.KEY_TIMESTAMP, formateDate(date));
 		values.put(IntentTables.KEY_CATEGORY, category);
 		return values;
 	}
 	
 	private Cursor fetchIntent(Integer rowid) throws SQLException {
 		Cursor mCursor = db.query(true, IntentTables.DB_TABLE_INTENT, 
 				IntentTables.DATABASE_KEYS,
 				IntentTables.KEY_ROWID + "=" + rowid,
 				null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 	
 	private Cursor fetchCategories(Integer rowid) throws SQLException {
 		Cursor mCursor = db.query(true, IntentTables.DB_TABLE_CATEGORY, 
 				IntentTables.DATABASE_CATEGORY_KEYS,
 				IntentTables.KEY_ROWID + "=" + rowid,
 				null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 }
