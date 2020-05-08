 package com.krld.memorize.common;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DbOpenHelper extends SQLiteOpenHelper {
     public static final int DB_VERSION = 5;
     public static final String DB_NAME = "mysqllite";
 
     public static final String MEASUREMENT = "measurement";
     public static final String VALUE = "weight";
     public static final String DATE = "date";
     public static final String TYPE = "type";
     // public static final String DATE_STRING = "date_string";
     // public static final String LOGIN = "login";
     //public static final String PASSW = "passw";
     public static final String CREATE_TABLE = "create table " + MEASUREMENT + " ( _id integer primary key autoincrement, "
             + VALUE + " REAL, " + DATE + " INTEGER," + TYPE + " TEXT)";
 
     public DbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
         super(context, name, factory, version);
     }
 
     @Override
     public void onCreate(SQLiteDatabase sqLiteDatabase) {
         sqLiteDatabase.execSQL(CREATE_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
         if (oldVersion == 2 && newVersion == 3) {
             sqLiteDatabase.execSQL("ALTER TABLE " + MEASUREMENT + " ADD COLUMN " + TYPE + " TEXT");
         }
        if (oldVersion != newVersion && newVersion == 5) {
            sqLiteDatabase.execSQL("ALTER TABLE " + MEASUREMENT + " ADD COLUMN " + TYPE + " TEXT");
             sqLiteDatabase.execSQL("update " + MEASUREMENT + " set " + TYPE + " = '" + DataType.WEIGHT + "' ");
         }
     }
 }
