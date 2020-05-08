 package ro.undef;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * Abstracts access to the Patois database.
  */
 public class PatoisDatabase {
 
     private static final String DATABASE_NAME = "patois";
    private static final int DATABASE_VERSION = 1;
     private static final String[] DATABASE_SCHEMA = new String[] {
         "CREATE TABLE languages ( " +
         "    id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "    code TEXT NOT NULL, " +
         "    name TEXT NOT NULL, " +
         ");",
 
         "CREATE TABLE words ( " +
         "    id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "    name TEXT NOT NULL, " +
         "    language_id INTEGER NOT NULL, " +
         ");",
 
         "CREATE TABLE translations ( " +
         "    word_id1 INTEGER NOT NULL, " +
         "    word_id2 INTEGER NOT NULL, " +
         ");",
     };
 
     private final Context mCtx;
 
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
 }
