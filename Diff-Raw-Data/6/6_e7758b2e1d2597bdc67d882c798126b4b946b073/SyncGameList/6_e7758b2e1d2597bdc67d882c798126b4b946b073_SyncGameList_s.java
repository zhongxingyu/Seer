 package com.chess.genesis;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import java.util.ArrayList;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class SyncGameList implements Runnable
 {
 	public final static int MSG = 101;
 
 	private final Context context;
 	private final Handler callback;
 	private final NetworkClient net;
 
 	private int lock = 0;
 	private int sync_type = Enums.ONLINE_GAME;
 	private boolean error = false;
 	private boolean fullsync = false;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			final JSONObject json = (JSONObject) msg.obj;
 
 		try {
 			if (json.getString("result").equals("error")) {
 				callback.sendMessage(Message.obtain(callback, MSG, json));
 				error = true;
 				return;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 			switch (msg.what) {
 			case NetworkClient.GAME_STATUS:
 				game_status(json);
 				lock--;
 				break;
 			case NetworkClient.GAME_INFO:
 				game_info(json);
 				lock--;
 				break;
 			case NetworkClient.GAME_DATA:
 				game_data(json);
 				lock--;
 				break;
 			case NetworkClient.READ_INBOX:
 				read_inbox(json);
 				lock--;
 				break;
 			case NetworkClient.CLEAR_INBOX:
 				lock--;
 				break;
 			case NetworkClient.SYNC_GAMIDS:
 				if (sync_type == Enums.ONLINE_GAME)
 					sync_active(json);
 				else
 					sync_archive(json);
 				lock--;
 				break;
 			}
 		}
 	};
 
 	public SyncGameList(final Context _context, final Handler handler)
 	{
 		context = _context;
 		callback = handler;
 
 		net = new NetworkClient(context, handle);
 	}
 
 	private void trylock()
 	{
 	try {
		lock = 1;
 		while (lock > 0 && !error)
 			Thread.sleep(16);
 	} catch (java.lang.InterruptedException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public void setFullSync(final boolean value)
 	{
 		fullsync = value;
 	}
 
 	public void run()
 	{
 		net.read_inbox();
 		net.run();
 		trylock();
 
 		if (!error) {
 			if (fullsync) {
 				sync_type = Enums.ONLINE_GAME;
 				net.sync_gameids("active");
 				net.run();
 
 				trylock();
 
 				sync_type = Enums.ARCHIVE_GAME;
 				net.sync_gameids("archive");
 				net.run();
 			} else {
 				update_gamestatus();
 			}
 		}
 		trylock();
 
 		if (!error) {
 			final JSONObject json = new JSONObject();
 			try {
 				json.put("result", "ok");
 				json.put("reason", "gamelist updated");
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 			callback.sendMessage(Message.obtain(callback, MSG, json));
 		}
 	}
 
 	private void read_inbox(final JSONObject json)
 	{
 	try {
 		final JSONArray games = json.getJSONArray("games");
 		final JSONArray msgs = json.getJSONArray("msgs");
 
 		long mtime = 0;
 
 		for (int i = 0; i < games.length(); i++) {
 			final JSONObject data = games.getJSONObject(i);
 
 			final long time = data.getLong("time");
 			mtime = Math.max(time, mtime);
 
 			final String gameid = data.getString("gameid");
 
 			if (error)
 				return;
 			net.game_info(gameid);
 			net.run();
 
 			lock++;
 		}
 		for (int i = 0; i < msgs.length(); i++) {
 			final JSONObject data = msgs.getJSONObject(i);
 
 			final long time = data.getLong("time");
 			mtime = Math.max(time, mtime);
 
 			final String gameid = data.getString("gameid");
 			final String msg = data.getString("msg");
 			final String username = data.getString("username");
 
 			final GameDataDB db = new GameDataDB(context);
 			db.insertMsg(gameid, time, username, msg);
 			db.close();
 		}
 		if (error)
 			return;
 		net.clear_inbox(mtime);
 		net.run();
 
 		lock++;
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void sync_active(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		final ObjectArray<String> list = db.getOnlineGameIds();
 		db.close();
 
 		final ArrayList<String> list_have = new ArrayList<String>();
 		for (int i = 0; i < list.size(); i++)
 			list_have.add(list.get(i));
 
 		final ArrayList<String> list_need = new ArrayList<String>();
 	try {
 		final JSONArray ids = json.getJSONArray("gameids");
 		for (int i = 0; i < ids.length(); i++)
 			list_need.add(ids.getString(i));
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 
 		list_need.removeAll(list_have);
 
 		for (int i = 0; i < list_need.size(); i++) {
 			if (error)
 				return;
 			net.game_info(list_need.get(i));
 			net.run();
 
 			lock++;
 		}
 	}
 
 	private void sync_archive(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		final ObjectArray<String> list = db.getArchiveGameIds();
 		db.close();
 
 		final ArrayList<String> list_have = new ArrayList<String>();
 		for (int i = 0; i < list.size(); i++)
 			list_have.add(list.get(i));
 
 		final ArrayList<String> list_need = new ArrayList<String>();
 	try {
 		final JSONArray ids = json.getJSONArray("gameids");
 		for (int i = 0; i < ids.length(); i++)
 			list_need.add(ids.getString(i));
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 
 		list_need.removeAll(list_have);
 
 		for (int i = 0; i < list_need.size(); i++) {
 			if (error)
 				return;
 			net.game_data(list_need.get(i));
 			net.run();
 
 			lock++;
 		}
 	}
 
 	private void update_gamestatus()
 	{
 		final GameDataDB db = new GameDataDB(context);
 		final ObjectArray<String> list = db.getOnlineGameIds();
 		db.close();
 
 		for (int i = 0; i < list.size(); i++) {
 			if (error)
 				return;
 			net.game_status(list.get(i));
 			net.run();
 
 			lock++;
 		}
 	}
 
 	private void game_info(final JSONObject json)
 	{
 	try {
 		final String gameid = json.getString("gameid");
 		final String white = json.getString("white");
 		final String black = json.getString("black");
 		final long ctime = json.getLong("ctime");
 		final int gametype = Enums.GameType(json.getString("gametype"));
 		final int eventtype = Enums.EventType(json.getString("eventtype"));
 
 		final GameDataDB db = new GameDataDB(context);
 		db.insertOnlineGame(gameid, gametype, eventtype, ctime, white, black);
 		db.close();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void game_status(final JSONObject json)
 	{
 	try {
 		final String gameid = json.getString("gameid");
 		final String zfen = json.getString("zfen");
 		final String history = json.getString("history");
 		final long stime = json.getLong("stime");
 		final int status = Enums.GameStatus(json.getString("status"));
 
 		final GameDataDB db = new GameDataDB(context);
 		db.updateOnlineGame(gameid, status, stime, zfen, history);
 		db.close();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void game_data(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		db.insertArchiveGame(json);
 		db.close();
 	}
 }
