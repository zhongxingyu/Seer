 package com.geeksong.agricolascorer.mapper;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class Database extends SQLiteOpenHelper {
 	private static final String Name = "AgricolaScorer";
 	private static final int Version = 20;
 	
 	
     public static final String TABLE_RECENTPLAYERS = "RecentPlayers";
 	public static final String TABLE_GAMES = "Games";
 	public static final String TABLE_SCORES = "Scores";
     
     public static final String KEY_NAME = "name";
 	
 	public static final String KEY_DATE = "playedDate";
 	public static final String KEY_FARMERS = "farmers";
 	
 	public static final String KEY_ID = "id";
 	public static final String KEY_FINALSCORE = "finalScore";
 	
 	public static final String KEY_FIELDSCORE = "fieldScore";
 	public static final String KEY_PASTURESCORE = "pastureScore";
 	public static final String KEY_GRAINSCORE = "grainScore";
 	public static final String KEY_VEGETABLESCORE = "vegetableScore";
 	public static final String KEY_SHEEPSCORE = "sheepScore";
 	public static final String KEY_WILDBOARSCORE = "wildBoarScore";
 	public static final String KEY_CATTLESCORE = "cattleScore";
 	public static final String KEY_UNUSEDSPACESSCORE = "unusedSpacesScore";
 	public static final String KEY_FENCEDSTABLESSCORE = "fencedStablesScore";
 	public static final String KEY_ROOMSSCORE = "roomsScore";
 	public static final String KEY_FAMILYMEMBERSCORE = "familyMemberScore";
 	public static final String KEY_POINTSFORCARDS = "pointsForCards";
 	public static final String KEY_BONUSPOINTS = "bonusPoints";
 	public static final String KEY_BEGGINGCARDSSCORE = "beggingCardsScore";
 	public static final String KEY_HORSESCORE = "horseScore";
 	public static final String KEY_INBEDFAMILYCOUNT = "inBedFamilyCount";
 	public static final String KEY_TOTALFAMILYCOUNT = "totalFamilyCount";
 	
 	public static final String KEY_ROOMTYPE = "roomType";
 	public static final String KEY_ROOMCOUNT = "roomCount";
 	public static final String KEY_PLAYERID = "playerId";
 	public static final String KEY_GAMEID = "gameId";
 	
 	private static Database instance;
 	public static Database getInstance() {
 		return instance;
 	}
 	
 	public static void initialize(Context context) {
 		instance = new Database(context);
 	}
 	
 	private Database(Context context) {
 		super(context, Name, null, Version);
 	}
 	
     @Override
     public void onCreate(SQLiteDatabase db) {
         String CREATE_RECENTPLAYERS_TABLE = "CREATE TABLE " + TABLE_RECENTPLAYERS + "(" +
         		KEY_ID + " INTEGER PRIMARY KEY," +
         		KEY_NAME + " TEXT " +
 				")";
         db.execSQL(CREATE_RECENTPLAYERS_TABLE);
         
     	String CREATE_GAMES_TABLE = "CREATE TABLE " + TABLE_GAMES + "(" +
 	    		KEY_ID + " INTEGER PRIMARY KEY," +
 	    		KEY_DATE + " INTEGER " +
 	    		KEY_FARMERS + " INTEGER " +
 			")";
     	db.execSQL(CREATE_GAMES_TABLE);
         	
 	    String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES + "(" +
 		    KEY_ID + " INTEGER PRIMARY KEY," +
 		    KEY_FINALSCORE + " INTEGER, " +
 		    KEY_FIELDSCORE + " INTEGER, " +
 		    KEY_PASTURESCORE + " INTEGER, " +
 		    KEY_GRAINSCORE + " INTEGER, " +
 		    KEY_VEGETABLESCORE + " INTEGER, " +
 		    KEY_SHEEPSCORE + " INTEGER, " +
 		    KEY_WILDBOARSCORE + " INTEGER, " +
 		    KEY_CATTLESCORE + " INTEGER, " +
 		    KEY_UNUSEDSPACESSCORE + " INTEGER, " +
 		    KEY_FENCEDSTABLESSCORE + " INTEGER, " +
 		    KEY_ROOMSSCORE + " INTEGER, " +
 		    KEY_FAMILYMEMBERSCORE + " INTEGER, " +
 		    KEY_POINTSFORCARDS + " INTEGER, " +
 		    KEY_BONUSPOINTS + " INTEGER, " +
 		    KEY_BEGGINGCARDSSCORE + " INTEGER, " +
 		    KEY_ROOMTYPE + " INTEGER, " +
 		    KEY_ROOMCOUNT + " INTEGER, " +
 		    KEY_PLAYERID + " INTEGER, " +
 		    KEY_GAMEID + " INTEGER, " + 
		    KEY_HORSESCORE + " INTEGER, " +
		    KEY_INBEDFAMILYCOUNT + " INTEGER, " +
		    KEY_TOTALFAMILYCOUNT + " INTEGER, " + 
 			"FOREIGN KEY(" + KEY_PLAYERID + ") REFERENCES " + TABLE_RECENTPLAYERS + "(" + KEY_ID + "), " +
 			"FOREIGN KEY(" + KEY_GAMEID + ") REFERENCES " + TABLE_GAMES + "(" + KEY_ID + ")" +
 			")";
 	    db.execSQL(CREATE_SCORES_TABLE);
     }
  
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     	// KEY_GAMECOUNT on TABLE_RECENTPLAYERS isn't used anymore and could be dropped if SQLite supported it nicely.
     	db.execSQL("ALTER TABLE " + TABLE_GAMES + " ADD COLUMN " + KEY_FARMERS + " INTEGER");
     	db.execSQL("ALTER TABLE " + TABLE_SCORES + " ADD COLUMN " + KEY_HORSESCORE + " INTEGER");
     	db.execSQL("ALTER TABLE " + TABLE_SCORES + " ADD COLUMN " + KEY_INBEDFAMILYCOUNT + " INTEGER");
     	db.execSQL("ALTER TABLE " + TABLE_SCORES + " ADD COLUMN " + KEY_TOTALFAMILYCOUNT + " INTEGER");
     }
 }
