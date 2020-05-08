 package activerecord;
 
 import java.lang.annotation.Annotation;
 import java.sql.SQLDataException;
 
 import activerecord.annotations.Model;
 
 import android.R;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 abstract class SQLiteActiveRecordHelper extends SQLiteOpenHelper 
 {
 	// public
 	
 	/*
 	 * constructor 
 	 * @param context
 	 * @param model
 	 */
 	public SQLiteActiveRecordHelper(Context context, Class model) 
 	{	
 		super(	context, 
 				getDbName(),  
 				null, // factory 
 				version(model) 
 				);	
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) 
 	{
 		String sql = this.createTableSqlString();
 		tryExecuteSql(db, sql);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
 	{
 		String sql = this.updateTableSqlString();
 		tryExecuteSql(db, sql);
 	}
 	
 	public long newRow(ActiveRecord row)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = mapObject(row);
 		long id = -1;
 		Log.d("xrx-d", "content values: " + values);
 		try
 		{
 			id = db.insertOrThrow(this.getTableName(), null, values);
 		}
 		catch (SQLiteException ex)
 		{
 			Log.e("xrx-sql", ex.getMessage() + "--- in SQLiteActiveRecordHelper.newRow()");
 		}
 		finally
 		{
 			db.close();
 		}
 		return id;
 	}
 	
 	
 
 	public void updateRow(ActiveRecord row)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = mapObject(row);
 		long id = row.getId();
 		try
 		{
 			db.update(getTableName(), values, "id=" + id, null);
 		}
 		catch (SQLiteException ex)
 		{
 			Log.e("xrx-sql", ex.getMessage());
 			throw ex;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public boolean deleteRow(ActiveRecord row) 
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		long id = row.getId();
 		boolean success;
 		try
 		{
 			db.delete(getTableName(), "id=" + id, null);
 			success = true;
 		}
 		catch (SQLiteException ex)
 		{
 			Log.e("xrx-sql", ex.getMessage());
 			success = false;
 			throw ex;
 		}
 		finally
 		{
 			db.close();
 		}	
 		return success;
 	}
 	
 
 	// abstract
 	
 	protected abstract ContentValues mapObject(ActiveRecord row);
 
 	protected abstract String getTableName();
 
 	protected abstract String updateTableSqlString();
 
 	protected abstract String createTableSqlString();
 	
 	// private
 
 	private static int version(Class model) 
 	{
 		Annotation[] annotations = model.getAnnotations();
 		for(Annotation annotation : annotations)
 		{
 			if ( annotation instanceof Model )
 			{
 				int version = ((Model) annotation).version();
 				if ( version > 0 )
 				{
 					return version;
 				}
 			}
 		}
 		return 1;
 	}
 
 	private static String getDbName() 
 	{
 		return "AppName".toLowerCase(); // TODO
 	}
 
 		
 	private void tryExecuteSql(SQLiteDatabase db, String sql) 
 	{
 		try
 		{
 			db.execSQL(sql);
 		}
 		catch (SQLiteException ex)
 		{
 			Log.e("xrx-sql", ex.getMessage());
 		}
 		Log.d("xrx-sql", "sql exectue success");		
 	}
 }
