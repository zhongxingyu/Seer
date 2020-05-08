 package com.quizz.places.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.R;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.util.Log;
 
 import com.quizz.core.application.BaseQuizzApplication;
 import com.quizz.core.db.DbHelper;
 import com.quizz.core.db.QuizzDAO;
 import com.quizz.core.models.Hint;
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 import com.quizz.core.models.Stat;
 
 public class PlacesDAO {
 
 	private Context mContext;
 
 	public PlacesDAO(Context context) {
 		this.mContext = context;
 	}
 
 	public List<Stat> getStats() {
 
 		String sqlQuery = "SELECT" + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_SECTIONS + " WHERE "
 				+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_UNLOCKED
 				+ " = " + Section.SECTION_UNLOCKED + ") AS sections_unlocked,"
				+ " COUNT(" + DbHelper.TABLE_SECTIONS + "."
				+ DbHelper.COLUMN_ID + ") AS sections_total," + " (SELECT"
				+ " COUNT(" + DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID
				+ ")" + " FROM " + DbHelper.TABLE_LEVELS + " " + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + ") AS levels_clear," + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID
 				+ ") AS levels_total," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + " AND " + DbHelper.TABLE_LEVELS
 				+ "." + DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_EASY
 				+ "\"" + ") AS levels_easy_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_EASY + "\"" + ") AS levels_easy_total,"
 				+ " (SELECT" + " COUNT(" + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_ID + ")" + " FROM " + DbHelper.TABLE_LEVELS
 				+ " WHERE " + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_STATUS + " = " + Level.STATUS_LEVEL_CLEAR
 				+ " AND " + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_MEDIUM
 				+ "\"" + ") AS levels_medium_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_MEDIUM + "\""
 				+ ") AS levels_medium_total," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + " AND " + DbHelper.TABLE_LEVELS
 				+ "." + DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_HARD
 				+ "\"" + ") AS levels_hard_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_HARD + "\"" + ") AS levels_hard_total,"
 				+ " (SELECT" + " COUNT(" + DbHelper.TABLE_HINTS + "."
 				+ DbHelper.COLUMN_ID + ")" + " FROM " + DbHelper.TABLE_HINTS
 				+ " WHERE " + DbHelper.TABLE_HINTS + "."
 				+ DbHelper.COLUMN_UNLOCKED + "=" + Hint.STATUS_HINT_REVEALED
 				+ ") AS revealed_hints, " + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_HINTS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_HINTS + ") AS total_hints"
 				+ " FROM " + DbHelper.TABLE_SECTIONS + " LEFT JOIN "
 				+ DbHelper.TABLE_LEVELS;
 
 		Log.d("BaseQuizzDAO", sqlQuery);
 
 		Cursor cursor = QuizzDAO.INSTANCE.getDbHelper().getReadableDatabase()
 				.rawQuery(sqlQuery, null);
 		Log.d("BaseQuizzDAO", DatabaseUtils.dumpCursorToString(cursor));
 		return this.cursorToStat(cursor);
 	}
 
 	public List<Stat> cursorToStat(Cursor cursor) {
 
 		ArrayList<Stat> stats = new ArrayList<Stat>();
 
 		cursor.moveToFirst();
 		stats.add(new Stat(mContext.getResources().getIdentifier("sections",
 				"drawable", "com.quizz.places"), "Niveaux dbloqus", cursor
 				.getInt(cursor.getColumnIndex("sections_unlocked")), cursor
 				.getInt(cursor.getColumnIndex("sections_total")), true));
 		stats.add(new Stat(mContext.getResources().getIdentifier("levels",
 				"drawable", "com.quizz.places"), "Images trouves", cursor
 				.getInt(cursor.getColumnIndex("levels_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_total")), true));
 		stats.add(new Stat(mContext.getResources().getIdentifier("easy",
 				"drawable", "com.quizz.places"), "Trouves en facile", cursor
 				.getInt(cursor.getColumnIndex("levels_easy_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_easy_total")), true));
 		stats.add(new Stat(mContext.getResources().getIdentifier("medium",
 				"drawable", "com.quizz.places"), "Trouves en moyen", cursor
 				.getInt(cursor.getColumnIndex("levels_medium_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_medium_total")), true));
 		stats.add(new Stat(mContext.getResources().getIdentifier("hard",
 				"drawable", "com.quizz.places"), "Trouves en difficile",
 				cursor.getInt(cursor.getColumnIndex("levels_hard_clear")),
 				cursor.getInt(cursor.getColumnIndex("levels_hard_total")), true));
 		stats.add(new Stat(mContext.getResources().getIdentifier("hint",
 				"drawable", "com.quizz.places"), "Indices utiliss", cursor
 				.getInt(cursor.getColumnIndex("revealed_hints")), cursor
 				.getInt(cursor.getColumnIndex("total_hints")), false));
 		cursor.close();
 
 		Log.d("test", String.valueOf(stats.size()));
 
 		return stats;
 	}
 }
