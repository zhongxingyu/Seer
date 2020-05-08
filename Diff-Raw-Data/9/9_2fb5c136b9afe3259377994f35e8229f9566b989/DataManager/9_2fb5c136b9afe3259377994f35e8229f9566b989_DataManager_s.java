 package com.multimedia.slidepuzzel.data;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DataManager extends SQLiteOpenHelper{
 
 	private static final String DATABASE_NAME = "slidepuzzel_data";
 	private static final String SETTINGS_TABLE = "settings";
 	private static final String HIGHSCORE_TABLE = "highscores";
 	
 	public DataManager(Context context) {
 		super(context, DATABASE_NAME, null, 1);
 		
 		getWritableDatabase().execSQL("DROP TABLE " + SETTINGS_TABLE);
 		getWritableDatabase().execSQL("DROP TABLE " + HIGHSCORE_TABLE);
 		onCreate(getWritableDatabase());
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db){
 		// Create settings table
 		db.execSQL("CREATE TABLE " + SETTINGS_TABLE + " ("
 				+ "id INTEGER PRIMARY KEY, "
 				+ "difficulty TEXT, "
 				+ "size INTEGER, "
 				+ "mode INTEGER"
 			+ ")");
 		
 		// Create highscores table
 		db.execSQL("CREATE TABLE " + HIGHSCORE_TABLE + " ("
 				+ "id INTEGER PRIMARY KEY, "
 				+ "difficulty TEXT, "
 				+ "name TEXT, "
 				+ "size INTEGER, "
 				+ "time INTEGER, "
 				+ "mode INTEGER"
 			+ ")");
 		
 		// Insert default settings
 		ContentValues val = new ContentValues();
 		val.put("difficulty", "EASY");
 		val.put("size", 3);
		val.put("mode", 1);
 		db.insert(SETTINGS_TABLE, null, val);
 	}
 	
 	public void updateSettings(Settings s){
 		ContentValues val = new ContentValues();
 		val.put("difficulty", s.getDifficulty());
 		val.put("size", s.getSize());
 		val.put("mode", s.getMode());
 		getWritableDatabase().update(SETTINGS_TABLE, val, null, null);
 	}
 	
 	public Settings getSettings(){
 		Settings s = new Settings();
 		Cursor c = getReadableDatabase().query(SETTINGS_TABLE, new String[] {"difficulty", "size", "mode"}, null,
 				null, null, null, null, "1");
 		if(c == null)return null;
 		c.moveToFirst();
 		s.setDifficulty(c.getString(0));
 		s.setSize(c.getInt(1));
 		s.setMode(c.getInt(2));
 		c.close();
 		return s;
 	}
 	
 	public void insertHighscore(HighscoreEntry h){
 		ContentValues val = new ContentValues();
 		val.put("difficulty", h.getSettings().getDifficulty());
 		val.put("time", h.getTime());
 		val.put("name", h.getName());
 		val.put("size", h.getSettings().getSize());
 		val.put("mode", h.getSettings().getMode());
 		
 		getWritableDatabase().insert(HIGHSCORE_TABLE, null, val);
 	}
 	
 	public HighscoreEntry[] getHighscore(String difficulty, int max, String qsize){
 		HighscoreEntry[] entries = new HighscoreEntry[max];
 		
 		// Select highscore 
 		Cursor c = getReadableDatabase().query(HIGHSCORE_TABLE, new String[] {"difficulty", "name", "time", "size", "mode"}, "difficulty = ? AND " + qsize,
 				new String[] {difficulty}, null, null, "time ASC", "" + max);
 		
 		// Parse highscores
 		int i = 0;
 		while(c.moveToNext()){
 			Settings s = new Settings();
 			s.setDifficulty(c.getString(0));
 			s.setSize(c.getInt(3));
 			s.setMode(c.getInt(4));
 			HighscoreEntry h = new HighscoreEntry();
 			h.setName(c.getString(1));
 			h.setTime(c.getInt(2));
 			h.setSettings(s);
 			entries[i++] = h;
 		}
 		c.close();
 		
 		return entries;
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldv, int newv){
 	}
 }
