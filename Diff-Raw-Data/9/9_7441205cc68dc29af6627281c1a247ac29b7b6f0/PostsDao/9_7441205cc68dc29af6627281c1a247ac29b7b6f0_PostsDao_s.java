 /* $Id: $
    Copyright 2012, G. Blake Meike
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.marakana.android.stream.db.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.marakana.android.stream.BuildConfig;
 import com.marakana.android.stream.db.ProjectionMap;
 import com.marakana.android.stream.db.ColumnMap;
 import com.marakana.android.stream.db.DbHelper;
 import com.marakana.android.stream.db.StreamContract;
 
 
 /**
  * Using URIs as foreign keys is really convenient, but pretty expensive, space-wise.
  *   Might have to fix that, at some point in the future.
  *
  * @version $Revision: $
  * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
  */
 public class PostsDao extends BaseDao {
     private static final String TAG = "POSTS-DAO";
 
     static final String TABLE = "posts";
     static final String COL_ID = "id";
 
     private static final String COL_URI = "uri";
     private static final String COL_TITLE = "title";
     private static final String COL_AUTHOR = "author";
     private static final String COL_DATE = "pub_date";
     private static final String COL_TAGS = "tags";
     private static final String COL_SUMMARY = "summary";
     private static final String COL_CONTENT = "content";
     private static final String COL_TYPE = "type";
     private static final String COL_THUMB = "thumb";
 
     private static final String CREATE_TABLE
         = "CREATE TABLE " + TABLE + " ("
             + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
             + COL_URI + " text UNIQUE,"
             + COL_TITLE + " text,"
             + COL_AUTHOR + " text REFERENCES " + AuthorsDao.TABLE + "(" + AuthorsDao.COL_URI + "),"
             + COL_DATE + " integer,"
             + COL_TAGS + " text,"
             + COL_SUMMARY + " text,"
             + COL_THUMB + " text REFERENCES " + ThumbsDao.TABLE + "(" + ThumbsDao.COL_URI + "),"
             + COL_TYPE + " text,"
             + COL_CONTENT + " text)";
 
     private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
 
     private static final String FEED_TABLE
         = TABLE + " INNER JOIN " + AuthorsDao.TABLE
             + " ON(" + TABLE + "." + COL_AUTHOR
                 + "=" + AuthorsDao.TABLE + "." + AuthorsDao.COL_URI + ")"
             + " LEFT OUTER JOIN " + ThumbsDao.TABLE
                 + " ON(" + TABLE + "." + COL_THUMB
                     + "=" + ThumbsDao.TABLE + "." + ThumbsDao.COL_URI + ")";
 
     private static final ProjectionMap FEED_COL_AS_MAP = new ProjectionMap.Builder()
         .addColumn(StreamContract.Feed.Columns.ID, TABLE, COL_ID)
         .addColumn(StreamContract.Feed.Columns.TITLE, TABLE, COL_TITLE)
         .addColumn(StreamContract.Feed.Columns.AUTHOR, AuthorsDao.TABLE, AuthorsDao.COL_NAME)
         .addColumn(StreamContract.Feed.Columns.PUB_DATE, TABLE, COL_DATE)
         .addColumn(StreamContract.Feed.Columns.SUMMARY, TABLE, COL_SUMMARY)
         .addColumn(StreamContract.Feed.Columns.TAGS, TABLE, COL_TAGS)
         .addColumn(StreamContract.Feed.Columns.CONTENT, TABLE, COL_CONTENT)
         .addColumn(StreamContract.Feed.Columns.THUMB, ThumbsDao.TABLE, ThumbsDao.COL_ID)
         .build();
 
     private static final String PK_CONSTRAINT = COL_ID + "=";
     private static final String DEFAULT_SORT = StreamContract.Posts.Columns.PUB_DATE + " DESC";
 
     /**
      * @param context
      * @param db
      */
     public static void dropTable(Context context, SQLiteDatabase db) {
         if (BuildConfig.DEBUG) { Log.d(TAG, "drop posts db: " + DROP_TABLE); }
         db.execSQL(DROP_TABLE);
     }
 
     /**
      * @param context
      * @param db
      */
     public static void initDb(Context context, SQLiteDatabase db) {
         if (BuildConfig.DEBUG) { Log.d(TAG, "create posts db: " + CREATE_TABLE); }
         db.execSQL(CREATE_TABLE);
     }
 
     /**
      * @param dbHelper
      */
     public PostsDao(DbHelper dbHelper) {
         super(
             TAG,
             dbHelper,
             TABLE,
             COL_ID,
             DEFAULT_SORT,
             new ColumnMap.Builder()
                 .addColumn(StreamContract.Posts.Columns.ID, COL_ID, ColumnMap.Type.LONG)
                 .addColumn(StreamContract.Posts.Columns.LINK, COL_URI, ColumnMap.Type.STRING)
                 .addColumn(StreamContract.Posts.Columns.TITLE, COL_TITLE, ColumnMap.Type.STRING)
                .addColumn(StreamContract.Posts.Columns.AUTHOR, COL_AUTHOR, ColumnMap.Type.LONG)
                .addColumn(StreamContract.Posts.Columns.PUB_DATE, COL_DATE, ColumnMap.Type.STRING)
                 .addColumn(StreamContract.Posts.Columns.TAGS, COL_TAGS, ColumnMap.Type.STRING)
                .addColumn(StreamContract.Posts.Columns.SUMMARY, COL_SUMMARY, ColumnMap.Type.LONG)
                .addColumn(StreamContract.Posts.Columns.LINK, COL_THUMB, ColumnMap.Type.STRING)
                 .addColumn(StreamContract.Posts.Columns.TYPE, COL_TYPE, ColumnMap.Type.STRING)
                 .addColumn(StreamContract.Posts.Columns.CONTENT, COL_CONTENT, ColumnMap.Type.STRING)
                 .build(),
             new ProjectionMap.Builder()
                 .addColumn(StreamContract.Posts.Columns.ID, COL_ID)
                 .addColumn(StreamContract.Posts.Columns.TITLE, COL_TITLE)
                 .addColumn(StreamContract.Posts.Columns.AUTHOR, COL_AUTHOR)
                 .addColumn(StreamContract.Posts.Columns.PUB_DATE, COL_DATE)
                 .addColumn(StreamContract.Posts.Columns.SUMMARY, COL_SUMMARY)
                 .addColumn(StreamContract.Posts.Columns.TAGS, COL_TAGS)
                 .addColumn(StreamContract.Posts.Columns.CONTENT, COL_CONTENT)
                 .addColumn(StreamContract.Posts.Columns.THUMB, COL_THUMB)
                 .addColumn(
                         StreamContract.Posts.Columns.MAX_PUB_DATE,
                         "MAX(" + StreamContract.Posts.Columns.PUB_DATE + ")")
                 .build());
     }
 
     /**
      * @param vals
      * @return the count of inserted rows
      */
     public int bulkInsert(ContentValues[] vals) {
         int n = 0;
 
         SQLiteDatabase db = getDb();
         try {
             db.beginTransaction();
             for (ContentValues row: vals) {
                 if (0 <= insert(row)) { n++; }
             }
             db.setTransactionSuccessful();
         }
         finally { db.endTransaction(); }
 
         return n;
     }
 
     /**
      * @param proj
      * @param sel
      * @param args
      * @param ord
      * @param pk
      * @return cursor
      */
     public Cursor queryFeed(String[] proj, String sel, String[] args, String ord, long pk) {
         SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
         qb.setStrict(true);
 
         qb.setProjectionMap(FEED_COL_AS_MAP.getProjectionMap());
 
         qb.setTables(FEED_TABLE);
 
         if (0 <= pk) { qb.appendWhere(PK_CONSTRAINT + pk); }
 
         if (TextUtils.isEmpty(ord)) { ord = DEFAULT_SORT; }
 
         return qb.query(getDb(), proj, sel, args, null, null, ord);
     }
 }
