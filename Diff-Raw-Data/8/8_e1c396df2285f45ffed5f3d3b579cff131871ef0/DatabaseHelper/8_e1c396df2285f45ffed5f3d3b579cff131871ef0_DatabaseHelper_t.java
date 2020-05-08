 /**
  *	DatabaseHelper.java
  *
  *  Manage Database creation and version managements. 
  *
  *	@author lisastenberg
  *	@copyright (c) 2012 Johan Brook, Robin Andersson, Lisa Stenberg, Mattias Henriksson
  *	@license MIT
  */
 
 package se.chalmers.watchme.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "watchme.db";
 
 	private static final String TRIGGER_DETACH = "detach";
 	private static final String TRIGGER_DETACH2 = "detach2";
 
 	/**
 	 * When a Movie is deleted, the rows in the HasTag-table containing that
 	 * Movie should also be deleted.
 	 */
 	private static final String CREATE_TRIGGER_DETACH = "CREATE TRIGGER "
 			+ TRIGGER_DETACH + " AFTER " + "DELETE ON "
 			+ MoviesTable.TABLE_MOVIES + " FOR EACH ROW BEGIN "
 			+ "DELETE FROM " + HasTagTable.TABLE_HAS_TAG + " WHERE "
 			+ HasTagTable.COLUMN_MOVIE_ID + " = old."
 			+ MoviesTable.COLUMN_MOVIE_ID + "; END;";
 
 	/**
 	 * When a Tag is deleted, the rows in the HasTag-table containing that Tag
 	 * should also be deleted.
 	 */
 	private static final String CREATE_TRIGGER_DETACH2 = "CREATE TRIGGER "
 			+ TRIGGER_DETACH2 + " AFTER " + "DELETE ON " + TagsTable.TABLE_TAGS
 			+ " FOR EACH ROW BEGIN " + "DELETE FROM "
 			+ HasTagTable.TABLE_HAS_TAG + " WHERE " + HasTagTable.COLUMN_TAG_ID
 			+ " = old." + TagsTable.COLUMN_TAG_ID + "; END;";
 
 	// TODO Delete if we don't get it to work correctly
 	private static final String CREATE_TRIGGER_DELETETAG = "CREATE TRIGGER deleteTag AFTER "
 			+ "DELETE ON "
 			+ HasTagTable.TABLE_HAS_TAG
 			+ " BEGIN "
 			+ "SELECT CASE WHEN (1 > (SELECT Count(*) "
 			+ "FROM "
 			+ HasTagTable.TABLE_HAS_TAG
 			+ " WHERE "
 			+ HasTagTable.COLUMN_TAG_ID
 			+ " = old."
 			+ HasTagTable.COLUMN_TAG_ID
 			+ "))"
 			+ " THEN "
 			+ "DELETE FROM "
 			+ TagsTable.TABLE_TAGS
 			+ " WHERE "
 			+ TagsTable.COLUMN_TAG_ID
 			+ " = old."
 			+ HasTagTable.COLUMN_TAG_ID
 			+ "; END; " + "END;";
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		MoviesTable.onCreate(db);
 		TagsTable.onCreate(db);
 		HasTagTable.onCreate(db);
 
 		db.execSQL(CREATE_TRIGGER_DETACH);
		db.execSQL(CREATE_TRIGGER_DETACH2);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		MoviesTable.onUpgrade(db, oldVersion, newVersion);
 		TagsTable.onUpgrade(db, oldVersion, newVersion);
 		HasTagTable.onUpgrade(db, oldVersion, newVersion);
 
 		db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_DETACH);
		db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_DETACH2);
 		db.execSQL(CREATE_TRIGGER_DETACH);
		db.execSQL(CREATE_TRIGGER_DETACH2);
 	}
 }
