 package com.dgsd.android.uws.ShuttleTracker.Data;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class Db extends SQLiteOpenHelper {
     private static final String TAG = Db.class.getSimpleName();
 
     private static final int VERSION = 1;
     public static final String DB_NAME ="uws_shuttle_tracker.db";
 
     private static Db mInstance;
 
     public static Db getInstance(Context c) {
         if(mInstance == null)
             mInstance = new Db(c);
 
         return mInstance;
     }
 
     protected Db(Context context) {
         super(context.getApplicationContext(), DB_NAME, null, VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(DbTable.VEHICLE_READING.createSql());
        db.execSQL(DbTable.STOPS.createSql());
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL(DbTable.VEHICLE_READING.dropSql());
        db.execSQL(DbTable.STOPS.dropSql());
     }
 }
 
