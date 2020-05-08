 package com.chalmers.schmaps;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SearchSQL {
 
 	public static final String KEY_ROWID = "_id"; //raden i databasen
 	public static final String KEY_ROOM = "room_name";
 	public static final String KEY_LAT = "latitude";
 	public static final String KEY_LONG = "longitude";
 	public static final String KEY_STREET = "street_name";
 	public static final String KEY_LEVEL = "level";
 	
 	private static final String DATABASE_NAME = "Schmapsdb"; //namnet på vår databas
 	private static final String DATABASE_TABLE = "RoomTable"; //namnet på vår tabell (kan ha flera tabeller)
 	private static final int DATABASE_VERSION = 1;
 	
 	private MySQLiteOpenHelper ourHelper;
 	private final Context ourContext;
 	private SQLiteDatabase ourDatabase;
 	
	public SearchSQL(Context c){
 		ourContext = c;
 		
 	}
 	
	public SearchSQL open(){
 		ourHelper = new MySQLiteOpenHelper(ourContext);
 		ourDatabase = ourHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public void createEntry(){ //ska lägga in med den här metoden
 		ContentValues cv = new ContentValues();
 		
 		/* ett exempel på hur man lägger in
 		cv.put(KEY_ROOM, "Hej");
 		cv.put(KEY_LAT, 58070517);
 		cv.put(KEY_LONG, 11760864);
 		cv.put(KEY_STREET, "Chalmersplatsen 2");
 		cv.put(KEY_LEVEL, "Plan 4");
 		ourDatabase.insert(DATABASE_TABLE, null, cv);
 		*/ 
 		// tips på hur man kan mata in från fil http://stackoverflow.com/questions/8801423/how-can-i-insert-data-in-an-sqlite-table-from-a-text-file-in-android
 		
 	}
 	
 	
 	
 	
 	public void close(){ 
 		ourHelper.close();
 	}
 	
 	private static class MySQLiteOpenHelper extends SQLiteOpenHelper{
 
 		public MySQLiteOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 			db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +
 					KEY_ROWID + " INTEGER AUTOINCREMENT, " +
 					KEY_ROOM + " TEXT PRIMARY KEY, " +
 					KEY_LAT + " INTEGER, " + 
 					KEY_LONG + " INTEGER, " +
 					KEY_STREET + " TEXT NOT NULL, " +
 					KEY_LEVEL + " TEXT NOT NULL);"		
 			);				
 		}
 		
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE);
 			onCreate(db);
 			
 		}
 		
 	}
 	
 }
