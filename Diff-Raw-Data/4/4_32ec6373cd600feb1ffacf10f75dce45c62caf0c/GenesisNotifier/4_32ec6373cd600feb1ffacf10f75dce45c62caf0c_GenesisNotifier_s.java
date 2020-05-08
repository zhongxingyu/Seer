 package com.chess.genesis;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import java.util.Calendar;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GenesisNotifier extends Service implements Runnable
 {
 	public final static int POLL_FREQ = 30;
 
 	public final static int ERROR_NOTE = 1;
 	public final static int YOURTURN_NOTE = 2;
 	public final static int NEWMGS_NOTE = 4;
 
 	private NetworkClient net;
 	private SocketClient socket;
 	private GameDataDB db;
 	private SharedPreferences pref;
 	private int lock;
 	private boolean fromalarm;
 	private boolean error;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			final JSONObject json = (JSONObject) msg.obj;
 
 		try {
 			if (json.getString("result").equals("error")) {
 				error = true;
 				socket.disconnect();
				final String title = "Error in GenesisNotifier";
				SendNotification(title, json.getString("reason"), ERROR_NOTE);
 				return;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 			switch (msg.what) {
 			case NetworkClient.SYNC_LIST:
 				NewMove(json);
 				break;
 			case NetworkClient.SYNC_MSGS:
 				NewMsgs(json);
 				break;
 			case NetworkClient.GAME_STATUS:
 				game_status(json);
 				break;
 			}
 			// release lock
 			lock--;
 		}
 	};
 
 	public static void clearNotification(final Context context, final int id)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		final NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
 
 		if ((id & YOURTURN_NOTE) != 0) {
 			if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() == 0)
 				nm.cancel(YOURTURN_NOTE);
 		} else if ((id & NEWMGS_NOTE) != 0) {
 			if (db.getUnreadMsgCount() == 0)
 				nm.cancel(NEWMGS_NOTE);
 		}
 		db.close();
 	}
 
 	public void run()
 	{
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (!pref.getBoolean("isLoggedIn", false) || !pref.getBoolean("noteEnabled", true)) {
 			stopSelf();
 			return;
 		} else if (internetIsActive() && fromalarm) {
 			CheckServer();
 		}
 		ScheduleWakeup();
 		stopSelf();
 	}
 
 	@Override
 	public void onStart(final Intent intent, final int startid)
 	{
 		super.onStart(intent, startid);
 		Bundle bundle = null;
 
 		if (intent == null)
 			fromalarm = false;
 		else if ((bundle = intent.getExtras()) == null)
 			fromalarm = false;
 		else
 			fromalarm = bundle.getBoolean("fromAlarm", false);
 
 		(new Thread(this)).start();
 	}
 
 	@Override
 	public IBinder onBind(final Intent intent)
 	{
 		return null;
 	}
 
 	private void ScheduleWakeup()
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		final Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, pref.getInt("notifierPolling", GenesisNotifier.POLL_FREQ));
 
 		final Intent intent = new Intent(this, GenesisAlarm.class);
 		final PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
 	}
 
 	private boolean internetIsActive()
 	{
 		final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 		return (netInfo != null && netInfo.isConnected());
 	}
 
 	private void SendNotification(final String title, final String text, final int id)
 	{
 		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		final Notification note = new Notification(R.drawable.icon, title, System.currentTimeMillis());
 
 		setupNotification(note, id);
 
 		final Intent intent = new Intent(this, OnlineGameList.class);
 		final PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		final Context context = getApplicationContext();
 
 		note.setLatestEventInfo(context, title, text, pintent);
 		nm.notify(id, note);
 	}
 
 	private void setupNotification(final Notification note, final int id)
 	{
 		if (id == ERROR_NOTE) {
 			note.flags |= Notification.FLAG_AUTO_CANCEL;
 			return;
 		} else {
 			note.flags |= Notification.FLAG_NO_CLEAR;
 			note.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
 		}
 
 		if (pref.getBoolean("noteRingtoneEnable", false))
 			note.sound = Uri.parse(pref.getString("noteRingtone", "content://settings/system/notification_sound"));
 		if (pref.getBoolean("noteVibrateEnable", true))
 			note.vibrate = parseVibrate();
 	}
 
 	private long[] parseVibrate()
 	{
 		final String str = pref.getString("noteVibrate", "0,150");
 		final String[] arr = str.trim().split(",");
 		final long[] vib = new long[arr.length];
 
 		for (int i = 0; i < arr.length; i++)
 			vib[i] = Long.valueOf(arr[i]);
 		return vib;
 	}
 
 	/*
 	 * Sync Code
 	 */
 
 	private void trylock()
 	{
 	try {
 		lock++;
 		while (lock > 0 && !error)
 			Thread.sleep(16);
 		lock = 0;
 	} catch (java.lang.InterruptedException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public void CheckServer()
 	{
 		error = false;
 		lock = 0;
 		socket = SocketClient.getInstance(0);
 		net = new NetworkClient(socket, this, handle);
 		db = new GameDataDB(this);
 
 		final long mtime = pref.getLong("lastmsgsync", 0);
 		final long gtime = pref.getLong("lastgamesync", 0);
 
 		if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0) {
 			SendNotification("It's Your turn", "It's your turn in a game you're in", YOURTURN_NOTE);
 		} else {
 			net.sync_list(gtime);
 			net.run();
 			trylock();
 
 			if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0)
 				SendNotification("It's Your turn", "It's your turn in a game you're in", YOURTURN_NOTE);
 		}
 
 		if (db.getUnreadMsgCount() > 0) {
 			SendNotification("New Message", "A new message was posted to a game you're in", NEWMGS_NOTE);
 		} else {
 			net.sync_msgs(mtime);
 			net.run();
 			trylock();
 
 			if (db.getUnreadMsgCount() > 0)
 				SendNotification("New Message", "A new message was posted to a game you're in", NEWMGS_NOTE);
 		}
 		socket.disconnect();
 		db.close();
 	}
 
 	private void NewMove(final JSONObject json)
 	{
 	try {
 		final JSONArray ids = json.getJSONArray("gameids");
 		final long time = json.getLong("time");
 
 		for (int i = 0; i < ids.length(); i++) {
 			if (error)
 				return;
 			net.game_status(ids.getString(i));
 			net.run();
 
 			lock++;
 		}
 		// Save sync time
 		final Editor editor = pref.edit();
 		editor.putLong("lastgamesync", time);
 		editor.commit();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void NewMsgs(final JSONObject json)
 	{
 	try {
 		final JSONArray msgs = json.getJSONArray("msglist");
 		final long time = json.getLong("time");
 
 		for (int i = 0; i < msgs.length(); i++) {
 			final JSONObject item = msgs.getJSONObject(i);
 			db.insertMsg(item);
 		}
 
 		// Save sync time
 		final Editor editor = pref.edit();
 		editor.putLong("lastmsgsync", time);
 		editor.commit();
 	}  catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void game_status(final JSONObject json)
 	{
 		db.updateOnlineGame(json);
 	}
 }
