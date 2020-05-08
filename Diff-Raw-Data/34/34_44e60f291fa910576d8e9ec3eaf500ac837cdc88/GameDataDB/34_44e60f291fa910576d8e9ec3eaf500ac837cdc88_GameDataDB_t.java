 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class GameDataDB
 {
 	private final SQLiteDatabase db;
 	private final Context context;
 
 	public GameDataDB(final Context _context)
 	{
 		context = _context;
 		db = (new DatabaseOpenHelper(context)).getWritableDatabase();
 	}
 
 	public void close()
 	{
 		db.close();
 	}
 
 	public static Bundle rowToBundle(final SQLiteCursor cursor, final int index)
 	{
 		final Bundle bundle = new Bundle();
 		final String[] column = cursor.getColumnNames();
 
 		cursor.moveToPosition(index);
 		for (int i = 0; i < cursor.getColumnCount(); i++)
 			bundle.putString(column[i], cursor.getString(i));
 		return bundle;
 	}
 
 	public Bundle newLocalGame(final String gamename, final int gametype, final int opponent)
 	{
 		final long time = (new Date()).getTime();
 		final Object[] data = {gamename, time, time, gametype, opponent};
 		final String[] data2 = {String.valueOf(time)};
 
 		db.execSQL("INSERT INTO localgames (name, ctime, stime, gametype, opponent) VALUES (?, ?, ?, ?, ?);", data);
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM localgames WHERE ctime=?", data2);
 
 		return rowToBundle(cursor, 0);
 	}
 
 	public void saveLocalGame(final int id, final long stime, final String zfen, final String history)
 	{
 		final Object[] data = {stime, zfen, history, id};
 		db.execSQL("UPDATE localgames SET stime=?, zfen=?, history=? WHERE id=?;", data);
 	}
 
 	public void deleteLocalGame(final int id)
 	{
 		final Object[] data = {id};
 		db.execSQL("DELETE FROM localgames WHERE id=?;", data);
 	}
 
 	public void deleteArchiveGame(final String gameid)
 	{
 		final Object[] data = {gameid};
 		db.execSQL("DELETE FROM archivegames WHERE gameid=?;", data);
 	}
 
 	public void renameLocalGame(final int id, final String name)
 	{
 		final Object[] data = {name, id};
 		db.execSQL("UPDATE localgames SET name=? WHERE id=?;", data);
 	}
 
 	public SQLiteCursor getLocalGameList()
 	{
 		return (SQLiteCursor) db.rawQuery("SELECT * FROM localgames ORDER BY stime DESC", null);
 	}
 
 	public SQLiteCursor getOnlineGameList(final int yourturn)
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 
 		final String username = pref.getString("username", "!error!");
 		final String[] data = {username, username, String.valueOf(yourturn)};
 		final String query = "SELECT * FROM onlinegames WHERE (white=? OR black=?) AND yourturn=? ORDER BY stime DESC";
 
 		return (SQLiteCursor) db.rawQuery(query, data);
 	}
 
 	public SQLiteCursor getArchiveGameList()
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString("username", "!error!");
 		final String[] data = {username, username};
 
 		return (SQLiteCursor) db.rawQuery("SELECT * FROM archivegames WHERE white=? OR black=? ORDER BY stime DESC", data);
 	}
 
 	public ObjectArray<String> getOnlineGameIds()
 	{
 		final ObjectArray<String> list = new ObjectArray<String>();
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString("username", "!error!");
 		final String[] data = {username, username};
 
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM onlinegames WHERE white=? OR black=?", data);
 
 		cursor.moveToFirst();
 		for (int i = 0; i < cursor.getCount(); i++) {
 			list.push(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		return list;
 	}
 
 	public ObjectArray<String> getArchiveGameIds()
 	{
 		final ObjectArray<String> list = new ObjectArray<String>();
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString("username", "!error!");
 		final String[] data = {username, username};
 
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM archivegames WHERE white=? OR black=?", data);
 
 		cursor.moveToFirst();
 		for (int i = 0; i < cursor.getCount(); i++) {
 			list.push(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		return list;
 	}
 
 	public long getNewestOnlineTime()
 	{
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT stime FROM onlinegames", null);
 
 		long time = 0;
 		cursor.moveToFirst();
 		for (int i = 0; i < cursor.getCount(); i++) {
 			time = Math.max(time, cursor.getLong(0));
 			cursor.moveToNext();
 		}
 		return time;
 	}
 
	public void clearOnlineTime()
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String username = pref.getString("username", "!error!");
		final Object[] data = {0, username, username};

		db.execSQL("UPDATE onlinegames SET stime=? WHERE white=? OR black=?;", data);
	}

 	public void insertMsg(final String gameid, final long time, final String username, final String msg)
 	{
 		final Object[] data = {gameid, time, username, msg};
 		db.execSQL("INSERT INTO msgtable (gameid, time, username, msg) VALUES (?, ?, ?, ?);", data);
 	}
 
 	public void updateOnlineGame(final String gameid, final int status, final long stime, final String zfen, final String history)
 	{
 		final String[] data1 = {gameid};
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data1);
		if (cursor.getCount() < 1)
			return;
 
		final Bundle row = rowToBundle(cursor, 0);
 		final GameInfo info = new GameInfo(context, status, history, row.getString("white"), row.getString("black"));
 
 		final int ply = info.getPly(), yourturn = info.getYourTurn();
 
 		final Object[] data2 = {stime, status, ply, yourturn, zfen, history, gameid};
 		db.execSQL("UPDATE onlinegames SET stime=?, status=?, ply=?, yourturn=?, zfen=?, history=? WHERE gameid=?;", data2);
 	}
 
 	public void insertOnlineGame(final String gameid, final int gametype, final int eventtype, final long ctime, final String white, final String black)
 	{
 		final Object[] data = {gameid, gametype, eventtype, ctime, white, black};
 		db.execSQL("INSERT OR REPLACE INTO onlinegames (gameid, gametype, eventtype, ctime, white, black) VALUES (?, ?, ?, ?, ?, ?);", data);
 	}
 
 	public void insertArchiveGame(final JSONObject json)
 	{
 	try {
 		final String gameid = json.getString("gameid");
 		final int gametype = Enums.GameType(json.getString("gametype"));
 		final int eventtype = Enums.EventType(json.getString("eventtype"));
 		final int status = Enums.GameStatus(json.getString("status"));
 		final long ctime = json.getLong("ctime");
 		final long stime = json.getLong("stime");
 		final String white = json.getString("white");
 		final String black = json.getString("black");
 		final String zfen = json.getString("zfen");
 		final String history = json.getString("history");
 
 		int w_psrfrom = 0;
 		int w_psrto = 0;
 		int b_psrfrom = 0;
 		int b_psrto = 0;
 
 		if (eventtype != Enums.INVITE) {
 			w_psrfrom = json.getJSONObject("score").getJSONObject("white").getInt("from");
 			w_psrto = json.getJSONObject("score").getJSONObject("white").getInt("to");
 			b_psrfrom = json.getJSONObject("score").getJSONObject("black").getInt("from");
 			b_psrto = json.getJSONObject("score").getJSONObject("black").getInt("to");
 		}
 
 		final String tmp[] = zfen.split(":");
 		final int ply = Integer.valueOf(tmp[tmp.length - 1]);
 
 		final Object[] data = {gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto,
 			ctime, stime, ply, white, black, zfen, history};
 
		final String q1 = "INSERT OR REPLACE INTO archivegames ";
 		final String q2 = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ";
 		final String q3 = "ctime, stime, ply, white, black, zfen, history) ";
 		final String q4 = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
 
 		db.execSQL(q1 + q2 + q3 + q4, data);
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public void archiveNetworkGame(final String gameid, final int w_from, final int w_to, final int b_from, final int b_to)
 	{
 		final String[] data = {gameid};
 
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data);
 		final Bundle row = rowToBundle(cursor, 0);
 
 		final String tnames = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ctime, stime, ply, white, black, zfen, history)";
 		final String dstring = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 		final Object[] data2 = {row.get("gameid"), row.get("gametype"), row.get("eventtype"), row.get("status"),
 			w_from, w_to, b_from, b_to, row.get("ctime"), row.get("stime"), row.get("ply"),
 			row.get("white"), row.get("black"), row.get("zfen"), row.get("history")};
 
 		db.execSQL("INSERT OR REPLACE INTO archivegames " + tnames + " VALUES " + dstring + ";", data2);
 		db.execSQL("DELETE FROM onlinegames WHERE gameid=?;", data);
 	}
 
 	public void copyGameToLocal(final String gameid, final int gametype)
 	{
 		final String[] data = {gameid};
 		final String type = (gametype == Enums.ONLINE_GAME)? "onlinegames" : "archivegames";
 
 		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM " + type + " WHERE gameid=?", data);
 		final Bundle row = rowToBundle(cursor, 0);
 
 		final long time = (new Date()).getTime();
 		final String tnames = "(name, ctime, stime, gametype, opponent, zfen, history)";
 		final String dstring = "(?, ?, ?, ?, ?, ?, ?)";
 		final Object[] data2 = {row.get("white") + " vs. " + row.get("black"), time, time,
 			row.get("gametype"), Enums.HUMAN_OPPONENT, row.get("zfen"), row.get("history")};
 
 		db.execSQL("INSERT INTO localgames" + tnames + " VALUES " + dstring + ";", data2);
 	}
 }
