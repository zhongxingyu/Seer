 package edu.colorado.trackers.db;
 
 import java.io.File;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * This class implements SQLLite database functionality
  * This is a implementation class and shouldn't be used
  * directly.
  * @author Vikas
  *
  */
 
 public class SQLiteDBImpl {
 
 	@SuppressWarnings("unused")
 	private static final int version = 1;
 	SQLiteDatabase dbReadable;		
 	SQLiteDatabase dbWritable;
 	private boolean isRead = false;
 	private boolean isWritten = false;
 	private String dbName;
 	private Context context;
 
 	
 	public SQLiteDBImpl(Context con, String db) {
 		dbName = db;
 		context = con;
 	}
 
 	protected void openReadMode() {
 		if (!isRead) {	
 			File dbFile= context.getDatabasePath(dbName);	
			dbReadable = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
 			isRead = true;
 		}
 	}
 	
 	protected void openWriteMode() {
 		if (!isWritten) {			
 			File dbFile= context.getDatabasePath(dbName);
 			dbWritable = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);			
 			isWritten = true;
 		}
 	}
 
 	public Selector selector(String table) {
 		openReadMode();
 		Selector select = new Selector(dbReadable, table);
 		return select;
 	}
 	
 	public boolean tableExists(String tableName) {
 		openReadMode();
 		String[] strs = {tableName};
 		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
 		Cursor curs = dbReadable.rawQuery(sql, strs);
 		int count = 0;
 		count = curs.getCount();
 		curs.close();
 		if (count == 0) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}		
 
 
 	public void createTable(String tableName, String cols) {	
 		openWriteMode();	
 		dbWritable.execSQL("create table " + tableName + "(" + cols + ");");
 	}
 	
 	public void dropTable(String tableName) {
 		openWriteMode();	
 		dbWritable.execSQL("drop table " + tableName);
 	}
 
 	public Inserter inserter(String tableName) {
 		openWriteMode();		
 		Inserter ins = new Inserter(dbWritable, tableName);
 		return ins;
 	}
 		
 	public Deleter deleter(String tableName) {
 		openWriteMode();		
 		// delete rows from a table.
 		Deleter del = new Deleter(dbWritable, tableName);
 		return del;
 	}
 
 	public void close() {
 		if (isRead) {				
 			dbReadable.close();
 			isRead = false;
 		}
 		if (isWritten) {				
 			dbWritable.close();
 			isWritten = false;
 		}
 	}
 	
 }
