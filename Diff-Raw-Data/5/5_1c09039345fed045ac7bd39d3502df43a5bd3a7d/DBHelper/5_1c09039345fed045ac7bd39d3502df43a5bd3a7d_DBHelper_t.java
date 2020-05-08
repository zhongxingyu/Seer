 package com.redsemaphore.livingword;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBHelper extends SQLiteOpenHelper {
 	public static final String KEY_ROWID = "_id";
 	public static final String PASSAGE = "passage";
 	public static final String CONTENT = "content";
 
 	private static final int DB_VERSION = 1;
 
 	// TABLE NAMES are defined through res/values/array.xml
 	private static final String DB_PATH = "/data/data/com.redsemaphore.livingword/databases/";
 	private static final String DB_NAME = "the_word_db";
 	private SQLiteDatabase myDB;
 	private final Context myContext;
 
 	/**
 	 * Constructor
 	 */
 
 	public DBHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 		this.myContext = context;
 	}
 
 	/**
 	 * Copies database from file on hand to output file
 	 * 
 	 */
 	public void copyDatabase() {
 		// to trick the system in creating an
 		// empty db for us in the path
 		this.getReadableDatabase();
 
 		InputStream myInput;
 		OutputStream myOutput;
 		// Open local db as input stream
 		try {
 			myInput = myContext.getAssets().open(DB_NAME);
 		} catch (IOException e) {
			throw new Error("Cant open resource DB");
 		}
 		// Path to the newly created empty db
 		String outFileName = DB_PATH + DB_NAME;
 
 		// Open the empty db as output stream
 		try {
 			myOutput = new FileOutputStream(outFileName);
 		} catch (FileNotFoundException e) {
			throw new Error("Cant open target DB");
 		}
 
 		// transfer bytes from input file to output file
 		byte[] buffer = new byte[1024];
 		int length;
 		try {
 			while ((length = myInput.read(buffer)) > 0) {
 				myOutput.write(buffer, 0, length);
 			}
 
 			// close streams
 			myOutput.flush();
 			myOutput.close();
 			myInput.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if DB file exists on path and copies a new db if it doesn't
 	 * 
 	 * 
 	 */
 	public boolean checkDataBase() {
 		SQLiteDatabase checkDB = null;
 		// int dbVersion = 0;
 		try {
 			String myPath = DB_PATH + DB_NAME;
 			checkDB = SQLiteDatabase.openDatabase(myPath, null,
 					SQLiteDatabase.OPEN_READONLY);
 		} catch (SQLiteException e) {
 			// database cannot be opened (doesn't exist!)
 		}
 
 		if (checkDB != null) {
 			// dbVersion = checkDB.getVersion();
 			checkDB.close();
 			return true;
 		} else {
 			return false;
 		}
 
 		// if (dbVersion >= DB_VERSION)
 		// return true;
 		// else
 		// return false;
 	}
 
 	/**
 	 * Opens the database for reading from
 	 * 
 	 * @throws SQLiteException
 	 */
 	public void openDB() throws SQLiteException {
 		String myPath = DB_PATH + DB_NAME;
 		myDB = SQLiteDatabase.openDatabase(myPath, null,
 				SQLiteDatabase.OPEN_READONLY);
 
 	}
 
 	/**
 	 * Closes the database
 	 */
 	@Override
 	public synchronized void close() {
 		if (myDB != null)
 			myDB.close();
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Mutator. changes the values of parameters to one of the values in the db.
 	 * 
 	 * @param address
 	 * @param content
 	 */
 	public Verse getRandom(ArrayList<String> topics) {
 		String passage = "nothing";
 		String content = "nothing";
 
 		Random rand = new Random();
 
 		String tableName = topics.get(rand.nextInt(topics.size()));
 
 		int entries = (int) DatabaseUtils.queryNumEntries(myDB, tableName);
 		int i = rand.nextInt(entries) + 1;
 		String[] columns = { PASSAGE, CONTENT };
 		Cursor c = myDB.query(tableName, columns, KEY_ROWID + "=" + i, null,
 				null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			passage = c.getString(c.getColumnIndex(PASSAGE));
 			content = c.getString(c.getColumnIndex(CONTENT));
 		}
 		return new Verse(passage, content);
 	}
 
 }
