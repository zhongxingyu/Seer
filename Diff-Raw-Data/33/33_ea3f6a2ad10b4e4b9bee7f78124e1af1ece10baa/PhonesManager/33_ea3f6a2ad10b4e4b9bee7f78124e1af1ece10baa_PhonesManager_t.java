 package com.vgershman.whocall.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vgershman
  * Date: 08.01.13
  * Time: 1:12
  * To change this template use File | Settings | File Templates.
  */
 public class PhonesManager {
 
     private SQLiteDatabase db;
     private DBHelper dbHelper;
 
     private static final String DBNAME = "KtoZvonil";
     private static final String TABLENAME = "phoneNumbers";
 
     public PhonesManager(Context mContext){
 
             List<String> fields = new ArrayList<String>();
             fields.add("phone text");
             dbHelper = new DBHelper(mContext, DBNAME, TABLENAME, fields);
     }
 
     public List<String> readPhoneNumbers(){
         List<String> phoneNumbers = null;
         db = dbHelper.getReadableDatabase();
         String []columns = {"phone"};
         Cursor cursor;
         cursor = db.query(TABLENAME,columns,null,null,null,null,"phone");
         phoneNumbers = new ArrayList<String>();
         while (cursor.moveToNext()) {
             if(cursor.getString(0)!=null){
                 phoneNumbers.add(cursor.getString(0));
             }
         }
         cursor.close();
         db.close();
         return phoneNumbers;
     }
 
     public void deletePhone(String phone){
         db = dbHelper.getWritableDatabase();
        db.delete(TABLENAME,"phone like '%" + phone+"'",null );
         //db.rawQuery("delete * from " + TABLENAME + "where phone ==" + phone,null);
         db.close();
     }
 
     public void addPhone(String phone){
         List<String> numbers = readPhoneNumbers();
         if(!numbers.contains(phone)){
             db = dbHelper.getWritableDatabase();
             ContentValues values = new ContentValues();
             values.put("phone", phone);
             db.insert(TABLENAME, null, values);
             db.close();
         }
     }
 }
