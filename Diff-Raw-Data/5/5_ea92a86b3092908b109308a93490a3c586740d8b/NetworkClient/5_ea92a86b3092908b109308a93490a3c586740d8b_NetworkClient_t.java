 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import java.io.IOException;
 import java.net.SocketException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class NetworkClient implements Runnable
 {
 	public final static int NONE = 0;
 	public final static int LOGIN = 1;
 	public final static int REGISTER = 2;
 	public final static int JOIN_GAME = 3;
 	public final static int NEW_GAME = 4;
 	public final static int READ_INBOX = 5;
 	public final static int CLEAR_INBOX = 6;
 	public final static int GAME_STATUS = 7;
 	public final static int GAME_INFO = 8;
 	public final static int SUBMIT_MOVE = 9;
 	public final static int SUBMIT_MSG = 10;
 	public final static int SYNC_GAMIDS = 11;
 	public final static int GAME_SCORE = 12;
 	public final static int GAME_DATA = 13;
 	public final static int RESIGN_GAME = 14;
 	public final static int SYNC_LIST = 15;
 
 	private final Context context;
 	private final Handler callback;
 	
 	private JSONObject json;
 	private int fid = NONE;
 	private boolean loginRequired;
 	private boolean error = false;
 
 	public NetworkClient(final Context _context, final Handler handler)
 	{
 		callback = handler;
 		context = _context;
 	}
 
 	private boolean relogin(final SocketClient net)
 	{
		if (net.isLoggedin)
 			return true;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString("username", "!error!");
 		final String password = pref.getString("passhash", "!error!");
 
 		JSONObject json2 = new JSONObject();
 
 		try {
 			try {
 				json2.put("request", "login");
 				json2.put("username", username);
 				json2.put("passhash", Crypto.LoginKey(password));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error) {
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			error = false;
 			return false;
 		}
 
 		try {
 			net.write(json2);
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error) {
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			error = false;
 			return false;
 		}
 
 		try {
 			json2 = net.read();
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (JSONException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Server response illogical");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 		if (error) {
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			error = false;
 			return false;
 		}
 
 		try {
 			if (!json2.getString("result").equals("ok")) {
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				return false;
 			}
			net.isLoggedin = true;
 			return true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void run()
 	{
 		final SocketClient net = new SocketClient();
 		JSONObject json2 = null;
 
 		if (error) {
 			error = false;
 			net.disconnect();
 			return;
 		} else if (loginRequired && !relogin(net)) {
 			net.disconnect();
 			return;
 		}
 
 		try {
 			net.write(json);
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error) {
 			net.disconnect();
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			error = false;
 			return;
 		}
 
 		try {
 			json2 = net.read();
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (JSONException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Server response illogical");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 		if (error)
 			error = false;
 		callback.sendMessage(Message.obtain(callback, fid, json2));
 		net.disconnect();
 	}
 
 	public void register(final String username, final String password, final String email)
 	{
 		fid = REGISTER;
 		loginRequired = false;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "register");
 			json.put("username", username);
 			json.put("passhash", Crypto.HashPasswd(password));
 			json.put("email", email);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void login_user(final String username, final String password)
 	{
 		fid = LOGIN;
 		loginRequired = false;
 
 		JSONObject json2 = null;
 		json = new JSONObject();
 
 		try {
 			try {
 				json.put("request", "login");
 				json.put("username", username);
 				json.put("passhash", Crypto.LoginKey(password));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (SocketException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			json2 = new JSONObject();
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Lost connection durring sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error) {
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			error = true;
 		}
 	}
 
 	public void join_game(final String gametype)
 	{
 		fid = JOIN_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "joingame");
 			json.put("gametype", gametype);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void new_game(final String opponent, final String gametype, final String color)
 	{
 		fid = NEW_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "newgame");
 			json.put("opponent", opponent);
 			json.put("gametype", gametype);
 			json.put("color", color);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 	}
 
 	public void submit_move(final String gameid, final String move)
 	{
 		fid = SUBMIT_MOVE;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "sendmove");
 			json.put("gameid", gameid);
 			json.put("move", move);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void resign_game(final String gameid)
 	{
 		fid = RESIGN_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "resign");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void submit_msg(final String gameid, final String msg)
 	{
 		fid = SUBMIT_MSG;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "sendmsg");
 			json.put("gameid", gameid);
 			json.put("msg", msg);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_status(final String gameid)
 	{
 		fid = GAME_STATUS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamestatus");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_info(final String gameid)
 	{
 		fid = GAME_INFO;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gameinfo");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_data(final String gameid)
 	{
 		fid = GAME_DATA;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamedata");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void read_inbox()
 	{
 		fid = READ_INBOX;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "inbox");
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void clear_inbox(final long time)
 	{
 		fid = CLEAR_INBOX;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "clearinbox");
 			json.put("time", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void sync_gameids(final String type)
 	{
 		fid = SYNC_GAMIDS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gameids");
 			json.put("type", type);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void sync_list(final long time)
 	{
 		fid = SYNC_LIST;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "synclist");
 			json.put("time", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_score(final String gameid)
 	{
 		fid = GAME_SCORE;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamescore");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 }
