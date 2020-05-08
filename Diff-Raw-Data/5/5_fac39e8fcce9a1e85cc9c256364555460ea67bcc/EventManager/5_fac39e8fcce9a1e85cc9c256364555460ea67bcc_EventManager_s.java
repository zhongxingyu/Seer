 package de.tum.in.tumcampus.models;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class EventManager extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 
 	private SQLiteDatabase db;
 
 	public EventManager(Context context, String database) {
 		super(context, database, null, DATABASE_VERSION);
 
 		db = this.getWritableDatabase();
 		onCreate(db);
 	}
 
 	public void downloadFromExternal(boolean force) throws Exception {
 
 		if (!force && !SyncManager.needSync(db, this, 86400)) {
 			return;
 		}
 		String baseUrl = "https://graph.facebook.com/162327853831856/events?access_token=";
 		String token = URLEncoder
 				.encode("141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU");
 
 		JSONArray jsonArray = Utils.downloadJson(baseUrl + token).getJSONArray(
 				"data");
 
 		String eventUrl = "http://graph.facebook.com/";
 
 		List<JSONObject> list = new ArrayList<JSONObject>();
 		for (int i = 0; i < jsonArray.length(); i++) {
 			if (i > 24) {
 				break;
 			}
 			String eventId = jsonArray.getJSONObject(i).getString("id");
 			list.add(Utils.downloadJson(eventUrl + eventId));
 		}
 
 		cleanupDb();
 		db.beginTransaction();
 		for (int i = 0; i < list.size(); i++) {
 			replaceIntoDb(getFromJson(list.get(i)));
 		}
 		SyncManager.replaceIntoDb(db, this);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public Cursor getNextFromDb() {
 		return db.rawQuery(
 				"SELECT image, name, strftime('%w', start) as weekday, "
 						+ "strftime('%d.%m.%Y %H:%M', start) as start_de, "
 						+ "strftime('%H:%M', end) as end_de, "
 						+ "location, id as _id "
						+ "FROM events WHERE end > datetime() "
 						+ "ORDER BY start ASC LIMIT 25", null);
 	}
 
 	public Cursor getPastFromDb() {
 		return db.rawQuery(
 				"SELECT image, name, strftime('%w', start) as weekday, "
 						+ "strftime('%d.%m.%Y %H:%M', start) as start_de, "
 						+ "strftime('%H:%M', end) as end_de, "
 						+ "location, id as _id "
						+ "FROM events WHERE end <= datetime() "
 						+ "ORDER BY start DESC LIMIT 25", null);
 	}
 
 	public Cursor getFromDb(String id) {
 		return db.rawQuery(
 				"SELECT image, name, strftime('%w', start) as weekday, "
 						+ "strftime('%d.%m.%Y %H:%M', start) as start_de, "
 						+ "strftime('%H:%M', end) as end_de, "
 						+ "location, description, link, id as _id "
 						+ "FROM events WHERE id = ?", new String[] { id });
 	}
 
 	/**
 	 * 
 	 * 
 	 * Example JSON: e.g. { "id": "166478443419659", "owner": { "name":
 	 * "TUM Campus App for Android", "category": "Software", "id":
 	 * "162327853831856" }, "name":
 	 * "R\u00fcckmeldung f\u00fcr Wintersemester 2011/12", "description": "..."
 	 * , "start_time": "2011-08-15T00:00:00", "end_time": "2011-08-15T03:00:00",
 	 * "location": "TU M\u00fcnchen", "privacy": "OPEN", "updated_time":
 	 * "2011-06-25T06:26:14+0000" }
 	 * 
 	 * @param json
 	 * @return Event
 	 * @throws JSONException
 	 */
 	public static Event getFromJson(JSONObject json) throws Exception {
 
 		String eventId = json.getString("id");
 
 		String picture = "http://graph.facebook.com/" + eventId
 				+ "/Picture?type=large";
 
 		String target = Utils.getCacheDir("events/cache") + eventId + ".jpg";
 		Utils.downloadFileThread(picture, target);
 
 		String description = "";
 		if (json.has("description")) {
 			description = json.getString("description");
 		}
 		String location = "";
 		if (json.has("location")) {
 			location = json.getString("location");
 		}
 		String link = "";
 		if (json.has("link")) {
 			link = json.getString("link");
 		}
 
 		return new Event(eventId, json.getString("name"),
 				Utils.getDateTime(json.getString("start_time")),
 				Utils.getDateTime(json.getString("end_time")), location,
 				description, link, target);
 	}
 
 	public void replaceIntoDb(Event e) throws Exception {
 
 		if (e.id.length() == 0) {
 			throw new Exception("Invalid id.");
 		}
 		if (e.name.length() == 0) {
 			throw new Exception("Invalid name.");
 		}
 		db.execSQL(
 				"REPLACE INTO events (id, name, start, end, location, "
 						+ "description, link, image) "
 						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
 				new String[] { String.valueOf(e.id), e.name,
 						Utils.getDateTimeString(e.start),
 						Utils.getDateTimeString(e.end), e.location,
 						e.description, e.link, e.image });
 	}
 
 	public void removeCache() {
 		Log.d("TumCampus events deleteAllFromDb", "");
 
 		db.execSQL("DELETE FROM events");
 		Utils.emptyCacheDir("events/cache");
 	}
 
 	public void cleanupDb() {
 		db.execSQL("DELETE FROM events WHERE start < date('now','-14 day')");
 	}
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE IF NOT EXISTS events ("
 				+ "id VARCHAR PRIMARY KEY, name VARCHAR, start VARCHAR, "
 				+ "end VARCHAR, location VARCHAR, description VARCHAR, "
 				+ "link VARCHAR, image VARCHAR)");
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
