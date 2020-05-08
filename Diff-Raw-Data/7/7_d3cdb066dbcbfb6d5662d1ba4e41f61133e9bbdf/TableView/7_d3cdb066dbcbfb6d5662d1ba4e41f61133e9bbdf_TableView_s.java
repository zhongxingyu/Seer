 /*******************************************************************************
  * Copyright (c) Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package com.mimpidev.dev.sql;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.internal.btree.SqlJetBtree;
 import org.tmatesoft.sqljet.core.internal.schema.SqlJetSchema;
 import org.tmatesoft.sqljet.core.schema.ISqlJetColumnDef;
 import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 import com.mimpidev.dev.debug.Log;
 import com.mimpidev.dev.sql.data.definition.SqlDefinition;
 import com.mimpidev.podsalinan.Podsalinan;
 
 /**
  * @author bugman
  *
  */
 public class TableView {
 
     /**
      * The database Connection
      */
 	private SqlJetDb db;
 	/**
 	 * The list of columns in the database table
 	 */
 	private ArrayList<SqlDefinition> columnList;
 	/**
 	 * The log.
 	 */
 	private Log log;
     /**
      * The name of the table this is related to.
      */
 	private String name;
 	/**
 	 * 
 	 */
 	private ISqlJetTable table;
 	/**
 	 * Status for when columns are added to the table 
 	 */
 	public static final int NEW_COLUMNS_ADDED = 1;
 	public static final int NOTHING_CHANGED = 0;
 	public static final int ERROR = -1;
 	
 	/**
 	 * 
 	 */
 	public static final int ITEM_ADDED_TO_DATABASE = 1;
 	/**
 	 * 
 	 */
 	public static final int ITEM_REMOVED_FROM_DATABASE = 2;
 	/**
 	 * 
 	 */
 	public static final int ITEM_UPDATED_IN_DATABASE = 3;
 	
 	public TableView(File databaseFile, String tableName, Log debugLog){
 		this (new ArrayList<SqlDefinition>(), tableName, debugLog);
 		db = new SqlJetDb(databaseFile,true);
 		initializeTable();
 	}
 	
 	private TableView(ArrayList<SqlDefinition> newColumnList, String tableName, Log debugLog){
 		columnList = newColumnList;
 		log = debugLog;
 		name=tableName;
 	}
 	
 	public TableView(File databaseFile, ArrayList<SqlDefinition> newColumnList, String tableName, Log debugLog){
 		this(newColumnList, tableName, debugLog);
 		db = new SqlJetDb(databaseFile,true);
 		initializeTable();
 	}
 	
 	public TableView(SqlJetDb newDb, ArrayList<SqlDefinition> newColumnList, String tableName, Log debugLog){
 		this(newColumnList, tableName, debugLog);
 		db = newDb;
 		initializeTable();
 	}
 
     /**
      * 
      */
 	public void initializeTable(){
 		try {
 			if (!setTable()){
 				createTable();
 			} else {
 				checkColumns();
 			}
 		} catch (SqlException e) {
 			e.getErrorCode();
 		}
 	}
 	
     /**
      * 
      * @return Error status
      */
 	public int checkColumns(){
 		int result=0;
 		for (int cc=0; cc<columnList.size(); cc++){
 			int newResult=0;
 			try {
 				newResult = addNewColumn(columnList.get(cc).name,columnList.get(cc).type);
 			} catch (SqlException e) {
 	        	log.println("[Table:"+name+"] Error Adding Column:"+columnList.get(cc).name);
 	        	newResult = -1;
 	        }
 			if (result>=0)
 				result=newResult;
 		}
 		
 		return result;
 	}
 	
 	public void dbCommit() throws SqlJetException{
 		db.commit();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isDbOpen(){
 		return ((db!=null)&&(db.isOpen()));
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws SqlException
 	 */
 	public boolean createTable() throws SqlException{
 		if ((name!=null)&&
 			(name.length()>1)&&
 			(columnList.size()>0)){
 			String sql = "CREATE TABLE IF NOT EXISTS "+name+" (";
 			// Do a for loop to go through each of the columns in columnList, and add them to the sql string
 			for (int cc=0; cc<columnList.size(); cc++){
 				sql.concat(columnList.get(cc).name+" "+columnList.get(cc).type);
 			}
 			
 			sql.concat(");");
 			if (isDbOpen()){
 				try {
 					db.beginTransaction(SqlJetTransactionMode.WRITE);
 					db.createTable(sql);
 					db.commit();
 					return true;
 				} catch (SqlJetException e) {
 					log.printStackTrace(e.getStackTrace());
 					throw new SqlException(SqlException.CREATE_TABLE_FAILED);
 				}
 			}
 			setTable();
 		}
 		throw new SqlException(SqlException.ERROR_DB_FAILURE);
 	}
 	
 	/**
 	 * This function is used to output a Map object of only the columns found in the currently loaded table 
 	 * @param data
 	 * @return
 	 */
 	public Map<String, Object> confirmColumns(Map<String, Object> data) {
 		Map<String, Object> values = new HashMap<String,Object>();
 		// The following loop checks the data passed in, and copies into the values Map, only columns
 		// that exist in this table.
 		for (int cc=0; cc<columnList.size(); cc++){
			if (data.containsKey(columnList.get(cc))){
 				values.put(columnList.get(cc).name, data.get(columnList.get(cc).name));
 			}
 		}
		
 		return values;
 	}
 	
 	/**
 	 * 
 	 * @param data
 	 * @return
 	 * @throws SqlException
 	 */
 	public boolean insert(Map<String,Object> data) throws SqlException{
 		Map<String, Object> values = confirmColumns(data);
 		// always call setTable
 		setTable();
 		if ((isDbOpen())&&(values.size()>0)){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_SET_TRANSACTION_MODE);
 			}
 			try{
 				table.insertByFieldNames(values);
 				db.commit();
 				return true;
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.FAILED_INSERT_RECORD);
 			}
 		} else if (!isDbOpen()){
 			throw new SqlException(SqlException.ERROR_DB_FAILURE);
 		} else if (values.size()==0) {
 			throw new SqlException(SqlException.ERROR_INVALID_TABLE);
 		}
 		return false;	
 	}
 	
     /**
      * 
      * @param data
      * @param condition
      * @return
      * @throws SqlException
      */
 	public boolean update(Map<String, Object> data, Map<String, Object> condition) throws SqlException{
 		Map<String, Object> values = confirmColumns(data);
 		setTable();
 		/* Search through table for condition (column, value)
 		 * When found, update the values with the values stored in data
 		 */
 		if ((isDbOpen())&&(values.size()>0)){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_SET_TRANSACTION_MODE);
 			}
 			try{
 				ISqlJetCursor updateCursor = table.lookup((String)condition.keySet().toArray()[0], condition.get((String)condition.keySet().toArray()[0]));
 				updateCursor.updateByFieldNames(values);
 				updateCursor.close();
 				db.commit();
 				return true;
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_UPDATING_RECORD);
 			}
 		} else if (!isDbOpen()){
 			throw new SqlException(SqlException.ERROR_DB_FAILURE);
 		} else if (values.size()==0) {
 			throw new SqlException(SqlException.ERROR_INVALID_TABLE);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param condition
 	 * @return
 	 */
 	public boolean delete(Map<String, Object> condition) throws SqlException{
 		Map<String, Object> values = confirmColumns(condition);
 		setTable();
 		if ((isDbOpen())&&(values.size()>0)){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_SET_TRANSACTION_MODE);
 			}
 			try {
 				ISqlJetCursor deleteCursor = table.lookup((String)condition.keySet().toArray()[0], condition.get((String)condition.keySet().toArray()[0]));
 				deleteCursor.delete();
 				deleteCursor.close();
 				db.commit();
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_DELETING_RECORD);
 			}
 		} else if (!isDbOpen()){
 			throw new SqlException(SqlException.ERROR_DB_FAILURE);
 		} else if (values.size()==0) {
 			throw new SqlException(SqlException.ERROR_INVALID_TABLE);
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param columnName
 	 * @param columnType
 	 * @return
 	 * @throws SqlException
 	 */
 	public int addNewColumn(String columnName, String columnType) throws SqlException{
 		boolean columnFound=false;
 		List<ISqlJetColumnDef> columns = null;
 
 		if (isDbOpen()){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_SET_TRANSACTION_MODE);
 			}
 			
 			try {
 				columns = db.getSchema().getTable(name).getColumns();
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_READING_TABLE_SCHEMA);
 			}
 			if (columns!=null){
 				for (ISqlJetColumnDef column : columns){
 					if (column.getName().equalsIgnoreCase(columnName))
 						columnFound=true;
 				}
 			} else {
 				return ERROR;
 			}
 			
 			if (!columnFound){
 				try {
 					db.alterTable("ALTER TABLE "+name+" ADD COLUMN "+columnName+" "+columnType.toUpperCase()+";");
 					log.println("[Table:"+name+"] Added Column:"+columnName);
 					return NEW_COLUMNS_ADDED;
 				} catch (SqlJetException e) {
 					log.printStackTrace(e.getStackTrace());
 					throw new SqlException(SqlException.FAILED_ADDING_NEW_COLUMN);
 				}
 			}
 		} else if (!isDbOpen()){
 			throw new SqlException(SqlException.ERROR_DB_FAILURE);
 		}
 		return NOTHING_CHANGED;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws SqlException
 	 */
 	public ISqlJetCursor selectAll() throws SqlException{
 		if (isDbOpen()){
 			ISqlJetCursor currentLine=null;
 			try {
 				db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 				currentLine = table.order(table.getPrimaryKeyIndexName());
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.FAILED_READING_RECORDS);
 			}
 			return currentLine;
 		}
 		throw new SqlException(SqlException.ERROR_DB_FAILURE);
 	}
 	
 	public ISqlJetCursor selectByField(Map<String, Object> conditions) throws SqlException{
 		Map<String, Object> values = confirmColumns(conditions);
 		if ((isDbOpen())&&(values.size()>0)){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 			} catch (SqlJetException e){
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.ERROR_SET_TRANSACTION_MODE);
 			}
 			try {
 				ISqlJetCursor currentLine = table.scope((String) values.keySet().toArray()[0],new Object[] {null}, new Object[] {values.get(values.keySet().toArray()[0])});
 				return currentLine;
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.FAILED_READING_RECORDS);
 			}
 		} else if (!isDbOpen()){
 			throw new SqlException(SqlException.ERROR_DB_FAILURE);
 		} else if (values.size()==0) {
 			throw new SqlException(SqlException.ERROR_INVALID_TABLE);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getTableName (){
 		return name;
 	}
 	
 	/**
 	 * @return the table
 	 */
 	public ISqlJetTable getTable() {
 		return table;
 	}
 
 	/**
 	 * @param table the table to set
 	 */
 	private boolean setTable() throws SqlException{
 		if (table==null){
 			try {
 				System.out.println(db.isOpen());
 				System.out.println(name);
 				table = db.getTable(name);
 			} catch (SqlJetException e) {
 				log.printStackTrace(e.getStackTrace());
 				throw new SqlException(SqlException.FAILED_SET_TABLE);
 			}
 		}
 		return true;
 	}
 	
 	/*TODO: I need to change the layout of the Table. remove the id field, and make sure we just have the text fields
 	 * that way we don't need to reset the table index. 
 	 */
 	public boolean purgeTable() throws SqlException{
 		if (table!=null){
 			try {
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 				table.clear();
 			} catch (SqlJetException e) {
 				e.printStackTrace();
 				//log.printStackTrace(e.getStackTrace());
 				//throw new SqlException(SqlException.ERROR_PURGE_TABLE);
 			}
 		}
 		
 		return true;
 	}
 }
