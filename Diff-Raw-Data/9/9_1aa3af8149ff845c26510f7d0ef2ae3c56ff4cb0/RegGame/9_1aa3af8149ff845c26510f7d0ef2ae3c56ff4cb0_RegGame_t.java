 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 public class RegGame extends Activity implements OnClickListener, OnLongClickListener, OnTouchListener
 {
 	public static RegGame self;
 	public static ViewFlip3D game_board;
 
 	private WakeLock wakelock;
 	private RegGameState gamestate;
 	private Bundle settings;
 	private int type;
 	private boolean newMsgs = false;
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		self = this;
 
 		// Set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		// create gamestate instance
 		settings = (savedInstanceState != null)?
 			savedInstanceState : getIntent().getExtras();
 		type = settings.getInt("type");
 
 		// set content view
 		if (type != Enums.LOCAL_GAME)
 			setContentView(R.layout.activity_game_online);
 		else
 			setContentView(R.layout.activity_game_local);
 
 		// must be called after setContentView
 		gamestate = new RegGameState(this, settings);
 
 		// set click listeners
 		ImageView button = (ImageView) findViewById((type != Enums.LOCAL_GAME)? R.id.topbar_genesis : R.id.topbar);
 		button.setOnTouchListener(this);
 		button.setOnLongClickListener(this);
 
 		if (type != Enums.LOCAL_GAME) {
 			button = (ImageView) findViewById(R.id.chat);
 			button.setOnTouchListener(this);
 			button.setOnClickListener(this);
 		}
 
 		final int list[] = new int[]{R.id.place_piece, R.id.backwards,
 			R.id.forwards, R.id.current};
 		for (int i = 0; i < list.length; i++) {
 			button = (ImageView) findViewById(list[i]);
 			button.setOnTouchListener(this);
 			button.setOnClickListener(this);
 		}
 
 		game_board = (ViewFlip3D) findViewById(R.id.board_flip);
 	}
 
 	@Override
 	public void onSaveInstanceState(final Bundle savedInstanceState)
 	{
 		savedInstanceState.putAll(gamestate.getBundle());
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if (type == Enums.ONLINE_GAME)
 			NetActive.inc();
 		if (type != Enums.LOCAL_GAME) {
 			final GameDataDB db = new GameDataDB(this);
 			final int count = db.getUnreadMsgCount(settings.getString("gameid"));
 			final int img = (count > 0)? R.drawable.newmsg : R.drawable.chat;
 
 			newMsgs = (count > 0);
 
 			final ImageView v = (ImageView) findViewById(R.id.chat);
 			((ImageView) v).setImageResource(img);
 			db.close();
 		}
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean("screenAlwaysOn", false)) {
 			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "GenesisChess");
 			wakelock.acquire();
 		}
 	}
 
 	@Override
 	public void onPause()
 	{
 		gamestate.save(this, true);
 		if (type == Enums.ONLINE_GAME)
 			NetActive.dec();
 
 		if (wakelock != null)
 			wakelock.release();
 
 		super.onPause();
 	}
 
 	public void onClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.place_piece:
 			game_board.flip();
 			break;
 		case R.id.chat:
 			final Intent intent = new Intent(this, MsgBox.class);
 			intent.putExtra("gameid", settings.getString("gameid"));
 			startActivity(intent);
 			break;
 		case R.id.backwards:
 			gamestate.backMove();
 			break;
 		case R.id.forwards:
 			gamestate.forwardMove();
 			break;
 		case R.id.current:
 			gamestate.currentMove();
 			break;
 		}
 	}
 
 	public boolean onLongClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 		case R.id.topbar_genesis:
 			gamestate.save(this, true);
 			finish();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.place_piece:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.place_piece_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.place_piece);
 			break;
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
 		case R.id.chat:
 			if (newMsgs) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.newmsg_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.newmsg);
 			} else {
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 					((ImageView) v).setImageResource(R.drawable.chat_pressed);
 				else if (event.getAction() == MotionEvent.ACTION_UP)
 					((ImageView) v).setImageResource(R.drawable.chat);
 			}
 			break;
 		case R.id.backwards:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.backwards_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.backwards);
 			break;
 		case R.id.forwards:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.forwards_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.forwards);
 			break;
 		case R.id.current:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.current_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.current);
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			getMenuInflater().inflate(R.menu.options_game_local, menu);
 			break;
 		case Enums.ONLINE_GAME:
 			getMenuInflater().inflate(R.menu.options_game_online, menu);
 			break;
 		case Enums.ARCHIVE_GAME:
 			getMenuInflater().inflate(R.menu.options_game_archive, menu);
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.first_move:
 			gamestate.firstMove();
 			break;
 		case R.id.resync:
 			gamestate.resync();
 			break;
 		case R.id.resign:
 			gamestate.resign();
 			break;
 		case R.id.rematch:
 			gamestate.rematch();
 			break;
 		case R.id.cpu_time:
 			gamestate.setCpuTime();
 			break;
 		case R.id.chat:
 			final Intent intent = new Intent(this, MsgBox.class);
 			intent.putExtra("gameid", settings.getString("gameid"));
 			startActivity(intent);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	public void onActivityResult(final int reques, final int result, final Intent data)
 	{
 		switch (result) {
 		case RESULT_OK:
 			gamestate.submitMove();
 			break;
 		case RESULT_CANCELED:
 			gamestate.undoMove();
 			break;
 		}
 	}
 
 	public void displaySubmitMove()
 	{
 		startActivityForResult(new Intent(self, SubmitMove.class), 1);
 	}
 
 	public void reset()
 	{
 		for (int i = 0; i < 64; i++) {
 			final BoardButton square = (BoardButton) findViewById(i);
 			square.setPiece(RegBoard.InitRegBoard[i]);
 		}
 		for (int i = 94; i < 100; i++) {
 			final PlaceButton piece = (PlaceButton) findViewById(i);
 			piece.reset();
 		}
 		for (int i = 101; i < 107; i++) {
 			final PlaceButton piece = (PlaceButton) findViewById(i);
 			piece.reset();
 		}
 		gamestate.setStm();
 	}
 }
