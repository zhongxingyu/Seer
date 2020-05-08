 package com.gmail.altakey.mint;
 
 import android.content.Context;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.net.Uri;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.database.sqlite.SQLiteStatement;
 import android.content.UriMatcher;
 import android.content.ContentUris;
 import android.database.Cursor;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class TaskProvider extends ContentProvider {
     public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/tasks", ProviderMap.AUTHORITY_TASK));
 
     public static final String[] PROJECTION = new String[] {
         "_id", "cookie", "task", "title", "note", "modified", "completed", "folder", "context", "priority", "star", "duedate", "duetime", "status", "folder_id", "folder_name", "folder_private", "folder_archived", "folder_ord", "context_id", "context_name"
     };
 
     public static final String DEFAULT_ORDER = "order by duedate,priority desc";
     public static final String NO_ORDER = "";
     public static final String HOTLIST_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";
     public static final String ID_FILTER = "tasks._id=?";
     public static final String ALL_FILTER = "1=1";
     public static final String MULTIPLE_TASKS_FILTER = "task in (%s)";
     public static final String DIRTY_SINCE_FILTER = "tasks.task is null or tasks.modified > ?";
 
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_COOKIE = "cookie";
     public static final String COLUMN_TASK = "task";
     public static final String COLUMN_TITLE = "title";
     public static final String COLUMN_NOTE = "note";
     public static final String COLUMN_MODIFIED = "modified";
     public static final String COLUMN_COMPLETED = "completed";
     public static final String COLUMN_FOLDER = "folder";
     public static final String COLUMN_CONTEXT = "context";
     public static final String COLUMN_PRIORITY = "priority";
     public static final String COLUMN_STAR = "star";
     public static final String COLUMN_DUEDATE = "duedate";
     public static final String COLUMN_DUETIME = "duetime";
     public static final String COLUMN_STATUS = "status";
     public static final String COLUMN_FOLDER_ID = "folder_id";
     public static final String COLUMN_FOLDER_NAME = "folder_name";
     public static final String COLUMN_FOLDER_PRIVATE = "folder_private";
     public static final String COLUMN_FOLDER_ARCHIVED = "folder_archived";
     public static final String COLUMN_FOLDER_ORD = "folder_ord";
     public static final String COLUMN_CONTEXT_ID = "context_id";
     public static final String COLUMN_CONTEXT_NAME = "context_name";
 
     public static final int COL_ID = 0;
     public static final int COL_COOKIE = 1;
     public static final int COL_TASK = 2;
     public static final int COL_TITLE = 3;
     public static final int COL_NOTE = 4;
     public static final int COL_MODIFIED = 5;
     public static final int COL_COMPLETED = 6;
     public static final int COL_FOLDER = 7;
     public static final int COL_CONTEXT = 8;
     public static final int COL_PRIORITY = 9;
     public static final int COL_STAR = 10;
     public static final int COL_DUEDATE = 11;
     public static final int COL_DUETIME = 12;
     public static final int COL_STATUS = 13;
     public static final int COL_FOLDER_ID = 14;
     public static final int COL_FOLDER_NAME = 15;
     public static final int COL_FOLDER_PRIVATE = 16;
     public static final int COL_FOLDER_ARCHIVED = 17;
     public static final int COL_FOLDER_ORD = 18;
     public static final int COL_CONTEXT_ID = 19;
     public static final int COL_CONTEXT_NAME = 20;
 
     private static final String TASK_QUERY = "SELECT tasks._id,tasks.cookie,task,title,note,modified,completed,priority,star,duedate,duetime,status,folder AS folder_id,folders.name AS folder_name,folders.private AS folder_private,folders.archived AS folder_archived,folders.ord AS folder_ord,context AS context_id,contexts.name AS context_name FROM tasks LEFT JOIN folders USING (folder) LEFT JOIN contexts USING (context) WHERE %s %s";
 
    private static final String TASK_INSERT_QUERY = "INSERT INTO tasks (cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 
    private static final String TASK_REPLACE_QUERY = "REPLACE INTO tasks (id,cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 
     private static final String TASK_UPDATE_QUERY = "UPDATE tasks set cookie=?,task=?,title=?,note=?,modified=?,completed=?,folder=?,context=?,priority=?,star=?,duedate=?,duetime=?,status=? %s";
 
     private static final String TASK_DELETE_QUERY = "DELETE FROM tasks %s";
 
     private SQLiteOpenHelper mHelper;
 
     @Override
     public String getType(Uri uri) {
         return new ProviderMap(uri).getContentType();
     }
 
     @Override
     public boolean onCreate() {
         mHelper = new Schema.OpenHelper(getContext());
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         final SQLiteDatabase db = mHelper.getReadableDatabase();
 
         switch (new ProviderMap(uri).getResourceType()) {
         case ProviderMap.TASKS:
             return db.rawQuery(String.format(TASK_QUERY, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
         case ProviderMap.TASKS_ID:
             return db.rawQuery(String.format(TASK_QUERY, ID_FILTER, NO_ORDER), new String[] { String.valueOf(ContentUris.parseId(uri)) });
         default:
             return null;
         }
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
         final int resourceType = new ProviderMap(uri).getResourceType();
 
         if (resourceType == ProviderMap.TASKS) {
             final SQLiteStatement stmt = db.compileStatement(TASK_INSERT_QUERY);
             stmt.bindString(1, (String)values.get("cookie"));
             stmt.bindString(2, (String)values.get("task"));
             stmt.bindString(3, (String)values.get("title"));
             stmt.bindString(4, (String)values.get("note"));
             stmt.bindString(5, (String)values.get("modified"));
             stmt.bindString(6, (String)values.get("completed"));
             stmt.bindString(7, (String)values.get("folder"));
             stmt.bindString(8, (String)values.get("cnotext"));
             stmt.bindString(9, (String)values.get("priority"));
             stmt.bindString(10, (String)values.get("star"));
             stmt.bindString(11, (String)values.get("duedate"));
             stmt.bindString(12, (String)values.get("duetime"));
             stmt.bindString(13, (String)values.get("status"));
             try {
                 return ContentUris.withAppendedId(uri, stmt.executeInsert());
             } finally {
                 stmt.close();
             }
         } else if (resourceType == ProviderMap.TASKS_ID) {
             final SQLiteStatement stmt = db.compileStatement(TASK_REPLACE_QUERY);
             stmt.bindString(1, (String)values.get("_id"));
             stmt.bindString(2, (String)values.get("cookie"));
             stmt.bindString(3, (String)values.get("task"));
             stmt.bindString(4, (String)values.get("title"));
             stmt.bindString(5, (String)values.get("note"));
             stmt.bindString(6, (String)values.get("modified"));
             stmt.bindString(7, (String)values.get("completed"));
             stmt.bindString(8, (String)values.get("folder"));
             stmt.bindString(9, (String)values.get("cnotext"));
             stmt.bindString(10, (String)values.get("priority"));
             stmt.bindString(11, (String)values.get("star"));
             stmt.bindString(12, (String)values.get("duedate"));
             stmt.bindString(13, (String)values.get("duetime"));
             stmt.bindString(14, (String)values.get("status"));
             try {
                 stmt.executeInsert();
                 return uri;
             } finally {
                 stmt.close();
             }
         } else {
             return null;
         }
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
         final int resourceType = new ProviderMap(uri).getResourceType();
 
         if (resourceType == ProviderMap.TASKS) {
             if (MULTIPLE_TASKS_FILTER.equals(selection)) {
                 selection = new FilterExpander(selection, selectionArgs).expand();
             }
 
             final SQLiteStatement stmt = db.compileStatement(String.format(TASK_UPDATE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));
             stmt.bindString(1, (String)values.get("cookie"));
             stmt.bindString(2, (String)values.get("task"));
             stmt.bindString(3, (String)values.get("title"));
             stmt.bindString(4, (String)values.get("note"));
             stmt.bindString(5, (String)values.get("modified"));
             stmt.bindString(6, (String)values.get("completed"));
             stmt.bindString(7, (String)values.get("folder"));
             stmt.bindString(8, (String)values.get("cnotext"));
             stmt.bindString(9, (String)values.get("priority"));
             stmt.bindString(10, (String)values.get("star"));
             stmt.bindString(11, (String)values.get("duedate"));
             stmt.bindString(12, (String)values.get("duetime"));
             stmt.bindString(13, (String)values.get("status"));
 
             int offset = 14;
             for (final String arg: selectionArgs) {
                 stmt.bindString(offset++, arg);
             }
             try {
                 return stmt.executeUpdateDelete();
             } finally {
                 stmt.close();
             }
         } else {
             return 0;
         }
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
 
         switch (new ProviderMap(uri).getResourceType()) {
         case ProviderMap.TASKS:
             if (MULTIPLE_TASKS_FILTER.equals(selection)) {
                 selection = new FilterExpander(selection, selectionArgs).expand();
             }
 
             final SQLiteStatement stmt =
                 db.compileStatement(String.format(TASK_DELETE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));
 
             int offset = 1;
             for (final String arg: selectionArgs) {
                 stmt.bindString(offset++, arg);
             }
             try {
                 return stmt.executeUpdateDelete();
             } finally {
                 stmt.close();
             }
         default:
             return 0;
         }
     }
 }
