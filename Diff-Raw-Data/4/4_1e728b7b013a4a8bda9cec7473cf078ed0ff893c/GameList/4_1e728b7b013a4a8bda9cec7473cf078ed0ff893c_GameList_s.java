 package com.chess.genesis;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GameList extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnTouchListener
 {
 	public static GameList self;
 
 	public GameListAdapter gamelist_adapter;
 
 	private Bundle settings;
 	private NetworkClient net;
 	private ProgressMsg progress;
 	private int type;
 	private boolean yourmove;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case NewOnlineGameDialog.MSG:
 				Bundle data = (Bundle) msg.obj;
 
 				if (data.getInt("opponent") == Enums.INVITE) {
 					(new InviteOptionsDialog(self, handle, data)).show();
 					return;
 				}
 				progress.setText("Sending newgame request");
 				String gametype = Enums.GameType(data.getInt("gametype"));
 
 				net.join_game(gametype);
 				(new Thread(net)).start();
 				break;
 			case RematchConfirm.MSG:
 				data = (Bundle) msg.obj;
 				progress.setText("Sending newgame request");
 
 				final String opponent = data.getString("opp_name");
 				String color = Enums.ColorType(data.getInt("color"));
 				gametype = Enums.GameType(data.getInt("gametype"));
 
 				net.new_game(opponent, gametype, color);
 				(new Thread(net)).start();
 				break;
 			case InviteOptionsDialog.MSG:
 				data = (Bundle) msg.obj;
 				progress.setText("Sending newgame request");
 
 				gametype = Enums.GameType(data.getInt("gametype"));
 				color = Enums.ColorType(data.getInt("color"));
 
 				net.new_game(data.getString("opp_name"), gametype, color);
 				(new Thread(net)).start();
 				break;
 			case NewLocalGameDialog.MSG:
 				final GameDataDB db = new GameDataDB(self);
 				final Bundle bundle = (Bundle) msg.obj;
 
 				final int gametype2 = bundle.getInt("gametype");
 				final int gameopp = bundle.getInt("opponent");
 				String gamename = bundle.getString("name");
 				if (gamename.length() < 1)
 					gamename = "untitled";
 
 				final Intent intent;
 				if (gametype2 == Enums.GENESIS_CHESS)
 					intent = new Intent(self, GenGame.class);
 				else
 					intent = new Intent(self, RegGame.class);
 				intent.putExtras(db.newLocalGame(gamename, gametype2, gameopp));
 				intent.putExtras(settings);
 				db.close();
 
 				startActivity(intent);
 				break;
 			case SyncGameList.MSG:
 			case NetworkClient.JOIN_GAME:
 			case NetworkClient.NEW_GAME:
 				final JSONObject json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					progress.remove();
 
 					if (msg.what == SyncGameList.MSG) {
 						gamelist_adapter.update();
 						if (yourmove && gamelist_adapter.getCount() == 0) {
 							final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 							nm.cancelAll();
 						}
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
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
 
 		// store settings from main menu
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		settings = getIntent().getExtras();
 		settings.putString("username", prefs.getString("username", "!error!"));
 		type = settings.getInt("type", Enums.ONLINE_GAME);
 		yourmove = true;
 
 		net = new NetworkClient(this, handle);
 		progress = new ProgressMsg(this);
 
 		// set content view
 		switch (type) {
 		case Enums.ONLINE_GAME:
 			setContentView(R.layout.gamelist_online);
 
 			ImageView button = (ImageView) findViewById(R.id.your_move);
 			button.setOnTouchListener(this);
 			button.setOnClickListener(this);
 
 			button = (ImageView) findViewById(R.id.their_move);
 			button.setOnTouchListener(this);
 			button.setOnClickListener(this);
 			break;
 		case Enums.LOCAL_GAME:
 		default:
 			setContentView(R.layout.gamelist_local);
 			break;
 		case Enums.ARCHIVE_GAME:
 			setContentView(R.layout.gamelist_archive);
 			break;
 		}
 
 		// set click listeners
 		switch (type) {
 		case Enums.ONLINE_GAME:
 		case Enums.LOCAL_GAME:
 		default:
 			ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
 			button.setOnTouchListener(this);
 			button.setOnLongClickListener(this);
 
 			button = (ImageView) findViewById(R.id.topbar_plus);
 			button.setOnTouchListener(this);
 			button.setOnClickListener(this);
 			break;
 		case Enums.ARCHIVE_GAME:
 			button = (ImageView) findViewById(R.id.topbar);
 			button.setOnTouchListener(this);
 			button.setOnLongClickListener(this);
 			break;
 		}
 
 		// set list adapters
 		gamelist_adapter = new GameListAdapter(this, settings);
 
 		final ListView gamelist_view = (ListView) findViewById(R.id.game_list);
 		gamelist_view.setAdapter(gamelist_adapter);
 		gamelist_view.setOnItemClickListener(this);
 
 		// set empty view item
 		final View empty = gamelist_adapter.getEmptyView(this);
 		((ViewGroup) gamelist_view.getParent()).addView(empty);
 		gamelist_view.setEmptyView(empty);
 
 		registerForContextMenu(gamelist_view);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if (settings.getInt("type", Enums.ONLINE_GAME) == Enums.ONLINE_GAME) {
 			// start background notifier
 			startService(new Intent(this, GenesisNotifier.class));
 
 			NetActive.inc();
 			progress.setText("Updating game list");
 
 			// Must not be final
 			SyncGameList sync = new SyncGameList(this, handle);
 			(new Thread(sync)).start();
		} else {
			gamelist_adapter.update();
 		}
 	}
 
 	@Override
 	public void onPause()
 	{
 		if (settings.getInt("type") == Enums.ONLINE_GAME)
 			NetActive.dec();
 
 		super.onPause();
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		gamelist_adapter.close();
 		super.onDestroy();
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
 		case R.id.topbar_genesis:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.topbar_genesis_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.topbar_genesis);
 			break;
 		case R.id.topbar_plus:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.topbar_plus_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.topbar_plus);
 			break;
 		case R.id.your_move:
 			if (yourmove) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.yourmove_selected_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.yourmove_selected);
 			} else {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.yourmove_notselected_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.yourmove_notselected);
 			}
 			break;
 		case R.id.their_move:
 			if (yourmove) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.theirmove_notselected_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.theirmove_notselected);
 			} else {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.theirmove_selected_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.theirmove_selected);
 			}
 			break;
 		}
 		return false;
 	}
 
 	public void onClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar_plus:
 			if (settings.getInt("type") == Enums.LOCAL_GAME)
 				(new NewLocalGameDialog(v.getContext(), handle)).show();
 			else
 				(new NewOnlineGameDialog(v.getContext(), handle)).show();
 			break;
 		case R.id.your_move:
 			if (yourmove)
 				return;
 			yourmove = true;
 			gamelist_adapter.setYourturn(1);
 
 			ImageView button = (ImageView) findViewById(R.id.your_move);
 			button.setImageResource(R.drawable.yourmove_selected);
 
 			button = (ImageView) findViewById(R.id.their_move);
 			button.setImageResource(R.drawable.theirmove_notselected);
 			break;
 		case R.id.their_move:
 			if (!yourmove)
 				return;
 			yourmove = false;
 			gamelist_adapter.setYourturn(0);
 
 			button = (ImageView) findViewById(R.id.your_move);
 			button.setImageResource(R.drawable.yourmove_notselected);
 
 			button = (ImageView) findViewById(R.id.their_move);
 			button.setImageResource(R.drawable.theirmove_selected);
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
 
 	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
 	{
 		final Bundle data = (Bundle) parent.getItemAtPosition(position);
 		final Intent intent;
 
 		if (Integer.valueOf(data.getString("gametype")) == Enums.GENESIS_CHESS)
 			intent = new Intent(this, GenGame.class);
 		else
 			intent = new Intent(this, RegGame.class);
 
 		intent.putExtras(data);
 		intent.putExtras(settings);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			getMenuInflater().inflate(R.menu.gamelist_context_local, menu);
 			break;
 		case Enums.ONLINE_GAME:
 			getMenuInflater().inflate(R.menu.gamelist_context_online, menu);
 			break;
 		case Enums.ARCHIVE_GAME:
 			getMenuInflater().inflate(R.menu.gamelist_context_archive, menu);
 			break;
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item)
 	{
 		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		final Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);
 
 		switch (item.getItemId()) {
 		case R.id.delete_game:
 			if (type == Enums.LOCAL_GAME)
 				(new DeleteLocalDialog(this, Integer.valueOf(bundle.getString("id")))).show();
 			else if (type == Enums.ARCHIVE_GAME)
 				(new DeleteArchiveDialog(this, bundle.getString("gameid"))).show();
 			break;
 		case R.id.rename_game:
 			(new RenameGameDialog(this, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
 			break;
 		case R.id.local_copy:
 			(new CopyGameConfirm(this, bundle.getString("gameid"), type)).show();
 			break;
 		case R.id.rematch:
 			final String opponent = settings.getString("username").equals(bundle.getString("white"))?
 				bundle.getString("black") : bundle.getString("white");
 			(new RematchConfirm(this, handle, opponent)).show();
 			break;
 		default:
 			return super.onContextItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		if (type == Enums.ONLINE_GAME)
 			getMenuInflater().inflate(R.menu.gamelist_options_online, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.resync:
 			progress.setText("Updating game list");
 
 			// Must not be final
 			SyncGameList sync = new SyncGameList(this, handle);
 			(new Thread(sync)).start();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 }
