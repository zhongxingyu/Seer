 package org.homelinux.murray.scorekeep;
 
 import java.util.ArrayList;
 import org.homelinux.murray.scorekeep.provider.Player;
 import org.homelinux.murray.scorekeep.provider.Score;
 
 import android.app.AlertDialog;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public final class PlayerData implements OnClickListener {
 	private static final String DEBUG_TAG = "ScoreKeep:PlayerData";
 	final long id;
 	final String name;
 	final long color;
 	final GameData game;
 	private long total;
 	private String currentContext = null;
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
 		total = 0; //TODO starting score
 		long id, created;
 		Long score;
 		String scoreContext = null;
 		while(sc.moveToNext()) {
 			id = sc.getLong(idColumn);
 			if(sc.isNull(scoreColumn)) {
 				score = null;
 			} else {
 				score = new Long(sc.getLong(scoreColumn));
 			}
 			
 			if(!sc.isNull(contextColumn)) {
 				scoreContext = sc.getString(contextColumn);
 				currentContext = scoreContext;
 			}
 
 			created = sc.getLong(createdColumn);
 			scores.add(new ScoreData(id, score, scoreContext, created));
 			total += score;
 		}
 		
 
 	}
 
 	public long addScore(long score) {
 		return addScoreAndContext(Long.toString(score), null);
 	}
 	
 	public void addContext(String context) {
 		addScoreAndContext(null,context);
 	}
 	
 	public long addScoreAndContext(String score, String context) {
 		// insert to the DB
 		ContentValues values = new ContentValues();
 		values.put(Score.COLUMN_NAME_GAME_ID, game.id);
 		values.put(Score.COLUMN_NAME_PLAYER_ID, id);
 		Long scoreObject = null;
 		if(score==null&&context==null) {
 			Log.d(DEBUG_TAG, "Score and Context are null, WTH?");
 		}
		if(score != null) {
 			long ls = evaluate(score);
 			scoreObject = new Long(ls);
 			values.put(Score.COLUMN_NAME_SCORE, ls);
 		}
		if(context != null) {
 			values.put(Score.COLUMN_NAME_CONTEXT, context);
 			currentContext = context;
 		}
 		Long now = Long.valueOf(System.currentTimeMillis());
 		values.put(Score.COLUMN_NAME_CREATE_DATE, now);
 		
 		Uri uri = appContext.getContentResolver().insert(Score.CONTENT_URI, values);
 		// add to history
 		scores.add(new ScoreData(ContentUris.parseId(uri), scoreObject, context, now));
 		// add to total
 		total += total;
 		game.notifyDataSetChanged();
 		return total;
 	}
 	
 	/**
 	 * Evaluates the string as a math formula
 	 * 
 	 * @param math Math formula to evaluate
 	 * @return returns result
 	 */
 	private static long evaluate(String math) {
 		MathEval me = new MathEval();
 		double result = me.evaluate(math);
 		return Math.round(result);
 	}
 	
 	public long getTotal() {
 		return total;
 	}
 	
 	public String getCurrentContext() {
 		return currentContext;
 	}
 
 	public void onClick(View v) {
 		int vid = v.getId();
 		switch(vid) {
 		case R.id.badge_add:
 			final AlertDialog.Builder addScoreDialog = new AlertDialog.Builder(v.getContext());
 			addScoreDialog.setTitle("Enter Score");
 			LinearLayout ll = new LinearLayout(v.getContext());
 			ll.setOrientation(LinearLayout.VERTICAL);
 			final EditText scoreInput = new EditText(v.getContext());
 			scoreInput.setHint("Score");
 			ll.addView(scoreInput);
 			final EditText contextText = new EditText(v.getContext());
 			contextText.setHint("Context");
 			ll.addView(contextText);
 			addScoreDialog.setView(ll);
 			addScoreDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					String value = scoreInput.getText().toString().trim();
 					addScoreAndContext(value, contextText.getText().toString());
 				}
 			});
 			addScoreDialog.setNegativeButton(R.string.cancel,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					dialog.cancel();
 				}
 			});
 			addScoreDialog.show();
 			return;
 		case R.id.badge_history:
 			System.out.println("Score History button clicked.");
 		}
 	}
 }
