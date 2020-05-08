 package ro.undef.patois;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.util.ArrayList;
 
 
 /**
  * Abstracts access to the Patois database.
  */
 public class PatoisDatabase {
 
     private static final String DATABASE_NAME = "patois";
     private static final int DATABASE_VERSION = 1;
     private static final String[] DATABASE_SCHEMA = new String[] {
         "CREATE TABLE languages ( " +
         "    _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "    code TEXT NOT NULL, " +
        "    name TEXT NOT NULL, " +
         ");",
 
         "CREATE TABLE words ( " +
         "    _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "    name TEXT NOT NULL, " +
        "    language_id INTEGER NOT NULL, " +
         ");",
 
         "CREATE TABLE translations ( " +
         "    word_id1 INTEGER NOT NULL, " +
        "    word_id2 INTEGER NOT NULL, " +
         ");",
     };
 
     private final Context mCtx;
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             for (String statement : DATABASE_SCHEMA) {
                 db.execSQL(statement);
             }
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             // TODO: When we'll have a new version of the database, implement
             // this method.
         }
     }
 
     /**
      * Creates a new PatoisDatabase.
      *
      * @param ctx the Context to use to access the database.
      */
     PatoisDatabase(Context ctx) {
         this.mCtx = ctx;
     }
 
     /**
      * Opens a new database connection, and initializes the database if necessary.
      *
      * @throws SQLException if the database could not be opened nor created.
      */
     public void open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
     }
 
     /**
      * Closes the database connection.
      */
     public void close() {
         mDbHelper.close();
     }
 
     public ArrayList<Language> getLanguages() {
         ArrayList<Language> languages = new ArrayList<Language>();
 
         Cursor cursor = mDb.query("languages", new String[] { "_id", "code", "name" },
                 null, null, null, null, null);
         while (cursor.moveToNext()) {
             languages.add(
                     new Language(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
         };
         cursor.close();
 
         return languages;
     }
 }
