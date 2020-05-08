 package com.tihonchik.lenonhonor360.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tihonchik.lenonhonor360.models.BlogEntry;
 import com.tihonchik.lenonhonor360.util.AppUtils;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class BlogDatabase extends SQLiteOpenHelper implements SQLHelper {
 
 	public BlogDatabase(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_BLOG_TABLE);
 		db.execSQL(CREATE_IMAGES_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (DATABASE_VERSION < newVersion) {
 			db.execSQL(DROP_IMAGE_TABLE);
 			db.execSQL(DROP_BLOG_TABLE);
 			onCreate(db);
 		}
 	}
 
 	public List<BlogEntry> findAllBlogEntries() {
 		return findNBlogEntries(0);
 	}
 
 	public List<BlogEntry> findNBlogEntries(int numberOfEntries) {
 		List<BlogEntry> blogEntries = new ArrayList<BlogEntry>();
 		String selectQuery = SELECT_ENTRIES;
 
 		if (numberOfEntries != 0) {
			selectQuery += " Limit " + numberOfEntries;
 		}
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst()) {
 			do {
 				BlogEntry entry = new BlogEntry();
 				entry.setId(cursor.getInt(cursor.getColumnIndex(KEY_BLOG_ID)));
 				entry.setCreated(cursor.getString(cursor
 						.getColumnIndex(KEY_BLOG_CREATED)));
 				entry.setTitle(cursor.getString(cursor
 						.getColumnIndex(KEY_BLOG_TITLE)));
 				entry.setBlog(cursor.getString(cursor.getColumnIndex(KEY_BLOG)));
 				entry.setBlogDate(cursor.getString(cursor
 						.getColumnIndex(KEY_BLOG_DATE)));
 				blogEntries.add(entry);
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		db.close();
 		return blogEntries;
 	}
 
 	public int getNewestBlogId() {
 		int blogId = -1;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(GET_NEWEST_ID, null);
 
 		if (cursor.moveToFirst()) {
 			blogId = cursor.getInt(cursor.getColumnIndex(KEY_BLOG_ID));
 		}
 		cursor.close();
 		db.close();
 		return blogId;
 	}
 
 	public String getImageById(int id) {
 		String image = "";
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(GET_BLOG_IMAGE,
 				new String[] { Integer.toString(id) });
 
 		if (cursor.moveToFirst()) {
 			image = cursor.getString(cursor.getColumnIndex(KEY_IMAGE));
 		}
 		cursor.close();
 		db.close();
 		return image;
 	}
 
 	public List<String> getImagesById(int id) {
 		List<String> images = new ArrayList<String>();
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(GET_BLOG_IMAGES,
 				new String[] { Integer.toString(id) });
 
 		if (cursor.moveToFirst()) {
 			do {
 				images.add(cursor.getString(cursor.getColumnIndex(KEY_IMAGE)));
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		db.close();
 		return images;
 	}
 
 	public void insertNewBlogEntries(List<BlogEntry> blogEntries) {
 		if (blogEntries == null || blogEntries.size() == 0) {
 			return;
 		}
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.beginTransaction();
 
 		for (BlogEntry entry : blogEntries) {
 			try {
 				ContentValues values = new ContentValues();
 				values.put(KEY_BLOG_CREATED, entry.getCreated());
 				values.put(KEY_BLOG_TITLE, entry.getTitle());
 				values.put(KEY_BLOG, entry.getBlog());
 				values.put(KEY_BLOG_DATE, entry.getBlogDate());
 				db.insert(TABLE_BLOG_ENTRIES, null, values);
 			} catch (Exception e) {
 				Log.e("LH360", e.getLocalizedMessage(), e);
 			}
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 		db.close();
 	}
 
 	public void insertNewImage(int id, String image) {
 		if (id < 0 || image == null || "".equals(image)) {
 			return;
 		}
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.beginTransaction();
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_IMAGE_CREATED, AppUtils.getCurrentTimeStamp());
 		values.put(KEY_IMAGE, image);
 		values.put(KEY_BLOG_ID_FK, id);
 		db.insert(TABLE_IMAGES, null, values);
 
 		db.setTransactionSuccessful();
 		db.endTransaction();
 		db.close();
 	}
 }
