 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.View.OnLongClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MsgBox extends Activity implements OnClickListener, OnTouchListener, OnLongClickListener
 {
 	private MsgListAdapter msglist_adapter;
 	private ListView msglist_view;
 	private NetworkClient net;
 	private ProgressMsg progress;
 	private String gameid;
 	private MsgBox self;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			final JSONObject json = (JSONObject) msg.obj;
 
 			try {
 				if (json.getString("result").equals("error")) {
 					progress.remove();
 					Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				switch (msg.what) {
 				case NetworkClient.SUBMIT_MSG:
 					final EditText txt = (EditText) findViewById(R.id.new_msg);
 					txt.setText("");
 
 					updateMsgList();
 					break;
 				case NetworkClient.SYNC_MSGS:
 					saveMsgs(json);
 					msglist_adapter.update();
 					msglist_view.setSelection(msglist_view.getCount() - 1);
 					progress.dismiss();
 					break;
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		self = this;
 
 		// Set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		net = new NetworkClient(this, handle);
 		progress = new ProgressMsg(this);
 		gameid = getIntent().getExtras().getString("gameid");
 
 		// set content view
 		setContentView(R.layout.msgbox);
 
 		ImageView image = (ImageView) findViewById(R.id.submit_msg);
 		image.setOnTouchListener(this);
 		image.setOnClickListener(this);
 
 		image = (ImageView) findViewById(R.id.topbar);
 		image.setOnTouchListener(this);
 		image.setOnLongClickListener(this);
 
 		// set list adapters
 		msglist_adapter = new MsgListAdapter(this, gameid);
 
 		msglist_view = (ListView) findViewById(R.id.msg_list);
 		msglist_view.setAdapter(msglist_adapter);
 
 		// set empty view item
 		final View empty = msglist_adapter.getEmptyView(this);
 		((ViewGroup) msglist_view.getParent()).addView(empty);
 		msglist_view.setEmptyView(empty);
 
 		// scroll to bottom
 		msglist_view.setSelection(msglist_view.getCount() - 1);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		NetActive.inc();
 		updateMsgList();
 	}
 
 	@Override
 	public void onPause()
 	{
 		NetActive.dec();
 		super.onPause();
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.topbar_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.topbar);
 			break;
 		case R.id.submit_msg:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.rsubmit_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.rsubmit);
 			break;
 		}
 		return false;
 	}
 
 	public void onClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.submit_msg:
 			progress.setText("Sending Message...");
 
 			final EditText txt = (EditText) findViewById(R.id.new_msg);
 			final String msg = txt.getText().toString().trim();
 
 			if (msg.length() < 1)
 				return;
 			net.submit_msg(gameid, msg);
 			(new Thread(net)).start();
 			break;
 		}
 	}
 
 	public boolean onLongClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 		case R.id.topbar_genesis:
 			finish();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.msgbox_options, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.resync:
 			updateMsgList();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	private void updateMsgList()
 	{
 		progress.setText("Updating Messages...");
 		final GameDataDB db = new GameDataDB(self);
 		net.sync_msgs(db.getNewestMsg());
 		(new Thread(net)).start();
 		db.close();
 	}
 
 	private void saveMsgs(final JSONObject data)
 	{
 	try {
 		final JSONArray msgs = data.getJSONArray("msglist");
 		final GameDataDB db = new GameDataDB(this);
 
 		for (int i = 0; i < msgs.length(); i++) {
 			final JSONObject item = msgs.getJSONObject(i);
 
 			final JSONArray players = item.getJSONArray("players");
 
 			final String gameid = item.getString("gameid"),
 				username = item.getString("username"),
 				opponent = (username.equals(players.getString(0)))? players.getString(1) : players.getString(0),
 				txt = item.getString("txt");
 			final long time = item.getLong("time");
 
 			db.insertMsg(gameid, time, username, txt, opponent);
 		}
 		db.setMsgsRead(gameid);
 		db.close();
 	}  catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 }
