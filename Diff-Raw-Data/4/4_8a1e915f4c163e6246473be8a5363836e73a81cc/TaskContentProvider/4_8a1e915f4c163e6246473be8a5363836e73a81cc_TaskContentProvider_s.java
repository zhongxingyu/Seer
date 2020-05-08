 package net.fibulwinter.gtd.infrastructure;
 
 import android.content.*;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 import java.util.HashMap;
 
 public class TaskContentProvider extends ContentProvider {
     private static final String TAG = "TasksContentProvider";
 
     private static final int DATABASE_VERSION = 2;
     private static final String DATABASE_NAME = "gtd_db";
     private static final String TASKS_TABLE_NAME = "tasks";
 
     public static final String AUTHORITY = "net.fibulwinter.gtd.infrastructure.TaskContentProvider";
 
     private static final UriMatcher sUriMatcher;
 
     private static final int TASKS = 1;
 
     private static HashMap<String, String> tasksProjectionMap;
 
     static {
         sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         sUriMatcher.addURI(AUTHORITY, TASKS_TABLE_NAME, TASKS);
 
         tasksProjectionMap = new HashMap<String, String>();
         tasksProjectionMap.put(TaskTableColumns.TASK_ID, TaskTableColumns.TASK_ID);
         tasksProjectionMap.put(TaskTableColumns.TITLE, TaskTableColumns.TITLE);
         tasksProjectionMap.put(TaskTableColumns.STATUS, TaskTableColumns.STATUS);
         tasksProjectionMap.put(TaskTableColumns.MASTER, TaskTableColumns.MASTER);
         tasksProjectionMap.put(TaskTableColumns.START_DATE, TaskTableColumns.START_DATE);
         tasksProjectionMap.put(TaskTableColumns.DUE_DATE, TaskTableColumns.DUE_DATE);
 
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         private final Resources resources;
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             resources = context.getResources();
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL("CREATE TABLE " + TASKS_TABLE_NAME + " ("
                     + TaskTableColumns.TASK_ID + " INTEGER PRIMARY KEY, "
                     + TaskTableColumns.TITLE + " TEXT, "
                     + TaskTableColumns.STATUS + " TEXT, "
                    + TaskTableColumns.MASTER + " INTEGER "
                    + TaskTableColumns.START_DATE + " INTEGER "
                     + TaskTableColumns.DUE_DATE + " INTEGER "
                     + ");");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                     + "");
             if (oldVersion < 2) {
                 db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TaskTableColumns.START_DATE + " INTEGER");
                 db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TaskTableColumns.DUE_DATE + " INTEGER");
             }
         }
     }
 
     private DatabaseHelper dbHelper;
 
     @Override
     public int delete(Uri uri, String where, String[] whereArgs) {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         int count;
         switch (sUriMatcher.match(uri)) {
             case TASKS:
                 count = db.delete(TASKS_TABLE_NAME, where, whereArgs);
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
         return count;
     }
 
     @Override
     public String getType(Uri uri) {
         switch (sUriMatcher.match(uri)) {
             case TASKS:
                 return TaskTableColumns.CONTENT_TYPE;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues initialValues) {
         if (sUriMatcher.match(uri) != TASKS) {
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         ContentValues values;
         if (initialValues != null) {
             values = new ContentValues(initialValues);
         } else {
             values = new ContentValues();
         }
 
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         long rowId = db.insert(TASKS_TABLE_NAME, TaskTableColumns.TASK_ID, values);
         if (rowId != -1) {
             Uri noteUri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, rowId);
             getContext().getContentResolver().notifyChange(noteUri, null);
             return noteUri;
         }
 
         throw new SQLException("Failed to insert row into " + uri);
     }
 
     @Override
     public boolean onCreate() {
         dbHelper = new DatabaseHelper(getContext());
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
         switch (sUriMatcher.match(uri)) {
             case TASKS:
                 qb.setTables(TASKS_TABLE_NAME);
                 qb.setProjectionMap(tasksProjectionMap);
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 
         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         int count;
         switch (sUriMatcher.match(uri)) {
             case TASKS:
                 count = db.update(TASKS_TABLE_NAME, values, where, whereArgs);
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
         return count;
     }
 
 
 }
