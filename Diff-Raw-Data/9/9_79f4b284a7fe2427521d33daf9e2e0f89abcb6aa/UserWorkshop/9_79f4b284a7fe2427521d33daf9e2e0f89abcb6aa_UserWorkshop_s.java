 package event.monitor.models;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.*;
 import android.util.Log;
 
 
 public class UserWorkshop extends SQLiteOpenHelper {
 	private static final String DB_TABLE_NAME = "user_workshops";
 	private static final String DB_COLUMN_1_NAME = "name";
 	private String sql;
 	SQLiteDatabase db;
 	private Object sqliteDBInstance;
 	
	public User(Context context, String name, CursorFactory factory, int version) {
 		super(context, name, factory, version);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {	
 		db.execSQL("CREATE TABLE user_workshops (_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, workshop_id INTEGER)");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void saveAttributes(String user_id, String workshop_id){
 		db=getWritableDatabase();
 		sql="INSERT INTO user_workshops (user_id, workshop_id) VALUES ('" + user_id + "','" + workshop_id + "')";
 		db.execSQL(sql);
 		db.close();
 	}
 	
 	public void destroy(String name){
 		db=getWritableDatabase();
 		db.delete("user_workshops", "id='" + name + "'", null);
 		db.close();
 	}
 	
 	public ArrayList<CUserWorkshop> all(){
 		db = getWritableDatabase();
 		sql = "SELECT _id, user_id, workshop_id FROM user_workshops";
 		Cursor cursor = db.rawQuery(sql, null);
 		ArrayList<CUserWorkshop> user_workshops = new ArrayList<CUserWorkshop>();
 
 		while (cursor.moveToNext())
 		{
 			CUserWorkshop user_workshop = new CUserWorkshop();
 			user_workshop._id = cursor.getString(0);
			user_workshop. = cursor.getString(1);
			user_workshop.email = cursor.getString(2);
			user_workshops.add(user);
 		}
 		db.close();
 		cursor.close();
 		return user_workshops;
 	}
 	
 }
