 package com.hasgeek.funnel.misc;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 
 
 public class DataProvider extends ContentProvider {
 
     private DBManager mDBM;
     private Context mContext;
 
     public static final String PROVIDER_NAME = "com.hasgeek.funnel.data";
     public static final String SQLITE_INSERT_OR_REPLACE_MODE = "__SQLITE_INSERT_OR_REPLACE_MODE__";
 
     public static final Uri SESSION_URI = Uri.parse("content://" + PROVIDER_NAME + "/sessions");
     public static final Uri ROOM_URI = Uri.parse("content://" + PROVIDER_NAME + "/rooms");
     public static final Uri VENUE_URI = Uri.parse("content://" + PROVIDER_NAME + "/venues");
 
     private static final UriMatcher uriMatcher;
     private static final int SESSIONS_MATCH = 4201;
     private static final int SESSION_MATCH = 4202;
     private static final int ROOMS_MATCH = 4204;
     private static final int ROOM_MATCH = 4205;
     private static final int VENUES_MATCH = 4241;
     private static final int VENUE_MATCH = 4242;
 
 
     static {
         uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "sessions", SESSIONS_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "session/#", SESSION_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "rooms", ROOMS_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "room/#", ROOM_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "venues", VENUES_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "venue/#", VENUE_MATCH);
     }
 
 
     @Override
     public boolean onCreate() {
         mContext = getContext();
         mDBM = DBManager.getInstance(mContext);
         return true;
     }
 
 
     @Override
     public String getType(Uri uri) {
         switch (uriMatcher.match(uri)) {
             case SESSIONS_MATCH:
                 return "vnd.android.cursor.dir/vnd.com.hasgeek.funnel.sessions";
             case SESSION_MATCH:
                 return "vnd.android.cursor.item/vnd.com.hasgeek.funnel.sessions";
             case ROOMS_MATCH:
                 return "vnd.android.cursor.dir/vnd.com.hasgeek.funnel.rooms";
             case ROOM_MATCH:
                 return "vnd.android.cursor.item/vnd.com.hasgeek.funnel.rooms";
             case VENUES_MATCH:
                 return "vnd.android.cursor.dir/vnd.com.hasgeek.funnel.venues";
             case VENUE_MATCH:
                 return "vnd.android.cursor.item/vnd.com.hasgeek.funnel.venues";
             default:
                 throw new RuntimeException("Unsupported URI: " + uri);
         }
     }
 
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
 
         switch (uriMatcher.match(uri)) {
             case SESSIONS_MATCH:
                 sqlBuilder.setTables(DBManager.SESSIONS_TABLE);
                 break;
 
             case SESSION_MATCH:
                 sqlBuilder.setTables(DBManager.SESSIONS_TABLE);
                 sqlBuilder.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
                 break;
 
             case VENUES_MATCH:
                 sqlBuilder.setTables(DBManager.VENUES_TABLE);
                 break;
 
             case VENUE_MATCH:
                 sqlBuilder.setTables(DBManager.VENUES_TABLE);
                 sqlBuilder.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
                 break;
 
             case ROOMS_MATCH:
                 sqlBuilder.setTables(DBManager.ROOMS_TABLE);
                 break;
 
             case ROOM_MATCH:
                 sqlBuilder.setTables(DBManager.ROOMS_TABLE);
                 sqlBuilder.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
                 break;
 
             default:
                 throw new RuntimeException("Incorrect URI matched!");
         }
 
         Cursor c = sqlBuilder.query(mDBM.getWritableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
         c.setNotificationUri(mContext.getContentResolver(), uri);
         return c;
     }
 
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         SQLiteDatabase db = mDBM.getWritableDatabase();
         String id = uri.getPathSegments().get(1);
         int count;
 
         switch (uriMatcher.match(uri)) {
             case SESSIONS_MATCH:
                 count = db.delete(DBManager.SESSIONS_TABLE, selection, selectionArgs);
                 break;
 
             case SESSION_MATCH:
                 count = db.delete(
                         DBManager.SESSIONS_TABLE,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             case ROOMS_MATCH:
                 count = db.delete(DBManager.ROOMS_TABLE, selection, selectionArgs);
                 break;
 
             case ROOM_MATCH:
                 count = db.delete(
                         DBManager.ROOMS_TABLE,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             case VENUES_MATCH:
                 count = db.delete(DBManager.VENUES_TABLE, selection, selectionArgs);
                 break;
 
             case VENUE_MATCH:
                 count = db.delete(
                         DBManager.VENUES_TABLE,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         mContext.getContentResolver().notifyChange(uri, null);
         return count;
     }
 
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         SQLiteDatabase db = mDBM.getWritableDatabase();
         ContentValues v = new ContentValues(values);
 
         boolean replace = false;
         if (values.containsKey(SQLITE_INSERT_OR_REPLACE_MODE)) {
             replace = values.getAsBoolean(SQLITE_INSERT_OR_REPLACE_MODE);
             v.remove(SQLITE_INSERT_OR_REPLACE_MODE);
         }
 
         if (uri.getPathSegments().get(0).equals("sessions")) {
             try {
                 long row;
                 if (replace) {
                     row = db.replaceOrThrow(DBManager.SESSIONS_TABLE, null, v);
                 } else {
                     row = db.insertOrThrow(DBManager.SESSIONS_TABLE, null, v);
                 }
                 if (row > 0) {
                     Uri u = ContentUris.withAppendedId(SESSION_URI, row);
                     mContext.getContentResolver().notifyChange(u, null);
                     return u;
                 }
             } catch (SQLiteConstraintException e) {
                 e.printStackTrace();
                 throw new RuntimeException("Insertion failed.");
             }
 
         } else if (uri.getPathSegments().get(0).equals("rooms")) {
             try {
                 long row;
                 if (replace) {
                     row = db.replaceOrThrow(DBManager.ROOMS_TABLE, null, v);
                 } else {
                     row = db.insertOrThrow(DBManager.ROOMS_TABLE, null, v);
                 }
                 if (row > 0) {
                     Uri u = ContentUris.withAppendedId(ROOM_URI, row);
                     mContext.getContentResolver().notifyChange(u, null);
                     return u;
                 }
             } catch (SQLiteConstraintException e) {
                 e.printStackTrace();
                 throw new RuntimeException("Insertion failed.");
             }
 
         } else if (uri.getPathSegments().get(0).equals("venues")) {
             try {
                 long row;
                 if (replace) {
                     row = db.replaceOrThrow(DBManager.VENUES_TABLE, null, v);
                 } else {
                     row = db.insertOrThrow(DBManager.VENUES_TABLE, null, v);
                 }
                 if (row > 0) {
                     Uri u = ContentUris.withAppendedId(VENUE_URI, row);
                     mContext.getContentResolver().notifyChange(u, null);
                     return u;
                 }
             } catch (SQLiteConstraintException e) {
                 e.printStackTrace();
                 throw new RuntimeException("Insertion failed.");
             }
 
         } else {
             throw new RuntimeException("Insert is broken.");
         }
 
         return null;
     }
 
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         SQLiteDatabase db = mDBM.getWritableDatabase();
        //todo Fix usages of update to use singular match as well.
        // Uses only multiple matching at the moment like SESSIONS_MATCH and VENUES_MATCH.
        String id = uri.getPathSegments().get(0);
         int count;
 
         switch (uriMatcher.match(uri)) {
             case SESSIONS_MATCH:
                 count = db.update(DBManager.SESSIONS_TABLE, values, selection, selectionArgs);
                 break;
             case SESSION_MATCH:
                 count = db.update(DBManager.SESSIONS_TABLE,
                         values,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             case ROOMS_MATCH:
                 count = db.update(DBManager.ROOMS_TABLE, values, selection, selectionArgs);
                 break;
             case ROOM_MATCH:
                 count = db.update(DBManager.ROOMS_TABLE,
                         values,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             case VENUES_MATCH:
                 count = db.update(DBManager.VENUES_TABLE, values, selection, selectionArgs);
                 break;
             case VENUE_MATCH:
                 count = db.update(DBManager.VENUES_TABLE,
                         values,
                         BaseColumns._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                         selectionArgs
                 );
                 break;
 
             default:
                 throw new RuntimeException("Unknown URI: " + uri);
         }
 
         mContext.getContentResolver().notifyChange(uri, null);
         return count;
     }
 }
