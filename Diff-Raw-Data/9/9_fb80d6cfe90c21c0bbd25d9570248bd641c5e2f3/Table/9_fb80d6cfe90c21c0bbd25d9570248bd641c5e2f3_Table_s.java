 package activerecord;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import activerecord.annotations.Database;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 
 /*
  * represent a database schema of a single table, suitable to a ActiveRecord class
  */
 class Table extends SQLiteActiveRecordHelper
 {
 	private String name;
 	private Column[] cols;
 	private Context context;
 	
 	/*
 	 * constructor
 	 * 
 	 * @param modelClass Class, better be a subclass of ActiveRecord
 	 * 
 	 * generate database Table map (schema) for the modelClass
 	 *   
 	 */
 	public Table(Context context, Class modelClass) 
 	{	
 		super(context, modelClass); 
 		name = nameFromClass(modelClass);
 		this.context = context;
 		initCols(modelClass);
 	}
 
 	private static String nameFromClass(Class modelClass) 
 	{
 		String className = modelClass.getSimpleName();
 		return Grammar.toTableName(className);
 	}
 
 	private void initCols(Class modelClass) 
 	{
 		Field[] fields = getDatabaseFields(modelClass);
 		
 		this.cols = new Column[fields.length];
 		
 		for ( int i = 0 ; i < fields.length ; i ++ )
 		{
 			this.cols[i] = new Column(fields[i]);
 		}
 	}
 
 	private Field[] getDatabaseFields(Class modelClass) 
 	{
 		
 		Field[] allFields = modelClass.getDeclaredFields();
 		
 		ArrayList<Field> databaseFields = new ArrayList<Field>();
 		
 		for ( Field field : allFields )
 		{
 			
 			if ( isDatabaseField(field) )
 			{
 				databaseFields.add(field);
 			}
 		}		
 		Field[] output = new Field[databaseFields.size()];
 		for ( int i = 0 ; i < output.length ; i ++ )
 		{
 			output[i] = databaseFields.get(i);
 		}
 		return output;
 	}
 	
 	private boolean isDatabaseField(Field field) 
 	{
 		Log.d("field", field.getName());
 		
 		Annotation [] annotations = field.getAnnotations();
 		
 		Log.d("annotations", ""+annotations.length);
 		for(Annotation annotation : annotations)
 		{
 			if ( annotation instanceof Database )
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	/*
 	 * generate sql to drop the table and create a new updated version of it
 	 * 
 	 * TODO - smart-up this method so it wont necessarily drop the data
 	 */	 
 	@Override
 	protected String updateTableSqlString() 
 	{
		String sql = "DROP TABLE IF EXISTS " + this.name + "; " + this.createTableSqlString(); 		
 		return sql;
 	}
 
 	/*
 	 * generate sql create updated version of the table schema on the database
 	 * 
 	 * TODO - smart-up this method so it wont necessarily drop the data
 	 */
 	@Override
 	protected String createTableSqlString() 
 	{
 		String sql = 	"CREATE TABLE " + this.name + 
 				"(id INTEGER PRIMARY KEY";
 
 		for ( Column col : this.cols )
 		{
 			sql += ", " + col.getName() + " " + col.getType();
 		}
 		
 		sql += ");";
 					
 		return sql;
 	}
 
 	@Override
 	protected String getTableName() 
 	{
 		return name;
 	}
 
 	@Override
 	protected ContentValues mapObject(ActiveRecord row) 
 	{
 		Log.d("xrx-d", "mapObject(row)");
 		ContentValues map = new ContentValues();
 		for (Column col : cols)
 		{
 			
 			String name = col.getName();
 			String value = col.getStringValue(row);
 			
 			Log.d("xrx-d", "Iterate cols- name: " + col.getName() + ", value: " + value);
 			
 			map.put(name, value);
 			
 		}
 		return map;
 	}
 
 	public Object getContext() 
 	{	
 		return context;
 	}
 
 	
 
 	
 
 }
