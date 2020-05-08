 package com.dnsalias.sanja.simplecarbocalc;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class ProdListOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION= 1;
 	private static final String DATABASE_NAME= "ProdList";
 		
     static final String FULLPRODLIST_TABLE_CREATE=
                 "CREATE TABLE " + ProdList.FULLPRODLIST_TABLE_NAME + " (" +
                 ProdList.PROD_ID + " INTEGER PRIMARY KEY, " +
                 ProdList.PROD_CARB + " REAL);";
     static final String FULLPRODLISTNAME_TABLE_CREATE=
             "CREATE TABLE " + ProdList.FULLPRODLISTNAME_TABLE_NAME + " (" +
             ProdList.PROD_ID + " INTEGER, " +
             ProdList.PROD_LANG + " TEXT(2), " +
             ProdList.PROD_NAME + " TEXT, " +
             ProdList.PROD_NAMES + " TEXT, " +
             "PRIMARY KEY (" + ProdList.PROD_ID + "," + ProdList.PROD_LANG + ")," +
            "UNIQUE (" + ProdList.PROD_ID + "," + ProdList.PROD_NAME + ")," +
            "UNIQUE (" + ProdList.PROD_ID + "," + ProdList.PROD_NAMES + "));";
     static final String PRODLIST_TABLE_CREATE=
             "CREATE VIRTUAL TABLE " + ProdList.PRODLIST_TABLE_NAME + " USING fts3 (" +
             ProdList.PROD__ID + " PRIMARY KEY, " +
             ProdList.PROD_NAME + " TEXT UNIQUE, " +
             ProdList.PROD_NAMES + " TEXT UNIQUE, " +
             ProdList.PROD_CARB + " REAL);";
     static final String LANGLIST_TABLE_CREATE=
             "CREATE TABLE " + ProdList.LANGLIST_TABLE_NAME + " (" +
             ProdList.PROD_LANG + " TEXT(2) PRIMARY KEY, " +
             ProdList.PROD_LANGNAME + " TEXT UNIQUE);";
     
 	ProdListOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }	
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(FULLPRODLIST_TABLE_CREATE);
 		db.execSQL(FULLPRODLISTNAME_TABLE_CREATE);
 		db.execSQL(PRODLIST_TABLE_CREATE);
 		db.execSQL(LANGLIST_TABLE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		 * No new version yet :)
		 */
 	}
 
 }
