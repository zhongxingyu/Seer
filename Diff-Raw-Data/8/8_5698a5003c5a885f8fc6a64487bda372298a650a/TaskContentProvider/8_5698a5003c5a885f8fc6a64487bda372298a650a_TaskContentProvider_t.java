 package com.indax.taskmanager.providers;
 
 import java.util.HashMap;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 
 import com.indax.taskmanager.models.ExecuteLog.ExecuteLogs;
 import com.indax.taskmanager.models.Task.Tasks;
 import com.indax.taskmanager.utils.Preferences;
 
 public class TaskContentProvider extends ContentProvider {
 
 	private static final String DATABASE_NAME = "taskmanager.db";
 	private static final int DATABASE_VERSION = 2;
 	private static final String TASKS_TABLE_NAME = "tasks";
 	private static final String EXECUTELOG_TABLE_NAME = "executelogs";
 	private static final UriMatcher URI_MATCHER;
 	private static final int TASKS = 1;
 	private static final int TASKS_ID = 2;
 	private static final int TASKS_GUID = 3;
 	private static final int EXECUTELOGS = 4;
 	private static HashMap<String, String> tasksProjectionMap;
 	private static HashMap<String, String> executelogsProjectionMap;
 
 	public static final String AUTHORITY = "com.indax.taskmanager.providers.TaskContentProvider";
 
	private static class DatabaseHelper extends SQLiteOpenHelper {		
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("CREATE TABLE " + TASKS_TABLE_NAME + " (" 
 					+ Tasks.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ Tasks.GUID + " INTEGER, "
 					+ Tasks.NAME + " VARCHAR(256), " + Tasks.TYPE
 					+ " VARCHAR(128), " + Tasks.FINISH + " INTEGER, "
 					+ Tasks.REMARK + " TEXT" + " );");
 			db.execSQL("CREATE TABLE " + EXECUTELOG_TABLE_NAME + " ("
 					+ ExecuteLogs.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ ExecuteLogs.TASK + " INTEGER, "
 					+ ExecuteLogs.LOG_TIME + " INTEGER, "
 					+ ExecuteLogs.REMARK + " TEXT" + " );");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + EXECUTELOG_TABLE_NAME);
 			onCreate(db);
 		}
 
 	} // DatabaseHelper
 	
 	private DatabaseHelper db_helper;
 
 	@Override
 	public boolean onCreate() {
 		db_helper = new DatabaseHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		queryBuilder.setTables(TASKS_TABLE_NAME);
 		queryBuilder.setProjectionMap(tasksProjectionMap);
 		
 		switch ( URI_MATCHER.match(uri) ) {
 		case TASKS:
 			break;
 		case TASKS_ID:
 			selection = selection + Tasks.ID + " = " + uri.getLastPathSegment();
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
 		SQLiteDatabase db = db_helper.getReadableDatabase();
 		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 		return cursor;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch ( URI_MATCHER.match(uri) ) {
 		case TASKS:
 			return Tasks.CONTENT_TYPE;
 		default:
 			throw new IllegalArgumentException("Unkown URI " + uri);
 		}
 	} // getType
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		SQLiteDatabase db = db_helper.getWritableDatabase();		
 		long row_id;
 		
 		switch ( URI_MATCHER.match(uri) ) {
 		case TASKS:
 			row_id = db.insert(TASKS_TABLE_NAME, Tasks.NAME, values);
 			if ( row_id > 0 ) {
 				Uri task_uri = ContentUris.withAppendedId(Tasks.CONTENT_URI, row_id);
 				getContext().getContentResolver().notifyChange(task_uri, null);
 				return task_uri;
 			}			
 			break;
 		case EXECUTELOGS:
 			row_id = db.insert(EXECUTELOG_TABLE_NAME, ExecuteLogs.REMARK, values);
 			if ( row_id > 0 ) {
 				Uri log_uri = ContentUris.withAppendedId(ExecuteLogs.CONTENT_URI, row_id);
 				getContext().getContentResolver().notifyChange(log_uri, null);
 				return log_uri;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}		
 		
 		throw new SQLException("Failed to insert row into " + uri);
 	} // insert
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		SQLiteDatabase db = db_helper.getWritableDatabase();
 		switch ( URI_MATCHER.match(uri) ) {
 		case TASKS:
 			break;
 		case TASKS_ID:
 			selection = selection + Tasks.ID + " = " + uri.getLastPathSegment();
 			break;
 		case TASKS_GUID:
 			selection = Tasks.GUID + " = " + uri.getLastPathSegment();
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
 		int count = db.delete(TASKS_TABLE_NAME, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	} // delete
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		SQLiteDatabase db = db_helper.getWritableDatabase();
 		int count = 0;
 		switch (URI_MATCHER.match(uri)) {
 		case TASKS:
 			count = db.update(TASKS_TABLE_NAME, values, selection, selectionArgs);
 			break;
 		case TASKS_GUID:
 			count = db.update(TASKS_TABLE_NAME, values, Tasks.GUID + " = " + uri.getLastPathSegment(), selectionArgs);
 			break;
 
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	static {
 		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 		URI_MATCHER.addURI(AUTHORITY, TASKS_TABLE_NAME, TASKS);
 		URI_MATCHER.addURI(AUTHORITY, TASKS_TABLE_NAME + "/#", TASKS_ID);
 		URI_MATCHER.addURI(AUTHORITY, TASKS_TABLE_NAME + "/guid/#", TASKS_GUID);
 		URI_MATCHER.addURI(AUTHORITY, EXECUTELOG_TABLE_NAME, EXECUTELOGS);
 		
 		tasksProjectionMap = new HashMap<String, String>();
 		tasksProjectionMap.put(Tasks.ID, Tasks.ID);
 		tasksProjectionMap.put(Tasks.GUID, Tasks.GUID);
 		tasksProjectionMap.put(Tasks.NAME, Tasks.NAME);
 		tasksProjectionMap.put(Tasks.TYPE, Tasks.TYPE);
 		tasksProjectionMap.put(Tasks.FINISH, Tasks.FINISH);
 		tasksProjectionMap.put(Tasks.REMARK, Tasks.REMARK);
 		
 		executelogsProjectionMap = new HashMap<String, String>();
 		executelogsProjectionMap.put(ExecuteLogs.ID, ExecuteLogs.ID);
 		executelogsProjectionMap.put(ExecuteLogs.TASK, ExecuteLogs.TASK);
 		executelogsProjectionMap.put(ExecuteLogs.LOG_TIME, ExecuteLogs.LOG_TIME);
 		executelogsProjectionMap.put(ExecuteLogs.REMARK, ExecuteLogs.REMARK);
 	}
 }
