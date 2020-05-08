 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2011 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol". 
  */
 
 package com.funambol.storage;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.content.ContentValues;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.Cursor;
 
 import com.funambol.util.Log;
 
 public class SQLiteTable extends Table {
 
     private static final String TAG_LOG = "SQLiteTable";
     private static final int FUNAMBOL_DATABASE_VERSION = 1;
 
     private static final int KEY_MAX_LENGTH = 128;
     private static final int VALUE_MAX_LENGTH = 128;
 
     private Context context;
     private String  dbName;
     private SQLiteDatabase dbStore;
     private DatabaseHelper mDatabaseHelper = null;
 
     private final Object lock = new Object();
 
     private int numRefs = 0;
 
     public SQLiteTable(Context context, String dbName, String tableName, int colsType[], int keyIdx) {
         this(context, dbName, tableName, colsType, keyIdx, false);
     }
 
     public SQLiteTable(Context context, String dbName, String tableName, String colsName[], int colsType[], int keyIdx) {
         this(context, dbName, tableName, colsName, colsType, keyIdx, false);
     }
 
     public SQLiteTable(Context context, String dbName, String tableName, int colsType[], int keyIdx,
                        boolean autoincrement)
     {
         super(tableName, colsType, keyIdx, autoincrement);
         this.dbName  = dbName;
         this.context = context;
     }
 
     public SQLiteTable(Context context, String dbName, String tableName, String colsName[], int colsType[], int keyIdx,
                        boolean autoincrement)
     {
         super(tableName, colsName, colsType, keyIdx, autoincrement);
         this.dbName  = dbName;
         this.context = context;
     }
 
     @Override
     public void open() throws IOException {
         synchronized(lock) {
 
             numRefs++;
 
             // If the table is already open, we have nothing to do
             if (dbStore != null) {
                 return;
             }
 
             mDatabaseHelper = new DatabaseHelper(context, dbName, getName());
 
             // Create the table containing the key value pairs (if it does not exist
             // already)
             dbStore = mDatabaseHelper.getWritableDatabase();
             try {
                 dbStore.execSQL(getCreateSQLCommand());
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot create table", e);
                 throw new IOException("Cannot create table " + getName());
             }
         }
     }
 
     @Override
     public void close() {
         if (dbStore == null) {
             return;
         }
         synchronized(lock) {
             numRefs--;
             if (numRefs == 0) {
                 if (dbStore != null) {
                     dbStore.close();
                     dbStore = null;
                 }
             }
         }
     }
 
     @Override
     protected void insertTuple(Tuple tuple) throws IOException {
 
         if (dbStore == null) {
             throw new IOException("Table must be opened before inserting");
         }
 
         ContentValues cv = prepareValue(tuple);
 
         synchronized(lock) {
             long rowId = dbStore.insert(getName(), null, cv);
             if(rowId != -1) {
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Insert new record. Key: " + tuple.getKey());
                 }
                 // We store in the tuple the assigned key as a return value
                 if (autoincrement) {
                     tuple.setField(getKeyIdx(), new Long(rowId));
                 }
             } else {
                 throw new IOException("Cannot perform insert into table " + getName() + " for key " + tuple.getKey());
             }
         }
     }
 
     @Override
     protected void updateTuple(Tuple tuple) throws IOException {
         if (dbStore == null) {
             throw new IOException("Table must be opened before inserting");
         }
         Object key = tuple.getKey();
         ContentValues cv = prepareValue(tuple);
 
         StringBuffer where = new StringBuffer(getColName(getKeyIdx()));
         where.append("=\"").append(key.toString()).append("\"");
 
         synchronized(lock) {
             if(dbStore.update(getName(), cv, where.toString(), null) != -1) {
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Update record. Key: " + tuple.getKey());
                 }
             } else {
                 throw new IOException("Cannot perform update into table " + getName() + " for key " + tuple.getKey());
             }
         }
     }
 
     @Override
     protected void deleteTuple(Object key) throws IOException {
         if (dbStore == null) {
             throw new IOException("Table must be opened before inserting");
         }
         StringBuffer where = new StringBuffer(getColName(getKeyIdx()));
         where.append("=\"").append(key.toString()).append("\"");
         synchronized(lock) {
             int deleted = dbStore.delete(getName(), where.toString(), null);
             if (deleted == 0) {
                 throw new IOException("Cannot delete row from table " + getName() + " for key " + key);
             }
         }
     }
 
     @Override
     public QueryResult query(QueryFilter filter, int orderBy, boolean ascending) throws IOException {
         if (dbStore == null) {
             throw new IOException("Table must be opened before querying");
         }
         // In order to be more efficient we translate the QueryFilter into a SQL
         // statement
         String whereClause = null;
         if (filter != null) {
             whereClause = filter.getSQLWhereClause(this);
         }
 
         String order;
         if (orderBy != -1) {
             if (ascending) {
                 order = getColName(orderBy) +  " ASC";
             } else {
                 order = getColName(orderBy) +  " DESC";
             }
         } else {
             order = null;
         }
 
         synchronized(lock) {
             Cursor cursor = dbStore.query(getName(),null,whereClause,null,null,null,order);
             return new CursorQueryResult(cursor);
         }
     }
 
     @Override
     public void save() throws IOException {
         // Nothing to do here
     }
 
     @Override
     protected void resetTable() throws IOException {
         // See SQLiteDatabase documentation to understand the meaning of the "1"
         // as where clause
         synchronized(lock) {
             int deleted = dbStore.delete(getName(), "1", null);
            if (deleted == 0) {
                throw new IOException("Cannot delete all rows from table " + getName());
            }
         }
     }
 
     @Override
     protected void dropTable() throws IOException {
         if (dbStore != null) {
             throw new IOException("Table must be closed before dropping it");
         }
 
         synchronized(lock) {
             dbStore = mDatabaseHelper.getWritableDatabase();
             String sqlDrop = "DROP TABLE " + getName() + ";";
             try {
                 dbStore.execSQL(sqlDrop);
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot drop table", e);
                 throw new IOException("Cannot drop table " + getName());
             } finally {
                 dbStore.close();
             }
         }
     }
 
     protected String getCreateSQLCommand() {
 
         StringBuffer createStmt = new StringBuffer();
         createStmt.append("CREATE TABLE IF NOT EXISTS ").append(getName()).append(" (");
        
         for(int i=0;i<getArity();++i) {
             String colName = getColName(i);
              
             if (i>0) {
                 createStmt.append(",");
             }
             
             createStmt.append(colName).append(" ");
 
             if (TYPE_STRING == getColType(i)) {
                 createStmt.append(" varchar[").append(VALUE_MAX_LENGTH).append("]");
             } else if (TYPE_LONG == getColType(i)) {
                 createStmt.append(" integer");
             } else {
                 throw new IllegalStateException("Invalid table type " + getColType(i));
             }
 
             if (i == getKeyIdx()) {
                 createStmt.append(" PRIMARY KEY");
 
                 if (autoincrement) {
                     if (getColType(getKeyIdx()) != TYPE_LONG) {
                         throw new IllegalArgumentException("Autoincrement can only be applied to long keys");
                     }
                     createStmt.append(" AUTOINCREMENT");
                 }
             }
         }
         createStmt.append(");");
         return createStmt.toString();
     }
 
     private ContentValues prepareValue(Tuple tuple) {
         ContentValues cv = new ContentValues();
         for(int i=0;i<tuple.getArity();++i) {
             String v;
             boolean skipCol = false;
             if (i == getKeyIdx() && autoincrement) {
                 skipCol = true;
                 v = null;
             } else if (tuple.isUndefined(i)) {
                 skipCol = true;
                 v = null;
             } else if (tuple.getType(i) == TYPE_STRING) {
                 v = tuple.getStringField(i);
             } else if (tuple.getType(i) == TYPE_LONG) {
                 v = "" + tuple.getLongField(i);
             } else {
                 throw new IllegalStateException("Unknown field type " + tuple.getType(i));
             }
             if (!skipCol) {
                 String colName = getColName(i);
                 cv.put(colName, v);
             }
         }
         return cv;
     }
 
     protected class CursorQueryResult implements QueryResult {
         private Cursor cursor;
 
         public CursorQueryResult(Cursor cursor) {
             this.cursor = cursor;
         }
 
         public boolean hasMoreElements() {
             if (cursor.getCount() > 0) {
                 return cursor.getPosition() < (cursor.getCount() - 1);
             } else {
                 return false;
             }
         }
 
         public Tuple nextElement() {
             boolean ok = cursor.move(1);
             if (!ok) {
                 return null;
             }
             // Create a Tuple
             Tuple res = new Tuple(getColsType(), getKeyIdx());
             if (cursor.getColumnCount() != getArity()) {
                 throw new IllegalStateException("Table and cursor number of columns mismatch");
             }
             for(int i=0;i<getArity();++i) {
                 int colsType[] = getColsType();
                 if (colsType[i] == TYPE_STRING) {
                     res.setField(i, cursor.getString(i));
                 } else if (colsType[i] == TYPE_LONG) {
                     res.setField(i, cursor.getLong(i));
                 } else {
                     throw new IllegalStateException("Unknown field type " + colsType[i]);
                 }
             }
             return res;
         }
 
         public void close() {
             if (cursor != null) {
                 cursor.close();
                 cursor = null;
             }
         }
 
         public int getCount() {
             return cursor.getCount();
         }
     }
 
     /**
      * Helps on creating and upgrading the SQLite db.
      */
     private class DatabaseHelper extends SQLiteOpenHelper {
 
         public DatabaseHelper(Context context, String dbName, String tableName) {
             super(context, dbName, null, FUNAMBOL_DATABASE_VERSION);
         }
 
         public void onCreate(SQLiteDatabase db) {
         }
 
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         }
     }
 }
 
