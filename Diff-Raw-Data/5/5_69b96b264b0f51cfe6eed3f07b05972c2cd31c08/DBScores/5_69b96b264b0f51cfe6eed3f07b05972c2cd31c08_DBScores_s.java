 package com.turlutu;
 
 
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class DBScores
 {
     public static final String KEY_ID = "id";
     public static final String KEY_SCORE = "score";
     public static final String KEY_NAME = "name";
     private static final String TAG = "DBScore";
     
     private static final String DATABASE_NAME = "exitjump";
     private static final String DATABASE_TABLE = "scores";
     private static final int DATABASE_VERSION = 6;
 
     private static final String DATABASE_CREATE =
     "create table scores ("+KEY_ID+" integer primary key autoincrement, "
     + KEY_SCORE+" integer not null, "
     + KEY_NAME+" text not null);";
     
     private final Context context; 
     
     private DatabaseHelper DBHelper;
     private SQLiteDatabase db;
 
     public DBScores(Context ctx) 
     {
         this.context = ctx;
         DBHelper = new DatabaseHelper(context);
     }
         
     private static class DatabaseHelper extends SQLiteOpenHelper 
     {
         DatabaseHelper(Context context) 
         {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) 
         {
         	Log.i(TAG,"creation de la database");
             db.execSQL(DATABASE_CREATE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, 
         int newVersion) 
         {
             Log.w(TAG, "Upgrading database from version " + oldVersion 
                     + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
             onCreate(db);
         }
     }    
     
     //---opens the database---
     public DBScores open() throws SQLException 
     {
         db = DBHelper.getWritableDatabase();
         return this;
     }
 
     //---closes the database---    
     public void close() 
     {
         DBHelper.close();
         db.close();
     }
     
     public long insertScore(int score, String nom)
     {
     	Cursor c = getAllScores();
     	if (c.getCount() >= 1)
     	{
     		c.moveToLast();
     		if (c.getInt(1) < score)
     		{
     			return replace(c.getInt(0),score,nom);
     		}
     		else return -1;
     	}
     	else
     		return insert(score, nom);
     	
     }
     
     private long insert(int score, String nom)
     {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_SCORE , score);
         initialValues.put(KEY_NAME, nom);
         long retour = -1;
         try {
         	retour = db.insertOrThrow(DATABASE_TABLE, null, initialValues);}
         catch (SQLException e) {
         	Log.e(TAG,"error insert ("+score+","+nom+")");
         	return -1;}
         if (retour < 0)
         {
         	Log.w(TAG,"insert echoue ("+score+","+nom+") retour : "+retour);
     		return -1;
         }
         else
         {
         	Log.i(TAG,"insert ("+score+","+nom+") to index : "+retour);
 			return retour;
         }
     }
 
     private long replace(int id, int score, String nom)
     {
     	ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_ID , id);
         initialValues.put(KEY_SCORE , score);
         initialValues.put(KEY_NAME, nom);
         long retour = -1;
         try {
         	retour = db.replaceOrThrow(DATABASE_TABLE, null, initialValues);}
         catch (SQLException e) {
         	Log.e(TAG,"error replace ("+id+","+score+","+nom+")");
         	return -1;}
         if (retour < 0)
         {
         	Log.w(TAG,"replace echoue ("+id+","+score+","+nom+") retour : "+retour);
     		return -1;
         }
         else
         {
         	Log.i(TAG,"replace ("+id+","+score+","+nom+") to index : "+retour);
 			return retour;
         }
     }
     
     //---deletes a particular title---
     public boolean deleteScore(int score) 
     {
         return db.delete(DATABASE_TABLE, KEY_SCORE + 
         		"=" + score, null) > 0;
     }
 
     //---retrieves all the scores---
     public Cursor getAllScores() 
     {
         return db.query(DATABASE_TABLE, new String[] {
         		KEY_ID,
         		KEY_SCORE,
         		KEY_NAME}, 
                 null, 
                 null, 
                 null, 
                 null, 
                 KEY_SCORE + " DESC");
     }
 
     public boolean reset()
     {
     	long retour = -1;
     	
     	try {
    		retour = db.delete(DATABASE_TABLE,"1",null); }
     	catch (SQLException e) {
     		Log.e(TAG,"error reset : "+e.getMessage());
     		return false;
     	}
    	if (retour > 0)
     	{
     		Log.i(TAG,"reset ok");
     		return true;
     	}
     	else
     	{
     		Log.w(TAG,"reset echoue");
     		return false;
     	}
     }
     
     public static int getWorstScore(Context ctx)
     {
     	DBScores db = new DBScores(ctx);
     	db.open();
     	Cursor c = db.getAllScores();
     	if (c.moveToLast())
     	{
     		int worstScore = c.getInt(1);
     		Log.i(TAG,"getWorstScore : "+worstScore);
     		db.close();
     		return worstScore;
     	}
     	else
     	{
     		db.close();
     		Log.w(TAG,"getWorstScore echoue");
     		return -1;
     	}
     }
     
 }
 
 
