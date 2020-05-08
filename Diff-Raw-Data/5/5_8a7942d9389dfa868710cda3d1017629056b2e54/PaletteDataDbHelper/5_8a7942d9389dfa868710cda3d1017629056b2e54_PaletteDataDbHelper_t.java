 package com.tzapps.tzpalette.db;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.tzapps.tzpalette.db.PaletteDataContract.PaletteDataEntry;
 import com.tzapps.tzpalette.db.PaletteDataContract.PaletteThumbEntry;
 
 public class PaletteDataDbHelper extends SQLiteOpenHelper
 {
     private static final String TAG = "PaletteDataDbHelper";
     
     /**
      * Database version.<br/><br/>
      * If you change the database schema, you must increment the database version.
      * <ul>
      * <li>Version 3: new thumb table</li>
      * <li>Version 4: palette table: isFavourite column</li>
      * </ul>
      */
     public static final int DATABASE_VERSION = 4;
     public static final String DATABASE_NAME = "TzPalette.db";
     
     private static final String TEXT_TYPE    = " TEXT";
     private static final String INTEGER_TYPE = " INTEGER";
     private static final String BLOB_TYPE    = " BLOB";
     private static final String COMMA_SEP    = ",";
     private static final String SEMICOLON_SEP = ";";
     
     private static String SQL_CREATE_PALETTE_TABLE =
             "CREATE TABLE " + PaletteDataEntry.TABLE_NAME + " (" +
             PaletteDataEntry._ID + " INTEGER PRIMARY KEY," +
             PaletteDataEntry.COLUMN_NAME_TITLE    + TEXT_TYPE    + COMMA_SEP +
             PaletteDataEntry.COLUMN_NAME_COLORS   + TEXT_TYPE    + COMMA_SEP +
             PaletteDataEntry.COLUMN_NAME_UPDATED  + INTEGER_TYPE + COMMA_SEP +
             PaletteDataEntry.COLUMN_NAME_IMAGEURL + TEXT_TYPE    + COMMA_SEP +
             // Version 4:
             PaletteDataEntry.COLUMN_NAME_ISFAVOURITE + INTEGER_TYPE +
             ")";
     
     private static String SQL_CREATE_THUMB_TABLE = 
             "CREATE TABLE " + PaletteThumbEntry.TABLE_NAME + " (" +
                     PaletteThumbEntry._ID + " INTEGER PRIMARY KEY," +
                     PaletteThumbEntry.COLUMN_NAME_PALETTE_ID + TEXT_TYPE    + COMMA_SEP +
                     PaletteThumbEntry.COLUMN_NAME_THUMB    + BLOB_TYPE    + 
                     ")";
     
     private static final String SQL_DELETE_PALETTE_TABLE =
             "DROP TABLE IF EXISTS " + PaletteDataEntry.TABLE_NAME;
     
     private static final String SQL_DELETE_THUMB_TABLE =
             "DROP TABLE IF EXISTS " + PaletteThumbEntry.TABLE_NAME;
     
     // Upgrade SQL statement for version 4
     private static final String SQL_UPGRADE_PALETTE_TABLE_V4 =
             "ALTER TABLE " + PaletteDataEntry.TABLE_NAME + " ADD COLUMN "
                 + PaletteDataEntry.COLUMN_NAME_ISFAVOURITE + TEXT_TYPE + SEMICOLON_SEP;
     
     public PaletteDataDbHelper(Context context)
     {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db)
     {
         db.execSQL(SQL_CREATE_PALETTE_TABLE);
         db.execSQL(SQL_CREATE_THUMB_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {
         Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + " .");
         
         if (newVersion > oldVersion)
         {
             // Upgrade
             // Note: SQLite3 only allows adding one column at a time. So we 
             // need to write new SQL statement for each new column added
             switch (oldVersion)
             {
                case 3:
                     // Upgrade from version 3 to 4.
                     try
                     {
                         db.execSQL(SQL_UPGRADE_PALETTE_TABLE_V4);
                     }
                     catch (SQLException e)
                     {
                         Log.e(TAG, "Error executing SQL: ", e);
                         // If the error is "duplicate column name" then everything is fine,
                         // as this happens after upgrading oldVersion->newVersion, then
                         // downgrading newVersion->oldVersion, and then upgrading again
                         
                     }
                     // fall through for further upgrades
                case 4:
                     // add more columns here
                     break;
                 
                 default:
                     Log.w(TAG, "Unknown version " + oldVersion + " . Creating new database.");
                     db.execSQL(SQL_DELETE_PALETTE_TABLE);
                     db.execSQL(SQL_DELETE_THUMB_TABLE);
                     onCreate(db);
             }
         }
         else
         {
             // newVersion <= oldVersion, downgrade
             Log.w(TAG, "Don't know how to downgrade. Will just not touch database and hope they are compatible.");
             // Do nothing.
         }
     }
     
     public void onDownGrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {
         onUpgrade(db, oldVersion, newVersion);
     }
     
 
 }
