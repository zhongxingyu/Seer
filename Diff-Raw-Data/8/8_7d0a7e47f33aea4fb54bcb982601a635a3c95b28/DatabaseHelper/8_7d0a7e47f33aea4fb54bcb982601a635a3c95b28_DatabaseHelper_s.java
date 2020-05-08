 package com.imaginea.android.sugarcrm.provider;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * This class helps open, create, and upgrade the database file.
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final String DATABASE_NAME = "sugar_crm.db";
 
     private static final int DATABASE_VERSION = 1;
 
     public static final String ACCOUNTS_TABLE_NAME = "accounts";
 
     private static final String TAG = "DatabaseHelper";
 
     DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         createAccountsTable(db);
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                                         + ", which will destroy all old data");
         db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME);
         onCreate(db);
     }
 
     private static void createAccountsTable(SQLiteDatabase db) {
 
        db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE_NAME + " (" + SugarBeans._ID
                                        + " INTEGER PRIMARY KEY," + SugarBeans.BEAN + " TEXT,"
                                        + SugarBeans.CREATED_DATE + " INTEGER,"
                                        + SugarBeans.MODIFIED_DATE + " INTEGER" + ");");
     }
 
 }
