 package com.LoLCompanionApp;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	// The Android's default system path of your application database.
 	private static String DB_PATH = "/data/data/com.LoLCompanionApp/databases/";
 	// private static String DB_NAME = "LoLCompanionAppDatabase.db";
 	private static String DB_NAME = "gameStats_en_US.sqlite";
 	private SQLiteDatabase myDataBase;
 	private final Context myContext;
 
 	/**
 	 * Constructor Takes and keeps a reference of the passed context in order to
 	 * access to the application assets and resources.
 	 * 
 	 * @param context
 	 */
 	public DatabaseHelper(Context context) {
 
 		super(context, DB_NAME, null, 1);
 		this.myContext = context;
 
 		try {
 
 			createDataBase();
 
 		} catch (IOException ioe) {
 
 			throw new Error("Unable to create database");
 
 		}
 
 		try {
 
 			openDataBase();
 
 		} catch (SQLException sqle) {
 
 			throw sqle;
 
 		}
 
 	}
 
 	/**
 	 * Creates a empty database on the system and rewrites it with your own
 	 * database.
 	 * */
 	public void createDataBase() throws IOException {
 
 		boolean dbExist = checkDataBase();
 
 		if (dbExist) {
 			// do nothing - database already exist
 		} else {
 
 			// By calling this method and empty database will be created into
 			// the default system path
 			// of your application so we are gonna be able to overwrite that
 			// database with our database.
 			this.getReadableDatabase();
 
 			try {
 
 				copyDataBase();
 
 			} catch (IOException e) {
 
 				throw new Error("Error copying database");
 
 			}
 		}
 
 	}
 
 	/**
 	 * Check if the database already exist to avoid re-copying the file each
 	 * time you open the application.
 	 * 
 	 * @return true if it exists, false if it doesn't
 	 */
 	private boolean checkDataBase() {
 
 		SQLiteDatabase checkDB = null;
 
 		try {
 			String myPath = DB_PATH + DB_NAME;
 			checkDB = SQLiteDatabase.openDatabase(myPath, null,
 					SQLiteDatabase.OPEN_READONLY);
 
 		} catch (SQLiteException e) {
 
 			// database does't exist yet.
 
 		}
 
 		if (checkDB != null) {
 
 			checkDB.close();
 
 		}
 
 		return checkDB != null ? true : false;
 	}
 
 	/**
 	 * Copies your database from your local assets-folder to the just created
 	 * empty database in the system folder, from where it can be accessed and
 	 * handled. This is done by transfering bytestream.
 	 * */
 	private void copyDataBase() throws IOException {
 
 		// Open your local db as the input stream
 		InputStream myInput = myContext.getAssets().open(DB_NAME);
 
 		// Path to the just created empty db
 		String outFileName = DB_PATH + DB_NAME;
 
 		// Open the empty db as the output stream
 		OutputStream myOutput = new FileOutputStream(outFileName);
 
 		// transfer bytes from the inputfile to the outputfile
 		byte[] buffer = new byte[1024];
 		int length;
 		while ((length = myInput.read(buffer)) > 0) {
 			myOutput.write(buffer, 0, length);
 		}
 
 		// Close the streams
 		myOutput.flush();
 		myOutput.close();
 		myInput.close();
 
 	}
 
 	public void openDataBase() throws SQLException {
 
 		// Open the database
 		String myPath = DB_PATH + DB_NAME;
 		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
 				SQLiteDatabase.OPEN_READONLY);
 
 	}
 
 	@Override
 	public synchronized void close() {
 
 		if (myDataBase != null)
 			myDataBase.close();
 
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 	}
 
 	public String[] getAllChampions() throws SQLiteException {
 		Cursor cur;
 		String[] result = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		cur = database.rawQuery(
				"SELECT displayName FROM champions ORDER BY displayName ASC", null);
 
 		// initialize variable
 		result = new String[cur.getCount()];
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			for (int i = 0; i < result.length; i += 1) {
 				result[i] = cur.getString(0);
 				cur.moveToNext();
 			}
 		}
 		database.close();
 
 		return result;
 	}
 
 	public String[] getAllChampionTitles() throws SQLiteException {
 		Cursor cur;
 		String[] result = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		cur = database.rawQuery(
 				"SELECT title FROM champions ORDER BY displayName ASC", null);
 
 		// initialize variable
 		result = new String[cur.getCount()];
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			for (int i = 0; i < result.length; i += 1) {
 				result[i] = cur.getString(0);
 				cur.moveToNext();
 			}
 		}
 		database.close();
 
 		return result;
 	}
 
 	public String getChampionTitle(String champ) throws SQLiteException {
 		String string = null;
 		int id = getChampionId(champ);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery(
 				"SELECT title FROM champions WHERE id=\'" + id + "\'", null);
 		if (cur.moveToFirst()) {
 			string = cur.getString(0);
 		}
 
 		database.close();
 
 		return string;
 	}
 
 	public int getChampionId(String champ) throws SQLiteException {
 		int result = 0;
 
 		champ = removeSpecialChars(champ);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery("SELECT id FROM champions WHERE name=\'"
 				+ champ + "\'", null);
 		if (cur.moveToFirst()) {
 			result = cur.getInt(0);
 		}
 
 		database.close();
 
 		return result;
 	}
 
 	public String getChampionName(int id) throws SQLiteException {
 		String string = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery("SELECT name FROM champions WHERE id=\'"
 				+ id + "\'", null);
 		if (cur.moveToFirst()) {
 			string = cur.getString(0);
 		}
 
 		database.close();
 
 		return string;
 	}
 
 	public String removeSpecialChars(String name) {
 		return name.replace(".", "").replace(" ", "").replace("/", "")
 				.replace("'", "").replace(";", "").replace(":", "")
 				.replace("-", "").replace("!", "");
 	}
 
 	public String[][] getAllSkillsByChampName(String champ)
 			throws SQLiteException {
 		String[][] result = null;
 		int id = getChampionId(champ);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery(
 				"SELECT * FROM championAbilities WHERE championId=\'" + id
 						+ "\'", null);
 
 		// go to first row
 		if (cur.moveToFirst()) {
 			result = new String[cur.getCount()][6];
 			for (int i = 0; i < cur.getCount(); i += 1) {
 				// get the two values
 				result[i][0] = "" + cur.getString(cur.getColumnIndex("name"));
				result[i][1] = ""
						+ cur.getString(cur.getColumnIndex("description"));
 				result[i][2] = "" + cur.getString(cur.getColumnIndex("hotkey"));
 				result[i][3] = "" + cur.getString(cur.getColumnIndex("cost"));
 				result[i][4] = "" + cur.getString(cur.getColumnIndex("range"));
				result[i][5] = ""
						+ cur.getString(cur.getColumnIndex("cooldown"));
 				// move to next row
 				cur.moveToNext();
 			}
 		}
 
 		database.close();
 
 		return result;
 	}
 
 	public int getNumSkinsByChampName(String champ) throws SQLiteException {
 		int result = 0;
 		int id = getChampionId(champ);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		Cursor cur = database.rawQuery(
 				"SELECT COUNT(*) FROM championSkins WHERE championId=\'" + id
 						+ "\'", null);
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			// initialize variable
 			result = cur.getInt(0);
 		}
 		database.close();
 
 		return result;
 	}
 
 	public String[] getChampionCounters(String champion) {
 		String[] string = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery(
 				"SELECT counters FROM champions WHERE name=\'"
 						+ champion.replace("'", "''") + "\'", null);
 		if (cur.moveToFirst()) {
 			string = cur.getString(0).split(",");
 		}
 
 		database.close();
 
 		return string;
 	}
 
 	public String getSkinName(String champ, int rank) {
 		String string = null;
 
 		champ = removeSpecialChars(champ).replace(" ", "");
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery(
 				"SELECT displayName FROM championSkins WHERE portraitPath=\'"
 						+ champ + "_" + rank + ".jpg\'", null);
 		if (cur.moveToFirst()) {
 			string = cur.getString(0);
 		}
 
 		database.close();
 
 		return string;
 	}
 
 	public String[] getChampionCounteredBy(String champion) {
 		String[] string = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query and get result
 		Cursor cur = database.rawQuery(
 				"SELECT countered FROM champions WHERE name=\'"
 						+ champion.replace("'", "''") + "\'", null);
 		if (cur.moveToFirst() && cur.getString(0) != null) {
 			string = cur.getString(0).split(",");
 		}
 		;
 
 		database.close();
 
 		return string;
 	}
 
 	public String getChampionAttributes(String champion) throws SQLiteException {
 		Cursor cur;
 		String result = "";
 		int id = getChampionId(champion);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		cur = database.rawQuery("SELECT tags FROM champions WHERE id=\'" + id
 				+ "\'", null);
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			result = cur.getString(0).toUpperCase();
 		}
 		database.close();
 
 		return result;
 	}
 
 	public String getChampionLore(String champion) {
 		String result = null;
 		int id = getChampionId(champion);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		Cursor cur = database.rawQuery(
 				"SELECT description FROM champions WHERE id=\'" + id + "\'",
 				null);
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			// initialize variable
 			result = cur.getString(cur.getColumnIndex("description"));
 		}
 		database.close();
 
 		return result;
 	}
 
 	public String[][] getChampionStats(String champion) {
 		String result[][] = new String[9][2];
 		int id = getChampionId(champion);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// run the query
 		Cursor cur = database.rawQuery("SELECT * FROM champions WHERE id=\'"
 				+ id + "\'", null);
 
 		// go through data and retrieve the name of drinks
 		if (cur.moveToFirst()) {
 			// initialize variable
 			result[0][0] = cur.getString(cur.getColumnIndex("range"));
 			result[1][0] = cur.getString(cur.getColumnIndex("moveSpeed"));
 			result[2][0] = cur.getString(cur.getColumnIndex("armorBase"));
 			result[3][0] = cur.getString(cur.getColumnIndex("manaBase"));
 			result[4][0] = cur.getString(cur.getColumnIndex("manaRegenBase"));
 			result[5][0] = cur.getString(cur.getColumnIndex("healthRegenBase"));
 			result[6][0] = cur.getString(cur.getColumnIndex("magicResistBase"));
 			result[7][0] = cur.getString(cur.getColumnIndex("healthBase"));
 			result[8][0] = cur.getString(cur.getColumnIndex("attackBase"));
 			result[2][1] = cur.getString(cur.getColumnIndex("armorBase"));
 			result[3][1] = cur.getString(cur.getColumnIndex("manaLevel"));
 			result[4][1] = cur.getString(cur.getColumnIndex("manaRegenLevel"));
 			result[5][1] = cur
 					.getString(cur.getColumnIndex("healthRegenLevel"));
 			result[6][1] = cur
 					.getString(cur.getColumnIndex("magicResistLevel"));
 			result[7][1] = cur.getString(cur.getColumnIndex("healthLevel"));
 			result[8][1] = cur.getString(cur.getColumnIndex("attackLevel"));
 		}
 		database.close();
 
 		return result;
 	}
 }
