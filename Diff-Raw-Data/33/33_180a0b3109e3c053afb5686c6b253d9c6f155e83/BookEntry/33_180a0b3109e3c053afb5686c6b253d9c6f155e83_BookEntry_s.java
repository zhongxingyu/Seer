 package org.csie.mpp.buku.db;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class BookEntry extends Entry {
 	public static final Schema SCHEMA = new Schema(BookEntry.class);
 	
 	@Column(name="id", type=Type.INTEGER, primary=true)
 	public int id;
 	
 	@Column(name="isbn", type=Type.TEXT, notNull=true, skip=false)
 	public String isbn;
 	
 	@Column(name="time", type=Type.DATE, defaultVal="CURRENT_DATE")
 	public long time;
 	
	@Column(name="update_time", type=Type.DATE, defaultVal="0")
	public long update_time;
	
	@Column(name="title", type=Type.TEXT)
 	public String title;
 	
	@Column(name="author", type=Type.TEXT)
 	public String author;
 	
 	public boolean insert(SQLiteDatabase db) {
 		return SCHEMA.insert(db, this);
 	}
 
 	public boolean delete(SQLiteDatabase db) {
 		return SCHEMA.delete(db, "id =\"" + id + "\"");
 	}
 	
 	public static int count(SQLiteDatabase db) {
 		return SCHEMA.count(db);
 	}
 	
 	public static boolean exists(SQLiteDatabase db, String isbn) {
 		return SCHEMA.exists(db, "isbn = \"" + isbn + "\"");
 	}
 	
 	public static BookEntry[] queryAll(SQLiteDatabase db) {
 		return queryAll(db, null);
 	}
 	
 	public static BookEntry[] queryAll(SQLiteDatabase db, String orderBy) {
 		Cursor cursor = SCHEMA.queryAll(db, orderBy);
 		BookEntry[] entries = new BookEntry[cursor.getCount()];
 		for(int i = 0; cursor.moveToNext(); i++) {
 			entries[i] = new BookEntry();
 			SCHEMA.extract(cursor, entries[i]);
 		}
 		return entries;
 	}
 }
