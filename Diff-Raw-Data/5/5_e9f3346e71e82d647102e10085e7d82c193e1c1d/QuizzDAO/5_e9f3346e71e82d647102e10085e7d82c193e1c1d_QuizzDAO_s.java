 package com.quizz.core.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 
 public enum QuizzDAO {
 	INSTANCE;
 
 	private DbHelper mDbHelper;
 
 	private static final String _COLUMN_LEVEL_ID = "level_id";
 	private static final String _COLUMN_SECTION_ID = "section_id";
 	
 	private static final String _COLUMN_LEVEL_STATUS = "level_status";
 	private static final String _COLUMN_SECTION_STATUS = "section_status";
 	
 	private static final String _COLUMN_LEVEL_REF = "level_ref";
 	private static final String _COLUMN_SECTION_REF = "section_ref";
 
 	private static final String _ATTACH_USERDATA_DB = "userdata_db";
 
 	public void setDbHelper(DbHelper dbHelper) {
 		mDbHelper = dbHelper;
 	}
 
 	public synchronized DbHelper getDbHelper() {
 		return mDbHelper;
 	}
 
 	public List<Section> getSections() {
 		String attachDBQuery = "ATTACH '" + mDbHelper.getUserdataDBFullPath()
 									+ "' AS "+ _ATTACH_USERDATA_DB +";";
 
 		String subQuerySectionStatus = 
 				"SELECT " + DbHelper.COLUMN_STATUS
 				+ " FROM " + _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA
 				+ " WHERE "
 					+ DbHelper.TABLE_SECTIONS  + "." + DbHelper.COLUMN_REF
 				+ " = "
 					+ _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA + "." + DbHelper.COLUMN_REF
 				+ " AND "
 					+ _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA + "." + DbHelper.COLUMN_REF_FROM_TABLE
 				+ " = "
 					+ "\"" + DbHelper.TABLE_SECTIONS + "\""
 				+ " LIMIT 1";
 
 		String subQueryLevelStatus = 
 				"SELECT " + DbHelper.COLUMN_STATUS
 				+ " FROM " + _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA
 				+ " WHERE "
 					+ DbHelper.TABLE_LEVELS  + "." + DbHelper.COLUMN_REF
 				+ " = "
 					+ _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA + "." + DbHelper.COLUMN_REF
 				+ " AND "
 					+ _ATTACH_USERDATA_DB + "." + DbHelper.TABLE_USERDATA + "." + DbHelper.COLUMN_REF_FROM_TABLE
 				+ " = "
 					+ "\"" + DbHelper.TABLE_LEVELS + "\""
 				+ " LIMIT 1";
 		
 		String sqlQuery = 
 		" SELECT "
 			+ "IFNULL((" + subQuerySectionStatus + "), " + Section.SECTION_LOCKED + ") AS " + _COLUMN_SECTION_STATUS + ", "
 			+ "IFNULL((" + subQueryLevelStatus + "), " + Level.STATUS_LEVEL_UNCLEAR + ") AS " + _COLUMN_LEVEL_STATUS + ", "
 			+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_ID + " AS " + _COLUMN_SECTION_ID + ", "
 			+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_REF + " AS " + _COLUMN_SECTION_REF + ", "
 			+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_NUMBER + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + " AS " + _COLUMN_LEVEL_ID + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_REF + " AS " + _COLUMN_LEVEL_REF + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_LEVEL + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_IMAGE + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_PARTIAL_RESPONSE + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_INDICATION + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_RESPONSE + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_LINK + ", "
 			+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_FK_SECTION
 		+ " FROM " + DbHelper.TABLE_SECTIONS
 		+ " LEFT JOIN " + DbHelper.TABLE_LEVELS
 			+ " ON "
 				+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_ID
 			+ " = "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_FK_SECTION;
 
 		Log.d("SQL QUERY : ", sqlQuery);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		db.execSQL(attachDBQuery);
 		Cursor cursor = db.rawQuery(sqlQuery, null);
 		Log.d("CURSOR DUMP", DatabaseUtils.dumpCursorToString(cursor));
 		Log.d("cursor", String.valueOf(cursor.getCount()));
 		return cursorToSections(cursor);
 	}
 
 	private Level cursorToLevel(Cursor cursor) {
 		Level level = new Level();
 		level.id = cursor.getInt(cursor.getColumnIndex(_COLUMN_LEVEL_ID));
 		level.ref = cursor.getString(cursor.getColumnIndex(_COLUMN_LEVEL_REF));
 		level.imageName = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_IMAGE));
 		level.indication = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_INDICATION));
 		level.partialResponse = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PARTIAL_RESPONSE));
 		level.response = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_RESPONSE));
 		level.moreInfosLink = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_LINK));
 		level.status = cursor.getInt(cursor.getColumnIndex(_COLUMN_LEVEL_STATUS));
 		return level;
 	}
 
 	private Section cursorToSection(Cursor cursor) {
 		Section section = new Section();
 		section.id = cursor.getInt(cursor.getColumnIndex(_COLUMN_SECTION_ID));
 		section.ref = cursor.getString(cursor.getColumnIndex(_COLUMN_SECTION_REF));
 		section.number = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_NUMBER));
 		section.status = cursor.getInt(cursor.getColumnIndex(_COLUMN_SECTION_STATUS));
 		return section;
 	}
 	
 	private List<Section> cursorToSections(Cursor cursor) {
 		ArrayList<Section> sections = new ArrayList<Section>();
 		int lastId = 0;
 		Section section = null;
 		List<Level> levels = new ArrayList<Level>();
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			int column = cursor.getColumnIndex(_COLUMN_SECTION_ID);
 			if (cursor.getInt(column) != lastId && lastId != 0) {
 				if (section != null) {
 					section.levels.addAll(levels);
 					sections.add(section);
 				}
 				levels.clear();
 			}
 			section = QuizzDAO.INSTANCE.cursorToSection(cursor);
 			Level tmpLevel = QuizzDAO.INSTANCE.cursorToLevel(cursor);
 			tmpLevel.sectionId = section.id;
 			levels.add(tmpLevel);
 			lastId = cursor.getInt(cursor.getColumnIndex(_COLUMN_SECTION_ID));
 			if (cursor.isLast()) {
 				for (Level level : levels) {
 					section.levels.add(level);
 				}
 				sections.add(section);
 			}
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return sections;
 	}
 
 	public void updateSection(Section section) {
 		ContentValues progressValues = new ContentValues();
 		progressValues.put(DbHelper.COLUMN_STATUS, section.status);
 		
 		String whereClause = DbHelper.COLUMN_REF + " = \"" + section.ref + "\""
				+ "AND" + DbHelper.COLUMN_REF_FROM_TABLE + "=" + DbHelper.TABLE_SECTIONS;
 		
 		mDbHelper.getWritableUserdataDatabase().update(
 				DbHelper.TABLE_USERDATA, progressValues, 
 				whereClause,
 				null);
 	}
 	
 	public void updateLevel(Level level) {
 		ContentValues cv = new ContentValues();
 		cv.put(DbHelper.COLUMN_STATUS, level.status);
 		
 		String whereClause = DbHelper.COLUMN_REF + " = \"" + level.ref + "\""
				+ "AND" + DbHelper.COLUMN_REF_FROM_TABLE + "=" + DbHelper.TABLE_LEVELS;
 		
 		mDbHelper.getWritableDatabase().update(
 				DbHelper.TABLE_USERDATA, cv, 
 				whereClause, null);
 	}
 	
 }
