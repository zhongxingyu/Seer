 /**
  * 
  */
 package fi.dy.esav.FlightLog;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * @author Oppilas
  *
  */
 public class DBHelper extends SQLiteOpenHelper {
 	
 	 final static String DB_NAME = "FlightLogDB";
 	 final static int DB_VERSION = 1;
 	 
 	 final static int MODELS_ID_COLUMN = 0;
 	 final static int MODELS_NAME_COLUMN = 1;
 	 
 	 final static int FLIGHTS_ID_COLUMN = 0;
 	 final static int FLIGHTS_TIME_COLUMN = 1;
 	 final static int FLIGHTS_PLANE_COLUMN = 2;
 	 
 	 Context context;
 	 
 	 SQLiteDatabase rw_db;
 	 SQLiteDatabase ro_db;
 	
 	public DBHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 		this.context = context;
 		
 		rw_db = this.getWritableDatabase();
 		ro_db = this.getReadableDatabase();
 		
 		Log.d("FL", "Initialized db");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE models (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 				"name TEXT NOT NULL);");
 		db.execSQL("CREATE TABLE flights (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 				"time INTEGER NOT NULL," +
 				"model INTEGER NOT NULL," +
 				"FOREIGN KEY (model) REFERENCES models(_id));");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 	}
 	
 	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
 		return ro_db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
 	}
 	
 	public long insert(String table, String nullColumnHack, ContentValues values) {
 
 		return rw_db.insert(table, nullColumnHack, values);
 	}
 	
 	public void insert_test() {
 		Log.d("FL", "Inserting values to DB");
 		ContentValues values = new ContentValues();
 		values.put("name", "Radjet");
 		insert("models", null, values);
 		values.put("name", "Axn Floater Jet");
 		insert("models", null, values);
 	}
 	
 	public void list_all() {
 		Log.d("FL", "Printing models from DB");
 		Cursor c = query("models", null, null, null, null, null, null);
 		Log.d("FL", "Amoun of rows: " + c.getCount());
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			Log.d("FL", "_ID: " + c.getInt(0) + ", name: " + c.getString(1));
 			c.moveToNext();
 		}
 	}
 	
 	public ArrayList<HashMap<String, String>> getModels() {
 		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
 		Cursor c = query("models", null, null, null, null, null, null);
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			HashMap<String,String> map = new HashMap<String, String>();
			map.put("id", String.valueOf(c.getInt(0)));
			map.put("name", c.getString(1));
 			list.add(map);
 			c.moveToNext();
 		}
 		return list;
 	}
 	
 	public void close() {
 		rw_db.close();
 		ro_db.close();
 		super.close();
 	}
 	
 }
