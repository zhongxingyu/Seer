 package fr.itinerennes.database;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.impl.AndroidLoggerFactory;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.exceptions.ItineRennesRuntimeException;
 import fr.itinerennes.utils.FileUtils;
 
 /**
  * The database helper. Manage database creation and update.
  * 
  * @author Jérémie Huchet
  */
 public final class DatabaseHelper extends SQLiteOpenHelper {
 
     /** The event logger. */
     private static final Logger LOGGER = AndroidLoggerFactory.getLogger(DatabaseHelper.class);
 
     /** The database name. */
     private static final String DATABASE_NAME = "fr.itinerennes";
 
     /** The database create script URI. */
     private static final String CREATE_SCRIPT = "database/create.sql";
 
     /** The database drop script URI. */
     private static final String DROP_SCRIPT = "database/drop.sql";
 
     /** The database upgrade script URI. */
     private static final String UPGRADE_SCRIPT = "database/upgrade_%s_to_%s.sql";
 
     /** The assets manager. */
     private final AssetManager assets;
 
     /**
      * Creates the database helper.
      * 
      * @param context
      *            the context
      */
     public DatabaseHelper(final Context context) {
 
         super(context, DATABASE_NAME, null, ItineRennesConstants.DATABASE_VERSION);
         assets = context.getAssets();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
      */
     @Override
     public void onCreate(final SQLiteDatabase db) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(String.format("database creation - name=%s, version=%d, script=%s",
                     DATABASE_NAME, ItineRennesConstants.DATABASE_VERSION, CREATE_SCRIPT));
         }
 
         execScript(db, readScript(CREATE_SCRIPT));
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
      *      int, int)
      */
     @Override
     public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(String.format("database upgrade - name=%s, oldVersion=%d, newVersion=%d",
                     DATABASE_NAME, oldVersion, newVersion));
         }
 
         for (int i = oldVersion; i < newVersion; i++) {
             final String script = String.format(UPGRADE_SCRIPT, i, i + 1);
 
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug(String.format("executing database upgrade script : %s", script));
             }
 
            execScript(db, readScript(script));
         }
 
     }
 
     /**
      * Reads a script located in the assets folder.
      * 
      * @param filename
      *            the name of the asset to read
      * @return the content of the script
      */
     private String readScript(final String filename) {
 
         try {
             return FileUtils.read(assets.open(filename, AssetManager.ACCESS_STREAMING));
         } catch (final IOException e) {
             final String msg = String
                     .format("Failed to read packaged database script: an error occured while accessing asset %s",
                             filename);
             LOGGER.error(msg);
             throw new ItineRennesRuntimeException(msg, e);
         }
     }
 
     /**
      * Executes the given script on the database. The script must contains ';' only for statement
      * separation. If a value contains a ';' the execution will crash.
      * 
      * @param db
      *            the database to use
      * @param script
      *            the sql script to execute
      */
     private void execScript(final SQLiteDatabase db, final String script) {
 
         final String[] statements = script.replaceAll("[\r\n]*", "").replaceAll("[\n\r]*\\s*$", "")
                 .split(";");
 
         try {
             db.beginTransaction();
             for (final String statement : statements) {
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("execute SQL : [{}]", statement);
                 }
                 db.execSQL(statement);
 
             }
             db.setTransactionSuccessful();
         } finally {
             db.endTransaction();
         }
     }
 }
