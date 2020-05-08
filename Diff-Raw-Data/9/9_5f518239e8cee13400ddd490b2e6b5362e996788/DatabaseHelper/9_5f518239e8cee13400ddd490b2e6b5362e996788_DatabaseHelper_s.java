 
 package com.andrewma.vision.database;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.andrewma.vision.database.core.DbColumn;
 import com.andrewma.vision.database.core.DbTable;
 import com.andrewma.vision.database.core.annotations.Column;
 import com.andrewma.vision.database.core.annotations.PrimaryKey;
 import com.andrewma.vision.database.core.annotations.Table;
 import com.andrewma.vision.models.ArchivedGlasses;
 import com.andrewma.vision.models.Batch;
 import com.andrewma.vision.models.Glasses;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final String TAG = "SQLite";
 
     private static final String DATABASE_NAME = "vision.db";
     private static final int DATABASE_VERSION = 1;
 
     private static DatabaseHelper instance = null;
     private final Map<Class<?>, DbTable> tables = new HashMap<Class<?>, DbTable>();
     private final SQLiteDatabase db;
 
     public static DatabaseHelper getInstance(Context context) {
         if (instance == null) {
             instance = new DatabaseHelper(context.getApplicationContext());
         }
         return instance;
     }
 
     private DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         registerModel(Glasses.class);
         registerModel(Batch.class);
         registerModel(ArchivedGlasses.class);
         db = getWritableDatabase();
     }
 
     public void close() {
         db.close();
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         for (DbTable table : tables.values()) {
             final String createTableSql = table.generateCreateTableSql();
             Log.v(TAG, createTableSql);
             db.execSQL(createTableSql);
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     }
 
     private void registerModel(Class<?> clazz) {
         if (clazz.isAnnotationPresent(Table.class)) {
             final DbTable table = new DbTable(clazz, (Table) clazz.getAnnotation(Table.class));
 
             final Field[] fields = clazz.getFields();
             for (Field field : fields) {
                 if (field.isAnnotationPresent(PrimaryKey.class)) {
                     final PrimaryKey annotation = (PrimaryKey) field
                             .getAnnotation(PrimaryKey.class);
                     table.setPrimarykey(field, annotation);
                 } else if (field.isAnnotationPresent(Column.class)) {
                     final Column annotation = (Column) field.getAnnotation(Column.class);
                     table.columns().add(new DbColumn(field, annotation));
                 }
             }
 
             tables.put(clazz, table);
         }
     }
 
     private DbTable lookupModelTable(Class<?> modelClass) {
         if (!tables.containsKey(modelClass)) {
             Log.e(TAG, "Could not insert object of class " + modelClass);
             return null;
         } else {
             return tables.get(modelClass);
         }
     }
 
     /**
      * Insert an object into the database
      * 
      * @param model
      * @return returns -1 if there was an error
      */
     public long insert(Object model) {
         long rowid = -1;
         final DbTable table = lookupModelTable(model.getClass());
         if (table == null) {
             return rowid;
         }
 
         final ContentValues contentValues = table.getContentValues(model);
         rowid = db.insert(table.getTableName(), null, contentValues);
         return rowid;
     }
 
     /**
      * Update an object
      * 
      * @param model
      * @return
      */
     public int update(Object model) {
         final DbTable table = lookupModelTable(model.getClass());
         if (table == null) {
             return 0;
         }
 
         final ContentValues contentValues = table.getContentValues(model);
         String whereClause;
         try {
             whereClause = table.getPrimaryKeyName() + "="
                     + table.getPrimaryKeyField().getInt(model);
         } catch (Exception e) {
             Log.e(TAG, "Failed to get primary key from model for table "
                     + table.getTableName());
             e.printStackTrace();
             return 0;
         }
         return db.update(table.getTableName(), contentValues, whereClause, null);
     }
 
     /**
      * Get all rows from the table
      * 
      * @param modelClass
      * @return
      */
     public <E> List<E> getAll(Class<?> modelClass) {
         return getAll(modelClass, Integer.MAX_VALUE);
     }
 
     /**
      * Get all rows from the table (with a max number)
      * 
      * @param modelClass
      * @param max will not get more rows that this
      * @return
      */
     public <E> List<E> getAll(Class<?> modelClass, int max) {
         final DbTable table = lookupModelTable(modelClass);
         if (table == null) {
             Log.w(TAG, table + " is not a registered model");
             return null;
         }
 
         final List<E> list = new ArrayList<E>();
         int count = 0;
         final Cursor cursor = db.query(table.getTableName(), null, null, null, null, null, null);
         while (cursor.moveToNext()) {
             final E row = cursorToObject(table, cursor);
             if (row != null) {
                 list.add(row);
                 count++;
 
                 // stop traversing cursor if we have enough in the list
                 if (count == max) {
                     break;
                 }
             }
         }
         cursor.close();
         return list;
     }
 
     public <E> List<E> executeSql(Class<?> modelClass, String sqlStmt) {
         Log.v(TAG, "Executing SQL: " + sqlStmt);
         final DbTable table = lookupModelTable(modelClass);
         if (table == null) {
             return null;
         }
 
         final List<E> list = new ArrayList<E>();
         final Cursor cursor = db.rawQuery(sqlStmt, null);
         while (cursor.moveToNext()) {
             final E row = cursorToObject(table, cursor);
             if (row != null) {
                 list.add(row);
             }
         }
         cursor.close();
         return list;
     }
 
     public <E> E get(Class<?> modelClass, int id) {
         final DbTable table = lookupModelTable(modelClass);
         if (table == null) {
             return null;
         }
 
         E result = null;
         final String selection = table.getPrimaryKeyName() + " = " + id;
         final Cursor cursor = db.query(table.getTableName(), null, selection,
                 null, null, null, null);
         if (cursor.moveToNext()) {
             result = cursorToObject(table, cursor);
         }
         cursor.close();
         return result;
     }
 
     /**
      * Gets the <E> objects from the current cursor position
      * 
      * @param table
      * @param cursor
      * @return
      */
     @SuppressWarnings("unchecked")
     private <E> E cursorToObject(DbTable table, Cursor cursor) {
         E cursorRow = null;
         try {
             cursorRow = (E) table.getModelClass().newInstance();
 
             final int primaryKeyColumnIndex = cursor.getColumnIndex(table
                     .getPrimaryKeyName());
             table.getPrimaryKeyField().setInt(cursorRow,
                     cursor.getInt(primaryKeyColumnIndex));
 
             for (DbColumn column : table.columns()) {
                 cursorColumnToObjectField(cursor, cursorRow, column);
             }
         } catch (Exception e) {
             Log.e(TAG, "cursorToObject failed for " + table.getModelClass().getName());
             e.printStackTrace();
         }
         return cursorRow;
     }
 
     /**
      * For the given {@link DbColumn}, read and convert the appropriate data
      * from the cursor
      * 
      * @param cursor
      * @param cursorRow
      * @param column
      * @throws IllegalAccessException
      */
     private <E> void cursorColumnToObjectField(Cursor cursor, E cursorRow, DbColumn column)
             throws IllegalAccessException {
         final Field columnField = column.columnField;
         final String columnName = column.getColumnName();
 
         final int cursorColumnindex = cursor.getColumnIndex(columnName);
         final Class<?> columnFieldClass = columnField.getType();
         switch (column.columnAnnotation.dataType()) {
             case INTEGER:
                 final int value = cursor.getInt(cursorColumnindex);
                 if (boolean.class.equals(columnFieldClass)) {
                     columnField.setBoolean(cursorRow, (value == 1));
                 } else if (Date.class.equals(columnFieldClass)) {
                     columnField.set(cursorRow, new Date((long) value * 1000));
                 } else {
                     columnField.setInt(cursorRow, value);
                 }
                 break;
             case REAL:
                 columnField.setFloat(cursorRow,
                         cursor.getFloat(cursorColumnindex));
                 break;
             case TEXT:
                 final String cursorString = cursor.getString(cursorColumnindex);
                 if (char.class.equals(columnFieldClass)) {
                     boolean nullOrEmpty = (cursorString == null || cursorString.equals(""));
                     char charVal = (nullOrEmpty ? ' ' : cursorString.charAt(0));
                     columnField.set(cursorRow, charVal);
                 } else {
                     columnField.set(cursorRow, cursorString);
                 }
                 break;
         }
     }
 
     /**
      * Deletes a row from the database
      * 
      * @param modelClass
      * @param id
      */
     public void delete(Class<?> modelClass, int id) {
         final DbTable table = lookupModelTable(modelClass);
         if (table == null) {
             return;
         }
 
         final String where = table.getPrimaryKeyName() + "=" + id;
         db.delete(table.getTableName(), where, null);
     }
 
     /**
      * Delete the entire database from disk
      * 
      * @param context
      * @return
      */
     public static boolean deleteDatabase(Context context) {
        if (instance == null) {
             instance.close();
         }
 
         if (context.deleteDatabase(DATABASE_NAME)) {
             Log.i(TAG, "Deleting database");
            for (DatabaseDeleteEvent e : databaseDeleteEvents) {
                e.onDelete();
             }
             return true;
         } else {
             Log.e(TAG, "Tried to delete database but was unable to");
             return false;
         }
     }
 
     public static void addDatabaseDeleteListener(DatabaseDeleteEvent event) {
         databaseDeleteEvents.add(event);
     }
 
     private static final List<DatabaseDeleteEvent> databaseDeleteEvents = new LinkedList<DatabaseDeleteEvent>();
 
     public interface DatabaseDeleteEvent {
         public void onDelete();
     }
 }
