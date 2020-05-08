 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GameListOnline extends FragmentActivity implements OnClickListener, OnLongClickListener, OnTouchListener, OnItemClickListener
 {
 	private final static int THEIR_PAGE = 0;
 	private final static int YOUR_PAGE = 1;
 	private final static int ARCHIVE_PAGE = 2;
 
 	private Context context;
 	private GameListAdapter[] gamelistadapter_arr;
 	private NetworkClient net;
 	private ProgressMsg progress;
 	private ViewPager pager;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case DeleteArchiveDialog.MSG:
 			case ReadAllMsgsDialog.MSG:
 				updateGameListAdapters();
 				break;
 			case NewOnlineGameDialog.MSG:
 				Bundle data = (Bundle) msg.obj;
 
 				if (data.getInt("opponent") == Enums.INVITE) {
 					(new InviteOptionsDialog(context, handle, data)).show();
 					return;
 				}
 				progress.setText("Sending Newgame Request");
 				String gametype = Enums.GameType(data.getInt("gametype"));
 
 				net.join_game(gametype);
 				(new Thread(net)).start();
 				break;
 			case RematchConfirm.MSG:
 				data = (Bundle) msg.obj;
 				progress.setText("Sending Newgame Request");
 
 				final String opponent = data.getString("opp_name");
 				String color = Enums.ColorType(data.getInt("color"));
 				gametype = Enums.GameType(data.getInt("gametype"));
 
 				net.new_game(opponent, gametype, color);
 				(new Thread(net)).start();
 				break;
 			case NudgeConfirm.MSG:
 				progress.setText("Sending Nudge");
 
 				final String gameid = (String) msg.obj;
 				net.nudge_game(gameid);
 				(new Thread(net)).start();
 				break;
 			case InviteOptionsDialog.MSG:
 				data = (Bundle) msg.obj;
 				progress.setText("Sending Newgame Request");
 
 				gametype = Enums.GameType(data.getInt("gametype"));
 				color = Enums.ColorType(data.getInt("color"));
 
 				net.new_game(data.getString("opp_name"), gametype, color);
 				(new Thread(net)).start();
 				break;
 			case SyncClient.MSG:
 			case NetworkClient.JOIN_GAME:
 				JSONObject json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					if (msg.what == SyncClient.MSG || msg.what == NetworkClient.JOIN_GAME) {
 						progress.setText("Checking Game Pool");
 						updateGameListAdapters();
 						GenesisNotifier.clearNotification(context, GenesisNotifier.YOURTURN_NOTE|GenesisNotifier.NEWMGS_NOTE);
 						net.pool_info();
 						(new Thread(net)).start();
 					} else {
 						progress.remove();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			case NetworkClient.POOL_INFO:
 				json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					final JSONArray games = json.getJSONArray("games");
 					final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
 					pref.putString("poolinfo", games.toString());
 					pref.commit();
 
 					findViewById(R.id.game_search).setVisibility((games.length() == 0)? View.GONE : View.VISIBLE);
 
 					progress.remove();
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			case NetworkClient.NEW_GAME:
 			case NetworkClient.NUDGE_GAME:
 				json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					progress.setText("Updating Game List");
 					final SyncClient sync = new SyncClient(context, handle);
 					(new Thread(sync)).start();
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			}
 		}
 	};
 
 	private class GameListPager extends PagerAdapter
 	{
 		@Override
 		public int getCount()
 		{
 			return 3;
 		}
 
 		@Override
 		public Object instantiateItem(final ViewGroup collection, final int position)
 		{
 			int type = Enums.ONLINE_GAME, yourmove = Enums.YOUR_TURN;
 
 			switch (position) {
 			case THEIR_PAGE:
 				yourmove = Enums.THEIR_TURN;
 				break;
 			case YOUR_PAGE:
 				// already initialized
 				break;
 			case ARCHIVE_PAGE:
 				type = Enums.ARCHIVE_GAME;
 				break;
 			}
 			final GameListAdapter list = new GameListAdapter(context, type, yourmove);
 			gamelistadapter_arr[position] = list;
 
 			final FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.gamelist_listview, null);
 			final ListView listview = (ListView) layout.getChildAt(0);
 			final View empty = list.getEmptyView(context);
 
 			layout.addView(empty, 1);
 			listview.setEmptyView(empty);
 			listview.setAdapter(list);
 			listview.setOnItemClickListener(GameListOnline.this);
 			registerForContextMenu(listview);
 
 			((ViewPager) collection).addView(layout, 0);
 
 			return layout;
 		}
 
 		@Override
 		public void destroyItem(final ViewGroup collection, final int position, final Object view)
 		{
 			gamelistadapter_arr[position].close();
 			((ViewPager) collection).removeView((FrameLayout) view);
 		}
 
 		@Override
 		public void startUpdate(final ViewGroup arg0)
 		{
 		}
 
 		@Override
 		public void finishUpdate(final ViewGroup arg0)
 		{
 		}
 
 		@Override
 		public boolean isViewFromObject(final View view, final Object object)
 		{
 			return view == ((FrameLayout) object);
 		}
 
 		@Override
 		public void restoreState(final Parcelable arg0, final ClassLoader arg1)
 		{
 		}
 
 		@Override
 		public Parcelable saveState()
 		{
 			return null;
 		}
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		context = this;
 		gamelistadapter_arr = new GameListAdapter[3];
 
 		// Set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		net = new NetworkClient(this, handle);
 		progress = new ProgressMsg(this);
 
 		// set content view
 		setContentView(R.layout.activity_gamelist_online);
 
 	try {
 		// Set "waiting for opponent"
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		final JSONArray pool = new JSONArray(pref.getString("poolinfo", "[]"));
 		final View tpool = findViewById(R.id.game_search);
 		tpool.setVisibility((pool.length() == 0)? View.GONE : View.VISIBLE);
 		tpool.setOnClickListener(this);
 		tpool.setOnTouchListener(this);
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 
 		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
 		button.setOnLongClickListener(this);
 		button = (ImageView) findViewById(R.id.topbar_plus);
 		button.setOnClickListener(this);
 
 		final SwipeTabsPagerAdapter tabAdapter = new SwipeTabsPagerAdapter(this, getSupportFragmentManager());
 		tabAdapter.setTitles(new String[]{"Their Turn", "Your Turn", "Archive Games"});
 
 		final SwipeTabs swipetabs = (SwipeTabs) findViewById(R.id.swipetabs);
 		swipetabs.setAdapter(tabAdapter);
 
 		pager = (ViewPager) findViewById(R.id.swipe_list);
 		tabAdapter.setViewPager(pager);
 		pager.setAdapter(new GameListPager());
 		pager.setOnPageChangeListener(swipetabs);
 		pager.setCurrentItem(YOUR_PAGE);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		updateGameListAdapters();
 
 		// start background notifier
 		startService(new Intent(this, GenesisNotifier.class));
 
 		NetActive.inc();
 		progress.setText("Updating Game List");
 
 		final SyncClient sync = new SyncClient(this, handle);
 		(new Thread(sync)).start();
 
 		AdsHandler.run(this);
 	}
 
 	@Override
 	public void onPause()
 	{
 		NetActive.dec();
 		super.onPause();
 	}
 
 	@Override
 	public void onDestroy()
 	{
		for (int i = 0; i < 3; i++)
			gamelistadapter_arr[i].close();
 		super.onDestroy();
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.game_search:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				v.setBackgroundColor(0xff00b7eb);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				v.setBackgroundColor(0x00ffffff);
 			break;
 		}
 		return false;
 	}
 
 	public void onClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar_plus:
 			(new NewOnlineGameDialog(v.getContext(), handle)).show();
 			break;
 		case R.id.game_search:
 			(new GamePoolDialog(v.getContext())).show();
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
 		getMenuInflater().inflate(R.menu.options_gamelist_online, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.resync:
 			progress.setText("Updating Game List");
 
 			final SyncClient sync = new SyncClient(this, handle);
 			(new Thread(sync)).start();
 			break;
 		case R.id.readall_msgs:
 			(new ReadAllMsgsDialog(this, handle)).show();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
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
 		startActivity(intent);
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		switch (pager.getCurrentItem()) {
 		case THEIR_PAGE:
 			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 			final GameListAdapter listAdapter = gamelistadapter_arr[pager.getCurrentItem()];
 			final Bundle bundle = (Bundle) listAdapter.getItem((int) info.id);
 
 			if (bundle.getString("idle").equals("1")) {
 				getMenuInflater().inflate(R.menu.context_gamelist_online_nudge, menu);
 				break;
 			}
 		case YOUR_PAGE:
 			getMenuInflater().inflate(R.menu.context_gamelist_online, menu);
 			break;
 		case ARCHIVE_PAGE:
 			getMenuInflater().inflate(R.menu.context_gamelist_archive, menu);
 			break;
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item)
 	{
 		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		final GameListAdapter listAdapter = gamelistadapter_arr[pager.getCurrentItem()];
 		final Bundle bundle = (Bundle) listAdapter.getItem((int) info.id);
 
 		switch (item.getItemId()) {
 		case R.id.delete_game:
 			(new DeleteArchiveDialog(this, handle, bundle.getString("gameid"))).show();
 			break;
 		case R.id.local_copy:
 			final int type = (pager.getCurrentItem() == ARCHIVE_PAGE)? Enums.ARCHIVE_GAME : Enums.ONLINE_GAME;
 			(new CopyGameConfirm(this, bundle.getString("gameid"), type)).show();
 			break;
 		case R.id.rematch:
 			final String username = listAdapter.getExtras().getString("username");
 			final String opponent = username.equals(bundle.getString("white"))?
 					bundle.getString("black") : bundle.getString("white");
 			(new RematchConfirm(this, handle, opponent)).show();
 			break;
 		case R.id.nudge:
 			(new NudgeConfirm(this, handle, bundle.getString("gameid"))).show();
 			break;
 		default:
 			return super.onContextItemSelected(item);
 		}
 		return true;
 	}
 
 	private void updateGameListAdapters()
 	{
 		for (int i = 0; i < 3; i++) {
 			if (gamelistadapter_arr[i] != null)
 				gamelistadapter_arr[i].update();
 		}
 	}
 }
