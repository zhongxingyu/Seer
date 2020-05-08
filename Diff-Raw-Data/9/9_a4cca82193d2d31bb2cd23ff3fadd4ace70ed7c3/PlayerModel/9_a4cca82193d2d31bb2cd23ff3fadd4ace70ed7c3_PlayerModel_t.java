 package com.jlebrech.matchreport;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 
 public class PlayerModel {
 	PlayerDBHelper playerData;
 	
 	public PlayerModel(Context context){
 		playerData = new PlayerDBHelper(context);
 	}
 	
 	public Cursor getAll(){
 		SQLiteDatabase db = playerData.getReadableDatabase();
 	    Cursor cursor = db.query(PlayerDBHelper.TABLE, null, null, null, null,
 	        null, null);
 	    
 	    return cursor;
 	}
 
     public void addPlayer(String title) {
       SQLiteDatabase db = playerData.getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(PlayerDBHelper.TIME, System.currentTimeMillis());
       values.put(PlayerDBHelper.TITLE, title);
       db.insert(PlayerDBHelper.TABLE, null, values);
     }
 
     public void editPlayer(long player_id, String title) {
       ContentValues map = new ContentValues(); 
 
       map.put("title", title);
 
       String[] whereArgs = new String[]{Long.toString(player_id)};
      
       try{ 
     	  playerData.getWritableDatabase().update(PlayerDBHelper.TABLE, map, "_id=?", whereArgs);
       } catch (SQLException e) {
     	  Log.e("Error writing to Player", e.toString());
       }
     }

 }
