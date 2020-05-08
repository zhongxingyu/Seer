 package org.homelinux.murray.scorekeep;
 
 import java.util.ArrayList;
 import org.homelinux.murray.scorekeep.provider.Player;
 import org.homelinux.murray.scorekeep.provider.Score;
 
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 
 public final class PlayerData implements View.OnClickListener, DialogInterface.OnClickListener {
 	private static final String DEBUG_TAG = "ScoreKeep:PlayerData";
 	final long id;
 	final String name;
 	final long color;
 	final GameData game;
 	private long total;
 	private String scoreContext = null;
 	private final ArrayList<ScoreData> scores = new ArrayList<ScoreData>();
 	private final Context appContext;
 	
 	/**
 	 * Load player data from the DB with scores from a game
 	 * 
 	 * @param context The context to run a query
 	 * @param playerId Unique player identifier
 	 * @param gameId Unique id for game to get scores from
 	 */
 	public PlayerData(Context context, long playerId, GameData game) {
 		id = playerId;
 		this.game = game;
 		appContext = context.getApplicationContext();
 
 		Cursor pc = appContext.getContentResolver().query(ContentUris.withAppendedId(Player.CONTENT_ID_URI_BASE, playerId), null, null, null, null);
 		pc.moveToFirst();
 		name = pc.getString(pc.getColumnIndex(Player.COLUMN_NAME_NAME));
 		color = pc.getLong(pc.getColumnIndex(Player.COLUMN_NAME_COLOR));
 
 		String selection = Score.COLUMN_NAME_GAME_ID + "=" + game.id + " AND " + Score.COLUMN_NAME_PLAYER_ID + "=" + playerId;
 		Cursor sc = appContext.getContentResolver().query(Score.CONTENT_URI, null, selection, null, null);
 		int scoreColumn = sc.getColumnIndex(Score.COLUMN_NAME_SCORE);
 		int idColumn = sc.getColumnIndex(Score._ID);
 		int contextColumn = sc.getColumnIndex(Score.COLUMN_NAME_CONTEXT);
 		int createdColumn = sc.getColumnIndex(Score.COLUMN_NAME_CREATE_DATE);
 		total = game.starting_score;
 		long id, created;
 		Long score;
 		String tmpContext = null;
 		while(sc.moveToNext()) {
 			id = sc.getLong(idColumn);
 			if(sc.isNull(scoreColumn)) {
 				score = null;
 			} else {
 				score = new Long(sc.getLong(scoreColumn));
 				total += score;
 			}
 			
 			if(!sc.isNull(contextColumn)) {
 				tmpContext = sc.getString(contextColumn);
 				scoreContext = tmpContext;
 			}
 
 			created = sc.getLong(createdColumn);
 			scores.add(new ScoreData(id, score, tmpContext, created));
 		}
 		
 
 	}
 
 	public long addScore(long score) {
 		return addScoreAndContext(score, null);
 	}
 	
 	public void addContext(String context) {
 		addScoreAndContext(null,context);
 	}
 	
 	public long addScoreAndContext(Long score, String context) {
 		// insert to the DB
 		ContentValues values = new ContentValues();
 		values.put(Score.COLUMN_NAME_GAME_ID, game.id);
 		values.put(Score.COLUMN_NAME_PLAYER_ID, id);
 		if(score==null&&(context==null||context.isEmpty())) {
 			Log.d(DEBUG_TAG, "Score and Context are null, WTH?");
 			return total;
 		}
 		if(score != null) {
 			values.put(Score.COLUMN_NAME_SCORE, score.longValue());
 			total += score;
 		}
 		if(context != null&&!context.isEmpty()) {
 			values.put(Score.COLUMN_NAME_CONTEXT, context);
 			scoreContext = context;
 		}
 		Long now = Long.valueOf(System.currentTimeMillis());
 		values.put(Score.COLUMN_NAME_CREATE_DATE, now);
 		
 		Uri uri = appContext.getContentResolver().insert(Score.CONTENT_URI, values);
 		// add to history
 		scores.add(new ScoreData(ContentUris.parseId(uri), score, context, now));
 		// add to total
 
 		appContext.getContentResolver().notifyChange(uri, null);
		game.notifyDataSetChanged();
		
 		return total;
 	}
 	
 	public long getTotal() {
 		return total;
 	}
 	
 	public String getCurrentContext() {
 		return scoreContext;
 	}
 
 	public void onClick(View v) {
 		int vid = v.getId();
 		switch(vid) {
 		case R.id.badge_add:
 			(new AddScoreDialog(v.getContext(), this)).show();
 			return;
 		case R.id.badge_history:
 			Context context = v.getContext();
 			Log.d(DEBUG_TAG, "Score History button clicked.");
 			final AlertDialog.Builder alert = new AlertDialog.Builder(context);
 			
 			HistoryAdapter ha = new HistoryAdapter(context, scores);
 			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			alert.setCustomTitle(inflater.inflate(R.layout.score_history_item, null));
 			alert.setAdapter(ha, this);
 			alert.show();
 			
 		}
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		// TODO Auto-generated method stub, from history list
 		
 	}
 
 	public void resetScore() {
 		ContentResolver cr = appContext.getContentResolver();
 		String where = Score.COLUMN_NAME_GAME_ID+"="+game.id+" AND "+Score.COLUMN_NAME_PLAYER_ID+"="+id;
 		total = game.starting_score;
 		scoreContext = "";
 		scores.clear();
 		cr.delete(Score.CONTENT_URI, where, null);
		// must notify after complete
 	}
 }
