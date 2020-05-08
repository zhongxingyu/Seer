 
 package com.michitsuchida.marketfavoritter.db;
 
 import static com.michitsuchida.marketfavoritter.util.StringUtils.splitWithCommaAndSpace;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.michitsuchida.marketfavoritter.main.AppElement;
 
 /**
  * データベースを開いたりレコードの取得、更新等を行うためのクラス。
  * 
  * @author MichiTsuchida
  */
 public class DBMainStore {
 
     /** LOG TAG */
     static final String LOG_TAG = "MarketBookmark";
 
     /** DBOpenHelperオブジェクト */
     private DBOpenHelper mHelper;
 
     /** SQLiteDatabaseオブジェクト */
     private SQLiteDatabase mDb;
 
     /** データベースのテーブル名:アプリDB */
     static final String TBL_NAME = "app";
 
     /** DBのカラム:ID */
     public static final String COLUMN_ID = "_id";
 
     /** アプリDBのカラム:アプリ名 */
     public static final String COLUMN_APP_NAME = "name";
 
     /** アプリDBのカラム:パッケージ名 */
     public static final String COLUMN_APP_PACKAGE = "pkg";
 
     /** アプリDBのカラム:URL */
     public static final String COLUMN_APP_URL = "url";
 
     /** アプリDBのカラム:ラベル */
     public static final String COLUMN_APP_LABEL = "label";
 
     /** テーブルのソートオーダー */
     public static final String ASC = "ASC";
 
     /** テーブルのソートオーダー */
     public static final String DESC = "DESC";
 
     /** データの件数のためのカウンタ */
     private int mCount = 0;
 
     /**
      * コンストラクタ。データベースのオブジェクトを取得する。
      * 
      * @param context コンテキスト
      * @param isUpdate レコードを更新するかどうか
      */
     public DBMainStore(Context context, boolean isUpdate) {
         mHelper = DBOpenHelper.getInstance(context);
         if (mHelper != null) {
             if (isUpdate) {
                 mDb = mHelper.getWritableDatabase();
             } else {
                 mDb = mHelper.getReadableDatabase();
             }
         } else {
             mDb = null;
         }
     }
 
     /**
      * データベースを閉じる。
      */
     public void close() {
         mDb.close();
     }
 
     /**
      * データベースにレコードを追加する。
      * 
      * @param name アプリ名
      * @param pkg パッケージ名
      * @param url URL
      */
     public void add(String name, String pkg, String url) {
         add(name, pkg, url, "");
     }
 
     /**
      * データベースにレコードを追加する。
      * 
      * @param name アプリ名
      * @param pkg パッケージ名
      * @param url URL
      * @param labels ラベル(カンマ区切り、複数OK)
      */
     public void add(String name, String pkg, String url, String labels) {
         ContentValues values = new ContentValues();
         values.put(COLUMN_APP_NAME, name);
         values.put(COLUMN_APP_PACKAGE, pkg);
         values.put(COLUMN_APP_URL, url);
 
         // ラベルをカンマのみの区切りに整形
         String label = splitWithCommaAndSpace(labels);
         values.put(COLUMN_APP_LABEL, label);
 
         // DBに追加する
         mDb.insert(TBL_NAME, null, values);
     }
 
     /**
      * データベースのレコードを更新する。
      * 
      * @param _id レコードのID
      * @param name アプリ名
      * @param pkg パッケージ名
      * @param url URL
      */
     public void update(int _id, String name, String pkg, String url, String labels) {
         ContentValues values = new ContentValues();
         values.put(COLUMN_APP_NAME, name);
         values.put(COLUMN_APP_PACKAGE, pkg);
         values.put(COLUMN_APP_URL, url);
 
         // ラベルをカンマのみの区切りに整形
         String label = splitWithCommaAndSpace(labels);
         values.put(COLUMN_APP_LABEL, label);
 
         mDb.update(TBL_NAME, values, COLUMN_ID + "=?", new String[] {
             Integer.toString(_id)
         });
     }
 
     /**
      * データベースからレコードを削除する。
      * 
      * @param レコードのIDを指定した配列
      */
     public void delete(String[] _ids) {
         for (int i = 0; i < _ids.length; i++) {
             mDb.delete(TBL_NAME, "_id=?", new String[] {
                 _ids[i]
             });
         }
     }
 
     /**
      * すべてのレコードをアプリデータベースから取得する。
      * 
      * @param order 並び順を指定しない場合は、nullをセットすること。
      *            並び順を指定する場合は、"pkg DESC"のように、"COLUMN_NAME ASC|DESC"と指定すること。
      * @return すべてのレコードが格納されたArrayList
      */
     public List<AppElement> fetchAllAppData(String order) {
         List<AppElement> data = new ArrayList<AppElement>();
         Cursor c = null;
         try {
             c = mDb.query(TBL_NAME, new String[] {
                     COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PACKAGE, COLUMN_APP_URL,
                     COLUMN_APP_LABEL
             }, null, null, null, null, order);
             c.moveToFirst();
             mCount = c.getCount();
             for (int i = 0; i < mCount; i++) {
                 AppElement elem = new AppElement(c.getString(1), c.getString(2), c.getString(3),
                         c.getString(4), c.getInt(0));
                 data.add(elem);
                 c.moveToNext();
             }
         } catch (SQLException e) {
             Log.e(LOG_TAG, "SQLException occurred..\n" + e.getMessage());
         } finally {
             if (c != null)
                 c.close();
         }
         return data;
     }
 
     /**
      * 特定のラベルを含むレコードをアプリデータベースから取得する。
      * 
      * @param filter フィルタリングするラベル
      * @param order 並び順を指定しない場合は、nullをセットすること。
      *            並び順を指定する場合は、"pkg DESC"のように、"COLUMN_NAME ASC|DESC"と指定すること。
      * @return 特定のレコードが格納されたArrayList
      */
     public List<AppElement> fetchFilteredAppData(String filter, String order) {
         List<AppElement> data = new ArrayList<AppElement>();
         String where = DBMainStore.COLUMN_APP_LABEL + " like ?";
        String param = "%" + filter + "%";
 
         Cursor c = null;
         try {
            // ラベル未設定の場合
             if (filter.equals("")) {
                 where = DBMainStore.COLUMN_APP_LABEL + "=\"" + filter + "\"";
                 c = mDb.query(TBL_NAME, new String[] {
                         COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PACKAGE, COLUMN_APP_URL,
                         COLUMN_APP_LABEL
                 }, where, null, null, null, order);
             } else {
                 c = mDb.query(TBL_NAME, new String[] {
                         COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PACKAGE, COLUMN_APP_URL,
                         COLUMN_APP_LABEL
                 }, where, new String[] {
                     param
                 }, null, null, order);
             }
             c.moveToFirst();
             mCount = c.getCount();
             for (int i = 0; i < mCount; i++) {
                 AppElement elem = new AppElement(c.getString(1), c.getString(2), c.getString(3),
                         c.getString(4), c.getInt(0));
                 data.add(elem);
                 c.moveToNext();
             }
         } catch (SQLException e) {
             Log.e(LOG_TAG, "SQLException occurred..\n" + e.getMessage());
         } finally {
             if (c != null)
                 c.close();
         }
         return data;
     }
 
     /**
      * カラム名と値を使用して、特定のレコードをアプリデータベースから取得する。
      * 
      * @param column カラム名
      * @param value 指定したカラムの特定の値
      * @return 指定したカラムの特定の値が含まれるレコード、見つからなかった場合はnull
      */
     public AppElement fetchAppDataByColumnAndValue(String column, String value) {
         AppElement data = new AppElement();
         String where = column + "=\"" + value + "\"";
         Cursor c = null;
         try {
             c = mDb.query(TBL_NAME, new String[] {
                     COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PACKAGE, COLUMN_APP_URL,
                     COLUMN_APP_LABEL
             }, where, null, null, null, null);
             c.moveToFirst();
             if (c.getCount() > 0) {
                 data = new AppElement(c.getString(1), c.getString(2), c.getString(3),
                         c.getString(4), c.getInt(0));
             } else {
                 return null;
             }
         } catch (SQLException e) {
             Log.e(LOG_TAG, "SQLException occurred..\n" + e.getMessage());
         } finally {
             if (c != null)
                 c.close();
         }
         return data;
     }
 
     /**
      * 特定のラベルを含むレコードの件数を取得する。
      * 
      * @param filter 件数を取得するラベル
      * @return 特定のラベルを含むレコードの件数
      */
     public int getAppCountOfLabel(String filter) {
         int count = 0;
         String where = DBMainStore.COLUMN_APP_LABEL + " like ?";
         String param = "%" + filter + "%";
         Cursor c = null;
         try {
             c = mDb.query(TBL_NAME, new String[] {
                     COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PACKAGE, COLUMN_APP_URL,
                     COLUMN_APP_LABEL
             }, where, new String[] {
                 param
             }, null, null, null);
             count = c.getCount();
         } catch (SQLException e) {
             Log.e(LOG_TAG, "SQLException occurred..\n" + e.getMessage());
         } finally {
             if (c != null)
                 c.close();
         }
         return count;
     }
 
     /**
      * アプリデータベースに格納されているレコードの件数を取得する。
      * リアルタイム更新ではなく、fetchAllAppData()かfetchFilteredAppData()が実行された際に更新される。
      * 
      * @return レコードの件数
      */
     public int getCount() {
         return mCount;
     }
 }
