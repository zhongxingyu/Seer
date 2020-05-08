 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Helper class to create, load, update etc the database
  * 
  * @author John Lawson
  * 
  */
 public class SQLite extends SQLiteOpenHelper {
 
 	private static final String TAG = "SQLiteHelper";
 	private static final boolean DEBUG = false;
 
 	/** Table name */
 	public static final String TABLE_INFO = "info";
 	/** Auto-incrementing ID column */
 	public static final String COLUMN_ID = "_id";
 	/** Day of the trial column */
 	public static final String COLUMN_DAY = "day";
 	/** Time column */
 	public static final String COLUMN_TIME = "time";
 	/** Comments column */
 	public static final String COLUMN_COMMENT = "comment";
 	/** Question column prefix */
 	public static final String COLUMN_QUESTION = "question";
 
 	/**
 	 * The number of questions, hence the number of columns called
 	 * {@code question<i>}
 	 */
 	private int mNumQuestion;
 
 	/** Database file name */
 	public static final String DATABASE_NAME = "info.db";
 
 	/** Construct database with custom name and version number */
 	public SQLite(Context context) {
 		super(context, DATABASE_NAME, null, context.getSharedPreferences(Keys.CONFIG_NAME,
 				Context.MODE_PRIVATE).getInt(Keys.CONFIG_DB_VERSION, 1));
 	}
 
 	/**
 	 * Set the number of questions asked in data input. Must be called before
 	 * the database is first created.
 	 * 
 	 * @param num
 	 *            The number of questions asked.
 	 */
 	public void setQuestionNumber(int num) {
 		mNumQuestion = num;
 	}
 
 	public int getNumberQuestions() {
 		return mNumQuestion;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(makeCreate());
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (DEBUG)
 			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
 
 		// Make array of all column headers
 		int num = getNumberQuestions();
		String[] columns = new String[num + 3];
 		columns[0] = SQLite.COLUMN_ID;
 		columns[1] = SQLite.COLUMN_DAY;
 		columns[2] = SQLite.COLUMN_TIME;
 
 		for (int i = 0; i < num; i++) {
 			columns[i + 3] = SQLite.COLUMN_QUESTION + i;
 		}
 		columns[num + 3] = SQLite.COLUMN_COMMENT;
 
 		// Get all data from database
 		Cursor cursor;
 		synchronized (DataSource.sDataLock) {
 			cursor = db.query(TABLE_INFO, columns, null, null, null, null, null);
 		}
 
 		// Store all data in arrays. Could be lots of data
 		int size = cursor.getCount();
 		int[] days = new int[size];
 		String[] times = new String[size];
 		int[][] questions = new int[size][num];
 		String[] comments = new String[size];
 
 		cursor.moveToFirst();
 
 		for (int i = 0; i < size; i++) {
 			days[i] = cursor.getInt(1);
 			times[i] = cursor.getString(2);
 			for (int j = 0; j < num; j++) {
 				questions[i][j] = cursor.getInt(j + 3);
 			}
 			comments[i] = cursor.getString(num + 3);
 
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 		// Delete current table
 		synchronized (DataSource.sDataLock) {
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
 		}
 
 		// Make new table
 		onCreate(db);
 
 		// Fill new table with old data
 		for (int i = 0; i < size; i++) {
 			ContentValues values = new ContentValues();
 			values.put(COLUMN_DAY, days[i]);
 			values.put(COLUMN_TIME, times[i]);
 			for (int j = 0; j < questions[i].length; j++) {
 				values.put(SQLite.COLUMN_QUESTION + j, questions[i][j]);
 			}
 			values.put(COLUMN_COMMENT, comments[i]);
 
 			synchronized (DataSource.sDataLock) {
 				db.insert(SQLite.TABLE_INFO, null, values);
 			}
 		}
 	}
 
 	/** Create the database creation SQL command */
 	private String makeCreate() {
 		StringBuilder sb = new StringBuilder("create table ");
 		sb.append(TABLE_INFO).append("(").append(COLUMN_ID)
 				.append(" integer primary key autoincrement, ").append(COLUMN_DAY)
 				.append(" integer").append(COLUMN_TIME).append(" text");
 
 		if (mNumQuestion == 0) Log.e(TAG, "No question number set");
 		for (int i = 0; i < mNumQuestion; i++) {
 			sb.append(", ").append("question").append(i).append(" integer");
 		}
 
 		sb.append(", ").append(COLUMN_COMMENT).append(" text");
 		sb.append(");");
 		return sb.toString();
 	}
 }
