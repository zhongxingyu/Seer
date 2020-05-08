 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.io;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 
 public class Database {
     public static class CacheReader {
         public static class WhereFactory {
             // 1 degree ~= 111km
             public static final double DEGREES_DELTA = 0.08;
 
             public String getWhere(Location location) {
                 if (location == null)
                     return null;
                 double latitude = location.getLatitude();
                 double longitude = location.getLongitude();
 
                 double latLow = latitude - WhereFactory.DEGREES_DELTA;
                 double latHigh = latitude + WhereFactory.DEGREES_DELTA;
                 double lat_radians = Math.toRadians(latitude);
                 double cos_lat = Math.cos(lat_radians);
                 double lonLow = Math.max(-180, longitude - WhereFactory.DEGREES_DELTA / cos_lat);
                 double lonHigh = Math.min(180, longitude + WhereFactory.DEGREES_DELTA / cos_lat);
                 return "Latitude > " + latLow + " AND Latitude < " + latHigh + " AND Longitude > "
                         + lonLow + " AND Longitude < " + lonHigh;
             }
         }
 
         public static final String SQL_QUERY_LIMIT = "1000";
 
         public static CacheReader create(SQLiteWrapper sqliteWrapper) {
             final WhereFactory whereFactory = new WhereFactory();
             return new CacheReader(sqliteWrapper, whereFactory);
         }
 
         private Cursor mCursor;
         private final SQLiteWrapper mSqliteWrapper;
 
         private final WhereFactory mWhereFactory;
 
         public CacheReader(SQLiteWrapper sqliteWrapper, WhereFactory whereFactory) {
             mSqliteWrapper = sqliteWrapper;
             mWhereFactory = whereFactory;
         }
 
         public void close() {
             mCursor.close();
         }
 
         public String getCache() {
             String name = mCursor.getString(3);
             String id = mCursor.getString(2);
             if (name.length() > 0 && id.length() > 0) {
                 name = ": " + name;
             }
 
             return mCursor.getString(0) + ", " + mCursor.getString(1) + " (" + id + name + ")";
         }
 
         public int getTotalCount() {
             Cursor cursor = mSqliteWrapper.rawQuery(SQL_COUNT_CACHES, null);
             cursor.moveToFirst();
             int count = cursor.getInt(0);
             cursor.close();
             return count;
         }
 
         public boolean moveToNext() {
             return mCursor.moveToNext();
         }
 
         public boolean open(Location location) {
             String where = mWhereFactory.getWhere(location);
 
             mCursor = mSqliteWrapper.query(TBL_CACHES, READER_COLUMNS, where, null, null, null,
                     null, SQL_QUERY_LIMIT);
             final boolean result = mCursor.moveToFirst();
             if (!result)
                 mCursor.close();
             return result;
         }
     }
 
     public static class CacheWriter {
         private final SQLiteWrapper mSqlite;
 
         public CacheWriter(SQLiteWrapper sqlite) {
             mSqlite = sqlite;
         }
 
         public void clearAllImportedCaches() {
            mSqlite.execSQL(Database.SQL_DELETE_ALL_IMPORTED_CACHES, new Object[] {});
 
         }
 
         public void clearCaches(String source) {
             mSqlite.execSQL(SQL_CLEAR_CACHES, new Object[] {
                 source
             });
         }
 
         public void deleteCache(CharSequence id) {
             mSqlite.execSQL(Database.SQL_DELETE_CACHE, new Object[] {
                 id
             });
         }
 
         public void insertAndUpdateCache(CharSequence id, CharSequence name, double latitude,
                 double longitude, String source) {
             try {
                 insertCache(id, name, latitude, longitude, source);
             } catch (final SQLiteConstraintException e) {
                 // TODO: What if these queries have errors?
                 deleteCache(id);
                 insertCache(id, name, latitude, longitude, source);
             }
         }
 
         private void insertCache(CharSequence id, CharSequence name, double latitude,
                 double longitude, String source) {
             mSqlite.execSQL(Database.SQL_INSERT_CACHE, new Object[] {
                     id, name, new Double(latitude), new Double(longitude), source
             });
         }
 
         public void startWriting() {
             mSqlite.beginTransaction();
         }
 
         public void stopWriting() {
             // TODO: abort if no writes--otherwise sqlite is unhappy.
             mSqlite.setTransactionSuccessful();
             mSqlite.endTransaction();
         }
     }
 
     public static class GeoBeagleSqliteOpenHelper extends SQLiteOpenHelper {
         private final OpenHelperDelegate mOpenHelperDelegate;
 
         public GeoBeagleSqliteOpenHelper(Context context, OpenHelperDelegate openHelperDelegate) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             mOpenHelperDelegate = openHelperDelegate;
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             mOpenHelperDelegate.onCreate(db);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             mOpenHelperDelegate.onUpgrade(db, oldVersion, newVersion);
         }
     }
 
     public static class OpenHelperDelegate {
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(SQL_CREATE_CACHE_TABLE);
             db.execSQL(SQL_CREATE_IDX_LATITUDE);
             db.execSQL(SQL_CREATE_IDX_LONGITUDE);
             db.execSQL(SQL_CREATE_IDX_SOURCE);
         }
 
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 9) {
                 db.execSQL(SQL_DROP_CACHE_TABLE);
                 db.execSQL(SQL_CREATE_CACHE_TABLE);
                db.execSQL(SQL_CREATE_IDX_LATITUDE);
                db.execSQL(SQL_CREATE_IDX_LONGITUDE);
                db.execSQL(SQL_CREATE_IDX_SOURCE);
             }
         }
     }
 
     public static class SQLiteWrapper {
         private SQLiteDatabase mSQLiteDatabase;
 
         public void beginTransaction() {
             mSQLiteDatabase.beginTransaction();
         }
 
         public void close() {
             mSQLiteDatabase.close();
         }
 
         public void endTransaction() {
             mSQLiteDatabase.endTransaction();
         }
 
         public void execSQL(String sql) {
             mSQLiteDatabase.execSQL(sql);
         }
 
         public void execSQL(String sql, Object[] bindArgs) {
             mSQLiteDatabase.execSQL(sql, bindArgs);
         }
 
         public void openReadableDatabase(Database database) {
             mSQLiteDatabase = database.getReadableDatabase();
         }
 
         public void openWritableDatabase(Database database) {
             mSQLiteDatabase = database.getWritableDatabase();
         }
 
         public Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
             return mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy,
                     orderBy, having, limit);
         }
 
         /**
          * @param sql
          * @param selectionArgs
          * @return
          */
         public Cursor rawQuery(String sql, String[] selectionArgs) {
             return mSQLiteDatabase.rawQuery(sql, selectionArgs);
         }
 
         public void setTransactionSuccessful() {
             mSQLiteDatabase.setTransactionSuccessful();
         }
     }
 
     /**
      * SCHEMA LOG:
      * 
      * <pre>
      * version 6
      * CREATE TABLE IF NOT EXISTS CACHES (Id VARCHAR PRIMARY KEY,
      *          Description VARCHAR Latitude DOUBLE, Longitude DOUBLE, Source
      *          VARCHAR)
      *          
      * version 7
      * CREATE TABLE IF NOT EXISTS CACHES (Id VARCHAR PRIMARY
      *          KEY, Description VARCHAR Latitude DOUBLE, Longitude DOUBLE,
      *          Source VARCHAR)
      * CREATE INDEX IDX_LATITUDE on CACHES (Latitude)
      * CREATE INDEX IDX_LONGITUDE on CACHES (Longitude)
      * CREATE INDEX IDX_SOURCE on CACHES (Source)
      * 
      * version 8
     * same as version 7 but rebuilds everything because a released version mistakenly puts 
     * *intent* into imported caches.
     * 
     * version 9
     * fixes bug where INDEX wasn't being created on upgrade.
     * 
     * version 10
     * CREATE TABLE IF NOT EXISTS CACHES (Id VARCHAR PRIMARY
     *          KEY, Description VARCHAR Latitude DOUBLE, Longitude DOUBLE,
     *          Source VARCHAR)
     * CREATE TABLE IF NOT EXISTS GPX (Name VARCHAR PRIMARY KEY, Time DATETIME)
     * CREATE INDEX IDX_LATITUDE on CACHES (Latitude)
     * CREATE INDEX IDX_LONGITUDE on CACHES (Longitude)
     * CREATE INDEX IDX_SOURCE on CACHES (Source)
      * </pre>
      */
 
     public static final String DATABASE_NAME = "GeoBeagle.db";
 
    public static final int DATABASE_VERSION = 9;
     public static final String[] READER_COLUMNS = new String[] {
             "Latitude", "Longitude", "Id", "Description"
     };
     public static final String SQL_CLEAR_CACHES = "DELETE FROM CACHES WHERE Source=?";
     public static final String SQL_COUNT_CACHES = "SELECT COUNT(*) FROM CACHES";
     public static final String SQL_CREATE_CACHE_TABLE = "CREATE TABLE IF NOT EXISTS CACHES ("
             + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
             + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR)";
     public static final String SQL_CREATE_IDX_LATITUDE = "CREATE INDEX IF NOT EXISTS IDX_LATITUDE on CACHES (Latitude)";
     public static final String SQL_CREATE_IDX_LONGITUDE = "CREATE INDEX IF NOT EXISTS IDX_LONGITUDE on CACHES (Longitude)";
     public static final String SQL_CREATE_IDX_SOURCE = "CREATE INDEX IF NOT EXISTS IDX_SOURCE on CACHES (Source)";
    public static final String SQL_DELETE_ALL_IMPORTED_CACHES = "DELETE FROM CACHES WHERE Source != 'intent'";
     public static final String SQL_DELETE_CACHE = "DELETE FROM CACHES WHERE Id=?";
     public static final String SQL_DROP_CACHE_TABLE = "DROP TABLE CACHES";
     public static final String SQL_INSERT_CACHE = "INSERT INTO CACHES "
             + "(Id, Description, Latitude, Longitude, Source) " + "VALUES (?, ?, ?, ?, ?)";
     public static final String TBL_CACHES = "CACHES";
 
     public static Database create(Context context) {
         final OpenHelperDelegate openHelperDelegate = new OpenHelperDelegate();
         final GeoBeagleSqliteOpenHelper sqliteOpenHelper = new GeoBeagleSqliteOpenHelper(context,
                 openHelperDelegate);
         return new Database(sqliteOpenHelper);
     }
 
     private final SQLiteOpenHelper mSqliteOpenHelper;
 
     public Database(SQLiteOpenHelper sqliteOpenHelper) {
         mSqliteOpenHelper = sqliteOpenHelper;
     }
 
     public CacheWriter createCacheWriter(SQLiteWrapper sqliteWrapper) {
         return new CacheWriter(sqliteWrapper);
     }
 
     public SQLiteDatabase getReadableDatabase() {
         return mSqliteOpenHelper.getReadableDatabase();
     }
 
     public SQLiteDatabase getWritableDatabase() {
         return mSqliteOpenHelper.getWritableDatabase();
     }
 }
