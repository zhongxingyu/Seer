 package com.raulete.evoluzzion.models;
 
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import com.raulete.evoluzzion.db.DatabaseHelper;
 import com.raulete.evoluzzion.models.interfaces.DatabaseModel;
 
 public class Model implements DatabaseModel{
   
 	public static String FIELD_ID = "_id";
 	public String TAG = "Evoluzzion";
 	Context context;
 	long id;
 	long _id;
 	DatabaseHelper mDbHelper;
 
 	public Model(){}
 
 	public Model(Context paramContext){ this.context = paramContext; }
 
 	public void setContext(Context paramContext){
 		this.context = paramContext;
 	}
   
 	public void clearTable(){
 		this.mDbHelper = new DatabaseHelper(this.context);
 		try{
 			this.mDbHelper.getWritableDatabase().execSQL("DELETE FROM " + getTableName());
 		} finally {
 			this.mDbHelper.close();
 		}
 	}
 
 	public List<DatabaseModel> cursor2ListOptions(Cursor paramCursor){
 		return null;
 	}
 
 	public List<DatabaseModel> find(String paramString){
 		this.mDbHelper = new DatabaseHelper(this.context);
 		List<DatabaseModel> localList = null;
 		try{
 			Cursor localCursor = this.mDbHelper.getReadableDatabase().rawQuery(paramString, null);
 			localList = cursor2ListOptions(localCursor);
 			localCursor.close();
 		} finally {
 			this.mDbHelper.close();
 		}
 		return localList;
 	}
 
 	public List<DatabaseModel> findAll(){
 		return find("SELECT * FROM " + getTableName());
 	}	
 
 	public boolean delete(long id){
 		this.mDbHelper = new DatabaseHelper(this.context);
		String deleteQuery = getTableName() + "._id = ?";
		String values[] = {id + ""};
 		try{
			this.mDbHelper.getWritableDatabase().delete(getTableName(), deleteQuery, values);
 		} finally {
 			this.mDbHelper.close();
 		}
 		return true;
 	}
 	
 	public long getId(){
 		return this._id;
 	}
 	
 	public String getIdField(){
 		return Model.FIELD_ID;
 	}
 
 	public String getSelectSQL(long paramLong){
 		return null;
 	}
 
 	public String getSqlWithCondition(String paramString, long paramLong){
 		String str = " SELECT *  FROM " + getTableName() + " WHERE " + getTableName() + "." + paramString + "=" + paramLong;
 		return str;
 	}
 
 	public String getTableName(){
 		return null;
 	}
 
 	public ContentValues parse2ContentValues(){
 		return null;
 	}
 
 	public void parseCursor(Cursor paramCursor){}
 
 	public long parseToLong(String paramString){
 		long l2 = 0;
 		try{
 			l2 = Long.parseLong(paramString);
 		} catch (Exception localException){
 			
 		}
 		return l2;
 	}
 
 	public DatabaseModel read(long paramLong){
 		this.mDbHelper = new DatabaseHelper(this.context);
 		try{
 			Cursor localCursor = this.mDbHelper.getReadableDatabase().rawQuery(getSqlWithCondition(getIdField(), paramLong), null);
 			localCursor.moveToFirst();
 			parseCursor(localCursor);
 			localCursor.close();
 			return this;
 		} finally {
 			this.mDbHelper.close();
 		}
 	}
 
 	public boolean save(){
 		boolean success = false;
 		this.mDbHelper = new DatabaseHelper(this.context);
 		try{
 			SQLiteDatabase localSQLiteDatabase = this.mDbHelper.getWritableDatabase();
 			ContentValues localContentValues = this.parse2ContentValues();
 			try{
 				this.id = localSQLiteDatabase.insertOrThrow(getTableName(), null, localContentValues);
 			}catch(SQLiteConstraintException e){
 				this.id = -1;
 			}
 			if(this.id == -1){
 				if( (localSQLiteDatabase.update(getTableName(), localContentValues, "_id = " + getId(), null) == 1 ))
 					this.id = getId();
 			} else {
 				this.id = getId();
 			}
 			if (this.id == -1)
 				success = false;
 			else
 				success = true;
 		}catch (Exception e){
 			Log.i(TAG, e.toString());
 			Log.i(TAG, e.getMessage());
 			this.mDbHelper.close();
 		}finally{
 			this.mDbHelper.close();
 		}
 		return success;
 	}
 
 	public void setTextView(View paramView, int paramInt, String paramString){
 		TextView localTextView = (TextView)paramView.findViewById(paramInt);
 		if (localTextView != null)
 			localTextView.setText(paramString);
 	}
 
 	public View populateItem(View view) {
 		return null;
 	}
 	
 	public View populateListItem(View view) {
 		return null;
 	}
 }
