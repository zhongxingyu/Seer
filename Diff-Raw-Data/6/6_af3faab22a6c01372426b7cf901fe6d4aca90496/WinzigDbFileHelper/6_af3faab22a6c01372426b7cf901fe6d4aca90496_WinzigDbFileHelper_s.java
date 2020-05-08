 /**
  *
  */
 package de.theappguys.winzigsql;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Extension of SQLiteOpenHelper that handles initialization of
  * a database by using a database from a file in the assets.
  *
  * Automatically initializes the database from a db file in the assets.
  * The file must be named like your database, so if your db is named
  * "foo", it looks for the file "assets/foo.db"
  *
  * This helper has only very simple upgrade conventions, it simply completely
  * replaces the current db with the one from the assets. If you need to preserve
  * data that might have been added to the db, make sure you overwrite onCreate
  * and handle that case manually.
  *
  * Works hand-in-hand with the WinzigDbProvider
  */
 public class WinzigDbFileHelper extends SQLiteOpenHelper {
 
     public static final String LOG_TAG = "winzigsql";
 
 	private final Context context;
 
 	private final String dbName;
 
 	private final File dbFile;
 
 	/**
 	 * Let your custom db provider extends this class and call the superconstructor like so:
 	 * <pre><code>
 	 * private MyDbHelper(final Context context) {
 	 *     super (context, "myDbName", 42);
 	 * }
 	 * </code></pre>
 	 *
 	 * Then, to obtain instances, implement a get Instance method like this:
 	 * <pre><code>
 	 * private static MyDbHelper instance = null;
 	 *
 	 * public static MyDbHelper(final Context context) {
 	 *     if (instance == null) {
 	 *         return (instance = new MyDbHelper(context))
 	 *     } else {
 	 *         return instance;
 	 *     }
 	 * }
 	 * </code></pre>
 	 *
 	 */
 	public WinzigDbFileHelper(final Context context, final String dbName, final int dbVersion) {
 		super(context, dbName, null, dbVersion);
 		//TODO: use constructor with error handler with last argument
 		//final DatabaseErrorHandler errorHandler
 		this.dbName = dbName;
 		this.context = context.getApplicationContext();
 		this.dbFile = context.getDatabasePath(dbName);
 	}
 
 	@Override
 	public SQLiteDatabase getReadableDatabase() {
 	    if (!dbExists()) copyDb();
 	    return super.getReadableDatabase();
 	}
 
 	@Override
     public SQLiteDatabase getWritableDatabase() {
         if (!dbExists()) copyDb();
         return super.getWritableDatabase();
     }
 
 	/** (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(final SQLiteDatabase db) {
 		//nothing to do, db file is corerctly initialized by calling copyDb
 	    //from getReadable/WritableDatabase
 	}
 
 	/**
      * Just overwrites the existing database with the newest one from the assets.
      * If you store custom data in your database after copying it from the assets,
      * you need to overwrite this method and make sure the old data is preserved.
 	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(final SQLiteDatabase db,
 			              final int oldVersion,
 			              final int newVersion) {
 	    //we just erase the old database and replace it
 	    db.close();
 		copyDb();
 	}
 
 	/**
 	 * copies the database from the assets to the db file location
 	 * @throws IOException
 	 */
 	protected void copyDb() {
 	    final String assetName = dbName + ".sqlite";
 	    Log.d(LOG_TAG, "Copying db '" + assetName + "'");
 	    try {
     	    final InputStream in = context.getAssets().open(assetName);
     	    try {
     	        final OutputStream out = new FileOutputStream(dbFile);
     	        try {
     	            ResourceUtils.copy(in, out);
     	        } finally {
     	            out.close();
     	        }
     	    } finally {
     	        in.close();
     	    }
 	    } catch (final IOException e) {
 	        throw new SQLiteException(
 	                "Could not copy db '" + dbName + "' from asset '" +
	                assetName + "' to file '" + dbFile + "'.");
 	    }
 	}
 
 	/**
 	 * @return if the database was already copied or not
 	 */
 	protected boolean dbExists() {
 	    return dbFile.isFile();
 	}
 
 
 	/**
 	 * @return true: db was deleted.
 	 */
 	public boolean dropDb() {
 		return context.deleteDatabase(dbName);
 	}
 }
