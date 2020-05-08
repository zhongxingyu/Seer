 package org.nosreme.app.urlhelper;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class UrlStore {
 	   private static final String URLSTORE_TABLE_NAME = "urls";
 	     
 	   static class DbHelper extends SQLiteOpenHelper {
         private static final String DATABASE_NAME = "urlstore2.db";
         private static final int DATABASE_VERSION = 2;
         
         private static final String URLSTORE_TABLE_CREATE =
                     "CREATE TABLE " + URLSTORE_TABLE_NAME + " (" +
                     "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "url TEXT," +
                     "time INTEGER," +
                     "seen INTEGER" +
                     ");";
 
         DbHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
         
          @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(URLSTORE_TABLE_CREATE);
             ContentValues values = new ContentValues();
             values.put("url", "http://www.nosreme.org");
             values.put("time", System.currentTimeMillis());
             values.put("seen", 0);
 			db.insert(URLSTORE_TABLE_NAME, "URL", values);
         }
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
             // Kills the table and existing data
             db.execSQL("DROP TABLE IF EXISTS " + URLSTORE_TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS urls");
             db.execSQL("DROP TABLE IF EXISTS URLS");
 
             // Recreates the database with a new version
             onCreate(db);
         }
     }
     
     private DbHelper dbhelper;
     
     public UrlStore(Context context)
     {
     	dbhelper = new DbHelper(context);
     }
     
     private String[] cols = new String[] { "_id", "url", "seen" };
     
     public Cursor getUrlCursor()
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
     	db = dbhelper.getReadableDatabase();
     	
     	Cursor cursor = db.query(URLSTORE_TABLE_NAME, cols, null, null, null, null, null);
     	
     	return cursor;
     	
     }
     public void addUrl(String url)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put("url", url);  
         values.put("time", System.currentTimeMillis());
         values.put("seen", 0);
 		db.insert(URLSTORE_TABLE_NAME, "URL", values);
     }
     public String getUrl(long id) 
     {
     	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	Cursor cursor = db.query(URLSTORE_TABLE_NAME, cols, "_id = " + Long.toString(id), null, null, null, null);
    	cursor.moveToFirst();
     	return cursor.getString(1);
     	
     }
 }
 
 
