 package de.tum.in.tumcampus.models;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import de.tum.in.tumcampus.Const;
 
 public class FeedManager extends SQLiteOpenHelper {
 
 	private SQLiteDatabase db;
 
 	public static int lastInserted = 0;
 
 	public String lastInfo = "";
 
 	public FeedManager(Context context, String database) {
 		super(context, database, null, Const.dbVersion);
 
 		db = this.getWritableDatabase();
 		onCreate(db);
 	}
 
 	public void importFromInternal() throws Exception {
 		File[] files = new File(Utils.getCacheDir("rss")).listFiles();
 
 		int count = Utils.getCount(db, "feeds");
 
 		db.beginTransaction();
 		for (File file : files) {
			if (file.getName().endsWith(".URL")) {
				lastInfo = file.getName();
				String name = file.getName().replace(".URL", "");
 				String url = Utils.getLinkFromUrlFile(file);
 
 				insertUpdateIntoDb(new Feed(name, url));
 			}
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 
 		lastInserted += Utils.getCount(db, "feeds") - count;
 	}
 
 	public Cursor getAllFromDb() {
 		return db.rawQuery("SELECT name, feedUrl, id as _id "
 				+ "FROM feeds ORDER BY name", null);
 	}
 
 	public boolean empty() {
 		boolean result = true;
 		Cursor c = db.rawQuery("SELECT id FROM feeds LIMIT 1", null);
 		if (c.moveToNext()) {
 			result = false;
 		}
 		c.close();
 		return result;
 	}
 
 	public List<Integer> getAllIdsFromDb() {
 		List<Integer> list = new ArrayList<Integer>();
 
 		Cursor c = db.rawQuery("SELECT id FROM feeds ORDER BY id", null);
 
 		while (c.moveToNext()) {
 			list.add(c.getInt(0));
 		}
 		c.close();
 		return list;
 	}
 
 	public void insertUpdateIntoDb(Feed n) throws Exception {
 		Utils.Log(n.toString());
 
 		if (n.name.length() == 0) {
 			throw new Exception("Invalid name.");
 		}
 		if (n.feedUrl.length() == 0) {
 			throw new Exception("Invalid feedUrl.");
 		}
 
 		Cursor c = db.rawQuery("SELECT id FROM feeds WHERE name = ?",
 				new String[] { n.name });
 
 		if (c.moveToNext()) {
 			db.execSQL("UPDATE feeds SET name=?, feedUrl=? WHERE id=?",
 					new String[] { n.name, n.feedUrl, c.getString(0) });
 
 		} else {
 			db.execSQL("INSERT INTO feeds (name, feedUrl) VALUES (?, ?)",
 					new String[] { n.name, n.feedUrl });
 		}
 	}
 
 	public void deleteFromDb(String id) {
 		db.execSQL("DELETE FROM feeds WHERE id = ?", new String[] { id });
 	}
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE IF NOT EXISTS feeds ("
 				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, feedUrl VARCHAR)");
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
