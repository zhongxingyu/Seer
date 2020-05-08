 package org.svcba.scoreboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 import org.svcba.scoreboard.dialog.AssistActivity;
 import org.svcba.scoreboard.dialog.OffCourtPlayerActivity;
 import org.svcba.scoreboard.dialog.ReboundActivity;
 import org.svcba.scoreboard.dialog.RefTimeoutActivity;
 import org.svcba.scoreboard.dialog.ShootPlayerActivity;
 import org.svcba.scoreboard.dialog.ShootResultActivity;
 import org.svcba.scoreboard.dialog.StatActivity;
 import org.svcba.scoreboard.dialog.StealActivity;
 import org.svcba.scoreboard.dialog.TimeoutActivity;
 import org.svcba.scoreboard.dialog.ActionRemoveActivity;
 import org.svcba.scoreboard.model.Action;
 import org.svcba.scoreboard.model.Game;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.AdapterView;
 
 public class NormalActivity extends Activity
 {
 	private static final int REQUEST_TIMEOUT_TEAM = 0;
 	private static final int REQUEST_SHOOT_2_PLAYER = 1;
 	private static final int REQUEST_SHOOT_2_RESULT = 2;
 	private static final int REQUEST_REBOUND = 3;
 	private static final int REQUEST_SHOOT_1_PLAYER = 4;
 	private static final int REQUEST_SHOOT_1_RESULT = 5;
 	private static final int REQUEST_SHOOT_3_PLAYER = 6;
 	private static final int REQUEST_SHOOT_3_RESULT = 7;
 	private static final int REQUEST_FOUL = 8;
 	private static final int REQUEST_TURNOVER = 9;
 	private static final int REQUEST_SUB_OFF = 10;
 	private static final int REQUEST_SUB_ON = 11;
 	private static final int REQUEST_STEAL = 12;
 	private static final int REQUEST_ASSIST = 13;
 	private static final int CONFIRM_ACTION_REMOVE = 14;
 	private Game _game;
 	private Timer _timer = new Timer(true);
 
 	private Handler _handler = new Handler()
 	{
 		@Override
 		public void handleMessage(Message msg)
 		{
 			switch (msg.what)
 			{
 				case 1:
 					updateTime();
 					updateTimeoutFoul();
 					break;
 				default:
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	private TimerTask _timertask = new TimerTask()
 	{
 
 		@Override
 		public void run()
 		{
 			Message msg = new Message();
 			msg.what = 1;
 			_handler.sendMessage(msg);
 		}
 
 	};
 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.normal);
 
 		SVCBAApp app = (SVCBAApp)getApplicationContext();
 		_game = app.getGame();
 
 		TextView tx = (TextView)findViewById(R.id.tx_hometeam_name);
 		tx.setText(_game.getHomeTeam().getName());
 
 		tx = (TextView)findViewById(R.id.tx_awayteam_name);
 		tx.setText(_game.getAwayTeam().getName());
 
 		updateScore();
 		updateTimeoutFoul();
 		updateTime();
 
 		Button btn = (Button)findViewById(R.id.btn_timeout);
 		btn.setOnClickListener(new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent intent = new Intent(v.getContext(), TimeoutActivity.class);
 				startActivityForResult(intent, REQUEST_TIMEOUT_TEAM);
 			}
 		});
 		btn = (Button)findViewById(R.id.btn_sub);
 		btn.setOnClickListener(new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent intent = new Intent(v.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_SUB_OFF);
 			}
 		});
 
 		btn = (Button)findViewById(R.id.btn_foul);
 		btn.setOnClickListener(new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent intent = new Intent(v.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_FOUL);
 			}
 		});
 		btn = (Button)findViewById(R.id.btn_turnover);
 		btn.setOnClickListener(new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent intent = new Intent(v.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_TURNOVER);
 			}
 		});
 		btn = (Button)findViewById(R.id.btn_shoot1);
 		btn.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View view)
 			{
 				Intent intent = new Intent(view.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_1_PLAYER);
 			}
 
 		});
 		btn = (Button)findViewById(R.id.btn_shoot2);
 		btn.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View view)
 			{
 				Intent intent = new Intent(view.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_2_PLAYER);
 			}
 
 		});
 		btn = (Button)findViewById(R.id.btn_shoot3);
 		btn.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View view)
 			{
 				Intent intent = new Intent(view.getContext(), ShootPlayerActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_3_PLAYER);
 			}
 
 		});
 		_timer.schedule(_timertask, 1000, 1000);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.normal_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.menu_quit:
 				System.exit(0);
 				break;
 			case R.id.menu_home_stat:
 				Intent intent = new Intent(this, StatActivity.class);
 				intent.putExtra("side", StatActivity.HOME);
 				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(intent);
 				break;
 			case R.id.menu_away_stat:
 				intent = new Intent(this, StatActivity.class);
 				intent.putExtra("side", StatActivity.AWAY);
 				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(intent);
 				break;
 			case R.id.menu_ref_timeout:
 				_game.setRefTimeout();
 				intent = new Intent(this, RefTimeoutActivity.class);
 				startActivity(intent);
 				break;
 			case R.id.menu_chart:
 				showChart();
 				break;
 			case R.id.menu_undo_action:
 				//Toast.makeText(this, R.string.err_not_implement, Toast.LENGTH_LONG).show();
 				intent = new Intent(this, ActionRemoveActivity.class);
 				intent.putExtra("pos2Remove", 0);  // 0 is the latest action
 				startActivityForResult(intent, CONFIRM_ACTION_REMOVE);
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == CONFIRM_ACTION_REMOVE)
 		{
 			int pos2Remove = data.getIntExtra("pos2Remove", 0);
 			if (resultCode == RESULT_OK)
 			{			
 				//Toast.makeText( this, "Confirm revmoal at item " + pos2Remove, Toast.LENGTH_LONG ).show();
 				boolean removed = _game.removeActionFromTheEnd(pos2Remove);            
 				if (!removed)
 				{
 					Toast.makeText( this, "ItemClick at item " + pos2Remove + "can't be remove!", Toast.LENGTH_LONG ).show();
 		        }
 				updateAction();
 				updateScore();
 			}
 			else if(resultCode == RESULT_CANCELED)
 			{
 				//Toast.makeText( this, "Cancel Removal at item " + pos2Remove, Toast.LENGTH_LONG ).show();
 			}
 		}
 		else if (requestCode == REQUEST_TIMEOUT_TEAM)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action tempaction;
 				tempaction = createAction();
 				tempaction.setAction(Action.TIMEOUT);
 				int team = data.getIntExtra("team", 0);
 				if (team == TimeoutActivity.RESULT_HOMETEAM)
 				{
 					tempaction.setTeam(_game.getHomeTeam());
 					_game.addHomeTimeout(1);
 				}
 				else if (team == TimeoutActivity.RESULT_AWAYTEAM)
 				{
 					tempaction.setTeam(_game.getAwayTeam());
 					_game.addAwayTimeout(1);
 				}
 				_game.addAction(tempaction);
 				updateAction();
 				updateTimeoutFoul();
 			}
 			else
 			{
 			}
 		}
 		else if (requestCode == REQUEST_SUB_OFF)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.SUB_OFF);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 				}
 				_game.setTempAction(action);
 				Intent intent = new Intent(this, OffCourtPlayerActivity.class);
 				intent.putExtra("team", team);
 				intent.putExtra("off", pos);
 				startActivityForResult(intent, REQUEST_SUB_ON);
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_SUB_ON)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.SUB_ON);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				int off = data.getIntExtra("off", 0);
 				if (team == TimeoutActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOffCourt().get(pos));
 					_game.getHomeTeam().moveFromCourt(off);
 					_game.getHomeTeam().moveToCourt(pos);
 				}
 				else if (team == TimeoutActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOffCourt().get(pos));
 					_game.getAwayTeam().moveFromCourt(off);
 					_game.getAwayTeam().moveToCourt(pos);
 				}
 				_game.addAction(_game.getTempAction());
 				_game.addAction(action);
 				updateAction();
 			}
 			else
 			{
 			}
 		}
 		else if (requestCode == REQUEST_FOUL)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.FOUL);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 					action.setSide(Action.HOME);
 					_game.addHomeFoul(1);
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 					action.setSide(Action.AWAY);
 					_game.addAwayFoul(1);
 				}
 				_game.addAction(action);
 				action.setResult(_game.getFoulTimes((String)action.getPlayer().get("name")));
 				updateAction();
 				updateTimeoutFoul();
 				if (action.getResult() >= 5)
 				{
 					Resources res = getResources();
 					String foul = (String)action.getPlayer().get("name");
 					foul += res.getString(R.string.foulout);
 					Toast.makeText(this, foul , Toast.LENGTH_LONG).show();
 				}
 			}
 			else
 			{
 
 			}
 
 		}
 		else if (requestCode == REQUEST_TURNOVER)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.TURNOVER);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 				}
 				_game.addAction(action);
 				updateAction();
 				Intent intent = new Intent(this, StealActivity.class);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					intent.putExtra("team", Action.AWAY);
 				}
 				else
 				{
 					intent.putExtra("team", Action.HOME);
 				}
 				startActivityForResult(intent, REQUEST_STEAL);
 			}
 			else
 			{
 
 			}
 
 		}
 		else if (requestCode == REQUEST_SHOOT_1_PLAYER)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.SHOOT_1);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 					action.setSide(Action.HOME);
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 					action.setSide(Action.AWAY);
 				}
 				_game.setTempAction(action);
 				Intent intent = new Intent(this, ShootResultActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_1_RESULT);
 			}
 			else
 			{
 
 			}
 
 		}
 		else if (requestCode == REQUEST_SHOOT_1_RESULT)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = _game.getTempAction();
 				int result = data.getIntExtra("result", 0);
 				if (result == ShootResultActivity.MADE)
 				{
 					action.setResult(Action.MADE);
 					if (action.getSide() == Action.HOME)
 					{
 						_game.addHomeScore(1);
 					}
 					else
 					{
 						_game.addAwayScore(1);
 					}
 					_game.addAction(action);
 					updateScore();
 					updateAction();
 				}
 				else if (result == ShootResultActivity.MISS)
 				{
 					action.setResult(Action.MISS);
 					_game.addAction(action);
 					updateAction();
 					Intent intent = new Intent(this, ReboundActivity.class);
 					if (action.getSide() == Action.HOME)
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_HOMETEAM);
 					else
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_AWAYTEAM);
 					startActivityForResult(intent, REQUEST_REBOUND);
 				}
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_SHOOT_2_PLAYER)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.SHOOT_2);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 					action.setSide(Action.HOME);
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 					action.setSide(Action.AWAY);
 				}
 				_game.setTempAction(action);
 				Intent intent = new Intent(this, ShootResultActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_2_RESULT);
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_SHOOT_2_RESULT)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = _game.getTempAction();
 				int result = data.getIntExtra("result", 0);
 				if (result == ShootResultActivity.MADE)
 				{
 					action.setResult(Action.MADE);
 					if (action.getSide() == Action.HOME)
 					{
 						_game.addHomeScore(2);
 					}
 					else
 					{
 						_game.addAwayScore(2);
 					}
 					_game.addAction(action);
 					updateScore();
 					updateAction();
 					Intent intent = new Intent(this, AssistActivity.class);
 					intent.putExtra("team", action.getSide());
 					startActivityForResult(intent, REQUEST_ASSIST);
 				}
 				else if (result == ShootResultActivity.MISS)
 				{
 					action.setResult(Action.MISS);
 					_game.addAction(action);
 					updateAction();
 					Intent intent = new Intent(this, ReboundActivity.class);
 					if (action.getSide() == Action.HOME)
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_HOMETEAM);
 					else
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_AWAYTEAM);
 					startActivityForResult(intent, REQUEST_REBOUND);
 				}
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_SHOOT_3_PLAYER)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action;
 				action = createAction();
 				action.setAction(Action.SHOOT_3);
 				int team = data.getIntExtra("team", 0);
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 					action.setSide(Action.HOME);
 				}
 				else if (team == ShootPlayerActivity.RESULT_AWAYTEAM)
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 					action.setSide(Action.AWAY);
 				}
 				_game.setTempAction(action);
 				Intent intent = new Intent(this, ShootResultActivity.class);
 				startActivityForResult(intent, REQUEST_SHOOT_3_RESULT);
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_SHOOT_3_RESULT)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = _game.getTempAction();
 				int result = data.getIntExtra("result", 0);
 				if (result == ShootResultActivity.MADE)
 				{
 					action.setResult(Action.MADE);
 					if (action.getSide() == Action.HOME)
 					{
 						_game.addHomeScore(3);
 					}
 					else
 					{
 						_game.addAwayScore(3);
 					}
 					_game.addAction(action);
 					updateScore();
 					updateAction();
 					Intent intent = new Intent(this, AssistActivity.class);
 					intent.putExtra("team", action.getSide());
 					startActivityForResult(intent, REQUEST_ASSIST);
 				}
 				else if (result == ShootResultActivity.MISS)
 				{
 					action.setResult(Action.MISS);
 					_game.addAction(action);
 					updateAction();
 					Intent intent = new Intent(this, ReboundActivity.class);
 					if (action.getSide() == Action.HOME)
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_HOMETEAM);
 					else
 						intent.putExtra("shoot", ShootPlayerActivity.RESULT_AWAYTEAM);
 					startActivityForResult(intent, REQUEST_REBOUND);
 				}
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_REBOUND)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = createAction();
 				int shoot = data.getIntExtra("shoot", -1);
 				int team = data.getIntExtra("team", -1);
 				if (shoot == team)
 				{
 					action.setAction(Action.REBOUND_F);
 				}
 				else
 				{
 					action.setAction(Action.REBOUND_B);
 				}
 				int pos = data.getIntExtra("pos", 0);
 				if (team == ShootPlayerActivity.RESULT_HOMETEAM)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 				}
 				else
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 				}
 				_game.addAction(action);
 				updateAction();
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_STEAL)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = createAction();
 				action.setAction(Action.STEAL);
 				int pos = data.getIntExtra("pos", -1);
 				int team = data.getIntExtra("team", -1);
 				if (team == Action.HOME)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 				}
 				else
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 				}
 				_game.addAction(action);
 				updateAction();
 			}
 			else
 			{
 
 			}
 		}
 		else if (requestCode == REQUEST_ASSIST)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Action action = createAction();
 				action.setAction(Action.ASSIST);
 				int pos = data.getIntExtra("pos", -1);
 				int team = data.getIntExtra("team", -1);
 				if (team == Action.HOME)
 				{
 					action.setPlayer(_game.getHomeTeam().getOnCourt().get(pos));
 				}
 				else
 				{
 					action.setPlayer(_game.getAwayTeam().getOnCourt().get(pos));
 				}
 				_game.addAction(action);
 				updateAction();
 			}
 			else
 			{
 
 			}
 		}
 	}
 	private Action createAction()
 	{
 		Action result = new Action();
 		result.setTime(System.currentTimeMillis()/1000, _game.getCurrentQuarterInt(), _game.getCurrentTimeInt());
 		return result;
 	}
 	private void updateTime()
 	{
 		_game.runOneSec();
 		int status = _game.getGameStatus();
 		String timeout = _game.getCurrentTimeout();
 		String time = _game.getCurrentTime();
 		String quarter = _game.getCurrentQuarter();
 
 		TextView tv;
 		if (status == Game.GAME_NORMAL)
 		{
 			tv = (TextView)findViewById(R.id.tx_time);
 			tv.setText(time);
 			tv = (TextView)findViewById(R.id.tx_time_backup);
 			tv.setText("");
 		}
 		else if (status == Game.GAME_PAUSE)
 		{
 			tv = (TextView)findViewById(R.id.tx_time);
 			tv.setText(timeout);
 			tv = (TextView)findViewById(R.id.tx_time_backup);
 			tv.setText(time);
 		}
 		tv = (TextView)findViewById(R.id.tx_quarter);
 		tv.setText(quarter);
 		if (_game.getGameStatus() == Game.GAME_OVER)
 		{
 			tv = (TextView)findViewById(R.id.tx_time);
 			tv.setText(time);
 			tv = (TextView)findViewById(R.id.tx_time_backup);
 			tv.setText("");
 			_timer.cancel();
 		}
 	}
 	private void updateScore()
 	{
 		TextView tv;
 		int home = _game.getHomeScore();
 		int away = _game.getAwayScore();
 		int homecolor;
 		int awaycolor;
 		if (home == away)
 		{
 			homecolor = R.color.cl_score_tie;
 			awaycolor = R.color.cl_score_tie;
 		}
 		else if (home > away)
 		{
 			homecolor = R.color.cl_score_lead;
 			awaycolor = R.color.cl_score_behind;
 		}
 		else
 		{
 			homecolor = R.color.cl_score_behind;
 			awaycolor = R.color.cl_score_lead;
 		}
 		Resources res = getResources();
 		tv = (TextView)findViewById(R.id.tx_hometeam_score);
 		tv.setText("" + home);
 		tv.setTextColor(res.getColor(homecolor));
 
 		tv = (TextView)findViewById(R.id.tx_awayteam_score);
 		tv.setText("" + away);
 		tv.setTextColor(res.getColor(awaycolor));
 	}
 	private void updateTimeoutFoul()
 	{
 		Resources res = getResources();
 		TextView tv;
 
 		tv = (TextView)findViewById(R.id.tx_hometeam_timeout);
 		tv.setText(res.getString(R.string.ab_timeout)+_game.getHomeTimeout());
 
 		tv = (TextView)findViewById(R.id.tx_awayteam_timeout);
 		tv.setText(res.getString(R.string.ab_timeout)+ _game.getAwayTimeout());
 
 		tv = (TextView)findViewById(R.id.tx_hometeam_foul);
 		tv.setText(res.getString(R.string.ab_foul)+_game.getHomeFoul());
 
 		tv = (TextView)findViewById(R.id.tx_awayteam_foul);
 		tv.setText(res.getString(R.string.ab_foul)+_game.getAwayFoul());
 	}
 
     private class myLongClickListener implements OnItemLongClickListener
 	{
         public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id ) 
         {
         	// Try to open up the action removal dialog/activity .
         	// Then transfer the result out.
         	Intent intent = new Intent(view.getContext(), ActionRemoveActivity.class);
         	intent.putExtra("pos2Remove", position);
 			startActivityForResult(intent, CONFIRM_ACTION_REMOVE);
             return false;
         }
     }
 
 	private void updateAction()
 	{
 		List<Action> actions = _game.getLastActions();
 		List<Map<String, Object>> show = new ArrayList<Map<String, Object>>();
 		Resources res = getResources();
 		for (Action a : actions)
 		{
 			Map<String, Object> action = new HashMap<String, Object>();
 			switch (a.getAction())
 			{
 				case Action.TIMEOUT:
 					action.put("who", a.getTeam().getName());
 					action.put("do", res.getString(R.string.timeout));
 					action.put("what", "");
 					break;
 				case Action.SUB_OFF:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.sub_off));
 					action.put("what", "");
 					break;
 				case Action.SUB_ON:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.sub_on));
 					action.put("what", "");
 					break;
 				case Action.FOUL:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.foul));
 					action.put("what", ""+a.getResult());
 					break;
 				case Action.TURNOVER:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.turnover));
 					action.put("what","");
 					break;
 				case Action.SHOOT_1:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.shoot1));
 					if (a.getResult() == Action.MADE)
 						action.put("what", res.getString(R.string.made));
 					else
 						action.put("what", res.getString(R.string.miss));
 					break;
 				case Action.SHOOT_2:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.shoot2));
 					if (a.getResult() == Action.MADE)
 						action.put("what", res.getString(R.string.made));
 					else
 						action.put("what", res.getString(R.string.miss));
 					break;
 				case Action.SHOOT_3:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.shoot3));
 					if (a.getResult() == Action.MADE)
 						action.put("what", res.getString(R.string.made));
 					else
 						action.put("what", res.getString(R.string.miss));
 					break;
 				case Action.REBOUND_B:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.rebound_b));
 					action.put("what", "");
 					break;
 				case Action.REBOUND_F:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.rebound_f));
 					action.put("what", "");
 					break;
 				case Action.STEAL:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.steal));
 					action.put("what", "");
 					break;
 				case Action.ASSIST:
 					action.put("who", a.getPlayer().get("name"));
 					action.put("do", res.getString(R.string.assist));
 					action.put("what", "");
 					break;
 				default:
 			}
 			show.add(action);
 		}
 
 		ListView lv = (ListView)findViewById(R.id.actions);
         SimpleAdapter sa = new SimpleAdapter(this, show,R.layout.action,new String[]{"who","do","what"},new int[]{R.id.action_who,R.id.action_do,R.id.action_what});
         lv.setAdapter(sa);
 
         myLongClickListener listener = new myLongClickListener();
         lv.setOnItemLongClickListener( listener ) ;
 	}
 
    public void onDestroy()
    {
    	super.onDestroy();
    	_timer.cancel();
    }
 	public void onBackPressed()
 	{
 
 	}
 	private void showChart()
 	{
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		XYSeries homedata = new XYSeries(_game.getHomeTeam().getName());
 		XYSeries awaydata = new XYSeries(_game.getAwayTeam().getName());
 		XYSeriesRenderer homerender = new XYSeriesRenderer();
 		XYSeriesRenderer awayrender = new XYSeriesRenderer();
 		homerender.setColor(Color.BLUE);
 		awayrender.setColor(Color.GREEN);
 		List<Action> actions = _game.getActions();
 		homedata.add(0, 0);
 		awaydata.add(0, 0);
 		int home = 0;
 		int away = 0;
 		int time = 0;
 		for (Action action : actions)
 		{
 			int q = action.getQuarter();
 			int m = action.getMoment();
 			int t = 0;
 			if (q <=4)
 			{
 				t = Game.MIN_PER_QTR*60 - m + (q-1)*Game.MIN_PER_QTR*60;
 			}
 			else
 			{
 				t = 2*60 - m + (q - 4 -1)*2*60 + 4 * Game.MIN_PER_QTR * 60;
 			}
 			if (t/60 > time)
 			{
 				time = t/60 ;
 				homedata.add(time, home);
 				awaydata.add(time, away);
 			}
 			if (action.getAction() == Action.SHOOT_1 && action.getResult() == Action.MADE)
 			{
 				if (action.getSide() == Action.HOME)
 				{
 					home ++;
 				}
 				else
 				{
 					away ++;
 				}
 			}
 			else if (action.getAction() == Action.SHOOT_2 && action.getResult() == Action.MADE)
 			{
 				if (action.getSide() == Action.HOME)
 				{
 					home += 2;
 				}
 				else
 				{
 					away += 2;
 				}
 			}
 			else if (action.getAction() == Action.SHOOT_3 && action.getResult() == Action.MADE)
 			{
 				if (action.getSide() == Action.HOME)
 				{
 					home += 3;
 				}
 				else
 				{
 					away += 3;
 				}
 			}
 		}
 		homedata.add(time+1, home);
 		awaydata.add(time+1, away);
 		renderer.addSeriesRenderer(homerender);
 		renderer.addSeriesRenderer(awayrender);
 		dataset.addSeries(homedata);
 		dataset.addSeries(awaydata);
 		Intent intent = ChartFactory.getLineChartIntent(this, dataset, renderer, getResources().getString(R.string.stat_chart));
 		startActivity(intent);
 	}
 }
