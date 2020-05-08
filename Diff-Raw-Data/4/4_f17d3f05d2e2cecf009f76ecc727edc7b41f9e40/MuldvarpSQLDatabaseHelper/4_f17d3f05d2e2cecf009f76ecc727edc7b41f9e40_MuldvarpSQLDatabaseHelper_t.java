 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * This class is responsible for creating the SQLITE database.
  * 
  * @author johan
  */
 public class MuldvarpSQLDatabaseHelper extends SQLiteOpenHelper {
 
     //General
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_NAME = "_name";
     public static final String COLUMN_UPDATED = "_updated";
     private static final String DATABASE_NAME = "muldvarp.db";
     private static final int DATABASE_VERSION = 1;
     //Programmes
     public static final String TABLE_PROGRAMME = "programmes";    
     public static final String COLUMN_PROGRAMME_NAME = "name";    
     //Courses
     public static final String TABLE_COURSES = "courses";    
     public static final String COLUMN_COURSENAME = "name";    
     //Quizzes
     public static final String TABLE_QUIZ = "Quiz";    
     public static final String COLUMN_QUIZNAME = "quizname";    
     
     
     
 
     // Database creation sql statement
     private static final String DATABASE_CREATE = "create table "
         + TABLE_PROGRAMME + "(" + COLUMN_ID
         + " integer primary key autoincrement, " + COLUMN_PROGRAMME_NAME
         + " text not null);";
 
     public MuldvarpSQLDatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context.deleteDatabase(DATABASE_NAME);
     }
 
     @Override
    public void onCreate(SQLiteDatabase database) {        
         database.execSQL(DATABASE_CREATE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(MuldvarpSQLDatabaseHelper.class.getName(),
             "Upgrading database from version " + oldVersion + " to "
                 + newVersion + ", which will destroy all old data");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAMME);
         onCreate(db);
     }
 
 }
