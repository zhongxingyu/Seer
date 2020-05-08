 package com.danilov.smsfirewall;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 class DBHelper extends SQLiteOpenHelper {
 	
 	private static String DBVERSION = "2";
 	private static String VERSION = "VERSION";
 
 	Context context;
 	
     public DBHelper(Context context) {
 
       super(context, "myDB", null, 1);
       this.context = context;
       if(isDatabaseExist()){
       	if(isUpgradeable()){
       		SQLiteDatabase db = this.getWritableDatabase();	
       		onUpgrade(db, 0, 1);
       	}
       }
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
       Log.d("g", "--- onCreate database ---");
 
       db.execSQL("create table mytable ("
           + "id integer primary key autoincrement," 
           + "name text," +
           "number text);");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	    ArrayList<String> numbers = new ArrayList<String>();
 	    PairOfList listOfNames = BlackListActivity.getNameFromContacts(context);
 		Cursor c = db.query("mytable", null, null, null, null, null, null);
 		if (c.moveToFirst()) {
 			int nameColIndex = c.getColumnIndex("name");
 			do {
 			       numbers.add(c.getString(nameColIndex));
 			}while (c.moveToNext());
 		}
 		int a = db.delete("mytable", null, null);
 		db.execSQL("ALTER TABLE mytable ADD COLUMN number TEXT;");
 		ContentValues cv = new ContentValues();
 		db = this.getWritableDatabase();
 		for(int i = 0; i < numbers.size(); i++){
 			cv.clear();
 			cv.put("number", numbers.get(i));
 			String name = BlackListActivity.findNameInList(numbers.get(i), listOfNames);
 			cv.put("name", name);
 			db.insert("mytable", null, cv);
 		}
 		SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
 		Editor ed = sPref.edit();
 		ed.putString(VERSION, DBVERSION);
 		ed.commit();
 	
     }
     
     public boolean isDatabaseExist(){
     	File dbFile=context.getDatabasePath("myDB");
     	return dbFile.exists();
     }
     
     public boolean isUpgradeable(){
     	String version = "";
     	SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
 		version = sPref.getString(VERSION, "0");
 		if(!(version.equals(DBVERSION))){
 			return true;
 		}
 		return false;
     }
     
     public void addToDb(String name, String number){
     	ArrayList<String> list = new ArrayList<String>();
     	SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put("name", name);
 		cv.put("number", number);
 		boolean isExistInDb = false;
 		Cursor c = db.query("mytable", null, null, null, null, null, null);
 		if (c.moveToFirst()) {
 			int nameColIndex = c.getColumnIndex("number");
 			do {
 		        list.add(c.getString(nameColIndex));
 		    } while (c.moveToNext());
 		}
 		c.close();
 		boolean isEmpty = false;
 		if(number.equals("")){
 			isEmpty = true;
 		}
 		for(int i = 0; i < list.size(); i++){
 			if(number.toLowerCase(Locale.getDefault()).equals(list.get(i).toLowerCase(Locale.getDefault()))){
 				isExistInDb = true;
 				break;
 			}
 		}
 		Resources res = context.getResources();
 		String exists = res.getString(R.string.alreadyExisits);
 		String empty = res.getString(R.string.empty);
 		if(!isExistInDb && !isEmpty){
 			long rowID = db.insert("mytable", null, cv);
 		}else if(isEmpty){
 			Toast toast = Toast.makeText(context, empty, Toast.LENGTH_SHORT);
 			toast.show();
 		}else{
 			Toast toast = Toast.makeText(context, exists, Toast.LENGTH_SHORT);
 			toast.show();
 		}
 		db.close();
     }
   }
