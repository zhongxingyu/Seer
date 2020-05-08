 /*	GenesisChess, an Android chess application
 	Copyright (C) 2012, Justin Madru (justin.jdm64@gmail.com)
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.chess.genesis.engine;
 
 import android.app.*;
 import android.content.*;
 import android.content.SharedPreferences.Editor;
 import android.os.*;
 import android.preference.*;
 import android.view.*;
 import android.widget.*;
 import com.chess.genesis.*;
 import com.chess.genesis.activity.*;
 import com.chess.genesis.data.*;
 import com.chess.genesis.dialog.*;
 import com.chess.genesis.net.*;
 import com.chess.genesis.util.*;
 import com.chess.genesis.view.*;
 import java.util.*;
 import org.json.*;
 
 public abstract class GameState
 {
 	protected Handler handle;
 	protected GameFrag game;
 	protected Activity activity;
 	protected Bundle settings;
 
 	protected NetworkClient net;
 	protected ProgressMsg progress;
 	protected ObjectArray<Move> history;
 	protected Board board;
 	protected Engine cpu;
 	protected IntArray callstack;
 
 	protected int ycol;
 	protected int type;
 	protected int oppType;
 	protected int hindex = -1;
 
 	protected abstract boolean runCPU();
 	protected abstract void applyRemoteMove(final String hist);
 
 	public abstract void firstMove();
 	public abstract void currentMove();
 	public abstract void forwardMove();
 	public abstract void undoMove();
 	public abstract void backMove();
 	public abstract void setBoard();
 
 	public abstract void boardClick(final View v);
 	public abstract void placeClick(final View v);
 
 	protected void handleOther(final Message msg)
 	{
 	try {
 		switch (msg.what) {
 		case CpuTimeDialog.MSG:
 			final Editor pref = PreferenceManager.getDefaultSharedPreferences(activity).edit();
 			pref.putInt(PrefKey.CPUTIME, (Integer) msg.obj);
 			pref.commit();
 			cpu.setTime((Integer) msg.obj);
 			break;
 		case NetworkClient.GAME_DRAW:
 		case NetworkClient.SUBMIT_MOVE:
 			JSONObject json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error")) {
 				undoMove();
 				progress.remove();
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 				return;
 			}
 			progress.setText("Checking Game Status");
 
 			net.game_status(settings.getString("gameid"));
 			new Thread(net).start();
 			break;
 		case ResignConfirm.MSG:
 			progress.setText("Sending Resignation");
 
 			net.resign_game(settings.getString("gameid"));
 			new Thread(net).start();
 			break;
 		case NudgeConfirm.MSG:
 			progress.setText("Sending Nudge");
 
 			net.nudge_game(settings.getString("gameid"));
 			new Thread(net).start();
 			break;
 		case IdleResignConfirm.MSG:
 			progress.setText("Sending Idle Resign");
 
 			net.idle_resign(settings.getString("gameid"));
 			new Thread(net).start();
 			break;
 		case DrawDialog.MSG:
 		case AcceptDrawDialog.MSG:
 			final String value = (String) msg.obj;
 			progress.setText("Sending Draw");
 
 			net.game_draw(settings.getString("gameid"), value);
 			new Thread(net).start();
 			break;
 		case NetworkClient.RESIGN_GAME:
 		case NetworkClient.IDLE_RESIGN:
 			json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error")) {
 				progress.remove();
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 				return;
 			}
 			progress.setText("Resignation Sent");
 
 			net.game_status(settings.getString("gameid"));
 			new Thread(net).start();
 			break;
 		case NetworkClient.NUDGE_GAME:
 			json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error"))
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 			progress.remove();
 			break;
 		case NetworkClient.GAME_STATUS:
 			json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error")) {
 				progress.remove();
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 				return;
 			}
 			final int status = Enums.GameStatus(json.getString("status"));
 
 			settings.putString("status", String.valueOf(status));
 
 			final GameDataDB db = new GameDataDB(activity);
 			db.updateOnlineGame(json);
 			db.close();
 			GenesisNotifier.clearNotification(activity, GenesisNotifier.YOURTURN_NOTE);
 
 			applyRemoteMove(json.getString("history"));
 			if (status != Enums.ACTIVE) {
 				if (Integer.parseInt(settings.getString("eventtype")) == Enums.INVITE) {
 					progress.remove();
 
 					json.put("yourcolor", ycol);
 					json.put("white_name", settings.getString("white"));
 					json.put("black_name", settings.getString("black"));
 					json.put("eventtype", settings.getString("eventtype"));
 					json.put("status", settings.getString("status"));
 					json.put("gametype", Enums.GameType(Integer.parseInt(settings.getString("gametype"))));
 					json.put("gameid", settings.getString("gameid"));
 
 					new GameStatsDialog(activity, json).show();
 					return;
 				}
 				progress.setText("Retrieving Score");
 
 				net.game_score(settings.getString("gameid"));
 				new Thread(net).start();
 			} else {
 				progress.setText("Status Synced");
 				progress.remove();
 			}
 			break;
 		case NetworkClient.GAME_SCORE:
 			json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error")) {
 				progress.remove();
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 				return;
 			}
 			progress.setText("Score Loaded");
 			progress.remove();
 
 			ShowGameStats(json);
 			break;
 		case RematchConfirm.MSG:
 			final Bundle data = (Bundle) msg.obj;
 			progress.setText("Sending Newgame Request");
 
 			final String opponent = data.getString("opp_name");
 			final String color = Enums.ColorType(data.getInt("color"));
 			final String gametype = Enums.GameType(data.getInt("gametype"));
 
 			net.new_game(opponent, gametype, color);
 			new Thread(net).start();
 			break;
 		case NetworkClient.NEW_GAME:
 			json = (JSONObject) msg.obj;
 
 			if (json.getString("result").equals("error")) {
 				progress.remove();
 				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 				return;
 			}
 			progress.setText(json.getString("reason"));
 			progress.remove();
 			break;
 		}
 	} catch (final JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public void reset()
 	{
 		hindex = -1;
 		callstack.clear();
 		game.reset();
 	}
 
 	protected int yourColor()
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			if (oppType == Enums.HUMAN_OPPONENT)
 				return board.getStm();
 		case Enums.ONLINE_GAME:
 			return ycol;
 		case Enums.ARCHIVE_GAME:
 			return board.getStm();
 		default:
 			return 0;
 		}
 	}
 
 	protected void check_endgame()
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			return;
 		case Enums.ONLINE_GAME:
 			if (Integer.parseInt(settings.getString("status")) == Enums.ACTIVE) {
 				// check for draw offers and pending draws
 				if (Integer.parseInt(settings.getString("drawoffer")) * ycol < 0)
 					new AcceptDrawDialog(activity, handle).show();
 				else if (Integer.parseInt(settings.getString("drawoffer")) * ycol > 0)
 					new PendingDrawDialog(activity).show();
 				return;
 			} else if (Integer.parseInt(settings.getString("eventtype")) == Enums.INVITE) {
 			try {
 				ShowGameStats(new JSONObject());
 			} catch (final JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 			} else {
 				progress.setText("Retrieving Score");
 				net.game_score(settings.getString("gameid"));
 				new Thread(net).start();
 			}
 			break;
 		case Enums.ARCHIVE_GAME:
 			settings.putInt("yourcolor", ycol);
 			new GameStatsDialog(activity, settings).show();
 			break;
 		}
 	}
 
 	public Bundle getBundle()
 	{
 		return settings;
 	}
 
 	public void resync()
 	{
 		progress.setText("Updating Game State");
 		net.game_status(settings.getString("gameid"));
 		new Thread(net).start();
 	}
 
 	public void setCpuTime()
 	{
 		new CpuTimeDialog(activity, handle, cpu.getTime()).show();
 	}
 
 	public void submitMove()
 	{
 		progress.setText("Sending Move");
 
 		final String gameid = settings.getString("gameid");
 		final String move = history.top().toString();
 
 		net.submit_move(gameid, move);
 		new Thread(net).start();
 	}
 
 	public void save(final Context context, final boolean exitgame)
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			final GameDataDB db = new GameDataDB(context);
 			final int id = Integer.parseInt(settings.getString("id"));
 
 			if (history.size() < 1) {
 				db.deleteLocalGame(id);
 				db.close();
 				return;
 			} else if (exitgame) {
 				db.close();
 				return;
 			}
 			final long stime = new Date().getTime();
 			final String zfen = board.printZfen();
 			final String hist = history.toString();
 
 			// save update local game data bundle
 			settings.putString("history", hist);
 			settings.putString("stime", String.valueOf(stime));
 			settings.putString("zfen", zfen);
 
 			db.saveLocalGame(id, stime, zfen, hist);
 			db.close();
 			break;
 		case Enums.ONLINE_GAME:
 			if (exitgame)
 				return;
 			game.displaySubmitMove();
 		case Enums.ARCHIVE_GAME:
 			break;
 		}
 	}
 
 	public final void setStm()
 	{
 		String think = "", wstr, bstr;
 		boolean mate = false;
 
 		switch (board.isMate()) {
 		case Move.NOT_MATE:
 		default:
 			if (runCPU())
 				think = " thinking";
 			break;
 		case Move.CHECK_MATE:
 		case Move.STALE_MATE:
 			mate = true;
 			break;
 		}
 		if (type == Enums.LOCAL_GAME) {
 			wstr = "White";
 			bstr = "Black";
 		} else {
 			wstr = settings.getString("white");
 			bstr = settings.getString("black");
 		}
 
 		final TabText white = (TabText) activity.findViewById(R.id.white_name);
 		final TabText black = (TabText) activity.findViewById(R.id.black_name);
 
 		if (board.getStm() == Piece.WHITE) {
 			white.setText(wstr + think);
 			white.setActive(true);
 
 			black.setText(bstr);
 			black.setActive(false);
 
 			if (mate)
 				white.setTabTextColor(0xffeb0000);
 		} else {
 			white.setText(wstr);
 			white.setActive(false);
 
 			black.setText(bstr + think);
 			black.setActive(true);
 
 			if (mate)
 				black.setTabTextColor(0xffeb0000);
 		}
 	}
 
 	protected void setBoard(final int[] pieces)
 	{
 		for (int i = -6; i < 0; i++) {
 			final PlaceButton button = (PlaceButton) activity.findViewById(i + 1000);
 			button.setCount(pieces[i + 6]);
 		}
 		for (int i = 1; i < 7; i++) {
 			final PlaceButton button = (PlaceButton) activity.findViewById(i + 1000);
 			button.setCount(pieces[i + 6]);
 		}
 
 		// set board pieces
 		final int[] squares = board.getBoardArray();
 		for (int i = 0; i < 64; i++) {
 			final int loc = BaseBoard.SF88(i);
 			final BoardButton button = (BoardButton) activity.findViewById(loc);
 			button.setPiece(squares[loc]);
 		}
 		// set last move highlight
 		if (history.size() != 0) {
 			final BoardButton to = (BoardButton) activity.findViewById(history.top().to);
 			to.setLast(true);
 		}
 
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) activity.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	public void nudge_resign()
 	{
 		final GameDataDB db = new GameDataDB(activity);
 		final Bundle data = db.getOnlineGameData(settings.getString("gameid"));
 		db.close();
 
 		final int yturn = Integer.parseInt(data.getString("yourturn"));
 		if (yturn == Enums.YOUR_TURN) {
 			new ResignConfirm(activity, handle).show();
 			return;
 		}
 
 		switch (Integer.parseInt(data.getString("idle"))) {
 		default:
 		case Enums.NOTIDLE:
 			Toast.makeText(activity, "Game must be idle before nudging", Toast.LENGTH_LONG).show();
 			break;
 		case Enums.IDLE:
 			new NudgeConfirm(activity, handle).show();
 			break;
 		case Enums.NUDGED:
 			Toast.makeText(activity, "Game is already nudged", Toast.LENGTH_LONG).show();
 			break;
 		case Enums.CLOSE:
 			new IdleResignConfirm(activity, handle).show();
 			break;
 		}
 	}
 
 	public void rematch()
 	{
 		final String opp = settings.getString("username").equals(settings.getString("white"))?
 			settings.getString("black") : settings.getString("white");
 		new RematchConfirm(activity, handle, opp).show();
 	}
 
 	public void draw()
 	{
 		new DrawDialog(activity, handle).show();
 	}
 
 	private void ShowGameStats(final JSONObject json) throws JSONException
 	{
 		json.put("yourcolor", ycol);
 		json.put("white_name", settings.getString("white"));
 		json.put("black_name", settings.getString("black"));
 		json.put("eventtype", settings.getString("eventtype"));
 		json.put("status", settings.getString("status"));
 		json.put("gametype", Enums.GameType(Integer.parseInt(settings.getString("gametype"))));
 		json.put("gameid", settings.getString("gameid"));
 
 		new GameStatsDialog(activity, json).show();
 	}
 }
